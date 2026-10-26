package com.physlab.electric.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class InstrumentCategory(val title: String, val subtitle: String, val shortTitle: String) {
    POWER("电源", "提供电压", "电源"),
    LOAD("用电器", "消耗电能", "用电"),
    METER("测量", "电流与电压", "测量"),
    CONTROL("控制与元件", "开关和电阻", "控制"),
    DYNAMIC("动态元件", "电容与电感", "动态"),
    MAGNET("磁学", "磁场与电磁", "磁学"),
    WIRE("导线", "自由选择颜色", "导线"),
}

enum class MeterResistanceMode {
    ANALOG,
    DIGITAL,
    HIGH_PRECISION,
}

enum class MultimeterMode(val title: String, val symbol: String) {
    DC_VOLTAGE("直流电压", "DCV"),
    AC_VOLTAGE("交流电压", "ACV"),
    DC_CURRENT("直流电流", "DCA"),
    AC_CURRENT("交流电流", "ACA"),
    RESISTANCE("电阻", "Ω"),
    DIODE("二极管", "▷|"),
}

data class MultimeterRange(
    val max: Float,
    val label: String,
    val unit: String,
    val decimals: Int,
)

val MultimeterRanges: Map<MultimeterMode, List<MultimeterRange>> = mapOf(
    MultimeterMode.DC_VOLTAGE to listOf(
        MultimeterRange(2f, "2V", "V", 3),
        MultimeterRange(20f, "20V", "V", 2),
        MultimeterRange(200f, "200V", "V", 1),
        MultimeterRange(1000f, "1000V", "V", 0),
    ),
    MultimeterMode.AC_VOLTAGE to listOf(
        MultimeterRange(2f, "2V", "V", 3),
        MultimeterRange(20f, "20V", "V", 2),
        MultimeterRange(200f, "200V", "V", 1),
        MultimeterRange(700f, "700V", "V", 0),
    ),
    MultimeterMode.DC_CURRENT to listOf(
        MultimeterRange(0.002f, "2mA", "mA", 3),
        MultimeterRange(0.02f, "20mA", "mA", 2),
        MultimeterRange(0.2f, "200mA", "mA", 1),
        MultimeterRange(10f, "10A", "A", 2),
    ),
    MultimeterMode.AC_CURRENT to listOf(
        MultimeterRange(0.002f, "2mA", "mA", 3),
        MultimeterRange(0.02f, "20mA", "mA", 2),
        MultimeterRange(0.2f, "200mA", "mA", 1),
        MultimeterRange(10f, "10A", "A", 2),
    ),
    MultimeterMode.RESISTANCE to listOf(
        MultimeterRange(200f, "200Ω", "Ω", 1),
        MultimeterRange(2_000f, "2kΩ", "kΩ", 3),
        MultimeterRange(20_000f, "20kΩ", "kΩ", 2),
        MultimeterRange(200_000f, "200kΩ", "kΩ", 1),
        MultimeterRange(2_000_000f, "2MΩ", "MΩ", 2),
    ),
    MultimeterMode.DIODE to listOf(
        MultimeterRange(2f, "二极管", "V", 2),
    ),
)

fun multimeterRanges(mode: MultimeterMode): List<MultimeterRange> =
    MultimeterRanges[mode] ?: MultimeterRanges.getValue(MultimeterMode.DC_VOLTAGE)

fun PlacedInstrument.multimeterRange(): MultimeterRange {
    val ranges = multimeterRanges(multimeterMode)
    return ranges[multimeterRangeIndex.coerceIn(ranges.indices)]
}

enum class LampSpec(
    val voltage: Float,
    val current: Float,
    val label: String,
) {
    V2_5_0_3(2.5f, 0.3f, "2.5V 0.75W"),
    V3_8_0_3(3.8f, 0.3f, "3.8V 1.14W"),
    V2_5_0_2(2.5f, 0.2f, "2.5V 0.5W"),
    V1_5_0_45(1.5f, 0.3f, "1.5V 0.45W"),
    V6_0_3_6(6f, 0.6f, "6V 3.6W");

    val resistance: Float get() = voltage / current
    val ratedPower: Float get() = voltage * current
}

enum class WireMaterial(
    val label: String,
    val resistivity: Float,
    val wireColor: Color,
) {
    NICHROME("镍铬合金", 1.10f, Color(0xFFD83942)),
    FECRAL("铁铬铝合金", 1.45f, Color(0xFF1E232B)),
    FECHROME("铁铬合金", 1.25f, Color(0xFF1E232B)),
}

data class ResistanceWireSample(
    val index: Int,
    val name: String,
    val material: WireMaterial,
    val length: Float,
    val area: Float,
) {
    val resistance: Float get() = material.resistivity * length / area
    val color: Color get() = material.wireColor
}

val ResistanceWireSamples = listOf(
    ResistanceWireSample(0, "长细镍铬合金丝", WireMaterial.NICHROME, 1.2f, 0.2f),
    ResistanceWireSample(1, "长细铁铬铝合金丝", WireMaterial.FECRAL, 1.2f, 0.2f),
    ResistanceWireSample(2, "长粗铁铬合金丝", WireMaterial.FECHROME, 1.2f, 0.6f),
    ResistanceWireSample(3, "短细铁铬合金丝", WireMaterial.FECHROME, 0.6f, 0.2f),
)

