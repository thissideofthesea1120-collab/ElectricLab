package com.physlab.electric.ui.render

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.Selection
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.isMagnet
import com.physlab.electric.model.isMeter
import com.physlab.electric.model.isPowerSource
import com.physlab.electric.ui.LabColors
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

data class BoardScene(
    val components: List<PlacedInstrument>,
    val wires: List<WireSegment>,
    val solution: CircuitSolution,
    val selection: Selection = Selection(),
    val wireStart: TerminalRef? = null,
    val wirePointer: Offset? = null,
    val wireBends: List<Offset> = emptyList(),
    val wireColor: WireColor = WireColor.BLACK,
    val fadingWireId: Long? = null,
    val hoveredTerminal: TerminalRef? = null,
    val showTerminals: Boolean = false,
    val showReadings: Boolean = true,
    val showMagneticField: Boolean = true,
    val electronPhase: Float = 0f,
    val worldLeft: Float = 0f,
    val worldTop: Float = 0f,
    val worldRight: Float = 0f,
    val worldBottom: Float = 0f,
)

fun DrawScope.drawBoard(scene: BoardScene, density: Float) {
    val worldLeft = scene.worldLeft * density
    val worldTop = scene.worldTop * density
    val w = (scene.worldRight - scene.worldLeft).coerceAtLeast(1f) * density
    val h = (scene.worldBottom - scene.worldTop).coerceAtLeast(1f) * density
    drawRect(
        color = Color(0xFF05070A),
        topLeft = Offset(worldLeft, worldTop),
        size = Size(w, h),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF121A25), Color(0xFF090C12), Color(0xFF040507)),
            center = Offset(worldLeft + w * 0.46f, worldTop + h * 0.42f),
            radius = maxOf(w, h) * 0.72f,
        ),
        topLeft = Offset(worldLeft, worldTop),
        size = Size(w, h),
    )

    val step = 26f * density
    val dotColor = Color(0xFF6FD5FF).copy(alpha = 0.065f)
    val dotRadius = 0.9f * density
    var x = worldLeft + step
    val worldRightPx = worldLeft + w
    val worldBottomPx = worldTop + h
    while (x < worldRightPx) {
        var y = worldTop + step
        while (y < worldBottomPx) {
            drawCircle(dotColor, dotRadius, Offset(x, y))
            y += step
        }
        x += step
    }

    val major = step * 4f
    x = worldLeft + major
    while (x < worldRightPx) {
        drawLine(
            Color(0xFF6FD5FF).copy(alpha = 0.038f),
            Offset(x, worldTop),
            Offset(x, worldBottomPx),
            0.7f * density,
        )
        x += major
    }
    var y = worldTop + major
    while (y < worldBottomPx) {
        drawLine(
            Color(0xFF6FD5FF).copy(alpha = 0.038f),
            Offset(worldLeft, y),
            Offset(worldRightPx, y),
            0.7f * density,
        )
        y += major
    }

    // Soft inner border, like a lab bench mat.
    drawRoundRect(
        color = Color(0xFF263244).copy(alpha = 0.55f),
        topLeft = Offset(worldLeft + 10f * density, worldTop + 10f * density),
        size = Size(w - 20f * density, h - 20f * density),
        cornerRadius = CornerRadius(18f * density, 18f * density),
        style = Stroke(1f * density),
    )
    drawRoundRect(
        color = Color(0xFF6FD5FF).copy(alpha = 0.05f),
        topLeft = Offset(worldLeft + 10f * density, worldTop + 10f * density),
        size = Size(w - 20f * density, h - 20f * density),
        cornerRadius = CornerRadius(18f * density, 18f * density),
        style = Stroke(2.4f * density),
    )

    val byId = scene.components.associateBy { it.id }

    if (scene.showMagneticField) {
        drawMagneticFieldScene(scene, density)
    }

    val sorted = scene.components.sortedBy { if (it.id == scene.selection.componentId) 1 else 0 }
    for (component in sorted) {
        val reading = scene.solution.readings[component.id] ?: ComponentReading()
        val selected = component.id == scene.selection.componentId
        val visual = InstrumentVisual(
            selected = selected,
            closed = component.closed,
            lit = (component.type == InstrumentType.LAMP || component.type == InstrumentType.TEMP_LAMP) &&
                reading.power > 0.005f,
            brightness = lampBrightness(reading.power),
            slider = component.slider,
            meterFraction = if (reading.meterRange > 0f) {
                val fraction = reading.meterSignedValue / reading.meterRange
                if (component.type == InstrumentType.GALVANOMETER) {
                    fraction.coerceIn(-1f, 1f)
                } else {
                    fraction.coerceIn(-1f / 6f, 1f)
                }
            } else {
                null
            },
            meterRangeLabel = reading.meterRangeLabel,
            meterOverRange = reading.meterOverRange,
            meterTerminal = reading.meterTerminal,
            meterFlipped = component.meterFlipped,
            batteryCells = component.batteryCells,
            lampSpec = component.lampSpec,
            polarityFlipped = component.polarityFlipped,
            rotation = component.rotation,
            rheostatMax = component.rheostatMax,
            supplyVoltage = component.supplyVoltage,
            supplyAc = component.supplyAc,
            capacitanceUf = component.capacitanceUf,
            inductanceMh = component.inductanceMh,
            switchPosition = component.switchPosition,
            storageVoltage = component.storageVoltage,
            resistanceBoxValue = component.resistanceBoxValue,
            wireSampleIndex = component.wireSampleIndex,
            showWireResistance = component.showWireResistance,
            energized = reading.energized,
            magneticReversed = (reading.signedCurrent < -0.0001f) xor
                ((component.type == InstrumentType.SOLENOID ||
                    component.type == InstrumentType.ELECTROMAGNET) && component.windingReversed),
            windingReversed = component.windingReversed,
            fanAngle = if (component.type == InstrumentType.MOTOR && reading.energized) {
                val speed = (reading.power / (4.5f * 4.5f / 12f)).coerceIn(0.08f, 2f)
                scene.electronPhase * (if (reading.signedCurrent < 0f) -0.9f else 0.9f) * speed
            } else {
                0f
            },
            compassAngle = if (component.type == InstrumentType.COMPASS) {
                compassAngle(component, scene.components, scene.solution)
            } else 0f,
            paperclipPull = if (component.type == InstrumentType.PAPERCLIPS) {
                paperclipAttraction(component, scene.components, scene.solution)
            } else {
                Offset.Zero
            },
        )
        if (selected) {
            val pulse = 0.30f + 0.12f * kotlin.math.sin(scene.electronPhase * 0.0022f)
            withTransform({ rotate(component.rotation, pivot = component.center * density) }) {
                drawRoundRect(
                    color = LabColors.Primary.copy(alpha = pulse),
                    topLeft = Offset(
                        (component.center.x - component.type.halfWidth - 7f) * density,
                        (component.center.y - component.type.halfHeight - 7f) * density,
                    ),
                    size = Size(
                        (component.type.width + 14f) * density,
                        (component.type.height + 14f) * density,
                    ),
                    cornerRadius = CornerRadius(15f * density, 15f * density),
                    style = Stroke(1.6f * density),
                )
            }
        }
        drawInstrument(
            type = component.type,
            center = component.center * density,
            scale = density,
            visual = visual,
        )
        if (component.damaged) {
            drawDamagedOverlay(component, density)
        }
    }

    // Wires are drawn above the instruments so every connection stays visible.
    for (wire in scene.wires) {
        val a = byId[wire.a.componentId] ?: continue
        val b = byId[wire.b.componentId] ?: continue
        val start = a.terminalPosition(wire.a.index) * density
        val end = b.terminalPosition(wire.b.index) * density
        val selected = scene.selection.wireId == wire.id
        val fading = scene.fadingWireId == wire.id
        val touchesVoltmeter = a.type == InstrumentType.VOLTMETER || b.type == InstrumentType.VOLTMETER
        val endpointCurrent = maxOf(
            scene.solution.readings[a.id]?.current ?: 0f,
            scene.solution.readings[b.id]?.current ?: 0f,
        )
        val bends = wire.bends.map { it * density }
        drawPhysicalWire(start, end, bends, wire.color, density, selected, if (fading) 0.22f else 1f)
        if (!fading && !touchesVoltmeter && scene.showReadings && endpointCurrent > 0.0005f &&
            scene.solution.totalCurrent > 0.001f
        ) {
            val forward = wireCurrentForward(wire, a, b, scene.solution)
            val flowStart = if (forward) start else end
            val flowEnd = if (forward) end else start
            val flowBends = if (forward) bends else bends.reversed()
            drawWireDirectionArrow(flowStart, flowEnd, flowBends, wire.color, density)
            drawElectrons(flowStart, flowEnd, flowBends, wire.color, density, scene.electronPhase)
        }
    }

    val startRef = scene.wireStart
    if (startRef != null && scene.wirePointer != null) {
        val comp = byId[startRef.componentId]
        if (comp != null) {
            val start = comp.terminalPosition(startRef.index) * density
            val end = scene.wirePointer * density
            drawDraftWire(start, end, scene.wireBends.map { it * density }, scene.wireColor, density)
        }
    }

    // Re-draw a compact binding-post cap above each connected wire end, so the
    // wire never hides which terminal it is clamped to.
    val connectedTerminals = linkedMapOf<Pair<Long, Int>, WireColor>()
    for (wire in scene.wires) {
        connectedTerminals[wire.a.componentId to wire.a.index] = wire.color
        connectedTerminals[wire.b.componentId to wire.b.index] = wire.color
    }
    for ((ref, wireColor) in connectedTerminals) {
        val comp = byId[ref.first] ?: continue
        val position = comp.terminalPosition(ref.second) * density
        drawCircle(Color.Black.copy(alpha = 0.5f), 5.2f * density, position)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFF0F3F8), Color(0xFF9AA5B4), Color(0xFF4B5563)),
                center = position + Offset(-1.2f, -1.2f) * density,
                radius = 5f * density,
            ),
            radius = 4.2f * density,
            center = position,
        )
        drawCircle(
            color = wireColor.body,
            radius = 3.2f * density,
            center = position,
            style = Stroke(1.15f * density),
        )
        drawCircle(Color(0xFF141920), 1.45f * density, position)
        drawCircle(Color.White.copy(alpha = 0.55f), 0.65f * density, position + Offset(-0.8f, -0.8f) * density)
    }

    for (component in sorted) {
        drawComponentLabel(component, scene.solution, density, scene.showReadings)
    }

    // Selected wire bend handles stay above everything so they are easy to grab.
    val selectedWire = scene.wires.firstOrNull { it.id == scene.selection.wireId }
    if (selectedWire != null) {
        val a = byId[selectedWire.a.componentId]
        val b = byId[selectedWire.b.componentId]
        if (a != null && b != null) {
            val start = a.terminalPosition(selectedWire.a.index) * density
            val end = b.terminalPosition(selectedWire.b.index) * density
            for (bend in selectedWire.bends.map { it * density }) {
                drawCircle(Color(0xFF0B1118), 8f * density, bend)
                drawCircle(LabColors.Amber, 5.2f * density, bend)
                drawCircle(Color.White.copy(alpha = 0.8f), 1.8f * density, bend)
            }
        }
    }

    // Rotation handle for magnet instruments.
    val rotationTarget = scene.components.firstOrNull {
        it.id == scene.selection.componentId && it.type.isMagnet
    }
    if (rotationTarget != null) {
        val center = rotationTarget.center * density
        val handle = rotationHandlePosition(rotationTarget) * density
        val top = (rotationTarget.center + rotateOffset(Offset(0f, -rotationTarget.type.halfHeight), rotationTarget.rotation)) * density
        drawLine(
            LabColors.Green.copy(alpha = 0.38f),
            top,
            handle,
            1.2f * density,
            StrokeCap.Round,
        )
        drawCircle(Color(0xFF0B1118), 16f * density, handle)
        drawCircle(LabColors.Green, 13f * density, handle, style = Stroke(1.4f * density))
        drawArc(
            LabColors.Green,
            startAngle = -50f,
            sweepAngle = 250f,
            useCenter = false,
            topLeft = Offset(handle.x - 7f * density, handle.y - 7f * density),
            size = Size(14f * density, 14f * density),
            style = Stroke(1.5f * density, cap = StrokeCap.Round),
        )
        val tip = Offset(handle.x + 5.6f * density, handle.y - 3.6f * density)
        drawLine(LabColors.Green, tip, tip + Offset(-2.2f, -3.4f) * density, 1.5f * density, StrokeCap.Round)
        drawLine(LabColors.Green, tip, tip + Offset(3.3f, -1.8f) * density, 1.5f * density, StrokeCap.Round)
    }

    // Terminal affordances are drawn above every instrument.
    for (component in scene.components) {
        for (index in component.type.terminalOffsets.indices) {
            val center = component.terminalPosition(index) * density
            val ref = TerminalRef(component.id, index)
            val isStart = scene.wireStart == ref
            val isHover = scene.hoveredTerminal == ref
            val show = scene.showTerminals || isStart || isHover
            if (show) {
                drawCircle(
                    color = when {
                        isHover -> LabColors.Green
                        isStart -> LabColors.Primary
                        else -> LabColors.Primary.copy(alpha = 0.32f)
                    },
                    radius = 10f * density,
                    center = center,
                    style = Stroke(1.5f * density),
                )
                drawCircle(
                    color = (if (isHover) LabColors.Green else LabColors.Primary).copy(alpha = 0.11f),
                    radius = 12.5f * density,
                    center = center,
                )
            }
            if (isHover) {
                drawCircle(LabColors.Green.copy(alpha = 0.9f), 2.8f * density, center)
            }
        }
    }
}

