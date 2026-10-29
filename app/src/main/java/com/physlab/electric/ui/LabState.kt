package com.physlab.electric.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.InstrumentCategory
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.MeterResistanceMode
import com.physlab.electric.model.MultimeterMode
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.Selection
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.isMagnet
import com.physlab.electric.model.isMeter
import com.physlab.electric.model.isRotatable
import com.physlab.electric.model.isPowerSource
import org.json.JSONArray
import org.json.JSONObject

private data class Snapshot(
    val components: List<PlacedInstrument>,
    val wires: List<WireSegment>,
    val nextId: Long,
)

enum class ExperimentTemplate(
    val title: String,
    val subtitle: String,
) {
    SERIES("串联电路", "电池组、开关、小灯泡、电流表"),
    PARALLEL("并联电路", "两只小灯泡并联，干路接电流表"),
    OHM_LAW("伏安法测电阻", "滑动变阻器、电压表、电流表测未知电阻"),
    ELECTROMAGNET("电磁铁与磁场", "电磁铁、螺线管、小磁针"),
    MAGNETS("磁体间相互作用", "两块条形磁体与小磁针"),
    MULTIMETER("多用电表测电阻", "电阻与多用电表，选择合适的电阻档"),
    RC_CHARGE("观察电容器充、放电实验", "电容、电阻、电表等已摆好，请按步骤接线"),    SIMPLE("简单电路", "电池组、开关、小灯泡"),
    SERIES_CURRENT("串联电流规律", "串联电路测量各处电流"),
    SERIES_VOLTAGE("串联电压规律", "串联电路测量各部分电压"),
    PARALLEL_CURRENT("并联电流规律", "并联电路测量各支路电流"),
    PARALLEL_VOLTAGE("并联电压规律", "并联电路测量各支路电压"),
    BULB_POWER("测量小灯泡电功率", "电源、滑动变阻器、灯泡、电表"),
    VOLT_AMMETER_IN("伏安法·电流表内接", "电流表内接测电阻"),
    VOLT_AMMETER_OUT("伏安法·电流表外接", "电流表外接测电阻"),
    DIVIDER("分压接法", "滑动变阻器分压连接"),
    LIMITER("限流接法", "滑动变阻器限流连接"),
    BULB_CURVE("小灯泡伏安特性", "测量灯泡的 U-I 关系"),
    EMF_INTERNAL("测电源电动势和内阻", "改变外电阻，记录 U、I"),
    METER_MOD("电表改装", "灵敏电流计改装为电压表/电流表"),
    HALF_BIAS("半偏法测电阻", "灵敏电流计与电阻箱"),
}

@Stable
class LabEditorState {
    var components by mutableStateOf<List<PlacedInstrument>>(emptyList())
        private set
    var wires by mutableStateOf<List<WireSegment>>(emptyList())
        private set
    var selection by mutableStateOf(Selection())
        private set
    var wireColor by mutableStateOf(WireColor.RED)
        private set
    var wireStart by mutableStateOf<TerminalRef?>(null)
        private set
    var wirePointer by mutableStateOf<Offset?>(null)
        private set
    var wireBends by mutableStateOf<List<Offset>>(emptyList())
        private set
    var wireDraftColor by mutableStateOf<WireColor?>(null)
        private set
    var fadingWireId by mutableStateOf<Long?>(null)
        private set
    var hoveredTerminal by mutableStateOf<TerminalRef?>(null)
    var libraryOpen by mutableStateOf(true)
        private set
    var category by mutableStateOf(InstrumentCategory.CONTROL)
        private set
    var specTargetId by mutableStateOf<Long?>(null)
        private set
    var templateDialogOpen by mutableStateOf(false)
        private set
    var myTemplatesOpen by mutableStateOf(false)
        private set
    var activeTemplate by mutableStateOf<ExperimentTemplate?>(null)
        private set
    var guideOpen by mutableStateOf(false)
        private set
    var topologyEpoch by mutableStateOf(0L)
        private set
    var schematicOpen by mutableStateOf(false)
        private set
    var helpOpen by mutableStateOf(false)
        private set
    var challengeOpen by mutableStateOf(false)
        private set
    var challengeActive by mutableStateOf(false)
        private set
    var challengeIndex by mutableStateOf(0)
        private set
    var challengeInputs by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var challengeDataTokens by mutableStateOf(0)
        private set
    var challengeDataActiveKey by mutableStateOf<String?>(null)
        private set
    var challengeDataEditVersion by mutableStateOf(0)
        private set
    var challengeCapacitorVoltageTokens by mutableStateOf(0)
        private set
    var challengeCapacitorCurrentTokens by mutableStateOf(0)
        private set
    var challengeCapacitorActiveRow by mutableStateOf<Int?>(null)
        private set
    var challengeElapsedMs by mutableStateOf(0L)
        private set
    var challengeTimerRunning by mutableStateOf(false)
        private set
    var challengeTimerPaused by mutableStateOf(false)
        private set
    var challengeConclusionChoices by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var challengeCompleted by mutableStateOf<Set<Int>>(emptySet())
        private set
    var challengeScores by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set
    var rheostatTouchVersion by mutableStateOf(0)
        private set
    var rheostatDragging by mutableStateOf(false)
        private set
    var challengeResult by mutableStateOf<ChallengeResult?>(null)
        private set
    var challengeVisitedWireSamples by mutableStateOf<Set<Int>>(emptySet())
        private set
    var challengeLampSpecChanges by mutableStateOf(0)
        private set
    var challengeRheostatMoved by mutableStateOf(false)
        private set
    var challengeRheostatCurrents by mutableStateOf<Set<Int>>(emptySet())
        private set
    var challengeRheostatPositions by mutableStateOf<Set<Int>>(emptySet())
        private set
    var aboutOpen by mutableStateOf(false)
        private set
    var toast by mutableStateOf<String?>(null)
        private set
    var boardWidth by mutableStateOf(800f)
        private set
    var boardHeight by mutableStateOf(360f)
        private set
    var boardMeasured by mutableStateOf(false)
        private set
    var boardPan by mutableStateOf(Offset.Zero)
        private set
    var boardZoom by mutableStateOf(1f)
        private set
    var meterZoomId by mutableStateOf<Long?>(null)
        private set
    var wirePanelId by mutableStateOf<Long?>(null)
        private set
    var records by mutableStateOf<List<MeasurementRecord>>(emptyList())
        private set

    val worldLeft: Float get() = -boardWidth * 0.20710678f
    val worldTop: Float get() = -boardHeight * 0.56066017f
    val worldRight: Float get() = boardWidth * 1.2071068f
    val worldBottom: Float get() = boardHeight * 1.5606602f

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private val undoStack = ArrayDeque<Snapshot>()
    private val redoStack = ArrayDeque<Snapshot>()
    private var nextId = 1L
    private var seeded = false

    val selectedComponent: PlacedInstrument?
        get() = components.firstOrNull { it.id == selection.componentId }

    val selectedWire: WireSegment?
        get() = wires.firstOrNull { it.id == selection.wireId }

    val specTarget: PlacedInstrument?
        get() = components.firstOrNull { it.id == specTargetId }

    fun setBoardSize(width: Float, height: Float) {
        if (width > 10f && height > 10f) {
            boardWidth = width
            boardHeight = height
            boardMeasured = true
            if (components.isNotEmpty()) {
                val clamped = components.map { it.copy(center = clampCenter(it.type, it.center)) }
                if (clamped != components) components = clamped
            }
        }
    }

    fun updateCategory(value: InstrumentCategory) {
        category = value
    }

    fun updateLibraryOpen(open: Boolean) {
        libraryOpen = open
    }

    fun selectComponent(id: Long?) {
        selection = Selection(componentId = id)
        wireStart = null
        wirePointer = null
        wireBends = emptyList()
    }

    fun selectWire(id: Long?) {
        selection = Selection(wireId = id)
        wireStart = null
        wirePointer = null
        wireBends = emptyList()
    }

    fun clearSelection() {
        selection = Selection()
    }

    fun panBy(delta: Offset) {
        val maxX = boardWidth * 0.20710678f
        val maxY = boardHeight * 0.56066017f
        boardPan = Offset(
            (boardPan.x + delta.x).coerceIn(-maxX, maxX),
            (boardPan.y + delta.y).coerceIn(-maxY, maxY),
        )
    }

    fun resetPan() {
        boardPan = Offset.Zero
        boardZoom = 1f
    }

    fun zoomBy(factor: Float) {
        if (factor.isNaN() || factor <= 0f) return
        boardZoom = (boardZoom * factor).coerceIn(1f / 1.5f, 1.5f)
    }

    fun openMeterZoom(id: Long) {
        val component = components.firstOrNull { it.id == id } ?: return
        if (!component.type.isMeter || component.type == InstrumentType.MULTIMETER) return
        meterZoomId = id
        wirePanelId = null
        selection = Selection(componentId = id)
    }

    fun closeMeterZoom() {
        meterZoomId = null
    }