enum class InstrumentType(
    val id: String,
    val displayName: String,
    val category: InstrumentCategory,
    val width: Float,
    val height: Float,
    val terminalOffsets: List<Offset>,
    val terminalLabels: List<String> = emptyList(),
    val defaultLabel: String = "",
) {
    BATTERY(
        "battery", "电池组", InstrumentCategory.POWER, 150f, 56f,
        listOf(Offset(-70f, 0f), Offset(70f, 0f)),
        listOf("−", "+"), "电源",
    ),
    POWER_SUPPLY(
        "power_supply", "学生电源", InstrumentCategory.POWER, 126f, 86f,
        listOf(Offset(-43f, 32f), Offset(43f, 32f)),
        listOf("−", "+"), "电源",
    ),
    STORAGE_BATTERY(
        "storage_battery", "蓄电池", InstrumentCategory.POWER, 106f, 66f,
        listOf(Offset(-38f, 33f), Offset(38f, 33f)),
        listOf("−", "+"), "蓄电池",
    ),
    LAMP(
        "lamp", "小灯泡", InstrumentCategory.LOAD, 84f, 92f,
        listOf(Offset(-28f, 32f), Offset(28f, 32f)),
        listOf("1", "2"), "L1",
    ),
    TEMP_LAMP(
        "temp_lamp", "热敏小灯泡", InstrumentCategory.LOAD, 84f, 92f,
        listOf(Offset(-28f, 32f), Offset(28f, 32f)),
        listOf("1", "2"), "Lt",
    ),
    MOTOR(
        "motor", "电动机", InstrumentCategory.LOAD, 104f, 80f,
        listOf(Offset(-25f, 37f), Offset(25f, 37f)),
        listOf("+", "−"), "M",
    ),
    BUZZER(
        "buzzer", "蜂鸣器", InstrumentCategory.LOAD, 78f, 70f,
        listOf(Offset(-25f, 31f), Offset(25f, 31f)),
        listOf("+", "−"), "BZ",
    ),
    LED(
        "led", "发光二极管", InstrumentCategory.LOAD, 84f, 72f,
        listOf(Offset(-27f, 32f), Offset(27f, 32f)),
        listOf("+", "−"), "LED",
    ),
    AMMETER(
        "ammeter", "电流表", InstrumentCategory.METER, 84f, 92f,
        listOf(Offset(-26f, 39f), Offset(0f, 39f), Offset(26f, 39f)),
        listOf("−", "0.6", "3"), "A",
    ),
    VOLTMETER(
        "voltmeter", "电压表", InstrumentCategory.METER, 84f, 92f,
        listOf(Offset(-26f, 39f), Offset(0f, 39f), Offset(26f, 39f)),
        listOf("−", "3", "15"), "V",
    ),
    GALVANOMETER(
        "galvanometer", "灵敏电流计", InstrumentCategory.METER, 84f, 92f,
        listOf(Offset(-26f, 39f), Offset(26f, 39f)),
        listOf("−", "+"), "G",
    ),
    MILLIAMMETER(
        "milliammeter", "毫安表", InstrumentCategory.METER, 84f, 92f,
        listOf(Offset(-26f, 39f), Offset(0f, 39f), Offset(26f, 39f)),
        listOf("−", "50", "500"), "mA",
    ),
    MULTIMETER(
        "multimeter", "多用电表", InstrumentCategory.METER, 108f, 104f,
        listOf(Offset(-32f, 46f), Offset(0f, 46f), Offset(32f, 46f)),
        listOf("COM", "VΩ", "A"), "M",
    ),
    SWITCH(
        "switch", "开关", InstrumentCategory.CONTROL, 100f, 54f,
        listOf(Offset(-35f, 13f), Offset(35f, 13f)),
        listOf("1", "2"), "S",
    ),
    RHEOSTAT(
        "rheostat", "滑动变阻器", InstrumentCategory.CONTROL, 136f, 86f,
        listOf(
            Offset(-48f, 34f), Offset(48f, 34f),
            Offset(-30f, -33f), Offset(30f, -33f),
        ),
        listOf("A", "B", "C", "D"), "R",
    ),
    SPDT_SWITCH(
        "spdt_switch", "单刀双掷开关", InstrumentCategory.CONTROL, 112f, 66f,
        listOf(Offset(0f, 24f), Offset(-44f, 24f), Offset(44f, 24f)),
        listOf("中", "左", "右"), "S₂",
    ),
    CAPACITOR(
        "capacitor", "电容", InstrumentCategory.DYNAMIC, 94f, 84f,
        listOf(Offset(-24f, 38f), Offset(24f, 38f)),
        listOf("+", "−"), "C",
    ),
    INDUCTOR(
        "inductor", "电感", InstrumentCategory.DYNAMIC, 112f, 70f,
        listOf(Offset(-48f, 30f), Offset(48f, 30f)),
        listOf("1", "2"), "L",
    ),
    RESISTOR(
        "resistor", "定值电阻", InstrumentCategory.CONTROL, 110f, 44f,
        listOf(Offset(-55f, 0f), Offset(55f, 0f)),
        listOf("1", "2"), "R0",
    ),
    RESISTANCE_BOX(
        "resistance_box", "电阻箱", InstrumentCategory.CONTROL, 126f, 88f,
        listOf(Offset(-48f, 37f), Offset(48f, 37f)),
        listOf("1", "2"), "R箱",
    ),
    RESISTANCE_WIRE(
        "resistance_wire", "电阻丝", InstrumentCategory.CONTROL, 192f, 112f,
        listOf(Offset(-80f, 50f), Offset(80f, 50f)),
        listOf("1", "2"), "R丝",
    ),
    ELECTROMAGNET(
        "electromagnet", "电磁铁", InstrumentCategory.MAGNET, 98f, 78f,
        listOf(Offset(-41f, 35f), Offset(41f, 35f)),
        listOf("+", "−"), "EM",
    ),
    SOLENOID(
        "solenoid", "螺线管", InstrumentCategory.MAGNET, 110f, 64f,
        listOf(Offset(-49f, 31f), Offset(49f, 31f)),
        listOf("1", "2"), "L",
    ),
    BAR_MAGNET(
        "bar_magnet", "条形磁体", InstrumentCategory.MAGNET, 118f, 42f,
        emptyList(), emptyList(), "磁体",
    ),
    HORSESHOE_MAGNET(
        "horseshoe", "蹄形磁体", InstrumentCategory.MAGNET, 82f, 86f,
        emptyList(), emptyList(), "磁体",
    ),
    COMPASS(
        "compass", "小磁针", InstrumentCategory.MAGNET, 62f, 62f,
        emptyList(), emptyList(), "磁针",
    ),
    PAPERCLIPS(
        "paperclips", "回形针", InstrumentCategory.MAGNET, 78f, 54f,
        emptyList(), emptyList(), "回形针",
    );

    val hasTerminals: Boolean get() = terminalOffsets.isNotEmpty()
    val halfWidth: Float get() = width / 2f
    val halfHeight: Float get() = height / 2f

    fun terminal(index: Int): Offset =
        terminalOffsets.getOrElse(index) { Offset.Zero }
}

enum class WireColor(
    val displayName: String,
    val body: Color,
    val highlight: Color,
    val shadow: Color,
) {
    BLACK(
        "蓝色", Color(0xFF2F6FDB), Color(0xFF8AB8FF), Color(0xFF0A2350),
    ),
    RED(
        "红色", Color(0xFFD8343F), Color(0xFFFFA0A6), Color(0xFF41060A),
    ),
    WHITE(
        "白色", Color(0xFFE8EBF0), Color(0xFFFFFFFF), Color(0xFF585D67),
    ),
}

