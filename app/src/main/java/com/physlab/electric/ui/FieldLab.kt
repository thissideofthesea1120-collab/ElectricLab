package com.physlab.electric.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.FieldCharge
import com.physlab.electric.model.FieldEngine
import com.physlab.electric.model.FieldParticle
import com.physlab.electric.model.ParallelPlate
import com.physlab.electric.model.ParticleKind
import com.physlab.electric.model.UniformField
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Stable
class FieldEditorState {
    var charges by mutableStateOf(
        listOf(
            FieldCharge(1, Offset(0.34f, 0.50f), 2f, "Q1"),
            FieldCharge(2, Offset(0.66f, 0.50f), -2f, "Q2"),
        ),
    )
    var particles by mutableStateOf(
        listOf(
            FieldParticle(
                id = 3,
                position = Offset(0.50f, 0.22f),
                velocity = Offset(0.12f, 0f),
                chargeMicroC = 1f,
                label = "P1",
                initialPosition = Offset(0.50f, 0.22f),
                initialVelocity = Offset(0.12f, 0f),
                kind = ParticleKind.PROTON,
                chargeElementary = 1f,
                massKg = 1.67e-27f,
                effectiveMass = 4f,
            ),
        ),
    )
    var uniformFields by mutableStateOf<List<UniformField>>(emptyList())
    var parallelPlates by mutableStateOf<List<ParallelPlate>>(emptyList())
    var running by mutableStateOf(false)
    var showFieldLines by mutableStateOf(true)
    var showForces by mutableStateOf(true)
    var selectedChargeId by mutableStateOf<Long?>(null)
    var selectedParticleId by mutableStateOf<Long?>(null)
    var selectedUniformFieldId by mutableStateOf<Long?>(null)
    var selectedParallelPlateId by mutableStateOf<Long?>(null)
    var viewZoom by mutableStateOf(1.8f)
    var viewPan by mutableStateOf(Offset.Zero)
    private var nextId = 10L
    private var initialParticles = particles

    fun addCharge(sign: Float) {
        val count = charges.size
        val position = Offset(
            (0.28f + (count % 4) * 0.16f).coerceIn(0.10f, 0.90f),
            (0.28f + ((count / 4) % 3) * 0.20f).coerceIn(0.10f, 0.88f),
        )
        val charge = FieldCharge(nextId++, position, sign * 2f, "Q${charges.size + 1}")
        charges = charges + charge
        selectedChargeId = charge.id
        selectedParticleId = null
        selectedUniformFieldId = null
        selectedParallelPlateId = null
    }

    fun addUniformField() {
        val field = UniformField(
            id = nextId++,
            center = Offset(0.50f, 0.52f),
            width = 0.42f,
            height = 0.28f,
            directionDegrees = 0f,
            strength = 0.05f,
            label = "E${uniformFields.size + 1}",
        )
        uniformFields = uniformFields + field
        selectedUniformFieldId = field.id
        selectedParallelPlateId = null
        selectedChargeId = null
        selectedParticleId = null
    }

    fun addParallelPlate() {
        val plate = ParallelPlate(
            id = nextId++,
            center = Offset(0.50f, 0.52f),
            rotation = 0f,
            separation = 0.18f,
            length = 0.46f,
            voltage = 12f,
            width = 0.05f,
            label = "板${parallelPlates.size + 1}",
        )
        parallelPlates = parallelPlates + plate
        selectedParallelPlateId = plate.id
        selectedUniformFieldId = null
        selectedChargeId = null
        selectedParticleId = null
    }

    fun addParticle(kind: ParticleKind) {
        val (charge, massKg, effectiveMass, label) = when (kind) {
            ParticleKind.ELECTRON -> Quad(-1f, 9.11e-31f, 1f, "e⁻")
            ParticleKind.PROTON -> Quad(1f, 1.67e-27f, 4f, "p⁺")
            ParticleKind.ALPHA -> Quad(2f, 6.64e-27f, 8f, "α")
            ParticleKind.CUSTOM -> Quad(1f, 1.67e-27f, 4f, "P${particles.size + 1}")
        }
        val start = Offset(0.50f, 0.20f)
        val velocity = Offset(0.15f, 0f)
        val particle = FieldParticle(
            id = nextId++,
            position = start,
            velocity = velocity,
            chargeMicroC = charge,
            mass = effectiveMass,
            label = label,
            initialPosition = start,
            initialVelocity = velocity,
            kind = kind,
            chargeElementary = charge,
            massKg = massKg,
            effectiveMass = effectiveMass,
        )
        particles = particles + particle
        selectedParticleId = particle.id
        selectedChargeId = null
        selectedUniformFieldId = null
        selectedParallelPlateId = null
        initialParticles = particles
    }

