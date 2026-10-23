package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

enum class LabIcon {
    UNDO, REDO, TRASH, CLOSE, CHEVRON_LEFT, CHEVRON_RIGHT, CIRCUIT, MORE,
    WIRE, HELP, LIGHTNING, EXAMPLE, INFO, CHECK, FOLDER, SWITCH, RESET,
}

@Composable
fun LabIcon(
    icon: LabIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val s = minOf(size.width, size.height)
        val stroke = (s * 0.09f).coerceAtLeast(1.4f)
        val center = Offset(size.width / 2f, size.height / 2f)
        when (icon) {
            LabIcon.UNDO, LabIcon.REDO -> {
                val flip = if (icon == LabIcon.REDO) -1f else 1f
                val path = Path().apply {
                    moveTo(center.x + 8f * s / 24f * flip, center.y + 5f * s / 24f)
                    cubicTo(
                        center.x - 7f * s / 24f * flip, center.y + 11f * s / 24f,
                        center.x - 8f * s / 24f * flip, center.y - 7f * s / 24f,
                        center.x + 1f * s / 24f * flip, center.y - 8f * s / 24f,
                    )
                }
                drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round))
                val tip = Offset(center.x + 1f * s / 24f * flip, center.y - 8f * s / 24f)
                drawLine(tint, tip, tip + Offset(-5f * s / 24f * flip, -1f * s / 24f), stroke, StrokeCap.Round)
                drawLine(tint, tip, tip + Offset(-2f * s / 24f * flip, 5f * s / 24f), stroke, StrokeCap.Round)
            }

            LabIcon.TRASH -> {
                drawLine(tint, Offset(center.x - 8f * s / 24f, center.y - 7f * s / 24f), Offset(center.x + 8f * s / 24f, center.y - 7f * s / 24f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(center.x - 3f * s / 24f, center.y - 10f * s / 24f), Offset(center.x + 3f * s / 24f, center.y - 10f * s / 24f), stroke, StrokeCap.Round)
                drawRoundRect(
                    tint,
                    topLeft = Offset(center.x - 6f * s / 24f, center.y - 5f * s / 24f),
                    size = Size(12f * s / 24f, 15f * s / 24f),
                    cornerRadius = CornerRadius(2f * s / 24f),
                    style = Stroke(stroke),
                )
                drawLine(tint, Offset(center.x - 2f * s / 24f, center.y - 1f * s / 24f), Offset(center.x - 2f * s / 24f, center.y + 7f * s / 24f), stroke * 0.8f, StrokeCap.Round)
                drawLine(tint, Offset(center.x + 2f * s / 24f, center.y - 1f * s / 24f), Offset(center.x + 2f * s / 24f, center.y + 7f * s / 24f), stroke * 0.8f, StrokeCap.Round)
            }

            LabIcon.CLOSE -> {
                drawLine(tint, center + Offset(-7f, -7f) * s / 24f, center + Offset(7f, 7f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(7f, -7f) * s / 24f, center + Offset(-7f, 7f) * s / 24f, stroke, StrokeCap.Round)
            }

            LabIcon.CHEVRON_LEFT, LabIcon.CHEVRON_RIGHT -> {
                val dir = if (icon == LabIcon.CHEVRON_LEFT) -1f else 1f
                drawLine(tint, center + Offset(-2f * dir, -7f) * s / 24f, center + Offset(5f * dir, 0f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(5f * dir, 0f) * s / 24f, center + Offset(-2f * dir, 7f) * s / 24f, stroke, StrokeCap.Round)
            }

            LabIcon.CIRCUIT -> {
                drawRoundRect(
                    tint,
                    topLeft = center + Offset(-9f, -8f) * s / 24f,
                    size = Size(18f * s / 24f, 16f * s / 24f),
                    cornerRadius = CornerRadius(2f * s / 24f),
                    style = Stroke(stroke * 0.85f),
                )
                drawLine(tint, center + Offset(-5f, -2f) * s / 24f, center + Offset(-5f, 4f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(-1f, -4f) * s / 24f, center + Offset(-1f, 6f) * s / 24f, stroke * 0.8f, StrokeCap.Round)
                drawCircle(tint, 3.4f * s / 24f, center + Offset(4f, 1f) * s / 24f, style = Stroke(stroke * 0.85f))
                drawLine(tint, center + Offset(4f, -3f) * s / 24f, center + Offset(4f, 1f) * s / 24f, stroke * 0.7f)
            }

            LabIcon.MORE -> {
                for (dx in listOf(-6f, 0f, 6f)) {
                    drawCircle(tint, 1.7f * s / 24f, center + Offset(dx * s / 24f, 0f))
                }
            }

            LabIcon.WIRE -> {
                val path = Path().apply {
                    moveTo(center.x - 8f * s / 24f, center.y + 5f * s / 24f)
                    cubicTo(
                        center.x - 2f * s / 24f, center.y - 10f * s / 24f,
                        center.x + 5f * s / 24f, center.y + 9f * s / 24f,
                        center.x + 9f * s / 24f, center.y - 5f * s / 24f,
                    )
                }
                drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round))
                drawCircle(tint, 2.1f * s / 24f, center + Offset(-9f, 5f) * s / 24f)
                drawCircle(tint, 2.1f * s / 24f, center + Offset(9f, -5f) * s / 24f)
            }

            LabIcon.HELP -> {
                drawCircle(tint, 9f * s / 24f, center, style = Stroke(stroke * 0.9f))
                val path = Path().apply {
                    moveTo(center.x - 3.2f * s / 24f, center.y - 3.6f * s / 24f)
                    cubicTo(
                        center.x - 3f * s / 24f, center.y - 7.5f * s / 24f,
                        center.x + 4.5f * s / 24f, center.y - 7.5f * s / 24f,
                        center.x + 4f * s / 24f, center.y - 3f * s / 24f,
                    )
                    cubicTo(
                        center.x + 3.6f * s / 24f, center.y - 0.6f * s / 24f,
                        center.x, center.y - 1f * s / 24f,
                        center.x, center.y + 2.5f * s / 24f,
                    )
                }
                drawPath(path, tint, style = Stroke(stroke * 0.85f, cap = StrokeCap.Round))
                drawCircle(tint, 1.2f * s / 24f, center + Offset(0f, 6f) * s / 24f)
            }

            LabIcon.LIGHTNING -> {
                val path = Path().apply {
                    moveTo(center.x + 1.5f * s / 24f, center.y - 10f * s / 24f)
                    lineTo(center.x - 6f * s / 24f, center.y + 1f * s / 24f)
                    lineTo(center.x - 0.5f * s / 24f, center.y + 1f * s / 24f)
                    lineTo(center.x - 2f * s / 24f, center.y + 10f * s / 24f)
                    lineTo(center.x + 6.5f * s / 24f, center.y - 2.5f * s / 24f)
                    lineTo(center.x + 0.8f * s / 24f, center.y - 2.5f * s / 24f)
                    close()
                }
                drawPath(path, tint)
            }

            LabIcon.EXAMPLE -> {
                drawRoundRect(
                    tint,
                    topLeft = center + Offset(-9f, -7f) * s / 24f,
                    size = Size(18f * s / 24f, 15f * s / 24f),
                    cornerRadius = CornerRadius(2.5f * s / 24f),
                    style = Stroke(stroke * 0.85f),
                )
                drawLine(tint, center + Offset(-9f, -2f) * s / 24f, center + Offset(9f, -2f) * s / 24f, stroke * 0.65f)
                drawCircle(tint, 2.4f * s / 24f, center + Offset(-4f, 3f) * s / 24f)
                drawCircle(tint, 2.4f * s / 24f, center + Offset(4f, 3f) * s / 24f)
            }

            LabIcon.INFO -> {
                drawCircle(tint, 9f * s / 24f, center, style = Stroke(stroke * 0.9f))
                drawCircle(tint, 1.2f * s / 24f, center + Offset(0f, -4f) * s / 24f)
                drawLine(tint, center + Offset(0f, -0.5f) * s / 24f, center + Offset(0f, 5f) * s / 24f, stroke * 0.9f, StrokeCap.Round)
            }

            LabIcon.CHECK -> {
                drawLine(tint, center + Offset(-7f, 0f) * s / 24f, center + Offset(-2f, 5f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(-2f, 5f) * s / 24f, center + Offset(8f, -6f) * s / 24f, stroke, StrokeCap.Round)
            }

            LabIcon.FOLDER -> {
                val path = Path().apply {
                    moveTo(center.x - 9f * s / 24f, center.y - 6f * s / 24f)
                    lineTo(center.x - 2f * s / 24f, center.y - 6f * s / 24f)
                    lineTo(center.x + 1f * s / 24f, center.y - 3f * s / 24f)
                    lineTo(center.x + 9f * s / 24f, center.y - 3f * s / 24f)
                    lineTo(center.x + 8f * s / 24f, center.y + 7f * s / 24f)
                    lineTo(center.x - 8f * s / 24f, center.y + 7f * s / 24f)
                    close()
                }
                drawPath(path, tint, style = Stroke(stroke * 0.85f))
            }

            LabIcon.SWITCH -> {
                drawLine(tint, center + Offset(-9f, 5f) * s / 24f, center + Offset(-4f, 5f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(4f, 5f) * s / 24f, center + Offset(9f, 5f) * s / 24f, stroke, StrokeCap.Round)
                drawCircle(tint, 1.8f * s / 24f, center + Offset(-4f, 5f) * s / 24f)
                drawCircle(tint, 1.8f * s / 24f, center + Offset(4f, 5f) * s / 24f)
                drawLine(tint, center + Offset(-4f, 5f) * s / 24f, center + Offset(5f, -5f) * s / 24f, stroke, StrokeCap.Round)
            }

            LabIcon.RESET -> {
                drawArc(
                    tint,
                    startAngle = -50f,
                    sweepAngle = 280f,
                    useCenter = false,
                    topLeft = center + Offset(-8f, -8f) * s / 24f,
                    size = Size(16f * s / 24f, 16f * s / 24f),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                drawLine(tint, center + Offset(8f, -8f) * s / 24f, center + Offset(8f, -2f) * s / 24f, stroke, StrokeCap.Round)
                drawLine(tint, center + Offset(8f, -2f) * s / 24f, center + Offset(2f, -2f) * s / 24f, stroke, StrokeCap.Round)
            }
        }
    }
}
