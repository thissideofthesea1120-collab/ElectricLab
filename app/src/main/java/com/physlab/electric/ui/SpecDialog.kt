package com.physlab.electric.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.ResistanceWireSamples
import com.physlab.electric.model.isMagnet
import com.physlab.electric.model.isMeter
import com.physlab.electric.model.isPowerSource
import com.physlab.electric.ui.render.formatAmmeter
import com.physlab.electric.ui.render.formatCurrent
import com.physlab.electric.ui.render.formatNumber
import com.physlab.electric.ui.render.formatVoltmeter

@Composable
fun InstrumentSpecDialog(
    editor: LabEditorState,
    component: PlacedInstrument,
    solution: CircuitSolution,
    showReadings: Boolean,
) {
    val reading = solution.readings[component.id] ?: ComponentReading()
    AlertDialog(
        onDismissRequest = { editor.closeSpec() },
        title = {
            Column {
                Text("${component.type.displayName} · 详情与规格", fontWeight = FontWeight.Bold)
                Text(
                    "长按仪器可再次打开此面板",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 230.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (component.type != InstrumentType.RESISTANCE_WIRE) {
                    InstrumentDetailSummary(component, reading, showReadings)
                }
                when (component.type) {
                    InstrumentType.BATTERY -> {
                        SourcePolarityRow(editor, component)
                        Text("7 号电池串联，每节 1.5 V", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (cells in 1..4) {
                                OptionChip(
                                    text = "${cells}节",
                                    selected = component.batteryCells == cells,
                                    onClick = { editor.setBatteryCells(component.id, cells) },
                                )
                            }
                        }
                        Text(
                            "当前：${component.batteryCells} 节 × 1.5 V = ${component.voltage} V",
                            color = LabColors.Primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    InstrumentType.POWER_SUPPLY -> {
                        SourcePolarityRow(editor, component)
                        Text("输出方式：直流 DC", color = LabColors.Ink, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("输出电压", color = LabColors.InkMuted, fontSize = 10.sp)
                            Spacer(Modifier.weight(1f))
                            Text(
                                String.format(java.util.Locale.US, "%.1f V", component.supplyVoltage),
                                color = LabColors.Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Slider(
                            value = component.supplyVoltage,
                            onValueChange = { editor.setSupplyVoltage(component.id, it) },
                            valueRange = 0f..36f,
                            colors = SliderDefaults.colors(
                                thumbColor = LabColors.Primary,
                                activeTrackColor = LabColors.Primary,
                                inactiveTrackColor = LabColors.SurfaceHigh,
                            ),
                        )
                        Text(
                            "范围 0～36 V；学生电源只提供直流输出，已取消交流模式。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                        )
                    }

                    InstrumentType.STORAGE_BATTERY -> {
                        SourcePolarityRow(editor, component)
                        Text("蓄电池电压", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (voltage in listOf(2f, 6f, 12f)) {
                                OptionChip("$voltage V", component.storageVoltage == voltage) {
                                    editor.setStorageVoltage(component.id, voltage)
                                }
                            }
                        }
                    }

                    InstrumentType.MOTOR -> {
                        Text(
                            "额定电压 4.5 V；实际功率越大，扇叶转得越快。",
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }

                    InstrumentType.BUZZER -> {
                        Text(
                            "额定电压 4.5 V；实际功率越大，蜂鸣器声音越响。",
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                        )
                    }

                    InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> {
                        Text(
                            if (component.type == InstrumentType.TEMP_LAMP) {
                                "热敏小灯泡：额定电压与额定功率（电阻随温度变化）"
                            } else {
                                "灯泡额定电压与额定功率"
                            },
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                        )
                        for (row in LampSpec.entries.chunked(2)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                for (spec in row) {
                                    OptionChip(spec.label, component.lampSpec == spec) {
                                        editor.setLampSpec(component.id, spec)
                                    }
                                }
                            }
                        }
                    }

                    InstrumentType.RESISTOR -> {
                        if (component.resistanceHidden) {
                            Text(
                                "未知定值电阻：阻值待测，不能查看或更改规格。",
                                color = LabColors.InkMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                            )
                        } else {
                            Text("定值电阻阻值", color = LabColors.InkMuted, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (value in listOf(5f, 10f, 15f, 20f)) {
                                    OptionChip("${value.toInt()} Ω", component.resistance == value) {
                                        editor.setResistance(component.id, value)
                                    }
                                }
                            }
                        }
                    }

                    InstrumentType.RHEOSTAT -> {
                        Text("最大阻值", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (value in listOf(10f, 20f, 30f, 50f)) {
                                OptionChip("${value.toInt()} Ω", component.rheostatMax == value) {
                                    editor.setRheostatMax(component.id, value)
                                }
                            }
                        }
                        Text(
                            "也可以直接拖动仪器上的滑片改变接入阻值。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                        )
                    }

                    InstrumentType.RESISTANCE_BOX -> {
                        ResistanceBoxEditor(editor, component)
                    }

                    InstrumentType.RESISTANCE_WIRE -> {
                        ResistanceWireEditor(editor, component)
                    }

                    InstrumentType.BAR_MAGNET, InstrumentType.HORSESHOE_MAGNET -> {
                        Text("磁极方向", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptionChip("N 左 S 右", !component.polarityFlipped) {
                                editor.setPolarityFlipped(component.id, false)
                            }
                            OptionChip("S 左 N 右", component.polarityFlipped) {
                                editor.setPolarityFlipped(component.id, true)
                            }
                        }
                        Text(
                            "翻转磁极会改变磁体之间的吸引/排斥关系。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("旋转角度", color = LabColors.InkMuted, fontSize = 10.sp)
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${component.rotation.toInt()}°",
                                color = LabColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Slider(
                            value = component.rotation,
                            onValueChange = { editor.updateRotationLive(component.id, it) },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = LabColors.Primary,
                                activeTrackColor = LabColors.Primary,
                                inactiveTrackColor = LabColors.SurfaceHigh,
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (angle in listOf(0f, 45f, 90f, 180f)) {
                                OptionChip("${angle.toInt()}°", kotlin.math.abs(component.rotation - angle) < 0.5f) {
                                    editor.setRotation(component.id, angle)
                                }
                            }
                        }
                        Text(
                            "也可以拖动仪器上方的绿色旋转手柄。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                    }

                    InstrumentType.AMMETER, InstrumentType.VOLTMETER -> {
                        Text("接线柱方向", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptionChip("默认：− 左", !component.meterFlipped) {
                                editor.setMeterFlipped(component.id, false)
                            }
                            OptionChip("翻转：− 右", component.meterFlipped) {
                                editor.setMeterFlipped(component.id, true)
                            }
                        }
                        Text(
                            if (component.type == InstrumentType.AMMETER) {
                                "电流表有三个接线柱：− / 0.6 / 3。接哪个正接线柱就使用哪个测量范围。"
                            } else {
                                "电压表有三个接线柱：− / 3 / 15。接哪个正接线柱就使用哪个测量范围。"
                            } + " 正常应让电流从正接线柱流入、负接线柱流出；接反时指针会向负刻度偏转，越过负刻度线才会损坏。",
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }

                    InstrumentType.SWITCH -> {
                        Text("开关有两个接线柱，单击即可闭合或断开。", color = LabColors.InkMuted, fontSize = 10.sp)
                    }

                    InstrumentType.MOTOR, InstrumentType.BUZZER, InstrumentType.LED,
                    InstrumentType.COMPASS -> {
                        Text("该仪器没有可调规格。", color = LabColors.InkMuted, fontSize = 10.sp)
                    }

                    InstrumentType.SOLENOID, InstrumentType.ELECTROMAGNET -> {
                        CoilWindingEditor(editor, component)
                    }

                    InstrumentType.PAPERCLIPS -> {
                        Text(
                            "回形针没有接线柱。把它放在磁体或通电线圈附近，会被吸引并靠近磁极；移开磁体后恢复原位。",
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { editor.closeSpec() }) {
                Text("完成", color = LabColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    editor.deleteSelection()
                    editor.closeSpec()
                },
            ) {
                Text("删除仪器", color = LabColors.Red, fontSize = 11.sp)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}

@Composable
private fun SourcePolarityRow(editor: LabEditorState, component: PlacedInstrument) {
    Text("正负极", color = LabColors.InkMuted, fontSize = 10.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OptionChip("正极在右", !component.polarityFlipped) {
            editor.setPolarityFlipped(component.id, false)
        }
        OptionChip("正极在左", component.polarityFlipped) {
            editor.setPolarityFlipped(component.id, true)
        }
    }
}

@Composable
private fun InstrumentDetailSummary(
    component: PlacedInstrument,
    reading: ComponentReading,
    showReadings: Boolean,
) {
    val detail = when {
        component.damaged -> "已损坏，请先排除故障，再点击仪器上方“修复”"
        component.type.isPowerSource -> {
            val polarity = if (component.polarityFlipped) "正极在左" else "正极在右"
            if (showReadings) {
                "$polarity · 端电压 ${formatNumber(reading.voltage)} V · 输出电流 ${formatCurrent(reading.current)}"
            } else {
                "$polarity · 电源电压 ${formatNumber(component.voltage)} V"
            }
        }
        component.type.isMeter -> {
            if (showReadings) {
                if (component.type == InstrumentType.AMMETER) {
                    "示数 ${formatAmmeter(reading.meterSignedValue)} · 测量范围 ${reading.meterRangeLabel.ifBlank { "0.6A" }}"
                } else {
                    "示数 ${formatVoltmeter(reading.meterSignedValue)} · 测量范围 ${reading.meterRangeLabel.ifBlank { "3V" }}"
                }
            } else {
                "点击仪器可放大表盘，直接读取指针刻度"
            }
        }
        component.type == InstrumentType.SWITCH -> if (component.closed) "开关闭合" else "开关断开"
        component.type == InstrumentType.LAMP || component.type == InstrumentType.TEMP_LAMP -> {
            val kind = if (component.type == InstrumentType.TEMP_LAMP) "热敏小灯泡" else "额定"
            if (showReadings) {
                "$kind ${component.lampSpec.label} · 实际功率 ${formatNumber(reading.power)} W"
            } else {
                "$kind ${component.lampSpec.label}"
            }
        }
        component.type == InstrumentType.RHEOSTAT ->
            "接入 ${formatNumber(component.rheostatResistance)} Ω / 最大 ${formatNumber(component.rheostatMax)} Ω"
        component.type == InstrumentType.RESISTOR ->
            if (component.resistanceHidden) "未知定值电阻（阻值待测）"
            else "定值电阻 ${formatNumber(component.resistance)} Ω"
        component.type == InstrumentType.RESISTANCE_BOX -> "电阻箱 ${component.resistanceBoxValue} Ω"
        component.type == InstrumentType.RESISTANCE_WIRE ->
            "${component.wireSample.name} · ${component.wireSample.material.label} · " +
                "R=${formatNumber(component.wireResistance)} Ω"
        component.type.isMagnet ->
            "${if (component.polarityFlipped) "S 左 N 右" else "N 左 S 右"} · 旋转 ${component.rotation.toInt()}°"
        component.type == InstrumentType.MOTOR ->
            if (showReadings) {
                (if (reading.energized) "电动机转动中" else "电动机未转动") +
                    " · 电流 ${formatCurrent(reading.current)}"
            } else {
                "电动机 · 额定 4.5 V"
            }
        component.type == InstrumentType.BUZZER ->
            if (showReadings) {
                (if (reading.energized) "蜂鸣器发声" else "蜂鸣器静音") +
                    " · 电流 ${formatCurrent(reading.current)}"
            } else {
                "蜂鸣器 · 额定 4.5 V"
            }
        component.type == InstrumentType.LED ->
            if (showReadings) {
                (when {
                    reading.ledReversed -> "LED 反接不亮"
                    reading.energized -> "LED 发光"
                    else -> "LED 未发光"
                }) + " · 电流 ${formatCurrent(reading.current)}"
            } else {
                "发光二极管"
            }
        component.type == InstrumentType.COMPASS -> "小磁针指示磁场方向"
        else -> "长按可查看或调整仪器规格"
    }
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (component.damaged) {
            LabColors.Red.copy(alpha = 0.12f)
        } else {
            LabColors.SurfaceHigh.copy(alpha = 0.72f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (component.damaged) LabColors.Red.copy(alpha = 0.38f) else LabColors.OutlineSoft,
        ),
    ) {
        Column(
            Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                "当前状态",
                color = if (component.damaged) LabColors.Red else LabColors.Primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                detail,
                color = if (component.damaged) Color(0xFFFFA4AA) else LabColors.InkMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun CoilWindingEditor(editor: LabEditorState, component: PlacedInstrument) {
    Text(
        if (component.type == InstrumentType.SOLENOID) "螺线管绕线方式" else "电磁铁绕线方式",
        color = LabColors.InkMuted,
        fontSize = 10.sp,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OptionChip("正向绕线", !component.windingReversed) {
            editor.setWindingReversed(component.id, false)
        }
        OptionChip("反向绕线", component.windingReversed) {
            editor.setWindingReversed(component.id, true)
        }
    }
    Text(
        "改变绕线方向会改变 N/S 极和磁感线方向；改变电流方向同样会改变磁场方向。",
        color = LabColors.InkMuted,
        fontSize = 9.5.sp,
        lineHeight = 14.sp,
    )
}

@Composable
private fun ResistanceWireEditor(editor: LabEditorState, component: PlacedInstrument) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("显示电阻值", color = LabColors.Ink, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "默认关闭，避免直接给出电阻大小；打开后实验板和卡片显示计算值",
                color = LabColors.InkFaint,
                fontSize = 8.8.sp,
            )
        }
        Switch(
            checked = component.showWireResistance,
            onCheckedChange = { editor.setWireResistanceVisible(component.id, it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF052E24),
                checkedTrackColor = LabColors.Green,
                uncheckedThumbColor = LabColors.InkMuted,
                uncheckedTrackColor = LabColors.SurfaceHigh,
            ),
        )
    }
    Text(
        "四种电阻丝都装在实验板上，选择其中一种接入电路：",
        color = LabColors.InkMuted,
        fontSize = 10.sp,
    )
    for (row in 0..1) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (sample in ResistanceWireSamples.drop(row * 2).take(2)) {
                val selected = component.wireSampleIndex == sample.index
                Surface(
                    onClick = { editor.setWireSample(component.id, sample.index) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = if (selected) {
                        LabColors.Primary.copy(alpha = 0.14f)
                    } else {
                        LabColors.SurfaceHigh.copy(alpha = 0.55f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) LabColors.Primary.copy(alpha = 0.5f) else LabColors.OutlineSoft,
                    ),
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Text(
                            "${sample.index + 1}  ${sample.name}",
                            color = LabColors.Ink,
                            fontSize = 9.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            lineHeight = 12.sp,
                        )
                        Text(
                            "${sample.material.label} · ${formatNumber(sample.length)} m · " +
                                "${if (sample.area <= 0.25f) "细" else "粗"} " +
                                "${formatNumber(sample.area)} mm²",
                            color = LabColors.InkFaint,
                            fontSize = 8.2.sp,
                            lineHeight = 10.5.sp,
                        )
                        if (component.showWireResistance) {
                            Text(
                                "R = ${formatNumber(sample.resistance)} Ω",
                                color = if (selected) LabColors.Primary else LabColors.InkMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
    Text(
        if (component.showWireResistance) {
            "R = ρ·L / S；可比较材料、长度和横截面积对电阻的影响。"
        } else {
            "R = ρ·L / S；电阻值默认隐藏，可打开上方开关显示。"
        },
        color = LabColors.InkMuted,
        fontSize = 9.5.sp,
        lineHeight = 14.sp,
    )
}

@Composable
private fun ResistanceBoxEditor(editor: LabEditorState, component: PlacedInstrument) {
    val value = component.resistanceBoxValue.coerceIn(0, 9999)
    val digits = listOf(
        (value / 1000) % 10,
        (value / 100) % 10,
        (value / 10) % 10,
        value % 10,
    )
    val places = listOf(1000, 100, 10, 1)
    Text("旋钮阻值 0～9999 Ω", color = LabColors.InkMuted, fontSize = 10.sp)
    for (i in 0 until 4) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("×${places[i]}", color = LabColors.InkMuted, fontSize = 10.sp, modifier = Modifier.width(46.dp))
            DigitButton("−") {
                editor.setResistanceBoxValue(component.id, value - places[i])
            }
            Text(
                digits[i].toString(),
                color = LabColors.Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            DigitButton("+") {
                editor.setResistanceBoxValue(component.id, value + places[i])
            }
            Spacer(Modifier.weight(1f))
            if (i == 3) {
                Text(
                    "$value Ω",
                    color = LabColors.Ink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DigitButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        color = LabColors.SurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
            color = LabColors.Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OptionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) LabColors.Primary.copy(alpha = 0.16f) else LabColors.SurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) LabColors.Primary.copy(alpha = 0.5f) else LabColors.OutlineSoft,
        ),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = if (selected) LabColors.Primary else LabColors.InkMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
