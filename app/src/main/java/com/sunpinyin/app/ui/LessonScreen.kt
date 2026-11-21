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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunpinyin.domain.Lesson
import com.sunpinyin.domain.PronunciationKey
import com.sunpinyin.domain.PronunciationPlayer

@Composable
fun LessonScreen(
    lesson: Lesson,
    player: PronunciationPlayer,
    engine: com.sunpinyin.domain.ExerciseEngine,
    echoRecorder: com.sunpinyin.app.audio.InMemoryRecorder,
    onStars: (Int) -> Unit,
    onBack: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("点读", "听音选拼音", "看拼音选字", "跟读")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BigButton("返回", color = GameColors.TextMuted, fontSize = 16) { onBack() }
            Spacer(Modifier.size(12.dp))
            Text("${lesson.title}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
        }

        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { index, label ->
                TabChip(label, selected = tab == index, modifier = Modifier.weight(1f)) { tab = index }
            }
        }
        Spacer(Modifier.height(14.dp))

        when (tab) {
            0 -> ReadAloudContent(lesson, player)
            1 -> ExerciseContent(
                questions = remember(lesson.id) { engine.listenAndChoosePinyin(lesson.id) },
                player = player,
                engine = engine,
                onStars = onStars
            )
            2 -> ExerciseContent(
                questions = remember(lesson.id) { engine.chooseCharacterForPinyin(lesson.id) },
                player = player,
                engine = engine,
                onStars = onStars
            )
            3 -> EchoContent(lesson, player, echoRecorder)
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) GameColors.Primary else GameColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) androidx.compose.ui.graphics.Color.White else GameColors.Text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ReadAloudContent(lesson: Lesson, player: PronunciationPlayer) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionTitle("拼音宝宝 · 点一点听发音")
        Spacer(Modifier.height(10.dp))
        ItemGrid(
            lesson.pinyinItems.map { it.label to (PronunciationKey.item(it.id) to it.speechText) },
            player
        )

        if (lesson.exampleCharacters.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("例字宝宝 · 听一听读什么")
            Spacer(Modifier.height(10.dp))
            ItemGrid(
                lesson.exampleCharacters.map {
                    it.character to (PronunciationKey.character(it.syllable, it.character) to it.speechText)
                },
                player
            )
        }
    }
}

@Composable
private fun ItemGrid(entries: List<Pair<String, Pair<String, String>>>, player: PronunciationPlayer) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, keyText) ->
                    BigButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        color = GameColors.PrimaryLight,
                        textColor = GameColors.Text,
                        fontSize = 28
                    ) { player.play(keyText.first, keyText.second) }
                }
            }
        }
    }
}
