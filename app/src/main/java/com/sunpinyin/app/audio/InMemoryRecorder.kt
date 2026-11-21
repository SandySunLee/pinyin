package com.sunpinyin.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

data class Recording(val data: ByteArray, val peakAmplitude: Int, val durationMs: Long)

/** Records PCM into memory only. Nothing is written to storage. */
class InMemoryRecorder {
    private val sampleRate = 16_000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, encoding)
        .coerceAtLeast(sampleRate / 2)

    private var record: AudioRecord? = null
    private var reader: Thread? = null
    private val pcm = ByteArrayOutputStream()
    private var startedAt = 0L

    val isRecording: Boolean
        get() = record?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    fun start() {
        if (isRecording) return
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            encoding,
            bufferSize
        )
        recorder.startRecording()
        record = recorder
        pcm.reset()
        startedAt = System.currentTimeMillis()
        reader = thread(name = "pinyin-recorder") {
            val buffer = ByteArray(bufferSize)
            while (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) pcm.write(buffer, 0, read)
            }
        }
    }

    fun stop(): Recording? {
        val recorder = record ?: return null
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            recorder.stop()
        }
        reader?.join(1_000)
        recorder.release()
        record = null
        reader = null
        val data = pcm.toByteArray()
        var peak = 0
        var i = 0
        while (i + 1 < data.size) {
            val sample = (data[i].toInt() and 0xFF) or (data[i + 1].toInt() shl 8)
            val magnitude = kotlin.math.abs(sample)
            if (magnitude > peak) peak = magnitude
            i += 2
        }
        return Recording(data, peak, System.currentTimeMillis() - startedAt)
    }

    fun playBack(recording: Recording) {
        if (recording.data.isEmpty()) return
        val track = AudioTrack(
            android.media.AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            encoding,
            recording.data.size.coerceAtLeast(bufferSize),
            AudioTrack.MODE_STATIC
        )
        track.write(recording.data, 0, recording.data.size)
        track.play()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            { track.release() },
            recording.durationMs + 200
        )
    }
}