    fun addParticle(sign: Float) {
        addParticle(ParticleKind.CUSTOM)
    }

    fun selectAt(point: Offset) {
        val particle = particles.minByOrNull { (it.position - point).getDistanceSquared() }
        if (particle != null && (particle.position - point).getDistance() < 0.055f) {
            selectedParticleId = particle.id
            selectedChargeId = null
            return
        }
        val charge = charges.minByOrNull { (it.position - point).getDistanceSquared() }
        if (charge != null && (charge.position - point).getDistance() < 0.06f) {
            selectedChargeId = charge.id
            selectedParticleId = null
            return
        }
        selectedChargeId = null
        selectedParticleId = null
        selectedUniformFieldId = null
        selectedParallelPlateId = null
    }

    fun dragSelected(delta: Offset) {
        selectedParticleId?.let { id ->
            particles = particles.map {
                if (it.id == id) it.copy(position = clampPoint(it.position + delta)) else it
            }
            return
        }
        selectedChargeId?.let { id ->
            charges = charges.map {
                if (it.id == id) it.copy(position = clampPoint(it.position + delta)) else it
            }
        }
    }

    fun updateSelectedCharge(value: Float) {
        val id = selectedChargeId ?: return
        charges = charges.map { if (it.id == id) it.copy(chargeMicroC = FieldEngine.clampCharge(value)) else it }
    }

    fun updateSelectedParticleVelocity(speed: Float, angleDegrees: Float) {
        val id = selectedParticleId ?: return
        val angle = Math.toRadians(angleDegrees.toDouble())
        val velocity = Offset(
            (cos(angle) * speed).toFloat(),
            (sin(angle) * speed).toFloat(),
        )
        particles = particles.map { if (it.id == id) it.copy(velocity = velocity, initialVelocity = velocity) else it }
        initialParticles = initialParticles.map {
            if (it.id == id) it.copy(velocity = velocity, initialVelocity = velocity) else it
        }
    }

    fun updateSelectedPlateVoltage(value: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map { if (it.id == id) it.copy(voltage = value.coerceIn(-600f, 600f)) else it }
    }

    fun updateSelectedPlateField(valueVPerM: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map {
            if (it.id == id) {
                val dCm = it.separation * 25f
                it.copy(voltage = (valueVPerM * dCm / 100f).coerceIn(-600f, 600f))
            } else {
                it
            }
        }
    }

    fun updateSelectedPlateSeparation(value: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map {
            if (it.id == id) it.copy(separation = value.coerceIn(0.04f, 0.40f)) else it
        }
    }

    fun updateSelectedPlateLength(value: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map {
            if (it.id == id) it.copy(length = value.coerceIn(0.15f, 0.8f)) else it
        }
    }

    fun updateSelectedPlateWidth(value: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map {
            if (it.id == id) it.copy(width = value.coerceIn(0.02f, 0.12f)) else it
        }
    }

    fun updateSelectedPlateRotation(value: Float) {
        val id = selectedParallelPlateId ?: return
        parallelPlates = parallelPlates.map { if (it.id == id) it.copy(rotation = value) else it }
    }

    fun updateSelectedUniformWidth(value: Float) {
        val id = selectedUniformFieldId ?: return
        uniformFields = uniformFields.map { if (it.id == id) it.copy(width = value.coerceIn(0.2f, 0.8f)) else it }
    }

    fun updateSelectedUniformHeight(value: Float) {
        val id = selectedUniformFieldId ?: return
        uniformFields = uniformFields.map { if (it.id == id) it.copy(height = value.coerceIn(0.15f, 0.7f)) else it }
    }

    fun updateSelectedUniformStrengthVPerM(value: Float) {
        val id = selectedUniformFieldId ?: return
        uniformFields = uniformFields.map {
            if (it.id == id) it.copy(strength = (value / 10000f).coerceIn(0.01f, 1f)) else it
        }
    }

