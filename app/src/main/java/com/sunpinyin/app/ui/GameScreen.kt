package com.sunpinyin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunpinyin.domain.Course
import com.sunpinyin.domain.Lesson
import com.sunpinyin.domain.Progress

/** Which view is currently open. */
sealed interface GameDestination {
    data object Home : GameDestination
    data class Lesson(val lessonId: String) : GameDestination
    data object Tone : GameDestination
    data object Blend : GameDestination
    data object Parent : GameDestination
}

@Composable
fun GameHomeScreen(
    course: Course,
    progress: Progress,
    onLesson: (String) -> Unit,
    onTone: () -> Unit,
    onBlend: () -> Unit,
    onParent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Mascot(size = 64)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("孙少阳学拼音", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
                Text("一起闯关学拼音吧！", fontSize = 15.sp, color = GameColors.TextMuted)
            }
            StarCounter(progress.totalStars)
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton("声调屋", Modifier.weight(1f), color = GameColors.Secondary, onClick = onTone)
            BigButton("拼读桥", Modifier.weight(1f), color = GameColors.Primary, onClick = onBlend)
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("拼音闯关地图")
        Spacer(Modifier.height(10.dp))

        course.units.forEachIndexed { unitIndex, unit ->
            Text(
                "第${unitIndex + 1}关区 · ${unit.title}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                unit.lessons.forEach { lesson ->
                    LevelNode(
                        lesson = lesson,
                        stars = progress.starsByLesson[lesson.id] ?: 0,
                        completed = lesson.id in progress.completedLessonIds,
                        modifier = Modifier.weight(1f),
                        onClick = { onLesson(lesson.id) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(12.dp))
        BigButton(
            "家长设置 · 录制标准音",
            Modifier.fillMaxWidth(),
            color = GameColors.TextMuted,
            fontSize = 18,
            onClick = onParent
        )
    }
}

@Composable
private fun LevelNode(
    lesson: Lesson,
    stars: Int,
    completed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (completed) GameColors.PrimaryLight else GameColors.Card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                lesson.title.substringBefore(" "),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Text
            )
            Spacer(Modifier.height(4.dp))
            Row {
                repeat(3) { index -> Star(index < stars, 16) }
            }
        }
    }
}
