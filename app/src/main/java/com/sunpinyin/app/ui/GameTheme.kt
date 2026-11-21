package com.sunpinyin.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object GameColors {
    val Background = Color(0xFFF7F3FF)
    val Primary = Color(0xFF7B5CFF)
    val PrimaryLight = Color(0xFFEDE7FF)
    val Secondary = Color(0xFFFF8A3D)
    val Success = Color(0xFF35C47C)
    val Warning = Color(0xFFFFB020)
    val Text = Color(0xFF2B2244)
    val TextMuted = Color(0xFF8A80A6)
    val Card = Color(0xFFFFFFFF)
    val Locked = Color(0xFFE4E0EC)
}

private val gameColorScheme = lightColorScheme(
    primary = GameColors.Primary,
    onPrimary = Color.White,
    primaryContainer = GameColors.PrimaryLight,
    secondary = GameColors.Secondary,
    onSecondary = Color.White,
    background = GameColors.Background,
    onBackground = GameColors.Text,
    surface = GameColors.Card,
    onSurface = GameColors.Text
)

@Composable
fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = gameColorScheme, content = content)
}