data class PlacedInstrument(
    val id: Long,
    val type: InstrumentType,
    val center: Offset,
    val label: String = type.defaultLabel,
    val rotation: Float = 0f,
    val closed: Boolean = false,
    val batteryCells: Int = 2,
    val lampSpec: LampSpec = LampSpec.V2_5_0_3,
    val resistance: Float = 10f,
    val resistanceHidden: Boolean = false,
    val slider: Float = 0.5f,
    val rheostatMax: Float = 20f,
    val supplyVoltage: Float = 12f,
    val supplyAc: Boolean = false,
    val internalResistanceEnabled: Boolean = false,
    val internalResistanceOhm: Float = 0.1f,
    val internalResistanceHidden: Boolean = false,
    val specLocked: Boolean = false,
    val meterResistanceEnabled: Boolean = false,
    val meterResistanceMode: MeterResistanceMode = MeterResistanceMode.ANALOG,
    val multimeterMode: MultimeterMode = MultimeterMode.DC_VOLTAGE,
    val multimeterRangeIndex: Int = 1,
    val multimeterZeroOffset: Float = 0f,
    val capacitanceUf: Float = 470f,
    val inductanceMh: Float = 50f,
    val inductanceDcResistance: Float = 2f,
    val switchPosition: Int = 0,
    val storageVoltage: Float = 6f,
    val resistanceBoxValue: Int = 100,
    val wireSampleIndex: Int = 0,
    val showWireResistance: Boolean = false,
    val polarityFlipped: Boolean = false,
    val meterFlipped: Boolean = false,
    val windingReversed: Boolean = false,
    val damaged: Boolean = false,
) {
    val voltage: Float
        get() = when (type) {
            InstrumentType.BATTERY -> batteryCells * 1.5f
            InstrumentType.POWER_SUPPLY -> supplyVoltage
            InstrumentType.STORAGE_BATTERY -> storageVoltage
            else -> 0f
        }

    val rheostatResistance: Float get() = slider.coerceIn(0f, 1f) * rheostatMax

    val multimeterRangeValue: MultimeterRange get() = multimeterRange()

    val multimeterIsVoltage: Boolean
        get() = multimeterMode == MultimeterMode.DC_VOLTAGE ||
            multimeterMode == MultimeterMode.AC_VOLTAGE

    val multimeterIsCurrent: Boolean
        get() = multimeterMode == MultimeterMode.DC_CURRENT ||
            multimeterMode == MultimeterMode.AC_CURRENT

    val effectiveInternalResistance: Float
        get() = if (internalResistanceEnabled) {
            internalResistanceOhm.coerceIn(0.001f, 1f)
        } else {
            when (type) {
                InstrumentType.STORAGE_BATTERY -> 0.05f
                InstrumentType.POWER_SUPPLY -> 0.1f
                else -> 0.08f
            }
        }

    fun meterInternalResistance(terminal: Int): Float {
        if (type == InstrumentType.MULTIMETER) {
            return when {
                multimeterIsVoltage -> 10_000_000f
                multimeterIsCurrent -> when (multimeterRangeIndex.coerceIn(0, 3)) {
                    0 -> 100f
                    1 -> 10f
                    2 -> 1f
                    else -> 0.01f
                }
                else -> 10_000_000f
            }.takeIf { terminal != 2 || multimeterIsCurrent } ?: 10_000_000f
        }
        if (type == InstrumentType.GALVANOMETER) return 50f
        if (type == InstrumentType.MILLIAMMETER) return 5f
        val analog = if (type == InstrumentType.AMMETER) {
            if (terminal == 2) 0.05f else 0.1f
        } else {
            if (terminal == 2) 15_000f else 3_000f
        }
        if (!meterResistanceEnabled) {
            return if (type == InstrumentType.AMMETER) 0.01f else if (terminal == 2) 5_000_000f else 1_000_000f
        }
        return when (meterResistanceMode) {
            MeterResistanceMode.ANALOG -> analog
            MeterResistanceMode.DIGITAL -> if (type == InstrumentType.AMMETER) 0.01f else 1_000_000f
            MeterResistanceMode.HIGH_PRECISION -> if (type == InstrumentType.AMMETER) 0.001f else 10_000_000f
        }
    }

    val resistanceBoxResistance: Float get() = resistanceBoxValue.coerceIn(0, 9999).toFloat()

    val wireSample: ResistanceWireSample
        get() = ResistanceWireSamples[wireSampleIndex.coerceIn(0, ResistanceWireSamples.lastIndex)]

    val wireResistance: Float get() = wireSample.resistance

    fun terminalPosition(index: Int): Offset {
        val offset = type.terminal(index)
        val mirrored = if (meterFlipped && type.isMeter) Offset(-offset.x, offset.y) else offset
        if (rotation == 0f) return center + mirrored
        val radians = Math.toRadians(rotation.toDouble())
        val c = kotlin.math.cos(radians).toFloat()
        val s = kotlin.math.sin(radians).toFloat()
        return center + Offset(
            mirrored.x * c - mirrored.y * s,
            mirrored.x * s + mirrored.y * c,
        )
    }
}

val InstrumentType.isPowerSource: Boolean
    get() = this == InstrumentType.BATTERY ||
        this == InstrumentType.POWER_SUPPLY ||
        this == InstrumentType.STORAGE_BATTERY

val InstrumentType.isAppliance: Boolean
    get() = this == InstrumentType.LAMP ||
        this == InstrumentType.TEMP_LAMP ||
        this == InstrumentType.MOTOR ||
        this == InstrumentType.BUZZER ||
        this == InstrumentType.LED

val InstrumentType.isMagnet: Boolean
    get() = this == InstrumentType.BAR_MAGNET || this == InstrumentType.HORSESHOE_MAGNET

val InstrumentType.isMeter: Boolean
    get() = this == InstrumentType.AMMETER ||
        this == InstrumentType.VOLTMETER ||
        this == InstrumentType.GALVANOMETER ||
        this == InstrumentType.MILLIAMMETER ||
        this == InstrumentType.MULTIMETER

val InstrumentType.isRotatable: Boolean
    get() = !isMeter &&
        this != InstrumentType.POWER_SUPPLY &&
        this != InstrumentType.COMPASS &&
        this != InstrumentType.PAPERCLIPS

fun PlacedInstrument.ratedPower(): Float? = when (type) {
    InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> lampSpec.ratedPower
    InstrumentType.MOTOR -> 4.5f * 4.5f / 12f
    InstrumentType.BUZZER -> 4.5f * 4.5f / 30f
    InstrumentType.LED -> 0.08f
    else -> null
}

data class TerminalRef(val componentId: Long, val index: Int)

data class WireSegment(
    val id: Long,
    val a: TerminalRef,
    val b: TerminalRef,
    val color: WireColor,
    val bends: List<Offset> = emptyList(),
)

data class Selection(
    val componentId: Long? = null,
    val wireId: Long? = null,
) {
    val isEmpty: Boolean get() = componentId == null && wireId == null
}

enum class CircuitStatus(val title: String) {
    NO_POWER("未接电源"),
    OPEN("断路"),
    CLOSED("通路"),
    SHORT("短路"),
}

data class ComponentReading(
    val current: Float = 0f,
    val signedCurrent: Float = 0f,
    val voltage: Float = 0f,
    val power: Float = 0f,
    val energized: Boolean = false,
    val ledReversed: Boolean = false,
    val meterValue: Float = 0f,
    val meterSignedValue: Float = 0f,
    val meterRange: Float = 0f,
    val meterRangeLabel: String = "",
    val meterOverRange: Boolean = false,
    val meterNegativeOverRange: Boolean = false,
    val meterReversed: Boolean = false,
    val meterTerminal: Int = 1,
    val multimeterMode: MultimeterMode? = null,
    val multimeterDisplay: String = "",
    val multimeterWarning: String = "",
    val meterOl: Boolean = false,
)

data class CircuitSolution(
    val status: CircuitStatus = CircuitStatus.NO_POWER,
    val epoch: Long = 0L,
    val batteryVoltage: Float = 0f,
    val totalCurrent: Float = 0f,
    val readings: Map<Long, ComponentReading> = emptyMap(),
    val hasBattery: Boolean = false,
    val solvedNodes: Int = 0,
    val newlyDamaged: Set<Long> = emptySet(),
    val reversedMeters: Set<Long> = emptySet(),
    val overRangeMeters: Set<Long> = emptySet(),
    val negativeOverRangeMeters: Set<Long> = emptySet(),
    val capacitorVoltages: Map<Long, Float> = emptyMap(),
    val inductorCurrents: Map<Long, Float> = emptyMap(),
)

data class DynamicState(
    val capacitorVoltages: Map<Long, Float> = emptyMap(),
    val inductorCurrents: Map<Long, Float> = emptyMap(),
)

data class TransientResult(
    val solution: CircuitSolution,
    val state: DynamicState,
)

private data class DynamicBranch(
    val componentId: Long,
    val a: String,
    val b: String,
    val isCapacitor: Boolean,
    val conductance: Float,
    val previous: Float,
)

internal class UnionFind {
    private val parent = HashMap<String, String>()
    private val rank = HashMap<String, Int>()

    fun add(key: String) {
        if (!parent.containsKey(key)) {
            parent[key] = key
            rank[key] = 0
        }
    }

    fun find(key: String): String {
        add(key)
        var root = key
        while (parent.getValue(root) != root) root = parent.getValue(root)
        var current = key
        while (parent.getValue(current) != current) {
            val next = parent.getValue(current)
            parent[current] = root
            current = next
        }
        return root
    }

    fun union(a: String, b: String) {
        val ra = find(a)
        val rb = find(b)
        if (ra == rb) return
        val raRank = rank.getValue(ra)
        val rbRank = rank.getValue(rb)
        when {
            raRank < rbRank -> parent[ra] = rb
            raRank > rbRank -> parent[rb] = ra
            else -> {
                parent[rb] = ra
                rank[ra] = raRank + 1
            }
        }
    }
}

