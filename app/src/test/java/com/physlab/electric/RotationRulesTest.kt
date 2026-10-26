package com.physlab.electric

import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.isRotatable
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import org.junit.Test

class RotationRulesTest {
    @Test
    fun rotationExclusionsAreCorrect() {
        val excluded = setOf(
            InstrumentType.POWER_SUPPLY,
            InstrumentType.AMMETER,
            InstrumentType.VOLTMETER,
            InstrumentType.GALVANOMETER,
            InstrumentType.MILLIAMMETER,
            InstrumentType.MULTIMETER,
            InstrumentType.COMPASS,
            InstrumentType.PAPERCLIPS,
        )
        for (type in InstrumentType.entries) {
            check(type.isRotatable == (type !in excluded)) {
                "${type.name} rotatable=${type.isRotatable}, expected=${type !in excluded}"
            }
        }
        check(InstrumentType.BATTERY.isRotatable)
        check(InstrumentType.STORAGE_BATTERY.isRotatable)
    }

    @Test
    fun rotatedTerminalFollowsInstrument() {
        val battery = PlacedInstrument(1, InstrumentType.BATTERY, Offset(100f, 100f), rotation = 90f)
        val left = battery.terminalPosition(0)
        val right = battery.terminalPosition(1)
        check(abs(left.x - 100f) < 0.01f)
        check(abs(right.x - 100f) < 0.01f)
        check(abs(left.y - 30f) < 0.01f)
        check(abs(right.y - 170f) < 0.01f)
    }
}
