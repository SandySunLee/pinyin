package com.sunpinyin.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunpinyin.domain.Course
import com.sunpinyin.domain.ExerciseEngine
import com.sunpinyin.domain.PronunciationPlayer

@Composable
fun BlendScreen(
    course: Course,
    player: PronunciationPlayer,
    engine: ExerciseEngine,
    onStars: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BigButton("返回", color = GameColors.TextMuted, fontSize = 16) { onBack() }
            Spacer(Modifier.size(12.dp))
            Text("拼读桥", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
        }
        Spacer(Modifier.height(8.dp))
        Text("声母和韵母手拉手，一起拼成音节", fontSize = 16.sp, color = GameColors.TextMuted)
        Spacer(Modifier.height(16.dp))
        SectionTitle("小测验 · 选出正确的拼读")
        Spacer(Modifier.height(10.dp))
        ExerciseContent(
            questions = remember(course) { engine.blendExercises() },
            player = player,
            engine = engine,
            onStars = onStars
        )
    }
}
