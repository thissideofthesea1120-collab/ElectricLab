package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.DynamicState
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.MultimeterMode
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.simulateCircuit
import org.junit.Test

class MultimeterRegressionTest {
    @Test
    fun dcVoltageDivider() {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(0f, 0f), batteryCells = 1)
        val r1 = PlacedInstrument(2, InstrumentType.RESISTOR, Offset(50f, 0f), resistance = 10f)
        val r2 = PlacedInstrument(3, InstrumentType.RESISTOR, Offset(100f, 0f), resistance = 10f)
        val mm = PlacedInstrument(
            4, InstrumentType.MULTIMETER, Offset(150f, 0f),
            multimeterMode = MultimeterMode.DC_VOLTAGE, multimeterRangeIndex = 0,
        )
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(11, TerminalRef(2, 1), TerminalRef(3, 0), WireColor.RED),
            WireSegment(12, TerminalRef(3, 1), TerminalRef(1, 0), WireColor.BLACK),
            WireSegment(13, TerminalRef(4, 1), TerminalRef(3, 0), WireColor.RED),
            WireSegment(14, TerminalRef(4, 0), TerminalRef(3, 1), WireColor.BLACK),
        )
        val s = simulateCircuit(listOf(battery, r1, r2, mm), wires, DynamicState(), 0.05f)
        println("MM DCV display=${s.readings[4]?.multimeterDisplay} signed=${s.readings[4]?.meterSignedValue}")
        check((s.readings[4]?.meterSignedValue ?: 0f) in 0.70f..0.80f)
    }

    @Test
    fun resistanceWithNoPower() {
        val resistor = PlacedInstrument(1, InstrumentType.RESISTOR, Offset(0f, 0f), resistance = 10f)
        val mm = PlacedInstrument(
            2, InstrumentType.MULTIMETER, Offset(100f, 0f),
            multimeterMode = MultimeterMode.RESISTANCE, multimeterRangeIndex = 0,
        )
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 0), TerminalRef(2, 1), WireColor.RED),
            WireSegment(11, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.BLACK),
        )
        val s = simulateCircuit(listOf(resistor, mm), wires, DynamicState(), 0.05f)
        println("MM OHM display=${s.readings[2]?.multimeterDisplay}")
        check(s.readings[2]?.multimeterDisplay == "10.0 Ω") {
            "expected 10.0 Ω, got ${s.readings[2]?.multimeterDisplay}"
        }
    }

    @Test
    fun poweredResistanceWarnsInsteadOfReading() {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(0f, 0f), batteryCells = 1)
        val resistor = PlacedInstrument(2, InstrumentType.RESISTOR, Offset(100f, 0f), resistance = 10f)
        val mm = PlacedInstrument(
            3, InstrumentType.MULTIMETER, Offset(200f, 0f),
            multimeterMode = MultimeterMode.RESISTANCE, multimeterRangeIndex = 0,
        )
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(11, TerminalRef(2, 1), TerminalRef(1, 0), WireColor.BLACK),
            WireSegment(12, TerminalRef(3, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(13, TerminalRef(3, 0), TerminalRef(2, 1), WireColor.BLACK),
        )
        val s = simulateCircuit(listOf(battery, resistor, mm), wires, DynamicState(), 0.05f)
        println("MM POWERED display=${s.readings[3]?.multimeterDisplay}")
        check(s.readings[3]?.multimeterDisplay == "请断电测量")
    }

    @Test
    fun dcCurrentInSeries() {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(0f, 0f), batteryCells = 1)
        val resistor = PlacedInstrument(2, InstrumentType.RESISTOR, Offset(100f, 0f), resistance = 10f)
        val mm = PlacedInstrument(
            3, InstrumentType.MULTIMETER, Offset(200f, 0f),
            multimeterMode = MultimeterMode.DC_CURRENT, multimeterRangeIndex = 2,
        )
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 1), TerminalRef(3, 2), WireColor.RED),
            WireSegment(11, TerminalRef(3, 0), TerminalRef(2, 0), WireColor.BLACK),
            WireSegment(12, TerminalRef(2, 1), TerminalRef(1, 0), WireColor.BLACK),
        )
        val s = simulateCircuit(listOf(battery, resistor, mm), wires, DynamicState(), 0.05f)
        println("MM DCA display=${s.readings[3]?.multimeterDisplay}")
        check(s.readings[3]?.multimeterDisplay?.endsWith("mA") == true)
    }

    @Test
    fun acVoltageMeasuredAsRms() {
        val source = PlacedInstrument(
            1, InstrumentType.POWER_SUPPLY, Offset(0f, 0f),
            supplyVoltage = 12f, supplyAc = true,
        )
        val resistor = PlacedInstrument(2, InstrumentType.RESISTOR, Offset(100f, 0f), resistance = 100f)
        val mm = PlacedInstrument(
            3, InstrumentType.MULTIMETER, Offset(200f, 0f),
            multimeterMode = MultimeterMode.AC_VOLTAGE, multimeterRangeIndex = 1,
        )
        val wires = listOf(
            WireSegment(10, TerminalRef(1, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(11, TerminalRef(2, 1), TerminalRef(1, 0), WireColor.BLACK),
            WireSegment(12, TerminalRef(3, 1), TerminalRef(2, 0), WireColor.RED),
            WireSegment(13, TerminalRef(3, 0), TerminalRef(2, 1), WireColor.BLACK),
        )
        val result = com.physlab.electric.model.simulateAcFrame(
            components = listOf(source, resistor, mm),
            wires = wires,
            state = DynamicState(),
            startTimeSeconds = 0f,
            dt = 0.001f,
            steps = 20,
        )
        println("MM ACV display=${result.solution.readings[3]?.multimeterDisplay}")
        check(result.solution.readings[3]?.multimeterDisplay?.endsWith("V") == true)
    }
}
