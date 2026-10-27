package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.ui.FieldEditorState
import org.junit.Test

class FieldEditorStateTest {
    @Test
    fun anyChargeCanBeSelected() {
        val state = FieldEditorState()
        state.addCharge(1f)
        state.addCharge(-1f)
        val first = state.charges[0]
        val second = state.charges[1]
        state.selectAt(first.position)
        check(state.selectedChargeId == first.id)
        state.selectAt(second.position)
        check(state.selectedChargeId == second.id)
    }

    @Test
    fun blankDragClearsSelection() {
        val state = FieldEditorState()
        state.addCharge(1f)
        state.selectAt(state.charges.first().position)
        check(state.selectedChargeId != null)
        state.selectAt(Offset(0.95f, 0.95f))
        check(state.selectedChargeId == null)
        check(state.selectedParticleId == null)
    }

    @Test
    fun sourceChargesStayFixedDuringSimulation() {
        val state = FieldEditorState()
        state.addCharge(1f)
        val before = state.charges.map { it.position }
        repeat(100) { state.step(0.016f) }
        check(state.charges.map { it.position } == before)
    }
}
