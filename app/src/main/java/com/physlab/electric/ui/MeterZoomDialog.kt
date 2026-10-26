package com.physlab.electric.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.ui.render.formatAmmeter
import com.physlab.electric.ui.render.formatNumber
import com.physlab.electric.ui.render.formatVoltmeter
import com.physlab.electric.ui.render.textLabel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeterZoomDialog(
    editor: LabEditorState,
    component: PlacedInstrument,
    solution: CircuitSolution,
    showReadings: Boolean,
) {
    if (component.type == InstrumentType.GALVANOMETER) {
        GalvanometerZoomDialog(editor, component, solution, showReadings)
        return
    }
    val reading = solution.readings[component.id] ?: ComponentReading()
    val isAmmeter = component.type == InstrumentType.AMMETER
    val isMilliAmmeter = component.type == InstrumentType.MILLIAMMETER
    val unit = when {
        isAmmeter -> "A"
        isMilliAmmeter -> "mA"
        else -> "V"
    }
    val smallMax = when {
        isAmmeter -> 0.6f
        isMilliAmmeter -> 50f
        else -> 3f
    }
    val largeMax = when {
        isAmmeter -> 3f
        isMilliAmmeter -> 500f
        else -> 15f
    }
    val activeRange = reading.meterRange.takeIf { it > 0f } ?: smallMax
    val activeIsLarge = activeRange > smallMax + 0.01f
    val negativeDivisor = if (isMilliAmmeter) 5f else 6f
    val activeValue = reading.meterSignedValue
    var answer by remember(component.id, activeRange) { mutableStateOf("") }
    var feedback by remember(component.id, activeRange) { mutableStateOf<String?>(null) }
    var feedbackCorrect by remember(component.id, activeRange) { mutableStateOf(false) }
    var recorded by remember(component.id, activeRange) { mutableStateOf(false) }
    val tolerance = activeRange / 60f

    BackHandler { editor.closeMeterZoom() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f))
            .clickable { editor.closeMeterZoom() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(0.90f)
                .height(if (showReadings) 320.dp else 356.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(24.dp),
            color = LabColors.Surface.copy(alpha = 0.98f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
            shadowElevation = 24.dp,
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(LabColors.Primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                unit,
                                color = LabColors.Primary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${component.type.displayName} · 放大刻度",
                                color = LabColors.Ink,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "小测量范围在下，大测量范围在上 · 单位 $unit",
                                color = LabColors.InkFaint,
                                fontSize = 10.sp,
                            )
                        }
                        Surface(
                            onClick = { editor.closeMeterZoom() },
                            shape = RoundedCornerShape(10.dp),
                            color = LabColors.SurfaceHigh,
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LabIcon(LabIcon.CLOSE, LabColors.InkMuted, Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("关闭", color = LabColors.InkMuted, fontSize = 10.5.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h * 0.70f
                        val radius = minOf(w * 0.35f, h * 0.54f)
                        val startAngle = 205f
                        val sweepAngle = 140f
                        val unitSize = 12f * density

                        drawArc(
                            color = Color(0xFFEADFC8),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(3.2f * density, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = Color(0xFF6B5A3D).copy(alpha = 0.55f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(cx - radius - 2f * density, cy - radius - 2f * density),
                            size = Size((radius + 2f * density) * 2f, (radius + 2f * density) * 2f),
                            style = Stroke(0.8f * density),
                        )

                        val negativeSweep = sweepAngle / 6f
                        val negativeStart = startAngle - negativeSweep
                        drawArc(
                            color = Color(0xFFEADFC8),
                            startAngle = negativeStart,
                            sweepAngle = negativeSweep,
                            useCenter = false,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(3.2f * density, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = Color(0xFF6B5A3D).copy(alpha = 0.55f),
                            startAngle = negativeStart,
                            sweepAngle = negativeSweep,
                            useCenter = false,
                            topLeft = Offset(cx - radius - 2f * density, cy - radius - 2f * density),
                            size = Size((radius + 2f * density) * 2f, (radius + 2f * density) * 2f),
                            style = Stroke(0.8f * density),
                        )
                        val negativeAngleRad = Math.toRadians(negativeStart.toDouble())
                        val negativeDir = Offset(cos(negativeAngleRad).toFloat(), sin(negativeAngleRad).toFloat())
                        for (i in 0..4) {
                            val angle = Math.toRadians((negativeStart + negativeSweep * i / 5f).toDouble())
                            val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                            val major = i == 0
                            val inner = radius - (if (major) 10f else 4.5f) * density
                            val outer = radius + (if (major) 10f else 4.5f) * density
                            drawLine(
                                Color(0xFF4F412C),
                                Offset(cx + dir.x * inner, cy + dir.y * inner),
                                Offset(cx + dir.x * outer, cy + dir.y * outer),
                                (if (major) 1.5f else 0.8f) * density,
                                StrokeCap.Round,
                            )
                        }
                        val labelInnerNeg = radius - 34f * density
                        val labelOuterNeg = radius + 34f * density
                        val smallNegText = formatTick(-smallMax / negativeDivisor, 1)
                        val largeNegText = formatTick(-largeMax / negativeDivisor, 1)
                        val smallNegColor = if (!activeIsLarge) LabColors.Primary else Color(0xFF8D7B60)
                        val largeNegColor = if (activeIsLarge) LabColors.Primary else Color(0xFF8D7B60)
                        textLabel(
                            smallNegText,
                            cx + negativeDir.x * labelInnerNeg,
                            cy + negativeDir.y * labelInnerNeg + unitSize * 0.35f,
                            unitSize,
                            smallNegColor,
                            true,
                        )
                        textLabel(
                            largeNegText,
                            cx + negativeDir.x * labelOuterNeg,
                            cy + negativeDir.y * labelOuterNeg + unitSize * 0.35f,
                            unitSize,
                            largeNegColor,
                            true,
                        )

                        val scaleDivisions = if (isMilliAmmeter) 25 else 30
                        for (i in 0..scaleDivisions) {
                            val fraction = i / scaleDivisions.toFloat()
                            val angle = Math.toRadians((startAngle + sweepAngle * fraction).toDouble())
                            val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                            val major = i % 5 == 0
                            val inner = radius - (if (major) 10f else 4.5f) * density
                            val outer = radius + (if (major) 10f else 4.5f) * density
                            drawLine(
                                Color(0xFF4F412C),
                                Offset(cx + dir.x * inner, cy + dir.y * inner),
                                Offset(cx + dir.x * outer, cy + dir.y * outer),
                                (if (major) 1.5f else 0.8f) * density,
                                StrokeCap.Round,
                            )
                            if (!major) continue
                            val majorIndex = i / 5
                            val majorFraction = majorIndex / (scaleDivisions / 5f)
                            val smallValue = smallMax * majorFraction
                            val largeValue = largeMax * majorFraction
                            val smallText = formatTick(smallValue, 1)
                            val largeText = formatTick(largeValue, 1)
                            val smallColor = if (!activeIsLarge) LabColors.Primary else Color(0xFF8D7B60)
                            val largeColor = if (activeIsLarge) LabColors.Primary else Color(0xFF8D7B60)
                            val labelInner = radius - 34f * density
                            val labelOuter = radius + 34f * density
                            textLabel(
                                smallText,
                                cx + dir.x * labelInner,
                                cy + dir.y * labelInner + unitSize * 0.35f,
                                unitSize,
                                smallColor,
                                true,
                            )
                            textLabel(
                                largeText,
                                cx + dir.x * labelOuter,
                                cy + dir.y * labelOuter + unitSize * 0.35f,
                                unitSize,
                                largeColor,
                                true,
                            )
                        }

                        val fraction = (activeValue / activeRange).coerceIn(-1f / negativeDivisor, 1f)
                        val needleAngle = Math.toRadians((startAngle + sweepAngle * fraction).toDouble())
                        val needleDir = Offset(cos(needleAngle).toFloat(), sin(needleAngle).toFloat())
                        val tip = Offset(cx + needleDir.x * (radius - 2f * density), cy + needleDir.y * (radius - 2f * density))
                        drawLine(
                            Color(0xFFD83942),
                            Offset(cx, cy),
                            tip,
                            1.5f * density,
                            StrokeCap.Round,
                        )
                        drawCircle(Color(0xFF4B3520), 11f * density, Offset(cx, cy))
                        drawCircle(Color(0xFFD7C39D), 8.3f * density, Offset(cx, cy))
                        drawCircle(Color(0xFF2A2118), 3.5f * density, Offset(cx, cy))

                        textLabel(unit, cx, cy - 30f * density, 25f * density, Color(0xFF4F412C), true)
                        textLabel(
                            if (activeIsLarge) "大测量范围" else "小测量范围",
                            cx,
                            cy + 27f * density,
                            8.5f * density,
                            LabColors.Primary,
                            true,
                        )
                        textLabel(
                            "-${formatTick((if (activeIsLarge) largeMax else smallMax) / negativeDivisor, 1)}～" +
                                "${if (activeIsLarge) largeMax else smallMax} $unit",
                            cx,
                            cy + 40f * density,
                            7.6f * density,
                            Color(0xFF7C6A4D),
                            true,
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (showReadings) {
                                "当前示数 " + when {
                                    isAmmeter -> formatAmmeter(activeValue)
                                    isMilliAmmeter -> "${formatNumber(activeValue)} mA"
                                    else -> formatVoltmeter(activeValue)
                                } +
                                    when {
                                        reading.meterNegativeOverRange -> " · 超出负刻度"
                                        reading.meterOverRange -> " · 超测量范围"
                                        else -> ""
                                    }
                            } else {
                                "读数开关已关闭，请直接读取表盘指针"
                            },
                            color = if (reading.meterOverRange || reading.meterNegativeOverRange) {
                                LabColors.Red
                            } else {
                                LabColors.InkMuted
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "测量范围 -${formatNumber(activeRange / negativeDivisor)}～${formatNumber(activeRange)} $unit · 每小格 " +
                                String.format(
                                    java.util.Locale.US,
                                    "%.2f $unit",
                                    if (isMilliAmmeter) activeRange / 25f else activeRange / 30f,
                                ),
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                    }

                    if (!showReadings) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = answer,
                                onValueChange = {
                                    answer = it
                                    feedback = null
                                    recorded = false
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("输入读数（$unit，负刻度加 -）", fontSize = 10.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = LabColors.Ink,
                                    fontSize = 12.sp,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            )
                            Surface(
                                onClick = {
                                    answer = if (answer.startsWith("-")) {
                                        answer.drop(1)
                                    } else {
                                        "-$answer"
                                    }
                                    feedback = null
                                    recorded = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = LabColors.SurfaceHigh,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                            ) {
                                Text(
                                    "±",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                                    color = LabColors.InkMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Surface(
                                onClick = {
                                    val entered = answer.replace(',', '.').toFloatOrNull()
                                    when {
                                        entered == null -> {
                                            feedback = "先输入一个数字，再点验证。"
                                            feedbackCorrect = false
                                        }
                                        kotlin.math.abs(entered - activeValue) <= tolerance -> {
                                            feedback = "读数正确，观察得很仔细，继续保持！"
                                            feedbackCorrect = true
                                            editor.grantChallengeDataEntry(component.type)
                                        }
                                        else -> {
                                            feedback = "别灰心，再看一眼指针位置，你可以读对的！"
                                            feedbackCorrect = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = LabColors.Primary.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    LabColors.Primary.copy(alpha = 0.45f),
                                ),
                            ) {
                                Text(
                                    "验证",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    color = LabColors.Primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        feedback?.let { message ->
                            Text(
                                message,
                                color = if (feedbackCorrect) LabColors.Green else LabColors.Amber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        if (feedbackCorrect && !editor.challengeActive) {
                            Surface(
                                onClick = {
                                    val volts = editor.components
                                        .filter { it.type == InstrumentType.VOLTMETER }
                                        .mapNotNull { solution.readings[it.id]?.meterSignedValue }
                                    val amps = editor.components
                                        .filter { it.type == InstrumentType.AMMETER }
                                        .mapNotNull { solution.readings[it.id]?.meterSignedValue }
                                    editor.addRecord(
                                        MeasurementRecord(
                                            createdAt = System.currentTimeMillis(),
                                            sourceVoltage = solution.batteryVoltage,
                                            totalCurrent = solution.totalCurrent,
                                            voltmeterValues = volts,
                                            ammeterValues = amps,
                                        ),
                                    )
                                    recorded = true
                                    editor.notify("已记录到电压 / 电流记录表")
                                },
                                shape = RoundedCornerShape(9.dp),
                                color = LabColors.Green.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    LabColors.Green.copy(alpha = 0.45f),
                                ),
                            ) {
                                Text(
                                    "记录到表格",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = LabColors.Green,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (recorded) {
                                Text(
                                    "已记录到电压 / 电流记录表",
                                    color = LabColors.Green,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
}

private fun formatTick(value: Float, decimals: Int): String {
    return if (kotlin.math.abs(value - value.toInt()) < 0.05f) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.${decimals}f", value)
    }
}

@Composable
private fun GalvanometerZoomDialog(
    editor: LabEditorState,
    component: PlacedInstrument,
    solution: CircuitSolution,
    showReadings: Boolean,
) {
    val reading = solution.readings[component.id] ?: ComponentReading()
    val range = 300f
    val tolerance = 5f
    val activeValue = reading.meterSignedValue
    var answer by remember(component.id) { mutableStateOf("") }
    var feedback by remember(component.id) { mutableStateOf<String?>(null) }
    var feedbackCorrect by remember(component.id) { mutableStateOf(false) }

    BackHandler { editor.closeMeterZoom() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f))
            .clickable { editor.closeMeterZoom() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(0.90f)
                .height(if (showReadings) 320.dp else 360.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(24.dp),
            color = LabColors.Surface.copy(alpha = 0.98f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LabColors.Primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("G", color = LabColors.Primary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "灵敏电流计 · 放大刻度",
                            color = LabColors.Ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "−300 μA ～ 0 ～ +300 μA · 每小格 20 μA",
                            color = LabColors.InkFaint,
                            fontSize = 10.sp,
                        )
                    }
                    Surface(
                        onClick = { editor.closeMeterZoom() },
                        shape = RoundedCornerShape(10.dp),
                        color = LabColors.SurfaceHigh,
                    ) {
                        Text(
                            "关闭",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            color = LabColors.InkMuted,
                            fontSize = 10.5.sp,
                        )
                    }
                }

                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h * 0.72f
                    val radius = minOf(w * 0.36f, h * 0.56f)
                    val startAngle = 200f
                    val sweepAngle = 140f
                    val centerAngle = 270f
                    drawArc(
                        color = Color(0xFFEADFC8),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(3.2f * density, cap = StrokeCap.Round),
                    )
                    for (i in 0..30) {
                        val angle = Math.toRadians((startAngle + sweepAngle * i / 30f).toDouble())
                        val dir = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                        val major = i % 5 == 0
                        val inner = radius - (if (major) 10f else 4.5f) * density
                        val outer = radius + (if (major) 10f else 4.5f) * density
                        drawLine(
                            Color(0xFF4F412C),
                            Offset(cx + dir.x * inner, cy + dir.y * inner),
                            Offset(cx + dir.x * outer, cy + dir.y * outer),
                            (if (major) 1.5f else 0.8f) * density,
                            StrokeCap.Round,
                        )
                        if (major) {
                            val value = -range + range * 2f * i / 30f
                            textLabel(
                                if (i == 15) "0" else if (value > 0) "+${value.toInt()}" else value.toInt().toString(),
                                cx + dir.x * (radius + 24f * density),
                                cy + dir.y * (radius + 24f * density),
                                8.5f * density,
                                if (i == 15) LabColors.Primary else Color(0xFF6B5A3D),
                                true,
                            )
                        }
                    }
                    val fraction = (activeValue / range).coerceIn(-1f, 1f)
                    val needleAngle = Math.toRadians((centerAngle + fraction * sweepAngle / 2f).toDouble())
                    val dir = Offset(cos(needleAngle).toFloat(), sin(needleAngle).toFloat())
                    drawLine(
                        Color(0xFFD83942),
                        Offset(cx, cy),
                        Offset(cx + dir.x * (radius - 4f * density), cy + dir.y * (radius - 4f * density)),
                        1.7f * density,
                        StrokeCap.Round,
                    )
                    drawCircle(Color(0xFF4B3520), 11f * density, Offset(cx, cy))
                    drawCircle(Color(0xFFD7C39D), 8.3f * density, Offset(cx, cy))
                    drawCircle(Color(0xFF2A2118), 3.5f * density, Offset(cx, cy))
                    textLabel("μA", cx, cy - 26f * density, 22f * density, Color(0xFF4F412C), true)
                }

                Text(
                    if (showReadings) {
                        "当前示数 ${formatNumber(activeValue)} μA"
                    } else {
                        "读数开关已关闭，请读取中央零位指针"
                    },
                    color = if (reading.meterOverRange) LabColors.Red else LabColors.InkMuted,
                    fontSize = 11.sp,
                )
                if (!showReadings) {
                    Spacer(Modifier.height(5.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = answer,
                            onValueChange = {
                                answer = it
                                feedback = null
                                feedbackCorrect = false
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("输入读数（μA，左偏加 -）", fontSize = 10.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                        Surface(
                            onClick = {
                                val entered = answer.replace(',', '.').toFloatOrNull()
                                when {
                                    entered == null -> {
                                        feedback = "先输入一个数字，再点验证。"
                                        feedbackCorrect = false
                                    }
                                    kotlin.math.abs(entered - activeValue) <= tolerance -> {
                                        feedback = "读数正确，中央零位判断正确！"
                                        feedbackCorrect = true
                                        editor.grantChallengeDataEntry(InstrumentType.GALVANOMETER)
                                    }
                                    else -> {
                                        feedback = "再看一眼指针相对中央零位的位置。"
                                        feedbackCorrect = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = LabColors.Primary.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.45f)),
                        ) {
                            Text(
                                "验证",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = LabColors.Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    feedback?.let {
                        Text(
                            it,
                            color = if (feedbackCorrect) LabColors.Green else LabColors.Amber,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
