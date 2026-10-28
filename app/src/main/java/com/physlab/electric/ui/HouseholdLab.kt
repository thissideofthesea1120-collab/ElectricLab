package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HouseholdWireKind { PHASE, NEUTRAL, PE, LOAD }

enum class HouseholdDeviceType(
    val title: String,
    val width: Float,
    val height: Float,
    val terminals: List<HouseholdTerminalSpec>,
    val category: String,
) {
    METER("电能表", 82f, 64f, listOf(
        HouseholdTerminalSpec(Offset(-28f, 22f), HouseholdWireKind.PHASE, "1"),
        HouseholdTerminalSpec(Offset(-8f, 22f), HouseholdWireKind.PHASE, "2"),
        HouseholdTerminalSpec(Offset(12f, 22f), HouseholdWireKind.NEUTRAL, "3"),
        HouseholdTerminalSpec(Offset(32f, 22f), HouseholdWireKind.NEUTRAL, "4"),
    ), "配电"),
    MAIN("总开关", 86f, 68f, listOf(
        HouseholdTerminalSpec(Offset(-26f, 24f), HouseholdWireKind.PHASE, "L"),
        HouseholdTerminalSpec(Offset(26f, 24f), HouseholdWireKind.PHASE, "L"),
        HouseholdTerminalSpec(Offset(-26f, 40f), HouseholdWireKind.NEUTRAL, "N"),
        HouseholdTerminalSpec(Offset(26f, 40f), HouseholdWireKind.NEUTRAL, "N"),
    ), "配电"),
    BUS_PHASE("相线母线", 0f, 0f, (0..5).map {
        HouseholdTerminalSpec(Offset((it - 2.5f) * 95f, 0f), HouseholdWireKind.PHASE, "L${it + 1}")
    }, "母线"),
    BUS_NEUTRAL("中性线母线", 0f, 0f, (0..5).map {
        HouseholdTerminalSpec(Offset((it - 2.5f) * 95f, 0f), HouseholdWireKind.NEUTRAL, "N${it + 1}")
    }, "母线"),
    BUS_PE("保护线母线", 0f, 0f, (0..5).map {
        HouseholdTerminalSpec(Offset((it - 2.5f) * 95f, 0f), HouseholdWireKind.PE, "PE${it + 1}")
    }, "母线"),
    LAMP("灯泡", 76f, 78f, listOf(
        HouseholdTerminalSpec(Offset(-26f, 30f), HouseholdWireKind.LOAD, "1"),
        HouseholdTerminalSpec(Offset(26f, 30f), HouseholdWireKind.LOAD, "2"),
    ), "照明"),
    SWITCH("开关", 74f, 54f, listOf(
        HouseholdTerminalSpec(Offset(-24f, 20f), HouseholdWireKind.LOAD, "1"),
        HouseholdTerminalSpec(Offset(24f, 20f), HouseholdWireKind.LOAD, "2"),
    ), "照明"),
    SOCKET2("两孔插座", 78f, 66f, listOf(
        HouseholdTerminalSpec(Offset(-22f, 28f), HouseholdWireKind.PHASE, "L"),
        HouseholdTerminalSpec(Offset(22f, 28f), HouseholdWireKind.NEUTRAL, "N"),
    ), "插座"),
    SOCKET3("三孔插座", 84f, 74f, listOf(
        HouseholdTerminalSpec(Offset(-25f, 31f), HouseholdWireKind.PHASE, "L"),
        HouseholdTerminalSpec(Offset(25f, 31f), HouseholdWireKind.NEUTRAL, "N"),
        HouseholdTerminalSpec(Offset(0f, 31f), HouseholdWireKind.PE, "PE"),
    ), "插座"),
    KETTLE("电热水壶", 86f, 68f, listOf(
        HouseholdTerminalSpec(Offset(-25f, 28f), HouseholdWireKind.LOAD, "1"),
        HouseholdTerminalSpec(Offset(25f, 28f), HouseholdWireKind.LOAD, "2"),
    ), "家用电器"),
    RICE_COOKER("电饭锅", 86f, 68f, listOf(
        HouseholdTerminalSpec(Offset(-25f, 28f), HouseholdWireKind.LOAD, "1"),
        HouseholdTerminalSpec(Offset(25f, 28f), HouseholdWireKind.LOAD, "2"),
    ), "家用电器"),
}

