package com.physlab.electric.ui.render

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.isPowerSource
import com.physlab.electric.ui.LabColors
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

private data class SchematicItem(
    val component: PlacedInstrument,
    val center: Offset,
    val rotation: Float,
    val labelOffset: Offset,
)

private data class SchematicLayout(
    val items: List<SchematicItem>,
    val wires: List<WireSegment>,
    val ring: Boolean,
    val loop: Rect?,
    val contentRect: Rect,
)

data class SchematicScene(
    val components: List<PlacedInstrument>,
    val wires: List<WireSegment>,
    val solution: CircuitSolution,
    val showReadings: Boolean = true,
    val electronPhase: Float = 0f,
)

fun DrawScope.drawSchematic(scene: SchematicScene, density: Float) {
    val w = size.width
    val h = size.height
    drawRect(Color(0xFF06080C))
    drawRect(
        brush = Brush.radialGradient(
            listOf(Color(0xFF101823), Color(0xFF080B11), Color(0xFF050608)),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = maxOf(w, h) * 0.7f,
        ),
    )
    val step = 24f * density
    var x = step
    while (x < w) {
        var y = step
        while (y < h) {
            drawCircle(Color(0xFF6FD5FF).copy(alpha = 0.05f), 0.85f * density, Offset(x, y))
            y += step
        }
        x += step
    }

    if (scene.components.isEmpty()) return

    val content = Rect(
        86f * density,
        46f * density,
        w - 86f * density,
        h - 54f * density,
    )
    val layout = buildLayout(scene.components, scene.wires, scene.solution, content, density)
    if (layout.ring && layout.loop != null) {
        drawRing(layout, scene, density)
    } else {
        drawPhysicalDiagram(layout, scene, density)
    }
}

private fun buildLayout(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    solution: CircuitSolution,
    content: Rect,
    density: Float,
): SchematicLayout {
    val eligible = components.filter { schematicTerminals(it, solution) != null }
    val canRing = eligible.size == components.size &&
        components.size >= 2 &&
        components.none { it.type == InstrumentType.RHEOSTAT }
    if (canRing) {
        val ring = ringLayout(components, wires, solution, content, density)
        if (ring != null) return ring
    }
    return physicalLayout(components, wires, content, density)
}

private fun schematicTerminals(
    component: PlacedInstrument,
    solution: CircuitSolution,
): Pair<Int, Int>? {
    return when {
        component.type == InstrumentType.AMMETER || component.type == InstrumentType.VOLTMETER ||
            component.type == InstrumentType.MULTIMETER -> {
            val positive = solution.readings[component.id]?.meterTerminal ?: 1
            0 to positive.coerceIn(1, 2)
        }
        component.type.terminalOffsets.size == 2 -> 0 to 1
        else -> null
    }
}

