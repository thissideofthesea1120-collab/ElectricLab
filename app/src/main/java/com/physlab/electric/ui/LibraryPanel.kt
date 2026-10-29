package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.InstrumentCategory
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.WireColor
import com.physlab.electric.ui.render.InstrumentVisual
import com.physlab.electric.ui.render.drawInstrument
import com.physlab.electric.ui.render.thumbnailScale
import kotlin.math.abs
import kotlin.math.abs

val LibraryWidth = 218.dp

@Composable
fun InstrumentLibrary(
    editor: LabEditorState,
    modifier: Modifier = Modifier,
    onDragStart: (InstrumentType, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (InstrumentType, Offset) -> Unit,
) {
    Surface(
        modifier = modifier
            .width(LibraryWidth)
            .fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        color = LabColors.Surface.copy(alpha = 0.965f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline.copy(alpha = 0.85f)),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(31.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(LabColors.Primary.copy(alpha = 0.28f), LabColors.Primary.copy(alpha = 0.06f)),
                            ),
                        )
                        .border(1.dp, LabColors.Primary.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    LabIcon(LabIcon.FOLDER, LabColors.Primary, Modifier.size(18.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("仪器库", color = LabColors.Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("拖到实验台 · 长按看详情", color = LabColors.InkFaint, fontSize = 9.2.sp)
                }
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(LabColors.SurfaceHigh.copy(alpha = 0.72f))
                        .border(1.dp, LabColors.Outline, CircleShape)
                        .semantics { contentDescription = "隐藏仪器库" }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                var released = true
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        if (released) editor.updateLibraryOpen(false)
                                        break
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    LabIcon(LabIcon.CHEVRON_LEFT, LabColors.InkMuted, Modifier.size(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "全部仪器",
                    color = LabColors.InkFaint,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 2.dp, top = 2.dp),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(InstrumentType.entries.filterNot { it == InstrumentType.STORAGE_BATTERY }) { type ->
                    LibraryItem(
                        type = type,
                        onTap = { editor.addInstrumentAtCenter(type) },
                        onDragStart = onDragStart,
                        onDragMove = onDragMove,
                        onDragEnd = onDragEnd,
                    )
                }
            }

            WirePaletteRow(editor)
        }
    }
}

@Composable
private fun CategoryPill(
    category: InstrumentCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = categoryAccent(category)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accent.copy(alpha = 0.18f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) accent.copy(alpha = 0.55f) else LabColors.OutlineSoft,
        ),
    ) {
        Text(
            category.shortTitle,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            color = if (selected) accent else LabColors.InkMuted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}


@Composable
private fun LibraryItem(
    type: InstrumentType,
    onTap: () -> Unit,
    onDragStart: (InstrumentType, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (InstrumentType, Offset) -> Unit,
) {
    val density = LocalDensity.current.density
    val accent = categoryAccent(type.category)
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val previewWidth = 80f
    val previewHeight = 45f
    val artScale = thumbnailScale(type, previewWidth, previewHeight)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LabColors.SurfaceRaised.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
        modifier = Modifier
            .height(84.dp)
            .onGloballyPositioned { coordinates = it }
            .pointerInput(type) {
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragging = false
                    var aborted = false
                    var total = Offset.Zero
                    var lastRoot = coordinates?.localToRoot(down.position) ?: down.position
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val delta = change.positionChange()
                        total += delta
                        lastRoot = coordinates?.localToRoot(change.position) ?: change.position
                        if (!dragging && total.getDistance() > slop) {
                            // A mostly vertical gesture belongs to the library's scroll;
                            // dragging an instrument into the lab is a horizontal gesture.
                            if (abs(total.x) >= abs(total.y) * 0.8f) {
                                dragging = true
                                onDragStart(type, lastRoot)
                            } else {
                                aborted = true
                                break
                            }
                        }
                        if (dragging) {
                            onDragMove(lastRoot)
                            change.consume()
                        }
                    }
                    if (dragging) {
                        onDragEnd(type, lastRoot)
                    } else if (!aborted) {
                        onTap()
                    }
                }
            },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxWidth().height(52.dp)) {
                    drawInstrument(
                        type = type,
                        center = Offset(size.width / 2f, size.height / 2f + 2f * density),
                        scale = artScale * density,
                        visual = InstrumentVisual(
                            closed = type == InstrumentType.SWITCH,
                            lit = type == InstrumentType.LAMP || type == InstrumentType.TEMP_LAMP,
                            brightness = 0.6f,
                            slider = 0.55f,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                type.displayName,
                color = LabColors.Ink,
                fontSize = 10.2.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun CollapsedLibraryHandle(
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Surface(
        onClick = onOpen,
        modifier = modifier
            .width(38.dp)
            .height(94.dp)
            .semantics { contentDescription = "打开仪器库" },
        shape = RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp),
        color = LabColors.SurfaceRaised.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LabIcon(LabIcon.CHEVRON_RIGHT, LabColors.Primary, Modifier.size(16.dp))
            Spacer(Modifier.height(5.dp))
            Text("仪", color = LabColors.InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
            Text("器", color = LabColors.InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
            Text("库", color = LabColors.InkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
        }
    }
}

fun categoryAccent(category: InstrumentCategory): Color = when (category) {
    InstrumentCategory.POWER -> Color(0xFFFFC857)
    InstrumentCategory.LOAD -> Color(0xFFFF8E7A)
    InstrumentCategory.METER -> Color(0xFF6FD5FF)
    InstrumentCategory.CONTROL -> Color(0xFFB9A2FF)
    InstrumentCategory.DYNAMIC -> Color(0xFFFFA45C)
    InstrumentCategory.MAGNET -> Color(0xFF62E6A7)
    InstrumentCategory.WIRE -> Color(0xFFFFB28C)
}

@Composable
private fun WirePaletteRow(editor: LabEditorState) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
    ) {
        Text(
            "导线颜色 · 选色后点击接线柱接线",
            color = LabColors.InkFaint,
            fontSize = 9.2.sp,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (color in WireColor.entries) {
                val selected = editor.wireColor == color
                Surface(
                    onClick = { editor.chooseWireColor(color) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = if (selected) color.body.copy(alpha = 0.18f) else LabColors.SurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) color.highlight.copy(alpha = 0.8f) else LabColors.OutlineSoft,
                    ),
                ) {
                    Row(
                        Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color.body),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            color.displayName,
                            color = if (selected) color.highlight else LabColors.InkMuted,
                            fontSize = 9.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WireSpoolCard(
    color: WireColor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = LabColors.SurfaceRaised.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) color.highlight.copy(alpha = 0.85f) else LabColors.OutlineSoft,
        ),
        modifier = modifier.height(96.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 2f * density
                drawOval(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(cx - 25f * density, cy + 17f * density),
                    size = Size(50f * density, 9f * density),
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF454E5D), Color(0xFF141920)),
                    ),
                    topLeft = Offset(cx - 30f * density, cy - 17f * density),
                    size = Size(60f * density, 36f * density),
                    cornerRadius = CornerRadius(9f * density, 9f * density),
                )
                drawCircle(Color(0xFF202631), 21f * density, Offset(cx, cy))
                drawCircle(color.body, 18f * density, Offset(cx, cy))
                for (i in 0 until 5) {
                    drawCircle(
                        color.highlight.copy(alpha = 0.35f),
                        16f * density - i * 2.7f * density,
                        Offset(cx, cy),
                        style = Stroke(1.0f * density),
                    )
                }
                drawCircle(Color(0xFF0C1016), 6.5f * density, Offset(cx, cy))
                drawCircle(Color(0xFF596273), 4.2f * density, Offset(cx, cy))
                drawCircle(color.highlight.copy(alpha = 0.6f), 1.4f * density, Offset(cx - 1f * density, cy - 1f * density))
                drawLine(
                    color.body,
                    Offset(cx + 17f * density, cy + 5f * density),
                    Offset(cx + 33f * density, cy + 17f * density),
                    2.8f * density,
                    StrokeCap.Round,
                )
            }
            Text(
                "${color.displayName}导线",
                color = if (selected) color.highlight else LabColors.Ink,
                fontSize = 9.2.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