    fun openWirePanel(id: Long) {
        if (wires.none { it.id == id }) return
        wirePanelId = id
        meterZoomId = null
        selectWire(id)
    }

    fun closeWirePanel() {
        wirePanelId = null
    }

    fun addRecord(record: MeasurementRecord) {
        val last = records.lastOrNull()
        fun sameValues(a: List<Float>, b: List<Float>): Boolean =
            a.size == b.size && a.indices.all { kotlin.math.abs(a[it] - b[it]) < 0.001f }
        if (last != null &&
            kotlin.math.abs(last.sourceVoltage - record.sourceVoltage) < 0.001f &&
            kotlin.math.abs(last.totalCurrent - record.totalCurrent) < 0.0005f &&
            sameValues(last.voltmeterValues, record.voltmeterValues) &&
            sameValues(last.ammeterValues, record.ammeterValues)
        ) {
            return
        }
        records = (records + record).takeLast(100)
    }

    fun clearRecords() {
        records = emptyList()
    }

    fun setRotation(id: Long, degrees: Float) {
        val component = components.firstOrNull { it.id == id } ?: return
        if (!component.type.isRotatable) return
        pushHistory()
        updateRotationLive(id, degrees)
        toast = "${component.type.displayName}已旋转 ${normalizedRotation(degrees).toInt()}°"
    }

    fun updateRotationLive(id: Long, degrees: Float) {
        val value = normalizedRotation(degrees)
        components = components.map {
            if (it.id == id && it.type.isRotatable) it.copy(rotation = value) else it
        }
    }

    fun chooseWireColor(color: WireColor) {
        wireColor = color
        selection = Selection()
        cancelWire()
        toast = "已选择${color.displayName}导线"
        libraryOpen = false
    }

    fun beginWire(ref: TerminalRef, pointer: Offset? = null) {
        selection = Selection()
        wireStart = ref
        wirePointer = pointer
        wireBends = emptyList()
        wireDraftColor = null
        fadingWireId = null
    }

    fun beginWireRewire(
        wireId: Long,
        fixed: TerminalRef,
        color: WireColor,
        pointer: Offset? = null,
    ) {
        beginWire(fixed, pointer)
        fadingWireId = wireId
        wireDraftColor = color
    }

    fun updateWirePointer(pointer: Offset?) {
        wirePointer = pointer
    }

    fun addWireBend(point: Offset) {
        if (wireStart == null) return
        wireBends = wireBends + point
        wirePointer = point
    }

    fun updateHoveredTerminal(ref: TerminalRef?) {
        hoveredTerminal = ref
    }

    fun cancelWire() {
        wireStart = null
        wirePointer = null
        wireBends = emptyList()
        wireDraftColor = null
        fadingWireId = null
        hoveredTerminal = null
    }

    fun rewireWireEndpoint(
        wireId: Long,
        endpointA: Boolean,
        newRef: TerminalRef,
    ): Boolean {
        val wire = wires.firstOrNull { it.id == wireId } ?: return false
        val fixed = if (endpointA) wire.b else wire.a
        val original = if (endpointA) wire.a else wire.b
        if (newRef == fixed || newRef == original) {
            cancelWire()
            return false
        }
        pushHistory()
        wires = wires.map {
            if (it.id != wireId) {
                it
            } else {
                val updated = if (endpointA) it.copy(a = newRef) else it.copy(b = newRef)
                updated.copy(bends = emptyList())
            }
        }
        selection = Selection(wireId = wireId)
        cancelWire()
        val updated = wires.firstOrNull { it.id == wireId }
        toast = if (updated != null && wireCrossesExisting(updated)) {
            "导线交叉了，请重新调整走线，避免导线交叉"
        } else {
            "导线已改接到新的接线柱"
        }
        return true
    }

    fun handleTerminalTap(ref: TerminalRef) {        val start = wireStart
        if (start == null) {
            beginWire(ref)
            return
        }
        if (start == ref) {
            cancelWire()
            return
        }
        addWire(start, ref, wireColor, wireBends)
        cancelWire()
    }

    fun completeWireAt(ref: TerminalRef?) {
        val start = wireStart
        if (start != null && ref != null && start != ref) {
            addWire(start, ref, wireColor, wireBends)
        }
        cancelWire()
    }

    fun addWire(
        a: TerminalRef,
        b: TerminalRef,
        color: WireColor,
        bends: List<Offset> = emptyList(),
    ) {
        val byId = components.associateBy { it.id }
        val ca = byId[a.componentId] ?: return
        val cb = byId[b.componentId] ?: return
        if (a == b) return
        if (a.index !in ca.type.terminalOffsets.indices) return
        if (b.index !in cb.type.terminalOffsets.indices) return
        val duplicate = wires.any {
            (it.a == a && it.b == b) || (it.a == b && it.b == a)
        }
        if (duplicate) {
            toast = "这两个接线柱已经连接"
            return
        }
        pushHistory()
        val newWire = WireSegment(nextId++, a, b, color, bends)
        wires = wires + newWire
        selection = Selection(wireId = newWire.id)
        toast = if (wireCrossesExisting(newWire)) {
            "导线交叉了，请重新调整走线，避免导线交叉"
        } else {
            null
        }
    }

    fun warnIfWirePreviewCrosses(point: Offset) {
        val start = wireStart ?: return
        val byId = components.associateBy { it.id }
        val a = byId[start.componentId]?.terminalPosition(start.index) ?: return
        val candidatePoints = listOf(a) + wireBends + point
        for (wire in wires) {
            if (wire.id == fadingWireId) continue
            val points = wirePathPoints(wire, byId) ?: continue
            for (i in 0 until candidatePoints.size - 1) {
                for (j in 0 until points.size - 1) {
                    if (segmentsCrossStrict(
                            candidatePoints[i],
                            candidatePoints[i + 1],
                            points[j],
                            points[j + 1],
                        )
                    ) {
                        val warning = "导线交叉了，请重新调整走线，避免导线交叉"
                        if (toast != warning) toast = warning
                        return
                    }
                }
            }
        }
    }

