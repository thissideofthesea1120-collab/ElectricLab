package com.physlab.electric.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChallengeTableOverlay(
    editor: LabEditorState,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!editor.challengeActive) return
    val task = ChallengeTask.entries[editor.challengeIndex.coerceIn(0, ChallengeTask.entries.lastIndex)]
    val table = task.table ?: return
    Surface(
        modifier = modifier
            .widthIn(max = 310.dp)
            .fillMaxHeight(0.82f)
            .padding(end = 10.dp, top = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = LabColors.Surface.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, LabColors.Outline),
        shadowElevation = 12.dp,
    ) {
        Column(
            Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                task.title,
                color = LabColors.Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            ChallengeDataTable(task, table, editor, active = true)
            Spacer(Modifier.height(6.dp))
            Surface(
                onClick = onSubmit,
                shape = RoundedCornerShape(8.dp),
                color = LabColors.Primary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.45f)),
            ) {
                Text(
                    "提交验证",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    color = LabColors.Primary,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
