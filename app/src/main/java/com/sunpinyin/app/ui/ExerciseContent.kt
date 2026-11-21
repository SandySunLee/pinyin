package com.sunpinyin.app.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunpinyin.domain.ExerciseEngine
import com.sunpinyin.domain.ExerciseQuestion
import com.sunpinyin.domain.PronunciationPlayer

@Composable
fun ExerciseContent(
    questions: List<ExerciseQuestion>,
    player: PronunciationPlayer,
    engine: ExerciseEngine,
    onStars: (Int) -> Unit
) {
    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("这一课还没有练习，先点读学习吧。", fontSize = 18.sp, color = GameColors.TextMuted)
        }
        return
    }

    var index by remember(questions) { mutableIntStateOf(0) }
    var correctCount by remember(questions) { mutableIntStateOf(0) }
    var selectedOption by remember(questions) { mutableStateOf<String?>(null) }
    var answered by remember(questions) { mutableStateOf(false) }
    var finished by remember(questions) { mutableStateOf(false) }

    if (finished) {
        val stars = when {
            correctCount >= questions.size -> 3
            correctCount >= questions.size / 2 -> 2
            else -> 1
        }
        // Award stars once when finishing.
        androidx.compose.runtime.LaunchedEffect(Unit) { onStars(stars) }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Mascot(size = 96)
                Spacer(Modifier.height(16.dp))
                Text("闯关成功！", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = GameColors.Primary)
                Spacer(Modifier.height(8.dp))
                Row { repeat(3) { i -> Star(i < stars, 36) } }
                Spacer(Modifier.height(8.dp))
                Text("答对了 $correctCount / ${questions.size} 题", fontSize = 18.sp, color = GameColors.TextMuted)
            }
        }
        return
    }

    val question = questions[index]

    // Play the question audio automatically when it appears.
    androidx.compose.runtime.LaunchedEffect(question.id) {
        playPrompt(player, question)
    }

    fun replayPrompt() = playPrompt(player, question)

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = (index + 1) / questions.size.toFloat(),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = GameColors.Primary,
            trackColor = GameColors.PrimaryLight
        )
        Spacer(Modifier.height(16.dp))
        Text("第 ${index + 1} / ${questions.size} 题", fontSize = 14.sp, color = GameColors.TextMuted)

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GameColors.PrimaryLight)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    question.promptText ?: "听一听",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.Text,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                BigButton("再听一次", color = GameColors.Secondary, fontSize = 18) {
                    replayPrompt()
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        question.options.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { option ->
                    val isCorrect = option.id == question.correctOptionId
                    val optionColor by animateColorAsState(
                        targetValue = when {
                            !answered -> GameColors.Card
                            option.id == selectedOption && isCorrect -> GameColors.Success
                            option.id == selectedOption && !isCorrect -> Color(0xFFFF6B6B)
                            else -> GameColors.Card
                        },
                        label = "optionColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(optionColor)
                            .then(
                                if (!answered) Modifier.clickable {
                                    selectedOption = option.id
                                    answered = true
                                    val result = engine.answer(question, option.id)
                                    if (result.correct) correctCount++
                                } else Modifier
                            )
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(option.display, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (answered) {
            Spacer(Modifier.height(8.dp))
            val last = index == questions.lastIndex
            BigButton(
                label = if (last) "完成闯关" else "下一题",
                modifier = Modifier.fillMaxWidth(),
                color = GameColors.Primary
            ) {
                if (last) finished = true
                else {
                    index++
                    selectedOption = null
                    answered = false
                }
            }
        }
    }
}

private fun playPrompt(player: PronunciationPlayer, question: ExerciseQuestion) {
    val sequence = question.promptSequence
    if (!sequence.isNullOrEmpty()) {
        player.playSequence(sequence, question.promptSequenceGapMs)
    } else {
        player.play(question.promptAudioKey, question.promptSpeechText)
    }
}
