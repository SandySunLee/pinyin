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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sunpinyin.app.audio.ParentAudioRecorder
import com.sunpinyin.app.audio.ParentRecordingStore
import com.sunpinyin.domain.Course
import com.sunpinyin.domain.PronunciationCategory
import com.sunpinyin.domain.PronunciationEntry
import com.sunpinyin.domain.PronunciationMap

/** Parent gate and recording management. Recordings are saved in app-private storage. */
@Composable
fun ParentScreen(
    course: Course,
    store: ParentRecordingStore,
    recorder: ParentAudioRecorder,
    player: com.sunpinyin.domain.PronunciationPlayer,
    onBack: () -> Unit
) {
    var unlocked by remember { mutableStateOf(false) }

    if (!unlocked) {
        ParentGate(onUnlock = { unlocked = true }, onBack = onBack)
        return
    }
    ParentRecordingContent(course, store, recorder, player, onBack)
}

@Composable
private fun ParentGate(onUnlock: () -> Unit, onBack: () -> Unit) {
    var answer by remember { mutableStateOf("") }
    val correct = "8"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("家长入口", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
        Spacer(Modifier.height(12.dp))
        Text("请回答：6 + 2 = ?", fontSize = 22.sp, color = GameColors.Text)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("6", "8", "9").forEach { option ->
                BigButton(option, color = GameColors.PrimaryLight, textColor = GameColors.Text, fontSize = 22) {
                    answer = option
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (answer.isNotEmpty()) {
            Text(if (answer == correct) "回答正确" else "再想一想", color = GameColors.TextMuted, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
        BigButton("进入", color = if (answer == correct) GameColors.Primary else GameColors.Locked, enabled = answer == correct) { onUnlock() }
        Spacer(Modifier.height(12.dp))
        BigButton("返回", color = GameColors.TextMuted, fontSize = 16) { onBack() }
    }
}

@Composable
private fun ParentRecordingContent(
    course: Course,
    store: ParentRecordingStore,
    recorder: ParentAudioRecorder,
    player: com.sunpinyin.domain.PronunciationPlayer,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pronunciationMap = remember(course) { PronunciationMap(course) }
    var selected by remember { mutableStateOf<PronunciationEntry?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("请选择要录制的内容") }
    var refresh by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) status = "需要麦克风权限才能录制"
    }

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
            Text("录制标准音", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "按类别选择内容录制。孩子点击时会优先播放您的录音，未录制的内容使用系统语音。✓ 表示已录制。",
            fontSize = 14.sp,
            color = GameColors.TextMuted
        )
        Spacer(Modifier.height(12.dp))

        RECORDING_SECTIONS.forEach { (category, title) ->
            val entries = pronunciationMap.entries.filter { it.category == category }
            if (entries.isNotEmpty()) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GameColors.Primary)
                Spacer(Modifier.height(6.dp))
                entries.chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { entry ->
                            val recorded = remember(refresh, entry.key) { store.hasRecording(entry.key) }
                            BigButton(
                                label = if (recorded) "${entry.label}✓" else entry.label,
                                modifier = Modifier.weight(1f),
                                color = if (selected?.key == entry.key) GameColors.Primary else GameColors.Card,
                                textColor = if (selected?.key == entry.key) androidx.compose.ui.graphics.Color.White else GameColors.Text,
                                fontSize = 18
                            ) { selected = entry }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        selected?.let { entry ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GameColors.PrimaryLight)
                    .padding(16.dp)
            ) {
                Column {
                    Text("当前：${entry.label}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
                    Spacer(Modifier.height(6.dp))
                    Text("提示词：请清楚朗读「${entry.speechText}」", fontSize = 16.sp, color = GameColors.Text)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigButton("试听", Modifier.weight(1f), color = GameColors.Secondary, fontSize = 16) {
                            player.play(entry.key, entry.speechText)
                        }
                        BigButton(
                            if (isRecording) "停止" else "录制",
                            Modifier.weight(1f),
                            color = if (isRecording) GameColors.Warning else GameColors.Primary,
                            fontSize = 16
                        ) {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else if (isRecording) {
                                val file = recorder.stop()
                                if (file != null && store.save(entry.key, file)) {
                                    status = "已保存「${entry.label}」的录音"
                                } else {
                                    status = "保存失败，请重试"
                                }
                                file?.delete()
                                isRecording = false
                                refresh++
                            } else {
                                recorder.start(entry.key)
                                isRecording = true
                                status = "正在录制「${entry.speechText}」…"
                            }
                        }
                        BigButton("删除", Modifier.weight(1f), color = GameColors.TextMuted, fontSize = 16) {
                            if (store.delete(entry.key)) {
                                status = "已删除「${entry.label}」的录音"
                                refresh++
                            } else {
                                status = "该内容尚未录制"
                            }
                        }
                    }
                }
            }
        } ?: run {
            Text("请先选择一个内容", fontSize = 16.sp, color = GameColors.TextMuted)
        }

        Spacer(Modifier.height(12.dp))
        Text(status, fontSize = 16.sp, color = GameColors.Primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

private val RECORDING_SECTIONS = listOf(
    PronunciationCategory.PINYIN_ITEM to "拼音项",
    PronunciationCategory.EXAMPLE_CHARACTER to "例字",
    PronunciationCategory.SYLLABLE to "音节",
    PronunciationCategory.TONE to "声调示范",
    PronunciationCategory.BLEND_PART to "拼读部件"
)