    private fun wireCrossesExisting(candidate: WireSegment): Boolean {        val byId = components.associateBy { it.id }
        val candidatePoints = wirePathPoints(candidate, byId) ?: return false
        for (wire in wires) {
            if (wire.id == candidate.id) continue
            val points = wirePathPoints(wire, byId) ?: continue
            for (i in 0 until candidatePoints.size - 1) {
                for (j in 0 until points.size - 1) {
                    if (segmentsCrossStrict(
                            candidatePoints[i],
                            candidatePoints[i + 1],
                            points[j],
                            points[j + 1],
                        )
                    ) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun wirePathPoints(
        wire: WireSegment,
        byId: Map<Long, PlacedInstrument>,
    ): List<Offset>? {
        val a = byId[wire.a.componentId]?.terminalPosition(wire.a.index) ?: return null
        val b = byId[wire.b.componentId]?.terminalPosition(wire.b.index) ?: return null
        return listOf(a) + wire.bends + b
    }

    private fun segmentsCrossStrict(a: Offset, b: Offset, c: Offset, d: Offset): Boolean {
        val r = b - a
        val s = d - c
        val denom = r.x * s.y - r.y * s.x
        if (kotlin.math.abs(denom) < 1e-4f) return false
        val q = c - a
        val t = (q.x * s.y - q.y * s.x) / denom
        val u = (q.x * r.y - q.y * r.x) / denom
        return t > 0.06f && t < 0.94f && u > 0.06f && u < 0.94f
    }

    fun setSelectionWireColor(color: WireColor) {
        val id = selection.wireId ?: return
        if (wires.none { it.id == id }) return
        pushHistory()
        wires = wires.map { if (it.id == id) it.copy(color = color) else it }
        wireColor = color
    }

    fun addBendToSelectedWire() {
        val wire = selectedWire ?: return
        val byId = components.associateBy { it.id }
        val a = byId[wire.a.componentId]?.terminalPosition(wire.a.index) ?: return
        val b = byId[wire.b.componentId]?.terminalPosition(wire.b.index) ?: return
        val points = buildList {
            add(a)
            addAll(wire.bends)
            add(b)
        }
        var bestIndex = 0
        var bestLength = -1f
        for (i in 0 until points.size - 1) {
            val length = (points[i + 1] - points[i]).getDistance()
            if (length > bestLength) {
                bestLength = length
                bestIndex = i
            }
        }
        val mid = (points[bestIndex] + points[bestIndex + 1]) / 2f
        pushHistory()
        wires = wires.map {
            if (it.id == wire.id) it.copy(bends = it.bends + mid) else it
        }
        toast = "已添加一个弯折点，可直接拖动它"
    }

    /**
     * Starts an arbitrary-point wire deformation. If [point] is close to an
     * existing bend it returns that bend; otherwise it inserts a new bend at
     * the nearest segment of the current wire.
     */
    fun beginWireDeform(wireId: Long, point: Offset): Int? {
        val wire = wires.firstOrNull { it.id == wireId } ?: return null
        val byId = components.associateBy { it.id }
        val a = byId[wire.a.componentId]?.terminalPosition(wire.a.index) ?: return null
        val b = byId[wire.b.componentId]?.terminalPosition(wire.b.index) ?: return null
        val existingIndex = wire.bends.indexOfFirst { (it - point).getDistance() <= 16f }
        if (existingIndex >= 0) {
            pushHistory()
            selectWire(wireId)
            return existingIndex
        }
        val points = buildList {
            add(a)
            addAll(wire.bends)
            add(b)
        }
        var bestIndex = 0
        var bestDistance = Float.MAX_VALUE
        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val segment = end - start
            val lengthSquared = segment.x * segment.x + segment.y * segment.y
            val t = if (lengthSquared <= 0.001f) {
                0f
            } else {
                (((point.x - start.x) * segment.x + (point.y - start.y) * segment.y) / lengthSquared)
                    .coerceIn(0f, 1f)
            }
            val projection = start + segment * t
            val distance = (point - projection).getDistance()
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = i
            }
        }
        pushHistory()
        wires = wires.map {
            if (it.id != wireId) {
                it
            } else {
                val bends = it.bends.toMutableList()
                bends.add(bestIndex.coerceIn(0, bends.size), point)
                it.copy(bends = bends)
            }
        }
        selectWire(wireId)
        return bestIndex.coerceIn(0, wire.bends.size)
    }

    fun clearSelectedWireBends() {
        val wire = selectedWire ?: return
        if (wire.bends.isEmpty()) return
        pushHistory()
        wires = wires.map { if (it.id == wire.id) it.copy(bends = emptyList()) else it }
        toast = "导线已拉直"
    }

    fun updateWireBend(wireId: Long, index: Int, point: Offset) {
        wires = wires.map { wire ->
            if (wire.id != wireId || index !in wire.bends.indices) {
                wire
            } else {
                wire.copy(bends = wire.bends.toMutableList().also { it[index] = point })
            }
        }
    }

    fun pushWireEditHistory() {
        pushHistory()
    }

    fun deleteSelection() {
        val componentId = selection.componentId
        val wireId = selection.wireId
        if (componentId == null && wireId == null) return
        pushHistory()
        if (componentId != null) {
            components = components.filterNot { it.id == componentId }
            wires = wires.filterNot {
                it.a.componentId == componentId || it.b.componentId == componentId
            }
            toast = "已移除仪器及相连导线"
        } else if (wireId != null) {
            wires = wires.filterNot { it.id == wireId }
            toast = "已删除导线"
        }
        selection = Selection()
        if (wireStart?.componentId == componentId) cancelWire()
    }

    fun deleteWire(id: Long) {
        if (wires.none { it.id == id }) return
        pushHistory()
        wires = wires.filterNot { it.id == id }
        selection = Selection()
        wirePanelId = null
        cancelWire()
        toast = "导线已删除"
    }

    fun clearAll() {
        if (components.isEmpty() && wires.isEmpty()) return
        pushHistory()
        topologyEpoch += 1
        components = emptyList()
        wires = emptyList()
        selection = Selection()
        cancelWire()
        boardPan = Offset.Zero
        boardZoom = 1f
        meterZoomId = null
        wirePanelId = null
        toast = "实验台已清空"
    }

    fun addInstrument(type: InstrumentType, center: Offset) {
        pushHistory()
        val padded = clampCenter(type, center)
        val label = nextLabel(type)
        val instrument = PlacedInstrument(
            id = nextId++,
            type = type,
            center = padded,
            label = label,
            closed = false,
            batteryCells = 2,
            lampSpec = LampSpec.V2_5_0_3,
            resistance = 10f,
            slider = 0.5f,
            rheostatMax = 20f,
            supplyVoltage = 12f,
            supplyAc = false,
            storageVoltage = 6f,
            resistanceBoxValue = 100,
            wireSampleIndex = 0,
            polarityFlipped = false,
            damaged = false,
        )
        components = components + instrument
        selection = Selection()
        cancelWire()
        toast = "已放置${type.displayName}"
    }

    fun addInstrumentAtCenter(type: InstrumentType) {
        val safeLeft = if (libraryOpen) 226f else 24f
        val visibleCenterX = boardWidth / 2f - boardPan.x / boardZoom
        val visibleCenterY = boardHeight / 2f - boardPan.y / boardZoom
        val x = visibleCenterX.coerceAtLeast(safeLeft - boardPan.x / boardZoom + 80f)
        val y = visibleCenterY
        addInstrument(type, Offset(x, y))
    }

    fun moveComponent(id: Long, center: Offset) {
        val component = components.firstOrNull { it.id == id } ?: return
        val padded = clampCenter(component.type, center)
        components = components.map { if (it.id == id) it.copy(center = padded) else it }
    }

    fun pushHistoryForMove() {
        pushHistory()
    }

    fun toggleSwitch(id: Long) {
        val component = components.firstOrNull { it.id == id && it.type == InstrumentType.SWITCH } ?: return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(closed = !it.closed) else it
        }
        toast = if (!component.closed) "开关已闭合" else "开关已断开"
    }

    fun toggleSelectedSwitch() {
        val id = selection.componentId ?: return
        toggleSwitch(id)
    }

    fun setBatteryCells(id: Long, cells: Int) {
        if (components.none { it.id == id && it.type == InstrumentType.BATTERY }) return
        if (components.any { it.id == id && it.specLocked }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(batteryCells = cells.coerceIn(1, 4)) else it
        }
    }

    fun setLampSpec(id: Long, spec: LampSpec) {
        if (components.none {
                it.id == id && (it.type == InstrumentType.LAMP || it.type == InstrumentType.TEMP_LAMP)
            }
        ) return
        val oldSpec = components.firstOrNull { it.id == id }?.lampSpec
        if (challengeActive && oldSpec != null && oldSpec != spec) {
            challengeLampSpecChanges += 1
        }
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(lampSpec = spec) else it
        }
    }

    fun beginRheostatDrag() {
        rheostatDragging = true
    }

    fun endRheostatDrag() {
        rheostatDragging = false
    }

