package com.physlab.electric.ui.render

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.isRotatable
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.MultimeterMode
import com.physlab.electric.model.ResistanceWireSamples
import kotlin.math.cos
import kotlin.math.sin

data class InstrumentVisual(
    val selected: Boolean = false,
    val closed: Boolean = false,
    val rotation: Float = 0f,
    val lit: Boolean = false,
    val brightness: Float = 0f,
    val slider: Float = 0.5f,
    val meterFraction: Float? = null,
    val meterRangeLabel: String = "",
    val meterOverRange: Boolean = false,
    val meterTerminal: Int = 1,
    val meterFlipped: Boolean = false,
    val multimeterMode: MultimeterMode? = null,
    val multimeterDisplay: String = "",
    val multimeterWarning: String = "",
    val meterOl: Boolean = false,
    val batteryCells: Int = 2,
    val lampSpec: LampSpec? = null,
    val polarityFlipped: Boolean = false,
    val rheostatMax: Float = 20f,
    val supplyVoltage: Float = 12f,
    val supplyAc: Boolean = false,
    val capacitanceUf: Float = 470f,
    val inductanceMh: Float = 50f,
    val switchPosition: Int = 0,
    val storageVoltage: Float = 6f,
    val resistanceBoxValue: Int = 100,
    val wireSampleIndex: Int = 0,
    val showWireResistance: Boolean = false,
    val energized: Boolean = false,
    val magneticReversed: Boolean = false,
    val windingReversed: Boolean = false,
    val fanAngle: Float = 0f,
    val compassAngle: Float = 0f,
    val paperclipPull: Offset = Offset.Zero,
)

/**
 * Draws one instrument at [center] using design coordinates. One design unit is
 * meant to equal one dp on the board, so the same art scales cleanly from a
 * 5.5" phone to a 13" tablet.
 */
fun DrawScope.drawInstrument(
    type: InstrumentType,
    center: Offset,
    scale: Float,
    visual: InstrumentVisual = InstrumentVisual(),
) {
    withTransform({
        translate(center.x, center.y)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        if (type.isRotatable && visual.rotation != 0f) {
            rotate(visual.rotation, pivot = Offset.Zero)
        }
    }) {
        when (type) {
            InstrumentType.BATTERY -> drawBattery(visual)
            InstrumentType.POWER_SUPPLY -> drawPowerSupply(visual)
            InstrumentType.STORAGE_BATTERY -> drawStorageBattery(visual)
            InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> drawLampBulb(visual)
            InstrumentType.MOTOR -> drawMotor(visual)
            InstrumentType.BUZZER -> drawBuzzer(visual)
            InstrumentType.LED -> drawLed(visual)
            InstrumentType.AMMETER -> drawMeter(visual, isAmmeter = true)
            InstrumentType.VOLTMETER -> drawMeter(visual, isAmmeter = false)
            InstrumentType.MILLIAMMETER -> drawMeter(visual, isAmmeter = true)
            InstrumentType.MULTIMETER -> drawMultimeter(visual)
            InstrumentType.GALVANOMETER -> drawGalvanometerMeter(visual)
            InstrumentType.SWITCH -> drawSwitch(visual)
            InstrumentType.SPDT_SWITCH -> drawSpdtSwitch(visual)
            InstrumentType.CAPACITOR -> drawCapacitor(visual)
            InstrumentType.INDUCTOR -> drawInductor(visual)
            InstrumentType.RHEOSTAT -> drawRheostat(visual)
            InstrumentType.RESISTOR -> drawResistor()
            InstrumentType.RESISTANCE_BOX -> drawResistanceBox(visual)
            InstrumentType.RESISTANCE_WIRE -> drawResistanceWire(visual)
            InstrumentType.ELECTROMAGNET -> drawElectromagnet(visual)
            InstrumentType.SOLENOID -> drawSolenoid(visual)
            InstrumentType.BAR_MAGNET -> drawBarMagnet(visual)
            InstrumentType.HORSESHOE_MAGNET -> drawHorseshoeMagnet(visual)
            InstrumentType.COMPASS -> drawCompass(visual)
            InstrumentType.PAPERCLIPS -> drawPaperclips(visual)
        }
    }
}

// ---------------------------------------------------------------------------
// Shared 3D drawing helpers
// ---------------------------------------------------------------------------

private val Shadow = Color.Black.copy(alpha = 0.52f)
private val MetalLight = Color(0xFFE9EDF4)
private val MetalMid = Color(0xFF9BA5B5)
private val MetalDark = Color(0xFF4C5563)
private val BrassLight = Color(0xFFFFE4A4)
private val BrassMid = Color(0xFFC99A45)
private val BrassDark = Color(0xFF684E20)
private val CopperLight = Color(0xFFFFC58C)
private val CopperMid = Color(0xFFC56A2D)
private val CopperDark = Color(0xFF61290F)

internal fun DrawScope.textLabel(
    text: String,
    x: Float,
    y: Float,
    size: Float,
    color: Color,
    bold: Boolean = false,
    align: Paint.Align = Paint.Align.CENTER,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgbCompat()
        textSize = size
        textAlign = align
        typeface = Typeface.create(
            if (bold) "sans-serif-medium" else "sans-serif",
            if (bold) Typeface.BOLD else Typeface.NORMAL,
        )
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun Color.toArgbCompat(): Int = this.toArgb()

private fun DrawScope.softShadow(cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float = 0.42f) {
    drawOval(
        color = Color.Black.copy(alpha = alpha),
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f),
    )
}

private fun DrawScope.surfacePanel(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
    topColor: Color,
    bottomColor: Color,
    border: Color = Color.Black.copy(alpha = 0.55f),
    borderWidth: Float = 1.1f,
) {
    drawRoundRect(
        color = border,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        cornerRadius = CornerRadius(radius, radius),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(topColor, bottomColor),
            startY = top,
            endY = bottom,
        ),
        topLeft = Offset(left + borderWidth, top + borderWidth),
        size = Size(right - left - borderWidth * 2f, bottom - top - borderWidth * 2f),
        cornerRadius = CornerRadius(radius - borderWidth, radius - borderWidth),
    )
}

private fun DrawScope.bindingPost(x: Float, y: Float, plastic: Color? = null, radius: Float = 5.4f) {
    softShadow(x + 1.2f, y + 3.2f, radius * 1.3f, radius * 0.9f, 0.5f)
    if (plastic != null) {
        drawCircle(plastic.copy(alpha = 0.85f), radius * 1.55f, Offset(x, y + 0.4f))
        drawCircle(plastic, radius * 1.28f, Offset(x, y))
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(MetalLight, MetalMid, MetalDark),
            center = Offset(x - radius * 0.38f, y - radius * 0.42f),
            radius = radius * 1.9f,
        ),
        radius = radius,
        center = Offset(x, y),
    )
    drawCircle(MetalDark, radius, Offset(x, y), style = Stroke(0.7f))
    drawCircle(Color(0xFF343C49), radius * 0.46f, Offset(x, y))
    drawLine(
        color = Color(0xFF8F9AAB),
        start = Offset(x - radius * 0.26f, y - 0.2f),
        end = Offset(x + radius * 0.26f, y - 0.2f),
        strokeWidth = 0.9f,
        cap = StrokeCap.Round,
    )
    drawCircle(
        Color.White.copy(alpha = 0.7f),
        radius * 0.17f,
        Offset(x - radius * 0.34f, y - radius * 0.36f),
    )
}

private fun DrawScope.drawWireLead(from: Offset, to: Offset, width: Float = 2.2f) {
    drawLine(Color.Black.copy(alpha = 0.5f), from + Offset(0f, 1.2f), to + Offset(0f, 1.2f), width + 0.7f, StrokeCap.Round)
    drawLine(
        brush = Brush.linearGradient(
            listOf(MetalLight, MetalMid, MetalDark),
            start = from,
            end = to,
        ),
        start = from,
        end = to,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

// ---------------------------------------------------------------------------
// Battery
// ---------------------------------------------------------------------------

private fun DrawScope.drawBattery(visual: InstrumentVisual) {
    softShadow(0f, 26f, 68f, 9.5f, 0.55f)
    surfacePanel(-62f, -23f, 62f, 24f, 8f, Color(0xFF252C38), Color(0xFF0F131A), Color(0xFF080A0D), 1.4f)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.075f),
        topLeft = Offset(-58f, -20f),
        size = Size(116f, 2.2f),
        cornerRadius = CornerRadius(1f, 1f),
    )

    val cells = visual.batteryCells.coerceIn(1, 4)
    val gap = 4.5f
    val available = 108f
    val cellLength = ((available - gap * (cells - 1)) / cells).coerceIn(20f, 46f)
    val totalWidth = cellLength * cells + gap * (cells - 1)
    val startX = -totalWidth / 2f
    val cellTop = -8.5f
    val cellHeight = 17f
    val labelSize = when (cells) {
        1, 2 -> 4.8f
        3 -> 4.1f
        else -> 3.4f
    }

    for (index in 0 until cells) {
        val left = startX + index * (cellLength + gap)
        val cx = left + cellLength / 2f
        softShadow(cx, 12f, cellLength * 0.42f, 3.2f, 0.28f)
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF83C6FF), Color(0xFF2D68D0), Color(0xFF102750)),
                startY = cellTop,
                endY = cellTop + cellHeight,
            ),
            topLeft = Offset(left, cellTop),
            size = Size(cellLength, cellHeight),
            cornerRadius = CornerRadius(7f, 7f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.24f),
            topLeft = Offset(left + 3f, cellTop + 2.2f),
            size = Size((cellLength - 6f).coerceAtLeast(4f), 2.1f),
            cornerRadius = CornerRadius(1f, 1f),
        )
        // The whole battery pack can be reversed, so the cap follows polarity.
        val positiveRight = !visual.polarityFlipped
        val negativeX = if (positiveRight) left else left + cellLength - 3.2f
        val positiveX = if (positiveRight) left + cellLength - 3.2f else left
        drawRoundRect(
            color = Color(0xFF4B5563),
            topLeft = Offset(negativeX, cellTop + 1.8f),
            size = Size(3.2f, cellHeight - 3.6f),
            cornerRadius = CornerRadius(1.5f, 1.5f),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFF2F5FA), Color(0xFF9BA6B6), Color(0xFF525C69)),
            ),
            topLeft = Offset(positiveX, cellTop + 1.2f),
            size = Size(3.2f, cellHeight - 2.4f),
            cornerRadius = CornerRadius(1.5f, 1.5f),
        )
        drawRoundRect(
            color = Color(0xFFB9C3D0),
            topLeft = Offset(
                if (positiveRight) left + cellLength else left - 2.4f,
                -2.6f,
            ),
            size = Size(2.4f, 5.2f),
            cornerRadius = CornerRadius(1.2f, 1.2f),
        )
        textLabel("1.5V", cx, 1.7f, labelSize, Color.White.copy(alpha = 0.96f), true)

        if (index < cells - 1) {
            val nextLeft = left + cellLength + gap
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFE4EAF2), Color(0xFF8B96A7), Color(0xFF4E5866)),
                    startX = left + cellLength,
                    endX = nextLeft,
                ),
                start = Offset(left + cellLength + 1f, 0f),
                end = Offset(nextLeft - 1f, 0f),
                strokeWidth = 4.3f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = Offset(left + cellLength + 1f, -0.8f),
                end = Offset(nextLeft - 1f, -0.8f),
                strokeWidth = 1f,
                cap = StrokeCap.Round,
            )
        }
    }

    // Route the two ends of the series chain to the external binding posts.
    val left = startX
    val right = startX + totalWidth
    drawLine(MetalMid, Offset(left + 2f, 0f), Offset(-60f, 0f), 2.5f, StrokeCap.Round)
    drawLine(MetalMid, Offset(right - 2f, 0f), Offset(60f, 0f), 2.5f, StrokeCap.Round)
    drawLine(MetalMid, Offset(-60f, 0f), Offset(-70f, 0f), 2.4f, StrokeCap.Round)
    drawLine(MetalMid, Offset(60f, 0f), Offset(70f, 0f), 2.4f, StrokeCap.Round)

    bindingPost(-70f, 0f, if (visual.polarityFlipped) Color(0xFFD83942) else Color(0xFF1D2026))
    bindingPost(70f, 0f, if (visual.polarityFlipped) Color(0xFF1D2026) else Color(0xFFD83942))
    textLabel(if (visual.polarityFlipped) "−" else "+", 70f, -10f, 7.2f,
        if (visual.polarityFlipped) Color(0xFFB8C4D4) else Color(0xFFFF8F94), true)
    textLabel(if (visual.polarityFlipped) "+" else "−", -70f, -10f, 7.2f,
        if (visual.polarityFlipped) Color(0xFFFF8F94) else Color(0xFFB8C4D4), true)
}

