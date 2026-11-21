package com.sunpinyin.app.audio

import android.media.MediaRecorder
import com.sunpinyin.domain.AudioKey
import java.io.File

/** Records a parent's standard pronunciation to a WAV file in private storage. */
class ParentAudioRecorder(private val outputDirectory: File) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    val isRecording: Boolean get() = recorder != null

    fun start(key: String): File {
        val file = File(outputDirectory, AudioKey.fileName(key, "tmp"))
        val mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        currentFile = file
        return file
    }

    fun stop(): File? {
        val mediaRecorder = recorder ?: return null
        try {
            mediaRecorder.stop()
        } catch (_: RuntimeException) {
        }
        mediaRecorder.release()
        recorder = null
        return currentFile
    }

    fun cancel() {
        stop()
        currentFile?.delete()
        currentFile = null
    }
}
