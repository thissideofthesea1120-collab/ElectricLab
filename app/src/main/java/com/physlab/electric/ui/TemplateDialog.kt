package com.physlab.electric.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExperimentTemplateDialog(editor: LabEditorState) {
    AlertDialog(
        onDismissRequest = { editor.updateTemplateDialog(false) },
        title = {
            Column {
                Text("选择实验", fontWeight = FontWeight.Bold)
                Text(
                    "默认实验台为空，选择模板或自由搭建（可上下滑动）",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 248.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (template in ExperimentTemplate.entries) {
                    TemplateRow(
                        title = template.title,
                        subtitle = template.subtitle,
                        accent = when (template) {
                            ExperimentTemplate.SERIES -> LabColors.Primary
                            ExperimentTemplate.PARALLEL -> LabColors.Green
                            ExperimentTemplate.OHM_LAW -> LabColors.Amber
                            ExperimentTemplate.ELECTROMAGNET -> LabColors.Violet
                            ExperimentTemplate.MAGNETS -> Color(0xFFFF8E7A)
                        },
                        onClick = {
                            editor.loadTemplate(template)
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
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