// ---------------------------------------------------------------------------
// Student power supply
// ---------------------------------------------------------------------------

private fun DrawScope.drawPowerSupply(visual: InstrumentVisual) {
    softShadow(0f, 39f, 50f, 9f, 0.52f)
    drawRoundRect(
        color = Color(0xFF111720),
        topLeft = Offset(-22f, -45f),
        size = Size(44f, 11f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color(0xFF2E3744),
        topLeft = Offset(-18f, -43f),
        size = Size(36f, 7f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    surfacePanel(-52f, -38f, 52f, 36f, 10f, Color(0xFF485261), Color(0xFF171D26), Color(0xFF070A0E), 1.4f)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.09f),
        topLeft = Offset(-46f, -34f),
        size = Size(92f, 2.4f),
        cornerRadius = CornerRadius(1f, 1f),
    )

    // Display
    drawRoundRect(
        color = Color(0xFF07110F),
        topLeft = Offset(-39f, -26f),
        size = Size(48f, 26f),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        color = Color(0xFF0B2A22),
        topLeft = Offset(-37f, -24f),
        size = Size(44f, 22f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    val voltageText = String.format(java.util.Locale.US, "%.1f V", visual.supplyVoltage)
    textLabel(voltageText, -15f, -8f, 10.2f, Color(0xFF6FFFE0), true)
    textLabel(if (visual.supplyAc) "AC 50Hz" else "DC", -15f, -17f, 4.7f, Color(0xFF7FD3C6), true)
    textLabel("学生电源", 8f, 18f, 5.0f, Color(0xFFA9B6C7), true)

    // Output mode switch and voltage knob
    drawRoundRect(
        color = Color(0xFF252D38),
        topLeft = Offset(16f, -28f),
        size = Size(27f, 15f),
        cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFE8EDF4), Color(0xFF8994A5))),
        topLeft = if (visual.supplyAc) Offset(29f, -26.5f) else Offset(18f, -26.5f),
        size = Size(13f, 12f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    textLabel(if (visual.supplyAc) "AC" else "DC", 29.5f, 10.5f, 4.8f, Color(0xFFB8C6D8), true)

    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF5A6576), Color(0xFF242B35)),
            center = Offset(29f, -7f),
            radius = 13f,
        ),
        radius = 11f,
        center = Offset(29f, -7f),
    )
    drawCircle(Color(0xFF11161D), 8.7f, Offset(29f, -7f))
    for (i in 0 until 8) {
        val angle = Math.toRadians((i * 45.0) - 90.0)
        drawLine(
            Color(0xFF9EABBC),
            Offset(29f + cos(angle).toFloat() * 5.5f, -7f + sin(angle).toFloat() * 5.5f),
            Offset(29f + cos(angle).toFloat() * 8.2f, -7f + sin(angle).toFloat() * 8.2f),
            0.8f,
            StrokeCap.Round,
        )
    }
    drawLine(Color(0xFFFFC857), Offset(29f, -7f), Offset(24f, -12f), 1.5f, StrokeCap.Round)
    drawCircle(Color(0xFFFFC857), 1.6f, Offset(29f, -7f))

    drawLine(MetalMid, Offset(-43f, 22f), Offset(-43f, 32f), 2.2f)
    drawLine(MetalMid, Offset(43f, 22f), Offset(43f, 32f), 2.2f)
    bindingPost(-43f, 32f, if (visual.polarityFlipped) Color(0xFFD83942) else Color(0xFF23272E))
    bindingPost(43f, 32f, if (visual.polarityFlipped) Color(0xFF23272E) else Color(0xFFD83942))
    textLabel(if (visual.polarityFlipped) "+" else "−", -43f, 23f, 6.5f,
        if (visual.polarityFlipped) Color(0xFFFF8D94) else Color(0xFFC8D4E2), true)
    textLabel(if (visual.polarityFlipped) "−" else "+", 43f, 23f, 6.5f,
        if (visual.polarityFlipped) Color(0xFFC8D4E2) else Color(0xFFFF8D94), true)
}

// ---------------------------------------------------------------------------
// Storage battery
// ---------------------------------------------------------------------------

private fun DrawScope.drawStorageBattery(visual: InstrumentVisual) {
    softShadow(0f, 30f, 45f, 8f, 0.5f)
    surfacePanel(-46f, -28f, 46f, 30f, 7f, Color(0xFF3C6EA8), Color(0xFF173B69), Color(0xFF07111F), 1.3f)
    drawRoundRect(
        color = Color(0xFFB9C8D9),
        topLeft = Offset(-39f, -32f),
        size = Size(78f, 8f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    for (i in 0 until 6) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(-38f + i * 13f, -24f),
            size = Size(2.3f, 47f),
            cornerRadius = CornerRadius(1f, 1f),
        )
    }
    drawRoundRect(
        color = Color(0xFFFFF1C9),
        topLeft = Offset(-34f, -17f),
        size = Size(68f, 21f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    textLabel("蓄电池", 0f, -6f, 6.6f, Color(0xFF1D2A3C), true)
    textLabel("${formatVoltage(visual.storageVoltage)}", 0f, 11f, 10f, Color(0xFF1D2A3C), true)

    drawLine(MetalMid, Offset(-38f, 25f), Offset(-38f, 33f), 2.4f)
    drawLine(MetalMid, Offset(38f, 25f), Offset(38f, 33f), 2.4f)
    bindingPost(-38f, 33f, if (visual.polarityFlipped) Color(0xFFD83942) else Color(0xFF1D2026))
    bindingPost(38f, 33f, if (visual.polarityFlipped) Color(0xFF1D2026) else Color(0xFFD83942))
    textLabel(if (visual.polarityFlipped) "+" else "−", -38f, 24f, 6.3f,
        if (visual.polarityFlipped) Color(0xFFFF8D94) else Color(0xFFC8D4E2), true)
    textLabel(if (visual.polarityFlipped) "−" else "+", 38f, 24f, 6.3f,
        if (visual.polarityFlipped) Color(0xFFC8D4E2) else Color(0xFFFF8D94), true)
}

// ---------------------------------------------------------------------------
// Buzzer and LED
// ---------------------------------------------------------------------------

private fun DrawScope.drawBuzzer(visual: InstrumentVisual) {
    softShadow(0f, 33f, 30f, 6.5f, 0.5f)
    if (visual.energized) {
        for (i in 0 until 3) {
            drawArc(
                color = Color(0xFFFFC857).copy(alpha = 0.44f - i * 0.09f),
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(-28f - i * 7f, -28f - i * 4f),
                size = Size(56f + i * 14f, 56f + i * 8f),
                style = Stroke(1.5f, cap = StrokeCap.Round),
            )
        }
    }
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF4A5361), Color(0xFF171C23))),
        topLeft = Offset(-33f, 15f),
        size = Size(66f, 22f),
        cornerRadius = CornerRadius(6f, 6f),
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFE9EDF3), Color(0xFF9AA5B4), Color(0xFF4B5564)),
            center = Offset(-6f, -8f),
            radius = 30f,
        ),
        radius = 25f,
        center = Offset(0f, -2f),
    )
    drawCircle(Color(0xFF2B323C), 20f, Offset(0f, -2f))
    for (ring in 0 until 3) {
        val radius = 4.5f + ring * 5.2f
        for (i in 0 until 10 + ring * 4) {
            val angle = Math.toRadians((i * 360.0 / (10 + ring * 4)))
            drawCircle(
                Color(0xFF161B21),
                1.15f,
                Offset(cos(angle).toFloat() * radius, -2f + sin(angle).toFloat() * radius),
            )
        }
    }
    drawCircle(Color(0xFF6D7888), 3.2f, Offset(0f, -2f))
    textLabel("蜂鸣器", 0f, 30f, 4.8f, Color(0xFF94A2B4), true)
    bindingPost(-25f, 31f, Color(0xFFD83942))
    bindingPost(25f, 31f, Color(0xFF1D2026))
    textLabel("+", -25f, 22f, 5.7f, Color(0xFFFF8D94), true)
    textLabel("−", 25f, 22f, 5.7f, Color(0xFFC8D4E2), true)
}

