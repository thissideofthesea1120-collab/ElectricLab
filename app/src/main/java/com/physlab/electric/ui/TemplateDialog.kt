package com.physlab.electric.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExperimentTemplateDialog(editor: LabEditorState) {
    val context = LocalContext.current
    var userTemplates by remember { mutableStateOf(UserTemplateStore.load(context)) }
    AlertDialog(
        onDismissRequest = { editor.updateTemplateDialog(false) },
        title = {
            Column {
                Text("选择实验", fontWeight = FontWeight.Bold)
                Text(
                    "内置模板按教材布局；我的模板保留你的接线和仪器位置",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 310.dp)
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
                            ExperimentTemplate.MULTIMETER -> Color(0xFF8FD7FF)
                            ExperimentTemplate.RC_CHARGE -> Color(0xFF7ED6C4)
                        },
                        onClick = {
                            editor.loadTemplate(template)
                            editor.updateTemplateDialog(false)
                        },
                    )
                }

                Text(
                    "我的模板",
                    color = LabColors.InkMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (userTemplates.isEmpty()) {
                    Text(
                        "还没有自定义模板。连接好电路后点下方“保存当前为模板”。",
                        color = LabColors.InkFaint,
                        fontSize = 9.sp,
                    )
                }
                for (template in userTemplates) {
                    TemplateRow(
                        title = template.name,
                        subtitle = "${template.grade} · ${template.category} · ${template.payload.length} 字节",
                        accent = LabColors.Primary,
                        onClick = {
                            if (editor.loadSnapshot(template.payload)) {
                                editor.updateTemplateDialog(false)
                            } else {
                                editor.notify("模板读取失败")
                            }
                        },
                        onDelete = {
                            UserTemplateStore.remove(context, template.id)
                            userTemplates = UserTemplateStore.load(context)
                        },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val name = "我的模板 ${userTemplates.size + 1}"
                            UserTemplateStore.add(
                                context = context,
                                name = name,
                                grade = "自定义",
                                category = "电路",
                                payload = editor.exportSnapshot(),
                            )
                            userTemplates = UserTemplateStore.load(context)
                            editor.notify("已保存为模板：$name")
                        },
                    ) {
                        Text("保存当前为模板", color = LabColors.Primary, fontSize = 10.sp)
                    }
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("ElectricLab 模板", editor.exportSnapshot()),
                            )
                            editor.notify("模板 JSON 已复制")
                        },
                    ) {
                        Text("复制模板", color = LabColors.InkMuted, fontSize = 10.sp)
                    }
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                            if (text.isNullOrBlank() || !editor.loadSnapshot(text)) {
                                editor.notify("剪贴板中没有有效模板")
                                return@TextButton
                            }
                            UserTemplateStore.add(
                                context = context,
                                name = "导入模板 ${userTemplates.size + 1}",
                                grade = "导入",
                                category = "电路",
                                payload = text,
                            )
                            userTemplates = UserTemplateStore.load(context)
                            editor.notify("模板已导入并载入")
                        },
                    ) {
                        Text("从剪贴板导入", color = LabColors.InkMuted, fontSize = 10.sp)
                    }
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
    onDelete: (() -> Unit)? = null,
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
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("删除", color = LabColors.Red, fontSize = 9.sp)
                }
            }
        }
    }
}