    fun updateSelectedUniformDirection(value: Float) {
        val id = selectedUniformFieldId ?: return
        uniformFields = uniformFields.map { if (it.id == id) it.copy(directionDegrees = value) else it }
    }

    fun removeSelected() {
        selectedChargeId?.let { id -> charges = charges.filterNot { it.id == id } }
        selectedParticleId?.let { id -> particles = particles.filterNot { it.id == id } }
        selectedUniformFieldId?.let { id -> uniformFields = uniformFields.filterNot { it.id == id } }
        selectedParallelPlateId?.let { id -> parallelPlates = parallelPlates.filterNot { it.id == id } }
        selectedChargeId = null
        selectedParticleId = null
        selectedUniformFieldId = null
        selectedParallelPlateId = null
    }

    fun resetParticles() {
        particles = initialParticles.map {
            it.copy(position = it.initialPosition, velocity = it.initialVelocity, trail = emptyList())
        }
    }

    fun clearAll() {
        charges = emptyList()
        particles = emptyList()
        uniformFields = emptyList()
        parallelPlates = emptyList()
        selectedChargeId = null
        selectedParticleId = null
        selectedUniformFieldId = null
        selectedParallelPlateId = null
        running = false
    }

    fun step(dt: Float) {
        particles = FieldEngine.stepParticles(particles, charges, dt, uniformFields, parallelPlates)
    }

    fun resetView() {
        viewZoom = 1.8f
        viewPan = Offset.Zero
    }

    private fun clampPoint(point: Offset) = Offset(
        point.x.coerceIn(0.04f, 0.96f),
        point.y.coerceIn(0.08f, 0.92f),
    )
}