private fun DrawScope.drawLed(visual: InstrumentVisual) {
    softShadow(0f, 34f, 30f, 6.5f, 0.5f)
    if (visual.energized) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF4D5E).copy(alpha = 0.45f), Color.Transparent),
                center = Offset(0f, -7f),
                radius = 40f,
            ),
            radius = 40f,
            center = Offset(0f, -7f),
        )
    }
    drawRoundRect(
        color = Color(0xFF11151B),
        topLeft = Offset(-31f, 15f),
        size = Size(62f, 17f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    val dome = Path().apply {
        moveTo(-19f, 18f)
        cubicTo(-22f, 2f, -18f, -22f, 0f, -31f)
        cubicTo(18f, -22f, 22f, 2f, 19f, 18f)
        close()
    }
    drawPath(
        path = dome,
        brush = Brush.radialGradient(
            listOf(
                if (visual.energized) Color(0xFFFFD2D5) else Color(0xFFFF9DA5),
                Color(0xFFE33548),
                Color(0xFF7D0C1A),
            ),
            center = Offset(-5f, -13f),
            radius = 34f,
        ),
    )
    drawPath(dome, Color(0xFF6A0A14).copy(alpha = 0.8f), style = Stroke(1.2f))
    drawPath(
        Path().apply {
            moveTo(-12f, -14f)
            cubicTo(-7f, -24f, 5f, -24f, 10f, -17f)
        },
        Color.White.copy(alpha = 0.55f),
        style = Stroke(2.2f, cap = StrokeCap.Round),
    )
    drawLine(MetalMid, Offset(-15f, 18f), Offset(-27f, 32f), 2.2f, StrokeCap.Round)
    drawLine(MetalMid, Offset(15f, 18f), Offset(27f, 32f), 2.2f, StrokeCap.Round)
    bindingPost(-27f, 32f, Color(0xFFD83942))
    bindingPost(27f, 32f, Color(0xFF1D2026))
    textLabel("+", -27f, 23f, 5.8f, Color(0xFFFF8D94), true)
    textLabel("−", 27f, 23f, 5.8f, Color(0xFFC8D4E2), true)
    textLabel("LED", 0f, 30f, 4.8f, Color(0xFF94A2B4), true)
}

// ---------------------------------------------------------------------------
// Resistance box
// ---------------------------------------------------------------------------

private fun DrawScope.drawResistanceBox(visual: InstrumentVisual) {
    softShadow(0f, 38f, 53f, 8f, 0.5f)
    surfacePanel(-55f, -38f, 55f, 37f, 8f, Color(0xFF6B4A2E), Color(0xFF2A1A10), Color(0xFF100A06), 1.4f)
    drawRoundRect(
        color = Color(0xFFE7D7B5),
        topLeft = Offset(-47f, -31f),
        size = Size(94f, 52f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color(0xFF9D8460).copy(alpha = 0.7f),
        topLeft = Offset(-47f, -31f),
        size = Size(94f, 52f),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(0.9f),
    )
    textLabel("电阻箱", 0f, -23.5f, 5.8f, Color(0xFF4A3824), true)
    textLabel("0–9999 Ω", 0f, 15.5f, 5.4f, Color(0xFF4A3824), true)
    val value = visual.resistanceBoxValue.coerceIn(0, 9999)
    val digits = listOf(
        (value / 1000) % 10,
        (value / 100) % 10,
        (value / 10) % 10,
        value % 10,
    )
    val multipliers = listOf("×1000", "×100", "×10", "×1")
    for (i in 0 until 4) {
        val cx = -36f + i * 24f
        val cy = -7f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFF7EDD8), Color(0xFFC8B18B)),
                center = Offset(cx - 3f, cy - 4f),
                radius = 13f,
            ),
            radius = 10.5f,
            center = Offset(cx, cy),
        )
        drawCircle(Color(0xFF6B5335), 10.5f, Offset(cx, cy), style = Stroke(0.9f))
        for (tick in 0 until 10) {
            val angle = Math.toRadians(tick * 36.0 - 90.0)
            drawLine(
                Color(0xFF6D5637),
                Offset(cx + cos(angle).toFloat() * 7.1f, cy + sin(angle).toFloat() * 7.1f),
                Offset(cx + cos(angle).toFloat() * 9.5f, cy + sin(angle).toFloat() * 9.5f),
                0.65f,
                StrokeCap.Round,
            )
        }
        val pointerAngle = Math.toRadians(digits[i] * 36.0 - 90.0)
        drawLine(
            Color(0xFFB21F2A),
            Offset(cx, cy),
            Offset(cx + cos(pointerAngle).toFloat() * 7.6f, cy + sin(pointerAngle).toFloat() * 7.6f),
            1.6f,
            StrokeCap.Round,
        )
        drawCircle(Color(0xFF2A2621), 2.2f, Offset(cx, cy))
        textLabel(digits[i].toString(), cx, cy + 16.0f, 5.2f, Color(0xFF4A3824), true)
        textLabel(multipliers[i], cx, cy + 22.5f, 3.9f, Color(0xFF6B5335), true)
    }
    drawLine(MetalMid, Offset(-48f, 21f), Offset(-48f, 37f), 2.2f)
    drawLine(MetalMid, Offset(48f, 21f), Offset(48f, 37f), 2.2f)
    bindingPost(-48f, 37f, Color(0xFF1D2026))
    bindingPost(48f, 37f, Color(0xFFD83942))
}

private fun formatVoltage(value: Float): String =
    if (kotlin.math.abs(value - value.toInt()) < 0.01f) "${value.toInt()}V" else "${value}V"

// ---------------------------------------------------------------------------
// Resistance wire investigation board
// ---------------------------------------------------------------------------

