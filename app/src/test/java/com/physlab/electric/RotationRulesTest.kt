package com.physlab.electric

import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.isRotatable
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
}
