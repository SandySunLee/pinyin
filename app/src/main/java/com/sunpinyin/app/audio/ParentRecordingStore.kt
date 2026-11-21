package com.sunpinyin.app.audio

import android.content.Context
import com.sunpinyin.domain.AudioKey
import java.io.File

/**
 * Stores parent-provided standard recordings in app-private storage.
 * Keys are sanitized to safe file names; no storage permission is required.
 * These are distinct from child echo recordings, which stay in memory only.
 */
class ParentRecordingStore(context: Context) {
    private val directory = File(context.applicationContext.filesDir, "parent_audio").apply { mkdirs() }

    /** Directory used as scratch space by the recorder before saving. */
    val recorderDirectory: File get() = directory

    private fun fileForKey(key: String): File =
        File(directory, AudioKey.fileName(key, "3gp"))

    fun hasRecording(key: String): Boolean = fileForKey(key).exists()

    fun recordingFile(key: String): File? = fileForKey(key).takeIf { it.exists() }

    fun save(key: String, source: File): Boolean {
        return try {
            val target = fileForKey(key)
            source.inputStream().use { input -> target.outputStream().use { input.copyTo(it) } }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun delete(key: String): Boolean = fileForKey(key).delete()

    fun recordedKeys(): Set<String> =
        directory.listFiles { file -> file.extension == "3gp" }
            ?.map { it.nameWithoutExtension }
            ?.toSet()
            ?: emptySet()
}
