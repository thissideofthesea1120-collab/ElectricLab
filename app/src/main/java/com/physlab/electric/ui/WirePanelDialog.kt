package com.physlab.electric.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment

@Composable
fun WirePanelDialog(editor: LabEditorState, wire: WireSegment) {
    AlertDialog(
        onDismissRequest = { editor.closeWirePanel() },
        title = {
            Column {
                Text("导线 · ${wire.color.displayName}", fontWeight = FontWeight.Bold)
                Text(
                    "拖动导线任意位置即可弯曲",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("更换颜色", color = LabColors.InkMuted, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    for (color in WireColor.entries) {
                        val selected = color == wire.color
                        Surface(
                            onClick = { editor.setSelectionWireColor(color) },
                            shape = RoundedCornerShape(9.dp),
                            color = if (selected) color.body.copy(alpha = 0.18f) else LabColors.SurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) color.highlight.copy(alpha = 0.8f) else LabColors.OutlineSoft,
                            ),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(color.body),
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    color.displayName,
                                    color = if (selected) color.highlight else LabColors.InkMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
                Text(
                    "当前弯折点：${wire.bends.size} 个。直接拖动导线上任意位置即可继续改变形状。",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (wire.bends.isNotEmpty()) {
                        Surface(
                            onClick = { editor.clearSelectedWireBends() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9.dp),
                            color = LabColors.Primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                LabColors.Primary.copy(alpha = 0.35f),
                            ),
                        ) {
                            Text(
                                "拉直导线",
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = LabColors.Primary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Surface(
                        onClick = {
                            editor.deleteSelection()
                            editor.closeWirePanel()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.Red.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            LabColors.Red.copy(alpha = 0.35f),
                        ),
                    ) {
                        Text(
                            "删除导线",
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = LabColors.Red,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { editor.closeWirePanel() }) {
                Text("完成", color = LabColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}
