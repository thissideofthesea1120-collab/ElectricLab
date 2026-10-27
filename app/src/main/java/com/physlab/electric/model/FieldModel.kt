package com.physlab.electric.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 独立的电场/粒子模型。坐标使用 0..1 的实验台比例坐标，
 * 不参与电路 MNA，避免影响现有电路实验。
 */
data class FieldCharge(
    val id: Long,
    val position: Offset,
    val chargeMicroC: Float,
    val label: String = "",
)

enum class ParticleKind {
    ELECTRON,
    PROTON,
    ALPHA,
    CUSTOM,
}

enum class ParticleStatus {
    RUNNING,
    EXITED_PLATE,
    HIT_PLATE,
    LEFT_BOARD,
    STOPPED,
}

data class ParticleSource(
    val id: Long,
    val position: Offset,
    val directionDegrees: Float,
    val kind: ParticleKind,
    val speed: Float,
    val label: String = "",
)

data class FieldParticle(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val chargeMicroC: Float,
    val mass: Float = 1f,
    val trail: List<Offset> = emptyList(),
    val label: String = "",
    val initialPosition: Offset = position,
    val initialVelocity: Offset = velocity,
    val kind: ParticleKind = ParticleKind.CUSTOM,
    val chargeElementary: Float = chargeMicroC,
    val massKg: Float = 1.67e-27f,
    val effectiveMass: Float = mass,
    val status: ParticleStatus = ParticleStatus.RUNNING,
)

data class UniformField(
    val id: Long,
    val center: Offset,
    val width: Float,
    val height: Float,
    val directionDegrees: Float,
    val strength: Float,
    val label: String = "",
)

data class ParallelPlate(
    val id: Long,
    val center: Offset,
    val rotation: Float,
    val separation: Float,
    val length: Float,
    val voltage: Float,
    val width: Float = 0.05f,
    val label: String = "",
)

data class FieldScene(
    val charges: List<FieldCharge> = emptyList(),
    val particles: List<FieldParticle> = emptyList(),
    val uniformFields: List<UniformField> = emptyList(),
    val parallelPlates: List<ParallelPlate> = emptyList(),
    val particleSources: List<ParticleSource> = emptyList(),
)

object FieldEngine {
    /** 归一化坐标下的教学用耦合常数，保证动画稳定且方向正确。 */
    const val K = 0.018f
    private const val MIN_DISTANCE_SQ = 0.0004f
    private const val MAX_ACCELERATION = 24f
    private const val MAX_SPEED = 1.5f

    fun electricFieldAt(
        point: Offset,
        charges: List<FieldCharge>,
        uniformFields: List<UniformField> = emptyList(),
        parallelPlates: List<ParallelPlate> = emptyList(),
    ): Offset {
        var ex = 0f
        var ey = 0f
        for (charge in charges) {
            val dx = point.x - charge.position.x
            val dy = point.y - charge.position.y
            val r2 = (dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SQ)
            val r = sqrt(r2)
            val factor = K * charge.chargeMicroC / (r2 * r)
            ex += dx * factor
            ey += dy * factor
        }
        for (field in uniformFields) {
            val dx = point.x - field.center.x
            val dy = point.y - field.center.y
            if (abs(dx) <= field.width / 2f && abs(dy) <= field.height / 2f) {
                val angle = Math.toRadians(field.directionDegrees.toDouble())
                ex += cos(angle).toFloat() * field.strength
                ey += sin(angle).toFloat() * field.strength
            }
        }
        for (plate in parallelPlates) {
            val local = rotate(point - plate.center, -plate.rotation)
            if (abs(local.x) <= plate.length / 2f && abs(local.y) <= plate.separation / 2f) {
                val strength = (plate.voltage / plate.separation.coerceAtLeast(0.01f)) * 0.0025f
                val localField = Offset(0f, strength)
                val world = rotate(localField, plate.rotation)
                ex += world.x
                ey += world.y
            }
        }
        return Offset(ex, ey)
    }

    fun forceAt(
        point: Offset,
        chargeMicroC: Float,
        charges: List<FieldCharge>,
        uniformFields: List<UniformField> = emptyList(),
        parallelPlates: List<ParallelPlate> = emptyList(),
    ): Offset {
        val field = electricFieldAt(point, charges, uniformFields, parallelPlates)
        return Offset(field.x * chargeMicroC, field.y * chargeMicroC)
    }

    fun forceBetween(a: FieldCharge, b: FieldCharge): Offset {
        val dx = b.position.x - a.position.x
        val dy = b.position.y - a.position.y
        val r2 = (dx * dx + dy * dy).coerceAtLeast(MIN_DISTANCE_SQ)
        val r = sqrt(r2)
        val factor = K * a.chargeMicroC * b.chargeMicroC / (r2 * r)
        return Offset(dx * factor, dy * factor)
    }

    private fun rotate(value: Offset, degrees: Float): Offset {
        if (degrees == 0f) return value
        val radians = Math.toRadians(degrees.toDouble())
        val c = cos(radians).toFloat()
        val s = sin(radians).toFloat()
        return Offset(value.x * c - value.y * s, value.x * s + value.y * c)
    }

