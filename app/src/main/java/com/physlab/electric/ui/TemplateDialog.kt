package com.physlab.electric.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun ExperimentTemplate.isJunior(): Boolean = when (this) {
    ExperimentTemplate.SERIES,
    ExperimentTemplate.PARALLEL,
    ExperimentTemplate.OHM_LAW,
    ExperimentTemplate.SIMPLE,
    ExperimentTemplate.SERIES_CURRENT,
    ExperimentTemplate.SERIES_VOLTAGE,
    ExperimentTemplate.PARALLEL_CURRENT,
    ExperimentTemplate.PARALLEL_VOLTAGE,
    ExperimentTemplate.BULB_POWER -> true
    else -> false
}

@Composable
fun ExperimentTemplateDialog(editor: LabEditorState) {
    val context = LocalContext.current
    var juniorTab by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = { editor.updateTemplateDialog(false) },
        title = {
            Column {
                Text("选择实验", fontWeight = FontWeight.Bold)
                Text(
                    "内置模板按教材布局；我的模板请从“我的模板”按钮进入",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 330.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((item, junior) in listOf("初中" to true, "高中" to false)) {
                        Surface(
                            onClick = { juniorTab = junior },
                            shape = RoundedCornerShape(8.dp),
                            color = if (juniorTab == junior) LabColors.Primary.copy(alpha = 0.18f) else LabColors.SurfaceHigh,
                        ) {
                            Text(
                                "${item}实验",
                                color = if (juniorTab == junior) LabColors.Primary else LabColors.InkMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
                for (template in ExperimentTemplate.entries.filter { it.isJunior() == juniorTab }) {
                    TemplateRow(
                        title = template.title,
                        subtitle = template.subtitle,
                        accent = when (template) {
                            ExperimentTemplate.SERIES -> LabColors.Primary
                            ExperimentTemplate.PARALLEL -> LabColors.Green
                            ExperimentTemplate.OHM_LAW -> LabColors.Amber
                            ExperimentTemplate.ELECTROMAGNET -> LabColors.Violet
                            ExperimentTemplate.MAGNETS -> Color(0xFFFF8E7A)
                            ExperimentTemplate.MULTIMETER -> Color(0xFF8FD7FF)
                            ExperimentTemplate.RC_CHARGE -> Color(0xFF7ED6C4)
                            else -> Color(0xFF8FD7FF)
                        },
                        onClick = {
                            val asset = when (template) {
                                ExperimentTemplate.SIMPLE -> "templates/simple_circuit.json"
                                ExperimentTemplate.SERIES -> "templates/series_circuit.json"
                                ExperimentTemplate.OHM_LAW -> "templates/ohm_law.json"
                                else -> null
                            }
                            val payload = asset?.let {
                                runCatching { context.assets.open(it).bufferedReader().readText() }.getOrNull()
                            }
                            editor.loadTemplate(template, payload)
                            editor.updateTemplateDialog(false)
                        },
                    )
                }
                TemplateRow(
                    title = "空白实验台",
                    subtitle = "清空所有仪器，从仪器库自由搭建",
                    accent = LabColors.InkMuted,
                    onClick = {
                        editor.clearAll()
                        editor.updateTemplateDialog(false)
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { editor.updateTemplateDialog(false) }) {
                Text("取消", color = LabColors.InkMuted)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}

@Composable
private fun TemplateRow(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = LabColors.SurfaceHigh.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(26.dp),
            ) {
                Column(
                    Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
                        drawCircle(accent, size.minDimension / 2f)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = LabColors.Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = LabColors.InkFaint, fontSize = 8.8.sp, lineHeight = 11.sp)
            }
        }
    }
}
