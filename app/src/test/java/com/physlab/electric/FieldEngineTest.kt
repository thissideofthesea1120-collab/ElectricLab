package com.physlab.electric

import androidx.compose.ui.geometry.Offset
import com.physlab.electric.model.FieldCharge
import com.physlab.electric.model.FieldEngine
import com.physlab.electric.model.FieldParticle
import org.junit.Test

class FieldEngineTest {
    @Test
    fun likeChargesRepelAndOppositeChargesAttract() {
        val a = FieldCharge(1, Offset(0.4f, 0.5f), 1f)
        val b = FieldCharge(2, Offset(0.6f, 0.5f), 1f)
        val repel = FieldEngine.forceBetween(a, b)
        check(repel.x > 0f)

        val c = FieldCharge(3, Offset(0.6f, 0.5f), -1f)
        val attract = FieldEngine.forceBetween(a, c)
        check(attract.x < 0f)
    }

    @Test
    fun fieldDirectionPointsAwayFromPositiveCharge() {
        val charge = FieldCharge(1, Offset(0.5f, 0.5f), 2f)
        val field = FieldEngine.electricFieldAt(Offset(0.7f, 0.5f), listOf(charge))
        check(field.x > 0f)
    }

    @Test
    fun inverseSquareLaw() {
        val charge = FieldCharge(1, Offset(0.2f, 0.5f), 1f)
        val near = FieldEngine.electricFieldAt(Offset(0.4f, 0.5f), listOf(charge)).getDistance()
        val far = FieldEngine.electricFieldAt(Offset(0.6f, 0.5f), listOf(charge)).getDistance()
        check(near / far in 3.5f..4.5f)
    }

    @Test
    fun particleAcceleratesAlongField() {
        val charge = FieldCharge(1, Offset(0.3f, 0.5f), 2f)
        val particle = FieldParticle(
            id = 2,
            position = Offset(0.6f, 0.5f),
            velocity = Offset.Zero,
            chargeMicroC = 1f,
        )
        val stepped = FieldEngine.stepParticles(listOf(particle), listOf(charge), 0.016f).single()
        check(stepped.position.x > particle.position.x)
        check(stepped.trail.isNotEmpty())
    }
}
