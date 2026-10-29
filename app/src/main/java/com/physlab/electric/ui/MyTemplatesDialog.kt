package com.physlab.electric.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyTemplatesDialog(editor: LabEditorState) {
    val context = LocalContext.current
    var templates by remember { mutableStateOf(UserTemplateStore.load(context)) }
    AlertDialog(
        onDismissRequest = { editor.closeMyTemplates() },
        title = { Text("我的模板", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.heightIn(max = 330.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (templates.isEmpty()) {
                    Text(
                        "还没有自定义模板。连接好电路后点“保存当前为模板”。",
                        color = LabColors.InkFaint,
                        fontSize = 10.sp,
                    )
                }
                for (template in templates) {
                    Surface(
                        onClick = {
                            if (editor.loadSnapshot(template.payload)) {
                                editor.closeMyTemplates()
                            } else {
                                editor.notify("模板读取失败")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.SurfaceHigh.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(template.name, color = LabColors.Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text("${template.grade} · ${template.category}", color = LabColors.InkFaint, fontSize = 9.sp)
                            }
                            TextButton(onClick = {
                                UserTemplateStore.remove(context, template.id)
                                templates = UserTemplateStore.load(context)
                            }) {
                                Text("删除", color = LabColors.Red, fontSize = 9.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val name = "我的模板 ${templates.size + 1}"
                        UserTemplateStore.add(context, name, "自定义", "电路", editor.exportSnapshot())
                        templates = UserTemplateStore.load(context)
                        editor.notify("已保存为模板：$name")
                    }) {
                        Text("保存当前为模板", color = LabColors.Primary, fontSize = 10.sp)
                    }
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ElectricLab 模板", editor.exportSnapshot()))
                        editor.notify("模板 JSON 已复制")
                    }) {
                        Text("复制模板", color = LabColors.InkMuted, fontSize = 10.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                        if (text.isNullOrBlank() || !editor.loadSnapshot(text)) {
                            editor.notify("剪贴板中没有有效模板")
                            return@TextButton
                        }
                        UserTemplateStore.add(context, "导入模板 ${templates.size + 1}", "导入", "电路", text)
                        templates = UserTemplateStore.load(context)
                        editor.notify("模板已导入并载入")
                    }) {
                        Text("从剪贴板导入", color = LabColors.InkMuted, fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { editor.closeMyTemplates() }) {
                Text("关闭", color = LabColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}
