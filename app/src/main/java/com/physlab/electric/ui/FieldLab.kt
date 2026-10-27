package com.physlab.electric.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
            ),
        ),
    )
    var running by mutableStateOf(false)
    var showFieldLines by mutableStateOf(true)
    var showForces by mutableStateOf(true)
    var selectedChargeId by mutableStateOf<Long?>(null)
    var selectedParticleId by mutableStateOf<Long?>(null)
    var viewZoom by mutableStateOf(1f)
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
    }

    fun addParticle(sign: Float) {
        val particle = FieldParticle(
            id = nextId++,
            position = Offset(0.50f, 0.20f),
            velocity = Offset(0.15f, 0f),
            chargeMicroC = sign,
            label = "P${particles.size + 1}",
        )
        particles = particles + particle
        selectedParticleId = particle.id
        selectedChargeId = null
        initialParticles = particles
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
        }
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

    fun removeSelected() {
        selectedChargeId?.let { id -> charges = charges.filterNot { it.id == id } }
        selectedParticleId?.let { id -> particles = particles.filterNot { it.id == id } }
        selectedChargeId = null
        selectedParticleId = null
    }

    fun resetParticles() {
        particles = initialParticles.map { it.copy(position = it.position, velocity = it.velocity, trail = emptyList()) }
    }

    fun clearAll() {
        charges = emptyList()
        particles = emptyList()
        selectedChargeId = null
        selectedParticleId = null
        running = false
    }

    fun step(dt: Float) {
        particles = FieldEngine.stepParticles(particles, charges, dt)
    }

    fun resetView() {
        viewZoom = 1f
        viewPan = Offset.Zero
    }

    private fun clampPoint(point: Offset) = Offset(
        point.x.coerceIn(0.04f, 0.96f),
        point.y.coerceIn(0.08f, 0.92f),
    )
}

@Composable
fun FieldLabScreen(state: FieldEditorState, onClose: () -> Unit) {
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
                FieldButton("复位视图") { state.resetView() }
                Spacer(Modifier.width(8.dp))
                FieldButton("清空") { state.clearAll() }
                Spacer(Modifier.width(8.dp))
                FieldButton("返回电路", primary = true, onClick = onClose)
            }
            Row(Modifier.fillMaxSize()) {
                FieldCanvas(state, Modifier.weight(1f).fillMaxSize())
                FieldPanel(state, Modifier.width(286.dp).fillMaxSize())
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
                        if (state.selectedChargeId == null && state.selectedParticleId == null) {
                            state.selectAt(toNorm)
                        }
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
            if (state.showFieldLines) drawFieldLines(state.charges)
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
                val color = if (particle.chargeMicroC >= 0f) Color(0xFFFFE08A) else Color(0xFF9DDBFF)
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

private fun DrawScope.drawFieldLines(charges: List<FieldCharge>) {
    if (charges.isEmpty()) return
    val positive = charges.filter { it.chargeMicroC > 0f }
    val negative = charges.filter { it.chargeMicroC < 0f }
    val sources = if (positive.isNotEmpty()) positive else negative
    for (charge in sources) {
        val direction = if (charge.chargeMicroC > 0f) 1f else -1f
        for (i in 0 until 8) {
            val angle = (Math.PI * 2.0 * i / 8.0).toFloat()
            val seed = charge.position + Offset(cos(angle), sin(angle)) * 0.035f
            val points = FieldEngine.fieldLine(seed, direction, charges, 80)
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
                val field = FieldEngine.electricFieldAt(p, charges)
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

@Composable
private fun FieldPanel(state: FieldEditorState, modifier: Modifier = Modifier) {
    Column(
        modifier.background(LabColors.SurfaceRaised).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("添加电荷", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton("+ 正电荷") { state.addCharge(1f) }
            FieldButton("− 负电荷") { state.addCharge(-1f) }
        }
        Text("添加试探粒子", color = LabColors.InkMuted, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldButton("+ 试探粒子") { state.addParticle(1f) }
            FieldButton("− 试探粒子") { state.addParticle(-1f) }
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
        } else {
            Text("拖动电荷可选中；选中后可调电荷量。", color = LabColors.InkFaint, fontSize = 10.sp)
        }
        val selectedParticle = state.particles.firstOrNull { it.id == state.selectedParticleId }
        if (selectedParticle != null) {
            Text("${selectedParticle.label} 试探粒子", color = LabColors.InkMuted, fontSize = 11.sp)
            Text(
                "电荷 ${if (selectedParticle.chargeMicroC >= 0f) "+" else "−"}${abs(selectedParticle.chargeMicroC).format1()} μC",
                color = Color(0xFFFFE08A),
                fontSize = 11.sp,
            )
            FieldButton("删除粒子") { state.removeSelected() }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "电场模式独立运行，不会修改电路实验。\n" +
                "粒子运动使用库仑力积分，拖动电荷可实时改变电场。",
            color = LabColors.InkFaint,
            fontSize = 10.sp,
            lineHeight = 15.sp,
        )
    }
}