private fun ringLayout(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    solution: CircuitSolution,
    content: Rect,
    density: Float,
): SchematicLayout? {
    val byId = components.associateBy { it.id }
    val uf = SimpleNets()
    for (component in components) {
        for (index in component.type.terminalOffsets.indices) {
            uf.add("${component.id}:$index")
        }
    }
    for (wire in wires) {
        if (byId[wire.a.componentId] == null || byId[wire.b.componentId] == null) continue
        uf.union("${wire.a.componentId}:${wire.a.index}", "${wire.b.componentId}:${wire.b.index}")
    }
    fun net(id: Long, index: Int): String = uf.find("$id:$index")

    val nets = components.map {
        val terminals = schematicTerminals(it, solution) ?: return null
        net(it.id, terminals.first) to net(it.id, terminals.second)
    }
    if (nets.any { it.first == it.second }) return null
    val degree = HashMap<String, Int>()
    for ((a, b) in nets) {
        degree[a] = (degree[a] ?: 0) + 1
        degree[b] = (degree[b] ?: 0) + 1
    }
    if (degree.values.any { it != 2 }) return null

    val netEdges = HashMap<String, MutableList<Int>>()
    nets.forEachIndexed { index, (a, b) ->
        netEdges.getOrPut(a) { mutableListOf() }.add(index)
        netEdges.getOrPut(b) { mutableListOf() }.add(index)
    }
    val used = BooleanArray(components.size)
    val order = mutableListOf<Int>()
    var current = components.indexOfFirst { it.type.isPowerSource }.let { if (it < 0) 0 else it }
    var entryNet = nets[current].first
    var guard = 0
    while (!used[current] && guard++ <= components.size + 2) {
        used[current] = true
        order += current
        val (a, b) = nets[current]
        val exitNet = if (entryNet == a) b else a
        val candidates = netEdges[exitNet]?.filter { it != current } ?: emptyList()
        val next = candidates.firstOrNull { !used[it] } ?: candidates.firstOrNull()
        if (next == null) break
        entryNet = exitNet
        current = next
    }
    if (order.size != components.size) return null

    val left = content.left
    val top = content.top
    val right = content.right
    val bottom = content.bottom
    val width = right - left
    val height = bottom - top
    val perimeter = 2f * (width + height)
    val n = order.size
    val items = ArrayList<SchematicItem>(n)
    for (i in 0 until n) {
        val d = (width * 0.5f + perimeter * i / n) % perimeter
        var point: Offset
        var rotation: Float
        var labelOffset: Offset
        when {
            d < width -> {
                point = Offset(left + d, bottom)
                rotation = 0f
                labelOffset = Offset(0f, 35f * density)
            }
            d < width + height -> {
                val t = d - width
                point = Offset(right, bottom - t)
                rotation = -90f
                labelOffset = Offset(46f * density, 0f)
            }
            d < 2f * width + height -> {
                val t = d - width - height
                point = Offset(right - t, top)
                rotation = 180f
                labelOffset = Offset(0f, -35f * density)
            }
            else -> {
                val t = d - 2f * width - height
                point = Offset(left, top + t)
                rotation = 90f
                labelOffset = Offset(-46f * density, 0f)
            }
        }
        val comp = components[order[i]]
        items += SchematicItem(comp, point, rotation, labelOffset)
    }

    return SchematicLayout(
        items = items,
        wires = wires,
        ring = true,
        loop = Rect(left, top, right, bottom),
        contentRect = content,
    )
}

private fun physicalLayout(
    components: List<PlacedInstrument>,
    wires: List<WireSegment>,
    content: Rect,
    density: Float,
): SchematicLayout {
    val minX = components.minOf { it.center.x }
    val maxX = components.maxOf { it.center.x }
    val minY = components.minOf { it.center.y }
    val maxY = components.maxOf { it.center.y }
    val spanX = (maxX - minX).coerceAtLeast(120f)
    val spanY = (maxY - minY).coerceAtLeast(100f)
    val items = components.map { component ->
        val nx = (component.center.x - minX) / spanX
        val ny = (component.center.y - minY) / spanY
        val center = Offset(
            content.left + nx * content.width,
            content.top + ny * content.height,
        )
        SchematicItem(
            component = component,
            center = center,
            rotation = 0f,
            labelOffset = Offset(0f, 36f * density),
        )
    }
    return SchematicLayout(
        items = items,
        wires = wires,
        ring = false,
        loop = null,
        contentRect = content,
    )
}

private fun DrawScope.drawRing(layout: SchematicLayout, scene: SchematicScene, density: Float) {
    val loop = layout.loop ?: return
    val path = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                loop,
                CornerRadius(4f * density, 4f * density),
            ),
        )
    }
    drawPath(
        path,
        Color(0xFF7FD8FF).copy(alpha = 0.12f),
        style = Stroke(9f * density, cap = StrokeCap.Round),
    )
    drawPath(
        path,
        Color(0xFFEAF5FF),
        style = Stroke(2.1f * density, cap = StrokeCap.Round),
    )
    if (scene.showReadings &&
        scene.solution.status == com.physlab.electric.model.CircuitStatus.CLOSED &&
        scene.solution.totalCurrent > 0.001f
    ) {
        val measure = PathMeasure()
        measure.setPath(path, false)
        val length = measure.length
        val spacing = 80f * density
        var distance = (scene.electronPhase % spacing)
        while (distance < length) {
            val p = measure.getPosition(distance)
            drawCircle(LabColors.Primary.copy(alpha = 0.12f), 5f * density, p)
            drawCircle(Color(0xFFB9EDFF), 1.6f * density, p)
            distance += spacing
        }
    }

    for (item in layout.items) {
        drawClearance(item.center, density, 31f)
        drawSchematicSymbol(
            item = item,
            reading = scene.solution.readings[item.component.id] ?: ComponentReading(),
            density = density,
        )
    }
    for (item in layout.items) {
        drawSchematicLabel(item, scene, density)
    }
}