private data class MagneticPole(
    val position: Offset,
    val north: Boolean,
    val component: PlacedInstrument,
)

private fun DrawScope.drawMagneticFieldScene(scene: BoardScene, density: Float) {
    val polesByComponent = HashMap<Long, List<MagneticPole>>()
    for (component in scene.components) {
        val reading = scene.solution.readings[component.id] ?: ComponentReading()
        val poles = when {
            component.type == InstrumentType.BAR_MAGNET -> {
                listOf(
                    MagneticPole(
                        component.center + rotateOffset(Offset(-38f, 0f), component.rotation),
                        !component.polarityFlipped,
                        component,
                    ),
                    MagneticPole(
                        component.center + rotateOffset(Offset(38f, 0f), component.rotation),
                        component.polarityFlipped,
                        component,
                    ),
                )
            }
            component.type == InstrumentType.HORSESHOE_MAGNET -> {
                listOf(
                    MagneticPole(
                        component.center + rotateOffset(Offset(-25f, -18f), component.rotation),
                        !component.polarityFlipped,
                        component,
                    ),
                    MagneticPole(
                        component.center + rotateOffset(Offset(25f, -18f), component.rotation),
                        component.polarityFlipped,
                        component,
                    ),
                )
            }
            component.type == InstrumentType.ELECTROMAGNET && reading.energized -> {
                val reversed = (reading.signedCurrent < -0.0001f) xor component.windingReversed
                listOf(
                    MagneticPole(component.center + Offset(-44f, 0f), reversed, component),
                    MagneticPole(component.center + Offset(40f, 0f), !reversed, component),
                )
            }
            component.type == InstrumentType.SOLENOID && reading.energized -> {
                val reversed = (reading.signedCurrent < -0.0001f) xor component.windingReversed
                listOf(
                    MagneticPole(component.center + Offset(-46f, 0f), !reversed, component),
                    MagneticPole(component.center + Offset(46f, 0f), reversed, component),
                )
            }
            else -> emptyList()
        }
        if (poles.isNotEmpty()) polesByComponent[component.id] = poles
    }

    for (component in scene.components) {
        val poles = polesByComponent[component.id] ?: continue
        val center = component.center * density
        val north = poles.first { it.north }.position * density
        val south = poles.first { !it.north }.position * density
        val span = (south - north).getDistance().coerceAtLeast(40f * density)
        val color = Color(0xFF7FE3FF)
        val axis = south - north
        val axisLength = axis.getDistance().coerceAtLeast(1f)
        val normal = Offset(-axis.y / axisLength, axis.x / axisLength)
        val loopCount = 6
        for (i in 0 until loopCount) {
            val bulge = (14f + i * 15f) * density
            for (side in listOf(-1f, 1f)) {
                val offset = normal * (bulge * side)
                val path = Path().apply {
                    moveTo(north.x, north.y)
                    cubicTo(
                        north.x + offset.x, north.y + offset.y,
                        south.x + offset.x, south.y + offset.y,
                        south.x, south.y,
                    )
                }
                val alpha = 0.58f - i * 0.055f
                drawPath(path, color.copy(alpha = alpha), style = Stroke(1.6f * density, cap = StrokeCap.Round))
                if (i % 2 == 0) {
                    val mid = Offset(
                        (north.x + 3f * (north.x + offset.x) + 3f * (south.x + offset.x) + south.x) / 8f,
                        (north.y + 3f * (north.y + offset.y) + 3f * (south.y + offset.y) + south.y) / 8f,
                    )
                    val tangent = south - north
                    drawFieldArrow(mid, tangent, color.copy(alpha = alpha + 0.18f), density)
                }
            }
        }
        drawCircle(Color(0xFFFF8C8C).copy(alpha = 0.8f), 6f * density, north)
        drawCircle(Color(0xFF8CC5FF).copy(alpha = 0.8f), 6f * density, south)
        textLabel("N", north.x, north.y - 9f * density, 7f * density, Color(0xFFFF9A9A), true)
        textLabel("S", south.x, south.y - 9f * density, 7f * density, Color(0xFF9CCBFF), true)
        if (span < 1f) continue
    }

    val entries = polesByComponent.entries.toList()
    for (i in 0 until entries.size - 1) {
        for (j in i + 1 until entries.size) {
            val polesA = entries[i].value
            val polesB = entries[j].value
            var bestA = polesA.first()
            var bestB = polesB.first()
            var bestDistance = Float.MAX_VALUE
            for (a in polesA) {
                for (b in polesB) {
                    val distance = (a.position - b.position).getDistance()
                    if (distance < bestDistance) {
                        bestDistance = distance
                        bestA = a
                        bestB = b
                    }
                }
            }
            if (bestDistance > 330f) continue
            val start = bestA.position * density
            val end = bestB.position * density
            val attract = bestA.north != bestB.north
            val color = if (attract) LabColors.Primary else LabColors.Red
            val delta = end - start
            val length = delta.getDistance().coerceAtLeast(1f)
            val normal = Offset(-delta.y / length, delta.x / length)
            val bend = if (attract) -18f * density else 22f * density
            for (offsetIndex in -1..1) {
                val extra = offsetIndex * 11f * density
                val localBend = bend + extra
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(
                        (start.x + end.x) / 2f + normal.x * localBend,
                        (start.y + end.y) / 2f + normal.y * localBend,
                        end.x,
                        end.y,
                    )
                }
                drawPath(
                    path,
                    color.copy(alpha = if (offsetIndex == 0) 0.88f else 0.52f),
                    style = Stroke((if (offsetIndex == 0) 1.7f else 1.2f) * density, cap = StrokeCap.Round),
                )
            }
            val mid = Offset(
                (start.x + end.x) / 2f + normal.x * bend,
                (start.y + end.y) / 2f + normal.y * bend,
            )
            val arrowDir = if (attract) (end - start) else (start - end)
            val dir = if (arrowDir.getDistance() < 1f) Offset(1f, 0f) else arrowDir / arrowDir.getDistance()
            val tip = if (attract) end else start
            val back = tip - dir * 15f * density
            drawLine(color, tip, back + normal * 6f * density, 1.7f * density, StrokeCap.Round)
            drawLine(color, tip, back - normal * 6f * density, 1.7f * density, StrokeCap.Round)
            val label = if (attract) "吸引" else "排斥"
            drawRoundRect(
                color = Color(0xFF080B11).copy(alpha = 0.88f),
                topLeft = Offset(mid.x - 18f * density, mid.y - 9f * density),
                size = Size(36f * density, 14f * density),
                cornerRadius = CornerRadius(7f * density, 7f * density),
            )
            textLabel(label, mid.x, mid.y + 2.8f * density, 7.4f * density, color, true)
        }
    }
}

