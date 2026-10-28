package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HouseholdTemplate(val title: String, val subtitle: String) {
    LAMP("一个开关控制一盏灯", "火线进开关，开关控制灯"),
    SOCKET("两孔与三孔插座", "左零右火，三孔上接地"),
    TWO_WAY("双控开关", "两个开关控制同一盏灯"),
}

enum class HouseholdFault(val title: String) {
    NONE("正常"),
    SHORT("火零短路"),
    OVERLOAD("过载"),
    LEAK("漏电"),
    OPEN_NEUTRAL("零线断路"),
    SWITCH_NEUTRAL("开关接零线"),
}

@Stable
class HouseholdState {
    var template by mutableStateOf(HouseholdTemplate.LAMP)
    var mainOn by mutableStateOf(true)
    var rcdOn by mutableStateOf(true)
    var lampSwitch by mutableStateOf(false)
    var secondSwitch by mutableStateOf(false)
    var fault by mutableStateOf(HouseholdFault.NONE)
    var tripped by mutableStateOf(false)

    val lampOn: Boolean
        get() = mainOn && rcdOn && !tripped &&
            fault != HouseholdFault.OPEN_NEUTRAL &&
            when (template) {
                HouseholdTemplate.LAMP -> lampSwitch
                HouseholdTemplate.TWO_WAY -> lampSwitch xor secondSwitch
                HouseholdTemplate.SOCKET -> false
            }

    val socketLive: Boolean
        get() = mainOn && rcdOn && !tripped &&
            fault != HouseholdFault.OPEN_NEUTRAL &&
            template == HouseholdTemplate.SOCKET

    val status: String
        get() = when {
            tripped -> "保护器已跳闸"
            fault == HouseholdFault.OPEN_NEUTRAL -> "零线断路，用电器不工作"
            fault == HouseholdFault.SWITCH_NEUTRAL -> "开关接在零线，存在安全隐患"
            !mainOn -> "总开关断开"
            !rcdOn -> "漏电保护器断开"
            lampOn -> "灯亮，电路正常"
            socketLive -> "插座带电，电路正常"
            else -> "待机"
        }

    fun applyFault(next: HouseholdFault) {
        fault = next
        tripped = next == HouseholdFault.SHORT ||
            next == HouseholdFault.OVERLOAD ||
            next == HouseholdFault.LEAK
        if (tripped) rcdOn = false
    }

    fun resetProtection() {
        fault = HouseholdFault.NONE
        tripped = false
        rcdOn = true
        mainOn = true
    }
}

@Composable
fun HouseholdLabScreen(state: HouseholdState, onClose: () -> Unit) {
    Surface(color = LabColors.Background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(LabColors.SurfaceRaised).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("家庭电路", color = LabColors.Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(14.dp))
                Text("220V 交流 · 安全用电", color = Color(0xFFFFC857), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                HouseButton(if (state.mainOn) "断开总开关" else "闭合总开关") { state.mainOn = !state.mainOn }
                Spacer(Modifier.width(8.dp))
                HouseButton("漏保复位", primary = true) { state.resetProtection() }
                Spacer(Modifier.width(8.dp))
                HouseButton("返回电路", primary = true, onClick = onClose)
            }
            Row(Modifier.fillMaxSize()) {
                HouseholdCanvas(state, Modifier.weight(1f).fillMaxSize())
                HouseholdPanel(state, Modifier.width(290.dp).fillMaxSize())
            }
        }
    }
}

@Composable
private fun HouseButton(label: String, primary: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) LabColors.Primary else LabColors.SurfaceHigh,
            contentColor = if (primary) LabColors.Background else LabColors.Ink,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HouseholdCanvas(state: HouseholdState, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFF080D13))) {
        drawHouseholdGrid()
        drawDistribution(state)
        when (state.template) {
            HouseholdTemplate.LAMP, HouseholdTemplate.TWO_WAY -> drawLampCircuit(state)
            HouseholdTemplate.SOCKET -> drawSocketCircuit(state)
        }
        drawStatus(state)
    }
}

private fun DrawScope.drawHouseholdGrid() {
    var x = 0f
    while (x < size.width) {
        drawLine(Color(0xFF152231).copy(alpha = 0.6f), Offset(x, 0f), Offset(x, size.height), 1f)
        x += 64f
    }
    var y = 0f
    while (y < size.height) {
        drawLine(Color(0xFF152231).copy(alpha = 0.6f), Offset(0f, y), Offset(size.width, y), 1f)
        y += 64f
    }
}