data class HouseholdTerminalSpec(val offset: Offset, val kind: HouseholdWireKind, val label: String)

data class HouseholdTerminalRef(val deviceId: Long, val index: Int)

data class HouseholdWire(
    val id: Long,
    val a: HouseholdTerminalRef,
    val b: HouseholdTerminalRef,
    val kind: HouseholdWireKind,
)

@Stable
class HouseholdState {
    var devices by mutableStateOf<List<PlacedHouseholdDevice>>(emptyList())
    var wires by mutableStateOf<List<HouseholdWire>>(emptyList())
    var selectedId by mutableStateOf<Long?>(null)
    var pending by mutableStateOf<HouseholdTerminalRef?>(null)
    var mainClosed by mutableStateOf(true)
    var fault by mutableStateOf(HouseholdFault.NONE)
    var tripped by mutableStateOf(false)
    var version by mutableStateOf(0)
    private var nextId = 1L
    private var nextWireId = 1L

    init {
        resetTemplate()
    }

    fun resetTemplate() {
        nextId = 1L
        nextWireId = 1L
        val source = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.METER, Offset(0.10f, 0.62f), false)
        val main = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.MAIN, Offset(0.23f, 0.62f), true)
        val busL = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.BUS_PHASE, Offset(0.62f, 0.17f), false)
        val busN = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.BUS_NEUTRAL, Offset(0.62f, 0.25f), false)
        val busPE = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.BUS_PE, Offset(0.62f, 0.33f), false)
        val lamp = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.LAMP, Offset(0.50f, 0.62f), false)
        val switch = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.SWITCH, Offset(0.38f, 0.50f), true)
        val socket = PlacedHouseholdDevice(nextId++, HouseholdDeviceType.SOCKET3, Offset(0.78f, 0.62f), false)
        devices = listOf(source, main, busL, busN, busPE, lamp, switch, socket)
        wires = buildList {
            add(wire(0, 1, 0, 0, HouseholdWireKind.PHASE)) // meter L out -> main L in
            add(wire(0, 1, 1, 0, HouseholdWireKind.PHASE))
            add(wire(1, 0, 2, 0, HouseholdWireKind.PHASE))
            add(wire(0, 3, 1, 2, HouseholdWireKind.NEUTRAL))
            add(wire(1, 2, 3, 0, HouseholdWireKind.NEUTRAL))
            add(wire(2, 0, 5, 0, HouseholdWireKind.LOAD))
            add(wire(5, 1, 3, 1, HouseholdWireKind.LOAD))
            add(wire(2, 1, 6, 0, HouseholdWireKind.PHASE))
            add(wire(6, 1, 5, 0, HouseholdWireKind.LOAD))
            add(wire(2, 2, 7, 0, HouseholdWireKind.PHASE))
            add(wire(3, 2, 7, 1, HouseholdWireKind.NEUTRAL))
            add(wire(4, 2, 7, 2, HouseholdWireKind.PE))
        }
        selectedId = null
        pending = null
        fault = HouseholdFault.NONE
        tripped = false
        version++
    }

    private fun wire(ai: Int, at: Int, bi: Int, bt: Int, kind: HouseholdWireKind) = HouseholdWire(
        nextWireId++,
        HouseholdTerminalRef(devices[ai].id, at),
        HouseholdTerminalRef(devices[bi].id, bt),
        kind,
    )

    fun addDevice(type: HouseholdDeviceType) {
        val device = PlacedHouseholdDevice(nextId++, type, Offset(0.55f, 0.55f), type == HouseholdDeviceType.SWITCH)
        devices = devices + device
        selectedId = device.id
        version++
    }

    fun deleteSelected() {
        val id = selectedId ?: return
        devices = devices.filterNot { it.id == id }
        wires = wires.filterNot { it.a.deviceId == id || it.b.deviceId == id }
        selectedId = null
        version++
    }

    fun toggleSelectedSwitch() {
        val id = selectedId ?: return
        devices = devices.map {
            if (it.id == id && (it.type == HouseholdDeviceType.SWITCH || it.type == HouseholdDeviceType.MAIN)) {
                it.copy(closed = !it.closed)
            } else it
        }
        version++
    }

    fun toggleMain() {
        mainClosed = !mainClosed
        devices = devices.map {
            if (it.type == HouseholdDeviceType.MAIN) it.copy(closed = mainClosed) else it
        }
        version++
    }

    fun tapTerminal(ref: HouseholdTerminalRef) {
        val pendingRef = pending
        if (pendingRef == null) {
            pending = ref
            selectedId = ref.deviceId
            return
        }
        if (pendingRef == ref) {
            pending = null
            return
        }
        val aDevice = devices.firstOrNull { it.id == pendingRef.deviceId } ?: return
        val bDevice = devices.firstOrNull { it.id == ref.deviceId } ?: return
        val aSpec = aDevice.type.terminals.getOrNull(pendingRef.index) ?: return
        val bSpec = bDevice.type.terminals.getOrNull(ref.index) ?: return
        val kind = when {
            aSpec.kind == HouseholdWireKind.PHASE || bSpec.kind == HouseholdWireKind.PHASE -> HouseholdWireKind.PHASE
            aSpec.kind == HouseholdWireKind.NEUTRAL || bSpec.kind == HouseholdWireKind.NEUTRAL -> HouseholdWireKind.NEUTRAL
            aSpec.kind == HouseholdWireKind.PE || bSpec.kind == HouseholdWireKind.PE -> HouseholdWireKind.PE
            else -> HouseholdWireKind.LOAD
        }
        wires = wires + HouseholdWire(nextWireId++, pendingRef, ref, kind)
        pending = null
        version++
    }

    fun selectDevice(id: Long) {
        selectedId = id
        pending = null
    }

    fun moveSelected(delta: Offset) {
        val id = selectedId ?: return
        devices = devices.map {
            if (it.id == id) it.copy(position = clamp(it.position + delta)) else it
        }
        version++
    }

    private fun clamp(point: Offset) = Offset(
        point.x.coerceIn(0.04f, 0.96f),
        point.y.coerceIn(0.38f, 0.90f),
    )
}

