package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.DynamicState
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.MultimeterMode
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.simulateCircuit
import org.junit.Test

class SnapshotDebugTest {
    @Test
    fun printSnapshot() {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(465.5f, 294.5f), batteryCells = 4)
        val mm = PlacedInstrument(2, InstrumentType.MULTIMETER, Offset(481.4f, 64.3f), multimeterMode = MultimeterMode.RESISTANCE)
        val resistor = PlacedInstrument(3, InstrumentType.RESISTOR, Offset(485.6f, 197.7f), resistance = 10f)
        val lamp = PlacedInstrument(10, InstrumentType.LAMP, Offset(640.4f, 165.6f), lampSpec = LampSpec.V2_5_0_3, damaged = true)
        val wires = listOf(
            WireSegment(4, TerminalRef(2, 1), TerminalRef(3, 1), WireColor.RED),
            WireSegment(5, TerminalRef(2, 0), TerminalRef(3, 0), WireColor.RED),
            WireSegment(13, TerminalRef(1, 0), TerminalRef(3, 0), WireColor.RED),
            WireSegment(14, TerminalRef(1, 1), TerminalRef(10, 1), WireColor.RED),
            WireSegment(15, TerminalRef(10, 0), TerminalRef(3, 1), WireColor.RED),
        )
        val s = simulateCircuit(listOf(battery, mm, resistor, lamp), wires, DynamicState(), 0.05f)
        println("SNAP status=${s.status} total=${s.totalCurrent} damaged=${s.newlyDamaged}")
        s.readings.forEach { (id, r) -> println("SNAP R $id current=${r.current} signed=${r.signedCurrent} display=${r.multimeterDisplay}") }
    }
}
