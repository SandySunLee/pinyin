package com.sunpinyin.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunpinyin.domain.Course
import com.sunpinyin.domain.ExerciseEngine
import com.sunpinyin.domain.PronunciationKey
import com.sunpinyin.domain.PronunciationPlayer

@Composable
fun ToneScreen(
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
            Text("声调屋", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
        }
        Spacer(Modifier.height(8.dp))
        Text("汉语拼音有四声，点一点听一听", fontSize = 16.sp, color = GameColors.TextMuted)

        Spacer(Modifier.height(16.dp))
        course.toneDemos.forEach { tone ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GameColors.Card)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(GameColors.PrimaryLight)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("${tone.mark} ${tone.toneName}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GameColors.Primary)
                    }
                    Spacer(Modifier.size(16.dp))
                    Text(
                        "${tone.exampleCharacter} (${tone.exampleSyllable})",
                        fontSize = 22.sp,
                        color = GameColors.Text,
                        modifier = Modifier.weight(1f)
                    )
                    BigButton("听", color = GameColors.Secondary, fontSize = 16) {
                        player.play(PronunciationKey.tone(tone.exampleSyllable), tone.exampleCharacter)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("小测验 · 听音选声调")
        Spacer(Modifier.height(10.dp))
        ExerciseContent(
            questions = remember(course) { engine.toneExercises() },
            player = player,
            engine = engine,
            onStars = onStars
        )
    }
}