data class PlacedHouseholdDevice(
    val id: Long,
    val type: HouseholdDeviceType,
    val position: Offset,
    val closed: Boolean = false,
    val rotation: Float = 0f,
)

enum class HouseholdFault(val title: String) {
    NONE("正常"),
    SHORT("相线—中性线短路"),
    OVERLOAD("过载"),
    LEAK("漏电"),
    OPEN_NEUTRAL("中性线断路"),
    SWITCH_NEUTRAL("开关接中性线"),
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
                Text("220V 交流 · 相线 / 中性线 / 保护线", color = Color(0xFFFFC857), fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                HouseButton(if (state.mainClosed) "断开总开关" else "闭合总开关") { state.toggleMain() }
                Spacer(Modifier.width(8.dp))
                HouseButton("排除故障并复位", primary = true) {
                    state.fault = HouseholdFault.NONE
                    state.tripped = false
                    state.mainClosed = true
                    state.devices = state.devices.map {
                        if (it.type == HouseholdDeviceType.MAIN) it.copy(closed = true) else it
                    }
                }
                Spacer(Modifier.width(8.dp))
                HouseButton("返回电路", primary = true, onClick = onClose)
            }
            Row(Modifier.fillMaxSize()) {
                HouseholdCanvas(state, Modifier.weight(1f).fillMaxSize())
                HouseholdPanel(state, Modifier.width(300.dp).fillMaxSize())
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
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val dragState = remember { DragState() }
    Canvas(
        modifier
            .background(Color(0xFF080D13))
            .onSizeChanged { boardSize = it }
            .pointerInput(Unit) {
                detectTapGestures { point ->
                    val hit = hitTerminal(state, point, boardSize) ?: hitDevice(state, point, boardSize)
                    if (hit != null) {
                        val (device, terminal) = hit
                        if (terminal >= 0) state.tapTerminal(HouseholdTerminalRef(device.id, terminal))
                        else state.selectDevice(device.id)
                    } else {
                        state.selectedId = null
                        state.pending = null
                    }
                }
            }
            .pointerInput(state.version) {
                detectDragGestures(
                    onDragStart = { point ->
                        val device = hitDevice(state, point, boardSize)?.first
                        dragState.deviceId = device?.id
                        if (device != null) state.selectedId = device.id
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (dragState.deviceId != null && boardSize.width > 0) {
                            state.moveSelected(
                                Offset(dragAmount.x / boardSize.width, dragAmount.y / boardSize.height),
                            )
                        }
                    },
                    onDragEnd = { dragState.deviceId = null },
                )
            },
    ) {
        drawHouseholdGrid()
        drawBuses(state)
        drawSourceChain(state)
        state.wires.forEach { wire -> drawHouseholdWire(state, wire) }
        state.devices.forEach { device -> drawHouseholdDevice(state, device) }
        drawHouseholdStatus(state)
        drawPending(state)
    }
}

private class DragState { var deviceId: Long? = null }

private fun hitTerminal(
    state: HouseholdState,
    point: Offset,
    size: IntSize,
): Pair<PlacedHouseholdDevice, Int>? {
    if (size.width == 0) return null
    val p = Offset(point.x / size.width, point.y / size.height)
    for (device in state.devices.asReversed()) {
        device.type.terminals.forEachIndexed { index, spec ->
            val terminal = householdTerminal(device, spec)
            if ((terminal - p).getDistance() < 0.022f) return device to index
        }
    }
    return null
}

private fun hitDevice(
    state: HouseholdState,
    point: Offset,
    size: IntSize,
): Pair<PlacedHouseholdDevice, Int>? {
    if (size.width == 0) return null
    val p = Offset(point.x / size.width, point.y / size.height)
    for (device in state.devices.asReversed()) {
        val halfW = (device.type.width / 900f).coerceAtLeast(0.03f)
        val halfH = (device.type.height / 700f).coerceAtLeast(0.03f)
        if (p.x in (device.position.x - halfW)..(device.position.x + halfW) &&
            p.y in (device.position.y - halfH)..(device.position.y + halfH)
        ) return device to -1
    }
    return null
}

private fun householdTerminal(device: PlacedHouseholdDevice, spec: HouseholdTerminalSpec): Offset {
    val radians = Math.toRadians(device.rotation.toDouble())
    val c = Math.cos(radians).toFloat()
    val s = Math.sin(radians).toFloat()
    return device.position + Offset(
        spec.offset.x / 900f * c - spec.offset.y / 700f * s,
        spec.offset.x / 900f * s + spec.offset.y / 700f * c,
    )
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

private fun DrawScope.drawBuses(state: HouseholdState) {
    val colorMap = mapOf(
        HouseholdDeviceType.BUS_PHASE to Color(0xFFFF6B72),
        HouseholdDeviceType.BUS_NEUTRAL to Color(0xFF5AB6FF),
        HouseholdDeviceType.BUS_PE to Color(0xFF9BD48B),
    )
    for (device in state.devices.filter { it.type in colorMap.keys }) {
        val y = device.position.y * size.height
        val x1 = (device.position.x - 0.20f) * size.width
        val x2 = (device.position.x + 0.20f) * size.width
        drawLine(colorMap.getValue(device.type), Offset(x1, y), Offset(x2, y), 4f)
        device.type.terminals.forEachIndexed { index, spec ->
            val point = Offset(householdTerminal(device, spec).x * size.width, householdTerminal(device, spec).y * size.height)
            drawCircle(colorMap.getValue(device.type), 5f, point)
            householdText(spec.label, point + Offset(0f, -10f), colorMap.getValue(device.type), 7f)
        }
        householdText(device.type.title, Offset(x1 - 54f, y + 4f), colorMap.getValue(device.type), 10f)
    }
}

private fun DrawScope.drawSourceChain(state: HouseholdState) {
    state.devices.filter { it.type == HouseholdDeviceType.METER || it.type == HouseholdDeviceType.MAIN }.forEach {
        drawHouseholdDevice(state, it)
    }
    householdText("入户线", Offset(size.width * 0.10f, size.height * 0.47f), Color(0xFFDCE9F6), 10f)
    householdText("~220V", Offset(size.width * 0.10f, size.height * 0.52f), Color(0xFFFFC857), 10f)
}

private fun DrawScope.drawHouseholdWire(state: HouseholdState, wire: HouseholdWire) {
    val a = state.devices.firstOrNull { it.id == wire.a.deviceId } ?: return
    val b = state.devices.firstOrNull { it.id == wire.b.deviceId } ?: return
    val sa = a.type.terminals.getOrNull(wire.a.index) ?: return
    val sb = b.type.terminals.getOrNull(wire.b.index) ?: return
    val p1 = householdTerminal(a, sa).let { Offset(it.x * size.width, it.y * size.height) }
    val p2 = householdTerminal(b, sb).let { Offset(it.x * size.width, it.y * size.height) }
    val color = when (wire.kind) {
        HouseholdWireKind.PHASE -> Color(0xFFFF6B72)
        HouseholdWireKind.NEUTRAL -> Color(0xFF5AB6FF)
        HouseholdWireKind.PE -> Color(0xFF9BD48B)
        HouseholdWireKind.LOAD -> Color(0xFFFFC857)
    }
    val midY = (p1.y + p2.y) / 2f
    val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p1.x, midY)
        lineTo(p2.x, midY)
        lineTo(p2.x, p2.y)
    }
    drawPath(path, color, style = Stroke(2.5f))
}