private fun DrawScope.drawResistanceWire(visual: InstrumentVisual) {
    softShadow(0f, 46f, 84f, 9f, 0.5f)
    surfacePanel(-94f, -50f, 94f, 44f, 10f, Color(0xFF5A4030), Color(0xFF261810), Color(0xFF0E0805), 1.3f)
    drawRoundRect(
        color = Color(0xFFE7DCC5),
        topLeft = Offset(-88f, -46f),
        size = Size(176f, 76f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color(0xFF9E8C6C),
        topLeft = Offset(-88f, -46f),
        size = Size(176f, 76f),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(0.8f),
    )
    textLabel("电阻丝实验板", 0f, -39.5f, 4.8f, Color(0xFF4D402C), true)

    val selected = ResistanceWireSamples[visual.wireSampleIndex.coerceIn(0, ResistanceWireSamples.lastIndex)]
    for (sample in ResistanceWireSamples) {
        val active = sample.index == selected.index
        val y = -31f + sample.index * 14f
        val halfWidth = if (sample.length < 1f) 40f else 72f
        val thickness = 1.4f + sample.area * 3.2f
        if (active) {
            drawLine(
                Color(0xFFFFC857).copy(alpha = 0.28f),
                Offset(-halfWidth, y),
                Offset(halfWidth, y),
                thickness + 5f,
                StrokeCap.Round,
            )
        }
        drawLine(
            sample.color.copy(alpha = if (active) 1f else 0.34f),
            Offset(-halfWidth, y),
            Offset(halfWidth, y),
            thickness,
            StrokeCap.Round,
        )
        drawLine(
            sample.color.copy(alpha = if (active) 0.9f else 0.2f),
            Offset(-84f, y),
            Offset(-halfWidth, y),
            thickness * 0.7f,
            StrokeCap.Round,
        )
        drawLine(
            sample.color.copy(alpha = if (active) 0.9f else 0.2f),
            Offset(halfWidth, y),
            Offset(84f, y),
            thickness * 0.7f,
            StrokeCap.Round,
        )
        drawLine(
            Color.White.copy(alpha = if (active) 0.36f else 0.12f),
            Offset(-halfWidth, y - thickness * 0.22f),
            Offset(halfWidth, y - thickness * 0.22f),
            0.8f,
            StrokeCap.Round,
        )
        textLabel(
            "${sample.index + 1}",
            -80f,
            y + 1.8f,
            5.2f,
            if (active) Color(0xFFB13A1E) else Color(0xFF8B7A5C),
            true,
        )
    }

    // Common 1/2 terminals and internal bus (original connection mode).
    drawLine(MetalMid, Offset(-80f, 30f), Offset(-80f, 50f), 1.8f, StrokeCap.Round)
    drawLine(MetalMid, Offset(80f, 30f), Offset(80f, 50f), 1.8f, StrokeCap.Round)
    drawLine(Color(0xFF7C6A4D).copy(alpha = 0.7f), Offset(-84f, -34f), Offset(-84f, 30f), 0.9f)
    drawLine(Color(0xFF7C6A4D).copy(alpha = 0.7f), Offset(84f, -34f), Offset(84f, 30f), 0.9f)
    textLabel(
        "接入 ${selected.index + 1}：${selected.name}",
        0f,
        20f,
        4.3f,
        Color(0xFF5D4D34),
        true,
    )
    if (visual.showWireResistance) {
        textLabel(
            String.format(java.util.Locale.US, "%.1fΩ", selected.resistance),
            0f,
            28f,
            5.2f,
            Color(0xFF7A3E1E),
            true,
        )
    }
    bindingPost(-80f, 50f, Color(0xFF1D2026))
    bindingPost(80f, 50f, Color(0xFFD83942))
}

// ---------------------------------------------------------------------------
// Lamp
// ---------------------------------------------------------------------------

private fun DrawScope.drawLampBulb(visual: InstrumentVisual) {
    softShadow(0f, 46f, 35f, 7.5f, 0.52f)
    val brightness = visual.brightness.coerceIn(0f, 1.8f)
    if (visual.lit) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFFE9A8).copy(alpha = (0.10f + brightness * 0.42f).coerceAtMost(0.72f)),
                    Color(0xFFFFC64A).copy(alpha = (0.03f + brightness * 0.22f).coerceAtMost(0.38f)),
                    Color.Transparent,
                ),
                center = Offset(0f, -10f),
                radius = 46f + brightness * 18f,
            ),
            radius = 46f + brightness * 18f,
            center = Offset(0f, -10f),
        )
    }

    surfacePanel(-34f, 19f, 34f, 42f, 8f, Color(0xFF3A302A), Color(0xFF171311), Color(0xFF080706), 1.2f)
    drawRoundRect(
        color = Color(0xFF0E0C0B),
        topLeft = Offset(-27f, 25f),
        size = Size(54f, 9f),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawLine(
        Color.White.copy(alpha = 0.13f),
        Offset(-27f, 22.8f),
        Offset(27f, 22.8f),
        1.2f,
        StrokeCap.Round,
    )

    // Screw base
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFE7ECF3), Color(0xFFB6BFCC), Color(0xFF6A7380)),
            startY = 5f,
            endY = 20f,
        ),
        topLeft = Offset(-15f, 5f),
        size = Size(30f, 16f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    for (i in 0 until 4) {
        val y = 7.5f + i * 3.6f
        drawLine(Color(0xFF737D8C), Offset(-14f, y), Offset(14f, y + 0.6f), 1.1f, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.28f), Offset(-14f, y - 0.55f), Offset(14f, y), 0.7f, StrokeCap.Round)
    }
    drawRoundRect(
        color = Color(0xFF535C69),
        topLeft = Offset(-17f, 18f),
        size = Size(34f, 4.5f),
        cornerRadius = CornerRadius(2f, 2f),
    )

    // Glass bulb
    val bulb = Path().apply {
        moveTo(-16f, 8f)
        cubicTo(-24f, 0f, -24f, -16f, -17f, -26f)
        cubicTo(-9f, -37f, 9f, -37f, 17f, -26f)
        cubicTo(24f, -16f, 24f, 0f, 16f, 8f)
        close()
    }
    drawPath(
        path = bulb,
        brush = Brush.radialGradient(
            colors = if (visual.lit) {
                listOf(
                    Color(0xFFFFFBE8).copy(alpha = (0.38f + brightness * 0.62f).coerceAtMost(1f)),
                    Color(0xFFFFE7A0).copy(alpha = (0.14f + brightness * 0.62f).coerceAtMost(1f)),
                    Color(0xFFFFB940).copy(alpha = (0.08f + brightness * 0.52f).coerceAtMost(0.92f)),
                )
            } else {
                listOf(
                    Color(0xFFDDE7F2).copy(alpha = 0.55f),
                    Color(0xFF8FA4BA).copy(alpha = 0.32f),
                    Color(0xFF415366).copy(alpha = 0.48f),
                )
            },
            center = Offset(-4f, -15f),
            radius = 34f,
        ),
    )
    drawPath(bulb, Color(0xFF8CA3B8).copy(alpha = 0.9f), style = Stroke(1.2f))
    drawPath(
        Path().apply {
            moveTo(-13f, -22f)
            cubicTo(-8f, -31f, 4f, -32f, 9f, -27f)
        },
        Color.White.copy(alpha = 0.55f),
        style = Stroke(1.8f, cap = StrokeCap.Round),
    )

    // Filament supports and the glowing coil
    drawLine(Color(0xFF5E6772), Offset(-6f, 9f), Offset(-6f, -10f), 1.1f)
    drawLine(Color(0xFF5E6772), Offset(6f, 9f), Offset(6f, -10f), 1.1f)
    val filamentColor = if (visual.lit) Color(0xFFFFD35C) else Color(0xFF7A6E56)
    val filament = Path().apply {
        moveTo(-6f, -10f)
        cubicTo(-6f, -20f, -2f, -21f, 0f, -13f)
        cubicTo(1.5f, -7f, 3.5f, -7f, 5f, -14f)
        cubicTo(6f, -20f, 6f, -18f, 6f, -10f)
    }
    if (visual.lit) {
        drawPath(
            filament,
            Color(0xFFFFE7A0).copy(alpha = (0.08f + brightness * 0.52f).coerceAtMost(0.7f)),
            style = Stroke(2.8f + brightness * 2.2f, cap = StrokeCap.Round),
        )
    }
    drawPath(filament, filamentColor, style = Stroke(1.5f, cap = StrokeCap.Round))

    if (visual.lit) {
        drawCircle(
            Color(0xFFFFDD77).copy(alpha = (0.10f + brightness * 0.45f).coerceAtMost(0.62f)),
            5.2f + brightness * 2.4f,
            Offset(0f, -12f),
        )
        drawCircle(Color(0xFFFFF4CF).copy(alpha = (0.35f + brightness * 0.55f).coerceAtMost(1f)), 2.6f, Offset(0f, -12f))
    }

    bindingPost(-28f, 32f)
    bindingPost(28f, 32f)
    textLabel("${visual.lampSpec?.voltage ?: 2.5f}V", 0f, 38.5f, 5.2f, Color(0xFFC4B59A), true)
}

// ---------------------------------------------------------------------------
// Motor
// ---------------------------------------------------------------------------

private fun DrawScope.drawMotor(visual: InstrumentVisual) {
    softShadow(0f, 38f, 31f, 6.5f, 0.5f)
    surfacePanel(-32f, 22f, 32f, 37f, 5f, Color(0xFF3A414C), Color(0xFF151920), Color(0xFF07090C), 1.1f)

    if (visual.energized) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF69E3FF).copy(alpha = 0.34f), Color.Transparent),
                center = Offset(8f, 2f),
                radius = 40f,
            ),
            radius = 40f,
            center = Offset(8f, 2f),
        )
    }

    // Motor can
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFDDE4EC), Color(0xFF9AA5B4), Color(0xFF4F5865)),
            startY = -29f,
            endY = 22f,
        ),
        topLeft = Offset(-27f, -29f),
        size = Size(47f, 51f),
        cornerRadius = CornerRadius(12f, 12f),
    )
    drawRoundRect(
        color = MetalDark.copy(alpha = 0.6f),
        topLeft = Offset(-27f, -29f),
        size = Size(47f, 51f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(1.1f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.22f),
        topLeft = Offset(-22f, -25f),
        size = Size(4f, 37f),
        cornerRadius = CornerRadius(2f, 2f),
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFF4B5360), Color(0xFF15191F))),
        radius = 14f,
        center = Offset(-6f, -3f),
    )
    drawCircle(Color(0xFF596273), 14f, Offset(-6f, -3f), style = Stroke(1.3f))
    textLabel("M", -6f, 1.5f, 10f, Color(0xFFE7EDF6), true)

    drawRoundRect(
        color = Color(0xFF373E49),
        topLeft = Offset(19f, -14f),
        size = Size(10f, 28f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawLine(MetalMid, Offset(29f, 0f), Offset(39f, 0f), 4.6f, StrokeCap.Round)
    drawLine(MetalLight, Offset(29f, -1.2f), Offset(39f, -1.2f), 1.2f, StrokeCap.Round)
    drawCircle(MetalDark, 5.5f, Offset(39f, 0f))
    drawCircle(MetalMid, 4.1f, Offset(39f, 0f))
    for (i in 0 until 3) {
        drawArc(
            color = Color(0xFF8FE8FF).copy(alpha = if (visual.energized) 0.55f else 0.08f),
            startAngle = -70f + i * 58f,
            sweepAngle = 34f,
            useCenter = false,
            topLeft = Offset(31f, -10f),
            size = Size(18f, 20f),
            style = Stroke(1.1f, cap = StrokeCap.Round),
        )
    }

    // Cooling fan: rotates when energized, reverses with current direction.
    withTransform({
        translate(40f, 0f)
        rotate(visual.fanAngle, pivot = Offset.Zero)
    }) {
        drawCircle(
            Color(0xFF1A2028).copy(alpha = 0.9f),
            12.5f,
            Offset.Zero,
        )
        drawCircle(
            if (visual.energized) Color(0xFF8FE8FF).copy(alpha = 0.45f) else Color(0xFF596273),
            12.5f,
            Offset.Zero,
            style = Stroke(1.1f),
        )
        for (i in 0 until 4) {
            withTransform({ rotate(i * 90f, pivot = Offset.Zero) }) {
                drawOval(
                    color = if (visual.energized) {
                        Color(0xFFBDEBFF).copy(alpha = 0.68f)
                    } else {
                        Color(0xFF778391).copy(alpha = 0.55f)
                    },
                    topLeft = Offset(-3.1f, -10.5f),
                    size = Size(6.2f, 11f),
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.35f),
                    startAngle = 200f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(-3.1f, -10.5f),
                    size = Size(6.2f, 11f),
                    style = Stroke(0.7f),
                )
            }
        }
        drawCircle(Color(0xFFE7EDF6), 3.1f, Offset.Zero)
        drawCircle(Color(0xFF6B7889), 1.4f, Offset.Zero)
    }

    drawWireLead(Offset(-25f, 23f), Offset(-25f, 37f), 1.7f)
    drawWireLead(Offset(8f, 23f), Offset(25f, 37f), 1.7f)
    drawWireLead(Offset(8f, 23f), Offset(25f, 26f), 1.7f)
    bindingPost(-25f, 37f, Color(0xFFDA3943))
    bindingPost(25f, 37f, Color(0xFF1C1F25))
}

// ---------------------------------------------------------------------------
// Ammeter and voltmeter
// ---------------------------------------------------------------------------

private fun DrawScope.drawGalvanometerMeter(visual: InstrumentVisual) {
    softShadow(0f, 44f, 39f, 6.5f, 0.55f)
    surfacePanel(-39f, -40f, 39f, 37f, 8f, Color(0xFF343B47), Color(0xFF11151C), Color(0xFF07090C), 1.3f)
    drawCircle(Color(0xFFEADFC8), 27f, Offset(0f, -4f))
    drawCircle(Color(0xFF4B3520), 27f, Offset(0f, -4f), style = Stroke(1f))
    drawLine(Color(0xFF6B5A3D), Offset(-21f, 3f), Offset(21f, 3f), 0.8f)
    val fraction = (visual.meterFraction ?: 0f).coerceIn(-1f, 1f)
    val angle = Math.toRadians((270.0 + fraction * 62.0))
    val tip = Offset((cos(angle) * 22f).toFloat(), (-4f + (sin(angle) * 22f)).toFloat())
    drawLine(Color(0xFFD83942), Offset(0f, 10f), tip, 1.5f, StrokeCap.Round)
    drawCircle(Color(0xFF2A2118), 3f, Offset(0f, 10f))
    textLabel("G", 0f, -18f, 13f, Color(0xFF4F412C), true)
    textLabel("μA", 0f, 18f, 5.5f, Color(0xFF6B5A3D), true)
    textLabel("−300       0       +300", 0f, 27f, 4.1f, Color(0xFF8D7B60), true)
    bindingPost(-26f, 39f, Color(0xFF3A404B))
    bindingPost(26f, 39f, Color(0xFF3A404B))
}

