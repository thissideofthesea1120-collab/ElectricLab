package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.WireColor
import com.physlab.electric.ui.render.InstrumentVisual
import com.physlab.electric.ui.render.drawInstrument
import com.physlab.electric.ui.render.formatAmmeter
import com.physlab.electric.ui.render.formatCurrent
import com.physlab.electric.ui.render.formatNumber
import com.physlab.electric.ui.render.formatVoltmeter
import com.physlab.electric.ui.render.thumbnailScale

@Composable
fun SelectionInspector(
    editor: LabEditorState,
    solution: CircuitSolution,
    showReadings: Boolean,
    modifier: Modifier = Modifier,
) {
    val component = editor.selectedComponent
    val wire = editor.selectedWire
    if (component == null && wire == null) return

    Surface(
        modifier = modifier
            .width(258.dp)
            .heightIn(max = 270.dp),
        shape = RoundedCornerShape(18.dp),
        color = LabColors.Surface.copy(alpha = 0.97f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
    ) {
        if (component != null) {
            ComponentInspector(editor, solution, component, showReadings)
        } else if (wire != null) {
            WireInspector(editor, wire.color)
        }
    }
}

@Composable
private fun ComponentInspector(
    editor: LabEditorState,
    solution: CircuitSolution,
    component: PlacedInstrument,
    showReadings: Boolean,
) {
    val density = LocalDensity.current.density
    val reading = solution.readings[component.id]
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(LabColors.Primary.copy(alpha = 0.14f), Color.Transparent),
                        ),
                    )
                    .border(1.dp, LabColors.OutlineSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxWidth().height(50.dp)) {
                    val scale = thumbnailScale(component.type, 40f, 38f) * density
                    val meterRange = reading?.meterRange ?: 0f
                    drawInstrument(
                        type = component.type,
                        center = Offset(size.width / 2f, size.height / 2f + 1f),
                        scale = scale,
                        visual = InstrumentVisual(
                            closed = component.closed,
                            lit = (reading?.power ?: 0f) > 0.03f,
                            brightness = ((reading?.power ?: 0f) / 2.5f).coerceIn(0f, 1.8f),
                            slider = component.slider,
                            meterFraction = if (meterRange > 0f) {
                                ((reading?.meterValue ?: 0f) / meterRange).coerceIn(0f, 1f)
                            } else {
                                null
                            },
                            meterRangeLabel = reading?.meterRangeLabel ?: "",
                            meterOverRange = reading?.meterOverRange == true,
                            meterTerminal = reading?.meterTerminal ?: 1,
                            batteryCells = component.batteryCells,
                            lampSpec = component.lampSpec,
                            polarityFlipped = component.polarityFlipped,
                            rheostatMax = component.rheostatMax,
                            supplyVoltage = component.supplyVoltage,
                            supplyAc = component.supplyAc,
                            storageVoltage = component.storageVoltage,
                            resistanceBoxValue = component.resistanceBoxValue,
                            energized = reading?.energized == true,
                        ),
                    )
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${component.type.displayName} · ${component.label}",
                    color = LabColors.Ink,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    component.type.category.title,
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(LabColors.SurfaceHigh)
                    .pointerInputClose { editor.clearSelection() },
                contentAlignment = Alignment.Center,
            ) {
                LabIcon(LabIcon.CLOSE, LabColors.InkMuted, Modifier.size(14.dp))
            }
        }

        if (component.damaged) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = LabColors.Red.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Red.copy(alpha = 0.35f)),
            ) {
                Text(
                    "已损坏 · 点击仪器上方的“修复”按钮",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    color = Color(0xFFFFA4AA),
                    fontSize = 9.8.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        when (component.type) {
            InstrumentType.SWITCH -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (component.closed) "开关闭合" else "开关断开",
                            color = LabColors.Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text("单击开关可直接切换", color = LabColors.InkFaint, fontSize = 9.5.sp)
                    }
                    Switch(
                        checked = component.closed,
                        onCheckedChange = { editor.toggleSwitch(component.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF052E24),
                            checkedTrackColor = LabColors.Green,
                            uncheckedThumbColor = LabColors.InkMuted,
                            uncheckedTrackColor = LabColors.SurfaceHigh,
                        ),
                    )
                }
                if (showReadings) {
                    ReadingRow("通过电流", "${formatNumber(reading?.current ?: 0f)} A")
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.BATTERY -> {
                Text("7 号电池 ${component.batteryCells} 节串联", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("长按仪器可更换电池节数 · 当前 ${formatNumber(component.voltage)} V")
                if (showReadings) {
                    ReadingRow("端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("输出电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.POWER_SUPPLY -> {
                Text(
                    "学生电源 · ${formatNumber(component.supplyVoltage)} V ${if (component.supplyAc) "交流 AC" else "直流 DC"}",
                    color = LabColors.Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                SpecText("长按仪器可调整电压与交直流模式（0～36 V）")
                if (showReadings) {
                    ReadingRow("输出电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("输出电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.STORAGE_BATTERY -> {
                Text(
                    "蓄电池 · ${formatNumber(component.storageVoltage)} V",
                    color = LabColors.Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                SpecText("长按仪器可选择 2 V / 6 V / 12 V")
                if (showReadings) {
                    ReadingRow("端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("输出电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.RHEOSTAT -> {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("接入电阻", color = LabColors.InkMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text(
                        "${formatNumber(component.rheostatResistance)} Ω",
                        color = LabColors.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = component.slider,
                    onValueChange = { editor.updateSliderLive(component.id, it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = LabColors.Primary,
                        activeTrackColor = LabColors.Primary,
                        inactiveTrackColor = LabColors.SurfaceHigh,
                    ),
                )
                Text(
                    "最大 ${formatNumber(component.rheostatMax)} Ω · 也可直接拖动仪器上的滑片",
                    color = LabColors.InkFaint,
                    fontSize = 9.sp,
                )
                val lowerConnected = editor.wires.count { it.a.componentId == component.id && it.a.index in 0..1 } +
                    editor.wires.count { it.b.componentId == component.id && it.b.index in 0..1 }
                val upperConnected = editor.wires.count { it.a.componentId == component.id && it.a.index in 2..3 } +
                    editor.wires.count { it.b.componentId == component.id && it.b.index in 2..3 }
                val hint = when {
                    lowerConnected == 0 && upperConnected == 0 -> "接线方法：一上一下，滑动才有效"
                    lowerConnected > 0 && upperConnected > 0 -> "接线正确：一上一下"
                    lowerConnected > 0 -> "只接了下方接线柱，电阻不会随滑片改变"
                    else -> "只接了上方接线柱，电阻不会随滑片改变"
                }
                Text(
                    hint,
                    color = if (lowerConnected > 0 && upperConnected > 0) LabColors.Green else LabColors.Amber,
                    fontSize = 9.6.sp,
                    lineHeight = 13.sp,
                )
                if (showReadings) {
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.DIODE -> {
                Text("普通二极管", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("单向导电，正向导通、反向截止")
            }

            InstrumentType.THERMISTOR, InstrumentType.PHOTORESISTOR -> {
                Text(
                    if (component.type == InstrumentType.THERMISTOR) "热敏电阻" else "光敏电阻",
                    color = LabColors.Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                SpecText("当前阻值 ${formatNumber(component.resistance)} Ω")
                if (showReadings) {
                    ReadingRow("发热功率", "${formatNumber(reading?.power ?: 0f)} W")
                }
            }

            InstrumentType.RESISTOR -> {
                if (component.resistanceHidden) {
                    Text("未知定值电阻（阻值待测）", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("定值电阻 ${formatNumber(component.resistance)} Ω", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    SpecText("长按仪器可更换 5 / 10 / 15 / 20 Ω")
                }
                if (showReadings) {
                    ReadingRow("两端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                    ReadingRow("发热功率", "${formatNumber(reading?.power ?: 0f)} W")
                } else {
                    ReadingHiddenNote()
                }
            }
            InstrumentType.RESISTANCE_BOX -> {
                Text("电阻箱 ${component.resistanceBoxValue} Ω", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("长按仪器可调整四个旋钮（0～9999 Ω）")
                if (showReadings) {
                    ReadingRow("两端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.RESISTANCE_WIRE -> {
                Text(
                    if (component.showWireResistance) {
                        "${component.wireSample.name} · ${formatNumber(component.wireResistance)} Ω"
                    } else {
                        component.wireSample.name
                    },
                    color = LabColors.Ink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                SpecText(
                    "${component.wireSample.material.label} · 长 " +
                        "${formatNumber(component.wireSample.length)} m · 横截面积 " +
                        "${formatNumber(component.wireSample.area)} mm²；长按可切换四种电阻丝",
                )
                if (showReadings) {
                    ReadingRow("两端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.PAPERCLIPS -> {
                Text("回形针", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("放在磁体或通电线圈附近，会被吸引并靠近磁极；移开磁体后恢复原位")
            }

            InstrumentType.SPDT_SWITCH -> {
                Text("单刀双掷开关", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText(if (component.switchPosition == 0) "当前：1 接 2" else "当前：1 接 3")
            }

            InstrumentType.CAPACITOR -> {
                Text("电容 ${formatNumber(component.capacitanceUf)} µF", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("额定电压 16 V；充电后两端有电压")
            }

            InstrumentType.INDUCTOR -> {
                Text("电感 ${formatNumber(component.inductanceMh)} mH", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("直流电阻 ${formatNumber(component.inductanceDcResistance)} Ω")
            }

            InstrumentType.AMMETER -> {
                val range = reading?.meterRangeLabel?.ifBlank { "0.6A" } ?: "0.6A"
                if (showReadings) {
                    MeterReading(
                        label = "电流表示数",
                        value = formatAmmeter(reading?.meterSignedValue ?: 0f),
                        hint = "使用 0～$range 测量范围　串联接入",
                    )
                    ReadingRow("测量范围选择", "0～$range")
                    MeterWarning(reading)
                } else {
                    Text("电流表 · 请直接读取表盘", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingRow("当前测量范围", "0～$range")
                    Text(
                        "表盘已按当前测量范围标出刻度，可自行估读。",
                        color = LabColors.InkFaint,
                        fontSize = 9.sp,
                    )
                    ReadingHiddenNote()
                }
            }

            InstrumentType.VOLTMETER -> {
                val range = reading?.meterRangeLabel?.ifBlank { "3V" } ?: "3V"
                if (showReadings) {
                    MeterReading(
                        label = "电压表示数",
                        value = formatVoltmeter(reading?.meterSignedValue ?: 0f),
                        hint = "使用 0～$range 测量范围　并联接入",
                    )
                    ReadingRow("测量范围选择", "0～$range")
                    MeterWarning(reading)
                } else {
                    Text("电压表 · 请直接读取表盘", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingRow("当前测量范围", "0～$range")
                    Text(
                        "表盘已按当前测量范围标出刻度，可自行估读。",
                        color = LabColors.InkFaint,
                        fontSize = 9.sp,
                    )
                    ReadingHiddenNote()
                }
            }

            InstrumentType.GALVANOMETER -> {
                val range = "±300 μA"
                if (showReadings) {
                    MeterReading(
                        label = "灵敏电流计示数",
                        value = "${formatNumber(reading?.meterSignedValue ?: 0f)} μA",
                        hint = "使用 $range 测量范围　串联接入",
                    )
                    ReadingRow("测量范围选择", range)
                } else {
                    Text("灵敏电流计 · 请直接读取中央零位表盘", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingRow("当前测量范围", range)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.MILLIAMMETER -> {
                val range = reading?.meterRangeLabel?.ifBlank { "50mA" } ?: "50mA"
                if (showReadings) {
                    MeterReading(
                        label = "毫安表示数",
                        value = "${formatNumber(reading?.meterSignedValue ?: 0f)} mA",
                        hint = "使用 0～$range 测量范围　串联接入",
                    )
                    ReadingRow("测量范围选择", "0～$range")
                } else {
                    Text("毫安表 · 请直接读取表盘", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingRow("当前测量范围", "0～$range")
                    ReadingHiddenNote()
                }
            }

            InstrumentType.MULTIMETER -> {
                val display = reading?.multimeterDisplay.orEmpty()
                    .ifBlank { reading?.multimeterWarning.orEmpty().ifBlank { "-" } }
                if (showReadings) {
                    MeterReading(
                        label = "多用电表示数",
                        value = display,
                        hint = "COM + VΩ/A　按档位接入",
                    )
                    ReadingRow("当前档位", component.multimeterRangeValue.label)
                } else {
                    Text("多用电表 · 打开读数查看显示值", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingRow("当前档位", component.multimeterRangeValue.label)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> {
                val spec = component.lampSpec
                val kind = if (component.type == InstrumentType.TEMP_LAMP) "热敏小灯泡" else "小灯泡"
                Text("$kind · ${spec.label}", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                SpecText("额定 ${formatNumber(spec.ratedPower)} W · 长按仪器可更换规格")
                if (showReadings) {
                    val power = reading?.power ?: 0f
                    MeterReading(
                        label = "灯泡实际功率",
                        value = "${formatNumber(power)} W",
                        hint = if (power > 0.03f) "灯泡发光" else "灯泡未发光",
                    )
                    if (power > spec.ratedPower * 1.1f) {
                        Text(
                            "实际功率超过额定值，达到 3 倍会烧坏",
                            color = LabColors.Amber,
                            fontSize = 9.2.sp,
                        )
                    }
                    ReadingRow("两端电压", "${formatNumber(reading?.voltage ?: 0f)} V")
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    ReadingHiddenNote()
                }
            }

            InstrumentType.MOTOR -> {
                if (showReadings) {
                    MeterReading(
                        label = "电动机",
                        value = if (reading?.energized == true) "转动中" else "未转动",
                        hint = "通电导体在磁场中受力转动",
                    )
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    Text("电动机 · 两个接线柱", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.BUZZER -> {
                if (showReadings) {
                    MeterReading(
                        label = "蜂鸣器",
                        value = if (reading?.energized == true) "发声" else "静音",
                        hint = "通过电流时蜂鸣器发声",
                    )
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    Text("蜂鸣器 · 两个接线柱", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.LED -> {
                if (showReadings) {
                    MeterReading(
                        label = "发光二极管",
                        value = when {
                            reading?.ledReversed == true -> "反接不亮"
                            reading?.energized == true -> "发光"
                            else -> "未发光"
                        },
                        hint = "长脚为正极，电流从正极流入时发光",
                    )
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    Text("发光二极管 · 两个接线柱", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.ELECTROMAGNET, InstrumentType.SOLENOID -> {
                if (showReadings) {
                    MeterReading(
                        label = "磁性强弱",
                        value = if (reading?.energized == true) "有磁性" else "无磁性",
                        hint = "电流越大，磁性越强；断电后磁性消失",
                    )
                    ReadingRow("通过电流", formatCurrent(reading?.current ?: 0f))
                } else {
                    Text("${component.type.displayName} · 两个接线柱", color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ReadingHiddenNote()
                }
            }

            InstrumentType.COMPASS -> {
                MeterReading(
                    label = "小磁针",
                    value = "指示磁场方向",
                    hint = "靠近磁体或通电螺线管时，磁针会发生偏转",
                )
            }

            InstrumentType.BAR_MAGNET, InstrumentType.HORSESHOE_MAGNET -> {
                MeterReading(
                    label = "磁极",
                    value = if (component.polarityFlipped) "S / N" else "N / S",
                    hint = "同名磁极相互排斥，异名磁极相互吸引；长按可翻转磁极",
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                onClick = { editor.deleteSelection() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = LabColors.Red.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Red.copy(alpha = 0.34f)),
            ) {
                Row(
                    Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabIcon(LabIcon.TRASH, LabColors.Red, Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("移除仪器", color = LabColors.Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun WireInspector(editor: LabEditorState, color: WireColor) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(LabColors.SurfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                LabIcon(LabIcon.WIRE, color.highlight, Modifier.size(21.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text("导线 · ${color.displayName}", color = LabColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("点击色块可更换导线颜色", color = LabColors.InkFaint, fontSize = 9.5.sp)
            }
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(LabColors.SurfaceHigh)
                    .pointerInputClose { editor.clearSelection() },
                contentAlignment = Alignment.Center,
            ) {
                LabIcon(LabIcon.CLOSE, LabColors.InkMuted, Modifier.size(14.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            for (wireColor in WireColor.entries) {
                WireColorChip(
                    color = wireColor,
                    selected = wireColor == color,
                    onClick = { editor.setSelectionWireColor(wireColor) },
                )
            }
        }

        val wire = editor.selectedWire
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(
                onClick = { editor.addBendToSelectedWire() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(9.dp),
                color = LabColors.Primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.32f)),
            ) {
                Text(
                    "添加弯折点",
                    modifier = Modifier.padding(vertical = 7.dp),
                    color = LabColors.Primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            if ((wire?.bends?.size ?: 0) > 0) {
                Surface(
                    onClick = { editor.clearSelectedWireBends() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = LabColors.SurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
                ) {
                    Text(
                        "拉直导线",
                        modifier = Modifier.padding(vertical = 7.dp),
                        color = LabColors.InkMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
        Text(
            "已设置 ${wire?.bends?.size ?: 0} 个弯折点，可直接拖动黄色圆点调整形状。",
            color = LabColors.InkFaint,
            fontSize = 9.sp,
            lineHeight = 13.sp,
        )

        Surface(
            onClick = { editor.deleteSelection() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = LabColors.Red.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Red.copy(alpha = 0.34f)),
        ) {
            Row(
                Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabIcon(LabIcon.TRASH, LabColors.Red, Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("删除导线", color = LabColors.Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReadingHiddenNote() {
    Text(
        "读数已隐藏 · 点击右上角「读数」开关显示",
        color = LabColors.InkFaint,
        fontSize = 9.2.sp,
        lineHeight = 13.sp,
    )
}

@Composable
private fun SpecText(text: String) {
    Text(
        text,
        color = LabColors.InkFaint,
        fontSize = 9.2.sp,
        lineHeight = 13.sp,
    )
}

@Composable
private fun MeterWarning(reading: ComponentReading?) {
    if (reading == null) return
    val messages = buildList {
        if (reading.meterOverRange) add("超过当前测量范围，请换用更大的正接线柱")
        if (reading.meterNegativeOverRange) add("指针越过负刻度线，仪表已损坏")
        else if (reading.meterReversed) add("正负接线柱接反，指针向负刻度偏转")
    }
    if (messages.isEmpty()) return
    Text(
        messages.joinToString("；"),
        color = if (reading.meterOverRange || reading.meterReversed) LabColors.Red else LabColors.Amber,
        fontSize = 9.3.sp,
        lineHeight = 13.sp,
    )
}

@Composable
private fun ReadingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = LabColors.InkMuted, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
        Text(value, color = LabColors.Primary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MeterReading(label: String, value: String, hint: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LabColors.Primary.copy(alpha = 0.07f))
            .border(1.dp, LabColors.Primary.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        Text(label, color = LabColors.InkMuted, fontSize = 9.8.sp)
        Text(value, color = LabColors.Primary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(hint, color = LabColors.InkFaint, fontSize = 9.sp, lineHeight = 12.sp)
    }
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) LabColors.Primary.copy(alpha = 0.17f) else LabColors.SurfaceRaised,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) LabColors.Primary.copy(alpha = 0.5f) else LabColors.OutlineSoft,
        ),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            color = if (selected) LabColors.Primary else LabColors.InkMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun WireColorChip(
    color: WireColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color.body.copy(alpha = 0.22f) else LabColors.SurfaceRaised,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) color.highlight.copy(alpha = 0.8f) else LabColors.OutlineSoft,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color.body)
                    .border(1.dp, color.highlight.copy(alpha = 0.65f), CircleShape),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                color.displayName,
                color = if (selected) color.highlight else LabColors.InkMuted,
                fontSize = 9.6.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun Modifier.pointerInputClose(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(Unit) {
            detectTapGestures { onClick() }
        },
    )
