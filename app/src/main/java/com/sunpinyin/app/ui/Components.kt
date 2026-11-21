package com.sunpinyin.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A large, springy button for children. */
@Composable
fun BigButton(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = GameColors.Primary,
    textColor: Color = Color.White,
    fontSize: Int = 24,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(90),
        label = "press"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) color else GameColors.Locked)
            .clickable(enabled = enabled) {
                pressed = true
                onClick()
                pressed = false
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) textColor else GameColors.TextMuted,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/** A star badge, filled when earned. */
@Composable
fun Star(earned: Boolean, size: Int = 22) {
    Text(
        text = if (earned) "★" else "☆",
        fontSize = size.sp,
        color = if (earned) GameColors.Warning else GameColors.TextMuted
    )
}

/** Simple game mascot drawn with shapes, no external art assets required. */
@Composable
fun Mascot(modifier: Modifier = Modifier, size: Int = 72) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(GameColors.Secondary, GameColors.Warning))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("•  •", fontSize = (size / 5).sp, color = GameColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("▽", fontSize = (size / 5).sp, color = GameColors.Text)
        }
    }
}

/** Section title used across screens. */
@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GameColors.Text)
}

/** A chip showing star count. */
@Composable
fun StarCounter(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(GameColors.PrimaryLight)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Star(true, 20)
        Spacer(Modifier.size(6.dp))
        Text("$count 颗星", fontWeight = FontWeight.Bold, color = GameColors.Primary, fontSize = 16.sp)
    }
}
