package com.physlab.electric.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.CircuitStatus
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.model.Selection
import com.physlab.electric.R
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.WireSegment
import com.physlab.electric.model.DynamicState
import com.physlab.electric.model.simulateCircuit
import com.physlab.electric.model.simulateAcFrame
import com.physlab.electric.model.simulateTransientStep
import com.physlab.electric.ui.render.InstrumentVisual
import com.physlab.electric.ui.render.drawInstrument
import com.physlab.electric.ui.render.formatAmmeter
import com.physlab.electric.ui.render.formatNumber
import com.physlab.electric.ui.render.thumbnailScale
import kotlinx.coroutines.delay

@Composable
fun LabApp(
    editorOverride: LabEditorState? = null,
    initialShowReadings: Boolean = false,
    initialShowMagneticField: Boolean = true,
) {
    val rememberedEditor = remember { LabEditorState() }
    val editor = editorOverride ?: rememberedEditor
    val dynamicState = remember { androidx.compose.runtime.mutableStateOf(DynamicState()) }
    val activeWires = editor.fadingWireId?.let { rewiredId ->
        editor.wires.filterNot { it.id == rewiredId }
    } ?: editor.wires
    val hasDynamic = editor.components.any {
        it.type == InstrumentType.CAPACITOR || it.type == InstrumentType.INDUCTOR
    }
    val hasAc = editor.components.any {
        it.type == InstrumentType.POWER_SUPPLY && it.supplyAc
    }
    val acTime = remember { androidx.compose.runtime.mutableStateOf(0f) }
    val acSolution = remember { androidx.compose.runtime.mutableStateOf<CircuitSolution?>(null) }
    val dynamicTopology = editor.components.map {
        listOf(it.id, it.type.ordinal, it.capacitanceUf, it.inductanceMh)
    } + editor.wires.map { listOf(it.a.componentId, it.a.index, it.b.componentId, it.b.index) }
    LaunchedEffect(dynamicTopology, editor.topologyEpoch) {
        dynamicState.value = DynamicState()
        acTime.value = 0f
        acSolution.value = null
    }
    LaunchedEffect(hasDynamic, hasAc, dynamicTopology) {
        if (!hasDynamic && !hasAc) {
            dynamicState.value = DynamicState()
            acSolution.value = null
            return@LaunchedEffect
        }
        while (true) {
            delay(50)
            if (editor.challengeTimerPaused) continue
            val wires = editor.fadingWireId?.let { rewiredId ->
                editor.wires.filterNot { it.id == rewiredId }
            } ?: editor.wires
            if (hasAc) {
                val result = simulateAcFrame(
                    components = editor.components,
                    wires = wires,
                    state = dynamicState.value,
                    startTimeSeconds = acTime.value,
                    dt = 0.001f,
                    steps = 20,
                    epoch = editor.topologyEpoch,
                )
                dynamicState.value = result.state
                acSolution.value = result.solution
                acTime.value += 0.02f
            } else {
                dynamicState.value = simulateTransientStep(
                    components = editor.components,
                    wires = wires,
                    state = dynamicState.value,
                    dt = 0.05f,
                ).state
            }
        }
    }
    val baseSolution = remember(
        editor.components, editor.wires, editor.fadingWireId,
        dynamicState.value, acTime.value, hasAc,
    ) {
        simulateCircuit(
            editor.components,
            activeWires,
            dynamicState.value,
            0.05f,
            editor.topologyEpoch,
            if (hasAc) acTime.value else 0f,
        )
    }
    val solution = if (hasAc) acSolution.value ?: baseSolution else baseSolution
    LaunchedEffect(solution.readings, editor.challengeActive) {
        editor.startCapacitorTimerIfNeeded(solution)
    }
    LaunchedEffect(editor.challengeActive, editor.challengeTimerRunning, editor.challengeTimerPaused) {
        while (editor.challengeActive && editor.challengeTimerRunning && !editor.challengeTimerPaused) {
            delay(50)
            editor.advanceChallengeTimer(50)
        }
    }
    var boardOrigin by remember { mutableStateOf(Offset.Zero) }
    var dragType by remember { mutableStateOf<InstrumentType?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showReadings by remember { mutableStateOf(initialShowReadings) }
    var showMagneticField by remember { mutableStateOf(initialShowMagneticField) }
    val density = LocalDensity.current.density
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val guidePrefs = remember(context) {
        runCatching {
            context.applicationContext.getSharedPreferences("electric_lab", 0)
        }.getOrNull()
    }
    var challengeProgressLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (inPreview) {
            challengeProgressLoaded = true
            return@LaunchedEffect
        }
        editor.loadChallengeProgress(ChallengeProgressStore.load(context))
        challengeProgressLoaded = true
    }
    LaunchedEffect(editor.challengeScores, challengeProgressLoaded) {
        if (!challengeProgressLoaded || inPreview) return@LaunchedEffect
        ChallengeProgressStore.save(context, editor.challengeScores)
    }
    var guideStep by remember { mutableStateOf(0) }
    var guideSkipConfirm by remember { mutableStateOf(false) }
    var guideVisible by remember {
        mutableStateOf(
            !inPreview && editorOverride == null &&
                guidePrefs?.getBoolean("guide_completed", false) != true,
        )
    }
    var buzzerMuted by remember {
        mutableStateOf(guidePrefs?.getBoolean("buzzer_muted", false) == true)
    }
    var snapshotDialogOpen by remember { mutableStateOf(false) }
    var recordDialogOpen by remember { mutableStateOf(false) }
    var changelogOpen by remember { mutableStateOf(false) }
    var fieldOpen by remember { mutableStateOf(false) }
    val fieldEditor = remember { FieldEditorState() }
    var challengeExitConfirm by remember { mutableStateOf(false) }
    var snapshots by remember { mutableStateOf(SnapshotStore.load(context)) }
    val finishGuide: () -> Unit = {
        runCatching {
            guidePrefs?.edit()?.putBoolean("guide_completed", true)?.apply()
        }
        guideVisible = false
    }

    LaunchedEffect(editor.toast) {
        val message = editor.toast
        if (message != null) {
            delay(3200)
            if (editor.toast == message) editor.consumeToast()
        }
    }

    LaunchedEffect(editor.challengeActive) {
        if (editor.challengeActive) showReadings = false
    }

    LaunchedEffect(solution.newlyDamaged, solution.epoch, editor.topologyEpoch) {
        if (solution.epoch != editor.topologyEpoch) return@LaunchedEffect
        if (solution.newlyDamaged.isNotEmpty()) {
            val safeDamage = solution.newlyDamaged.filter { id ->
                val type = editor.components.firstOrNull { it.id == id }?.type
                val isSource = type == InstrumentType.BATTERY ||
                    type == InstrumentType.POWER_SUPPLY ||
                    type == InstrumentType.STORAGE_BATTERY
                !isSource || solution.status == CircuitStatus.SHORT
            }.toSet()
            if (safeDamage.isNotEmpty()) {
                editor.applyDamage(
                    safeDamage,
                    solution.reversedMeters,
                    solution.overRangeMeters,
                    solution.negativeOverRangeMeters,
                )
            }
        }
    }

    LaunchedEffect(solution.reversedMeters, solution.newlyDamaged, solution.epoch, editor.topologyEpoch) {
        if (solution.epoch != editor.topologyEpoch) return@LaunchedEffect
        val reversedHealthy = solution.reversedMeters - solution.newlyDamaged
        if (reversedHealthy.isNotEmpty()) {
            val names = editor.components
                .filter { it.id in reversedHealthy }
                .map { it.type.displayName }
                .distinct()
                .joinToString("、")
            editor.notify("${names}正负接线柱接反，指针向负刻度偏转，请对调接线柱（未损坏）")
        }
    }

    val buzzerActive = editor.components.any {
        it.type == InstrumentType.BUZZER && solution.readings[it.id]?.energized == true
    }
    val buzzerRatio = (editor.components
        .filter { it.type == InstrumentType.BUZZER }
        .maxOfOrNull { solution.readings[it.id]?.power ?: 0f } ?: 0f) / (4.5f * 4.5f / 30f)
    val buzzerVolume = (0.55f + 0.35f * buzzerRatio.coerceIn(0f, 1.5f)).coerceIn(0.5f, 1f)
    val buzzerPlayer = remember { MediaPlayer() }
    DisposableEffect(Unit) {
        onDispose { runCatching { buzzerPlayer.release() } }
    }
    LaunchedEffect(buzzerActive, buzzerMuted, buzzerVolume) {
        runCatching { buzzerPlayer.reset() }
        if (!buzzerActive || buzzerMuted) return@LaunchedEffect
        try {
            while (true) {
                buzzerPlayer.reset()
                val afd = context.resources.openRawResourceFd(R.raw.buzzer_physics)
                    ?: break
                buzzerPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                buzzerPlayer.setVolume(buzzerVolume, buzzerVolume)
                buzzerPlayer.prepare()
                buzzerPlayer.start()
                while (buzzerPlayer.isPlaying) delay(50)
                delay(1000)
            }
        } catch (_: Exception) {
            // 音频资源不可用时保持静音，不影响电路实验。
        } finally {
            runCatching { buzzerPlayer.stop() }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(LabColors.Background),
    ) {
        val compact = maxWidth < 860.dp || maxHeight > maxWidth
        val tiny = maxWidth < 700.dp || maxHeight > maxWidth
        val portrait = maxHeight > maxWidth
        LaunchedEffect(portrait) {
            if (portrait && editor.libraryOpen) editor.updateLibraryOpen(false)
        }
        Column(Modifier.fillMaxSize()) {
            EditorTopBar(
                editor = editor,
                solution = solution,
                compact = compact,
                tiny = tiny,
                portrait = portrait,
                showReadings = showReadings,
                onToggleReadings = {
                    if (!editor.challengeActive) showReadings = !showReadings
                },
                showMagneticField = showMagneticField,
                onToggleMagneticField = { showMagneticField = !showMagneticField },
                onOpenTemplates = { editor.updateTemplateDialog(true) },
                onOpenField = { fieldOpen = true },
                onOpenChallenge = { editor.openChallenge() },
                onOpenSnapshots = { snapshotDialogOpen = true },
                onOpenRecords = { recordDialogOpen = true },
                onOpenChangelog = { changelogOpen = true },
                buzzerMuted = buzzerMuted,
                onToggleBuzzer = {
                    val next = !buzzerMuted
                    buzzerMuted = next
                    runCatching {
                        guidePrefs?.edit()?.putBoolean("buzzer_muted", next)?.apply()
                    }
                    editor.notify(if (next) "蜂鸣器已静音" else "蜂鸣器已开启")
                },
                onResetView = { editor.resetPan() },
                onClear = { showClearDialog = true },
                onHelp = { editor.updateHelpOpen(true) },
                onAbout = { editor.updateAboutOpen(true) },
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                LabBoard(
                    editor = editor,
                    solution = solution,
                    showReadings = showReadings,
                    showMagneticField = showMagneticField,
                    onBoardOrigin = { boardOrigin = it },
                )

                if (editor.components.isEmpty()) {
                    if (editor.challengeActive) {
                        Text(
                            "请从左侧仪器库拖动仪器，自己完成接线",
                            color = LabColors.InkFaint,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        EmptyBoardHint(
                            modifier = Modifier.align(Alignment.Center),
                            onChooseTemplate = { editor.updateTemplateDialog(true) },
                        )
                    }
                }

                ChallengeHud(
                    editor = editor,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onOpenTable = { editor.openChallenge() },
                    onSubmit = {
                        val task = ChallengeTask.entries[
                            editor.challengeIndex.coerceIn(0, ChallengeTask.entries.lastIndex)
                        ]
                        editor.submitChallenge(task, solution) { scores ->
                            ChallengeProgressStore.save(context, scores)
                        }
                    },
                )

                ChallengeTableOverlay(
                    editor = editor,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onSubmit = {
                        val task = ChallengeTask.entries[
                            editor.challengeIndex.coerceIn(0, ChallengeTask.entries.lastIndex)
                        ]
                        editor.submitChallenge(task, solution) { scores ->
                            ChallengeProgressStore.save(context, scores)
                        }
                    },
                )

                MiniVoltmeterOverlay(
                    editor = editor,
                    solution = solution,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 58.dp),
                )

                if (editor.challengeActive) {
                    Surface(
                        onClick = { challengeExitConfirm = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp),
                        shape = RoundedCornerShape(9.dp),
                        color = LabColors.Surface.copy(alpha = 0.94f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
                    ) {
                        Text(
                            "退出闯关",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = LabColors.InkMuted,
                            fontSize = 9.5.sp,
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = editor.libraryOpen,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                    enter = slideInHorizontally(tween(220)) { -it } + fadeIn(tween(160)),
                    exit = slideOutHorizontally(tween(180)) { -it } + fadeOut(tween(140)),
                ) {
                    InstrumentLibrary(
                        editor = editor,
                        modifier = Modifier.fillMaxHeight(),
                        onDragStart = { type, position ->
                            dragType = type
                            dragPosition = position
                        },
                        onDragMove = { position -> dragPosition = position },
                        onDragEnd = { type, position ->
                            dragType = null
                            val screenDp = (position - boardOrigin) / density
                            val boardCenter = Offset(editor.boardWidth / 2f, editor.boardHeight / 2f)
                            val local = boardCenter +
                                (screenDp - boardCenter - editor.boardPan) / editor.boardZoom
                            val leftScreen = if (editor.libraryOpen) 230f else 0f
                            val visibleLeft = boardCenter.x +
                                (leftScreen - boardCenter.x - editor.boardPan.x) / editor.boardZoom
                            val visibleTop = boardCenter.y +
                                (0f - boardCenter.y - editor.boardPan.y) / editor.boardZoom
                            val visibleRight = boardCenter.x +
                                (editor.boardWidth - boardCenter.x - editor.boardPan.x) / editor.boardZoom
                            val visibleBottom = boardCenter.y +
                                (editor.boardHeight - boardCenter.y - editor.boardPan.y) / editor.boardZoom
                            if (local.x in visibleLeft..visibleRight &&
                                local.y in visibleTop..visibleBottom
                            ) {
                                editor.addInstrument(type, local)
                            } else {
                                editor.notify("请把仪器拖到实验台内")
                            }
                        },
                    )
                }

                if (!editor.libraryOpen) {
                    CollapsedLibraryHandle(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 4.dp),
                        onOpen = { editor.updateLibraryOpen(true) },
                    )
                }

                if (editor.wireStart != null) {
                    WireModeHint(
                        editor = editor,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp),
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = editor.toast != null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 58.dp),
                    enter = fadeIn(tween(120)),
                    exit = fadeOut(tween(180)),
                ) {
                    ToastPill(editor.toast ?: "")
                }
            }
        }

        val ghostType = dragType
        if (ghostType != null) {
            LibraryDragGhost(
                type = ghostType,
                position = dragPosition,
                density = density,
            )
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("清空实验台？", fontWeight = FontWeight.Bold) },
                text = { Text("所有仪器和导线都会被移除。该操作可以撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            editor.clearAll()
                            showClearDialog = false
                        },
                    ) {
                        Text("清空", color = LabColors.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("取消", color = LabColors.InkMuted)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
                textContentColor = LabColors.InkMuted,
            )
        }

        if (editor.helpOpen) {
            AlertDialog(
                onDismissRequest = { editor.updateHelpOpen(false) },
                title = { Text("使用说明", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        Modifier
                            .heightIn(max = 205.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        HelpLine("1", "实验台默认是空的。可从左侧仪器库拖动或轻点仪器放置；也可用“实验模板”快速搭建串联、并联、伏安法、电磁铁、磁体或电容充放电实验；“更多 → 任务闯关”提供 15 个分级任务，自己连电路、填表、选结论并提交验证。")
                        HelpLine("2", "任务闯关从空实验台开始，80 分即可通过并解锁下一关，100 分才能解锁下下关；每关最高分自动保存，覆盖更新不会丢失。闯关中禁止使用实验模板，读数按钮锁定，退出闯关需确认。")
                        HelpLine("3", "仪器拖出后使用默认规格；长按仪器可查看详情并调整电池节数、灯泡规格、电源电压与内阻、电表内阻、变阻器最大阻值、电容容量、电感量、绕线方向等。定值电阻规格包含 10 kΩ。伏安法测电阻的未知定值电阻不显示阻值，也不能查看或修改规格。")
                        HelpLine("4", "点击一个接线柱，再点击另一个接线柱即可接线，也可直接拖拽。电源、电流表、电压表、灵敏电流计、毫安表有正负接线柱，接线要分清“+ / −”。单刀双掷开关的公共端接输入，两个掷端分别接充电和放电支路。如果新导线与其他导线交叉，会实时提示“导线交叉了”，请拖动导线调整走线。")
                        HelpLine("5", "拖动已有导线的接线柱端点可改接，原支路会暂时断开；拖动导线中段可增加弯折。长按导线可换色、拉直或删除。开关单击闭合/断开；单刀双掷开关单击切换掷向；滑动变阻器可拖动上方滑片。")
                        HelpLine("6", "点击电流表、电压表、灵敏电流计或毫安表可放大表盘：电流表/电压表每个大格分 5 小格；灵敏电流计为中央零位，每小格 20 μA；毫安表 50 mA 量程每小格 2 mA、500 mA 量程每小格 20 mA。关闭“读数”后可输入读数验证。指针超过正测量范围 1.2 倍或越过负刻度线会损坏；灵敏电流计允许正常反向偏转。")
                        HelpLine("7", "实验台上同时有电压表和滑动变阻器时，拖动滑片会自动显示小型透明电压表放大刻度，松手约 0.7 秒后自动消失，方便观察目标电压，不遮挡操作。")
                        HelpLine("8", "所有规格小灯泡的亮度都按实际功率统一显示，实际功率相同亮度就相同。电动机和蜂鸣器额定电压为 4.5 V；用电器超过额定功率 3 倍会损坏。测小灯泡电功率时，亮度通过下拉选项选择“亮度低 / 正常发光 / 亮度高”。")
                        HelpLine("9", "磁体可拖动绿色手柄旋转，长按可翻转 N/S 磁极；电源也可在长按面板中对调正负极。电磁铁/螺线管改变电流方向或绕线方向时磁场反向。右上角“磁场”开关显示磁感线。")
                        HelpLine("10", "双指拖动平移实验台，双指捏合/张开缩放（最大 1.5 倍）；“更多 → 复位实验台视图”可恢复。实验台纵向可布置区域已扩大。")
                        HelpLine("11", "顶部“记录表”按钮在“闯关”左边，用于查看电压、电流记录；“更多 → 实验快照”可保存/加载仪器、导线、视图和记录。记录只在表盘读数验证正确后由用户选择记录。电阻丝实验板使用公共 1/2 接线柱，长按可切换样品。")
                        HelpLine("12", "新手引导第 4 步会缩到实验台右下角，按提示连接串联电路后点“提交验证”；通过后继续下一步。每一步右上角都有“跳过”，跳过前会弹窗确认。")
                        HelpLine("13", "第 14 关电容充放电：第一次出现充电电流时开始计时；点击“记录本次数据”会暂停计时和动态仿真并自动填入时间，电压表验证后填 Uc，灵敏电流计验证后填 I，保存本行后恢复计时。")
                        HelpLine("14", "关于页提供 GitHub 开源地址，可点击在浏览器中打开；应用本身仍然完全离线运行，不需要网络权限。")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { editor.updateHelpOpen(false) }) {
                        Text("知道了", color = LabColors.Primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            editor.updateHelpOpen(false)
                            guideStep = 0
                            guideVisible = true
                        },
                    ) {
                        Text("新手引导", color = LabColors.InkMuted)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
                textContentColor = LabColors.InkMuted,
            )
        }

        if (challengeExitConfirm) {
            AlertDialog(
                onDismissRequest = { challengeExitConfirm = false },
                title = { Text("退出任务闯关？", fontWeight = FontWeight.Bold) },
                text = { Text("当前关卡进度和已填写的数据将不会保留，确定退出吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            challengeExitConfirm = false
                            editor.exitChallenge()
                        },
                    ) {
                        Text("确定退出", color = LabColors.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { challengeExitConfirm = false }) {
                        Text("继续闯关", color = LabColors.InkMuted)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
                textContentColor = LabColors.InkMuted,
            )
        }

        if (editor.challengeOpen) {
            ChallengeDialog(
                editor = editor,
                solution = solution,
                onClose = { editor.closeChallenge() },
                onStart = { task -> editor.startChallenge(task) },
                onSubmit = { task ->
                    editor.submitChallenge(task, solution) { scores ->
                        ChallengeProgressStore.save(context, scores)
                    }
                },
                onNextTask = { next -> editor.startChallenge(next) },
            )
        }

        if (editor.aboutOpen) {
            val githubUrl = "https://github.com/thissideofthesea1120-collab/ElectricLab"
            val versionName = remember(context) {
                runCatching {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }.getOrNull() ?: "2.4.1"
            }
            val uriHandler = LocalUriHandler.current
            AlertDialog(
                onDismissRequest = { editor.updateAboutOpen(false) },
                title = { Text("关于", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("电学虚拟实验室  v$versionName", color = LabColors.Ink, fontWeight = FontWeight.SemiBold)
                        Text("适用于物理电学与电磁学启蒙实验。", fontSize = 12.sp)
                        Text(
                            "本项目与市面上所有相关产品及其运营方没有任何隶属、合作、授权或背书关系。\n" +
                                "本项目未使用任何第三方的插图、图标、音频、题库、界面截图或专有代码。\n" +
                                "应用中的仪器图形由项目使用 Android Canvas 独立绘制，属于通用实验器材造型。\n" +
                                "项目中使用的第三方开源库遵循各自许可证，详见构建依赖与 LICENSE。",
                            fontSize = 10.5.sp,
                            lineHeight = 16.sp,
                        )
                        Text("完全离线运行，不收集任何数据。", fontSize = 12.sp)
                        Text("GitHub 开源地址", color = LabColors.Ink, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            githubUrl,
                            color = LabColors.Primary,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(githubUrl) },
                        )
                        Text(
                            "感谢：梁圣、MrYang、류이제",
                            color = LabColors.InkMuted,
                            fontSize = 10.5.sp,
                            lineHeight = 16.sp,
                        )
                        Text("©️ 2026 휘산", color = LabColors.InkMuted, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { editor.updateAboutOpen(false) }) {
                        Text("关闭", color = LabColors.Primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
                textContentColor = LabColors.InkMuted,
            )
        }

        editor.specTarget?.let { component ->
            InstrumentSpecDialog(
                editor = editor,
                component = component,
                solution = solution,
                showReadings = showReadings,
            )
        }

        editor.meterZoomId?.let { id ->
            editor.components.firstOrNull { it.id == id }?.let { component ->
                MeterZoomDialog(
                    editor = editor,
                    component = component,
                    solution = solution,
                    showReadings = showReadings,
                )
            }
        }

        editor.wirePanelId?.let { id ->
            editor.wires.firstOrNull { it.id == id }?.let { wire ->
                WirePanelDialog(editor = editor, wire = wire)
            }
        }

        if (editor.templateDialogOpen) {
            ExperimentTemplateDialog(editor = editor)
        }

        if (snapshotDialogOpen) {
            SnapshotDialog(
                snapshots = snapshots,
                onSave = { requestedName ->
                    if (editor.components.isEmpty()) {
                        editor.notify("实验台为空，暂时没有可保存的内容")
                    } else {
                        val now = System.currentTimeMillis()
                        val name = requestedName.ifBlank {
                            "实验 " + SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(now))
                        }
                        val item = SavedSnapshot(
                            id = now,
                            createdAt = now,
                            name = name,
                            payload = editor.exportSnapshot(),
                            componentCount = editor.components.size,
                            wireCount = editor.wires.size,
                        )
                        val updated = (listOf(item) + snapshots).take(20)
                        snapshots = updated
                        SnapshotStore.save(context, updated)
                        editor.notify("已保存实验快照")
                    }
                },
                onLoad = { snapshot ->
                    if (editor.loadSnapshot(snapshot.payload)) {
                        snapshotDialogOpen = false
                    } else {
                        editor.notify("快照读取失败")
                    }
                },
                onDelete = { snapshot ->
                    val updated = snapshots.filterNot { it.id == snapshot.id }
                    snapshots = updated
                    SnapshotStore.save(context, updated)
                },
                onClose = { snapshotDialogOpen = false },
            )
        }

        if (changelogOpen) {
            AlertDialog(
                onDismissRequest = { changelogOpen = false },
                title = { Text("更新日志 · 2.5.0", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "2.5.0 · 我的模板\n\n" +
                            "• 当前实验可保存为模板\n" +
                            "• 保留仪器位置、接线和导线拐点\n" +
                            "• 我的模板支持载入、删除\n" +
                            "• 支持复制模板 JSON\n" +
                            "• 支持从剪贴板导入模板\n" +
                            "• 内置模板与我的模板分开展示\n" +
                            "• 新增普通二极管、热敏电阻、光敏电阻\n" +
                            "• 光敏电阻：光照增强，电阻减小\n" +
                            "• 热敏电阻：温度升高，电阻减小",
                        color = LabColors.InkMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 16.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { changelogOpen = false }) {
                        Text("知道了", color = LabColors.Primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
            )
        }

        if (recordDialogOpen) {
            RecordTableDialog(
                records = editor.records,
                onClear = { editor.clearRecords() },
                onClose = { recordDialogOpen = false },
            )
        }

        if (guideSkipConfirm) {
            AlertDialog(
                onDismissRequest = { guideSkipConfirm = false },
                title = { Text("退出新手引导？", fontWeight = FontWeight.Bold) },
                text = { Text("退出后将不再显示引导，确定要退出吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            guideSkipConfirm = false
                            finishGuide()
                        },
                    ) {
                        Text("确定退出", color = LabColors.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { guideSkipConfirm = false }) {
                        Text("继续引导", color = LabColors.InkMuted)
                    }
                },
                containerColor = LabColors.SurfaceRaised,
                titleContentColor = LabColors.Ink,
                textContentColor = LabColors.InkMuted,
            )
        }

        if (guideVisible) {
            OnboardingOverlay(
                step = guideStep,
                onNext = {
                    if (guideStep < OnboardingStepCount - 1) guideStep++ else finishGuide()
                },
                onSkip = { guideSkipConfirm = true },
                onValidateSeries = {
                    val result = evaluateChallenge(
                        task = ChallengeTask.SERIES_CIRCUIT,
                        components = editor.components,
                        solution = solution,
                        records = emptyList(),
                        visitedWireSamples = emptySet(),
                        rheostatPositions = emptySet(),
                        rheostatMoved = false,
                        inputs = emptyMap(),
                    )
                    if (result.passed) {
                        editor.notify("串联电路连接正确，继续下一步！")
                        if (guideStep < OnboardingStepCount - 1) guideStep++
                    } else {
                        val missing = result.checks.firstOrNull { !it.done }?.text
                            ?: "电路还没有形成正确的串联回路"
                        editor.notify("还差一点：$missing")
                    }
                },
                onLoadSeriesTemplate = { editor.loadExample() },
            )
        }

        if (fieldOpen) {
            FieldLabScreen(state = fieldEditor, onClose = { fieldOpen = false })
        }
    }
}

@Composable
private fun EditorTopBar(
    editor: LabEditorState,
    solution: CircuitSolution,
    compact: Boolean,
    tiny: Boolean,
    portrait: Boolean,
    showReadings: Boolean,
    onToggleReadings: () -> Unit,
    showMagneticField: Boolean,
    onToggleMagneticField: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenField: () -> Unit,
    onOpenChallenge: () -> Unit,
    onOpenSnapshots: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenChangelog: () -> Unit,
    buzzerMuted: Boolean,
    onToggleBuzzer: () -> Unit,
    onResetView: () -> Unit,
    onClear: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        color = LabColors.Surface.copy(alpha = 0.98f),
        shadowElevation = 10.dp,
    ) {
        Row(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .then(
                    if (portrait) {
                        Modifier.horizontalScroll(rememberScrollState())
                    } else {
                        Modifier
                    },
                )
                .padding(start = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(if (compact) 136.dp else 178.dp)) {
                Text(
                    if (tiny) "电学虚拟实验室" else "电学虚拟实验室",
                    color = LabColors.Ink,
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "实物接线 · 电路仿真",
                    color = LabColors.InkFaint,
                    fontSize = 8.8.sp,
                    maxLines = 1,
                )
            }

            StatusPill(solution)
            if (!compact && showReadings) {
                Spacer(Modifier.width(7.dp))
                StatBadge("电源", "${formatNumber(solution.batteryVoltage)} V")
                Spacer(Modifier.width(6.dp))
                StatBadge("电流", formatAmmeter(solution.totalCurrent))
            }
            if (tiny && solution.status == CircuitStatus.SHORT) {
                Spacer(Modifier.width(6.dp))
                Text("短路", color = LabColors.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(if (portrait) Modifier.width(14.dp) else Modifier.weight(1f))

            TopTextButton("电场", onOpenField)
            Spacer(Modifier.width(4.dp))
            TopTextButton("记录表", onOpenRecords)
            Spacer(Modifier.width(4.dp))
            DisplayToggle(
                label = "闯关",
                active = editor.challengeActive || editor.challengeOpen,
                accent = LabColors.Amber,
                onClick = onOpenChallenge,
            )
            Spacer(Modifier.width(4.dp))
            DisplayToggle(
                label = "读数",
                active = showReadings,
                accent = LabColors.Primary,
                onClick = onToggleReadings,
            )
            Spacer(Modifier.width(4.dp))
            DisplayToggle(
                label = "磁场",
                active = showMagneticField,
                accent = LabColors.Green,
                onClick = onToggleMagneticField,
            )
            Spacer(Modifier.width(4.dp))
            DisplayToggle(
                label = "清空",
                active = false,
                accent = LabColors.Amber,
                onClick = onClear,
            )
            Spacer(Modifier.width(4.dp))
            DisplayToggle(
                label = "静音",
                active = buzzerMuted,
                accent = LabColors.Amber,
                onClick = onToggleBuzzer,
            )
            Spacer(Modifier.width(4.dp))
            TopAction(
                icon = LabIcon.UNDO,
                label = "撤销",
                enabled = editor.canUndo,
                onClick = { editor.undo() },
            )
            TopAction(
                icon = LabIcon.REDO,
                label = "重做",
                enabled = editor.canRedo,
                onClick = { editor.redo() },
            )
            TopAction(
                icon = LabIcon.TRASH,
                label = "删除所选",
                enabled = !editor.selection.isEmpty,
                onClick = { editor.deleteSelection() },
            )
            Box {
                TopAction(
                    icon = LabIcon.MORE,
                    label = "更多",
                    enabled = true,
                    onClick = { menuOpen = true },
                )
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = LabColors.SurfaceRaised,
                ) {
                    DropdownMenuItem(
                        text = { Text("实验模板", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.EXAMPLE, LabColors.Primary, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onOpenTemplates()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("实验快照", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.FOLDER, LabColors.InkMuted, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onOpenSnapshots()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("复位实验台视图", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.RESET, LabColors.InkMuted, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onResetView()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("使用说明", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.HELP, LabColors.InkMuted, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onHelp()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("更新日志", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.INFO, LabColors.Primary, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onOpenChangelog()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("关于", fontSize = 12.sp) },
                        leadingIcon = { LabIcon(LabIcon.INFO, LabColors.InkMuted, Modifier.size(16.dp)) },
                        onClick = {
                            menuOpen = false
                            onAbout()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTextButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = LabColors.SurfaceRaised.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            color = LabColors.InkMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DisplayToggle(
    label: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (active) accent.copy(alpha = 0.14f) else LabColors.SurfaceRaised.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) accent.copy(alpha = 0.45f) else LabColors.OutlineSoft,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (active) accent else LabColors.InkFaint),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                color = if (active) accent else LabColors.InkMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatusPill(solution: CircuitSolution) {
    val (color, label) = when (solution.status) {
        CircuitStatus.CLOSED -> LabColors.Green to "通路"
        CircuitStatus.OPEN -> LabColors.Amber to "断路"
        CircuitStatus.SHORT -> LabColors.Red to "短路"
        CircuitStatus.NO_POWER -> LabColors.InkMuted to "未接电源"
    }
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LabColors.SurfaceRaised.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.OutlineSoft),
    ) {
        Row(
            Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = LabColors.InkFaint, fontSize = 8.8.sp)
            Spacer(Modifier.width(4.dp))
            Text(value, color = LabColors.InkMuted, fontSize = 9.6.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TopAction(
    icon: LabIcon,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (enabled) LabColors.InkMuted else LabColors.InkFaint.copy(alpha = 0.35f)
    Box(
        Modifier
            .size(31.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) LabColors.SurfaceRaised.copy(alpha = 0.5f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        LabIcon(icon, tint, Modifier.size(16.dp))
    }
}

@Composable
private fun WireModeHint(
    editor: LabEditorState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = LabColors.Surface.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LabColors.Primary),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                if (editor.fadingWireId != null) {
                    "改接中：拖到目标接线柱松手 · 点空白取消"
                } else {
                    "接线中：点接线柱完成 · 接好后拖动导线任意位置弯曲"
                },
                color = LabColors.Ink,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(9.dp))
            Surface(
                onClick = { editor.cancelWire() },
                shape = RoundedCornerShape(7.dp),
                color = LabColors.SurfaceHigh,
            ) {
                Text(
                    "取消",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = LabColors.InkMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EmptyBoardHint(
    modifier: Modifier = Modifier,
    onChooseTemplate: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("实验台是空的", color = LabColors.InkMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text("选择一个实验开始，或从左侧仪器库自由搭建", color = LabColors.InkFaint, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        Surface(
            onClick = onChooseTemplate,
            shape = RoundedCornerShape(9.dp),
            color = LabColors.Primary.copy(alpha = 0.16f),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Primary.copy(alpha = 0.4f)),
        ) {
            Text(
                "选择实验模板",
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                color = LabColors.Primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ToastPill(message: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = LabColors.SurfaceHigh.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LabColors.Outline),
        shadowElevation = 10.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(LabColors.Primary))
            Spacer(Modifier.width(7.dp))
            Text(message, color = LabColors.Ink, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HelpLine(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(LabColors.Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, color = LabColors.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(7.dp))
        Text(text, color = LabColors.InkMuted, fontSize = 11.5.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun LibraryDragGhost(
    type: InstrumentType,
    position: Offset,
    density: Float,
) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(LabColors.Primary.copy(alpha = 0.18f), Color.Transparent),
                center = position,
                radius = 74f * density,
            ),
            radius = 74f * density,
            center = position,
        )
        drawInstrument(
            type = type,
            center = position,
            scale = density * 0.82f,
            visual = InstrumentVisual(
                closed = type == InstrumentType.SWITCH,
                lit = type == InstrumentType.LAMP || type == InstrumentType.TEMP_LAMP,
                brightness = 0.75f,
                slider = 0.5f,
            ),
        )
    }
}