private data class Branch(
    val componentId: Long,
    val a: String,
    val b: String,
    val resistance: Float,
)

private data class MeterBranch(
    val componentId: Long,
    val terminal: Int,
    val positive: String,
    val common: String,
    val resistance: Float,
    val range: Float,
)

private data class SourceBranch(
    val componentId: Long,
    val positive: String,
    val negative: String,
    val voltage: Float,
    val internalResistance: Float,
)

fun simulateCircuit(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    dynamicState: DynamicState? = null,
    dt: Float = 0.05f,
    epoch: Long = 0L,
    timeSeconds: Float = 0f,
): CircuitSolution {
    var hotPowers = emptyMap<Long, Float>()
    var result = simulateCircuitOnce(components, wires, hotPowers, dynamicState, dt, epoch, timeSeconds)
    repeat(3) {
        val tempLamps = components.filter { it.type == InstrumentType.TEMP_LAMP }
        if (tempLamps.isEmpty()) return attachMultimeterMeasurements(components, wires, result)
        hotPowers = tempLamps.associateWith { result.readings[it.id]?.power ?: 0f }
            .mapKeys { it.key.id }
        result = simulateCircuitOnce(components, wires, hotPowers, dynamicState, dt, epoch, timeSeconds)
    }
    return attachMultimeterMeasurements(components, wires, result)
}

private fun measureEquivalentResistance(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    probeA: TerminalRef,
    probeB: TerminalRef,
): Float? {
    val uf = UnionFind()
    fun key(componentId: Long, index: Int): String = terminalKey(componentId, index)
    for (component in components) {
        for (index in component.type.terminalOffsets.indices) uf.add(key(component.id, index))
    }
    uf.add(key(probeA.componentId, probeA.index))
    uf.add(key(probeB.componentId, probeB.index))
    for (wire in wires) {
        uf.union(key(wire.a.componentId, wire.a.index), key(wire.b.componentId, wire.b.index))
    }
    val aNode = uf.find(key(probeA.componentId, probeA.index))
    val bNode = uf.find(key(probeB.componentId, probeB.index))
    if (aNode == bNode) return 0f
    val nodeIds = components.flatMap { component ->
        component.type.terminalOffsets.indices.map { uf.find(key(component.id, it)) }
    }.plus(listOf(aNode, bNode)).distinct()
    val indexOf = nodeIds.withIndex().associate { it.value to it.index }
    val n = nodeIds.size
    val g = Array(n) { DoubleArray(n) }
    val adjacency = Array(n) { mutableListOf<Int>() }

    fun addResistance(ka: String, kb: String, resistance: Float) {
        if (resistance <= 0f) return
        val ia = indexOf[uf.find(ka)] ?: return
        val ib = indexOf[uf.find(kb)] ?: return
        if (ia == ib) return
        val conductance = 1.0 / resistance.coerceAtLeast(0.0005f)
        g[ia][ia] += conductance
        g[ib][ib] += conductance
        g[ia][ib] -= conductance
        g[ib][ia] -= conductance
        adjacency[ia] += ib
        adjacency[ib] += ia
    }

    for (component in components) {
        if (component.damaged) continue
        val a = key(component.id, 0)
        val b = if (component.type.terminalOffsets.size > 1) key(component.id, 1) else a
        val resistance = when (component.type) {
            InstrumentType.RESISTOR -> component.resistance
            InstrumentType.RHEOSTAT -> component.rheostatResistance
            InstrumentType.RESISTANCE_BOX -> component.resistanceBoxResistance
            InstrumentType.RESISTANCE_WIRE -> component.wireResistance
            InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> component.lampResistance(null)
            InstrumentType.SWITCH -> if (component.closed) 0.001f else 0f
            InstrumentType.INDUCTOR -> component.inductanceDcResistance
            InstrumentType.MOTOR -> 12f
            InstrumentType.BUZZER -> 30f
            InstrumentType.LED -> 180f
            InstrumentType.ELECTROMAGNET -> 6f
            InstrumentType.SOLENOID -> 5f
            else -> 0f
        }
        if (resistance > 0f) addResistance(a, b, resistance)
    }

    val startIndex = indexOf[aNode] ?: return null
    val groundIndex = indexOf[bNode] ?: return null
    val seen = BooleanArray(n)
    val queue = ArrayDeque<Int>()
    seen[startIndex] = true
    queue.addLast(startIndex)
    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        for (next in adjacency[node]) if (!seen[next]) {
            seen[next] = true
            queue.addLast(next)
        }
    }
    if (!seen[groundIndex]) return null

    val keep = (0 until n).filter { it != groundIndex }
    val reducedIndex = keep.withIndex().associate { it.value to it.index }
    val matrix = Array(keep.size) { r -> DoubleArray(keep.size) { c -> g[keep[r]][keep[c]] } }
    val rhs = DoubleArray(keep.size)
    reducedIndex[startIndex]?.let { rhs[it] = 1.0 }
    val solution = solveLinearSystem(matrix, rhs)
    val voltage = reducedIndex[startIndex]?.let { solution[it] } ?: 0.0
    return voltage.toFloat().takeIf { it.isFinite() && it >= 0f }
}


private fun sourceConnectedToProbes(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    probeA: TerminalRef,
    probeB: TerminalRef,
): Boolean {
    val uf = UnionFind()
    fun key(id: Long, index: Int): String = terminalKey(id, index)
    for (component in components) {
        for (index in component.type.terminalOffsets.indices) uf.add(key(component.id, index))
    }
    for (wire in wires) uf.union(key(wire.a.componentId, wire.a.index), key(wire.b.componentId, wire.b.index))
    for (component in components) {
        if (component.damaged || component.type.isPowerSource) continue
        val passive = when (component.type) {
            InstrumentType.RESISTOR,
            InstrumentType.RHEOSTAT,
            InstrumentType.RESISTANCE_BOX,
            InstrumentType.RESISTANCE_WIRE,
            InstrumentType.LAMP,
            InstrumentType.TEMP_LAMP,
            InstrumentType.INDUCTOR,
            InstrumentType.MOTOR,
            InstrumentType.BUZZER,
            InstrumentType.LED,
            InstrumentType.ELECTROMAGNET,
            InstrumentType.SOLENOID,
            -> true
            InstrumentType.SWITCH -> component.closed
            else -> false
        }
        if (passive && component.type.terminalOffsets.size > 1) {
            uf.union(key(component.id, 0), key(component.id, 1))
        }
    }
    val roots = setOf(uf.find(key(probeA.componentId, probeA.index)), uf.find(key(probeB.componentId, probeB.index)))
    return components.any { source ->
        if (!source.type.isPowerSource || source.damaged) return@any false
        val terminals = source.type.terminalOffsets.indices
        val wired = terminals.count { index ->
            wires.any { wire ->
                wire.a.componentId == source.id && wire.a.index == index ||
                    wire.b.componentId == source.id && wire.b.index == index
            }
        }
        if (wired < 2) return@any false
        terminals.any { index -> uf.find(key(source.id, index)) in roots }
    }
}


