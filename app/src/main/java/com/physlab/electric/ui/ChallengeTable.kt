package com.physlab.electric.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ChallengeDataTable(
    task: ChallengeTask,
    table: ChallengeTableSpec,
    editor: LabEditorState,
    active: Boolean,
) {
    Column(Modifier.fillMaxWidth()) {
        LaunchedEffect(editor.challengeDataActiveKey, editor.challengeDataEditVersion) {
            if (editor.challengeDataActiveKey != null) {
                delay(2200)
                editor.finishChallengeDataEntry()
            }
        }
        Text(
            if (active) "实验数据记录表（自己填写）" else "实验数据记录表（开始闯关后可填写）",
            color = LabColors.Primary,
            fontSize = 9.2.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (active && table.columns.indices.any { it !in table.calculatedColumns } &&
            editor.challengeDataTokens <= 0
        ) {
            Text(
                "请先点击电流表或电压表，完成表盘读数验证后再填写一处数据。",
                color = LabColors.Amber,
                fontSize = 8.2.sp,
                lineHeight = 11.sp,
            )
        }
        if (task == ChallengeTask.VOLT_AMMETER_LAMP || task == ChallengeTask.LAMP_POWER) {
            val rated = editor.components.firstOrNull { it.type == com.physlab.electric.model.InstrumentType.TEMP_LAMP }
                ?.lampSpec?.voltage
            if (rated != null) {
                Text(
                    "小灯泡额定电压：${String.format(java.util.Locale.US, "%.1f", rated)} V",
                    color = LabColors.Amber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (table.rowLabels.size > 1) {
                Text(
                    table.rowHeader.orEmpty(),
                    modifier = Modifier.width(58.dp),
                    color = LabColors.InkMuted,
                    fontSize = 8.2.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            for (column in table.columns) {
                Text(
                    column,
                    modifier = Modifier.weight(1f),
                    color = LabColors.InkMuted,
                    fontSize = 8.2.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        table.rowLabels.forEachIndexed { rowIndex, rowLabel ->
            Row(
                Modifier.fillMaxWidth().padding(top = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (table.rowLabels.size > 1) {
                    Text(
                        rowLabel,
                        modifier = Modifier.width(58.dp),
                        color = LabColors.InkFaint,
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                    )
                }
                for (column in table.columns.indices) {
                    val key = challengeCellKey(task, rowIndex, column)
                    val measured = active && column !in table.calculatedColumns
                    val enabled = if (measured) editor.canEditChallengeCell(key) else active
                    if (table.choiceColumn == column && table.choiceOptions.isNotEmpty()) {
                        var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
                        val current = editor.challengeInputs[key].orEmpty()
                        androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
                            Surface(
                                onClick = { if (active) expanded = true },
                                shape = RoundedCornerShape(6.dp),
                                color = LabColors.SurfaceHigh.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, LabColors.OutlineSoft),
                            ) {
                                Text(
                                    current.ifBlank { "请选择" },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 5.dp),
                                    color = if (current.isBlank()) LabColors.InkFaint else LabColors.Ink,
                                    fontSize = 8.2.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                containerColor = LabColors.SurfaceRaised,
                            ) {
                                for (option in table.choiceOptions) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                color = if (current == option) LabColors.Primary else LabColors.Ink,
                                                fontSize = 9.sp,
                                            )
                                        },
                                        onClick = {
                                            if (active) editor.updateChallengeInput(key, option)
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = editor.challengeInputs[key].orEmpty(),
                            onValueChange = { value ->
                                if (!measured || editor.canEditChallengeCell(key)) {
                                    if (measured) editor.noteChallengeCellEdited(key)
                                    editor.updateChallengeInput(key, value)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = enabled,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = LabColors.Ink,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                            ),
                            placeholder = {
                                Text("0.00", color = LabColors.InkFaint, fontSize = 10.sp)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        )
                    }
                }
            }
        }
        Text(
            table.hint + if (table.unit.isNotEmpty()) "（单位：${table.unit}）" else "",
            color = LabColors.InkFaint,
            fontSize = 8.5.sp,
            modifier = Modifier.padding(top = 3.dp),
        )
        if (task == ChallengeTask.EMF_INTERNAL_RESISTANCE) {
            EmfCalculationPanel(editor, active)
        }
        val conclusion = table.conclusion
        if (conclusion != null) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = LabColors.Primary.copy(alpha = 0.07f),
                border = BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.20f)),
            ) {
                Column(Modifier.padding(7.dp)) {
                    Text(
                        conclusion.question,
                        color = LabColors.Ink,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val selected = editor.challengeConclusionChoices[task.name]
                    conclusion.options.forEachIndexed { index, option ->
                        Surface(
                            onClick = { if (active) editor.setChallengeConclusion(task, index) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected == index) {
                                LabColors.Primary.copy(alpha = 0.16f)
                            } else {
                                LabColors.Surface.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selected == index) LabColors.Primary.copy(alpha = 0.45f) else LabColors.OutlineSoft,
                            ),
                        ) {
                            Text(
                                "${('A'.code + index).toChar()}. $option",
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 4.dp),
                                color = if (selected == index) LabColors.Primary else LabColors.InkMuted,
                                fontSize = 8.6.sp,
                                lineHeight = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmfCalculationPanel(editor: LabEditorState, active: Boolean) {
    Spacer(Modifier.height(5.dp))
    Text(
        "计算结果：用第 1/4、2/5、3/6 组分别联立求 E、r，再取平均值",
        color = LabColors.Amber,
        fontSize = 8.3.sp,
        lineHeight = 11.sp,
    )
    val pairKeys = listOf(
        Triple("第 1、4 组", EMF_E1_KEY, EMF_R1_KEY),
        Triple("第 2、5 组", EMF_E2_KEY, EMF_R2_KEY),
        Triple("第 3、6 组", EMF_E3_KEY, EMF_R3_KEY),
        Triple("平均值", EMF_E_AVG_KEY, EMF_R_AVG_KEY),
    )
    for ((label, eKey, rKey) in pairKeys) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                modifier = Modifier.width(58.dp),
                color = LabColors.InkFaint,
                fontSize = 8.sp,
            )
            ResultField(editor, eKey, "E/V", active, Modifier.weight(1f))
            ResultField(editor, rKey, "r/Ω", active, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultField(editor: LabEditorState, key: String, label: String, active: Boolean, modifier: Modifier) {
    OutlinedTextField(
        value = editor.challengeInputs[key].orEmpty(),
        onValueChange = { if (active) editor.updateChallengeInput(key, it) },
        modifier = modifier,
        singleLine = true,
        enabled = active,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = LabColors.Ink,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
        ),
        placeholder = { Text(label, color = LabColors.InkFaint, fontSize = 8.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
