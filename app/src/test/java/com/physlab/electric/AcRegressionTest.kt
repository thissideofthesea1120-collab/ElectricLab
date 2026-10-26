package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.DynamicState
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.simulateAcFrame
import org.junit.Test

class AcRegressionTest {
    @Test
    fun twelveVoltAcThroughCapacitorDrivesLamp() {
        val source = PlacedInstrument(
            1, InstrumentType.POWER_SUPPLY, Offset(100f, 100f),
            supplyVoltage = 12f, supplyAc = true,
        )
        val switch = PlacedInstrument(2, InstrumentType.SWITCH, Offset(200f, 100f), closed = true)
        val lamp = PlacedInstrument(3, InstrumentType.LAMP, Offset(300f, 100f), lampSpec = LampSpec.V2_5_0_3)
        val capacitor = PlacedInstrument(4, InstrumentType.CAPACITOR, Offset(400f, 100f), capacitanceUf = 470f)
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(11, TerminalRef(2, 1), TerminalRef(3, 0), WireColor.RED),
            WireSegment(12, TerminalRef(3, 1), TerminalRef(4, 0), WireColor.RED),
            WireSegment(13, TerminalRef(4, 1), TerminalRef(1, 0), WireColor.BLACK),
        )
        val result = simulateAcFrame(
            components = listOf(source, switch, lamp, capacitor),
            wires = wires,
            state = DynamicState(),
            startTimeSeconds = 0f,
            dt = 0.001f,
            steps = 20,
        )
        println("AC STATUS=${result.solution.status} DAMAGED=${result.solution.newlyDamaged}")
        result.solution.readings.forEach { (id, reading) ->
            println("AC READ $id current=${reading.current} voltage=${reading.voltage} power=${reading.power}")
        }
        check(result.solution.readings[3]?.power ?: 0f > 0.75f) {
            "lamp should receive more than rated power in this AC circuit"
        }
    }
}