private fun DrawScope.drawMultimeter(visual: InstrumentVisual) {
    softShadow(0f, 48f, 48f, 7f, 0.55f)
    surfacePanel(-50f, -50f, 50f, 49f, 9f, Color(0xFF303947), Color(0xFF141A22), Color(0xFF080B10), 1.4f)
    drawRoundRect(
        color = Color(0xFF465365),
        topLeft = Offset(-46f, -46f),
        size = Size(92f, 90f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(1.0f),
    )

    // LCD
    drawRoundRect(
        color = Color(0xFF10231B),
        topLeft = Offset(-42f, -43f),
        size = Size(84f, 35f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawRoundRect(
        color = Color(0xFF6FE0A2).copy(alpha = 0.55f),
        topLeft = Offset(-41f, -42f),
        size = Size(82f, 33f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(0.8f),
    )
    val modeLabel = visual.multimeterMode?.symbol ?: "DCV"
    textLabel(modeLabel, -35f, -36f, 5.5f, Color(0xFF8FE8B9), true)
    textLabel(
        visual.multimeterDisplay.ifBlank { "0.00" },
        0f,
        -18f,
        10f,
        Color(0xFFB6FFD4),
        true,
    )
    textLabel(
        visual.meterRangeLabel.ifBlank { "20V" },
        33f,
        -5f,
        4.8f,
        Color(0xFF8FE8B9),
        true,
    )
    if (visual.meterOl) {
        textLabel("OL", 30f, -36f, 5.2f, Color(0xFFFFB4A8), true)
    }

    // Rotary range switch
    drawCircle(Color(0xFF202833), 12f, Offset(0f, 31f))
    drawCircle(Color(0xFF0C1016), 9f, Offset(0f, 31f))
    drawLine(
        Color(0xFFD9E2EF),
        Offset(0f, 31f),
        Offset(0f, 22f),
        1.6f,
        StrokeCap.Round,
    )
    textLabel("M", 0f, 31f, 6f, Color(0xFF8FA3BC), true)

    // Jacks
    bindingPost(-34f, 43f, Color(0xFF202833))
    bindingPost(0f, 43f, Color(0xFFD83942))
    bindingPost(34f, 43f, Color(0xFF202833))
    textLabel("COM", -34f, 49f, 4f, Color(0xFF9FAEC1), true)
    textLabel("VΩ", 0f, 49f, 4f, Color(0xFFD9E2EF), true)
    textLabel("A", 34f, 49f, 4f, Color(0xFFD9E2EF), true)
}

private fun DrawScope.drawMeter(visual: InstrumentVisual, isAmmeter: Boolean) {
    softShadow(0f, 44f, 39f, 6.5f, 0.55f)
    surfacePanel(-39f, -40f, 39f, 37f, 8f, Color(0xFF343B47), Color(0xFF11151C), Color(0xFF07090C), 1.3f)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.10f),
        topLeft = Offset(-34f, -37f),
        size = Size(68f, 2.2f),
        cornerRadius = CornerRadius(1f, 1f),
    )

    // Cream dial
    drawRoundRect(
        color = Color(0xFF3E3427).copy(alpha = 0.8f),
        topLeft = Offset(-31f, -34f),
        size = Size(62f, 59f),
        cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFFBEF), Color(0xFFF0E6D0), Color(0xFFD8CCB2)),
            startY = -32f,
            endY = 23f,
        ),
        topLeft = Offset(-29.5f, -32.5f),
        size = Size(59f, 56f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawRoundRect(
        color = Color(0xFFB7A98E),
        topLeft = Offset(-29.5f, -32.5f),
        size = Size(59f, 56f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(0.8f),
    )

    val pivot = Offset(0f, 19f)
    drawArc(
        color = Color(0xFF544A3B),
        startAngle = 225f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = Offset(pivot.x - 25f, pivot.y - 25f),
        size = Size(50f, 50f),
        style = Stroke(1.0f, cap = StrokeCap.Round),
    )
    drawArc(
        color = Color(0xFF8A7A60),
        startAngle = 231f,
        sweepAngle = 88f,
        useCenter = false,
        topLeft = Offset(pivot.x - 21f, pivot.y - 21f),
        size = Size(42f, 42f),
        style = Stroke(0.7f),
    )
    for (i in 0..10) {
        val angle = Math.toRadians((225f + 10f * i).toDouble())
        val outer = 25f
        val inner = if (i % 5 == 0) 18.5f else 21f
        drawLine(
            Color(0xFF4A4135),
            Offset(pivot.x + cos(angle).toFloat() * inner, pivot.y + sin(angle).toFloat() * inner),
            Offset(pivot.x + cos(angle).toFloat() * outer, pivot.y + sin(angle).toFloat() * outer),
            0.8f,
            StrokeCap.Round,
        )
    }
    for (i in 0..4) {
        val angle = Math.toRadians((208.33 + (225f - 208.33f) * i / 5f).toDouble())
        val inner = if (i == 0) 18.5f else 21f
        drawLine(
            Color(0xFF4A4135),
            Offset(pivot.x + cos(angle).toFloat() * inner, pivot.y + sin(angle).toFloat() * inner),
            Offset(pivot.x + cos(angle).toFloat() * 25f, pivot.y + sin(angle).toFloat() * 25f),
            if (i == 0) 0.9f else 0.7f,
            StrokeCap.Round,
        )
    }

    val rangeLabelForTicks = visual.meterRangeLabel.ifBlank { if (isAmmeter) "0.6A" else "3V" }
    val rangeValue = when {
        rangeLabelForTicks.contains("0.6") -> 0.6f
        rangeLabelForTicks.contains("15") -> 15f
        else -> 3f
    }
    for (i in 0..3) {
        val fraction = i / 3f
        val angle = Math.toRadians((225f + 100f * fraction).toDouble())
        val tickValue = rangeValue * fraction
        val text = if (rangeValue < 1f) {
            String.format(java.util.Locale.US, "%.1f", tickValue)
        } else {
            String.format(java.util.Locale.US, "%.0f", tickValue)
        }
        val tx = pivot.x + cos(angle).toFloat() * 31f
        val ty = pivot.y + sin(angle).toFloat() * 31f + 1.5f
        textLabel(text, tx, ty, 4.2f, Color(0xFF5C5446), true)
    }

    val fraction = (visual.meterFraction ?: 0.12f).coerceIn(-1f / 6f, 1f)
    val needleAngle = Math.toRadians((225f + 100f * fraction).toDouble())
    val needleEnd = Offset(
        pivot.x + cos(needleAngle).toFloat() * 24f,
        pivot.y + sin(needleAngle).toFloat() * 24f,
    )
    drawLine(
        color = Color(0xFFB61F2A).copy(alpha = 0.28f),
        start = pivot + Offset(0.8f, 1.2f),
        end = needleEnd + Offset(0.8f, 1.2f),
        strokeWidth = 1.8f,
        cap = StrokeCap.Round,
    )
    drawLine(Color(0xFF9E1C25), pivot, needleEnd, 1.15f, StrokeCap.Round)
    drawCircle(Color(0xFF2A2621), 3.2f, pivot)
    drawCircle(Color(0xFF756A58), 1.7f, pivot)

    textLabel(if (isAmmeter) "A" else "V", 0f, 6f, 14f, Color(0xFF2D2A24), true)
    val rangeText = visual.meterRangeLabel.ifBlank { if (isAmmeter) "0.6A" else "3V" }
    textLabel(
        rangeText,
        0f,
        15.5f,
        5f,
        if (visual.meterOverRange) Color(0xFFB21F2A) else Color(0xFF5C5446),
        true,
    )
    if (visual.meterOverRange) {
        drawCircle(Color(0xFFB21F2A), 3.2f, Offset(23f, -26f))
        textLabel("!", 23f, -24.4f, 4.6f, Color.White, true)
    }

    val smallLabel = if (isAmmeter) "0.6" else "3"
    val largeLabel = if (isAmmeter) "3" else "15"
    val negativeX = if (visual.meterFlipped) 26f else -26f
    val smallX = 0f
    val largeX = if (visual.meterFlipped) -26f else 26f
    textLabel("−", negativeX, 29f, 6.2f, Color(0xFFC8D4E2), true)
    textLabel(smallLabel, smallX, 29f, 6.0f, Color(0xFFFF8D94), true)
    textLabel(largeLabel, largeX, 29f, 6.0f, Color(0xFFFF8D94), true)

    // Common negative, small-range positive, large-range positive.
    drawWireLead(Offset(negativeX, 31f), Offset(negativeX, 39f), 1.4f)
    drawWireLead(Offset(smallX, 31f), Offset(smallX, 39f), 1.4f)
    drawWireLead(Offset(largeX, 31f), Offset(largeX, 39f), 1.4f)
    bindingPost(negativeX, 39f, Color(0xFF1D2026))
    bindingPost(smallX, 39f, Color(0xFFD83942))
    bindingPost(largeX, 39f, Color(0xFFD83942))
    val activeX = when (visual.meterTerminal) {
        2 -> largeX
        0 -> negativeX
        else -> smallX
    }
    drawCircle(
        color = Color(0xFFFFC857).copy(alpha = 0.75f),
        radius = 8.6f,
        center = Offset(activeX, 39f),
        style = Stroke(1.1f),
    )
}

// ---------------------------------------------------------------------------
// Switch
// ---------------------------------------------------------------------------

private fun DrawScope.drawSwitch(visual: InstrumentVisual) {
    softShadow(0f, 27f, 41f, 5.5f, 0.52f)
    drawRoundRect(
        color = Color(0xFF12100D),
        topLeft = Offset(-39.5f, -11.5f),
        size = Size(79f, 36f),
        cornerRadius = CornerRadius(9f, 9f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFF9E9), Color(0xFFE3D8C1), Color(0xFFB2A58C)),
            startY = -10f,
            endY = 23f,
        ),
        topLeft = Offset(-38f, -10f),
        size = Size(76f, 33f),
        cornerRadius = CornerRadius(8f, 8f),
    )
    drawRoundRect(
        color = Color(0xFF8E826C).copy(alpha = 0.55f),
        topLeft = Offset(-38f, -10f),
        size = Size(76f, 33f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(0.9f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(-34f, -7.8f),
        size = Size(68f, 2.2f),
        cornerRadius = CornerRadius(1f, 1f),
    )
    drawCircle(Color(0xFF8A7E68), 1.6f, Offset(-32f, 17f))
    drawCircle(Color(0xFF8A7E68), 1.6f, Offset(32f, 17f))

    drawLine(Color(0xFF5B4E39), Offset(-35f, 13f), Offset(35f, 13f), 2.2f, StrokeCap.Round)
    bindingPost(-35f, 13f, Color(0xFF3A404B))
    bindingPost(35f, 13f, Color(0xFF3A404B))

    val tip = if (visual.closed) Offset(32f, 13f) else Offset(26f, -17f)
    drawLine(
        color = Color.Black.copy(alpha = 0.35f),
        start = Offset(-34f, 15.2f),
        end = tip + Offset(0.8f, 2.2f),
        strokeWidth = 6.6f,
        cap = StrokeCap.Round,
    )
    drawLine(
        brush = Brush.linearGradient(listOf(MetalLight, MetalMid, MetalDark), start = Offset(-35f, 13f), end = tip),
        start = Offset(-35f, 13f),
        end = tip,
        strokeWidth = 5.2f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.62f),
        start = Offset(-34f, 11.2f),
        end = tip + Offset(-0.8f, -1.2f),
        strokeWidth = 1.2f,
        cap = StrokeCap.Round,
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFF8A80), Color(0xFF8C161D)), center = tip, radius = 8f),
        radius = 5.2f,
        center = tip,
    )
    drawCircle(Color(0xFF5A0A10), 5.2f, tip, style = Stroke(0.8f))
    textLabel("S", 0f, 31.5f, 5.6f, Color(0xFF8D7D63), true)
}