private fun DrawScope.drawPhysicalDiagram(layout: SchematicLayout, scene: SchematicScene, density: Float) {
    val byItem = layout.items.associateBy { it.component.id }
    val anchorCount = HashMap<Offset, Int>()

    for (wire in layout.wires) {
        val aItem = byItem[wire.a.componentId] ?: continue
        val bItem = byItem[wire.b.componentId] ?: continue
        val start = schematicTerminal(aItem, wire.a.index, density)
        val end = schematicTerminal(bItem, wire.b.index, density)
        anchorCount[start] = (anchorCount[start] ?: 0) + 1
        anchorCount[end] = (anchorCount[end] ?: 0) + 1
        val path = orthogonalPath(start, end)
        drawPath(
            path,
            wire.color.body.copy(alpha = 0.4f),
            style = Stroke(7f * density, cap = StrokeCap.Round),
        )
        drawPath(
            path,
            if (wire.color == com.physlab.electric.model.WireColor.BLACK) Color(0xFFAEBACB)
            else wire.color.body,
            style = Stroke(2.4f * density, cap = StrokeCap.Round),
        )
    }

    for (item in layout.items) {
        drawClearance(item.center, density, 30f)
        drawSchematicSymbol(
            item = item,
            reading = scene.solution.readings[item.component.id] ?: ComponentReading(),
            density = density,
        )
    }

    for ((anchor, count) in anchorCount) {
        if (count >= 2) {
            drawCircle(Color(0xFF06080C), 4.2f * density, anchor)
            drawCircle(Color(0xFFEAF5FF), 3f * density, anchor)
        }
    }

    for (item in layout.items) {
        drawSchematicLabel(item, scene, density)
    }
}

private fun DrawScope.drawClearance(center: Offset, density: Float, radiusDp: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF080A0E), Color(0xFF080A0E), Color(0xFF080A0E).copy(alpha = 0f)),
            center = center,
            radius = radiusDp * density * 1.4f,
        ),
        radius = radiusDp * density,
        center = center,
    )
}

private fun schematicTerminal(item: SchematicItem, index: Int, density: Float): Offset {
    val local = when (item.component.type) {
        InstrumentType.AMMETER, InstrumentType.VOLTMETER, InstrumentType.MULTIMETER ->
            if (index == 0) Offset(-30f, 0f) else Offset(30f, 0f)
        InstrumentType.RHEOSTAT -> when (index) {
            0 -> Offset(-26f, 4f)
            1 -> Offset(26f, 4f)
            2 -> Offset(-14f, -16f)
            else -> Offset(14f, -16f)
        }
        else -> if (index == 0) Offset(-30f, 0f) else Offset(30f, 0f)
    }
    val scaled = local * density
    return if (item.rotation == 0f) item.center + scaled else item.center + rotateOffset(scaled, item.rotation)
}

private fun rotateOffset(value: Offset, degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    val c = Math.cos(rad).toFloat()
    val s = Math.sin(rad).toFloat()
    return Offset(value.x * c - value.y * s, value.x * s + value.y * c)
}

