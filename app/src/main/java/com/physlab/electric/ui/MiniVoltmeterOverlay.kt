package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.InstrumentType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniVoltmeterOverlay(
    editor: LabEditorState,
    solution: CircuitSolution,
    modifier: Modifier = Modifier,
) {
    if (!editor.rheostatDragging) return
    if (editor.components.none { it.type == InstrumentType.RHEOSTAT }) return
    val voltmeter = editor.components.firstOrNull { it.type == InstrumentType.VOLTMETER } ?: return
    val reading = solution.readings[voltmeter.id] ?: return
    val range = reading.meterRange.takeIf { it > 0f } ?: 3f
    val value = reading.meterSignedValue
    val fraction = (value / range).coerceIn(-1f / 6f, 1f)
    Column(modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(384.dp, 218.dp)) {
            val cx = size.width / 2f
            val cy = size.height * 0.84f
            val r = minOf(size.width * 0.34f, size.height * 0.62f)
            val start = 225f
            val sweep = 100f
            val scaleColor = Color(0xFFFFF6E4)
            val shadowColor = Color.Black.copy(alpha = 0.75f)
            val needleColor = Color(0xFFFF2D2D)

            // 大小量程共用同一圈刻度盘。
            drawArc(
                shadowColor,
                start,
                sweep,
                false,
                Offset(cx - r, cy - r),
                Size(r * 2, r * 2),
                style = Stroke(5.4f * density, cap = StrokeCap.Round),
            )
            drawArc(
                scaleColor,
                start,
                sweep,
                false,
                Offset(cx - r, cy - r),
                Size(r * 2, r * 2),
                style = Stroke(3.0f * density, cap = StrokeCap.Round),
            )
            for (i in 0..30) {
                val angle = Math.toRadians((start + sweep * i / 30f).toDouble())
                val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                val major = i % 5 == 0
                val len = if (major) 11f else 5.5f
                val innerEnd = r - len
                val outerEnd = r + len
                drawLine(
                    shadowColor,
                    Offset(cx + dir.x * (innerEnd - 1.5f), cy + dir.y * (innerEnd - 1.5f)),
                    Offset(cx + dir.x * (outerEnd + 1.5f), cy + dir.y * (outerEnd + 1.5f)),
                    3.6f * density,
                    StrokeCap.Round,
                )
                drawLine(
                    scaleColor,
                    Offset(cx + dir.x * innerEnd, cy + dir.y * innerEnd),
                    Offset(cx + dir.x * outerEnd, cy + dir.y * outerEnd),
                    (if (major) 2.4f else 1.2f) * density,
                    StrokeCap.Round,
                )
            }

            fun drawNumbers(maxValue: Float, radius: Float, textSize: Float) {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(255, 246, 228)
                    this.textSize = textSize * density
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                for (i in 0..6) {
                    val angle = Math.toRadians((start + sweep * i / 6f).toDouble())
                    val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                    val label = String.format(java.util.Locale.US, "%.1f", maxValue * i / 6f)
                        .trimEnd('0').trimEnd('.')
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        cx + dir.x * radius,
                        cy + dir.y * radius + 4f * density,
                        paint,
                    )
                }
            }

            // 同一圈刻度：外侧大量程 0～15 V，内侧小量程 0～3 V。
            drawNumbers(15f, r + 18f * density, 10f)
            drawNumbers(3f, r - 20f * density, 8.5f)

            val unitPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(255, 246, 228)
                textSize = 11f * density
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText("V", cx, cy + 30f * density, unitPaint)

            val needleAngle = Math.toRadians((start + sweep * fraction).toDouble())
            val needleEnd = Offset(
                cx + cos(needleAngle).toFloat() * (r - 8f * density),
                cy + sin(needleAngle).toFloat() * (r - 8f * density),
            )
            drawLine(Color.Black.copy(alpha = 0.85f), Offset(cx, cy), needleEnd, 6.6f * density, StrokeCap.Round)
            drawLine(needleColor, Offset(cx, cy), needleEnd, 3.2f * density, StrokeCap.Round)
            drawCircle(Color.Black.copy(alpha = 0.9f), 7.5f * density, Offset(cx, cy))
            drawCircle(needleColor, 5.2f * density, Offset(cx, cy))
            drawCircle(scaleColor, 2.2f * density, Offset(cx, cy))
        }
    }
}
