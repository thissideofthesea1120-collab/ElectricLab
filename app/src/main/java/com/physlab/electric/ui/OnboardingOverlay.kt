package com.physlab.electric.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val OnboardingStepCount = 8

private data class GuideStep(
    val title: String,
    val body: String,
    val showSeriesExample: Boolean = false,
)

private val GuideSteps = listOf(
    GuideStep(
        "认识实验台",
        "从左侧仪器库拖动仪器到实验台，或轻点卡片快速放到中央。\n" +
            "拖动仪器可移动位置；长按仪器可查看规格、调整参数或删除。",
    ),
    GuideStep(
        "接线与改接",
        "点一个接线柱再点另一个接线柱即可接线，也可以直接从接线柱拖到另一个。\n" +
            "电源、电流表、电压表都有正负接线柱，接线要分清“+ / −”。\n" +
            "如果两根导线交叉，软件会提醒“导线交叉了”，请拖动导线改变走线。",
    ),
    GuideStep(
        "弯线、改接和删除",
        "接好后拖动导线中段可以增加弯折，让走线更整齐。\n" +
            "从已有导线的接线柱拖动，可把这一端改接到别的接线柱；原支路会暂时断开。\n" +
            "长按导线可换色、拉直或删除；导线缩到最短也会自动删除。",
    ),
    GuideStep(
        "跟着连一个串联电路",
        "按下面顺序练习：\n" +
            "① 电池组“+” → 开关左端\n" +
            "② 开关右端 → 小灯泡 L1 左端\n" +
            "③ L1 右端 → 小灯泡 L2 左端\n" +
            "④ L2 右端 → 电流表“+”\n" +
            "⑤ 电流表“−” → 电池组“−”\n" +
            "五根线接好后点右上角“读数”，应形成一条闭合回路。",
        showSeriesExample = true,
    ),
    GuideStep(
        "读数、验证与记录",
        "点电流表或电压表可放大表盘：每个大格分 5 小格，0 左侧的一大格负刻度也分 5 小格。\n" +
            "超过正测量范围 1.2 倍，或指针越过负刻度线，仪表会损坏。\n" +
            "关闭“读数”后可在表盘下方输入读数并验证；验证正确后可自己选择是否记录到表格。",
    ),
    GuideStep(
        "灯泡、电动机和蜂鸣器",
        "小灯泡按实际功率统一决定亮度：不同规格实际功率相同，亮度就相同。\n" +
            "电动机和蜂鸣器额定电压都是 4.5 V；电动机实际功率越大扇叶转得越快，蜂鸣器实际功率越大声音越响。",
    ),
    GuideStep(
        "磁学与电源极性",
        "磁体、电磁铁和螺线管可拖动绿色手柄旋转；长按可翻转磁极或绕线方向。\n" +
            "通电线圈的磁场方向会随电流方向或绕线方向改变；电源也可长按对调正负极。",
    ),
    GuideStep(
        "视图、保存与帮助",
        "双指拖动平移实验台，双指捏合/张开缩放，最大 1.5 倍；“更多 → 复位实验台视图”可恢复。\n" +
            "“更多”中还有实验模板、任务闯关、实验快照、电压/电流记录表和使用说明。\n" +
            "任务闯关从空实验台开始，80 分过关、100 分解锁下下关；每关最高分自动保存。\n" +
            "同时有电压表和滑动变阻器时，拖动滑片会显示透明电压表放大刻度，松手后自动消失。",
    ),
)

@Composable
fun OnboardingOverlay(
    step: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onValidateSeries: () -> Unit = {},
    onLoadSeriesTemplate: () -> Unit = {},
    maxWidthDp: Dp = 620.dp,
    maxHeightDp: Dp = 560.dp,
) {
    val safeStep = step.coerceIn(0, GuideSteps.lastIndex)
    val guide = GuideSteps[safeStep]
    val last = safeStep == GuideSteps.lastIndex

    BackHandler { onSkip() }

    if (safeStep == 3) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.padding(12.dp).widthIn(max = 350.dp),
                shape = RoundedCornerShape(14.dp),
                color = LabColors.Surface.copy(alpha = 0.97f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.40f)),
                shadowElevation = 12.dp,
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "新手引导 4 / ${GuideSteps.size} · 串联电路",
                            color = LabColors.Primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onSkip) {
                            Text("跳过", color = LabColors.InkMuted, fontSize = 9.sp)
                        }
                    }
                    Text(
                        "电池组 + → 开关 → L1 → L2 → 电流表 +，电流表 − → 电池组 −。接好后点“提交验证”。",
                        color = LabColors.InkMuted,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = onLoadSeriesTemplate,
                            shape = RoundedCornerShape(8.dp),
                            color = LabColors.SurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                        ) {
                            Text(
                                "对照模板",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = LabColors.InkMuted,
                                fontSize = 9.sp,
                            )
                        }
                        Surface(
                            onClick = onValidateSeries,
                            shape = RoundedCornerShape(8.dp),
                            color = LabColors.Primary.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                "提交验证",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = LabColors.Primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.76f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = maxWidthDp)
                .fillMaxWidth(0.88f)
                .heightIn(max = maxHeightDp),
            shape = RoundedCornerShape(22.dp),
            color = LabColors.Surface.copy(alpha = 0.99f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.35f)),
            shadowElevation = 24.dp,
        ) {
            Column(
                Modifier
                    .heightIn(max = maxHeightDp - 20.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(LabColors.Primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${safeStep + 1}",
                            color = LabColors.Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(9.dp))
                    Text(
                        "新手引导  ${safeStep + 1} / ${GuideSteps.size}",
                        color = LabColors.InkFaint,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onSkip) {
                        Text("跳过", color = LabColors.InkMuted, fontSize = 10.5.sp)
                    }
                }

                Text(
                    guide.title,
                    color = LabColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    guide.body,
                    color = LabColors.InkMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                )

                if (guide.showSeriesExample) {
                    Surface(
                        onClick = onLoadSeriesTemplate,
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.Primary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            LabColors.Primary.copy(alpha = 0.36f),
                        ),
                    ) {
                        Text(
                            "载入串联模板对照",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = LabColors.Primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (index in GuideSteps.indices) {
                            Box(
                                Modifier
                                    .size(if (index == safeStep) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == safeStep) LabColors.Primary else LabColors.OutlineSoft,
                                    ),
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = onNext,
                        shape = RoundedCornerShape(10.dp),
                        color = LabColors.Primary.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            LabColors.Primary.copy(alpha = 0.45f),
                        ),
                    ) {
                        Text(
                            if (last) "开始实验" else "下一步",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                            color = LabColors.Primary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