private fun DrawScope.drawHouseholdDevice(state: HouseholdState, device: PlacedHouseholdDevice) {
    val center = Offset(device.position.x * size.width, device.position.y * size.height)
    if (device.type == HouseholdDeviceType.BUS_PHASE ||
        device.type == HouseholdDeviceType.BUS_NEUTRAL ||
        device.type == HouseholdDeviceType.BUS_PE
    ) return
    val selected = state.selectedId == device.id
    when (device.type) {
        HouseholdDeviceType.METER -> drawMeter(device, center, selected)
        HouseholdDeviceType.MAIN -> drawMainSwitch(device, center, selected)
        HouseholdDeviceType.LAMP -> drawLamp(device, center, selected, isLampOn(state))
        HouseholdDeviceType.SWITCH -> drawSwitch(device, center, selected)
        HouseholdDeviceType.SOCKET2, HouseholdDeviceType.SOCKET3 -> drawSocket(device, center, selected)
        HouseholdDeviceType.KETTLE, HouseholdDeviceType.RICE_COOKER -> drawAppliance(device, center, selected)
        else -> Unit
    }
    device.type.terminals.forEachIndexed { index, spec ->
        val point = householdTerminal(device, spec).let { Offset(it.x * size.width, it.y * size.height) }
        val color = when (spec.kind) {
            HouseholdWireKind.PHASE -> Color(0xFFFF6B72)
            HouseholdWireKind.NEUTRAL -> Color(0xFF5AB6FF)
            HouseholdWireKind.PE -> Color(0xFF9BD48B)
            HouseholdWireKind.LOAD -> Color(0xFFFFC857)
        }
        drawCircle(color, 4.5f, point)
        if (state.pending == HouseholdTerminalRef(device.id, index)) {
            drawCircle(Color.White, 9f, point, style = Stroke(2f))
        }
    }
}