private fun DrawScope.drawSchematicSymbol(
    item: SchematicItem,
    reading: ComponentReading,
    density: Float,
) {
    withTransform({
        translate(item.center.x, item.center.y)
        rotate(item.rotation, pivot = Offset.Zero)
        scale(density, density, pivot = Offset.Zero)
    }) {
        val type = item.component.type
        val line = Color(0xFFE7F2FF)
        val thin = 1.8f
        drawLine(Color(0xFFE7F2FF), Offset(-30f, 0f), Offset(-21f, 0f), thin, StrokeCap.Round)
        drawLine(Color(0xFFE7F2FF), Offset(21f, 0f), Offset(30f, 0f), thin, StrokeCap.Round)

        when (type) {
            InstrumentType.BATTERY, InstrumentType.POWER_SUPPLY, InstrumentType.STORAGE_BATTERY -> {
                drawLine(line, Offset(-10f, -8f), Offset(-10f, 8f), 4.4f, StrokeCap.Round)
                drawLine(line, Offset(-3f, -15f), Offset(-3f, 15f), 2.9f, StrokeCap.Round)
                drawLine(line, Offset(4f, -8f), Offset(4f, 8f), 4.4f, StrokeCap.Round)
                drawLine(line, Offset(11f, -15f), Offset(11f, 15f), 2.9f, StrokeCap.Round)
                textLabel(
                    if (item.component.polarityFlipped) "+" else "−",
                    -16f,
                    -13f,
                    6.4f,
                    if (item.component.polarityFlipped) Color(0xFFFF9CA1) else Color(0xFFAAB7C9),
                    true,
                )
                textLabel(
                    if (item.component.polarityFlipped) "−" else "+",
                    17f,
                    -13f,
                    6.4f,
                    if (item.component.polarityFlipped) Color(0xFFAAB7C9) else Color(0xFFFF9CA1),
                    true,
                )
            }

            InstrumentType.BUZZER -> {
                drawCircle(Color(0xFF10151D), 12f, Offset.Zero)
                drawCircle(line, 12f, Offset.Zero, style = Stroke(1.7f))
                drawArc(
                    line,
                    startAngle = -55f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(-6f, -8f),
                    size = Size(12f, 16f),
                    style = Stroke(1.5f, cap = StrokeCap.Round),
                )
                drawArc(
                    Color(0xFFFFC857),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(6f, -12f),
                    size = Size(15f, 24f),
                    style = Stroke(1.1f),
                )
            }

            InstrumentType.LED -> {
                val diode = Path().apply {
                    moveTo(-10f, -9f)
                    lineTo(-10f, 9f)
                    lineTo(7f, 0f)
                    close()
                }
                drawPath(diode, Color(0xFF10151D))
                drawPath(diode, line, style = Stroke(1.6f))
                drawLine(line, Offset(7f, -9f), Offset(7f, 9f), 2.3f, StrokeCap.Round)
                drawLine(Color(0xFFFF6B72), Offset(8f, -12f), Offset(14f, -17f), 1.1f, StrokeCap.Round)
                drawLine(Color(0xFFFF6B72), Offset(12f, -8f), Offset(18f, -13f), 1.1f, StrokeCap.Round)
            }

            InstrumentType.RESISTANCE_BOX -> {
                drawRect(Color(0xFF10151D), Offset(-16f, -9f), Size(32f, 18f))
                drawRect(line, Offset(-16f, -9f), Size(32f, 18f), style = Stroke(1.7f))
                textLabel("R", 0f, 3.6f, 9f, line, true)
                for (i in 0 until 3) {
                    drawCircle(line, 1.6f, Offset(-10f + i * 10f, 13f))
                }
            }

            InstrumentType.LAMP, InstrumentType.TEMP_LAMP -> {
                val lit = reading.power > 0.03f
                if (lit) {
                    drawCircle(Color(0xFFFFD35C).copy(alpha = 0.24f), 17f, Offset.Zero)
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(if (lit) Color(0xFFFFF3C4) else Color(0xFF1A2029), Color(0xFF080A0E)),
                        center = Offset.Zero,
                        radius = 11f,
                    ),
                    radius = 10.5f,
                    center = Offset.Zero,
                )
                drawCircle(line, 10.5f, Offset.Zero, style = Stroke(1.8f))
                val d = 7.4f
                drawLine(line, Offset(-d, -d), Offset(d, d), 1.5f)
                drawLine(line, Offset(-d, d), Offset(d, -d), 1.5f)
            }

            InstrumentType.SWITCH -> {
                drawCircle(Color(0xFF0D1219), 17f, Offset.Zero)
                drawCircle(line.copy(alpha = 0.22f), 17f, Offset.Zero, style = Stroke(1f))
                drawLine(line, Offset(-21f, 0f), Offset(-12f, 0f), thin, StrokeCap.Round)
                drawCircle(line, 2.4f, Offset(-12f, 0f))
                drawCircle(line, 2.4f, Offset(12f, 0f))
                if (item.component.closed) {
                    val blade = Path().apply {
                        moveTo(-12f, 0f)
                        quadraticTo(0f, -7.5f, 12f, 0f)
                    }
                    drawPath(blade, line, style = Stroke(2.1f, cap = StrokeCap.Round))
                } else {
                    drawLine(line, Offset(-12f, 0f), Offset(9f, -13f), 2.1f, StrokeCap.Round)
                }
                drawLine(line, Offset(12f, 0f), Offset(21f, 0f), thin, StrokeCap.Round)
            }

            InstrumentType.AMMETER, InstrumentType.VOLTMETER, InstrumentType.MULTIMETER -> {
                drawCircle(Color(0xFF10151D), 11.5f, Offset.Zero)
                drawCircle(line, 11.5f, Offset.Zero, style = Stroke(1.7f))
            }

            InstrumentType.RESISTOR -> {
                drawRect(Color(0xFF10151D), Offset(-14f, -7f), Size(28f, 14f))
                drawRect(line, Offset(-14f, -7f), Size(28f, 14f), style = Stroke(1.7f))
                drawLine(line, Offset(-21f, 0f), Offset(-14f, 0f), thin)
                drawLine(line, Offset(14f, 0f), Offset(21f, 0f), thin)
            }

            InstrumentType.RHEOSTAT -> {
                drawRect(Color(0xFF10151D), Offset(-15f, -7f), Size(30f, 14f))
                drawRect(line, Offset(-15f, -7f), Size(30f, 14f), style = Stroke(1.7f))
                drawLine(line, Offset(-21f, 0f), Offset(-15f, 0f), thin)
                drawLine(line, Offset(15f, 0f), Offset(21f, 0f), thin)
                drawLine(Color(0xFFFFC857), Offset(-18f, 14f), Offset(16f, -14f), 1.6f, StrokeCap.Round)
                drawLine(Color(0xFFFFC857), Offset(16f, -14f), Offset(10f, -12f), 1.6f, StrokeCap.Round)
                drawLine(Color(0xFFFFC857), Offset(16f, -14f), Offset(14f, -7f), 1.6f, StrokeCap.Round)
                drawLine(line, Offset(-14f, -16f), Offset(-14f, -7f), thin)
                drawLine(line, Offset(14f, -16f), Offset(14f, -7f), thin)
            }

            InstrumentType.MOTOR -> {
                drawCircle(Color(0xFF10151D), 12f, Offset.Zero)
                drawCircle(line, 12f, Offset.Zero, style = Stroke(1.7f))
                if (reading.energized) {
                    drawArc(
                        Color(0xFF8FE8FF).copy(alpha = 0.65f),
                        -70f, 55f, false,
                        Offset(-15f, -17f), Size(30f, 30f),
                        style = Stroke(1.2f),
                    )
                }
            }

            InstrumentType.ELECTROMAGNET -> {
                drawLine(line, Offset(-22f, 0f), Offset(-14f, 0f), thin)
                for (i in 0 until 5) {
                    val cx = -14f + i * 6.2f
                    drawArc(
                        line,
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(cx - 3.4f, -9f),
                        size = Size(6.8f, 18f),
                        style = Stroke(1.5f),
                    )
                }
                drawLine(line, Offset(17f, 0f), Offset(22f, 0f), thin)
                drawLine(Color(0xFFB9C8D8), Offset(-22f, 0f), Offset(20f, 0f), 1.1f)
            }

            InstrumentType.SOLENOID -> {
                for (i in 0 until 6) {
                    drawArc(
                        line,
                        -80f, 160f, false,
                        Offset(-18f + i * 5.6f, -8f), Size(11f, 16f),
                        style = Stroke(1.5f),
                    )
                }
                drawLine(line, Offset(-22f, 0f), Offset(-14f, 0f), thin)
                drawLine(line, Offset(18f, 0f), Offset(22f, 0f), thin)
            }

            InstrumentType.BAR_MAGNET -> {
                drawRect(Color(0xFFC62430), Offset(-16f, -7f), Size(16f, 14f))
                drawRect(Color(0xFF2668C4), Offset(0f, -7f), Size(16f, 14f))
                drawRect(line, Offset(-16f, -7f), Size(32f, 14f), style = Stroke(1.3f))
                textLabel("N", -8f, 3.2f, 8f, Color.White, true)
                textLabel("S", 8f, 3.2f, 8f, Color.White, true)
            }

            InstrumentType.HORSESHOE_MAGNET -> {
                drawArc(
                    Color(0xFFC62430), 0f, 180f, false,
                    Offset(-14f, -9f), Size(28f, 28f),
                    style = Stroke(8f, cap = StrokeCap.Round),
                )
                drawLine(Color(0xFFC62430), Offset(-10f, -8f), Offset(-10f, -16f), 8f, StrokeCap.Round)
                drawLine(Color(0xFF2668C4), Offset(10f, -8f), Offset(10f, -16f), 8f, StrokeCap.Round)
                drawLine(Color(0xFFB9C8D8), Offset(-14f, -17f), Offset(-6f, -17f), 2f)
                drawLine(Color(0xFFB9C8D8), Offset(6f, -17f), Offset(14f, -17f), 2f)
            }

            InstrumentType.COMPASS -> {
                drawCircle(Color(0xFF10151D), 12f, Offset.Zero)
                drawCircle(line, 12f, Offset.Zero, style = Stroke(1.6f))
                drawLine(Color(0xFFFF6B72), Offset(0f, -9f), Offset(4f, 0f), 1.7f)
                drawLine(Color(0xFFDCE9F6), Offset(0f, 9f), Offset(-4f, 0f), 1.7f)
                drawCircle(Color(0xFFFFC857), 1.7f, Offset.Zero)
            }

            else -> Unit
        }
    }

    // Text stays upright even when a symbol is rotated onto a vertical side.
    when (item.component.type) {
        InstrumentType.AMMETER -> textLabel(
            "A",
            item.center.x,
            item.center.y + 4.3f * density,
            12.5f * density,
            Color(0xFFE7F2FF),
            true,
        )
        InstrumentType.VOLTMETER -> textLabel(
            "V",
            item.center.x,
            item.center.y + 4.3f * density,
            12.5f * density,
            Color(0xFFE7F2FF),
            true,
        )
        InstrumentType.MULTIMETER -> textLabel(
            "M",
            item.center.x,
            item.center.y + 4.3f * density,
            12.5f * density,
            Color(0xFFE7F2FF),
            true,
        )
        InstrumentType.MOTOR -> textLabel(
            "M",
            item.center.x,
            item.center.y + 4.5f * density,
            11.5f * density,
            Color(0xFFE7F2FF),
            true,
        )
        else -> Unit
    }
}

