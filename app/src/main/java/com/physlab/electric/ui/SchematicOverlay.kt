package com.physlab.electric.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.CircuitStatus
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.WireSegment
import com.physlab.electric.ui.render.SchematicScene
import com.physlab.electric.ui.render.drawSchematic
import com.physlab.electric.ui.render.formatCurrent
import com.physlab.electric.ui.render.formatNumber

@Composable
fun SchematicOverlay(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    solution: CircuitSolution,
    showReadings: Boolean,
    onClose: () -> Unit,
) {
    BackHandler { onClose() }
    val density = LocalDensity.current.density
    val electronTransition = rememberInfiniteTransition(label = "schematic-flow")
    val electronPhase = electronTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "schematic-electron-phase",
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LabColors.Background,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(LabColors.SurfaceRaised.copy(alpha = 0.98f), LabColors.Surface.copy(alpha = 0.95f)),
                        ),
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LabColors.Primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    LabIcon(LabIcon.CIRCUIT, LabColors.Primary, Modifier.size(23.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "电路图",
                            color = LabColors.Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LabColors.Primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.24f)),
                        ) {
                            Text(
                                "由实物接线自动生成",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                color = LabColors.Primary,
                                fontSize = 9.3.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text(
                        "${components.size} 件仪器 · ${wires.size} 根导线 · 标准电路符号",
                        color = LabColors.InkFaint,
                        fontSize = 9.8.sp,
                    )
                }

                StatusChip(solution)
                if (showReadings) {
                    Spacer(Modifier.width(8.dp))
                    StatChip("电源", "${formatNumber(solution.batteryVoltage)} V")
                    Spacer(Modifier.width(6.dp))
                    StatChip("总电流", formatCurrent(solution.totalCurrent))
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    onClick = onClose,
                    shape = RoundedCornerShape(10.dp),
                    color = LabColors.SurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                ) {
                    Row(
                        Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LabIcon(LabIcon.CLOSE, LabColors.InkMuted, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("返回实验台", color = LabColors.InkMuted, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawSchematic(
                        SchematicScene(
                            components = components,
                            wires = wires,
                            solution = solution,
                            showReadings = showReadings,
                            electronPhase = if (showReadings && solution.status == CircuitStatus.CLOSED) {
                                electronPhase.value
                            } else {
                                0f
                            },
                        ),
                        density,
                    )
                }

                if (components.isEmpty()) {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LabIcon(LabIcon.CIRCUIT, LabColors.InkFaint, Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("还没有可转换的电路", color = LabColors.InkMuted, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(LabColors.Surface.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                LegendDot(if (showReadings) LabColors.Primary else LabColors.InkFaint, if (showReadings) "电流方向" else "读数已隐藏")
                Text(
                    "电路图依据接线柱之间的实际连接关系生成；串联回路会自动排成规范矩形。",
                    color = LabColors.InkFaint,
                    fontSize = 9.5.sp,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(solution: CircuitSolution) {
    val (color, text) = when (solution.status) {
        CircuitStatus.CLOSED -> LabColors.Green to "通路"
        CircuitStatus.OPEN -> LabColors.Amber to "断路"
        CircuitStatus.SHORT -> LabColors.Red to "短路"
        CircuitStatus.NO_POWER -> LabColors.InkMuted to "未接电源"
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.13f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.32f)),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(text, color = color, fontSize = 9.8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LabColors.SurfaceHigh.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = LabColors.InkFaint, fontSize = 9.2.sp)
            Spacer(Modifier.width(5.dp))
            Text(value, color = LabColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, color = LabColors.InkFaint, fontSize = 9.5.sp)
    }
}