private fun DrawScope.drawMeter(device: PlacedHouseholdDevice, center: Offset, selected: Boolean) {
    drawRoundRect(Color(0xFF2A3440), Offset(center.x - 41f, center.y - 28f), Size(82f, 56f), CornerRadius(8f))
    drawRoundRect(
        if (selected) Color.White else Color(0xFF6BCBE0),
        Offset(center.x - 41f, center.y - 28f), Size(82f, 56f), CornerRadius(8f),
        style = Stroke(if (selected) 2f else 1.2f),
    )
    householdText("电能表", center + Offset(0f, -6f), Color(0xFFDCE9F6), 10f)
    householdText("kWh", center + Offset(0f, 12f), Color(0xFFFFD35C), 11f)
}

private fun DrawScope.drawMainSwitch(device: PlacedHouseholdDevice, center: Offset, selected: Boolean) {
    drawRoundRect(Color(0xFF24303C), Offset(center.x - 43f, center.y - 32f), Size(86f, 64f), CornerRadius(8f))
    drawRoundRect(
        if (selected) Color.White else Color(0xFF6BCBE0),
        Offset(center.x - 43f, center.y - 32f), Size(86f, 64f), CornerRadius(8f),
        style = Stroke(if (selected) 2f else 1.2f),
    )
    householdText("总开关", center + Offset(0f, -5f), Color(0xFFDCE9F6), 10f)
    drawLine(
        if (device.closed) Color(0xFF7DE0C0) else Color(0xFFFF8A92),
        center + Offset(-18f, 15f), center + Offset(18f, 15f), 12f,
    )
    householdText(if (device.closed) "ON" else "OFF", center + Offset(0f, 18f), Color(0xFFDCE9F6), 7f)
}

