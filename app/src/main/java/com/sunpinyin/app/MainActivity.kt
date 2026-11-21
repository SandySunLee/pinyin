package com.sunpinyin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.sunpinyin.app.audio.InMemoryRecorder
import com.sunpinyin.app.audio.ParentAudioRecorder
import com.sunpinyin.app.audio.ParentRecordingStore
import com.sunpinyin.app.audio.PronunciationPlayerImpl
import com.sunpinyin.app.data.JsonCourseRepository
import com.sunpinyin.app.data.LocalProgressStore
import com.sunpinyin.app.ui.BlendScreen
import com.sunpinyin.app.ui.GameDestination
import com.sunpinyin.app.ui.GameHomeScreen
import com.sunpinyin.app.ui.GameTheme
import com.sunpinyin.app.ui.LessonScreen
import com.sunpinyin.app.ui.ParentScreen
import com.sunpinyin.app.ui.ToneScreen
import com.sunpinyin.domain.ExerciseEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PinyinApp()
                }
            }
        }
    }
}

@Composable
fun PinyinApp() {
    val context = LocalContext.current
    val course = remember { JsonCourseRepository(context).loadCourse() }
    val progressStore = remember { LocalProgressStore(context) }
    val recordingStore = remember { ParentRecordingStore(context) }
    val player = remember { PronunciationPlayerImpl(context, recordingStore) }
    val parentRecorder = remember { ParentAudioRecorder(recordingStore.recorderDirectory) }
    val echoRecorder = remember { InMemoryRecorder() }
    val engine = remember(course) { ExerciseEngine(course) }

    DisposableEffect(player, parentRecorder) {
        onDispose {
            player.shutdown()
            parentRecorder.cancel()
        }
    }

    var progress by remember { mutableStateOf(progressStore.load()) }
    var destination by remember { mutableStateOf<GameDestination>(GameDestination.Home) }

    fun saveProgress(updated: com.sunpinyin.domain.Progress) {
        progress = updated
        progressStore.save(updated)
    }

    when (val dest = destination) {
        GameDestination.Home -> GameHomeScreen(
            course = course,
            progress = progress,
            onLesson = { destination = GameDestination.Lesson(it) },
            onTone = { destination = GameDestination.Tone },
            onBlend = { destination = GameDestination.Blend },
            onParent = { destination = GameDestination.Parent }
        )
        is GameDestination.Lesson -> {
            val lesson = course.lessons.first { it.id == dest.lessonId }
            LessonScreen(
                lesson = lesson,
                player = player,
                engine = engine,
                echoRecorder = echoRecorder,
                onStars = { stars -> saveProgress(progress.awardStars(lesson.id, stars)) },
                onBack = { destination = GameDestination.Home }
            )
        }
        GameDestination.Tone -> ToneScreen(
            course = course,
            player = player,
            engine = engine,
            onStars = { stars -> saveProgress(progress.awardStars("tone-house", stars)) },
            onBack = { destination = GameDestination.Home }
        )
        GameDestination.Blend -> BlendScreen(
            course = course,
            player = player,
            engine = engine,
            onStars = { stars -> saveProgress(progress.awardStars("blend-bridge", stars)) },
            onBack = { destination = GameDestination.Home }
        )
        GameDestination.Parent -> ParentScreen(
            course = course,
            store = recordingStore,
            recorder = parentRecorder,
            player = player,
            onBack = { destination = GameDestination.Home }
        )
    }
}
