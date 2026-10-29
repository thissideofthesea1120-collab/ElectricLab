package com.physlab.electric.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.CircuitStatus
import com.physlab.electric.model.InstrumentType

private data class ExperimentStep(val title: String, val hint: String)

private fun stepsFor(template: ExperimentTemplate?): List<ExperimentStep> = when (template) {
    ExperimentTemplate.SIMPLE -> listOf(
        ExperimentStep("连接电路", "电池、开关、小灯泡依次串联"),
        ExperimentStep("闭合开关", "观察灯泡是否发光"),
        ExperimentStep("断开开关", "观察灯泡熄灭"),
    )
    ExperimentTemplate.SERIES,
    ExperimentTemplate.SERIES_CURRENT,
    ExperimentTemplate.SERIES_VOLTAGE -> listOf(
        ExperimentStep("连接串联电路", "电源、开关、两灯依次串联"),
        ExperimentStep("接入电流表", "电流表串联在干路"),
        ExperimentStep("接入电压表", "电压表并联在待测部分"),
        ExperimentStep("记录数据", "闭合开关，记录电流和电压"),
    )
    ExperimentTemplate.PARALLEL,
    ExperimentTemplate.PARALLEL_CURRENT,
    ExperimentTemplate.PARALLEL_VOLTAGE -> listOf(
        ExperimentStep("连接并联电路", "两灯并联，开关控制干路"),
        ExperimentStep("测干路电流", "电流表串联在干路"),
        ExperimentStep("测支路电流", "分别测量两条支路"),
        ExperimentStep("测电压", "电压表并联在各支路两端"),
    )
    ExperimentTemplate.OHM_LAW,
    ExperimentTemplate.VOLT_AMMETER_IN,
    ExperimentTemplate.VOLT_AMMETER_OUT,
    ExperimentTemplate.BULB_POWER,
    ExperimentTemplate.BULB_CURVE -> listOf(
        ExperimentStep("连接电路", "电源、开关、滑动变阻器、待测元件串联"),
        ExperimentStep("接入电流表", "电流表串联"),
        ExperimentStep("接入电压表", "电压表并联在待测元件两端"),
        ExperimentStep("移动滑片", "改变电压和电流，记录多组数据"),
        ExperimentStep("数据处理", "用 U-I 图像或 R=U/I 计算"),
    )
    ExperimentTemplate.EMF_INTERNAL -> listOf(
        ExperimentStep("连接电路", "电源、开关、电流表、滑动变阻器串联"),
        ExperimentStep("接入电压表", "电压表并联在电源两端"),
        ExperimentStep("改变外电阻", "移动滑片，记录多组 U、I"),
        ExperimentStep("作图", "U-I 图像截距为 E，斜率为 r"),
    )
    ExperimentTemplate.METER_MOD, ExperimentTemplate.HALF_BIAS -> listOf(
        ExperimentStep("连接基础电路", "灵敏电流计与电阻箱按图连接"),
        ExperimentStep("调节电阻", "观察指针偏转"),
        ExperimentStep("记录数据", "根据分流/分压关系计算"),
    )
    ExperimentTemplate.MULTIMETER -> listOf(
        ExperimentStep("选择档位", "选择电阻档并调零"),
        ExperimentStep("连接表笔", "表笔接电阻两端，注意断电"),
        ExperimentStep("读数", "根据倍率读取电阻值"),
    )
    ExperimentTemplate.RC_CHARGE -> listOf(
        ExperimentStep("连接电路", "电容、电阻、开关与电源连接"),
        ExperimentStep("充电", "闭合开关，观察电压变化"),
        ExperimentStep("放电", "断开电源，观察放电过程"),
    )
    else -> listOf(
        ExperimentStep("观察实验台", "确认仪器和接线"),
        ExperimentStep("闭合开关", "观察电路状态"),
        ExperimentStep("记录现象", "记录读数或发光情况"),
    )
}

@Composable
fun ExperimentGuideDialog(editor: LabEditorState, solution: CircuitSolution) {
    val template = editor.activeTemplate
    val steps = stepsFor(template)
    val closed = solution.status == CircuitStatus.CLOSED
    val hasCurrent = solution.totalCurrent > 0.0001f
    AlertDialog(
        onDismissRequest = { editor.closeGuide() },
        title = {
            Column {
                Text("实验步骤", fontWeight = FontWeight.Bold)
                Text(
                    template?.title ?: "自由实验",
                    color = LabColors.InkFaint,
                    fontSize = 10.sp,
                )
            }
        },
        text = {
            Column(
                Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                steps.forEachIndexed { index, step ->
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.SurfaceHigh.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${index + 1}",
                                color = LabColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Column {
                                Text(step.title, color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(step.hint, color = LabColors.InkFaint, fontSize = 9.sp, lineHeight = 13.sp)
                            }
                        }
                    }
                }
                val checks = buildList {
                    add("电路接通" to closed)
                    add("有电流" to hasCurrent)
                    add("至少有一个用电器" to editor.components.any {
                        it.type == InstrumentType.LAMP || it.type == InstrumentType.TEMP_LAMP
                    })
                    add("至少记录一组数据" to editor.records.isNotEmpty())
                }
                Text("完成检查", color = LabColors.InkMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                for ((label, done) in checks) {
                    Text(
                        "${if (done) "✓" else "○"} $label",
                        color = if (done) LabColors.Green else LabColors.InkFaint,
                        fontSize = 10.sp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { editor.closeGuide() }) {
                Text("关闭", color = LabColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}
