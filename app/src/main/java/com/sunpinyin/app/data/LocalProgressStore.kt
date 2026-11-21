package com.sunpinyin.app.data

import android.content.Context
import com.sunpinyin.domain.Progress
import com.sunpinyin.domain.ProgressStore

class LocalProgressStore(context: Context) : ProgressStore {
    private val preferences = context.applicationContext
        .getSharedPreferences("pinyin-progress", Context.MODE_PRIVATE)

    override fun load(): Progress {
        val ids = preferences.getStringSet("completed", emptySet()).orEmpty()
        val starsString = preferences.getString("stars", "").orEmpty()
        val stars = starsString.split(",")
            .filter { it.contains(":") }
            .associate { entry ->
                val parts = entry.split(":")
                parts[0] to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
            }
        return Progress(ids, stars)
    }

    override fun save(progress: Progress) {
        val starsString = progress.starsByLesson.entries.joinToString(",") { "${it.key}:${it.value}" }
        preferences.edit()
            .putStringSet("completed", progress.completedLessonIds)
            .putString("stars", starsString)
            .apply()
    }
}