private fun attachMultimeterMeasurements(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    result: CircuitSolution,
): CircuitSolution {
    val meters = components.filter { it.type == InstrumentType.MULTIMETER }
    if (meters.isEmpty()) return result
    val readings = result.readings.toMutableMap()
    for (meter in meters) {
        val range = meter.multimeterRangeValue
        if (meter.multimeterIsVoltage || meter.multimeterIsCurrent) continue
        val powered = components.any {
            it.type.isPowerSource && !it.damaged && (result.readings[it.id]?.current ?: 0f) > 0.00001f
        } && sourceConnectedToProbes(
            components,
            wires,
            TerminalRef(meter.id, 1),
            TerminalRef(meter.id, 0),
        )
        if (powered) {
            readings[meter.id] = (readings[meter.id] ?: ComponentReading()).copy(
                multimeterMode = meter.multimeterMode,
                multimeterDisplay = "请断电测量",
                multimeterWarning = "请断电测量",
                meterRangeLabel = range.label,
                meterRange = range.max,
                meterOl = false,
            )
            continue
        }
        val hasA = wires.any { it.a.componentId == meter.id && it.a.index == 1 || it.b.componentId == meter.id && it.b.index == 1 }
        val hasB = wires.any { it.a.componentId == meter.id && it.a.index == 0 || it.b.componentId == meter.id && it.b.index == 0 }
        if (!hasA || !hasB) {
            readings[meter.id] = (readings[meter.id] ?: ComponentReading()).copy(
                multimeterMode = meter.multimeterMode,
                multimeterDisplay = "OL",
                multimeterWarning = "",
                meterRangeLabel = range.label,
                meterRange = range.max,
                meterOl = true,
            )
            continue
        }

        val value = measureEquivalentResistance(
            components.filterNot { it.type == InstrumentType.MULTIMETER },
            wires,
            TerminalRef(meter.id, 1),
            TerminalRef(meter.id, 0),
        ) ?: Float.POSITIVE_INFINITY

        val display = when (meter.multimeterMode) {
            MultimeterMode.DIODE -> if (!value.isFinite()) "OL" else "1.00 V"
            MultimeterMode.RESISTANCE -> {
                val corrected = (value - meter.multimeterZeroOffset).coerceAtLeast(0f)
                if (corrected > range.max * 1.02f || !corrected.isFinite()) {
                    "OL"
                } else {
                    val scaled = when (range.unit) {
                        "kΩ" -> corrected / 1_000f
                        "MΩ" -> corrected / 1_000_000f
                        else -> corrected
                    }
                    String.format(java.util.Locale.US, "%.${range.decimals}f %s", scaled, range.unit)
                }
            }
            else -> "OL"
        }
        readings[meter.id] = (readings[meter.id] ?: ComponentReading()).copy(
            multimeterMode = meter.multimeterMode,
            multimeterDisplay = display,
            multimeterWarning = "",
            meterValue = if (value.isInfinite()) Float.MAX_VALUE else value,
            meterSignedValue = if (value.isInfinite()) Float.MAX_VALUE else value,
            meterRange = range.max,
            meterRangeLabel = range.label,
            meterOl = display == "OL",
        )
    }
    return result.copy(readings = readings)
}


fun simulateAcFrame(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    state: DynamicState,
    startTimeSeconds: Float,
    dt: Float = 0.001f,
    steps: Int = 20,
    epoch: Long = 0L,
): TransientResult {
    val count = steps.coerceAtLeast(1)
    var dynamic = state
    var last: CircuitSolution? = null
    val sumCurrent = HashMap<Long, Double>()
    val sumVoltage = HashMap<Long, Double>()
    val sumPower = HashMap<Long, Double>()
    val sumMeter = HashMap<Long, Double>()
    val damaged = linkedSetOf<Long>()

    for (i in 0 until count) {
        val solution = simulateCircuit(
            components = components,
            wires = wires,
            dynamicState = dynamic,
            dt = dt,
            epoch = epoch,
            timeSeconds = startTimeSeconds + dt * i,
        )
        dynamic = DynamicState(
            capacitorVoltages = solution.capacitorVoltages,
            inductorCurrents = solution.inductorCurrents,
        )
        last = solution
        damaged += solution.newlyDamaged
        for ((id, reading) in solution.readings) {
            sumCurrent[id] = (sumCurrent[id] ?: 0.0) + reading.current.toDouble() * reading.current
            sumVoltage[id] = (sumVoltage[id] ?: 0.0) + reading.voltage.toDouble() * reading.voltage
            sumPower[id] = (sumPower[id] ?: 0.0) + reading.power.toDouble() * reading.power
            sumMeter[id] = (sumMeter[id] ?: 0.0) + reading.meterSignedValue.toDouble() * reading.meterSignedValue
        }
    }

    val final = last ?: CircuitSolution(epoch = epoch)
    val readings = final.readings.mapValues { (id, reading) ->
        fun rms(sum: Double): Float = kotlin.math.sqrt(sum / count).toFloat()
        val rmsMeter = rms(sumMeter[id] ?: 0.0)
        val mm = components.firstOrNull { it.id == id }
        val mmDisplay = if (mm != null && mm.type == InstrumentType.MULTIMETER &&
            (mm.multimeterMode == MultimeterMode.AC_VOLTAGE || mm.multimeterMode == MultimeterMode.AC_CURRENT)
        ) {
            val range = mm.multimeterRangeValue
            if (rmsMeter > range.max * 1.02f) {
                "OL"
            } else {
                val scaled = if (mm.multimeterMode == MultimeterMode.AC_CURRENT && range.unit == "mA") {
                    rmsMeter * 1000f
                } else {
                    rmsMeter
                }
                String.format(java.util.Locale.US, "%.${range.decimals}f %s", scaled, range.unit)
            }
        } else {
            reading.multimeterDisplay
        }
        reading.copy(
            current = rms(sumCurrent[id] ?: 0.0),
            signedCurrent = rms(sumCurrent[id] ?: 0.0),
            voltage = rms(sumVoltage[id] ?: 0.0),
            power = rms(sumPower[id] ?: 0.0),
            meterSignedValue = rmsMeter,
            meterValue = rmsMeter,
            multimeterDisplay = mmDisplay,
        )
    }
    return TransientResult(
        solution = final.copy(
            readings = readings,
            totalCurrent = kotlin.math.sqrt(
                readings.values.sumOf { (it.current * it.current).toDouble() },
            ).toFloat(),
            newlyDamaged = damaged,
        ),
        state = dynamic,
    )
}

fun simulateTransientStep(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    state: DynamicState,
    dt: Float = 0.05f,
): TransientResult {
    val solution = simulateCircuit(components, wires, state, dt)
    return TransientResult(
        solution = solution,
        state = DynamicState(
            capacitorVoltages = solution.capacitorVoltages,
            inductorCurrents = solution.inductorCurrents,
        ),
    )
}

private fun PlacedInstrument.lampResistance(hotPower: Float?): Float {
    if (type != InstrumentType.TEMP_LAMP) return lampSpec.resistance
    val base = lampSpec.resistance
    val ratio = ((hotPower ?: 0f) / lampSpec.ratedPower).coerceIn(0f, 2.2f)
    // 冷态电阻较低，温度升高（功率增大）后电阻明显增大。
    return base * (0.55f + 0.65f * ratio)
}

