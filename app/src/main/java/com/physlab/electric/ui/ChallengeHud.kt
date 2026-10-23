package com.physlab.electric.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChallengeHud(
    editor: LabEditorState,
    onOpenTable: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!editor.challengeActive) return
    val task = ChallengeTask.entries[editor.challengeIndex.coerceIn(0, ChallengeTask.entries.lastIndex)]
    val result = editor.challengeResult
    Surface(
        modifier = modifier.padding(8.dp).widthIn(max = 232.dp),
        shape = RoundedCornerShape(12.dp),
        color = LabColors.Surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.40f)),
        shadowElevation = 10.dp,
    ) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(
                "闯关中 · ${task.title}",
                color = LabColors.Ink,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                if (result == null) "连接电路并完成数据后提交" else if (result.passed) {
                    "已完成 · ${result.score} 分"
                } else {
                    "未全部通过 · ${result.score} 分"
                },
                color = if (result?.passed == true) LabColors.Green else LabColors.InkFaint,
                fontSize = 8.sp,
                maxLines = 1,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onOpenTable,
                    shape = RoundedCornerShape(6.dp),
                    color = LabColors.SurfaceHigh,
                    border = BorderStroke(1.dp, LabColors.Outline),
                ) {
                    Text(
                        if (task.table != null) "数据表" else "查看目标",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        color = LabColors.InkMuted,
                        fontSize = 8.5.sp,
                    )
                }
                Surface(
                    onClick = onSubmit,
                    shape = RoundedCornerShape(6.dp),
                    color = LabColors.Primary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.45f)),
                ) {
                    Text(
                        "提交",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = LabColors.Primary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
