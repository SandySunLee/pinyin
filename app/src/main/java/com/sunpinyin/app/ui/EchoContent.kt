package com.sunpinyin.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sunpinyin.app.audio.InMemoryRecorder
import com.sunpinyin.app.audio.Recording
import com.sunpinyin.domain.Lesson
import com.sunpinyin.domain.PronunciationKey
import com.sunpinyin.domain.PronunciationPlayer
import com.sunpinyin.domain.RhythmHintEvaluator

@Composable
fun EchoContent(lesson: Lesson, player: PronunciationPlayer, recorder: InMemoryRecorder) {
    val context = LocalContext.current
    val targets: List<Triple<String, String, String>> =
        lesson.exampleCharacters.map {
            Triple(it.character, PronunciationKey.character(it.syllable, it.character), it.speechText)
        }.ifEmpty {
            lesson.pinyinItems.map { Triple(it.label, PronunciationKey.item(it.id), it.speechText) }
        }

    var selected by remember(lesson) { mutableStateOf(targets.firstOrNull()) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) hint = "需要麦克风权限才能跟读哦"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("选一个，跟着读一读")
        Spacer(Modifier.height(10.dp))
        targets.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { target ->
                    BigButton(
                        label = target.first,
                        modifier = Modifier.weight(1f),
                        color = if (selected?.second == target.second) GameColors.Primary else GameColors.Card,
                        textColor = if (selected?.second == target.second) androidx.compose.ui.graphics.Color.White else GameColors.Text,
                        fontSize = 28
                    ) { selected = target }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(10.dp))
        selected?.let { target ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigButton("听标准音", Modifier.weight(1f), color = GameColors.Secondary, fontSize = 18) {
                    player.play(target.second, target.third)
                }
                BigButton(
                    if (isRecording) "停止录音" else "开始跟读",
                    Modifier.weight(1f),
                    color = GameColors.Primary,
                    fontSize = 18
                ) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else if (isRecording) {
                        val result = recorder.stop()
                        recording = result
                        isRecording = false
                        if (result != null) {
                            hint = RhythmHintEvaluator.evaluate(result.peakAmplitude, result.durationMs).message +
                                "（这是音量与时长提示，不是发音评分）"
                        }
                    } else {
                        recorder.start()
                        isRecording = true
                        hint = "正在录音，请读「${target.first}」…"
                    }
                }
            }
        }

        if (recording != null) {
            Spacer(Modifier.height(12.dp))
            BigButton("回放我的声音", Modifier.fillMaxWidth(), color = GameColors.PrimaryLight, textColor = GameColors.Text, fontSize = 18) {
                recorder.playBack(recording!!)
            }
        }

        if (hint.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameColors.PrimaryLight)
                    .padding(16.dp)
            ) {
                Text(hint, fontSize = 16.sp, color = GameColors.Text, textAlign = TextAlign.Center)
            }
        }
    }
}
