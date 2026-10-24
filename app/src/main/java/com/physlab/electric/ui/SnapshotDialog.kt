package com.physlab.electric.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SnapshotDialog(
    snapshots: List<SavedSnapshot>,
    onSave: (String) -> Unit,
    onLoad: (SavedSnapshot) -> Unit,
    onDelete: (SavedSnapshot) -> Unit,
    onClose: () -> Unit,
) {
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
    val defaultName = "实验 " + formatter.format(Date())
    var naming by remember { mutableStateOf(false) }
    var snapshotName by remember { mutableStateOf(defaultName) }
    if (naming) {
        AlertDialog(
            onDismissRequest = { naming = false },
            title = { Text("命名实验快照", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = snapshotName,
                    onValueChange = { snapshotName = it.take(30) },
                    singleLine = true,
                    label = { Text("快照名称") },
                    placeholder = { Text(defaultName) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSave(snapshotName.trim().ifBlank { defaultName })
                    naming = false
                }) {
                    Text("保存", color = LabColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { naming = false }) {
                    Text("取消", color = LabColors.InkMuted)
                }
            },
            containerColor = LabColors.SurfaceRaised,
            titleContentColor = LabColors.Ink,
            textContentColor = LabColors.InkMuted,
        )
        return
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Column {
                Text("实验快照", fontWeight = FontWeight.Bold)
                Text(
                    "保存当前仪器、导线、实验台位置和记录，下次可继续",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 240.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Surface(
                    onClick = {
                        snapshotName = defaultName
                        naming = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = LabColors.Primary.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        LabColors.Primary.copy(alpha = 0.4f),
                    ),
                ) {
                    Text(
                        "保存当前实验",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        color = LabColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                if (snapshots.isEmpty()) {
                    Text(
                        "还没有保存的实验快照。搭建好电路后点击上方按钮保存。",
                        color = LabColors.InkFaint,
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                } else {
                    snapshots.forEach { snapshot ->
                        Surface(
                            onClick = { onLoad(snapshot) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(9.dp),
                            color = LabColors.SurfaceHigh.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        snapshot.name,
                                        color = LabColors.Ink,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "${formatter.format(Date(snapshot.createdAt))} · " +
                                            "${snapshot.componentCount} 件仪器 · ${snapshot.wireCount} 根导线",
                                        color = LabColors.InkFaint,
                                        fontSize = 8.8.sp,
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier
                                        .size(26.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Surface(
                                        onClick = { onDelete(snapshot) },
                                        shape = RoundedCornerShape(7.dp),
                                        color = LabColors.Red.copy(alpha = 0.1f),
                                    ) {
                                        LabIcon(
                                            LabIcon.TRASH,
                                            LabColors.Red,
                                            Modifier
                                                .padding(5.dp)
                                                .size(13.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("关闭", color = LabColors.InkMuted)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}
