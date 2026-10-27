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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.MeterResistanceMode
import com.physlab.electric.model.MultimeterMode
import com.physlab.electric.model.multimeterRanges
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.ResistanceWireSamples
import com.physlab.electric.model.isMagnet
import com.physlab.electric.model.isMeter
import com.physlab.electric.model.isRotatable
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
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (component.type) {
                    InstrumentType.BATTERY -> {
                        if (component.specLocked) {
                            Text("本关电池组规格已锁定，不能修改节数或内阻。", color = LabColors.Amber, fontSize = 10.sp)
                        }
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
                        InternalResistanceEditor(editor, component)
                    }

                    InstrumentType.POWER_SUPPLY -> {
                        if (component.specLocked) {
                            Text("本关电源规格已锁定，不能修改电压、内阻或交直流模式。", color = LabColors.Amber, fontSize = 10.sp)
                        }
                        SourcePolarityRow(editor, component)
                        Text(
                            if (component.supplyAc) "输出方式：交流 AC 50Hz" else "输出方式：直流 DC",
                            color = LabColors.Ink,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                            "范围 0～36 V；交流模式按有效值显示，纯电阻电路与直流等效。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptionChip("直流 DC", !component.supplyAc) {
                                editor.setSupplyAc(component.id, false)
                            }
                            OptionChip("交流 AC 50Hz", component.supplyAc) {
                                editor.setSupplyAc(component.id, true)
                            }
                        }
                        InternalResistanceEditor(editor, component)
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
                        InternalResistanceEditor(editor, component)
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
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                for (rowValues in listOf(
                                    listOf(5f, 10f, 15f, 20f),
                                    listOf(50f, 100f, 10_000f),
                                )) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        for (value in rowValues) {
                                            OptionChip(
                                                if (value >= 1000f) "${(value / 1000f).toInt()} kΩ" else "${value.toInt()} Ω",
                                                component.resistance == value,
                                            ) {
                                                editor.setResistance(component.id, value)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    InstrumentType.RHEOSTAT -> {
                        Text("最大阻值", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (value in listOf(10f, 20f, 30f, 50f, 100f)) {
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

                    InstrumentType.CAPACITOR -> {
                        Text("电容量", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (value in listOf(100f, 470f, 1000f)) {
                                OptionChip("${value.toInt()} µF", component.capacitanceUf == value) {
                                    editor.setCapacitance(component.id, value)
                                }
                            }
                        }
                        Text(
                            "额定电压 16 V；充电后电容两端会保持电压。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                        Text("正负极标记", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptionChip("默认：+ 左", !component.polarityFlipped) {
                                editor.setPolarityFlipped(component.id, false)
                            }
                            OptionChip("翻转：+ 右", component.polarityFlipped) {
                                editor.setPolarityFlipped(component.id, true)
                            }
                        }
                        Text(
                            "只改变电容正负极标记与接线柱颜色，不改变电容量。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                    }

                    InstrumentType.INDUCTOR -> {
                        Text("电感量", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (value in listOf(10f, 50f, 100f)) {
                                OptionChip("${value.toInt()} mH", component.inductanceMh == value) {
                                    editor.setInductance(component.id, value)
                                }
                            }
                        }
                        Text(
                            "直流电阻：10 mH 1 Ω，50 mH 2 Ω，100 mH 4 Ω。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                        )
                    }

                    InstrumentType.SPDT_SWITCH -> {
                        Text("单刀双掷开关", color = LabColors.InkMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OptionChip("1 接 2", component.switchPosition == 0) {
                                editor.setSwitchPosition(component.id, 0)
                            }
                            OptionChip("1 接 3", component.switchPosition == 1) {
                                editor.setSwitchPosition(component.id, 1)
                            }
                        }
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
                    }

                    InstrumentType.AMMETER, InstrumentType.VOLTMETER, InstrumentType.MILLIAMMETER -> {
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
                            when (component.type) {
                                InstrumentType.AMMETER ->
                                    "电流表有三个接线柱：− / 0.6 / 3。接哪个正接线柱就使用哪个测量范围。"
                                InstrumentType.MILLIAMMETER ->
                                    "毫安表有三个接线柱：− / 50 mA / 500 mA。接哪个正接线柱就使用哪个测量范围。"
                                else ->
                                    "电压表有三个接线柱：− / 3 / 15。接哪个正接线柱就使用哪个测量范围。"
                            } + " 正常应让电流从正接线柱流入、负接线柱流出；接反时指针会向负刻度偏转，越过负刻度线才会损坏。",
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        MeterResistanceEditor(editor, component)
                    }

                    InstrumentType.MULTIMETER -> {
                        MultimeterEditor(editor, component, reading)
                    }

                    InstrumentType.GALVANOMETER -> {
                        Text("灵敏电流计", color = LabColors.InkMuted, fontSize = 10.sp)
                        Text(
                            "量程 −300 μA ～ 0 ～ +300 μA，中央零位；每小格 20 μA，内阻固定 50 Ω。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                        )
                    }

                    InstrumentType.MILLIAMMETER -> {
                        Text("毫安表", color = LabColors.InkMuted, fontSize = 10.sp)
                        Text(
                            "量程 0～50 mA / 0～500 mA；分度值 2 mA / 20 mA，内阻固定 5 Ω。",
                            color = LabColors.InkFaint,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
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
                if (component.type.isRotatable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (angle in listOf(0f, 45f, 90f, 180f)) {
                            OptionChip(
                                "${angle.toInt()}°",
                                kotlin.math.abs(component.rotation - angle) < 0.5f,
                            ) {
                                editor.setRotation(component.id, angle)
                            }
                        }
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
                if (component.type == InstrumentType.MULTIMETER) {
                    reading.multimeterDisplay.ifBlank { reading.multimeterWarning.ifBlank { "-" } }
                } else if (component.type == InstrumentType.AMMETER) {
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

@Composable
private fun InternalResistanceEditor(editor: LabEditorState, component: PlacedInstrument) {
    val hideForEmfChallenge = editor.challengeActive &&
        ChallengeTask.entries.getOrNull(editor.challengeIndex) == ChallengeTask.EMF_INTERNAL_RESISTANCE &&
        component.type.isPowerSource
    if (hideForEmfChallenge) {
        Text(
            "本关电源内阻不可见，请通过实验测量。",
            color = LabColors.InkMuted,
            fontSize = 10.sp,
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("电源内阻", color = LabColors.InkMuted, fontSize = 10.sp)
            Text(
                if (component.internalResistanceEnabled) {
                    "手动设置 ${formatNumber(component.internalResistanceOhm)} Ω"
                } else {
                    "关闭：使用该电源的默认内阻"
                },
                color = LabColors.InkFaint,
                fontSize = 9.sp,
            )
        }
        Switch(
            checked = component.internalResistanceEnabled,
            onCheckedChange = { editor.setInternalResistanceEnabled(component.id, it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF052E24),
                checkedTrackColor = LabColors.Green,
                uncheckedThumbColor = LabColors.InkMuted,
                uncheckedTrackColor = LabColors.SurfaceHigh,
            ),
        )
    }
    if (component.internalResistanceEnabled) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (value in listOf(0.1f, 0.2f, 0.5f, 1f)) {
                OptionChip("${formatNumber(value)} Ω", component.internalResistanceOhm == value) {
                    editor.setInternalResistance(component.id, value)
                }
            }
        }
        var custom by remember(component.id) {
            mutableStateOf(formatNumber(component.internalResistanceOhm))
        }
        OutlinedTextField(
            value = custom,
            onValueChange = { text ->
                custom = text
                text.replace(',', '.').toFloatOrNull()?.let { value ->
                    if (value in 0.1f..1f) editor.setInternalResistance(component.id, value)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("自定义 0.1–1.0 Ω", fontSize = 9.sp) },
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

@Composable
private fun MultimeterEditor(
    editor: LabEditorState,
    component: PlacedInstrument,
    reading: ComponentReading,
) {
    Text("测量模式", color = LabColors.InkMuted, fontSize = 10.sp)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("DCV", component.multimeterMode == MultimeterMode.DC_VOLTAGE) {
                editor.setMultimeterMode(component.id, MultimeterMode.DC_VOLTAGE)
            }
            OptionChip("ACV", component.multimeterMode == MultimeterMode.AC_VOLTAGE) {
                editor.setMultimeterMode(component.id, MultimeterMode.AC_VOLTAGE)
            }
            OptionChip("DCA", component.multimeterMode == MultimeterMode.DC_CURRENT) {
                editor.setMultimeterMode(component.id, MultimeterMode.DC_CURRENT)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionChip("ACA", component.multimeterMode == MultimeterMode.AC_CURRENT) {
                editor.setMultimeterMode(component.id, MultimeterMode.AC_CURRENT)
            }
            OptionChip("Ω", component.multimeterMode == MultimeterMode.RESISTANCE) {
                editor.setMultimeterMode(component.id, MultimeterMode.RESISTANCE)
            }
            OptionChip("二极管", component.multimeterMode == MultimeterMode.DIODE) {
                editor.setMultimeterMode(component.id, MultimeterMode.DIODE)
            }
        }
    }

    Text("量程", color = LabColors.InkMuted, fontSize = 10.sp)
    val ranges = multimeterRanges(component.multimeterMode)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ranges.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEachIndexed { localIndex, range ->
                    val index = ranges.indexOf(range)
                    OptionChip(range.label, component.multimeterRangeIndex == index) {
                        editor.setMultimeterRange(component.id, index)
                    }
                }
            }
        }
    }

    if (component.multimeterMode == MultimeterMode.RESISTANCE) {
        Text(
            "调零：${String.format(java.util.Locale.US, "%.2f", component.multimeterZeroOffset)} Ω",
            color = LabColors.InkMuted,
            fontSize = 10.sp,
        )
        Slider(
            value = component.multimeterZeroOffset.coerceIn(-5f, 5f),
            onValueChange = { editor.setMultimeterZeroOffset(component.id, it) },
            valueRange = -5f..5f,
            colors = SliderDefaults.colors(
                thumbColor = LabColors.Primary,
                activeTrackColor = LabColors.Primary,
                inactiveTrackColor = LabColors.SurfaceHigh,
            ),
        )
    }

    Text(
        "读数：${reading.multimeterDisplay.ifBlank { reading.multimeterWarning.ifBlank { "—" } }}",
        color = if (reading.multimeterWarning.isNotBlank()) LabColors.Amber else LabColors.Ink,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
    )
    if (component.multimeterMode == MultimeterMode.RESISTANCE ||
        component.multimeterMode == MultimeterMode.DIODE
    ) {
        Text(
            "电阻档和二极管档需要断电测量；检测到电路带电时会显示“请断电测量”。",
            color = LabColors.InkFaint,
            fontSize = 9.sp,
            lineHeight = 13.sp,
        )
    }
    Text(
        "接线：COM 为公共端；VΩ 用于电压/电阻/二极管，A 用于电流。",
        color = LabColors.InkFaint,
        fontSize = 9.sp,
        lineHeight = 13.sp,
    )
}

@Composable
private fun MeterResistanceEditor(editor: LabEditorState, component: PlacedInstrument) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("仪表内阻", color = LabColors.InkMuted, fontSize = 10.sp)
            Text(
                if (component.meterResistanceEnabled) {
                    "按实际仪表内阻参与电路计算"
                } else {
                    "关闭：使用理想化近似内阻"
                },
                color = LabColors.InkFaint,
                fontSize = 9.sp,
            )
        }
        Switch(
            checked = component.meterResistanceEnabled,
            onCheckedChange = { editor.setMeterResistanceEnabled(component.id, it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF052E24),
                checkedTrackColor = LabColors.Green,
                uncheckedThumbColor = LabColors.InkMuted,
                uncheckedTrackColor = LabColors.SurfaceHigh,
            ),
        )
    }
    if (component.meterResistanceEnabled) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            OptionChip(
                "指针表",
                component.meterResistanceMode == MeterResistanceMode.ANALOG,
            ) { editor.setMeterResistanceMode(component.id, MeterResistanceMode.ANALOG) }
            OptionChip(
                "数字表",
                component.meterResistanceMode == MeterResistanceMode.DIGITAL,
            ) { editor.setMeterResistanceMode(component.id, MeterResistanceMode.DIGITAL) }
            OptionChip(
                "高精度",
                component.meterResistanceMode == MeterResistanceMode.HIGH_PRECISION,
            ) { editor.setMeterResistanceMode(component.id, MeterResistanceMode.HIGH_PRECISION) }
        }
        Text(
            if (component.type == InstrumentType.AMMETER) {
                "指针表：0.6A 档 0.1Ω / 3A 档 0.05Ω；数字表 0.01Ω；高精度 0.001Ω。"
            } else {
                "指针表：3V 档 3kΩ / 15V 档 15kΩ；数字表 1MΩ；高精度 10MΩ。"
            },
            color = LabColors.InkFaint,
            fontSize = 9.sp,
            lineHeight = 13.sp,
        )
    }
}