@Composable
fun FieldLabScreen(state: FieldEditorState, onClose: () -> Unit) {
    var panelOpen by remember { mutableStateOf(true) }
    LaunchedEffect(state.running) {
        if (!state.running) return@LaunchedEffect
        while (true) {
            delay(16)
            state.step(0.016f)
        }
    }
    Surface(color = LabColors.Background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(LabColors.SurfaceRaised).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("电场实验", color = LabColors.Ink, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.width(18.dp))
                Text("独立于电路实验", color = LabColors.InkFaint, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                FieldButton(if (state.running) "暂停" else "播放") { state.running = !state.running }
                Spacer(Modifier.width(8.dp))
                FieldButton("重置粒子") { state.resetParticles() }
                Spacer(Modifier.width(8.dp))
                FieldButton(if (panelOpen) "收起面板" else "展开面板") { panelOpen = !panelOpen }
                Spacer(Modifier.width(8.dp))
                FieldButton("复位视图") { state.resetView() }
                Spacer(Modifier.width(8.dp))
                FieldButton("清空") { state.clearAll() }
                Spacer(Modifier.width(8.dp))
                FieldButton("返回电路", primary = true, onClick = onClose)
            }
            Row(Modifier.fillMaxSize()) {
                FieldCanvas(state, Modifier.weight(1f).fillMaxSize())
                if (panelOpen) {
                    FieldPanel(state, Modifier.width(286.dp).fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun FieldButton(label: String, primary: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) LabColors.Primary else LabColors.SurfaceHigh,
            contentColor = if (primary) LabColors.Background else LabColors.Ink,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    }
}

@Composable
private fun FieldCanvas(state: FieldEditorState, modifier: Modifier = Modifier) {
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    Canvas(
        modifier
            .background(Color(0xFF080D13))
            .onSizeChanged { boardSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (boardSize.width <= 0 || boardSize.height <= 0) return@detectTransformGestures
                    val width = boardSize.width.toFloat()
                    val height = boardSize.height.toFloat()
                    val center = Offset(width / 2f, height / 2f)
                    if (zoom != 1f) {
                        state.viewZoom = (state.viewZoom * zoom).coerceIn(0.7f, 3f)
                        state.viewPan += pan
                    } else {
                        val toNorm = Offset(
                            ((centroid.x - center.x - state.viewPan.x) / state.viewZoom + center.x) / width,
                            ((centroid.y - center.y - state.viewPan.y) / state.viewZoom + center.y) / height,
                        )
                        // 每次单指拖动都重新命中，保证任意电荷/粒子都能拖动。
                        state.selectAt(toNorm)
                        if (state.selectedChargeId != null || state.selectedParticleId != null) {
                            state.dragSelected(
                                Offset(
                                    pan.x / (width * state.viewZoom),
                                    pan.y / (height * state.viewZoom),
                                ),
                            )
                        } else {
                            state.viewPan += pan
                        }
                    }
                }
            },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        withTransform({
            translate(state.viewPan.x, state.viewPan.y)
            scale(state.viewZoom, state.viewZoom, pivot = center)
        }) {
            drawFieldGrid()
            drawUniformFields(state.uniformFields)
            drawParallelPlates(state.parallelPlates)
            if (state.showFieldLines) {
                drawFieldLines(state.charges, state.uniformFields, state.parallelPlates)
            }
            if (state.showForces) drawForceArrows(state.charges)
            state.charges.forEach { charge ->
                val p = charge.position.toCanvas(size)
                val color = if (charge.chargeMicroC >= 0f) Color(0xFFFF5C67) else Color(0xFF5AB6FF)
                drawCircle(color.copy(alpha = 0.18f), 28f, p)
                drawCircle(color, 15f, p)
                drawCircle(Color.White.copy(alpha = 0.65f), 4f, p)
                drawCircle(
                    if (state.selectedChargeId == charge.id) Color.White else color.copy(alpha = 0.4f),
                    19f,
                    p,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2f),
                )
                drawFieldText(
                    "${if (charge.chargeMicroC >= 0f) "+" else "−"}${abs(charge.chargeMicroC).format1()} μC",
                    p + Offset(0f, 34f),
                    Color(0xFFDCE9F6),
                )
                drawFieldText(charge.label, p + Offset(0f, -28f), color)
            }
            state.particles.forEach { particle ->
                if (particle.trail.size > 1) {
                    val path = Path()
                    particle.trail.forEachIndexed { index, point ->
                        val p = point.toCanvas(size)
                        if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }
                    drawPath(path, Color(0xFF89E8FF).copy(alpha = 0.55f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.4f))
                }
                val p = particle.position.toCanvas(size)
                val color = when (particle.kind) {
                    ParticleKind.ELECTRON -> Color(0xFF62B8FF)
                    ParticleKind.PROTON -> Color(0xFFFF8A72)
                    ParticleKind.ALPHA -> Color(0xFFC59BFF)
                    ParticleKind.CUSTOM -> if (particle.chargeMicroC >= 0f) Color(0xFFFFE08A) else Color(0xFF9DDBFF)
                }
                drawCircle(color.copy(alpha = 0.22f), 18f, p)
                drawCircle(color, 8f, p)
                drawCircle(
                    if (state.selectedParticleId == particle.id) Color.White else color.copy(alpha = 0.45f),
                    13f,
                    p,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.8f),
                )
                drawFieldText(particle.label, p + Offset(0f, -20f), color)
            }
            drawFieldText(
                "拖动电荷/粒子移动 · 双指缩放 · 右侧添加与调参",
                Offset(size.width * 0.5f, size.height - 18f),
                Color(0xFF7D91A8),
            )
        }
    }
}

