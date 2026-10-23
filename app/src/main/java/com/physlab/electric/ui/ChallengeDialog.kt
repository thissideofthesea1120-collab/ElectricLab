package com.physlab.electric.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution

@Composable
fun ChallengeDialog(
    editor: LabEditorState,
    solution: CircuitSolution,
    onClose: () -> Unit,
    onStart: (ChallengeTask) -> Unit,
    onSubmit: (ChallengeTask) -> Unit,
    onNextTask: ((ChallengeTask) -> Unit)? = null,
) {
    val task = ChallengeTask.entries[editor.challengeIndex.coerceIn(0, ChallengeTask.entries.lastIndex)]
    val nextTask = ChallengeTask.entries.getOrNull(task.ordinal + 1)
    val nextUnlocked = nextTask != null && editor.isChallengeUnlocked(nextTask.ordinal)
    val active = editor.challengeActive && editor.challengeIndex == task.ordinal
    val result = if (active) editor.challengeResult else null
    val taskUnlocked = editor.isChallengeUnlocked(task.ordinal)

    BackHandler(onBack = onClose)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 940.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .heightIn(max = 390.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(22.dp),
            color = LabColors.Surface.copy(alpha = 0.99f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("任务闯关", color = LabColors.Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "自己连接电路；电流/电压规律和欧姆定律请先做实验，再把数据填入右侧表格并提交。",
                            color = LabColors.InkFaint,
                            fontSize = 9.2.sp,
                        )
                    }
                    Surface(
                        onClick = onClose,
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.SurfaceHigh,
                    ) {
                        Text(
                            "关闭",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            color = LabColors.InkMuted,
                            fontSize = 10.sp,
                        )
                    }
                }
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        itemsIndexed(ChallengeTask.entries) { index, item ->
                            val selected = index == editor.challengeIndex
                            val running = editor.challengeActive && index == editor.challengeIndex
                            val unlocked = editor.isChallengeUnlocked(index)
                            val completed = index in editor.challengeCompleted
                            Surface(
                                onClick = { if (unlocked) editor.selectChallenge(index) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) {
                                    LabColors.Primary.copy(alpha = 0.14f)
                                } else {
                                    LabColors.SurfaceHigh.copy(alpha = if (unlocked) 0.55f else 0.25f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) LabColors.Primary.copy(alpha = 0.45f) else LabColors.OutlineSoft,
                                ),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        if (completed) "✓" else "${index + 1}",
                                        color = if (completed) LabColors.Green
                                        else if (selected) LabColors.Primary else LabColors.InkFaint,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(17.dp),
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            item.title,
                                            color = if (unlocked) LabColors.Ink else LabColors.InkFaint,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                        )
                                        Row {
                                            Text(
                                                if (unlocked) item.level else "未解锁",
                                                color = LabColors.InkFaint,
                                                fontSize = 8.sp,
                                            )
                                            if (running) {
                                                Text(" 进行中", color = LabColors.Green, fontSize = 8.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(task.title, color = LabColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(7.dp))
                            Text(
                                task.level,
                                color = LabColors.Primary,
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .background(
                                        LabColors.Primary.copy(alpha = 0.12f),
                                        RoundedCornerShape(5.dp),
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                            Spacer(Modifier.weight(1f))
                            if (result != null) {
                                Text(
                                    "${result.score} 分",
                                    color = if (result.score >= 80) LabColors.Green else LabColors.Amber,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Text(task.description, color = LabColors.InkMuted, fontSize = 10.2.sp, lineHeight = 15.5.sp)
                            val table = task.table
                            if (table != null) {
                                Spacer(Modifier.height(8.dp))
                                ChallengeDataTable(task, table, editor, active)
                            }
                            if (result != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    when {
                                        result.score >= 100 -> "太棒了，满分通过！你的实验非常出色。"
                                        result.score >= 80 -> "做得很好，本关已通过；达到 100 分才能解锁下下关。"
                                        else -> "还有目标未完成，再认真试一次："
                                    },
                                    color = if (result.score >= 80) LabColors.Green else LabColors.Amber,
                                    fontSize = 9.5.sp,
                                )
                                Spacer(Modifier.height(3.dp))
                                for (check in result.checks) {
                                    Row(Modifier.padding(vertical = 1.5.dp), verticalAlignment = Alignment.Top) {
                                        Text(
                                            if (check.done) "✓" else "○",
                                            color = if (check.done) LabColors.Green else LabColors.InkFaint,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(17.dp),
                                        )
                                        Text(
                                            check.text,
                                            color = if (check.done) LabColors.Ink else LabColors.InkMuted,
                                            fontSize = 9.8.sp,
                                            lineHeight = 14.sp,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                                if (result.score in 80..99 && nextTask != null && nextUnlocked && onNextTask != null) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Surface(
                                            onClick = { onClose() },
                                            shape = RoundedCornerShape(8.dp),
                                            color = LabColors.SurfaceHigh,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                                        ) {
                                            Text(
                                                "继续本关",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = LabColors.InkMuted,
                                                fontSize = 9.sp,
                                            )
                                        }
                                        Surface(
                                            onClick = { onNextTask(nextTask) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = LabColors.Green.copy(alpha = 0.16f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Green.copy(alpha = 0.45f)),
                                        ) {
                                            Text(
                                                "进入下一关：${nextTask.title}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = LabColors.Green,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (active) {
                                        "实验台已准备好。连接电路、完成测量并填写表格后，点击右下角“提交验证”。"
                                    } else {
                                        "点击“开始闯关”后，实验台会清空，请自己从仪器库拖动仪器并连接电路。"
                                    },
                                    color = LabColors.InkFaint,
                                    fontSize = 9.5.sp,
                                    lineHeight = 14.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { if (taskUnlocked) onStart(task) },
                                enabled = taskUnlocked,
                                shape = RoundedCornerShape(9.dp),
                                color = if (taskUnlocked) LabColors.SurfaceHigh else LabColors.SurfaceHigh.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                            ) {
                                Text(
                                    if (active) "重新开始" else if (taskUnlocked) "开始闯关" else "未解锁",
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    color = LabColors.InkMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Surface(
                                onClick = {
                                    if (active) onSubmit(task)
                                },
                                shape = RoundedCornerShape(9.dp),
                                color = if (active) {
                                    LabColors.Primary.copy(alpha = 0.18f)
                                } else {
                                    LabColors.SurfaceHigh.copy(alpha = 0.4f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (active) LabColors.Primary.copy(alpha = 0.52f) else LabColors.OutlineSoft,
                                ),
                            ) {
                                Text(
                                    "提交验证",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (active) LabColors.Primary else LabColors.InkFaint,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeDataTableLegacy(
    task: ChallengeTask,
    table: ChallengeTableSpec,
    editor: LabEditorState,
    active: Boolean,
) {
    Text(
        if (active) "实验数据记录表（自己填写）" else "实验数据记录表（开始闯关后可填写）",
        color = LabColors.Primary,
        fontSize = 9.2.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(3.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        for (column in table.columns) {
            Text(
                column,
                modifier = Modifier.weight(1f),
                color = LabColors.InkMuted,
                fontSize = 8.3.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (column in table.columns.indices) {
            val key = challengeCellKey(task, 0, column)
            OutlinedTextField(
                value = editor.challengeInputs[key].orEmpty(),
                onValueChange = { editor.updateChallengeInput(key, it) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = active,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = LabColors.Ink,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                ),
                placeholder = {
                    Text("0.00", color = LabColors.InkFaint, fontSize = 10.sp, textAlign = TextAlign.Center)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
    }
    Text(
        table.hint + if (table.unit.isNotEmpty()) "（单位：${table.unit}）" else "",
        color = LabColors.InkFaint,
        fontSize = 8.5.sp,
        modifier = Modifier.padding(top = 2.dp),
    )
}