private fun simulateCircuitOnce(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    hotPowers: Map<Long, Float>,
    dynamicState: DynamicState? = null,
    dt: Float = 0.05f,
    epoch: Long = 0L,
    timeSeconds: Float = 0f,
): CircuitSolution {
    if (components.isEmpty()) return CircuitSolution(epoch = epoch)
    val byId = components.associateBy { it.id }
    val uf = UnionFind()

    for (component in components) {
        for (index in component.type.terminalOffsets.indices) {
            uf.add(terminalKey(component.id, index))
        }
        if (component.type == InstrumentType.RHEOSTAT) {
            uf.add(internalKey(component.id, "S"))
            uf.union(terminalKey(component.id, 2), internalKey(component.id, "S"))
            uf.union(terminalKey(component.id, 3), internalKey(component.id, "S"))
        }
    }

    for (wire in wires) {
        if (byId[wire.a.componentId] == null || byId[wire.b.componentId] == null) continue
        uf.union(terminalKey(wire.a.componentId, wire.a.index), terminalKey(wire.b.componentId, wire.b.index))
    }

    val sources = components.filter { it.type.isPowerSource && !it.damaged }
    if (sources.isEmpty()) {
        val readings = components.associate { it.id to ComponentReading() }
        return CircuitSolution(
            status = CircuitStatus.NO_POWER,
            epoch = epoch,
            readings = readings,
            hasBattery = components.any { it.type.isPowerSource },
            batteryVoltage = 0f,
        )
    }

    val ground = uf.find(
        terminalKey(
            sources.first().id,
            if (sources.first().polarityFlipped) 1 else 0,
        ),
    )
    val netKeys = linkedSetOf<String>()
    for (component in components) {
        for (index in component.type.terminalOffsets.indices) {
            netKeys += uf.find(terminalKey(component.id, index))
        }
        if (component.type == InstrumentType.RHEOSTAT) {
            netKeys += uf.find(internalKey(component.id, "S"))
        }
    }
    val nets = netKeys.filter { it != ground }
    val indexOf = nets.withIndex().associate { (index, key) -> key to index }
    val n = nets.size
    val g = Array(n) { DoubleArray(n) }
    val b = DoubleArray(n)

    fun idx(key: String): Int = if (key == ground) -1 else indexOf[key] ?: -1

    fun addConductance(a: String, bKey: String, resistance: Float) {
        val ia = idx(a)
        val ib = idx(bKey)
        if (ia < 0 && ib < 0) return
        val conductance = 1.0 / resistance.coerceAtLeast(0.0005f).toDouble()
        if (ia >= 0) g[ia][ia] += conductance
        if (ib >= 0) g[ib][ib] += conductance
        if (ia >= 0 && ib >= 0) {
            g[ia][ib] -= conductance
            g[ib][ia] -= conductance
        }
    }

    fun addCurrent(from: String, to: String, amps: Double) {
        val iFrom = idx(from)
        val iTo = idx(to)
        if (iFrom >= 0) b[iFrom] -= amps
        if (iTo >= 0) b[iTo] += amps
    }

    val branches = mutableListOf<Branch>()
    val meterBranches = mutableListOf<MeterBranch>()
    val dynamicBranches = mutableListOf<DynamicBranch>()

    fun addBranch(componentId: Long, a: String, bKey: String, resistance: Float) {
        if (resistance <= 0f) return
        addConductance(a, bKey, resistance)
        branches += Branch(componentId, a, bKey, resistance)
    }

    val sourceBranches = mutableListOf<SourceBranch>()

    for (component in components) {
        if (component.damaged) continue
        val t0 = uf.find(terminalKey(component.id, 0))
        val t1 = if (component.type.terminalOffsets.size > 1) {
            uf.find(terminalKey(component.id, 1))
        } else {
            t0
        }
        val t2 = if (component.type.terminalOffsets.size > 2) {
            uf.find(terminalKey(component.id, 2))
        } else {
            t1
        }
        when {
            component.type.isPowerSource -> {
                val r = component.effectiveInternalResistance
                val positive = if (component.polarityFlipped) t0 else t1
                val negative = if (component.polarityFlipped) t1 else t0
                val instantVoltage = if (
                    component.type == InstrumentType.POWER_SUPPLY && component.supplyAc
                ) {
                    (component.supplyVoltage * kotlin.math.sqrt(2.0) *
                        kotlin.math.sin(2.0 * Math.PI * 50.0 * timeSeconds)).toFloat()
                } else {
                    component.voltage
                }
                addConductance(positive, negative, r)
                addCurrent(negative, positive, instantVoltage.toDouble() / r)
                sourceBranches += SourceBranch(
                    componentId = component.id,
                    positive = positive,
                    negative = negative,
                    voltage = instantVoltage,
                    internalResistance = r,
                )
            }

            component.type == InstrumentType.LAMP || component.type == InstrumentType.TEMP_LAMP ->
                addBranch(component.id, t0, t1, component.lampResistance(hotPowers[component.id]))

            component.type == InstrumentType.RESISTOR ->
                addBranch(component.id, t0, t1, component.resistance.coerceIn(1f, 10_000f))

            component.type == InstrumentType.SWITCH ->
                if (component.closed) addBranch(component.id, t0, t1, 0.001f)

            component.type == InstrumentType.AMMETER -> {
                val r1 = component.meterInternalResistance(1)
                val r2 = component.meterInternalResistance(2)
                addBranch(component.id, t1, t0, r1)
                addBranch(component.id, t2, t0, r2)
                meterBranches += MeterBranch(component.id, 1, t1, t0, r1, 0.6f)
                meterBranches += MeterBranch(component.id, 2, t2, t0, r2, 3f)
            }

            component.type == InstrumentType.VOLTMETER -> {
                val r1 = component.meterInternalResistance(1)
                val r2 = component.meterInternalResistance(2)
                addBranch(component.id, t1, t0, r1)
                addBranch(component.id, t2, t0, r2)
                meterBranches += MeterBranch(component.id, 1, t1, t0, r1, 3f)
                meterBranches += MeterBranch(component.id, 2, t2, t0, r2, 15f)
            }

            component.type == InstrumentType.GALVANOMETER -> {
                val r = component.meterInternalResistance(1)
                addBranch(component.id, t1, t0, r)
                meterBranches += MeterBranch(component.id, 1, t1, t0, r, 300f)
            }

            component.type == InstrumentType.MILLIAMMETER -> {
                val r = component.meterInternalResistance(1)
                addBranch(component.id, t1, t0, r)
                addBranch(component.id, t2, t0, r)
                meterBranches += MeterBranch(component.id, 1, t1, t0, r, 50f)
                meterBranches += MeterBranch(component.id, 2, t2, t0, r, 500f)
            }

            component.type == InstrumentType.MULTIMETER -> {
                val range = component.multimeterRangeValue
                if (component.multimeterIsVoltage) {
                    val r = 10_000_000f
                    addBranch(component.id, t1, t0, r)
                    meterBranches += MeterBranch(component.id, 1, t1, t0, r, range.max)
                } else if (component.multimeterIsCurrent) {
                    val r = component.meterInternalResistance(2)
                    addBranch(component.id, t2, t0, r)
                    meterBranches += MeterBranch(component.id, 2, t2, t0, r, range.max)
                }
            }

            component.type == InstrumentType.SPDT_SWITCH -> {
                val selected = when (component.switchPosition) {
                    0 -> uf.find(terminalKey(component.id, 1))
                    1 -> uf.find(terminalKey(component.id, 2))
                    else -> null
                }
                if (selected != null) {
                    addBranch(component.id, t0, selected, 0.001f)
                }
            }

            component.type == InstrumentType.CAPACITOR -> {
                if (dynamicState != null && dt > 0f) {
                    val capacitance = (component.capacitanceUf * 1e-6f).coerceAtLeast(1e-9f)
                    val g = capacitance / dt
                    val previous = dynamicState.capacitorVoltages[component.id] ?: 0f
                    addConductance(t0, t1, dt / capacitance)
                    addCurrent(t1, t0, (g * previous).toDouble())
                    dynamicBranches += DynamicBranch(
                        component.id, t0, t1, true, g, previous,
                    )
                }
            }

            component.type == InstrumentType.INDUCTOR -> {
                if (dynamicState != null && dt > 0f) {
                    val inductance = (component.inductanceMh * 1e-3f).coerceAtLeast(1e-6f)
                    val g = dt / inductance
                    val previous = dynamicState.inductorCurrents[component.id] ?: 0f
                    addConductance(t0, t1, inductance / dt)
                    // 电感支路电流方向为 t0 -> t1；初始电流应作为该方向的源注入。
                    addCurrent(t0, t1, previous.toDouble())
                    dynamicBranches += DynamicBranch(
                        component.id, t0, t1, false, g, previous,
                    )
                } else {
                    addBranch(component.id, t0, t1, component.inductanceDcResistance.coerceAtLeast(0.01f))
                }
            }

            component.type == InstrumentType.MOTOR -> addBranch(component.id, t0, t1, 12f)
            component.type == InstrumentType.BUZZER -> addBranch(component.id, t0, t1, 30f)
            component.type == InstrumentType.LED -> addBranch(component.id, t0, t1, 180f)
            component.type == InstrumentType.ELECTROMAGNET -> addBranch(component.id, t0, t1, 6f)
            component.type == InstrumentType.SOLENOID -> addBranch(component.id, t0, t1, 5f)
            component.type == InstrumentType.RESISTANCE_BOX ->
                addBranch(
                    component.id,
                    t0,
                    t1,
                    component.resistanceBoxResistance.coerceAtLeast(0.05f),
                )
            component.type == InstrumentType.RESISTANCE_WIRE ->
                addBranch(component.id, t0, t1, component.wireResistance.coerceAtLeast(0.01f))

            component.type == InstrumentType.RHEOSTAT -> {
                val a = uf.find(terminalKey(component.id, 0))
                val bKey = uf.find(terminalKey(component.id, 1))
                val s = uf.find(internalKey(component.id, "S"))
                val total = component.rheostatMax
                val f = component.slider.coerceIn(0.02f, 0.98f)
                addBranch(component.id, a, s, (total * f).coerceAtLeast(0.05f))
                addBranch(component.id, s, bKey, (total * (1f - f)).coerceAtLeast(0.05f))
            }

            else -> Unit
        }
    }

    for (i in 0 until n) {
        if (g[i][i] < 1e-12) g[i][i] = 1e-12
    }

    val voltages = solveLinearSystem(g, b)
    val netVoltage = HashMap<String, Double>()
    netVoltage[ground] = 0.0
    for ((index, key) in nets.withIndex()) {
        netVoltage[key] = voltages.getOrElse(index) { 0.0 }
    }

    fun v(key: String): Double = netVoltage[key] ?: 0.0

    val readings = components.associate { it.id to ComponentReading() }.toMutableMap()
    val componentCurrents = HashMap<Long, Double>()
    for (branch in branches) {
        val current = (v(branch.a) - v(branch.b)) / branch.resistance
        val existing = componentCurrents[branch.componentId] ?: 0.0
        if (kotlin.math.abs(current) > kotlin.math.abs(existing)) {
            componentCurrents[branch.componentId] = current
        }
    }

    var totalBatteryCurrent = 0.0
    for (source in sourceBranches) {
        val component = byId[source.componentId] ?: continue
        val current = source.voltage / source.internalResistance +
            (v(source.negative) - v(source.positive)) / source.internalResistance
        totalBatteryCurrent += kotlin.math.abs(current)
        val terminalVoltage = kotlin.math.abs(v(source.positive) - v(source.negative))
        readings[source.componentId] = ComponentReading(
            current = kotlin.math.abs(current).toFloat(),
            signedCurrent = current.toFloat(),
            voltage = terminalVoltage.toFloat(),
            power = (terminalVoltage * kotlin.math.abs(current)).toFloat(),
            energized = kotlin.math.abs(current) > 0.0005,
        )
    }

    for (component in components) {
        if (component.type.isPowerSource) continue
        if (component.damaged) {
            readings[component.id] = ComponentReading()
            continue
        }
        val terminalIndex = 0
        val t0 = uf.find(terminalKey(component.id, terminalIndex))
        val t1 = if (component.type.terminalOffsets.size > terminalIndex + 1) {
            uf.find(terminalKey(component.id, terminalIndex + 1))
        } else {
            t0
        }

        if (component.type == InstrumentType.MULTIMETER &&
            !component.multimeterIsVoltage && !component.multimeterIsCurrent
        ) {
            val powered = sources.isNotEmpty()
            readings[component.id] = ComponentReading(
                multimeterMode = component.multimeterMode,
                multimeterDisplay = if (powered) "请断电测量" else "OL",
                multimeterWarning = if (powered) "请断电测量" else "",
                meterRangeLabel = component.multimeterRangeValue.label,
                meterRange = component.multimeterRangeValue.max,
                meterOl = !powered,
            )
            continue
        }

        if (component.type.isMeter) {
            val candidates = meterBranches.filter { it.componentId == component.id }
            fun terminalWired(terminal: Int): Boolean = wires.any {
                (it.a.componentId == component.id && it.a.index == terminal) ||
                    (it.b.componentId == component.id && it.b.index == terminal)
            }

            val wired = candidates.filter { terminalWired(it.terminal) }
            val active = wired.maxByOrNull {
                kotlin.math.abs((v(it.positive) - v(it.common)) / it.resistance)
            } ?: candidates.maxByOrNull {
                kotlin.math.abs((v(it.positive) - v(it.common)) / it.resistance)
            } ?: candidates.first()

            val signedCurrent = (v(active.positive) - v(active.common)) / active.resistance
            val rawValue = when (component.type) {
                InstrumentType.AMMETER -> signedCurrent
                InstrumentType.MILLIAMMETER -> signedCurrent * 1000.0
                InstrumentType.GALVANOMETER -> signedCurrent * 1_000_000.0
                InstrumentType.MULTIMETER -> if (component.multimeterIsCurrent) {
                    signedCurrent
                } else {
                    signedCurrent * active.resistance
                }
                else -> signedCurrent * active.resistance
            }
            val value = kotlin.math.abs(rawValue).toFloat()
            val rangeLabel = when (component.type) {
                InstrumentType.AMMETER -> if (active.range <= 0.6f) "0.6A" else "3A"
                InstrumentType.MILLIAMMETER -> if (active.range <= 50f) "50mA" else "500mA"
                InstrumentType.GALVANOMETER -> "±300μA"
                InstrumentType.MULTIMETER -> component.multimeterRangeValue.label
                else -> if (active.range <= 3f) "3V" else "15V"
            }
            val isGalvanometer = component.type == InstrumentType.GALVANOMETER
            val isMultimeter = component.type == InstrumentType.MULTIMETER
            val multimeterRange = if (isMultimeter) component.multimeterRangeValue else null
            val multimeterDisplay = if (isMultimeter && multimeterRange != null) {
                if (value > multimeterRange.max * 1.02f) {
                    "OL"
                } else {
                    val scaled = when (component.multimeterMode) {
                        MultimeterMode.DC_CURRENT, MultimeterMode.AC_CURRENT ->
                            if (multimeterRange.unit == "mA") value * 1000f else value
                        MultimeterMode.RESISTANCE ->
                            when (multimeterRange.unit) {
                                "kΩ" -> value / 1_000f
                                "MΩ" -> value / 1_000_000f
                                else -> value
                            }
                        else -> value
                    }
                    String.format(
                        java.util.Locale.US,
                        "%.${multimeterRange.decimals}f %s",
                        scaled,
                        multimeterRange.unit,
                    )
                }
            } else ""
            readings[component.id] = ComponentReading(
                current = if (component.type == InstrumentType.AMMETER) {
                    value
                } else {
                    kotlin.math.abs(signedCurrent).toFloat()
                },
                signedCurrent = signedCurrent.toFloat(),
                voltage = kotlin.math.abs(v(active.positive) - v(active.common)).toFloat(),
                power = 0f,
                energized = false,
                meterValue = value,
                meterSignedValue = rawValue.toFloat(),
                meterRange = active.range,
                meterRangeLabel = rangeLabel,
                meterOverRange = value > active.range * (if (isGalvanometer) 1f else 1.02f),
                meterNegativeOverRange = !isGalvanometer &&
                    rawValue < -active.range / (if (component.type == InstrumentType.MILLIAMMETER) 5f else 6f),
                meterReversed = !isGalvanometer && rawValue < -1e-9,
                meterTerminal = active.terminal,
                multimeterMode = if (isMultimeter) component.multimeterMode else null,
                multimeterDisplay = multimeterDisplay,
            )
            continue
        }

        val signedCurrent = componentCurrents[component.id]?.toFloat() ?: 0f
        val current = kotlin.math.abs(signedCurrent)
        val voltage = kotlin.math.abs(v(t0) - v(t1)).toFloat()
        val resistance = when (component.type) {
            InstrumentType.LAMP, InstrumentType.TEMP_LAMP ->
                component.lampResistance(hotPowers[component.id])
            InstrumentType.RESISTOR -> component.resistance
            InstrumentType.SWITCH -> if (component.closed) 0.001f else 0f
            InstrumentType.MOTOR -> 12f
            InstrumentType.BUZZER -> 30f
            InstrumentType.LED -> 180f
            InstrumentType.ELECTROMAGNET -> 6f
            InstrumentType.SOLENOID -> 5f
            InstrumentType.RHEOSTAT -> component.rheostatMax
            InstrumentType.RESISTANCE_BOX -> component.resistanceBoxResistance.coerceAtLeast(0.05f)
            InstrumentType.RESISTANCE_WIRE -> component.wireResistance.coerceAtLeast(0.01f)
            else -> 0f
        }
        val ledReversed = component.type == InstrumentType.LED && signedCurrent < -0.0001f
        val energized = when (component.type) {
            InstrumentType.ELECTROMAGNET, InstrumentType.SOLENOID -> current > 0.005f
            InstrumentType.MOTOR -> current > 0.005f
            InstrumentType.BUZZER -> current > 0.005f
            InstrumentType.LED -> signedCurrent > 0.001f && current > 0.002f
            else -> false
        }
        readings[component.id] = ComponentReading(
            current = current,
            signedCurrent = signedCurrent,
            voltage = voltage,
            power = kotlin.math.abs(current * current * resistance),
            energized = energized,
            ledReversed = ledReversed,
        )
    }

    val capacitorVoltages = linkedMapOf<Long, Float>()
    val inductorCurrents = linkedMapOf<Long, Float>()
    for (branch in dynamicBranches) {
        val voltage = v(branch.a) - v(branch.b)
        val current = if (branch.isCapacitor) {
            branch.conductance * (voltage.toFloat() - branch.previous)
        } else {
            branch.previous + branch.conductance * voltage.toFloat()
        }
        readings[branch.componentId] = ComponentReading(
            current = kotlin.math.abs(current),
            signedCurrent = current,
            voltage = kotlin.math.abs(voltage).toFloat(),
            power = kotlin.math.abs(voltage * current).toFloat(),
            energized = kotlin.math.abs(current) > 0.00001f,
        )
        if (branch.isCapacitor) {
            capacitorVoltages[branch.componentId] = voltage.toFloat()
        } else {
            inductorCurrents[branch.componentId] = current
        }
    }

    val total = kotlin.math.abs(totalBatteryCurrent).toFloat()
    val hasLoopCurrent = readings.values.any {
        kotlin.math.abs(it.signedCurrent) > 0.00001f
    }
    val closed = total > 0.00001f || hasLoopCurrent
    val shortedSources = sources.filter { source ->
        val reading = readings[source.id] ?: return@filter false
        val externalResistance = if (reading.current > 0.0001f) {
            source.voltage / reading.current - source.effectiveInternalResistance
        } else {
            Float.MAX_VALUE
        }
        // 低阻负载（例如 2 Ω 电感）不是短路；只有外电路接近 0 Ω
        // 或电流极端大时才判为电源短路。
        reading.current > 8f || externalResistance < 0.35f
    }
    val short = shortedSources.isNotEmpty()
    val status = when {
        short -> CircuitStatus.SHORT
        !closed -> CircuitStatus.OPEN
        else -> CircuitStatus.CLOSED
    }

    val newlyDamaged = linkedSetOf<Long>()
    val reversedMeters = linkedSetOf<Long>()
    val overRangeMeters = linkedSetOf<Long>()
    val negativeOverRangeMeters = linkedSetOf<Long>()
    if (short) {
        shortedSources.forEach { newlyDamaged += it.id }
    }
    for (component in components) {
        if (component.damaged) continue
        if (component.type.isMeter) {
            val reading = readings[component.id]
            if (reading != null && reading.meterReversed &&
                (reading.current > 1e-6f || reading.meterValue > 0.001f)
            ) {
                reversedMeters += component.id
            }
            if (reading != null && reading.meterRange > 0f &&
                reading.meterValue > reading.meterRange * 1.2f
            ) {
                newlyDamaged += component.id
                overRangeMeters += component.id
            } else if (component.type != InstrumentType.GALVANOMETER &&
                reading != null && reading.meterRange > 0f &&
                reading.meterSignedValue < -reading.meterRange / 6f
            ) {
                newlyDamaged += component.id
                negativeOverRangeMeters += component.id
            }
            continue
        }
        if (component.type !in setOf(
                InstrumentType.LAMP,
                InstrumentType.TEMP_LAMP,
                InstrumentType.MOTOR,
                InstrumentType.BUZZER,
                InstrumentType.LED,
            )
        ) {
            continue
        }
        val rated = component.ratedPower() ?: continue
        val power = readings[component.id]?.power ?: 0f
        if (power > rated * 3f) newlyDamaged += component.id
    }

    return CircuitSolution(
        status = status,
        epoch = epoch,
        batteryVoltage = sources.first().voltage,
        totalCurrent = total,
        readings = readings,
        hasBattery = components.any { it.type.isPowerSource },
        solvedNodes = n,
        newlyDamaged = newlyDamaged,
        reversedMeters = reversedMeters,
        overRangeMeters = overRangeMeters,
        negativeOverRangeMeters = negativeOverRangeMeters,
        capacitorVoltages = capacitorVoltages,
        inductorCurrents = inductorCurrents,
    )
}