// ---------------------------------------------------------------------------
// SPDT switch, capacitor, inductor
// ---------------------------------------------------------------------------

private fun DrawScope.drawSpdtSwitch(visual: InstrumentVisual) {
    softShadow(0f, 30f, 48f, 6f, 0.52f)
    drawRoundRect(
        color = Color(0xFF12100D),
        topLeft = Offset(-46f, -23f),
        size = Size(92f, 52f),
        cornerRadius = CornerRadius(9f, 9f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFF9E9), Color(0xFFE3D8C1), Color(0xFFB2A58C)),
            startY = -22f,
            endY = 28f,
        ),
        topLeft = Offset(-44f, -21f),
        size = Size(88f, 48f),
        cornerRadius = CornerRadius(8f, 8f),
    )
    bindingPost(0f, 24f, Color(0xFF3A404B))
    bindingPost(-44f, 24f, Color(0xFF3A404B))
    bindingPost(44f, 24f, Color(0xFF3A404B))
    val tip = when (visual.switchPosition) {
        0 -> Offset(-40f, 12f)
        1 -> Offset(40f, 12f)
        else -> Offset(0f, 4f)
    }
    drawLine(Color(0xFF5B4E39), Offset(0f, 18f), tip, 4.6f, StrokeCap.Round)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFF8A80), Color(0xFF8C161D)), center = tip, radius = 8f),
        radius = 5f,
        center = tip,
    )
    textLabel("左", -44f, 38f, 4.6f, Color(0xFF8D7D63), true)
    textLabel("中", 0f, 38f, 4.6f, Color(0xFF8D7D63), true)
    textLabel("右", 44f, 38f, 4.6f, Color(0xFF8D7D63), true)
    textLabel("S₂", 0f, -14f, 5.2f, Color(0xFF8D7D63), true)
}

private fun DrawScope.drawCapacitor(visual: InstrumentVisual) {
    softShadow(0f, 36f, 40f, 6f, 0.55f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF253C58), Color(0xFF12243A), Color(0xFF0A1524)),
            startY = -34f,
            endY = 34f,
        ),
        topLeft = Offset(-30f, -32f),
        size = Size(60f, 64f),
        cornerRadius = CornerRadius(12f, 12f),
    )
    drawRoundRect(
        color = Color(0xFF6FB8FF).copy(alpha = 0.35f),
        topLeft = Offset(-24f, -26f),
        size = Size(48f, 2f),
        cornerRadius = CornerRadius(1f, 1f),
    )
    drawLine(Color(0xFFB9D9FF), Offset(-18f, -28f), Offset(-18f, 28f), 3f, StrokeCap.Round)
    drawLine(Color(0xFFB9D9FF), Offset(18f, -28f), Offset(18f, 28f), 3f, StrokeCap.Round)
    bindingPost(-24f, 38f, if (visual.polarityFlipped) Color(0xFFB9D9FF) else Color(0xFFD83942))
    bindingPost(24f, 38f, if (visual.polarityFlipped) Color(0xFFD83942) else Color(0xFFB9D9FF))
    textLabel("${visual.capacitanceUf.toInt()}µF", 0f, 4f, 6.4f, Color(0xFFEAF5FF), true)
    textLabel(if (visual.polarityFlipped) "−" else "+", -24f, -45f, 5.4f,
        if (visual.polarityFlipped) Color(0xFFB9D9FF) else Color(0xFFFFD36A), true)
    textLabel(if (visual.polarityFlipped) "+" else "−", 24f, -45f, 5.4f,
        if (visual.polarityFlipped) Color(0xFFFFD36A) else Color(0xFFB9D9FF), true)
    textLabel("C", 0f, 49f, 5.8f, Color(0xFF7FC8FF), true)
}

private fun DrawScope.drawInductor(visual: InstrumentVisual) {
    softShadow(0f, 28f, 50f, 5.5f, 0.55f)
    drawRoundRect(
        color = Color(0xFF2C2118),
        topLeft = Offset(-48f, -25f),
        size = Size(96f, 46f),
        cornerRadius = CornerRadius(9f, 9f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF9A6A3C), Color(0xFF5E3C23), Color(0xFF2A1A10)),
            startY = -24f,
            endY = 20f,
        ),
        topLeft = Offset(-45f, -22f),
        size = Size(90f, 40f),
        cornerRadius = CornerRadius(8f, 8f),
    )
    for (i in 0..3) {
        drawArc(
            color = Color(0xFFE3B26E),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(-36f + i * 18f, -16f),
            size = Size(18f, 24f),
            style = Stroke(2.4f),
        )
    }
    bindingPost(-48f, 30f, Color(0xFF3A404B))
    bindingPost(48f, 30f, Color(0xFF3A404B))
    textLabel("${visual.inductanceMh.toInt()}mH", 0f, 3f, 6.2f, Color(0xFFFFE2B8), true)
    textLabel("L", 0f, 38f, 5.8f, Color(0xFFE3B26E), true)
}

// ---------------------------------------------------------------------------
// Rheostat
// ---------------------------------------------------------------------------

private fun DrawScope.drawRheostat(visual: InstrumentVisual) {
    softShadow(0f, 43f, 55f, 7.5f, 0.55f)
    surfacePanel(-55f, 22f, 55f, 38f, 6f, Color(0xFF363E4A), Color(0xFF11151B), Color(0xFF07090C), 1.2f)
    drawRoundRect(
        color = Color(0xFF0B0E12),
        topLeft = Offset(-48f, 35f),
        size = Size(14f, 8f),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawRoundRect(
        color = Color(0xFF0B0E12),
        topLeft = Offset(34f, 35f),
        size = Size(14f, 8f),
        cornerRadius = CornerRadius(3f, 3f),
    )

    // Ceramic tube
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFF5DC), Color(0xFFE2CEAA), Color(0xFF8D7654)),
            startY = -8f,
            endY = 16f,
        ),
        topLeft = Offset(-48f, -8f),
        size = Size(96f, 26f),
        cornerRadius = CornerRadius(11f, 11f),
    )
    drawRoundRect(
        color = Color(0xFF67563E).copy(alpha = 0.55f),
        topLeft = Offset(-48f, -8f),
        size = Size(96f, 26f),
        cornerRadius = CornerRadius(11f, 11f),
        style = Stroke(0.9f),
    )
    for (i in 0..30) {
        val x = -44f + i * 2.9f
        drawLine(
            color = if (i % 2 == 0) CopperMid else Color(0xFFA75222),
            start = Offset(x, -3.2f),
            end = Offset(x, 12.5f),
            strokeWidth = 1.25f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = CopperLight.copy(alpha = 0.55f),
            start = Offset(x - 0.4f, -2.5f),
            end = Offset(x - 0.4f, 10.8f),
            strokeWidth = 0.45f,
        )
    }

    // Top guide rod and brackets
    drawLine(Color(0xFF20262E), Offset(-50f, -22f), Offset(50f, -22f), 5.2f, StrokeCap.Round)
    drawLine(MetalLight, Offset(-50f, -23.4f), Offset(50f, -23.4f), 2.2f, StrokeCap.Round)
    drawLine(MetalDark, Offset(-50f, -20.4f), Offset(50f, -20.4f), 1.2f, StrokeCap.Round)
    drawLine(MetalMid, Offset(-48f, -21f), Offset(-48f, 16f), 3.2f, StrokeCap.Round)
    drawLine(MetalMid, Offset(48f, -21f), Offset(48f, 16f), 3.2f, StrokeCap.Round)

    // Slider
    val sliderX = -42f + visual.slider.coerceIn(0f, 1f) * 84f
    drawLine(
        color = Color(0xFF3F4854),
        start = Offset(sliderX, -31f),
        end = Offset(sliderX, 6f),
        strokeWidth = 3.6f,
        cap = StrokeCap.Round,
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(MetalLight, MetalMid, MetalDark)),
        topLeft = Offset(sliderX - 6.5f, -40f),
        size = Size(13f, 19f),
        cornerRadius = CornerRadius(3.5f, 3.5f),
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFFD877), Color(0xFFB87B16)), center = Offset(sliderX - 1.5f, -37f), radius = 9f),
        radius = 5.2f,
        center = Offset(sliderX, -36f),
    )
    drawLine(Color(0xFFFFE39B), Offset(sliderX, -31f), Offset(sliderX, -4f), 0.8f)

    // Lower and upper terminals
    drawLine(MetalMid, Offset(-48f, 15f), Offset(-48f, 34f), 2.0f)
    drawLine(MetalMid, Offset(48f, 15f), Offset(48f, 34f), 2.0f)
    drawLine(MetalMid, Offset(-40f, -22f), Offset(-30f, -33f), 1.8f)
    drawLine(MetalMid, Offset(40f, -22f), Offset(30f, -33f), 1.8f)
    bindingPost(-48f, 34f, Color(0xFF242932))
    bindingPost(48f, 34f, Color(0xFF242932))
    bindingPost(-30f, -33f, Color(0xFF242932))
    bindingPost(30f, -33f, Color(0xFF242932))
    textLabel("A", -43f, 49f, 5.4f, Color(0xFF8D99AA), true)
    textLabel("B", 43f, 49f, 5.4f, Color(0xFF8D99AA), true)
    textLabel("C", -34f, -39f, 5.4f, Color(0xFF8D99AA), true)
    textLabel("D", 34f, -39f, 5.4f, Color(0xFF8D99AA), true)
    textLabel("${visual.rheostatMax.toInt()}Ω", 0f, 48f, 5.4f, Color(0xFF9CAAAC), true)
}

