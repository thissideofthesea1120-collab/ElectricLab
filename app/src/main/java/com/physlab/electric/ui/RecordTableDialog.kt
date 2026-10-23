package com.physlab.electric.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.ui.render.formatAmmeter
import com.physlab.electric.ui.render.formatNumber
import com.physlab.electric.ui.render.formatVoltmeter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordTableDialog(
    records: List<MeasurementRecord>,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    val voltmeterCount = maxOf(1, records.maxOfOrNull { it.voltmeterValues.size } ?: 0)
    val ammeterCount = maxOf(1, records.maxOfOrNull { it.ammeterValues.size } ?: 0)
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Column {
                Text("电压 / 电流记录表", fontWeight = FontWeight.Bold)
                Text(
                    "放大表盘并验证读数正确后，可选择“记录到表格”；不验证不会自动记录",
                    color = LabColors.InkFaint,
                    fontSize = 9.2.sp,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 240.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TableHeader(voltmeterCount, ammeterCount)
                if (records.isEmpty()) {
                    Text(
                        "暂无记录。放大表盘、验证读数正确后，可选择“记录到表格”。",
                        color = LabColors.InkFaint,
                        fontSize = 9.5.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    records.asReversed().forEachIndexed { index, record ->
                        RecordRow(
                            number = records.size - index,
                            record = record,
                            time = timeFormatter.format(Date(record.createdAt)),
                            voltmeterCount = voltmeterCount,
                            ammeterCount = ammeterCount,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("关闭", color = LabColors.InkMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onClear) {
                Text("清空记录", color = LabColors.Red)
            }
        },
        containerColor = LabColors.SurfaceRaised,
        titleContentColor = LabColors.Ink,
        textContentColor = LabColors.InkMuted,
    )
}

@Composable
private fun TableHeader(voltmeterCount: Int, ammeterCount: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell("#", 0.4f)
        HeaderCell("时间", 1.05f)
        HeaderCell("电源电压", 0.95f)
        HeaderCell("总电流", 0.9f)
        for (index in 1..voltmeterCount) {
            HeaderCell("U$index", 0.8f)
        }
        for (index in 1..ammeterCount) {
            HeaderCell("I$index", 0.8f)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text,
        modifier = Modifier.weight(weight),
        color = LabColors.Primary,
        fontSize = 8.3.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun RecordRow(
    number: Int,
    record: MeasurementRecord,
    time: String,
    voltmeterCount: Int,
    ammeterCount: Int,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cell(number.toString(), 0.4f, LabColors.InkFaint)
        Cell(time, 1.05f, LabColors.InkMuted)
        Cell("${formatNumber(record.sourceVoltage)} V", 0.95f, LabColors.Ink)
        Cell(formatAmmeter(record.totalCurrent), 0.9f, LabColors.Ink)
        for (index in 0 until voltmeterCount) {
            val value = record.voltmeterValues.getOrNull(index)
            Cell(value?.let { formatVoltmeter(it) } ?: "—", 0.8f, LabColors.InkMuted)
        }
        for (index in 0 until ammeterCount) {
            val value = record.ammeterValues.getOrNull(index)
            Cell(value?.let { formatAmmeter(it) } ?: "—", 0.8f, LabColors.InkMuted)
        }
    }
}

@Composable
private fun RowScope.Cell(text: String, weight: Float, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        modifier = Modifier.weight(weight),
        color = color,
        fontSize = 8.6.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}