private fun DrawScope.drawLamp(device: PlacedHouseholdDevice, center: Offset, selected: Boolean, on: Boolean) {
    if (on) drawCircle(Color(0xFFFFD35C).copy(alpha = 0.25f), 34f, center + Offset(0f, -8f))
    drawCircle(if (on) Color(0xFFFFD35C) else Color(0xFF40505E), 20f, center + Offset(0f, -8f))
    drawCircle(Color(0xFF151A21), 13f, center + Offset(0f, -8f))
    drawLine(Color(0xFF8C7A58), center + Offset(0f, 12f), center + Offset(0f, 24f), 6f)
    drawRoundRect(Color(0xFF8C7A58), center + Offset(-16f, 22f), Size(32f, 8f), CornerRadius(3f))
    if (selected) drawCircle(Color.White, 30f, center, style = Stroke(1.5f))
    householdText("灯泡", center + Offset(0f, 45f), Color(0xFFDCE9F6), 9f)
}

private fun DrawScope.drawSwitch(device: PlacedHouseholdDevice, center: Offset, selected: Boolean) {
    drawRoundRect(Color(0xFF24303C), Offset(center.x - 34f, center.y - 20f), Size(68f, 38f), CornerRadius(7f))
    drawRoundRect(
        if (selected) Color.White else Color(0xFF7FE4C0),
        Offset(center.x - 34f, center.y - 20f), Size(68f, 38f), CornerRadius(7f),
        style = Stroke(if (selected) 2f else 1.2f),
    )
    drawLine(Color(0xFF7FE4C0), center + Offset(-18f, 0f), center + Offset(if (device.closed) 18f else 10f, if (device.closed) 0f else -14f), 3f)
    householdText(if (device.closed) "闭合" else "断开", center + Offset(0f, 28f), Color(0xFFDCE9F6), 8f)
}

