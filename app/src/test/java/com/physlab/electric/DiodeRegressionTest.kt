package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.simulateCircuit
import org.junit.Test

class DiodeRegressionTest {
    private fun circuit(reverse: Boolean) = run {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(100f, 100f), batteryCells = 2)
        val resistor = PlacedInstrument(2, InstrumentType.RESISTOR, Offset(220f, 100f), resistance = 100f)
        val diode = PlacedInstrument(3, InstrumentType.DIODE, Offset(340f, 100f))
        val components = listOf(battery, resistor, diode)
        val wires = if (reverse) {
            listOf(
                WireSegment(1, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
                WireSegment(2, TerminalRef(2, 1), TerminalRef(3, 1), WireColor.RED),
                WireSegment(3, TerminalRef(3, 0), TerminalRef(1, 0), WireColor.BLACK),
            )
        } else {
            listOf(
                WireSegment(1, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
                WireSegment(2, TerminalRef(2, 1), TerminalRef(3, 0), WireColor.RED),
                WireSegment(3, TerminalRef(3, 1), TerminalRef(1, 0), WireColor.BLACK),
            )
        }
        simulateCircuit(components, wires)
    }

    @Test
    fun diodeConductsForward() {
        val result = circuit(reverse = false)
        val current = result.readings[3]?.current ?: 0f
        check(current > 0.005f) { "forward current=$current" }
    }

    @Test
    fun diodeBlocksReverse() {
        val result = circuit(reverse = true)
        val current = result.readings[3]?.current ?: 0f
        check(current < 0.001f) { "reverse current=$current" }
    }
}