private fun DrawScope.drawSchematicLabel(
    item: SchematicItem,
    scene: SchematicScene,
    density: Float,
) {
    val reading = scene.solution.readings[item.component.id] ?: ComponentReading()
    val value = when {
        item.component.damaged -> "已损坏"
        item.component.type == InstrumentType.BATTERY ->
            "${item.component.batteryCells}节 ${formatNumber(item.component.voltage)}V"
        item.component.type == InstrumentType.POWER_SUPPLY ->
            "${formatNumber(item.component.supplyVoltage)}V ${if (item.component.supplyAc) "AC" else "DC"}"
        item.component.type == InstrumentType.STORAGE_BATTERY ->
            "${formatNumber(item.component.storageVoltage)}V"
        item.component.type == InstrumentType.AMMETER ->
            if (scene.showReadings) {
                "${formatAmmeter(reading.meterSignedValue)}${if (reading.meterOverRange) " 超" else ""}"
            } else ""
        item.component.type == InstrumentType.VOLTMETER ->
            if (scene.showReadings) {
                "${formatVoltmeter(reading.meterSignedValue)}${if (reading.meterOverRange) " 超" else ""}"
            } else ""
        item.component.type == InstrumentType.LAMP || item.component.type == InstrumentType.TEMP_LAMP ->
            if (scene.showReadings && reading.power > 0.03f) "${formatNumber(reading.power)}W" else ""
        item.component.type == InstrumentType.MOTOR || item.component.type == InstrumentType.BUZZER ->
            if (scene.showReadings) formatCurrent(reading.current) else ""
        item.component.type == InstrumentType.LED ->
            if (scene.showReadings) {
                if (reading.ledReversed) "反接" else formatCurrent(reading.current)
            } else ""
        item.component.type == InstrumentType.RESISTOR -> "${formatNumber(item.component.resistance)}Ω"
        item.component.type == InstrumentType.RHEOSTAT ->
            "${formatNumber(item.component.rheostatResistance)} / ${formatNumber(item.component.rheostatMax)}Ω"
        item.component.type == InstrumentType.RESISTANCE_BOX -> "${item.component.resistanceBoxValue}Ω"
        else -> ""
    }
    val text = if (value.isBlank()) item.component.label else "${item.component.label}  $value"
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (item.component.damaged || reading.meterOverRange || reading.meterReversed || reading.ledReversed) {
            0xFFFF7B82.toInt()
        } else {
            0xFFD7E6F7.toInt()
        }
        textSize = 9.4f * density
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    val x = (item.center.x + item.labelOffset.x).coerceIn(52f * density, size.width - 52f * density)
    val y = (item.center.y + item.labelOffset.y).coerceIn(24f * density, size.height - 18f * density)
    val tw = paint.measureText(text)
    drawRoundRect(
        color = Color(0xFF080B11).copy(alpha = 0.92f),
        topLeft = Offset(x - tw / 2f - 6f * density, y - 10f * density),
        size = Size(tw + 12f * density, 15f * density),
        cornerRadius = CornerRadius(7f * density, 7f * density),
    )
    drawRoundRect(
        color = Color(0xFF2D3B4F).copy(alpha = 0.7f),
        topLeft = Offset(x - tw / 2f - 6f * density, y - 10f * density),
        size = Size(tw + 12f * density, 15f * density),
        cornerRadius = CornerRadius(7f * density, 7f * density),
        style = Stroke(0.7f * density),
    )
    drawContext.canvas.nativeCanvas.drawText(text, x, y + 1.2f * density, paint)
}