    fun stepParticles(
        particles: List<FieldParticle>,
        charges: List<FieldCharge>,
        dt: Float,
        uniformFields: List<UniformField> = emptyList(),
        parallelPlates: List<ParallelPlate> = emptyList(),
    ): List<FieldParticle> {
        if (particles.isEmpty()) return particles
        val safeDt = dt.coerceIn(0f, 0.03f)
        return particles.map { particle ->
            if (particle.status != ParticleStatus.RUNNING) return@map particle
            val force = forceAt(particle.position, particle.chargeMicroC, charges, uniformFields, parallelPlates)
            var ax = force.x / particle.effectiveMass.coerceAtLeast(0.01f)
            var ay = force.y / particle.effectiveMass.coerceAtLeast(0.01f)
            val accel = sqrt(ax * ax + ay * ay)
            if (accel > MAX_ACCELERATION) {
                val scale = MAX_ACCELERATION / accel
                ax *= scale
                ay *= scale
            }
            var vx = particle.velocity.x + ax * safeDt
            var vy = particle.velocity.y + ay * safeDt
            val speed = sqrt(vx * vx + vy * vy)
            if (speed > MAX_SPEED) {
                val scale = MAX_SPEED / speed
                vx *= scale
                vy *= scale
            }
            val rawNext = Offset(
                particle.position.x + vx * safeDt,
                particle.position.y + vy * safeDt,
            )
            val next = Offset(
                rawNext.x.coerceIn(0.02f, 0.98f),
                rawNext.y.coerceIn(0.04f, 0.96f),
            )
            val trail = if (particle.trail.isEmpty() ||
                (next - particle.trail.last()).getDistance() > 0.004f
            ) {
                (particle.trail + next).takeLast(360)
            } else {
                particle.trail
            }
            val hit = parallelPlates.firstNotNullOfOrNull { plate ->
                hitPlate(particle.position, next, plate)
            }
            if (hit != null) {
                return@map particle.copy(
                    position = hit,
                    velocity = Offset.Zero,
                    trail = trail,
                    status = ParticleStatus.HIT_PLATE,
                )
            }
            val exitedPlate = parallelPlates.any { plate ->
                val oldLocal = localPoint(particle.position, plate)
                val newLocal = localPoint(next, plate)
                oldLocal.x <= plate.length / 2f &&
                    newLocal.x > plate.length / 2f &&
                    abs(newLocal.y) <= plate.separation / 2f
            }
            val leftBoard = rawNext.x !in 0f..1f || rawNext.y !in 0f..1f
            particle.copy(
                position = next,
                velocity = Offset(vx, vy),
                trail = trail,
                status = when {
                    exitedPlate -> ParticleStatus.EXITED_PLATE
                    leftBoard -> ParticleStatus.LEFT_BOARD
                    else -> ParticleStatus.RUNNING
                },
            )
        }
    }

    private fun localPoint(point: Offset, plate: ParallelPlate): Offset =
        rotate(point - plate.center, -plate.rotation)

    private fun hitPlate(old: Offset, next: Offset, plate: ParallelPlate): Offset? {
        val oldLocal = localPoint(old, plate)
        val newLocal = localPoint(next, plate)
        val halfLength = plate.length / 2f
        val halfSeparation = plate.separation / 2f
        val thickness = (plate.width * 0.5f).coerceAtLeast(0.002f)
        for (surface in listOf(-halfSeparation, halfSeparation)) {
            val oldSide = oldLocal.y - surface
            val newSide = newLocal.y - surface
            val crosses = oldSide == 0f || newSide == 0f || (oldSide < 0f) != (newSide < 0f)
            if (!crosses) {
                if (abs(newLocal.y - surface) <= thickness && abs(newLocal.x) <= halfLength) {
                    return rotate(Offset(newLocal.x, surface), plate.rotation) + plate.center
                }
                continue
            }
            val dy = newLocal.y - oldLocal.y
            if (abs(dy) < 0.00001f) continue
            val t = ((surface - oldLocal.y) / dy).coerceIn(0f, 1f)
            val x = oldLocal.x + (newLocal.x - oldLocal.x) * t
            if (abs(x) <= halfLength) {
                return rotate(Offset(x, surface), plate.rotation) + plate.center
            }
        }
        return null
    }

    fun totalForceOn(index: Int, charges: List<FieldCharge>): Offset {
        val target = charges.getOrNull(index) ?: return Offset.Zero
        var fx = 0f
        var fy = 0f
        for (other in charges) {
            if (other.id == target.id) continue
            val f = forceBetween(target, other)
            fx += f.x
            fy += f.y
        }
        return Offset(fx, fy)
    }

    fun fieldLine(
        start: Offset,
        direction: Float,
        charges: List<FieldCharge>,
        uniformFields: List<UniformField> = emptyList(),
        parallelPlates: List<ParallelPlate> = emptyList(),
        steps: Int = 90,
    ): List<Offset> {
        val points = mutableListOf(start)
        var point = start
        repeat(steps) {
            val field = electricFieldAt(point, charges, uniformFields, parallelPlates)
            val length = field.getDistance()
            if (length < 0.00001f) return@repeat
            point = point + Offset(field.x / length, field.y / length) * (0.012f * direction)
            if (point.x !in 0f..1f || point.y !in 0f..1f) return points
            points += point
            val nearNegative = charges.any {
                it.chargeMicroC < 0f && (it.position - point).getDistance() < 0.012f
            }
            if (nearNegative) return points
        }
        return points
    }

    fun isNearCharge(point: Offset, charges: List<FieldCharge>, radius: Float = 0.035f): Boolean =
        charges.any { (it.position - point).getDistance() < radius }

    fun clampCharge(value: Float): Float =
        value.coerceIn(-5f, 5f).let { if (abs(it) < 0.1f) 0.1f else it }
}