private fun DrawScope.drawDistribution(state: HouseholdState) {
    val left = size.width * 0.10f
    val top = size.height * 0.16f
    val width = size.width * 0.25f
    val height = size.height * 0.56f
    drawRoundRect(
        Color(0xFF1B2430),
        Offset(left, top),
        Size(width, height),
        CornerRadius(12f),
    )
    drawRoundRect(
        Color(0xFF4FB3D9).copy(alpha = 0.6f),
        Offset(left, top),
        Size(width, height),
        CornerRadius(12f),
        style = Stroke(1.5f),
    )
    householdText("配电箱", Offset(left + width / 2f, top + 26f), Color(0xFFDCE9F6), 13f)
    val bx = left + width / 2f
    val by = top + 76f
    drawLine(
        if (state.mainOn) Color(0xFF7DE0C0) else Color(0xFFFF8A92),
        Offset(bx - 28f, by),
        Offset(bx + 28f, by),
        15f,
    )
    householdText(if (state.mainOn) "总开关 ON" else "总开关 OFF", Offset(bx, by + 30f), Color(0xFFDCE9F6), 10f)
    val rx = bx
    val ry = by + 80f
    drawCircle(if (state.rcdOn) Color(0xFF7DE0C0) else Color(0xFFFF8A92), 17f, Offset(rx, ry))
    drawCircle(Color(0xFF05080C), 9f, Offset(rx, ry))
    householdText("漏电保护", Offset(rx, ry + 28f), Color(0xFFDCE9F6), 9.5f)
    householdText(if (state.rcdOn) "RCD 正常" else "RCD 跳闸", Offset(rx, ry + 45f), Color(0xFFDCE9F6), 8.5f)
}

private fun DrawScope.drawLampCircuit(state: HouseholdState) {
    val startX = size.width * 0.40f
    val lY = size.height * 0.28f
    val nY = size.height * 0.62f
    val peY = size.height * 0.74f
    drawWireLine(startX, lY, size.width * 0.92f, lY, Color(0xFFFF6B72))
    drawWireLine(startX, nY, size.width * 0.92f, nY, Color(0xFF5AB6FF))
    drawWireLine(startX, peY, size.width * 0.92f, peY, Color(0xFF9BD48B))
    householdText("L 火线", Offset(startX + 44f, lY - 14f), Color(0xFFFF9CA1), 10f)
    householdText("N 零线", Offset(startX + 44f, nY - 14f), Color(0xFF9CD6FF), 10f)
    householdText("PE 地线", Offset(startX + 50f, peY - 14f), Color(0xFFA8E3A0), 10f)

    val switchX = size.width * 0.49f
    drawLine(Color(0xFF151A21), Offset(switchX - 22f, lY), Offset(switchX + 22f, lY), 4f)
    drawCircle(Color(0xFF7FE4C0), 4f, Offset(switchX - 22f, lY))
    drawCircle(Color(0xFF7FE4C0), 4f, Offset(switchX + 22f, lY))
    if (state.lampSwitch) {
        drawLine(Color(0xFF7FE4C0), Offset(switchX - 20f, lY), Offset(switchX + 20f, lY), 3f)
    } else {
        drawLine(Color(0xFF7FE4C0), Offset(switchX - 20f, lY), Offset(switchX + 16f, lY - 24f), 3f)
    }
    householdText("S 开关", Offset(switchX, lY + 30f), Color(0xFFDCE9F6), 10f)

    val lampX = size.width * 0.70f
    val lampY = (lY + nY) / 2f
    drawLine(Color(0xFFFFC857), Offset(switchX + 20f, lY), Offset(lampX, lY), 2.5f)
    drawLine(Color(0xFFFFC857), Offset(lampX, lY), Offset(lampX, lampY - 24f), 2.5f)
    drawLine(Color(0xFF5AB6FF), Offset(lampX, lampY + 24f), Offset(lampX, nY), 2.5f)
    if (state.lampOn) {
        drawCircle(Color(0xFFFFD35C).copy(alpha = 0.23f), 42f, Offset(lampX, lampY))
    }
    drawCircle(
        if (state.lampOn) Color(0xFFFFD35C) else Color(0xFF35404D),
        24f,
        Offset(lampX, lampY),
    )
    drawCircle(Color(0xFF151A21), 16f, Offset(lampX, lampY))
    householdText("灯泡", Offset(lampX, lampY + 44f), Color(0xFFDCE9F6), 10f)

    if (state.template == HouseholdTemplate.TWO_WAY) {
        val secondX = size.width * 0.57f
        drawCircle(Color(0xFF7FE4C0), 4f, Offset(secondX, lY))
        householdText("S2", Offset(secondX, lY + 30f), Color(0xFFDCE9F6), 10f)
    }
    drawHouseholdPerson(state, size.width * 0.84f, nY + 30f)
}

