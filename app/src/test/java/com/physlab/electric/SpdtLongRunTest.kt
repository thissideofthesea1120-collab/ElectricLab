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

class SpdtLongRunTest {
    @Test
    fun rightThrowLongRunDoesNotDamage() {
        val battery = PlacedInstrument(10, InstrumentType.BATTERY, Offset(437f, 267f), batteryCells = 2)
        val switch = PlacedInstrument(11, InstrumentType.SPDT_SWITCH, Offset(312f, 103f), switchPosition = 1)
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
        var state = DynamicState()
        repeat(300) { step ->
            val solution = simulateCircuit(components, wires, state, 0.05f)
            state = DynamicState(solution.capacitorVoltages, solution.inductorCurrents)
            if (solution.newlyDamaged.isNotEmpty()) {
                println("LONG step=$step status=${solution.status} damaged=${solution.newlyDamaged} total=${solution.totalCurrent}")
                solution.readings.forEach { (id, r) ->
                    println("LONG READ $id current=${r.current} voltage=${r.voltage} power=${r.power}")
                }
            }
        }
        println("LONG DONE state=$state")
    }
}