    fun setSlider(id: Long, slider: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.RHEOSTAT }) return
        pushHistory()
        rheostatTouchVersion += 1
        challengeRheostatMoved = true
        challengeRheostatPositions = challengeRheostatPositions +
            (slider.coerceIn(0f, 1f) * 10f).toInt()
        components = components.map {
            if (it.id == id) it.copy(slider = slider.coerceIn(0f, 1f)) else it
        }
    }

    fun setResistance(id: Long, resistance: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.RESISTOR }) return
        if (components.any { it.id == id && it.resistanceHidden }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(resistance = resistance.coerceIn(1f, 10_000f)) else it
        }
    }

    fun setLightPercent(id: Long, percent: Float) {
        components = components.map {
            if (it.id == id && it.type == InstrumentType.PHOTORESISTOR) {
                it.copy(lightPercent = percent.coerceIn(0f, 100f))
            } else {
                it
            }
        }
    }

    fun setTemperatureC(id: Long, temperature: Float) {
        components = components.map {
            if (it.id == id && it.type == InstrumentType.THERMISTOR) {
                it.copy(temperatureC = temperature.coerceIn(-20f, 120f))
            } else {
                it
            }
        }
    }

    fun setResistanceHidden(id: Long, hidden: Boolean) {
        components = components.map {
            if (it.id == id && it.type == InstrumentType.RESISTOR) {
                it.copy(resistanceHidden = hidden)
            } else {
                it
            }
        }
    }

    fun setRheostatMax(id: Long, max: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.RHEOSTAT }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(rheostatMax = max.coerceIn(1f, 100f)) else it
        }
        toast = "滑动变阻器最大阻值 ${max.toInt()} Ω"
    }

    fun setSupplyVoltage(id: Long, voltage: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.POWER_SUPPLY }) return
        if (components.firstOrNull { it.id == id }?.specLocked == true) return
        components = components.map {
            if (it.id == id) it.copy(supplyVoltage = voltage.coerceIn(0f, 36f)) else it
        }
    }

    fun setSupplyAc(id: Long, ac: Boolean) {
        if (components.none { it.id == id && it.type == InstrumentType.POWER_SUPPLY }) return
        if (components.firstOrNull { it.id == id }?.specLocked == true) return
        components = components.map {
            if (it.id == id) it.copy(supplyAc = ac) else it
        }
    }

    fun setInternalResistanceEnabled(id: Long, enabled: Boolean) {
        if (components.none { it.id == id && it.type.isPowerSource }) return
        if (components.firstOrNull { it.id == id }?.specLocked == true) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(internalResistanceEnabled = enabled) else it
        }
    }

    fun setInternalResistance(id: Long, ohms: Float) {
        if (components.none { it.id == id && it.type.isPowerSource }) return
        if (components.firstOrNull { it.id == id }?.specLocked == true) return
        components = components.map {
            if (it.id == id) it.copy(internalResistanceOhm = ohms.coerceIn(0.1f, 1f)) else it
        }
    }

    fun setMeterResistanceEnabled(id: Long, enabled: Boolean) {
        if (components.none { it.id == id && it.type.isMeter }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(meterResistanceEnabled = enabled) else it
        }
    }

    fun setMeterResistanceMode(id: Long, mode: MeterResistanceMode) {
        if (components.none { it.id == id && it.type.isMeter }) return
        components = components.map {
            if (it.id == id) it.copy(meterResistanceMode = mode) else it
        }
    }

    fun setCapacitance(id: Long, uf: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.CAPACITOR }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(capacitanceUf = uf) else it
        }
    }

    fun setInductance(id: Long, mh: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.INDUCTOR }) return
        pushHistory()
        val dc = when (mh.toInt()) {
            10 -> 1f
            50 -> 2f
            100 -> 4f
            else -> 2f
        }
        components = components.map {
            if (it.id == id) it.copy(inductanceMh = mh, inductanceDcResistance = dc) else it
        }
    }

    fun setSwitchPosition(id: Long, position: Int) {
        if (components.none { it.id == id && it.type == InstrumentType.SPDT_SWITCH }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(switchPosition = position.coerceIn(0, 2)) else it
        }
    }

    fun setStorageVoltage(id: Long, voltage: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.STORAGE_BATTERY }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(storageVoltage = voltage) else it
        }
    }

    fun setResistanceBoxValue(id: Long, value: Int) {
        if (components.none { it.id == id && it.type == InstrumentType.RESISTANCE_BOX }) return
        components = components.map {
            if (it.id == id) it.copy(resistanceBoxValue = value.coerceIn(0, 9999)) else it
        }
    }

    fun setWireSample(id: Long, sampleIndex: Int) {
        if (components.none { it.id == id && it.type == InstrumentType.RESISTANCE_WIRE }) return
        pushHistory()
        challengeVisitedWireSamples = challengeVisitedWireSamples + sampleIndex.coerceIn(0, 3)
        components = components.map {
            if (it.id == id) it.copy(wireSampleIndex = sampleIndex.coerceIn(0, 3)) else it
        }
    }

    fun setWireResistanceVisible(id: Long, visible: Boolean) {
        if (components.none { it.id == id && it.type == InstrumentType.RESISTANCE_WIRE }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(showWireResistance = visible) else it
        }
    }

    fun setPolarityFlipped(id: Long, flipped: Boolean) {
        if (components.none { it.id == id }) return
        if (components.firstOrNull { it.id == id }?.specLocked == true) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(polarityFlipped = flipped) else it
        }
    }

    fun setMeterFlipped(id: Long, flipped: Boolean) {
        if (components.none { it.id == id && it.type.isMeter }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(meterFlipped = flipped) else it
        }
    }

    fun setMultimeterMode(id: Long, mode: MultimeterMode) {
        if (components.none { it.id == id && it.type == InstrumentType.MULTIMETER }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(multimeterMode = mode, multimeterRangeIndex = 0) else it
        }
    }

    fun setMultimeterRange(id: Long, index: Int) {
        if (components.none { it.id == id && it.type == InstrumentType.MULTIMETER }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(multimeterRangeIndex = index.coerceIn(0, 4)) else it
        }
    }

    fun setMultimeterZeroOffset(id: Long, offset: Float) {
        if (components.none { it.id == id && it.type == InstrumentType.MULTIMETER }) return
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(multimeterZeroOffset = offset) else it
        }
    }

    fun setWindingReversed(id: Long, reversed: Boolean) {
        if (components.none {
                it.id == id && (it.type == InstrumentType.SOLENOID || it.type == InstrumentType.ELECTROMAGNET)
            }
        ) {
            return
        }
        pushHistory()
        components = components.map {
            if (it.id == id) it.copy(windingReversed = reversed) else it
        }
    }

    fun updateSliderLive(id: Long, slider: Float) {
        val target = components.firstOrNull { it.id == id && it.type == InstrumentType.RHEOSTAT } ?: return
        rheostatTouchVersion += 1
        challengeRheostatMoved = true
        challengeRheostatPositions = challengeRheostatPositions +
            (slider.coerceIn(0f, 1f) * 10f).toInt()
        components = components.map {
            if (it.id == id && it.type == InstrumentType.RHEOSTAT) {
                it.copy(slider = slider.coerceIn(0f, 1f))
            } else {
                it
            }
        }
    }

    fun openSpec(id: Long) {
        if (components.any { it.id == id }) {
            specTargetId = id
            selection = Selection(componentId = id)
            meterZoomId = null
            wirePanelId = null
        }
    }

    fun closeSpec() {
        specTargetId = null
    }

    fun openGuide() { guideOpen = true }

    fun closeGuide() { guideOpen = false }

    fun updateTemplateDialog(open: Boolean) {
        if (open && challengeActive) {
            toast = "任务闯关模式禁止使用实验模板"
            return
        }
        templateDialogOpen = open
    }

    fun openMyTemplates() {
        if (challengeActive) {
            toast = "任务闯关模式禁止使用实验模板"
            return
        }
        myTemplatesOpen = true
    }

    fun closeMyTemplates() {
        myTemplatesOpen = false
    }

    fun applyDamage(
        ids: Set<Long>,
        reversedMeterIds: Set<Long> = emptySet(),
        overRangeMeterIds: Set<Long> = emptySet(),
        negativeOverRangeMeterIds: Set<Long> = emptySet(),
    ) {
        if (ids.isEmpty()) return
        val hurt = components.filter { it.id in ids && !it.damaged }
        if (hurt.isEmpty()) return
        pushHistory()
        components = components.map {
            if (it.id in ids && !it.damaged) it.copy(damaged = true) else it
        }
        val sourceHurt = hurt.any { it.type.isPowerSource }
        val reversedHurt = hurt.filter { it.id in reversedMeterIds }
        val overRangeHurt = hurt.filter { it.id in overRangeMeterIds }
        val negativeOverRangeHurt = hurt.filter { it.id in negativeOverRangeMeterIds }
        toast = when {
            negativeOverRangeHurt.isNotEmpty() -> {
                val names = negativeOverRangeHurt.map { it.type.displayName }.distinct().joinToString("、")
                "${names}指针超出负刻度线，已损坏！请检查接线后点击“修复”"
            }
            reversedHurt.isNotEmpty() -> {
                val names = reversedHurt.map { it.type.displayName }.distinct().joinToString("、")
                "${names}正负接线柱接反，已损坏！请对调接线后点击“修复”"
            }
            overRangeHurt.isNotEmpty() -> {
                val names = overRangeHurt.map { it.type.displayName }.distinct().joinToString("、")
                "${names}超出测量范围，已损坏！请更换大测量范围后点击“修复”"
            }
            sourceHurt -> "电源被短路，已损坏！点击仪器上方的“修复”按钮"
            hurt.any { it.type == InstrumentType.LAMP || it.type == InstrumentType.TEMP_LAMP } ->
                "小灯泡超过额定功率 3 倍，已烧坏！"
            hurt.any { it.type == InstrumentType.MOTOR } -> "电动机过载损坏！"
            hurt.any { it.type == InstrumentType.BUZZER } -> "蜂鸣器过载损坏！"
            hurt.any { it.type == InstrumentType.LED } -> "发光二极管过载损坏！"
            else -> "仪器损坏，请点击“修复”"
        }
    }

    fun repairComponent(id: Long) {
        val component = components.firstOrNull { it.id == id } ?: return
        if (!component.damaged) return
        pushHistory()
        topologyEpoch += 1
        components = components.map {
            if (it.id == id) it.copy(damaged = false) else it
        }
        toast = "${component.type.displayName}已修复"
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(currentSnapshot())
        restore(previous)
        refreshHistoryFlags()
        toast = "已撤销"
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(currentSnapshot())
        restore(next)
        refreshHistoryFlags()
        toast = "已重做"
    }

    fun exportSnapshot(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("nextId", nextId)
        root.put("panX", boardPan.x)
        root.put("panY", boardPan.y)
        val componentArray = JSONArray()
        for (component in components) {
            componentArray.put(
                JSONObject().apply {
                    put("id", component.id)
                    put("type", component.type.name)
                    put("x", component.center.x)
                    put("y", component.center.y)
                    put("label", component.label)
                    put("rotation", component.rotation)
                    put("closed", component.closed)
                    put("batteryCells", component.batteryCells)
                    put("lampSpec", component.lampSpec.name)
                    put("resistance", component.resistance)
                    put("resistanceHidden", component.resistanceHidden)
                    put("lightPercent", component.lightPercent)
                    put("temperatureC", component.temperatureC)
                    put("slider", component.slider)
                    put("rheostatMax", component.rheostatMax)
                    put("supplyVoltage", component.supplyVoltage)
                    put("supplyAc", component.supplyAc)
                    put("internalResistanceEnabled", component.internalResistanceEnabled)
                    put("internalResistanceOhm", component.internalResistanceOhm)
                    put("internalResistanceHidden", component.internalResistanceHidden)
                    put("specLocked", component.specLocked)
                    put("meterResistanceEnabled", component.meterResistanceEnabled)
                    put("meterResistanceMode", component.meterResistanceMode.name)
                    put("multimeterMode", component.multimeterMode.name)
                    put("multimeterRangeIndex", component.multimeterRangeIndex)
                    put("multimeterZeroOffset", component.multimeterZeroOffset)
                    put("capacitanceUf", component.capacitanceUf)
                    put("inductanceMh", component.inductanceMh)
                    put("inductanceDcResistance", component.inductanceDcResistance)
                    put("switchPosition", component.switchPosition)
                    put("storageVoltage", component.storageVoltage)
                    put("resistanceBoxValue", component.resistanceBoxValue)
                    put("wireSample", component.wireSampleIndex)
                    put("showWireResistance", component.showWireResistance)
                    put("polarityFlipped", component.polarityFlipped)
                    put("meterFlipped", component.meterFlipped)
                    put("windingReversed", component.windingReversed)
                    put("damaged", component.damaged)
                },
            )
        }
        root.put("components", componentArray)
        val wireArray = JSONArray()
        for (wire in wires) {
            val bends = JSONArray()
            for (bend in wire.bends) {
                bends.put(JSONObject().apply { put("x", bend.x); put("y", bend.y) })
            }
            wireArray.put(
                JSONObject().apply {
                    put("id", wire.id)
                    put("aId", wire.a.componentId)
                    put("aIndex", wire.a.index)
                    put("bId", wire.b.componentId)
                    put("bIndex", wire.b.index)
                    put("color", wire.color.name)
                    put("bends", bends)
                },
            )
        }
        root.put("wires", wireArray)
        val recordArray = JSONArray()
        for (record in records) {
            recordArray.put(
                JSONObject().apply {
                    put("createdAt", record.createdAt)
                    put("sourceVoltage", record.sourceVoltage)
                    put("totalCurrent", record.totalCurrent)
                    put("voltmeters", JSONArray(record.voltmeterValues))
                    put("ammeters", JSONArray(record.ammeterValues))
                },
            )
        }
        root.put("records", recordArray)
        return root.toString()
    }

    fun loadSnapshot(json: String): Boolean {
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return false
        val componentArray = root.optJSONArray("components") ?: return false
        val loadedComponents = mutableListOf<PlacedInstrument>()
        for (index in 0 until componentArray.length()) {
            val item = componentArray.optJSONObject(index) ?: continue
            val type = runCatching {
                InstrumentType.valueOf(item.optString("type"))
            }.getOrNull() ?: continue
            val id = item.optLong("id", -1L)
            if (id <= 0L) continue
            loadedComponents += PlacedInstrument(
                id = id,
                type = type,
                center = Offset(
                    item.optDouble("x", 0.0).toFloat(),
                    item.optDouble("y", 0.0).toFloat(),
                ),
                label = item.optString("label", type.defaultLabel),
                rotation = item.optDouble("rotation", 0.0).toFloat(),
                closed = item.optBoolean("closed", false),
                batteryCells = item.optInt("batteryCells", 2).coerceIn(1, 4),
                lampSpec = runCatching {
                    LampSpec.valueOf(item.optString("lampSpec", LampSpec.V2_5_0_3.name))
                }.getOrDefault(LampSpec.V2_5_0_3),
                resistance = item.optDouble("resistance", 10.0).toFloat(),
                resistanceHidden = item.optBoolean("resistanceHidden", false),
                lightPercent = item.optDouble("lightPercent", 50.0).toFloat().coerceIn(0f, 100f),
                temperatureC = item.optDouble("temperatureC", 25.0).toFloat().coerceIn(-20f, 120f),
                slider = item.optDouble("slider", 0.5).toFloat().coerceIn(0f, 1f),
                rheostatMax = item.optDouble("rheostatMax", 20.0).toFloat().coerceIn(1f, 100f),
                supplyVoltage = item.optDouble("supplyVoltage", 12.0).toFloat().coerceIn(0f, 36f),
                supplyAc = item.optBoolean("supplyAc", false),
                internalResistanceEnabled = item.optBoolean("internalResistanceEnabled", false),
                internalResistanceOhm = item.optDouble("internalResistanceOhm", 0.1).toFloat().coerceIn(0.1f, 1f),
                internalResistanceHidden = item.optBoolean("internalResistanceHidden", false),
                specLocked = item.optBoolean("specLocked", false),
                meterResistanceEnabled = item.optBoolean("meterResistanceEnabled", false),
                meterResistanceMode = runCatching {
                    MeterResistanceMode.valueOf(item.optString("meterResistanceMode", MeterResistanceMode.ANALOG.name))
                }.getOrDefault(MeterResistanceMode.ANALOG),
                multimeterMode = runCatching {
                    MultimeterMode.valueOf(item.optString("multimeterMode", MultimeterMode.DC_VOLTAGE.name))
                }.getOrDefault(MultimeterMode.DC_VOLTAGE),
                multimeterRangeIndex = item.optInt("multimeterRangeIndex", 1).coerceIn(0, 4),
                multimeterZeroOffset = item.optDouble("multimeterZeroOffset", 0.0).toFloat(),
                capacitanceUf = item.optDouble("capacitanceUf", 470.0).toFloat(),
                inductanceMh = item.optDouble("inductanceMh", 50.0).toFloat(),
                inductanceDcResistance = item.optDouble("inductanceDcResistance", 2.0).toFloat(),
                switchPosition = item.optInt("switchPosition", 0).coerceIn(0, 2),
                storageVoltage = item.optDouble("storageVoltage", 6.0).toFloat(),
                resistanceBoxValue = item.optInt("resistanceBoxValue", 100).coerceIn(0, 9999),
                wireSampleIndex = item.optInt("wireSample", 0).coerceIn(0, 3),
                showWireResistance = item.optBoolean("showWireResistance", false),
                polarityFlipped = item.optBoolean("polarityFlipped", false),
                meterFlipped = item.optBoolean("meterFlipped", false),
                windingReversed = item.optBoolean("windingReversed", false),
                damaged = item.optBoolean("damaged", false),
            )
        }
        if (componentArray.length() > 0 && loadedComponents.isEmpty()) return false
        val loadedIds = loadedComponents.map { it.id }.toSet()
        val loadedWires = mutableListOf<WireSegment>()
        val wireArray = root.optJSONArray("wires") ?: JSONArray()
        for (index in 0 until wireArray.length()) {
            val item = wireArray.optJSONObject(index) ?: continue
            val aId = item.optLong("aId", -1L)
            val bId = item.optLong("bId", -1L)
            if (aId !in loadedIds || bId !in loadedIds) continue
            val aComponent = loadedComponents.firstOrNull { it.id == aId } ?: continue
            val bComponent = loadedComponents.firstOrNull { it.id == bId } ?: continue
            val aIndex = item.optInt("aIndex", 0)
            val bIndex = item.optInt("bIndex", 0)
            if (aIndex !in aComponent.type.terminalOffsets.indices ||
                bIndex !in bComponent.type.terminalOffsets.indices
            ) {
                continue
            }
            val color = runCatching {
                WireColor.valueOf(item.optString("color", WireColor.BLACK.name))
            }.getOrDefault(WireColor.BLACK)
            val bends = mutableListOf<Offset>()
            val bendsArray = item.optJSONArray("bends") ?: JSONArray()
            for (bendIndex in 0 until bendsArray.length()) {
                val bend = bendsArray.optJSONObject(bendIndex) ?: continue
                bends += Offset(
                    bend.optDouble("x", 0.0).toFloat(),
                    bend.optDouble("y", 0.0).toFloat(),
                )
            }
            loadedWires += WireSegment(
                id = item.optLong("id", 0L).takeIf { it > 0L } ?: nextId++,
                a = TerminalRef(aId, aIndex),
                b = TerminalRef(bId, bIndex),
                color = color,
                bends = bends,
            )
        }
        val loadedRecords = mutableListOf<MeasurementRecord>()
        val recordArray = root.optJSONArray("records") ?: JSONArray()
        fun readMeterList(item: JSONObject, name: String, oldName: String): List<Float> {
            val array = item.optJSONArray(name)
            if (array != null) {
                return (0 until array.length()).map { array.optDouble(it, 0.0).toFloat() }
            }
            if (!item.isNull(oldName)) {
                return listOf(item.optDouble(oldName, 0.0).toFloat())
            }
            return emptyList()
        }
        for (index in 0 until recordArray.length()) {
            val item = recordArray.optJSONObject(index) ?: continue
            loadedRecords += MeasurementRecord(
                createdAt = item.optLong("createdAt", 0L),
                sourceVoltage = item.optDouble("sourceVoltage", 0.0).toFloat(),
                totalCurrent = item.optDouble("totalCurrent", 0.0).toFloat(),
                voltmeterValues = readMeterList(item, "voltmeters", "voltmeter"),
                ammeterValues = readMeterList(item, "ammeters", "ammeter"),
            )
        }
        pushHistory()
        components = loadedComponents
        wires = loadedWires
        records = loadedRecords
        nextId = maxOf(
            root.optLong("nextId", 1L),
            loadedComponents.maxOfOrNull { it.id + 1L } ?: 1L,
            loadedWires.maxOfOrNull { it.id + 1L } ?: 1L,
        )
        selection = Selection()
        cancelWire()
        specTargetId = null
        meterZoomId = null
        wirePanelId = null
        boardPan = Offset(
            root.optDouble("panX", 0.0).toFloat(),
            root.optDouble("panY", 0.0).toFloat(),
        ).let { pan ->
            Offset(
                pan.x.coerceIn(-boardWidth * 0.20710678f, boardWidth * 0.20710678f),
                pan.y.coerceIn(-boardHeight * 0.56066017f, boardHeight * 0.56066017f),
            )
        }
        boardZoom = 1f
        topologyEpoch += 1
        seeded = true
        toast = "已载入实验快照"
        return true
    }

    fun showSchematic() {        if (components.isEmpty()) {
            toast = "先从仪器库拖入仪器"
            return
        }
        wireStart = null
        wirePointer = null
        schematicOpen = true
    }

    fun hideSchematic() {
        schematicOpen = false
    }

    fun updateHelpOpen(open: Boolean) {
        helpOpen = open
    }

    fun updateAboutOpen(open: Boolean) {
        aboutOpen = open
    }

    fun loadChallengeProgress(scores: Map<Int, Int>) {
        challengeScores = scores
        challengeCompleted = scores.filterValues { it >= 80 }.keys
    }

    fun isChallengeUnlocked(index: Int): Boolean {
        if (index <= 0) return true
        val previous = challengeScores[index - 1] ?: 0
        if (previous < 80) return false
        for (i in 0 until index - 1) {
            if ((challengeScores[i] ?: 0) < 100) return false
        }
        return true
    }

    fun openChallenge() {
        challengeOpen = true
    }

    fun closeChallenge() {
        challengeOpen = false
    }

    fun selectChallenge(index: Int) {
        val next = index.coerceIn(0, ChallengeTask.entries.lastIndex)
        if (next != challengeIndex) {
            challengeResult = null
            challengeActive = false
            challengeDataTokens = 0
            challengeDataActiveKey = null
            challengeCapacitorVoltageTokens = 0
            challengeCapacitorCurrentTokens = 0
            challengeCapacitorActiveRow = null
            challengeElapsedMs = 0L
            challengeTimerRunning = false
            challengeTimerPaused = false
            challengeLampSpecChanges = 0
        }
        challengeIndex = next
    }

    fun startChallenge(task: ChallengeTask) {
        if (!isChallengeUnlocked(task.ordinal)) {
            toast = "请先完成前一关并达到 80 分，再挑战${task.title}"
            return
        }
        if (boardWidth >= 200f && boardHeight >= 150f) {
            pushHistory()
            components = emptyList()
            wires = emptyList()
            selection = Selection()
            cancelWire()
            specTargetId = null
            meterZoomId = null
            wirePanelId = null
            boardPan = Offset.Zero
            boardZoom = 1f
        }
        challengeVisitedWireSamples = emptySet()
        challengeRheostatMoved = false
        challengeRheostatPositions = emptySet()
        challengeRheostatCurrents = emptySet()
        challengeInputs = emptyMap()
        challengeConclusionChoices = emptyMap()
        challengeDataTokens = 0
        challengeDataActiveKey = null
        challengeCapacitorVoltageTokens = 0
        challengeCapacitorCurrentTokens = 0
        challengeCapacitorActiveRow = null
        challengeElapsedMs = 0L
        challengeTimerRunning = false
        challengeTimerPaused = false
        challengeLampSpecChanges = 0
        challengeResult = null
        challengeIndex = task.ordinal
        challengeActive = true
        challengeOpen = false
        when (task.target) {
            ChallengeTarget.FIXED_RESISTOR -> {
                addInstrument(InstrumentType.RESISTOR, Offset(boardWidth / 2f, boardHeight / 2f))
                components.lastOrNull()?.let {
                    setResistance(it.id, listOf(5f, 10f, 15f, 20f).random())
                    setResistanceHidden(it.id, true)
                }
            }
            ChallengeTarget.TEMP_LAMP -> {
                addInstrument(InstrumentType.TEMP_LAMP, Offset(boardWidth / 2f, boardHeight / 2f))
                components.lastOrNull()?.let {
                    setLampSpec(it.id, LampSpec.entries.random())
                }
            }
            ChallengeTarget.BATTERY_SOURCE -> {
                addInstrument(InstrumentType.BATTERY, Offset(boardWidth / 2f, boardHeight / 2f))
                components.lastOrNull()?.let { source ->
                    components = components.map {
                        if (it.id == source.id) {
                            it.copy(
                                batteryCells = 2,
                                internalResistanceEnabled = true,
                                internalResistanceOhm = (0.2f + 0.8f * kotlin.random.Random.nextFloat()),
                                internalResistanceHidden = true,
                                specLocked = true,
                            )
                        } else {
                            it
                        }
                    }
                }
            }
            null -> Unit
        }
        toast = if (task.target == null) {
            "开始${task.title}：请从仪器库拖动仪器，完成接线后提交"
        } else {
            "开始${task.title}：目标元件已放在实验台，请连接电路并提交"
        }
    }

    fun setChallengeConclusion(task: ChallengeTask, option: Int) {
        challengeConclusionChoices = challengeConclusionChoices + (task.name to option)
        challengeResult = null
    }

    fun exitChallenge() {
        challengeActive = false
        challengeOpen = false
        challengeResult = null
        challengeTimerRunning = false
        challengeTimerPaused = false
        challengeCapacitorActiveRow = null
        toast = "已退出任务闯关模式"
    }

    fun updateChallengeInput(key: String, value: String) {
        challengeInputs = challengeInputs + (key to value)
        challengeResult = null
    }

    fun grantChallengeDataEntry(meterType: InstrumentType? = null) {
        if (!challengeActive) return
        if (ChallengeTask.entries[challengeIndex] == ChallengeTask.CAPACITOR_CHARGE) {
            when (meterType) {
                InstrumentType.VOLTMETER -> challengeCapacitorVoltageTokens = 1
                InstrumentType.GALVANOMETER -> challengeCapacitorCurrentTokens = 1
                else -> Unit
            }
            return
        }
        if (challengeDataActiveKey == null) {
            challengeDataTokens = 1
        }
    }

    fun canEditChallengeCell(key: String): Boolean {
        if (!challengeActive) return false
        val task = ChallengeTask.entries[challengeIndex]
        if (task == ChallengeTask.CAPACITOR_CHARGE) {
            val parts = key.split(':')
            if (parts.size != 3) return false
            val row = parts[1].toIntOrNull() ?: return false
            val column = parts[2].toIntOrNull() ?: return false
            if (challengeCapacitorActiveRow != row) return false
            return when (column) {
                1 -> challengeCapacitorVoltageTokens > 0
                2 -> challengeCapacitorCurrentTokens > 0
                else -> false
            }
        }
        if (challengeDataTokens <= 0) return false
        return challengeDataActiveKey == null || challengeDataActiveKey == key
    }

    fun noteChallengeCellEdited(key: String) {
        if (!challengeActive) return
        if (ChallengeTask.entries[challengeIndex] == ChallengeTask.CAPACITOR_CHARGE) return
        if (challengeDataTokens <= 0) return
        challengeDataActiveKey = key
        challengeDataEditVersion += 1
    }

    fun recordCapacitorRow(timeText: String) {
        if (!challengeActive) return
        if (ChallengeTask.entries[challengeIndex] != ChallengeTask.CAPACITOR_CHARGE) return
        if (challengeCapacitorActiveRow != null) return
        val row = (0..5).firstOrNull {
            challengeInputs[challengeCellKey(ChallengeTask.CAPACITOR_CHARGE, it, 0)].isNullOrBlank()
        } ?: return
        challengeCapacitorActiveRow = row
        challengeTimerPaused = true
        updateChallengeInput(challengeCellKey(ChallengeTask.CAPACITOR_CHARGE, row, 0), timeText)
    }

    fun saveCapacitorRow(): Boolean {
        if (!challengeActive) return false
        if (ChallengeTask.entries[challengeIndex] != ChallengeTask.CAPACITOR_CHARGE) return false
        val row = challengeCapacitorActiveRow ?: return false
        val voltage = challengeInputs[challengeCellKey(ChallengeTask.CAPACITOR_CHARGE, row, 1)].orEmpty().trim()
        val current = challengeInputs[challengeCellKey(ChallengeTask.CAPACITOR_CHARGE, row, 2)].orEmpty().trim()
        if (voltage.isBlank() || current.isBlank()) return false
        challengeCapacitorVoltageTokens = 0
        challengeCapacitorCurrentTokens = 0
        challengeCapacitorActiveRow = null
        challengeTimerPaused = false
        return true
    }

    fun startCapacitorTimerIfNeeded(solution: CircuitSolution) {
        if (!challengeActive) return
        if (ChallengeTask.entries[challengeIndex] != ChallengeTask.CAPACITOR_CHARGE) return
        if (challengeTimerRunning || challengeTimerPaused) return
        val capacitor = components.firstOrNull { it.type == InstrumentType.CAPACITOR } ?: return
        val current = solution.readings[capacitor.id]?.signedCurrent ?: 0f
        if (kotlin.math.abs(current) > 0.000001f) {
            challengeTimerRunning = true
        }
    }

    fun advanceChallengeTimer(deltaMs: Long) {
        if (!challengeActive || !challengeTimerRunning || challengeTimerPaused) return
        challengeElapsedMs += deltaMs
    }

    fun pauseChallengeTimer() {
        if (challengeActive && challengeTimerRunning) challengeTimerPaused = true
    }

    fun resumeChallengeTimer() {
        if (challengeActive && challengeTimerRunning && challengeCapacitorActiveRow == null) {
            challengeTimerPaused = false
        }
    }

    fun finishChallengeDataEntry() {
        if (challengeDataActiveKey == null) return
        challengeDataActiveKey = null
        challengeDataTokens = 0
        challengeDataEditVersion += 1
    }

    fun submitChallenge(
        task: ChallengeTask,
        solution: CircuitSolution,
        onProgressSaved: ((Map<Int, Int>) -> Unit)? = null,
    ) {
        challengeIndex = task.ordinal
        challengeTimerRunning = false
        challengeTimerPaused = false
        challengeCapacitorActiveRow = null
        challengeDataTokens = 0
        challengeDataActiveKey = null
        challengeResult = evaluateChallenge(
            task = task,
            components = components,
            solution = solution,
            records = records,
            visitedWireSamples = challengeVisitedWireSamples,
            rheostatPositions = challengeRheostatPositions,
            rheostatMoved = challengeRheostatMoved,
            inputs = challengeInputs,
            conclusionChoice = challengeConclusionChoices[task.name],
            lampSpecChanges = challengeLampSpecChanges,
        )
        val result = challengeResult
        val score = result?.score ?: 0
        if (score >= 80) {
            challengeCompleted = challengeCompleted + task.ordinal
            challengeScores = challengeScores + (task.ordinal to maxOf(score, challengeScores[task.ordinal] ?: 0))
        }
        val allDone = ChallengeTask.entries.indices.all { (challengeScores[it] ?: 0) >= 100 }
        onProgressSaved?.invoke(challengeScores)

        if (score >= 100 && task.ordinal < ChallengeTask.entries.lastIndex) {
            val next = ChallengeTask.entries[task.ordinal + 1]
            if (isChallengeUnlocked(next.ordinal)) {
                startChallenge(next)
                toast = "满分通过，自动进入下一关：${next.title}"
                return
            }
        }

        challengeOpen = true
        toast = when {
            allDone -> "全部闯关完成，太棒了！你的实验能力非常出色！"
            score >= 100 -> "太棒了，本关满分通过！继续保持！"
            score >= 80 -> "做得很好，本关已通过，可以进入下一关！"
            else -> "${task.title}尚未通过，请查看目标清单，再试一次"
        }
    }

    fun consumeToast() {
        toast = null
    }

    fun notify(message: String) {
        toast = message
    }

    fun loadExample() {
        loadTemplate(ExperimentTemplate.SERIES)
    }

    fun loadTemplate(template: ExperimentTemplate) {
        if (boardWidth < 200f || boardHeight < 150f) return
        pushHistory()
        topologyEpoch += 1
        components = emptyList()
        wires = emptyList()
        selection = Selection()
        cancelWire()
        specTargetId = null
        boardPan = Offset.Zero
        boardZoom = 1f
        meterZoomId = null
        wirePanelId = null
        seeded = true
        activeTemplate = template

        val safeLeft = if (libraryOpen) 226f else 26f
        val left = safeLeft + 76f
        val right = boardWidth - 92f
        val available = (right - left).coerceAtLeast(280f)
        val usable = available.coerceAtMost(780f)
        val start = left + (available - usable) / 2f
        val top = boardHeight * 0.20f
        val bottom = boardHeight * 0.72f
        val middle = boardHeight * 0.46f

        when (template) {
            ExperimentTemplate.SERIES, ExperimentTemplate.SIMPLE, ExperimentTemplate.SERIES_CURRENT, ExperimentTemplate.SERIES_VOLTAGE -> {
                val battery = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BATTERY,
                    center = Offset(start + usable * 0.08f, bottom),
                    label = "电源",
                    batteryCells = 2,
                )
                val switch = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.SWITCH,
                    center = Offset(start + usable * 0.26f, top),
                    label = "S",
                    closed = true,
                )
                val lamp1 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.LAMP,
                    center = Offset(start + usable * 0.52f, top),
                    label = "L1",
                    lampSpec = LampSpec.V3_8_0_3,
                )
                val lamp2 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.LAMP,
                    center = Offset(start + usable * 0.72f, top),
                    label = "L2",
                    lampSpec = LampSpec.V3_8_0_3,
                )
                val ammeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.AMMETER,
                    center = Offset(start + usable * 0.90f, bottom),
                    label = "A",
                )
                components = listOf(battery, switch, lamp1, lamp2, ammeter)
                wires = listOf(
                    WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 1), TerminalRef(lamp1.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(lamp1.id, 1), TerminalRef(lamp2.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(lamp2.id, 1), TerminalRef(ammeter.id, 1), WireColor.WHITE),
                    WireSegment(nextId++, TerminalRef(ammeter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK),
                )
            }

            ExperimentTemplate.PARALLEL, ExperimentTemplate.PARALLEL_CURRENT, ExperimentTemplate.PARALLEL_VOLTAGE -> {
                val battery = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BATTERY,
                    center = Offset(start + usable * 0.08f, bottom),
                    label = "电源",
                    batteryCells = 1,
                )
                val switch = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.SWITCH,
                    center = Offset(start + usable * 0.24f, top),
                    label = "S",
                    closed = true,
                )
                val lamp1 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.LAMP,
                    center = Offset(start + usable * 0.62f, top),
                    label = "L1",
                    lampSpec = LampSpec.V2_5_0_3,
                )
                val lamp2 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.LAMP,
                    center = Offset(start + usable * 0.62f, middle),
                    label = "L2",
                    lampSpec = LampSpec.V2_5_0_3,
                )
                val ammeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.AMMETER,
                    center = Offset(start + usable * 0.92f, bottom),
                    label = "A",
                )
                components = listOf(battery, switch, lamp1, lamp2, ammeter)
                wires = listOf(
                    WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 1), TerminalRef(lamp2.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(lamp1.id, 0), TerminalRef(lamp2.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(lamp2.id, 1), TerminalRef(ammeter.id, 2), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(lamp1.id, 1), TerminalRef(lamp2.id, 1), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(ammeter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK),
                )
            }

            ExperimentTemplate.OHM_LAW, ExperimentTemplate.BULB_POWER, ExperimentTemplate.VOLT_AMMETER_IN, ExperimentTemplate.VOLT_AMMETER_OUT, ExperimentTemplate.DIVIDER, ExperimentTemplate.LIMITER, ExperimentTemplate.BULB_CURVE, ExperimentTemplate.EMF_INTERNAL, ExperimentTemplate.METER_MOD, ExperimentTemplate.HALF_BIAS -> {
                val battery = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BATTERY,
                    center = Offset(start + usable * 0.06f, bottom),
                    label = "电源",
                    batteryCells = 3,
                )
                val switch = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.SWITCH,
                    center = Offset(start + usable * 0.20f, top),
                    label = "S",
                    closed = true,
                )
                val rheostat = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.RHEOSTAT,
                    center = Offset(start + usable * 0.40f, top),
                    label = "R滑",
                    slider = 0.65f,
                )
                val resistor = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.RESISTOR,
                    center = Offset(start + usable * 0.66f, top),
                    label = "Rx",
                    resistance = 10f,
                )
                val ammeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.AMMETER,
                    center = Offset(start + usable * 0.91f, bottom),
                    label = "A",
                )
                val voltmeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.VOLTMETER,
                    center = Offset(start + usable * 0.66f, middle),
                    label = "V",
                    meterFlipped = true,
                )
                components = listOf(battery, switch, rheostat, resistor, ammeter, voltmeter)
                wires = listOf(
                    WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 1), TerminalRef(rheostat.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(rheostat.id, 2), TerminalRef(resistor.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(resistor.id, 1), TerminalRef(ammeter.id, 1), WireColor.WHITE),
                    WireSegment(nextId++, TerminalRef(ammeter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(resistor.id, 0), TerminalRef(voltmeter.id, 2), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(resistor.id, 1), TerminalRef(voltmeter.id, 0), WireColor.BLACK),
                )
            }

            ExperimentTemplate.ELECTROMAGNET -> {
                val battery = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BATTERY,
                    center = Offset(start + usable * 0.10f, bottom),
                    label = "电源",
                    batteryCells = 2,
                )
                val switch = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.SWITCH,
                    center = Offset(start + usable * 0.34f, top),
                    label = "S",
                    closed = true,
                )
                val coil = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.ELECTROMAGNET,
                    center = Offset(start + usable * 0.62f, middle),
                    label = "EM",
                )
                val compass = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.COMPASS,
                    center = Offset(start + usable * 0.84f, middle),
                    label = "磁针",
                )
                components = listOf(battery, switch, coil, compass)
                wires = listOf(
                    WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 1), TerminalRef(coil.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(coil.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK),
                )
            }

            ExperimentTemplate.MAGNETS -> {
                val magnet1 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BAR_MAGNET,
                    center = Offset(start + usable * 0.30f, middle),
                    label = "磁体1",
                )
                val magnet2 = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BAR_MAGNET,
                    center = Offset(start + usable * 0.62f, middle),
                    label = "磁体2",
                    polarityFlipped = true,
                )
                val compass = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.COMPASS,
                    center = Offset(start + usable * 0.46f, top),
                    label = "磁针",
                )
                components = listOf(magnet1, magnet2, compass)
            }

            ExperimentTemplate.MULTIMETER -> {
                val resistor = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.RESISTOR,
                    center = Offset(start + usable * 0.42f, middle),
                    label = "Rx",
                    resistance = 10f,
                )
                val multimeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.MULTIMETER,
                    center = Offset(start + usable * 0.68f, middle),
                    label = "M",
                    multimeterMode = MultimeterMode.RESISTANCE,
                    multimeterRangeIndex = 0,
                )
                components = listOf(resistor, multimeter)
                val lowerY = middle + 58f
                val upperY = middle - 8f
                wires = listOf(
                    WireSegment(
                        nextId++, TerminalRef(resistor.id, 0), TerminalRef(multimeter.id, 0),
                        WireColor.RED,
                        listOf(
                            Offset(resistor.center.x, lowerY),
                            Offset(multimeter.center.x - 45f, lowerY),
                        ),
                    ),
                    WireSegment(
                        nextId++, TerminalRef(resistor.id, 1), TerminalRef(multimeter.id, 1),
                        WireColor.BLACK,
                        listOf(
                            Offset(resistor.center.x + 35f, upperY),
                            Offset(multimeter.center.x, upperY),
                        ),
                    ),
                )
            }

            ExperimentTemplate.RC_CHARGE -> {
                val battery = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.BATTERY,
                    center = Offset(start + usable * 0.08f, top + (bottom - top) * 0.893f),
                    label = "电源",
                    batteryCells = 2,
                )
                val switch = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.SPDT_SWITCH,
                    center = Offset(start + usable * 0.315f, top + (bottom - top) * 0.371f),
                    label = "S",
                    switchPosition = 2,
                )
                val resistor = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.RESISTOR,
                    center = Offset(start + usable * 0.667f, bottom),
                    label = "R",
                    resistance = 10_000f,
                )
                val galvanometer = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.GALVANOMETER,
                    center = Offset(start + usable * 0.57f, top),
                    label = "G",
                )
                val capacitor = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.CAPACITOR,
                    center = Offset(start + usable * 0.92f, top + (bottom - top) * 0.551f),
                    label = "C",
                    capacitanceUf = 1000f,
                )
                val voltmeter = PlacedInstrument(
                    id = nextId++,
                    type = InstrumentType.VOLTMETER,
                    center = Offset(start + usable * 0.907f, top + (bottom - top) * 0.043f),
                    label = "V",
                    meterFlipped = true,
                )
                components = listOf(battery, switch, resistor, galvanometer, capacitor, voltmeter)
                wires = listOf(
                    WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 1), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 0), TerminalRef(galvanometer.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(switch.id, 2), TerminalRef(resistor.id, 0), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(capacitor.id, 0), TerminalRef(galvanometer.id, 1), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(capacitor.id, 1), TerminalRef(resistor.id, 1), WireColor.RED),
                    WireSegment(nextId++, TerminalRef(voltmeter.id, 0), TerminalRef(capacitor.id, 1), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(voltmeter.id, 1), TerminalRef(capacitor.id, 0), WireColor.BLACK),
                    WireSegment(nextId++, TerminalRef(battery.id, 0), TerminalRef(resistor.id, 0), WireColor.BLACK),
                )
            }
        }
        toast = "已载入${template.title}"
    }

    fun seedDemo(width: Float, height: Float, safeLeft: Float) {
        if (seeded) return
        if (components.isNotEmpty()) return
        if (width < 200f || height < 150f) return
        seeded = true
        val left = safeLeft + 76f
        val right = width - 92f
        val available = (right - left).coerceAtLeast(280f)
        val usable = available.coerceAtMost(780f)
        val start = left + (available - usable) / 2f
        val battery = PlacedInstrument(
            id = nextId++,
            type = InstrumentType.BATTERY,
            center = Offset(start + usable * 0.08f, height * 0.72f),
            label = "电源",
            batteryCells = 2,
        )
        val switch = PlacedInstrument(
            id = nextId++,
            type = InstrumentType.SWITCH,
            center = Offset(start + usable * 0.26f, height * 0.20f),
            label = "S",
            closed = true,
        )
        val lamp = PlacedInstrument(
            id = nextId++,
            type = InstrumentType.LAMP,
            center = Offset(start + usable * 0.72f, height * 0.20f),
            label = "L1",
            lampSpec = LampSpec.V3_8_0_3,
        )
        val ammeter = PlacedInstrument(
            id = nextId++,
            type = InstrumentType.AMMETER,
            center = Offset(start + usable * 0.88f, height * 0.72f),
            label = "A",
        )
        components = listOf(battery, switch, lamp, ammeter)
        wires = listOf(
            WireSegment(nextId++, TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED),
            WireSegment(nextId++, TerminalRef(switch.id, 1), TerminalRef(lamp.id, 0), WireColor.BLACK),
            WireSegment(nextId++, TerminalRef(lamp.id, 1), TerminalRef(ammeter.id, 1), WireColor.WHITE),
            WireSegment(nextId++, TerminalRef(ammeter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK),
        )
        selection = Selection()
    }

    private fun clampCenter(type: InstrumentType, center: Offset): Offset {
        val marginX = type.halfWidth + 24f
        val marginY = type.halfHeight + 26f
        return Offset(
            center.x.coerceIn(worldLeft + marginX, (worldRight - marginX).coerceAtLeast(worldLeft + marginX)),
            center.y.coerceIn(worldTop + marginY, (worldBottom - marginY).coerceAtLeast(worldTop + marginY)),
        )
    }

    private fun nextLabel(type: InstrumentType): String {
        val prefix = when (type) {
            InstrumentType.BATTERY -> return "电源"
            InstrumentType.POWER_SUPPLY -> "电源"
            InstrumentType.STORAGE_BATTERY -> "蓄电池"
            InstrumentType.LAMP -> "L"
            InstrumentType.TEMP_LAMP -> "Lt"
            InstrumentType.SWITCH -> "S"
            InstrumentType.AMMETER -> "A"
            InstrumentType.VOLTMETER -> "V"
            InstrumentType.GALVANOMETER -> "G"
            InstrumentType.MILLIAMMETER -> "mA"
            InstrumentType.MULTIMETER -> "M"
            InstrumentType.RESISTOR -> "R"
            InstrumentType.RHEOSTAT -> "R"
            InstrumentType.RESISTANCE_BOX -> "R箱"
            InstrumentType.RESISTANCE_WIRE -> "R丝"
            InstrumentType.MOTOR -> "M"
            InstrumentType.BUZZER -> "BZ"
            InstrumentType.LED -> "LED"
            InstrumentType.DIODE -> "D"
            InstrumentType.THERMISTOR -> "Rt"
            InstrumentType.PHOTORESISTOR -> "Rg"
            InstrumentType.ELECTROMAGNET -> "EM"
            InstrumentType.SOLENOID -> "L"
            InstrumentType.BAR_MAGNET -> "磁体"
            InstrumentType.HORSESHOE_MAGNET -> "磁体"
            InstrumentType.COMPASS -> "磁针"
            InstrumentType.PAPERCLIPS -> "回形针"
            InstrumentType.SPDT_SWITCH -> "S₂"
            InstrumentType.CAPACITOR -> "C"
            InstrumentType.INDUCTOR -> "L"
        }
        var index = 1
        var candidate: String
        do {
            candidate = "$prefix$index"
            index++
        } while (components.any { it.label == candidate })
        return candidate
    }

    private fun currentSnapshot(): Snapshot = Snapshot(components, wires, nextId)

    private fun restore(snapshot: Snapshot) {
        topologyEpoch += 1
        components = snapshot.components
        wires = snapshot.wires
        nextId = snapshot.nextId
        selection = Selection()
        cancelWire()
    }

    private fun pushHistory() {
        undoStack.addLast(currentSnapshot())
        while (undoStack.size > 40) undoStack.removeFirst()
        redoStack.clear()
        refreshHistoryFlags()
    }

    private fun refreshHistoryFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }
}

private fun normalizedRotation(value: Float): Float = ((value % 360f) + 360f) % 360f
