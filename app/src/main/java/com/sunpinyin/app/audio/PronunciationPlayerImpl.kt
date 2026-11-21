package com.sunpinyin.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.sunpinyin.domain.PronunciationCue
import com.sunpinyin.domain.PronunciationPlayer
import java.util.Locale

/**
 * Plays a parent recording when one exists for the key; otherwise falls back
 * to TTS using the scheme's speech text (a Chinese character, not a bare letter).
 */
class PronunciationPlayerImpl(
    context: Context,
    private val recordingStore: ParentRecordingStore
) : PronunciationPlayer {
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var sequenceToken = 0

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                when (tts?.setLanguage(Locale.CHINA)) {
                    TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> ttsReady = false
                }
            }
        }
    }

    override fun play(audioKey: String, speechText: String) {
        sequenceToken++ // a single play cancels any pending sequence
        handler.removeCallbacksAndMessages(null)
        playCue(PronunciationCue(audioKey, speechText))
    }

    override fun playSequence(cues: List<PronunciationCue>, gapMs: Long) {
        val token = ++sequenceToken
        handler.removeCallbacksAndMessages(null)
        if (cues.isEmpty()) return
        playCueAtIndex(cues, 0, gapMs, token)
    }

    private fun playCueAtIndex(cues: List<PronunciationCue>, index: Int, gapMs: Long, token: Int) {
        if (token != sequenceToken || index >= cues.size) return
        playCue(cues[index])
        if (index + 1 < cues.size) {
            handler.postDelayed({ playCueAtIndex(cues, index + 1, gapMs, token) }, gapMs)
        }
    }

    private fun playCue(cue: PronunciationCue) {
        lastSpeechText = cue.speechText
        val file = recordingStore.recordingFile(cue.audioKey)
        if (file != null) {
            playFile(file.absolutePath)
        } else {
            speak(cue.speechText)
        }
    }

    private fun playFile(path: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener { it.release() }
            }
        } catch (e: Exception) {
            // Corrupt or unsupported recording: fall back to speech text.
            mediaPlayer?.release()
            mediaPlayer = null
            speakLastText()
        }
    }

    private var lastSpeechText: String = ""

    private fun speakLastText() {
        if (lastSpeechText.isNotBlank()) speak(lastSpeechText)
    }

    private fun speak(text: String) {
        val engine = tts ?: return
        if (!ttsReady || text.isBlank()) return
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pinyin-${System.nanoTime()}")
    }

    fun shutdown() {
        sequenceToken++
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        tts?.shutdown()
    }
}