private fun DrawScope.drawSocket(device: PlacedHouseholdDevice, center: Offset, selected: Boolean) {
    drawRoundRect(Color(0xFFE7EDF5), Offset(center.x - 32f, center.y - 26f), Size(64f, 58f), CornerRadius(9f))
    drawRoundRect(
        if (selected) Color.White else Color(0xFF8FA3B8),
        Offset(center.x - 32f, center.y - 26f), Size(64f, 58f), CornerRadius(9f),
        style = Stroke(if (selected) 2f else 1f),
    )
    drawCircle(Color(0xFF2C3E50), 4f, center + Offset(-13f, -2f))
    drawCircle(Color(0xFF2C3E50), 4f, center + Offset(13f, -2f))
    if (device.type == HouseholdDeviceType.SOCKET3) {
        drawCircle(Color(0xFF2C3E50), 4f, center + Offset(0f, -18f))
    }
    householdText(if (device.type == HouseholdDeviceType.SOCKET3) "三孔" else "两孔", center + Offset(0f, 45f), Color(0xFFDCE9F6), 9f)
}

private fun DrawScope.drawAppliance(device: PlacedHouseholdDevice, center: Offset, selected: Boolean) {
    drawRoundRect(Color(0xFF31404E), Offset(center.x - 34f, center.y - 24f), Size(68f, 48f), CornerRadius(9f))
    drawRoundRect(
        if (selected) Color.White else Color(0xFF6BCBE0),
        Offset(center.x - 34f, center.y - 24f), Size(68f, 48f), CornerRadius(9f),
        style = Stroke(if (selected) 2f else 1.2f),
    )
    householdText(device.type.title, center + Offset(0f, 5f), Color(0xFFDCE9F6), 9f)
}

private fun DrawScope.drawHouseholdStatus(state: HouseholdState) {
    val live = isLampOn(state) || socketLive(state)
    householdText(
        when {
            state.tripped -> "保护器已跳闸"
            state.fault == HouseholdFault.OPEN_NEUTRAL -> "中性线断路"
            state.fault == HouseholdFault.SWITCH_NEUTRAL -> "开关接中性线，存在安全隐患"
            state.fault != HouseholdFault.NONE -> state.fault.title
            live -> "电路正常"
            else -> "待机"
        },
        Offset(size.width / 2f, size.height - 18f),
        if (state.tripped) Color(0xFFFF8A92) else if (live) Color(0xFF7DE0C0) else Color(0xFF8FA3B8),
        11f,
    )
}

private fun DrawScope.drawPending(state: HouseholdState) {
    val pending = state.pending ?: return
    val device = state.devices.firstOrNull { it.id == pending.deviceId } ?: return
    val spec = device.type.terminals.getOrNull(pending.index) ?: return
    val p = householdTerminal(device, spec).let { Offset(it.x * size.width, it.y * size.height) }
    householdText("选择另一接线柱", p + Offset(0f, -26f), Color.White, 9f)
}

private fun isLampOn(state: HouseholdState): Boolean {
    if (!state.mainClosed || state.tripped || state.fault == HouseholdFault.OPEN_NEUTRAL) return false
    val lamp = state.devices.firstOrNull { it.type == HouseholdDeviceType.LAMP } ?: return false
    val switch = state.devices.firstOrNull { it.type == HouseholdDeviceType.SWITCH } ?: return false
    if (!switch.closed) return false
    val busL = state.devices.firstOrNull { it.type == HouseholdDeviceType.BUS_PHASE } ?: return false
    val busN = state.devices.firstOrNull { it.type == HouseholdDeviceType.BUS_NEUTRAL } ?: return false
    return connected(state, busL, 0, lamp, 0) && connected(state, lamp, 1, busN, 0)
}

private fun socketLive(state: HouseholdState): Boolean {
    if (!state.mainClosed || state.tripped || state.fault == HouseholdFault.OPEN_NEUTRAL) return false
    val socket = state.devices.firstOrNull {
        it.type == HouseholdDeviceType.SOCKET2 || it.type == HouseholdDeviceType.SOCKET3
    } ?: return false
    val busL = state.devices.firstOrNull { it.type == HouseholdDeviceType.BUS_PHASE } ?: return false
    val busN = state.devices.firstOrNull { it.type == HouseholdDeviceType.BUS_NEUTRAL } ?: return false
    return connected(state, busL, 0, socket, 0) && connected(state, busN, 0, socket, 1)
}