// ---------------------------------------------------------------------------
// Fixed resistor
// ---------------------------------------------------------------------------

private fun DrawScope.drawResistor() {
    softShadow(0f, 20f, 40f, 4.5f, 0.46f)
    drawLine(Color(0xFF151A21), Offset(-55f, 0f), Offset(55f, 0f), 3.6f, StrokeCap.Round)
    drawLine(MetalLight, Offset(-55f, -0.8f), Offset(55f, -0.8f), 1.2f, StrokeCap.Round)
    bindingPost(-55f, 0f)
    bindingPost(55f, 0f)

    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFF4D8), Color(0xFFE6D1A8), Color(0xFF9F8760)),
            startY = -14f,
            endY = 14f,
        ),
        topLeft = Offset(-35f, -13f),
        size = Size(70f, 26f),
        cornerRadius = CornerRadius(11f, 11f),
    )
    drawRoundRect(
        color = Color(0xFF56452F).copy(alpha = 0.55f),
        topLeft = Offset(-35f, -13f),
        size = Size(70f, 26f),
        cornerRadius = CornerRadius(11f, 11f),
        style = Stroke(1f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.4f),
        topLeft = Offset(-28f, -10.2f),
        size = Size(48f, 3.2f),
        cornerRadius = CornerRadius(1.5f, 1.5f),
    )
    val bands = listOf(
        -16f to Color(0xFF6A4226),
        -8f to Color(0xFF181818),
        1f to Color(0xFF151515),
        12f to Color(0xFFC99A3B),
    )
    for ((x, color) in bands) {
        drawRoundRect(
            color = color,
            topLeft = Offset(x, -12.7f),
            size = Size(4.2f, 25.4f),
            cornerRadius = CornerRadius(1.5f, 1.5f),
        )
    }
    textLabel("10Ω", 0f, 32f, 5.3f, Color(0xFF9CA8B8), true)
}

// ---------------------------------------------------------------------------
// Electromagnet / solenoid
// ---------------------------------------------------------------------------

private fun DrawScope.drawElectromagnet(visual: InstrumentVisual) {
    softShadow(0f, 34f, 42f, 7f, 0.52f)
    if (visual.energized) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF78B7FF).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = 56f,
            ),
            radius = 56f,
            center = Offset(0f, 0f),
        )
        for (i in 0 until 3) {
            drawArc(
                color = Color(0xFF83C6FF).copy(alpha = 0.24f - i * 0.055f),
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(-32f - i * 7f, -26f - i * 5f),
                size = Size(64f + i * 14f, 52f + i * 10f),
                style = Stroke(1.2f, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color(0xFFFF9D9D).copy(alpha = 0.18f - i * 0.04f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(-32f - i * 7f, -26f - i * 5f),
                size = Size(64f + i * 14f, 52f + i * 10f),
                style = Stroke(1.0f, cap = StrokeCap.Round),
            )
        }
    }

    // Iron core (a large nail)
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(MetalLight, MetalMid, MetalDark), startY = -10f, endY = 11f),
        topLeft = Offset(-45f, -9f),
        size = Size(72f, 20f),
        cornerRadius = CornerRadius(9f, 9f),
    )
    drawCircle(
        brush = Brush.radialGradient(listOf(MetalLight, MetalMid, MetalDark), center = Offset(-42f, -4f), radius = 14f),
        radius = 12f,
        center = Offset(-38f, 1f),
    )
    drawCircle(MetalDark, 12f, Offset(-38f, 1f), style = Stroke(0.9f))
    drawLine(Color(0xFF4D5663), Offset(-38f, -11f), Offset(-38f, 13f), 1.1f)

    // Copper winding
    for (i in 0 until 14) {
        val x = -27f + i * 4.4f
        drawOval(
            color = if (i % 2 == 0) CopperMid else Color(0xFFAE5524),
            topLeft = Offset(x - 2.3f, -14f),
            size = Size(4.6f, 29f),
        )
        drawArc(
            color = CopperLight.copy(alpha = 0.72f),
            startAngle = 245f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(x - 2.2f, -13.4f),
            size = Size(4.4f, 26.8f),
            style = Stroke(0.7f),
        )
    }
    val windingDir = if (visual.windingReversed) -1f else 1f
    for (i in 0 until 3) {
        val x = -13f + i * 13f
        drawLine(
            CopperLight,
            Offset(x - 3f * windingDir, -19f),
            Offset(x, -16.5f),
            1.5f,
            StrokeCap.Round,
        )
        drawLine(
            CopperLight,
            Offset(x + 3f * windingDir, -19f),
            Offset(x, -16.5f),
            1.5f,
            StrokeCap.Round,
        )
    }
    drawLine(Color(0xFF73320E), Offset(-28f, -14f), Offset(-28f, -22f), 1.3f)
    drawLine(Color(0xFF73320E), Offset(34f, -12f), Offset(34f, -22f), 1.3f)
    drawLine(Color(0xFF73320E), Offset(-28f, -22f), Offset(-41f, 35f), 1.5f)
    drawLine(Color(0xFF73320E), Offset(34f, -22f), Offset(41f, 35f), 1.5f)
    bindingPost(-41f, 35f, Color(0xFFD83942))
    bindingPost(41f, 35f, Color(0xFF1D2026))
    textLabel(
        if (visual.windingReversed) "反向绕线" else "正向绕线",
        0f,
        29.5f,
        3.8f,
        Color(0xFF8E9AAA),
        true,
    )
    if (visual.energized) {
        val northX = if (visual.magneticReversed) -44f else 31f
        val southX = if (visual.magneticReversed) 31f else -44f
        textLabel("N", northX, -21f, 6.6f, Color(0xFFFF8A8A), true)
        textLabel("S", southX, -21f, 6.6f, Color(0xFF86B9FF), true)
    }
}

private fun DrawScope.drawSolenoid(visual: InstrumentVisual) {
    softShadow(0f, 34f, 49f, 7f, 0.52f)
    if (visual.energized) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF74C7FF).copy(alpha = 0.18f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = 70f,
            ),
            radius = 70f,
            center = Offset(0f, 0f),
        )
    }
    drawRoundRect(
        color = Color(0xFF9AA9B8).copy(alpha = 0.20f),
        topLeft = Offset(-44f, -19f),
        size = Size(88f, 38f),
        cornerRadius = CornerRadius(17f, 17f),
    )
    drawRoundRect(
        color = Color(0xFF6F8194).copy(alpha = 0.45f),
        topLeft = Offset(-44f, -19f),
        size = Size(88f, 38f),
        cornerRadius = CornerRadius(17f, 17f),
        style = Stroke(0.9f),
    )
    for (i in 0 until 12) {
        val x = -40f + i * 7.2f
        drawOval(
            color = if (i % 2 == 0) CopperMid else Color(0xFFA94E21),
            topLeft = Offset(x - 3.2f, -18f),
            size = Size(6.4f, 36f),
        )
        drawArc(
            color = CopperLight.copy(alpha = 0.65f),
            startAngle = 240f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(x - 3.1f, -17.4f),
            size = Size(6.2f, 34.8f),
            style = Stroke(0.8f),
        )
    }
    val windingDir = if (visual.windingReversed) -1f else 1f
    for (i in 0 until 4) {
        val x = -28f + i * 18f
        drawLine(
            CopperLight,
            Offset(x - 4f * windingDir, -23f),
            Offset(x, -20.5f),
            1.7f,
            StrokeCap.Round,
        )
        drawLine(
            CopperLight,
            Offset(x + 4f * windingDir, -23f),
            Offset(x, -20.5f),
            1.7f,
            StrokeCap.Round,
        )
    }
    drawLine(Color(0xFF73320E), Offset(-42f, 16f), Offset(-49f, 31f), 1.5f)
    drawLine(Color(0xFF73320E), Offset(42f, 16f), Offset(49f, 31f), 1.5f)
    bindingPost(-49f, 31f, Color(0xFF1D2026))
    bindingPost(49f, 31f, Color(0xFFD83942))
    textLabel(
        if (visual.windingReversed) "反向绕线" else "正向绕线",
        0f,
        -25f,
        4.2f,
        Color(0xFF94A2B4),
        true,
    )
    if (visual.energized) {
        val northX = if (visual.magneticReversed) 51f else -51f
        val southX = if (visual.magneticReversed) -51f else 51f
        val northColor = if (visual.magneticReversed) Color(0xFF8CC5FF) else Color(0xFFFF8C8C)
        val southColor = if (visual.magneticReversed) Color(0xFFFF8C8C) else Color(0xFF8CC5FF)
        drawCircle(northColor.copy(alpha = 0.88f), 8f, Offset(northX, -4f))
        drawCircle(southColor.copy(alpha = 0.88f), 8f, Offset(southX, -4f))
        textLabel("N", northX, -1.5f, 7f, Color(0xFF4D1010), true)
        textLabel("S", southX, -1.5f, 7f, Color(0xFF0B2444), true)
    }
    textLabel("L", 0f, 30f, 5.4f, Color(0xFF94A2B4), true)
}

