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

class ParallelDirectionTest {
    @Test
    fun printParallelSignedCurrents() {
        val battery = PlacedInstrument(37, InstrumentType.BATTERY, Offset(344.7f, 263.6f), batteryCells = 1)
        val switch = PlacedInstrument(38, InstrumentType.SWITCH, Offset(430.1f, 73.2f), closed = true)
        val l1 = PlacedInstrument(39, InstrumentType.LAMP, Offset(632.9f, 73.2f), lampSpec = LampSpec.V2_5_0_3)
        val l2 = PlacedInstrument(40, InstrumentType.LAMP, Offset(632.9f, 168.4f), lampSpec = LampSpec.V2_5_0_3)
        val meter = PlacedInstrument(41, InstrumentType.AMMETER, Offset(793.0f, 263.6f))
        val components = listOf(battery, switch, l1, l2, meter)
        val wires = listOf(
            WireSegment(1, TerminalRef(37, 1), TerminalRef(38, 0), WireColor.RED),
            WireSegment(2, TerminalRef(38, 1), TerminalRef(39, 0), WireColor.RED),
            WireSegment(3, TerminalRef(40, 1), TerminalRef(41, 2), WireColor.RED),
            WireSegment(4, TerminalRef(41, 0), TerminalRef(37, 0), WireColor.BLACK),
            WireSegment(5, TerminalRef(40, 0), TerminalRef(39, 0), WireColor.RED),
            WireSegment(6, TerminalRef(40, 1), TerminalRef(39, 1), WireColor.RED),
        )
        val s = simulateCircuit(components, wires, DynamicState(), 0.05f)
        println("STATUS=${s.status} total=${s.totalCurrent}")
        s.readings.forEach { (id, r) ->
            println("R $id signed=${r.signedCurrent} current=${r.current} meterTerminal=${r.meterTerminal}")
        }
    }
}
