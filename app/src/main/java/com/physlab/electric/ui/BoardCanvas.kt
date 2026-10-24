package com.physlab.electric.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.CircuitStatus
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.Selection
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.isMagnet
import com.physlab.electric.ui.render.BoardScene
import com.physlab.electric.ui.render.drawBoard
import com.physlab.electric.ui.render.rotationHandlePosition
import com.physlab.electric.ui.render.wireSamplePoints
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun LabBoard(
    editor: LabEditorState,
    solution: CircuitSolution,
    showReadings: Boolean,
    showMagneticField: Boolean,
    modifier: Modifier = Modifier,
    onBoardOrigin: (Offset) -> Unit,
) {
    val density = LocalDensity.current.density
    val currentSolution by rememberUpdatedState(solution)
    var longPressCandidate by remember { mutableStateOf<Long?>(null) }
    var longPressWireCandidate by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(longPressCandidate) {
        val id = longPressCandidate ?: return@LaunchedEffect
        delay(480)
        if (longPressCandidate == id) {
            longPressCandidate = null
            editor.openSpec(id)
        }
    }
    LaunchedEffect(longPressWireCandidate) {
        val id = longPressWireCandidate ?: return@LaunchedEffect
        delay(480)
        if (longPressWireCandidate == id) {
            longPressWireCandidate = null
            editor.openWirePanel(id)
        }
    }
    val electronTransition = rememberInfiniteTransition(label = "electron-flow")
    val electronPhase = electronTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "electron-phase",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "电学实验台，可拖动仪器、连接导线" },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .onGloballyPositioned { coords ->
                    onBoardOrigin(coords.localToRoot(Offset.Zero))
                }
                .onSizeChanged { size ->
                    editor.setBoardSize(size.width / density, size.height / density)
                }
                .pointerInput(Unit) {
                    val touchSlop = viewConfiguration.touchSlop

                    fun screenToWorld(positionPx: Offset): Offset {
                        val center = Offset(editor.boardWidth / 2f, editor.boardHeight / 2f)
                        val screenDp = positionPx / density
                        return center + (screenDp - center - editor.boardPan) / editor.boardZoom
                    }

                    fun hitTerminal(position: Offset): TerminalRef? {
                        var best: TerminalRef? = null
                        var bestDistance = 22f
                        for (component in editor.components) {
                            for (index in component.type.terminalOffsets.indices) {
                                val distance = (component.terminalPosition(index) - position).getDistance()
                                if (distance <= bestDistance) {
                                    bestDistance = distance
                                    best = TerminalRef(component.id, index)
                                }
                            }
                        }
                        return best
                    }

                    fun hitComponent(position: Offset) =
                        editor.components.asReversed().firstOrNull { component ->
                            val local = rotatePoint(position - component.center, -component.rotation)
                            abs(local.x) <= component.type.halfWidth + 7f &&
                                abs(local.y) <= component.type.halfHeight + 7f
                        }

                    fun hitRotationHandle(position: Offset): PlacedInstrument? {
                        val component = editor.selectedComponent ?: return null
                        if (!component.type.isMagnet) return null
                        return if ((rotationHandlePosition(component) - position).getDistance() <= 22f) {
                            component
                        } else {
                            null
                        }
                    }

                    fun hitWire(position: Offset): WireSegment? {
                        val byId = editor.components.associateBy { it.id }
                        return editor.wires.asReversed().firstOrNull { wire ->
                            val a = byId[wire.a.componentId] ?: return@firstOrNull false
                            val b = byId[wire.b.componentId] ?: return@firstOrNull false
                            val start = a.terminalPosition(wire.a.index)
                            val end = b.terminalPosition(wire.b.index)
                            wireSamplePoints(start, end, wire.bends).any { point ->
                                (point - position).getDistance() <= 10f
                            }
                        }
                    }

                    fun hitRheostatSlider(position: Offset): PlacedInstrument? {
                        return editor.components.asReversed().firstOrNull { component ->
                            if (component.type != InstrumentType.RHEOSTAT) return@firstOrNull false
                            val x = -42f + component.slider.coerceIn(0f, 1f) * 84f
                            (component.center + Offset(x, -36f) - position).getDistance() <= 18f
                        }
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startDp = screenToWorld(down.position)
                        val existingStart = editor.wireStart
                        val downRheostat = hitRheostatSlider(startDp)
                        val downTerminal = if (downRheostat == null) hitTerminal(startDp) else null
                        val downRotation = if (downTerminal == null && downRheostat == null) {
                            hitRotationHandle(startDp)
                        } else {
                            null
                        }
                        val downWire = if (
                            downTerminal == null && downRotation == null && downRheostat == null
                        ) {
                            hitWire(startDp)
                        } else {
                            null
                        }
                        val downComponent = if (
                            downTerminal == null && downRotation == null &&
                            downRheostat == null && downWire == null
                        ) {
                            hitComponent(startDp)
                        } else {
                            null
                        }

                        var lastDp = startDp
                        var totalMove = Offset.Zero
                        var moved = false
                        var panning = false
                        var panLast: Offset? = null
                        var pinchLastDistance: Float? = null
                        var draggingWire = false
                        var movingId: Long? = null
                        var moveOffset = Offset.Zero
                        var movingHistoryPushed = false
                        var rheostatDragId: Long? = null
                        var rheostatHistoryPushed = false
                        var rotatingId: Long? = null
                        var bendDrag: Pair<Long, Int>? = null
                        var rewire: RewireDrag? = null
                        var wireLeftStart = false

                        longPressCandidate = downComponent?.id
                        longPressWireCandidate = downWire?.id

                        if (downComponent != null) {
                            moveOffset = downComponent.center - startDp
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                longPressCandidate = null
                                longPressWireCandidate = null
                                val centroid = pressed
                                    .map { it.position }
                                    .reduce { a, b -> a + b } / pressed.size.toFloat()
                                if (pressed.size >= 2) {
                                    val pinchDistance = (pressed[0].position - pressed[1].position).getDistance()
                                    val previousDistance = pinchLastDistance
                                    if (previousDistance != null && previousDistance > 1f) {
                                        editor.zoomBy(pinchDistance / previousDistance)
                                    }
                                    pinchLastDistance = pinchDistance
                                }
                                if (!panning) {
                                    panning = true
                                    panLast = centroid
                                    moved = true
                                } else {
                                    val previous = panLast
                                    if (previous != null) {
                                        editor.panBy((centroid - previous) / density)
                                    }
                                    panLast = centroid
                                }
                                pressed.forEach { it.consume() }
                                continue
                            }
                            if (panning) break
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val positionDp = screenToWorld(change.position)
                            if (change.pressed) {
                                totalMove += change.positionChange()
                                lastDp = positionDp
                                if (!moved && totalMove.getDistance() > touchSlop) {
                                    moved = true
                                    longPressCandidate = null
                                    longPressWireCandidate = null
                                    when {
                                        downTerminal != null -> {
                                            draggingWire = true
                                            val existingWire = if (existingStart == null) {
                                                editor.wires.asReversed().firstOrNull { wire ->
                                                    wire.a == downTerminal || wire.b == downTerminal
                                                }
                                            } else {
                                                null
                                            }
                                            if (existingWire != null) {
                                                val endpointA = existingWire.a == downTerminal
                                                val fixed = if (endpointA) existingWire.b else existingWire.a
                                                rewire = RewireDrag(existingWire.id, endpointA, fixed)
                                                editor.beginWireRewire(
                                                    existingWire.id,
                                                    fixed,
                                                    existingWire.color,
                                                    positionDp,
                                                )
                                            } else if (existingStart == null) {
                                                editor.beginWire(downTerminal, positionDp)
                                            } else {
                                                editor.beginWire(existingStart, positionDp)
                                            }
                                            editor.updateHoveredTerminal(hitTerminal(positionDp))
                                        }

                                        downRotation != null -> {
                                            rotatingId = downRotation.id
                                            editor.selectComponent(downRotation.id)
                                            editor.pushHistoryForMove()
                                        }

                                        downWire != null -> {
                                            val index = editor.beginWireDeform(downWire.id, startDp)
                                            if (index != null) {
                                                bendDrag = downWire.id to index
                                            }
                                        }

                                        downRheostat != null -> {
                                            rheostatDragId = downRheostat.id
                                            editor.beginRheostatDrag()
                                            editor.selectComponent(downRheostat.id)
                                            if (!rheostatHistoryPushed) {
                                                editor.pushHistoryForMove()
                                                rheostatHistoryPushed = true
                                            }
                                        }

                                        downComponent != null && existingStart == null -> {
                                            movingId = downComponent.id
                                            editor.selectComponent(downComponent.id)
                                            if (!movingHistoryPushed) {
                                                editor.pushHistoryForMove()
                                                movingHistoryPushed = true
                                            }
                                        }
                                    }
                                }
                                if (moved) {
                                    when {
                                        draggingWire -> {
                                            val startRef = editor.wireStart
                                            val startComponent = startRef?.let { ref ->
                                                editor.components.firstOrNull { it.id == ref.componentId }
                                            }
                                            val startPosition = startComponent?.terminalPosition(startRef!!.index)
                                            val distanceToStart = if (startPosition != null) {
                                                (positionDp - startPosition).getDistance()
                                            } else {
                                                Float.MAX_VALUE
                                            }
                                            if (rewire == null) {
                                                if (distanceToStart > 26f) wireLeftStart = true
                                                if (wireLeftStart && distanceToStart < 12f) {
                                                    editor.cancelWire()
                                                    wireLeftStart = false
                                                    change.consume()
                                                } else {
                                                    editor.updateWirePointer(positionDp)
                                                    editor.warnIfWirePreviewCrosses(positionDp)
                                                    editor.updateHoveredTerminal(hitTerminal(positionDp))
                                                    change.consume()
                                                }
                                            } else {
                                                editor.updateWirePointer(positionDp)
                                                editor.warnIfWirePreviewCrosses(positionDp)
                                                editor.updateHoveredTerminal(hitTerminal(positionDp))
                                                change.consume()
                                            }
                                        }

                                        movingId != null -> {
                                            editor.moveComponent(movingId!!, positionDp + moveOffset)
                                            change.consume()
                                        }

                                        rotatingId != null -> {
                                            val component = editor.components.firstOrNull { it.id == rotatingId }
                                            if (component != null) {
                                                val angle = Math.toDegrees(
                                                    atan2(
                                                        (positionDp.y - component.center.y).toDouble(),
                                                        (positionDp.x - component.center.x).toDouble(),
                                                    ),
                                                ).toFloat() + 90f
                                                editor.updateRotationLive(component.id, angle)
                                            }
                                            change.consume()
                                        }

                                        rheostatDragId != null -> {
                                            val component = editor.components.firstOrNull { it.id == rheostatDragId }
                                            if (component != null) {
                                                val slider = ((positionDp.x - component.center.x + 42f) / 84f)
                                                    .coerceIn(0f, 1f)
                                                editor.updateSliderLive(component.id, slider)
                                            }
                                            change.consume()
                                        }

                                        bendDrag != null -> {
                                            editor.updateWireBend(bendDrag!!.first, bendDrag!!.second, positionDp)
                                            change.consume()
                                        }
                                    }
                                }
                            } else {
                                lastDp = positionDp
                                break
                            }
                        }
                        longPressCandidate = null
                        longPressWireCandidate = null
                        if (rheostatDragId != null) editor.endRheostatDrag()

                        if (moved) {
                            when {
                                draggingWire -> {
                                    if (rewire != null) {
                                        val target = hitTerminal(lastDp)
                                        val fixed = rewire!!.fixed
                                        val fixedPosition = editor.components
                                            .firstOrNull { it.id == fixed.componentId }
                                            ?.terminalPosition(fixed.index)
                                        val collapsed = target == fixed ||
                                            (target == null && fixedPosition != null &&
                                                (lastDp - fixedPosition).getDistance() < 18f)
                                        if (collapsed) {
                                            editor.deleteWire(rewire!!.wireId)
                                        } else if (target != null) {
                                            editor.rewireWireEndpoint(
                                                rewire!!.wireId,
                                                rewire!!.endpointA,
                                                target,
                                            )
                                        } else {
                                            editor.cancelWire()
                                        }
                                    } else {
                                        editor.completeWireAt(hitTerminal(lastDp))
                                    }
                                    editor.updateHoveredTerminal(null)
                                }

                                movingId != null -> editor.selectComponent(movingId)
                                rotatingId != null -> editor.selectComponent(rotatingId)
                                rheostatDragId != null -> editor.selectComponent(rheostatDragId)
                                bendDrag != null -> editor.selectWire(bendDrag.first)
                            }
                        } else {
                            when {
                                downTerminal != null -> editor.handleTerminalTap(downTerminal)
                                downRotation != null -> editor.selectComponent(downRotation.id)
                                downRheostat != null -> editor.selectComponent(downRheostat.id)
                                downWire != null -> editor.selectWire(downWire.id)
                                downComponent != null -> {
                                    if (editor.wireStart != null) {
                                        editor.cancelWire()
                                        editor.selectComponent(downComponent.id)
                                    } else if (editor.specTargetId == downComponent.id) {
                                        // A long press already opened this instrument's spec dialog.
                                    } else if (downComponent.type == InstrumentType.AMMETER ||
                                        downComponent.type == InstrumentType.VOLTMETER
                                    ) {
                                        editor.openMeterZoom(downComponent.id)
                                    } else if (downComponent.type == InstrumentType.SWITCH) {
                                        editor.selectComponent(downComponent.id)
                                        editor.toggleSwitch(downComponent.id)
                                    } else if (downComponent.type == InstrumentType.SPDT_SWITCH) {
                                        editor.selectComponent(downComponent.id)
                                        editor.setSwitchPosition(
                                            downComponent.id,
                                            if (downComponent.switchPosition == 0) 1 else 0,
                                        )
                                    } else {
                                        editor.selectComponent(downComponent.id)
                                    }
                                }

                                else -> {
                                    if (editor.wireStart != null) {
                                        editor.cancelWire()
                                    } else {
                                        editor.clearSelection()
                                    }
                                }
                            }
                        }
                    }
                },
        ) {
            val scene = BoardScene(
                components = editor.components,
                wires = editor.wires,
                solution = currentSolution,
                selection = Selection(editor.selection.componentId, editor.selection.wireId),
                wireStart = editor.wireStart,
                wirePointer = editor.wirePointer,
                wireBends = editor.wireBends,
                wireColor = editor.wireDraftColor ?: editor.wireColor,
                fadingWireId = editor.fadingWireId,
                hoveredTerminal = editor.hoveredTerminal,
                showTerminals = editor.wireStart != null || editor.selection.componentId != null,
                showReadings = showReadings,
                showMagneticField = showMagneticField,
                worldLeft = editor.worldLeft,
                worldTop = editor.worldTop,
                worldRight = editor.worldRight,
                worldBottom = editor.worldBottom,
                electronPhase = electronPhase.value,
            )
            withTransform({
                val centerX = editor.boardWidth / 2f * density
                val centerY = editor.boardHeight / 2f * density
                translate(editor.boardPan.x * density, editor.boardPan.y * density)
                scale(editor.boardZoom, editor.boardZoom, pivot = Offset(centerX, centerY))
            }) {
                drawBoard(scene, density)
            }
        }

        val damaged = editor.components.filter { it.damaged }
        for (component in damaged) {
            val buttonWidth = 56f
            val boardCenter = Offset(editor.boardWidth / 2f, editor.boardHeight / 2f)
            val screenX = boardCenter.x +
                (component.center.x - boardCenter.x) * editor.boardZoom + editor.boardPan.x
            val screenY = boardCenter.y +
                (component.center.y - boardCenter.y) * editor.boardZoom + editor.boardPan.y
            val x = (screenX - buttonWidth / 2f) * density
            val y = ((screenY - component.type.halfHeight * editor.boardZoom - 31f)
                .coerceAtLeast(2f)) * density
            Surface(
                onClick = { editor.repairComponent(component.id) },
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .width(buttonWidth.dp)
                    .size(width = buttonWidth.dp, height = 23.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3A1518),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Red.copy(alpha = 0.7f)),
                shadowElevation = 5.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabIcon(LabIcon.RESET, LabColors.Red, Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "修复",
                        color = Color(0xFFFFB4B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun rotatePoint(point: Offset, degrees: Float): Offset {
    if (degrees == 0f) return point
    val radians = Math.toRadians(degrees.toDouble())
    val cos = kotlin.math.cos(radians).toFloat()
    val sin = kotlin.math.sin(radians).toFloat()
    return Offset(point.x * cos - point.y * sin, point.x * sin + point.y * cos)
}

private data class RewireDrag(
    val wireId: Long,
    val endpointA: Boolean,
    val fixed: TerminalRef,
)