private fun DrawScope.drawDamagedOverlay(component: PlacedInstrument, density: Float) {
    val left = (component.center.x - component.type.halfWidth - 3f) * density
    val top = (component.center.y - component.type.halfHeight - 3f) * density
    val width = (component.type.width + 6f) * density
    val height = (component.type.height + 6f) * density
    drawRoundRect(
        color = Color(0xFF050608).copy(alpha = 0.48f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f * density, 10f * density),
    )
    drawRoundRect(
        color = LabColors.Red.copy(alpha = 0.62f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f * density, 10f * density),
        style = Stroke(1.4f * density),
    )
    val cx = component.center.x * density
    val cy = component.center.y * density
    val r = 13f * density
    drawLine(LabColors.Red.copy(alpha = 0.8f), Offset(cx - r, cy - r), Offset(cx + r, cy + r), 2.4f * density, StrokeCap.Round)
    drawLine(LabColors.Red.copy(alpha = 0.8f), Offset(cx + r, cy - r), Offset(cx - r, cy + r), 2.4f * density, StrokeCap.Round)
}

private fun DrawScope.drawPhysicalWire(
    start: Offset,
    end: Offset,
    bends: List<Offset>,
    color: WireColor,
    density: Float,
    selected: Boolean,
    alpha: Float = 1f,
) {
    val lane = when (color) {
        WireColor.RED -> 6.5f
        WireColor.BLACK -> -6.5f
        WireColor.WHITE -> 0f
    } * density
    val path = wirePath(start, end, bends, laneOffset = lane)
    if (selected) {
        drawPath(
            path,
            LabColors.Primary.copy(alpha = 0.52f * alpha),
            style = Stroke(12f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    withTransform({ translate(1.2f * density, 3.8f * density) }) {
        drawPath(path, Color.Black.copy(alpha = 0.58f * alpha), style = Stroke(7.5f * density, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    if (color == WireColor.BLACK) {
        drawPath(
            path,
            Color(0xFF505966).copy(alpha = alpha),
            style = Stroke(7.2f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    drawPath(
        path,
        color.body.copy(alpha = alpha),
        style = Stroke(5.0f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    withTransform({ translate(-0.7f * density, -1.1f * density) }) {
        drawPath(
            path,
            color.highlight.copy(alpha = (if (color == WireColor.BLACK) 0.55f else 0.78f) * alpha),
            style = Stroke(1.2f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    // Small connector sleeves at both ends; keep them slim so posts stay visible.
    drawCircle(color.body.copy(alpha = alpha), 2.4f * density, start)
    drawCircle(color.body.copy(alpha = alpha), 2.4f * density, end)
    drawCircle(color.highlight.copy(alpha = 0.4f * alpha), 0.9f * density, start + Offset(-0.6f, -0.6f) * density)
    drawCircle(color.highlight.copy(alpha = 0.4f * alpha), 0.9f * density, end + Offset(-0.6f, -0.6f) * density)
}

private fun DrawScope.drawDraftWire(
    start: Offset,
    end: Offset,
    bends: List<Offset>,
    color: WireColor,
    density: Float,
) {
    val path = wirePath(start, end, bends, sagFactor = 0.06f)
    drawPath(
        path,
        color.body.copy(alpha = 0.72f),
        style = Stroke(4.2f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    drawPath(
        path,
        color.highlight.copy(alpha = 0.5f),
        style = Stroke(1.1f * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    for (bend in bends) {
        drawCircle(LabColors.Amber.copy(alpha = 0.18f), 8f * density, bend)
        drawCircle(LabColors.Amber, 3.4f * density, bend)
    }
    drawCircle(LabColors.Primary.copy(alpha = 0.8f), 5f * density, end, style = Stroke(1.5f * density))
}

private fun terminalOutflow(
    component: PlacedInstrument,
    terminal: TerminalRef,
    solution: CircuitSolution,
): Boolean? {
    val reading = solution.readings[component.id] ?: return null
    if (component.type.isPowerSource) {
        val positive = if (component.polarityFlipped) 0 else 1
        return when (terminal.index) {
            positive -> true
            else -> false
        }
    }
    if (component.type.isMeter) {
        val positive = reading.meterTerminal
        if (positive == 0) return null
        val entersPositive = reading.signedCurrent > 0f
        return when (terminal.index) {
            positive -> !entersPositive
            0 -> entersPositive
            else -> null
        }
    }
    if (component.type == InstrumentType.SPDT_SWITCH) {
        val selected = when (component.switchPosition) {
            0 -> 1
            1 -> 2
            else -> null
        }
        if (selected == null) return null
        return when (terminal.index) {
            0 -> false
            selected -> true
            else -> null
        }
    }
    if (component.type in setOf(
            InstrumentType.RESISTOR,
            InstrumentType.LAMP,
            InstrumentType.TEMP_LAMP,
            InstrumentType.MOTOR,
            InstrumentType.BUZZER,
            InstrumentType.LED,
            InstrumentType.ELECTROMAGNET,
            InstrumentType.SOLENOID,
            InstrumentType.INDUCTOR,
            InstrumentType.CAPACITOR,
            InstrumentType.SWITCH,
            InstrumentType.RHEOSTAT,
            InstrumentType.RESISTANCE_BOX,
            InstrumentType.RESISTANCE_WIRE,
        )
    ) {
        if (terminal.index !in 0..1) return null
        val forward = reading.signedCurrent >= 0f
        return if (forward) terminal.index == 1 else terminal.index == 0
    }
    return null
}

private fun wireCurrentForward(
    wire: WireSegment,
    a: PlacedInstrument,
    b: PlacedInstrument,
    solution: CircuitSolution,
): Boolean {
    val outflowA = terminalOutflow(a, wire.a, solution)
    val outflowB = terminalOutflow(b, wire.b, solution)
    if (outflowA != null && outflowB != null) {
        // 两端都在向外/向内送电时，电流沿导线从另一端汇入；
        // 例如并联支路汇流段，不能简单优先取 a 端。
        if (outflowA == outflowB) return false
        return outflowA
    }
    if (outflowA != null) return outflowA
    if (outflowB != null) return !outflowB
    return true
}

private fun DrawScope.drawWireDirectionArrow(
    start: Offset,
    end: Offset,
    bends: List<Offset>,
    wireColor: WireColor,
    density: Float,
) {
    val lane = when (wireColor) {
        WireColor.RED -> 6.5f
        WireColor.BLACK -> -6.5f
        WireColor.WHITE -> 0f
    } * density
    val path = wirePath(start, end, bends, laneOffset = lane)
    val measure = PathMeasure()
    measure.setPath(path, false)
    val length = measure.length
    if (length < 55f * density) return
    val distance = length * 0.62f
    val position = measure.getPosition(distance)
    val tangent = measure.getTangent(distance)
    val tangentLength = tangent.getDistance()
    if (tangentLength < 0.001f) return
    val unit = Offset(tangent.x / tangentLength, tangent.y / tangentLength)
    val normal = Offset(-unit.y, unit.x)
    val tip = position + unit * (4.8f * density)
    val base = position - unit * (2.2f * density)
    val color = if (wireColor == WireColor.WHITE) {
        LabColors.Primary.copy(alpha = 0.85f)
    } else {
        Color(0xFFB9F0FF).copy(alpha = 0.8f)
    }
    drawLine(color, base + normal * (3.2f * density), tip, 1.25f * density, StrokeCap.Round)
    drawLine(color, base - normal * (3.2f * density), tip, 1.25f * density, StrokeCap.Round)
}

private fun DrawScope.drawElectrons(
    start: Offset,
    end: Offset,
    bends: List<Offset>,
    wireColor: WireColor,
    density: Float,
    phase: Float,
) {
    val lane = when (wireColor) {
        WireColor.RED -> 6.5f
        WireColor.BLACK -> -6.5f
        WireColor.WHITE -> 0f
    } * density
    val path = wirePath(start, end, bends, laneOffset = lane)
    val measure = PathMeasure()
    measure.setPath(path, false)
    val length = measure.length
    if (length < 30f * density) return
    val spacing = 58f * density
    var distance = (phase % spacing)
    while (distance < length) {
        val pos = measure.getPosition(distance)
        val dotColor = if (wireColor == WireColor.WHITE) LabColors.Primary else Color(0xFFA9E9FF)
        drawCircle(dotColor.copy(alpha = 0.14f), 1.55f * density, pos)
        drawCircle(dotColor.copy(alpha = 0.95f), 0.9f * density, pos)
        distance += spacing
    }
}

private fun DrawScope.drawComponentLabel(
    component: PlacedInstrument,
    solution: CircuitSolution,
    density: Float,
    showReadings: Boolean,
) {
    val reading = solution.readings[component.id] ?: ComponentReading()
    val suffix = when {
        component.damaged -> "已损坏"
        component.type == InstrumentType.BATTERY ->
            "7号×${component.batteryCells}  ${formatNumber(component.voltage)} V" +
                if (component.polarityFlipped) "  正极左" else ""
        component.type == InstrumentType.POWER_SUPPLY ->
            "${formatNumber(component.supplyVoltage)} V ${if (component.supplyAc) "AC" else "DC"}" +
                if (component.polarityFlipped) "  正极左" else ""
        component.type == InstrumentType.STORAGE_BATTERY ->
            "${formatNumber(component.storageVoltage)} V" +
                if (component.polarityFlipped) "  正极左" else ""
        component.type == InstrumentType.RESISTOR ->
            if (component.resistanceHidden) "Rx ?" else "${formatNumber(component.resistance)} Ω"
        component.type == InstrumentType.RESISTANCE_BOX ->
            "${component.resistanceBoxValue} Ω"
        component.type == InstrumentType.RESISTANCE_WIRE ->
            if (component.showWireResistance) {
                "${component.wireSample.name} · ${formatNumber(component.wireResistance)} Ω"
            } else {
                component.wireSample.name
            }
        component.type == InstrumentType.RHEOSTAT ->
            "${formatNumber(component.rheostatResistance)} / ${formatNumber(component.rheostatMax)} Ω"
        component.type == InstrumentType.SWITCH -> if (component.closed) "闭合" else "断开"
        component.type == InstrumentType.AMMETER ->
            if (showReadings) {
                "${formatAmmeter(reading.meterSignedValue)}${if (reading.meterOverRange) "  超范围" else if (reading.meterNegativeOverRange) "  超负刻度" else ""}"
            } else ""
        component.type == InstrumentType.VOLTMETER ->
            if (showReadings) {
                "${formatVoltmeter(reading.meterSignedValue)}${if (reading.meterOverRange) "  超范围" else if (reading.meterNegativeOverRange) "  超负刻度" else ""}"
            } else ""
        component.type == InstrumentType.GALVANOMETER ->
            if (showReadings) {
                "${formatNumber(reading.meterSignedValue)} μA${if (reading.meterOverRange) "  超范围" else ""}"
            } else ""
        component.type == InstrumentType.MILLIAMMETER ->
            if (showReadings) {
                "${formatNumber(reading.meterSignedValue)} mA${if (reading.meterOverRange) "  超范围" else if (reading.meterReversed) "  反接" else ""}"
            } else ""
        component.type == InstrumentType.LAMP || component.type == InstrumentType.TEMP_LAMP ->
            if (showReadings && reading.power > 0.03f) "${formatNumber(reading.power)} W" else ""
        component.type == InstrumentType.MOTOR ->
            if (showReadings) formatCurrent(reading.current) else ""
        component.type == InstrumentType.BUZZER ->
            if (showReadings) formatCurrent(reading.current) else ""
        component.type == InstrumentType.LED ->
            if (showReadings) {
                if (reading.ledReversed) "反接" else formatCurrent(reading.current)
            } else ""
        else -> ""
    }
    val text = if (suffix.isEmpty()) component.label else "${component.label}  $suffix"
    val fontSize = 9.2f * density
    val warning = component.damaged || reading.meterOverRange || reading.meterReversed || reading.ledReversed
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (warning) 0xFFFF7B82.toInt() else 0xFFC7D5E8.toInt()
        textSize = fontSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    val textWidth = paint.measureText(text)
    val x = component.center.x * density
    val y = (component.center.y + component.type.halfHeight + 16f) * density
    drawRoundRect(
        color = Color(0xFF090D13).copy(alpha = 0.88f),
        topLeft = Offset(x - textWidth / 2f - 6f * density, y - 10f * density),
        size = Size(textWidth + 12f * density, 15f * density),
        cornerRadius = CornerRadius(7f * density, 7f * density),
    )
    drawRoundRect(
        color = Color(0xFF39475A).copy(alpha = 0.55f),
        topLeft = Offset(x - textWidth / 2f - 6f * density, y - 10f * density),
        size = Size(textWidth + 12f * density, 15f * density),
        cornerRadius = CornerRadius(7f * density, 7f * density),
        style = Stroke(0.7f * density),
    )
    drawContext.canvas.nativeCanvas.drawText(text, x, y + 1.2f * density, paint)
}

/**
 * 所有规格的小灯泡共用同一亮度标准：只由实际功率决定，不再按各自额定功率归一化。
 */
private const val LAMP_BRIGHTNESS_REFERENCE_WATTS = 2.5f

private fun lampBrightness(power: Float): Float =
    (power / LAMP_BRIGHTNESS_REFERENCE_WATTS).coerceIn(0f, 1.8f)

fun formatNumber(value: Float): String {    if (value.isNaN() || value.isInfinite()) return "0"
    val rounded = Math.round(value * 10f) / 10f
    return if (kotlin.math.abs(rounded - rounded.toInt()) < 0.05f) {
        rounded.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", rounded)
    }
}

fun formatCurrent(value: Float): String {
    if (value.isNaN() || value.isInfinite()) return "0.000 A"
    return if (value < 1f) {
        String.format(java.util.Locale.US, "%.3f A", value)
    } else {
        String.format(java.util.Locale.US, "%.2f A", value)
    }
}

fun formatAmmeter(value: Float): String {
    if (value.isNaN() || value.isInfinite()) return "0.00 A"
    return String.format(java.util.Locale.US, "%.2f A", value)
}

fun formatVoltmeter(value: Float): String {
    if (value.isNaN() || value.isInfinite()) return "0.0 V"
    return String.format(java.util.Locale.US, "%.1f V", value)
}

fun rotationHandlePosition(component: PlacedInstrument): Offset =
    component.center + rotateOffset(Offset(0f, -component.type.halfHeight - 25f), component.rotation)

private fun rotateOffset(point: Offset, degrees: Float): Offset {
    if (degrees == 0f) return point
    val radians = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(radians).toFloat()
    val sin = kotlin.math.sin(radians).toFloat()
    return Offset(point.x * cos - point.y * sin, point.x * sin + point.y * cos)
}

private fun compassAngle(
    compass: PlacedInstrument,
    components: List<PlacedInstrument>,
    solution: CircuitSolution,
): Float {
    val sources = components.filter { candidate ->
        candidate.id != compass.id && (candidate.type == InstrumentType.BAR_MAGNET ||
            candidate.type == InstrumentType.HORSESHOE_MAGNET ||
            ((candidate.type == InstrumentType.ELECTROMAGNET || candidate.type == InstrumentType.SOLENOID) &&
                (solution.readings[candidate.id]?.energized == true)))
    }
    if (sources.isEmpty()) return 0f
    val nearest = sources.minByOrNull { (it.center - compass.center).getDistanceSquared() } ?: return 0f
    val delta = nearest.center - compass.center
    if (delta.getDistance() < 1f) return 0f
    var angle = Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble())).toFloat() + 90f
    val reversed = when {
        nearest.type == InstrumentType.ELECTROMAGNET ->
            ((solution.readings[nearest.id]?.signedCurrent ?: 0f) < -0.0001f) xor nearest.windingReversed
        nearest.type == InstrumentType.SOLENOID ->
            ((solution.readings[nearest.id]?.signedCurrent ?: 0f) < -0.0001f) xor nearest.windingReversed
        nearest.type.isMagnet -> nearest.polarityFlipped
        else -> false
    }
    if (reversed) angle += 180f
    return angle
}

private fun paperclipAttraction(
    paperclips: PlacedInstrument,
    components: List<PlacedInstrument>,
    solution: CircuitSolution,
): Offset {
    val sources = components.filter { candidate ->
        candidate.id != paperclips.id && (candidate.type == InstrumentType.BAR_MAGNET ||
            candidate.type == InstrumentType.HORSESHOE_MAGNET ||
            ((candidate.type == InstrumentType.ELECTROMAGNET || candidate.type == InstrumentType.SOLENOID) &&
                solution.readings[candidate.id]?.energized == true))
    }
    val nearest = sources.minByOrNull { (it.center - paperclips.center).getDistanceSquared() }
        ?: return Offset.Zero
    val delta = nearest.center - paperclips.center
    val distance = delta.getDistance()
    if (distance < 1f || distance > 280f) return Offset.Zero
    val pull = (1f - distance / 280f).coerceIn(0f, 1f)
    val move = minOf(distance * 0.72f, 86f) * pull
    return Offset(delta.x / distance * move, delta.y / distance * move)
}

private fun DrawScope.drawFieldArrow(
    position: Offset,
    direction: Offset,
    color: Color,
    density: Float,
) {
    val length = direction.getDistance()
    if (length < 0.001f) return
    val unit = Offset(direction.x / length, direction.y / length)
    val normal = Offset(-unit.y, unit.x)
    val tip = position + unit * (5f * density)
    val base = position - unit * (2.5f * density)
    drawLine(color, base + normal * (3f * density), tip, 1.2f * density, StrokeCap.Round)
    drawLine(color, base - normal * (3f * density), tip, 1.2f * density, StrokeCap.Round)
}

fun wirePath(
    start: Offset,
    end: Offset,
    bends: List<Offset> = emptyList(),
    sagFactor: Float = 0.16f,
    laneOffset: Float = 0f,
): Path {
    if (bends.isNotEmpty()) {
        return Path().apply {
            moveTo(start.x, start.y)
            for (bend in bends) lineTo(bend.x, bend.y)
            lineTo(end.x, end.y)
        }
    }
    val delta = end - start
    val distance = sqrt(delta.x * delta.x + delta.y * delta.y)
    val sag = min(34f, distance * sagFactor)
    val nx = if (distance > 0.001f) -delta.y / distance else 0f
    val ny = if (distance > 0.001f) delta.x / distance else 0f
    val c1 = Offset(start.x + delta.x * 0.30f + nx * laneOffset, start.y + delta.y * 0.08f + sag + ny * laneOffset)
    val c2 = Offset(end.x - delta.x * 0.30f + nx * laneOffset, end.y - delta.y * 0.08f + sag * 0.55f + ny * laneOffset)
    return Path().apply {
        moveTo(start.x, start.y)
        cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
    }
}

/** Samples the same path used by the renderer, for wire hit testing and bend handles. */
fun wireSamplePoints(
    start: Offset,
    end: Offset,
    bends: List<Offset> = emptyList(),
    segments: Int = 32,
    sagFactor: Float = 0.16f,
): List<Offset> {
    val path = wirePath(start, end, bends, sagFactor)
    val measure = PathMeasure()
    measure.setPath(path, false)
    val length = measure.length
    if (length <= 0.01f) return listOf(start, end)
    return (0..segments).map { index ->
        measure.getPosition(length * index / segments.toFloat())
    }
}