private fun DrawScope.drawSocketCircuit(state: HouseholdState) {
    val startX = size.width * 0.40f
    val lY = size.height * 0.28f
    val nY = size.height * 0.54f
    val peY = size.height * 0.76f
    drawWireLine(startX, lY, size.width * 0.92f, lY, Color(0xFFFF6B72))
    drawWireLine(startX, nY, size.width * 0.92f, nY, Color(0xFF5AB6FF))
    drawWireLine(startX, peY, size.width * 0.92f, peY, Color(0xFF9BD48B))
    householdText("L 火线", Offset(startX + 44f, lY - 14f), Color(0xFFFF9CA1), 10f)
    householdText("N 零线", Offset(startX + 44f, nY - 14f), Color(0xFF9CD6FF), 10f)
    householdText("PE 地线", Offset(startX + 50f, peY - 14f), Color(0xFFA8E3A0), 10f)
    val x = size.width * 0.66f
    drawRoundRect(Color(0xFF1B2430), Offset(x - 50f, lY + 30f), Size(100f, 150f), CornerRadius(12f))
    drawRoundRect(Color(0xFF6BCBE0), Offset(x - 50f, lY + 30f), Size(100f, 150f), CornerRadius(12f), style = Stroke(1.5f))
    householdText("三孔插座", Offset(x, lY + 58f), Color(0xFFDCE9F6), 10f)
    drawCircle(Color(0xFFFF9CA1), 5f, Offset(x - 22f, lY + 110f))
    drawCircle(Color(0xFF9CD6FF), 5f, Offset(x + 22f, lY + 110f))
    drawCircle(Color(0xFFA8E3A0), 5f, Offset(x, lY + 82f))
    drawWireLine(x - 22f, lY + 110f, x - 22f, nY, Color(0xFF5AB6FF))
    drawWireLine(x + 22f, lY + 110f, x + 22f, lY, Color(0xFFFF6B72))
    drawWireLine(x, lY + 82f, x, peY, Color(0xFF9BD48B))
    householdText(
        if (state.socketLive) "插座带电" else "插座无电",
        Offset(x, lY + 160f),
        if (state.socketLive) Color(0xFF7DE0C0) else Color(0xFF7D91A8),
        10f,
    )
}

private fun DrawScope.drawHouseholdPerson(state: HouseholdState, x: Float, y: Float) {
    if (state.fault != HouseholdFault.LEAK) return
    drawCircle(Color(0xFFFF8A92), 10f, Offset(x, y - 28f))
    drawLine(Color(0xFFFF8A92), Offset(x, y - 18f), Offset(x, y + 10f), 5f)
    drawLine(Color(0xFFFF8A92), Offset(x - 16f, y - 8f), Offset(x + 16f, y - 8f), 4f)
    drawLine(Color(0xFFFF8A92), Offset(x, y + 10f), Offset(x - 14f, y + 34f), 4f)
    drawLine(Color(0xFFFF8A92), Offset(x, y + 10f), Offset(x + 14f, y + 34f), 4f)
    householdText("触电危险", Offset(x, y + 52f), Color(0xFFFF8A92), 9.5f)
}

private fun DrawScope.drawStatus(state: HouseholdState) {
    val color = when {
        state.tripped -> Color(0xFFFF8A92)
        state.lampOn || state.socketLive -> Color(0xFF7DE0C0)
        else -> Color(0xFF8FA3B8)
    }
    householdText(state.status, Offset(size.width / 2f, size.height - 20f), color, 11f)
    householdText(
        if (state.tripped) "保护器已动作，请排除故障后复位" else "220V 交流，禁止湿手操作",
        Offset(size.width / 2f, size.height - 42f),
        Color(0xFF7D91A8),
        9f,
    )
}

private fun DrawScope.drawWireLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Color) {
    drawLine(color, Offset(x1, y1), Offset(x2, y2), 2.5f)
}

private fun DrawScope.householdText(text: String, position: Offset, color: Color, size: Float) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )
        textSize = size
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    drawContext.canvas.nativeCanvas.drawText(text, position.x, position.y, paint)
}

@Composable
private fun HouseholdPanel(state: HouseholdState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(LabColors.SurfaceRaised)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("实验模板", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        for (template in HouseholdTemplate.entries) {
            HouseTemplateRow(template, state.template == template) {
                state.template = template
                state.fault = HouseholdFault.NONE
                state.tripped = false
                state.rcdOn = true
            }
        }
        Text("操作", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HouseButton("开关") { state.lampSwitch = !state.lampSwitch }
            if (state.template == HouseholdTemplate.TWO_WAY) {
                HouseButton("S2") { state.secondSwitch = !state.secondSwitch }
            }
        }
        Text("故障模拟", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        for (fault in HouseholdFault.entries) {
            if (fault == HouseholdFault.NONE) continue
            HouseButton(fault.title, primary = state.fault == fault) { state.applyFault(fault) }
        }
        HouseButton("排除故障并复位", primary = true) { state.resetProtection() }
        Text(
            "说明：本页为家庭电路演示模式，采用 220V 交流简化模型；\n" +
                "火线进开关、左零右火上接地；故障时保护器动作。\n" +
                "后续将加入自由接线和更多家庭电路模板。",
            color = LabColors.InkFaint,
            fontSize = 10.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun HouseTemplateRow(template: HouseholdTemplate, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) LabColors.Primary.copy(alpha = 0.15f) else LabColors.SurfaceHigh.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(template.title, color = LabColors.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(template.subtitle, color = LabColors.InkFaint, fontSize = 9.sp)
        }
    }
}
