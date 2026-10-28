package com.physlab.electric.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/**
 * 仪器库中的现实器材缩略图。
 * 画布使用教材示意图，仪器库使用现实器材外观。
 */
fun DrawScope.drawHouseholdThumbnail(type: HouseholdDeviceType) {
    val unit = min(size.width, size.height) / 100f
    val center = Offset(size.width / 2f, size.height / 2f)
    fun p(x: Float, y: Float) = center + Offset(x * unit, y * unit)
    fun s(v: Float) = v * unit
    when (type) {
        HouseholdDeviceType.LAMP -> {
            drawCircle(Color(0xFFFFD35C).copy(alpha = 0.18f), s(28f), center + Offset(0f, -8f * unit))
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFF5CC), Color(0xFFFFD35C), Color(0xFFB98A2E)),
                    center = center + Offset(-5f * unit, -14f * unit),
                    radius = s(22f),
                ),
                radius = s(20f),
                center = center + Offset(0f, -8f * unit),
            )
            drawCircle(Color(0xFF5B626C), s(20f), center + Offset(0f, -8f * unit), style = Stroke(s(1.4f)))
            drawLine(Color(0xFF695B45), p(0f, 10f), p(0f, 24f), s(7f))
            drawRoundRect(
                Color(0xFFC0A26A),
                p(-13f, 24f),
                Size(s(26f), s(8f)),
                CornerRadius(s(3f)),
            )
        }
        HouseholdDeviceType.SWITCH -> {
            drawRoundRect(Color(0xFFE8EEF4), p(-30f, -22f), Size(s(60f), s(44f)), CornerRadius(s(5f)))
            drawRoundRect(Color(0xFFB9C6D2), p(-30f, -22f), Size(s(60f), s(44f)), CornerRadius(s(5f)), style = Stroke(s(1f)))
            drawRoundRect(Color(0xFFF8FBFE), p(-17f, -14f), Size(s(34f), s(28f)), CornerRadius(s(4f)))
            drawLine(Color(0xFF8FA3B5), p(0f, -11f), p(0f, 11f), s(1.2f))
        }
        HouseholdDeviceType.SOCKET2, HouseholdDeviceType.SOCKET3 -> {
            drawRoundRect(Color(0xFFF1F5F9), p(-28f, -28f), Size(s(56f), s(56f)), CornerRadius(s(6f)))
            drawRoundRect(Color(0xFFB9C6D2), p(-28f, -28f), Size(s(56f), s(56f)), CornerRadius(s(6f)), style = Stroke(s(1f)))
            drawCircle(Color(0xFF2D3B49), s(5f), center + Offset(-12f * unit, if (type == HouseholdDeviceType.SOCKET3) 2f * unit else 4f * unit))
            drawCircle(Color(0xFF2D3B49), s(5f), center + Offset(12f * unit, if (type == HouseholdDeviceType.SOCKET3) 2f * unit else 4f * unit))
            if (type == HouseholdDeviceType.SOCKET3) {
                drawCircle(Color(0xFF2D3B49), s(5f), center + Offset(0f, -16f * unit))
            }
        }
        HouseholdDeviceType.KETTLE -> {
            drawRoundRect(Color(0xFFD8E2EA), p(-24f, -20f), Size(s(42f), s(46f)), CornerRadius(s(8f)))
            drawRoundRect(Color(0xFF8FA3B5), p(-24f, -20f), Size(s(42f), s(46f)), CornerRadius(s(8f)), style = Stroke(s(1f)))
            drawArc(Color(0xFF4C5C6B), 250f, 160f, false, p(12f, -18f), Size(s(30f), s(34f)), style = Stroke(s(4f)))
            drawLine(Color(0xFF8FA3B5), p(-24f, -18f), p(-36f, -30f), s(4f))
            drawRoundRect(Color(0xFF465563), p(-28f, 26f), Size(s(50f), s(7f)), CornerRadius(s(3f)))
            drawCircle(Color(0xFF7DE0C0), s(3f), p(-12f, -8f))
        }
        HouseholdDeviceType.RICE_COOKER -> {
            drawRoundRect(Color(0xFFE8EEF4), p(-30f, -12f), Size(s(60f), s(34f)), CornerRadius(s(12f)))
            drawRoundRect(Color(0xFFB9C6D2), p(-30f, -12f), Size(s(60f), s(34f)), CornerRadius(s(12f)), style = Stroke(s(1f)))
            drawArc(Color(0xFFCBD6DF), 180f, 180f, true, p(-28f, -30f), Size(s(56f), s(34f)))
            drawRoundRect(Color(0xFF4C5C6B), p(-22f, -36f), Size(s(20f), s(8f)), CornerRadius(s(3f)))
            drawCircle(Color(0xFFFF8A92), s(3f), p(18f, 8f))
        }
        HouseholdDeviceType.METER -> {
            drawRoundRect(Color(0xFFE8DFC8), p(-32f, -28f), Size(s(64f), s(56f)), CornerRadius(s(5f)))
            drawRoundRect(Color(0xFFB9AA8A), p(-32f, -28f), Size(s(64f), s(56f)), CornerRadius(s(5f)), style = Stroke(s(1f)))
            drawRoundRect(Color(0xFFBFE8C4), p(-23f, -19f), Size(s(46f), s(18f)), CornerRadius(s(2f)))
            drawRoundRect(Color(0xFF7D91A8), p(-23f, 5f), Size(s(46f), s(7f)), CornerRadius(s(2f)))
        }
        HouseholdDeviceType.MAIN -> {
            drawRoundRect(Color(0xFFB9C6D2), p(-24f, -30f), Size(s(48f), s(60f)), CornerRadius(s(4f)))
            drawRoundRect(Color(0xFFE8EEF4), p(-15f, -25f), Size(s(30f), s(50f)), CornerRadius(s(3f)))
            drawRoundRect(Color(0xFF4C5C6B), p(-8f, -12f), Size(s(16f), s(24f)), CornerRadius(s(2f)))
        }
        else -> Unit
    }
}
