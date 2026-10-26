package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.DynamicState
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.simulateCircuit
import org.junit.Test
import kotlin.math.abs

class SpdtRegressionTest {
    @Test
    fun rightThrowDoesNotShortBattery() {
        val battery = PlacedInstrument(10, InstrumentType.BATTERY, Offset(437f, 267f), batteryCells = 2)
        val switch = PlacedInstrument(11, InstrumentType.SPDT_SWITCH, Offset(312f, 103f), switchPosition = 2)
        val inductor = PlacedInstrument(12, InstrumentType.INDUCTOR, Offset(503f, 23f), inductanceMh = 50f, inductanceDcResistance = 2f)
        val r1 = PlacedInstrument(13, InstrumentType.RESISTOR, Offset(621f, 296f), resistance = 10f)
        val lamp = PlacedInstrument(14, InstrumentType.LAMP, Offset(422f, 167f), lampSpec = LampSpec.V2_5_0_3)
        val r2 = PlacedInstrument(20, InstrumentType.RESISTOR, Offset(533f, 212f), resistance = 10f)
        val components = listOf(battery, switch, inductor, r1, lamp, r2)
        val wires = listOf(
            WireSegment(30, TerminalRef(12, 0), TerminalRef(11, 0), WireColor.RED),
            WireSegment(31, TerminalRef(11, 1), TerminalRef(10, 0), WireColor.RED),
            WireSegment(32, TerminalRef(10, 1), TerminalRef(13, 1), WireColor.RED),
            WireSegment(33, TerminalRef(13, 1), TerminalRef(12, 1), WireColor.RED),
            WireSegment(34, TerminalRef(11, 2), TerminalRef(14, 0), WireColor.RED),
            WireSegment(35, TerminalRef(14, 1), TerminalRef(20, 0), WireColor.RED),
            WireSegment(36, TerminalRef(20, 1), TerminalRef(13, 1), WireColor.RED),
        )
        val solution = simulateCircuit(components, wires, DynamicState(), 0.05f)
        println("STATUS=${solution.status} DAMAGED=${solution.newlyDamaged} TOTAL=${solution.totalCurrent}")
        solution.readings.forEach { (id, r) -> println("READ $id current=${r.current} signed=${r.signedCurrent} voltage=${r.voltage} power=${r.power}") }
        check(abs(solution.totalCurrent) < 0.01f) { "battery current should be ~0" }
    }

    @Test
    fun leftThrowLowResistanceInductorIsNotShort() {
        val battery = PlacedInstrument(10, InstrumentType.BATTERY, Offset(437f, 267f), batteryCells = 2)
        val switch = PlacedInstrument(11, InstrumentType.SPDT_SWITCH, Offset(312f, 103f), switchPosition = 0)
        val inductor = PlacedInstrument(12, InstrumentType.INDUCTOR, Offset(503f, 23f), inductanceMh = 50f, inductanceDcResistance = 2f)
        val r1 = PlacedInstrument(13, InstrumentType.RESISTOR, Offset(621f, 296f), resistance = 10f)
        val lamp = PlacedInstrument(14, InstrumentType.LAMP, Offset(422f, 167f), lampSpec = LampSpec.V2_5_0_3)
        val r2 = PlacedInstrument(20, InstrumentType.RESISTOR, Offset(533f, 212f), resistance = 10f)
        val components = listOf(battery, switch, inductor, r1, lamp, r2)
        val wires = listOf(
            WireSegment(30, TerminalRef(12, 0), TerminalRef(11, 0), WireColor.RED),
            WireSegment(31, TerminalRef(11, 1), TerminalRef(10, 0), WireColor.RED),
            WireSegment(32, TerminalRef(10, 1), TerminalRef(13, 1), WireColor.RED),
            WireSegment(33, TerminalRef(13, 1), TerminalRef(12, 1), WireColor.RED),
            WireSegment(34, TerminalRef(11, 2), TerminalRef(14, 0), WireColor.RED),
            WireSegment(35, TerminalRef(14, 1), TerminalRef(20, 0), WireColor.RED),
            WireSegment(36, TerminalRef(20, 1), TerminalRef(13, 1), WireColor.RED),
        )
        val solution = simulateCircuit(components, wires, DynamicState(), 0.05f)
        println("LEFT STATUS=${solution.status} DAMAGED=${solution.newlyDamaged} TOTAL=${solution.totalCurrent}")
        solution.readings.forEach { (id, r) -> println("LEFT READ $id current=${r.current} voltage=${r.voltage}") }
        check(solution.newlyDamaged.isEmpty()) { "low-resistance inductor must not damage battery" }
    }
}
