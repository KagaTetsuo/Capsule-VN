package com.akira.island.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Bảng màu thương hiệu Capsule VN: đen – đỏ – vàng kim. */
object Ui {
    val Bg = Color(0xFF0E0B0C)
    val Card = Color(0xFF1F1719)
    val IconBg = Color(0xFF33262A)
    val Primary = Color(0xFFE6C068)
    val OnPrimary = Color(0xFF2A1B05)
    val Red = Color(0xFFC8102E)
    val Text = Color(0xFFF3ECE4)
    val Sub = Color(0xFFB3A69F)
    val Divider = Color(0xFF4A3A3E)
    val Nav = Color(0xFF1C1416)
    val Pink = Color(0xFFFBDDF4)
    val Outline = Color(0xFF9A8A84)
}

object Tile {
    val Blue = Color(0xFF0A84FF)
    val Green = Color(0xFF34C759)
    val Orange = Color(0xFFFF9F0A)
    val Red = Color(0xFFFF453A)
    val Purple = Color(0xFFBF5AF2)
    val Pink = Color(0xFFFF2D78)
    val Teal = Color(0xFF30B0C7)
    val Indigo = Color(0xFF5E5CE6)
    val Yellow = Color(0xFFFFB800)
    val Gray = Color(0xFF8E8E93)
}

@Composable
fun IslandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Ui.Primary,
            onPrimary = Ui.OnPrimary,
            secondary = Ui.Red,
            background = Ui.Bg,
            onBackground = Ui.Text,
            surface = Ui.Card,
            onSurface = Ui.Text,
            surfaceVariant = Ui.IconBg,
            onSurfaceVariant = Ui.Sub,
            surfaceContainerHigh = Ui.Card,
            outline = Ui.Outline,
        ),
        typography = InterTypography,
        content = content,
    )
}