private fun orthogonalPath(start: Offset, end: Offset): Path {
    val dx = end.x - start.x
    val dy = end.y - start.y
    return Path().apply {
        moveTo(start.x, start.y)
        when {
            abs(dx) < 1.5f || abs(dy) < 1.5f -> lineTo(end.x, end.y)
            abs(dx) > abs(dy) -> {
                val midX = start.x + dx * 0.55f
                lineTo(midX, start.y)
                lineTo(midX, end.y)
                lineTo(end.x, end.y)
            }
            else -> {
                val midY = start.y + dy * 0.55f
                lineTo(start.x, midY)
                lineTo(end.x, midY)
                lineTo(end.x, end.y)
            }
        }
    }
}

private class SimpleNets {
    private val parent = HashMap<String, String>()
    fun add(key: String) {
        parent.putIfAbsent(key, key)
    }

    fun find(key: String): String {
        add(key)
        var root = key
        while (parent.getValue(root) != root) root = parent.getValue(root)
        var node = key
        while (parent.getValue(node) != node) {
            val next = parent.getValue(node)
            parent[node] = root
            node = next
        }
        return root
    }

    fun union(a: String, b: String) {
        val ra = find(a)
        val rb = find(b)
        if (ra != rb) parent[rb] = ra
    }
}