private fun connected(
    state: HouseholdState,
    a: PlacedHouseholdDevice,
    at: Int,
    b: PlacedHouseholdDevice,
    bt: Int,
): Boolean {
    val target = HouseholdTerminalRef(b.id, bt)
    val start = HouseholdTerminalRef(a.id, at)
    val queue = ArrayDeque<HouseholdTerminalRef>()
    val seen = mutableSetOf<HouseholdTerminalRef>()
    queue.add(start)
    seen.add(start)
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current == target) return true
        for (wire in state.wires) {
            val next = when {
                wire.a == current -> wire.b
                wire.b == current -> wire.a
                else -> null
            }
            if (next != null && seen.add(next)) queue.add(next)
        }
        val device = state.devices.firstOrNull { it.id == current.deviceId } ?: continue
        if (device.type == HouseholdDeviceType.BUS_PHASE ||
            device.type == HouseholdDeviceType.BUS_NEUTRAL ||
            device.type == HouseholdDeviceType.BUS_PE
        ) {
            for (index in device.type.terminals.indices) {
                val other = HouseholdTerminalRef(device.id, index)
                if (seen.add(other)) queue.add(other)
            }
        }
        if (device.type == HouseholdDeviceType.SWITCH && device.closed) {
            val other = HouseholdTerminalRef(device.id, if (current.index == 0) 1 else 0)
            if (seen.add(other)) queue.add(other)
        }
        if (device.type == HouseholdDeviceType.MAIN && device.closed) {
            val other = HouseholdTerminalRef(device.id, if (current.index % 2 == 0) current.index + 1 else current.index - 1)
            if (seen.add(other)) queue.add(other)
        }
    }
    return false
}

@Composable
private fun HouseholdPanel(state: HouseholdState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(LabColors.SurfaceRaised)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text("仪器库", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text("点击添加，拖动仪器，点击两个接线柱连线", color = LabColors.InkFaint, fontSize = 9.sp)
        for (category in listOf("照明", "插座", "家用电器")) {
            Text(category, color = LabColors.InkMuted, fontSize = 9.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (type in HouseholdDeviceType.entries.filter { it.category == category }) {
                    HouseButton(type.title) { state.addDevice(type) }
                }
            }
        }
        val selected = state.devices.firstOrNull { it.id == state.selectedId }
        if (selected != null) {
            Text("已选：${selected.type.title}", color = LabColors.Ink, fontSize = 10.5.sp)
            if (selected.type == HouseholdDeviceType.SWITCH || selected.type == HouseholdDeviceType.MAIN) {
                HouseButton(if (selected.closed) "断开开关" else "闭合开关") { state.toggleSelectedSwitch() }
            }
            HouseButton("删除选中仪器") { state.deleteSelected() }
        }
        Text("故障模拟", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        for (fault in HouseholdFault.entries) {
            if (fault == HouseholdFault.NONE) continue
            HouseButton(fault.title, primary = state.fault == fault) {
                state.fault = fault
                state.tripped = fault == HouseholdFault.SHORT ||
                    fault == HouseholdFault.OVERLOAD ||
                    fault == HouseholdFault.LEAK
                if (state.tripped) state.mainClosed = false
            }
        }
        HouseButton("排除故障并复位", primary = true) {
            state.fault = HouseholdFault.NONE
            state.tripped = false
            state.mainClosed = true
            state.devices = state.devices.map {
                if (it.type == HouseholdDeviceType.MAIN) it.copy(closed = true) else it
            }
        }
        Text(
            "220V 交流演示：相线经电能表、总开关进入上方母线；\n" +
                "开关接相线，灯泡另一端接中性线；三孔插座上孔接保护线。\n" +
                "当前仅做演示，后续加入完整接线检查和保护器动作。",
            color = LabColors.InkFaint,
            fontSize = 9.5.sp,
            lineHeight = 14.sp,
        )
    }
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