private fun terminalKey(id: Long, index: Int): String = "c$id:t$index"
private fun internalKey(id: Long, name: String): String = "c$id:i$name"

private fun solveLinearSystem(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
    val n = b.size
    if (n == 0) return DoubleArray(0)
    val m = Array(n) { i -> a[i].copyOf() }
    val x = b.copyOf()
    for (col in 0 until n) {
        var pivot = col
        var best = kotlin.math.abs(m[col][col])
        for (row in col + 1 until n) {
            val value = kotlin.math.abs(m[row][col])
            if (value > best) {
                best = value
                pivot = row
            }
        }
        if (best < 1e-14) continue
        if (pivot != col) {
            val tmpRow = m[col]
            m[col] = m[pivot]
            m[pivot] = tmpRow
            val tmp = x[col]
            x[col] = x[pivot]
            x[pivot] = tmp
        }
        val diagonal = m[col][col]
        for (row in col + 1 until n) {
            val factor = m[row][col] / diagonal
            if (factor == 0.0) continue
            for (k in col until n) m[row][k] -= factor * m[col][k]
            x[row] -= factor * x[col]
        }
    }
    val result = DoubleArray(n)
    for (row in n - 1 downTo 0) {
        if (kotlin.math.abs(m[row][row]) < 1e-14) {
            result[row] = 0.0
            continue
        }
        var sum = x[row]
        for (col in row + 1 until n) sum -= m[row][col] * result[col]
        result[row] = sum / m[row][row]
    }
    return result
}