private fun DrawScope.drawFieldGrid() {
    val step = 64f
    var x = 0f
    while (x < size.width) {
        drawLine(Color(0xFF152231).copy(alpha = 0.65f), Offset(x, 0f), Offset(x, size.height), 1f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(Color(0xFF152231).copy(alpha = 0.65f), Offset(0f, y), Offset(size.width, y), 1f)
        y += step
    }
    drawRect(Color(0xFF0B1520), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
}

private fun DrawScope.drawUniformFields(fields: List<UniformField>) {
    for (field in fields) {
        val center = field.center.toCanvas(size)
        val width = field.width * size.width
        val height = field.height * size.height
        drawRect(
            Color(0xFF4FB3D9).copy(alpha = 0.06f),
            topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
            size = androidx.compose.ui.geometry.Size(width, height),
        )
        drawRect(
            Color(0xFF6BCBE0).copy(alpha = 0.42f),
            topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
            size = androidx.compose.ui.geometry.Size(width, height),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f),
        )
        val angle = Math.toRadians(field.directionDegrees.toDouble())
        val unit = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        for (row in 0..2) {
            for (col in 0..3) {
                val x = center.x - width * 0.35f + width * 0.7f * col / 3f
                val y = center.y - height * 0.3f + height * 0.6f * row / 2f
                val start = Offset(x, y) - unit * 14f
                val end = Offset(x, y) + unit * 14f
                drawLine(Color(0xFF7CE4F2).copy(alpha = 0.72f), start, end, 1.4f)
                drawFieldLineArrow(end, unit, Color(0xFF7CE4F2).copy(alpha = 0.72f))
            }
        }
        drawFieldText(
            "E ${(field.strength * 10000f).toInt()} V/m",
            center + Offset(0f, height / 2f + 16f),
            Color(0xFF9BEAF3),
        )
    }
}

private fun DrawScope.drawParallelPlates(plates: List<ParallelPlate>) {
    for (plate in plates) {
        val center = plate.center.toCanvas(size)
        val length = plate.length * size.width
        val separation = plate.separation * size.height
        val thickness = (plate.width * size.height).coerceAtLeast(2f)
        withTransform({ rotate(plate.rotation, pivot = center) }) {
            val top = center.y - separation / 2f
            val bottom = center.y + separation / 2f
            drawLine(Color(0xFFFF6B72), Offset(center.x - length / 2f, top), Offset(center.x + length / 2f, top), thickness)
            drawLine(Color(0xFF5AB6FF), Offset(center.x - length / 2f, bottom), Offset(center.x + length / 2f, bottom), thickness)
            for (i in 1..4) {
                val x = center.x - length / 2f + length * i / 5f
                drawLine(
                    Color(0xFF7CE4F2).copy(alpha = 0.55f),
                    Offset(x, top + 5f),
                    Offset(x, bottom - 5f),
                    1.2f,
                )
                drawFieldLineArrow(
                    Offset(x, bottom - 7f),
                    Offset(0f, if (plate.voltage >= 0f) -1f else 1f),
                    Color(0xFF7CE4F2).copy(alpha = 0.7f),
                )
            }
        }
        val dCm = plate.separation * 25f
        val eValue = if (dCm > 0.01f) plate.voltage / (dCm / 100f) else 0f
        drawFieldText(
            "${plate.label}  ${String.format(java.util.Locale.US, "%.1f", plate.voltage)} V  ·  E ${eValue.toInt()} V/m",
            center + Offset(0f, separation / 2f + 18f),
            Color(0xFFDCE9F6),
        )
    }
}

private fun DrawScope.drawFieldLines(
    charges: List<FieldCharge>,
    uniformFields: List<UniformField> = emptyList(),
    parallelPlates: List<ParallelPlate> = emptyList(),
) {
    if (charges.isEmpty()) return
    val positive = charges.filter { it.chargeMicroC > 0f }
    val negative = charges.filter { it.chargeMicroC < 0f }
    val sources = if (positive.isNotEmpty()) positive else negative
    for (charge in sources) {
        val direction = if (charge.chargeMicroC > 0f) 1f else -1f
        for (i in 0 until 8) {
            val angle = (Math.PI * 2.0 * i / 8.0).toFloat()
            val seed = charge.position + Offset(cos(angle), sin(angle)) * 0.035f
            val points = FieldEngine.fieldLine(seed, direction, charges, uniformFields, parallelPlates, 80)
            if (points.size < 2) continue
            val path = Path()
            points.forEachIndexed { index, point ->
                val p = point.toCanvas(size)
                if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, Color(0xFF5BC6D9).copy(alpha = 0.42f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.1f))
            var arrowIndex = 10
            while (arrowIndex < points.size - 1) {
                val p = points[arrowIndex]
                val field = FieldEngine.electricFieldAt(p, charges, uniformFields, parallelPlates)
                val fieldLength = field.getDistance()
                if (fieldLength > 0.00001f) {
                    drawFieldLineArrow(
                        p.toCanvas(size),
                        Offset(field.x / fieldLength, field.y / fieldLength),
                        Color(0xFF79D8E8).copy(alpha = 0.72f),
                    )
                }
                arrowIndex += 14
            }
        }
    }
}

private fun DrawScope.drawFieldLineArrow(position: Offset, unit: Offset, color: Color) {
    val normal = Offset(-unit.y, unit.x)
    val tip = position + unit * 5.5f
    val back = position - unit * 3.5f
    drawLine(color, back + normal * 3.2f, tip, 1.2f, androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(color, back - normal * 3.2f, tip, 1.2f, androidx.compose.ui.graphics.StrokeCap.Round)
}

private fun DrawScope.drawForceArrows(charges: List<FieldCharge>) {
    for ((index, charge) in charges.withIndex()) {
        val force = FieldEngine.totalForceOn(index, charges)
        val length = force.getDistance()
        if (length < 0.00001f) continue
        val unit = Offset(force.x / length, force.y / length)
        val start = charge.position.toCanvas(size)
        val end = start + unit * (28f + (length * 20f).coerceAtMost(34f))
        drawLine(Color(0xFFFFC857).copy(alpha = 0.8f), start, end, 2f)
        drawCircle(Color(0xFFFFC857), 3f, end)
    }
}

private fun DrawScope.drawFieldText(text: String, position: Offset, color: Color) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgbCompat()
        textSize = 12f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(text, position.x, position.y, paint)
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(),
)

private fun Offset.toCanvas(size: androidx.compose.ui.geometry.Size): Offset =
    Offset(x * size.width, y * size.height)

private fun Float.format1(): String = String.format(java.util.Locale.US, "%.1f", this)

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun FieldPanel(state: FieldEditorState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(LabColors.SurfaceRaised)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("添加电荷", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton("+ 正电荷") { state.addCharge(1f) }
            FieldButton("− 负电荷") { state.addCharge(-1f) }
        }

        Text("添加粒子", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton("电子") { state.addParticle(ParticleKind.ELECTRON) }
            FieldButton("质子") { state.addParticle(ParticleKind.PROTON) }
            FieldButton("α粒子") { state.addParticle(ParticleKind.ALPHA) }
        }

        Text("添加场", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton("+ 匀强电场") { state.addUniformField() }
            FieldButton("+ 平行板") { state.addParallelPlate() }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton(if (state.showFieldLines) "隐藏电场线" else "显示电场线", primary = state.showFieldLines) {
                state.showFieldLines = !state.showFieldLines
            }
            FieldButton(if (state.showForces) "隐藏受力" else "显示受力", primary = state.showForces) {
                state.showForces = !state.showForces
            }
        }

        val selectedCharge = state.charges.firstOrNull { it.id == state.selectedChargeId }
        if (selectedCharge != null) {
            Text("${selectedCharge.label} 电荷量", color = LabColors.InkMuted, fontSize = 11.sp)
            Text(
                "${if (selectedCharge.chargeMicroC >= 0f) "+" else "−"}${abs(selectedCharge.chargeMicroC).format1()} μC",
                color = if (selectedCharge.chargeMicroC >= 0f) Color(0xFFFF8A92) else Color(0xFF8FD3FF),
                fontSize = 14.sp,
            )
            Slider(
                value = selectedCharge.chargeMicroC,
                onValueChange = { state.updateSelectedCharge(it) },
                valueRange = -5f..5f,
            )
            FieldButton("删除电荷") { state.removeSelected() }
        }

        val selectedParticle = state.particles.firstOrNull { it.id == state.selectedParticleId }
        if (selectedParticle != null) {
            val kindText = when (selectedParticle.kind) {
                ParticleKind.ELECTRON -> "电子 e⁻"
                ParticleKind.PROTON -> "质子 p⁺"
                ParticleKind.ALPHA -> "α粒子"
                ParticleKind.CUSTOM -> "自定义粒子"
            }
            Text("$kindText ${selectedParticle.label}", color = LabColors.InkMuted, fontSize = 11.sp)
            Text(
                "电荷 ${if (selectedParticle.chargeElementary >= 0f) "+" else "−"}${abs(selectedParticle.chargeElementary).format1()}e" +
                    " · 质量 ${String.format(java.util.Locale.US, "%.2e", selectedParticle.massKg)} kg",
                color = Color(0xFFFFE08A),
                fontSize = 10.sp,
            )
            val speed = selectedParticle.velocity.getDistance()
            val rawAngle = Math.toDegrees(
                atan2(selectedParticle.velocity.y.toDouble(), selectedParticle.velocity.x.toDouble()),
            ).toFloat()
            val signedAngle = if (rawAngle > 180f) rawAngle - 360f else rawAngle
            Text("初速度 ${String.format(java.util.Locale.US, "%.2f", speed)}", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = speed,
                onValueChange = { state.updateSelectedParticleVelocity(it, signedAngle) },
                valueRange = 0f..0.8f,
            )
            Text("初速度方向 ${if (signedAngle >= 0f) "+" else ""}${signedAngle.toInt()}°", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = signedAngle,
                onValueChange = { state.updateSelectedParticleVelocity(speed, it) },
                valueRange = -180f..180f,
            )
            FieldButton("删除粒子") { state.removeSelected() }
        }

        val selectedUniform = state.uniformFields.firstOrNull { it.id == state.selectedUniformFieldId }
        if (selectedUniform != null) {
            Text("${selectedUniform.label} 匀强电场", color = LabColors.InkMuted, fontSize = 11.sp)
            val eValue = selectedUniform.strength * 10000f
            Text("场强 ${eValue.toInt()} V/m", color = Color(0xFF9BEAF3), fontSize = 10.sp)
            Slider(
                value = eValue,
                onValueChange = { state.updateSelectedUniformStrengthVPerM(it) },
                valueRange = 100f..10000f,
            )
            Text("宽度 ${(selectedUniform.width * 100).toInt()}%", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedUniform.width,
                onValueChange = { state.updateSelectedUniformWidth(it) },
                valueRange = 0.2f..0.8f,
            )
            Text("高度 ${(selectedUniform.height * 100).toInt()}%", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedUniform.height,
                onValueChange = { state.updateSelectedUniformHeight(it) },
                valueRange = 0.15f..0.7f,
            )
            Text("方向 ${selectedUniform.directionDegrees.toInt()}°", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedUniform.directionDegrees,
                onValueChange = { state.updateSelectedUniformDirection(it) },
                valueRange = 0f..360f,
            )
            FieldButton("删除匀强电场") { state.removeSelected() }
        }

        val selectedPlate = state.parallelPlates.firstOrNull { it.id == state.selectedParallelPlateId }
        if (selectedPlate != null) {
            Text("${selectedPlate.label} 平行板", color = LabColors.InkMuted, fontSize = 11.sp)
            val dCm = selectedPlate.separation * 25f
            val eValue = if (dCm > 0.01f) selectedPlate.voltage / (dCm / 100f) else 0f
            Text(
                "电压 ${String.format(java.util.Locale.US, "%.1f", selectedPlate.voltage)} V" +
                    " · 场强 ${eValue.toInt()} V/m",
                color = Color(0xFFDCE9F6),
                fontSize = 10.sp,
            )
            Text("电压 U", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedPlate.voltage,
                onValueChange = { state.updateSelectedPlateVoltage(it) },
                valueRange = -600f..600f,
            )
            Text("场强 E", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = eValue.coerceIn(0f, 10000f),
                onValueChange = { state.updateSelectedPlateField(it) },
                valueRange = 0f..10000f,
            )
            Text("板间距 ${String.format(java.util.Locale.US, "%.1f", dCm)} cm", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedPlate.separation,
                onValueChange = { state.updateSelectedPlateSeparation(it) },
                valueRange = 0.04f..0.40f,
            )
            Text("板长 ${String.format(java.util.Locale.US, "%.1f", selectedPlate.length * 25f)} cm", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedPlate.length,
                onValueChange = { state.updateSelectedPlateLength(it) },
                valueRange = 0.15f..0.8f,
            )
            Text("板宽 ${(selectedPlate.width * 100).toInt()}%", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedPlate.width,
                onValueChange = { state.updateSelectedPlateWidth(it) },
                valueRange = 0.02f..0.12f,
            )
            Text("极板方向 ${selectedPlate.rotation.toInt()}°", color = LabColors.InkMuted, fontSize = 10.sp)
            Slider(
                value = selectedPlate.rotation,
                onValueChange = { state.updateSelectedPlateRotation(it) },
                valueRange = -180f..180f,
            )
            FieldButton("电场反向") { state.updateSelectedPlateVoltage(-selectedPlate.voltage) }
            FieldButton("删除平行板") { state.removeSelected() }
        }

        Spacer(Modifier.height(2.dp))
        Text(
            "电场模式独立运行，不会修改电路实验。\n" +
                "点电荷固定不动，只有试探粒子受电场力运动。",
            color = LabColors.InkFaint,
            fontSize = 10.sp,
            lineHeight = 15.sp,
        )
    }
}
