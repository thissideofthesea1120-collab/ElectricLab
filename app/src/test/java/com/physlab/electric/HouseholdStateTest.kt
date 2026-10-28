package com.physlab.electric

import com.physlab.electric.ui.HouseholdDeviceType
import com.physlab.electric.ui.HouseholdPreset
import com.physlab.electric.ui.HouseholdState
import org.junit.Test

class HouseholdStateTest {
    @Test
    fun twoWaySwitchTogglesLamp() {
        val state = HouseholdState()
        state.loadPreset(HouseholdPreset.TWO_WAY)
        check(state.lampOn) { "initial two-way circuit should be on" }

        val second = state.devices.first { it.type == HouseholdDeviceType.TWO_WAY }
        state.selectedId = second.id
        state.toggleSelectedSwitch()
        check(!state.lampOn) { "changing one traveler should turn lamp off" }

        state.toggleSelectedSwitch()
        check(state.lampOn) { "restoring traveler should turn lamp on" }
    }

    @Test
    fun socketPresetHasLiveSocketAndProtection() {
        val state = HouseholdState()
        state.loadPreset(HouseholdPreset.SOCKET)
        check(state.socketPowered)
        val socket = state.devices.first { it.type == HouseholdDeviceType.SOCKET3 }
        check(socket.type.terminals.size == 3)
        check(state.devices.any { it.type == HouseholdDeviceType.GROUND })
    }

    @Test
    fun breakerRatingTripsOverload() {
        val state = HouseholdState()
        state.loadPreset(HouseholdPreset.APPLIANCE)
        state.mainRatingA = 6f
        check(state.totalPower >= 2000f) { "appliance preset should have load, power=${state.totalPower}" }
        state.tick(1f)
        check(state.tripped) { "6A breaker should trip under appliance load" }
        check(!state.mainClosed)
    }

    @Test
    fun twoWaySwitchCanBeWiredFromPreset() {
        val state = HouseholdState()
        state.loadPreset(HouseholdPreset.TWO_WAY)
        check(state.devices.count { it.type == HouseholdDeviceType.TWO_WAY } == 2)
        check(state.warnings.isEmpty()) { "unexpected warnings=${state.warnings}" }
    }
}