// ---------------------------------------------------------------------------
// Permanent magnets and compass
// ---------------------------------------------------------------------------

private fun DrawScope.drawBarMagnet(visual: InstrumentVisual) {
    softShadow(0f, 20f, 48f, 4.5f, 0.5f)
    drawRoundRect(
        color = Color(0xFF111317),
        topLeft = Offset(-50f, -16f),
        size = Size(100f, 32f),
        cornerRadius = CornerRadius(7f, 7f),
    )
    val leftIsNorth = !visual.polarityFlipped
    drawRoundRect(
        brush = Brush.verticalGradient(
            if (leftIsNorth) {
                listOf(Color(0xFFFF6D73), Color(0xFFC51F2A), Color(0xFF700711))
            } else {
                listOf(Color(0xFF8BCBFF), Color(0xFF2B6BC8), Color(0xFF0B2C62))
            },
            startY = -15f,
            endY = 15f,
        ),
        topLeft = Offset(-49f, -15f),
        size = Size(49f, 30f),
        cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            if (leftIsNorth) {
                listOf(Color(0xFF8BCBFF), Color(0xFF2B6BC8), Color(0xFF0B2C62))
            } else {
                listOf(Color(0xFFFF6D73), Color(0xFFC51F2A), Color(0xFF700711))
            },
            startY = -15f,
            endY = 15f,
        ),
        topLeft = Offset(0f, -15f),
        size = Size(49f, 30f),
        cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(-44f, -12f),
        size = Size(88f, 3.2f),
        cornerRadius = CornerRadius(1.5f, 1.5f),
    )
    drawLine(Color(0xFF201014), Offset(0f, -14.5f), Offset(0f, 14.5f), 1.2f)
    textLabel(if (leftIsNorth) "N" else "S", -27f, 5.5f, 13f, Color.White, true)
    textLabel(if (leftIsNorth) "S" else "N", 27f, 5.5f, 13f, Color.White, true)
}

private fun DrawScope.drawHorseshoeMagnet(visual: InstrumentVisual) {
    softShadow(0f, 35f, 37f, 7f, 0.5f)
    val leftIsNorth = !visual.polarityFlipped
    val leftLeg = Brush.verticalGradient(
        if (leftIsNorth) {
            listOf(Color(0xFFFF7279), Color(0xFFC61D28), Color(0xFF69060E))
        } else {
            listOf(Color(0xFF8BCBFF), Color(0xFF2B6BC8), Color(0xFF0B2C62))
        },
        startY = -35f,
        endY = 24f,
    )
    val rightLeg = Brush.verticalGradient(
        if (leftIsNorth) {
            listOf(Color(0xFF8BCBFF), Color(0xFF2B6BC8), Color(0xFF0B2C62))
        } else {
            listOf(Color(0xFFFF7279), Color(0xFFC61D28), Color(0xFF69060E))
        },
        startY = -35f,
        endY = 24f,
    )
    drawArc(
        brush = Brush.linearGradient(
            if (leftIsNorth) {
                listOf(Color(0xFFC61D28), Color(0xFF7E3F9E), Color(0xFF2B6BC8))
            } else {
                listOf(Color(0xFF2B6BC8), Color(0xFF7E3F9E), Color(0xFFC61D28))
            },
            start = Offset(-30f, 5f),
            end = Offset(30f, 5f),
        ),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(-31f, -10f),
        size = Size(62f, 62f),
        style = Stroke(20f, cap = StrokeCap.Round),
    )
    drawRoundRect(
        brush = leftLeg,
        topLeft = Offset(-41f, -36f),
        size = Size(20f, 45f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        brush = rightLeg,
        topLeft = Offset(21f, -36f),
        size = Size(20f, 45f),
        cornerRadius = CornerRadius(5f, 5f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(-38f, -31f),
        size = Size(3.4f, 33f),
        cornerRadius = CornerRadius(1.5f, 1.5f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.24f),
        topLeft = Offset(24f, -31f),
        size = Size(3.4f, 33f),
        cornerRadius = CornerRadius(1.5f, 1.5f),
    )
    // Silver pole faces
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(MetalLight, MetalMid, MetalDark)),
        topLeft = Offset(-42f, -38f),
        size = Size(22f, 6f),
        cornerRadius = CornerRadius(2f, 2f),
    )
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(MetalLight, MetalMid, MetalDark)),
        topLeft = Offset(20f, -38f),
        size = Size(22f, 6f),
        cornerRadius = CornerRadius(2f, 2f),
    )
    textLabel(if (leftIsNorth) "N" else "S", -31f, -13f, 10f, Color.White, true)
    textLabel(if (leftIsNorth) "S" else "N", 31f, -13f, 10f, Color.White, true)
}

private fun DrawScope.drawCompass(visual: InstrumentVisual) {
    softShadow(0f, 23f, 27f, 5f, 0.55f)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFF4D48A), Color(0xFFC59636), Color(0xFF654A16)),
            center = Offset(-7f, -8f),
            radius = 37f,
        ),
        radius = 28f,
        center = Offset(0f, 0f),
    )
    drawCircle(Color(0xFF3E2E0B), 28f, Offset(0f, 0f), style = Stroke(1.4f))
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFE9F5FF), Color(0xFFA6BED2), Color(0xFF61758A)),
            center = Offset(-8f, -10f),
            radius = 26f,
        ),
        radius = 23f,
        center = Offset(0f, 0f),
    )
    drawCircle(Color.White.copy(alpha = 0.55f), 23f, Offset(0f, 0f), style = Stroke(0.8f))
    drawCircle(Color(0xFF5F4A1C), 21.5f, Offset(0f, 0f), style = Stroke(0.6f))

    for (i in 0 until 8) {
        val angle = Math.toRadians((i * 45.0) - 90.0)
        val outer = 19f
        val inner = if (i % 2 == 0) 15.5f else 17.5f
        drawLine(
            Color(0xFF5A4A2C).copy(alpha = 0.7f),
            Offset(cos(angle).toFloat() * inner, sin(angle).toFloat() * inner),
            Offset(cos(angle).toFloat() * outer, sin(angle).toFloat() * outer),
            0.8f,
            StrokeCap.Round,
        )
    }
    textLabel("N", 0f, -12.5f, 4.7f, Color(0xFF7C2A2A), true)

    rotate(degrees = visual.compassAngle, pivot = Offset.Zero) {
        val needle = Path().apply {
            moveTo(0f, -20f)
            lineTo(5.2f, 0f)
            lineTo(0f, 3.2f)
            lineTo(-5.2f, 0f)
            close()
        }
        drawPath(needle, Color(0xFFD22A34))
        val needleBack = Path().apply {
            moveTo(0f, 20f)
            lineTo(5.2f, 0f)
            lineTo(0f, 3.2f)
            lineTo(-5.2f, 0f)
            close()
        }
        drawPath(needleBack, Color(0xFFEFF5FF))
        drawPath(needle, Color(0x33FFFFFF), style = Stroke(0.6f))
        drawPath(needleBack, Color(0x55223344), style = Stroke(0.6f))
    }
    drawCircle(Color(0xFF4A3510), 4f, Offset(0f, 0f))
    drawCircle(Color(0xFFEAD28E), 2.2f, Offset(0f, 0f))
    drawCircle(Color.White.copy(alpha = 0.55f), 1f, Offset(-0.8f, -0.9f))
}

private fun DrawScope.drawPaperclips(visual: InstrumentVisual) {
    softShadow(0f, 25f, 34f, 5f, 0.42f)
    drawRoundRect(
        color = Color(0xFF20262F).copy(alpha = 0.82f),
        topLeft = Offset(-33f, -18f),
        size = Size(66f, 36f),
        cornerRadius = CornerRadius(10f, 10f),
    )
    drawRoundRect(
        color = Color(0xFF59616D).copy(alpha = 0.38f),
        topLeft = Offset(-33f, -18f),
        size = Size(66f, 36f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(0.9f),
    )
    val pull = visual.paperclipPull
    val clips = listOf(
        Triple(-23f, -8f, -24f),
        Triple(-12f, 8f, 18f),
        Triple(0f, -9f, 42f),
        Triple(10f, 7f, -32f),
        Triple(20f, -6f, 12f),
        Triple(-20f, 9f, 65f),
        Triple(7f, -1f, 80f),
        Triple(22f, 8f, -58f),
    )
    withTransform({ translate(pull.x, pull.y) }) {
        for ((x, y, angle) in clips) {
            withTransform({
                translate(x, y)
                rotate(angle, pivot = Offset.Zero)
            }) {
                drawPaperclip()
            }
        }
    }
}

private fun DrawScope.drawPaperclip() {
    val outer = Path().apply {
        moveTo(-4.2f, -7.5f)
        lineTo(-4.2f, 4.2f)
        quadraticTo(-4.2f, 8.2f, 0f, 8.2f)
        quadraticTo(4.2f, 8.2f, 4.2f, 4.2f)
        lineTo(4.2f, -7.5f)
    }
    val inner = Path().apply {
        moveTo(-1.6f, -5.2f)
        lineTo(-1.6f, 3.6f)
        quadraticTo(-1.6f, 5.3f, 0f, 5.3f)
        quadraticTo(1.6f, 5.3f, 1.6f, 3.6f)
        lineTo(1.6f, -5.2f)
    }
    drawPath(outer, Color(0xFFBFC7D2), style = Stroke(1.8f, cap = StrokeCap.Round))
    drawPath(inner, Color(0xFFDCE2EA), style = Stroke(1.35f, cap = StrokeCap.Round))
    drawPath(outer, Color.White.copy(alpha = 0.45f), style = Stroke(0.55f, cap = StrokeCap.Round))
}

/**
 * Small helper used by the library to choose a comfortable thumbnail scale.
 */
fun thumbnailScale(type: InstrumentType, boxWidth: Float, boxHeight: Float): Float {
    val widthScale = boxWidth / type.width
    val heightScale = boxHeight / type.height
    return minOf(widthScale, heightScale)
}

