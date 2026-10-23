package com.physlab.electric.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.physlab.electric.model.ComponentReading
import com.physlab.electric.model.InstrumentCategory
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.LampSpec
import com.physlab.electric.model.TerminalRef
import com.physlab.electric.model.WireColor
import com.physlab.electric.model.simulateCircuit
import com.physlab.electric.ui.render.InstrumentVisual
import com.physlab.electric.ui.render.drawInstrument
import com.physlab.electric.ui.render.thumbnailScale

@Composable
private fun seededEditor(width: Int, height: Int, schematic: Boolean = false): LabEditorState {
    return remember {
        LabEditorState().apply {
            setBoardSize(width.toFloat(), height.toFloat())
            seedDemo(width.toFloat(), height.toFloat(), 226f)
            if (schematic) showSchematic()
        }
    }
}

@Preview(
    name = "Electric Lab - phone landscape",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPhoneLandscapePreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(915, 412))
    }
}

@Preview(
    name = "Electric Lab - readings on",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPhoneReadingsPreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(915, 412), initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - empty start",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabEmptyStartPreview() {
    val editor = remember {
        LabEditorState().apply { setBoardSize(915f, 412f) }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - tablet landscape",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabTabletLandscapePreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(1280, 800))
    }
}

@Preview(
    name = "Electric Lab - small phone landscape",
    widthDp = 640,
    heightDp = 360,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSmallPhoneLandscapePreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(640, 360))
    }
}

@Preview(
    name = "Electric Lab - small schematic",
    widthDp = 640,
    heightDp = 360,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSmallSchematicPreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(640, 360, schematic = true))
    }
}

@Preview(
    name = "Electric Lab - schematic",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSchematicPreview() {
    ElectricLabTheme {
        LabApp(editorOverride = seededEditor(915, 412, schematic = true), initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - selected switch inspector",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabInspectorPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            selectComponent(2)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - voltmeter across lamp",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabVoltmeterPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            seedDemo(1280f, 800f, 226f)
            val lamp = components.first { it.type == InstrumentType.LAMP }
            addInstrument(InstrumentType.VOLTMETER, androidx.compose.ui.geometry.Offset(690f, 360f))
            val voltmeter = components.last()
            addWire(TerminalRef(lamp.id, 0), TerminalRef(voltmeter.id, 1), WireColor.RED)
            addWire(TerminalRef(lamp.id, 1), TerminalRef(voltmeter.id, 0), WireColor.BLACK)
            consumeToast()
            selectComponent(voltmeter.id)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - voltmeter schematic",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabVoltmeterSchematicPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            seedDemo(1280f, 800f, 226f)
            val lamp = components.first { it.type == InstrumentType.LAMP }
            addInstrument(InstrumentType.VOLTMETER, androidx.compose.ui.geometry.Offset(690f, 360f))
            val voltmeter = components.last()
            addWire(TerminalRef(lamp.id, 0), TerminalRef(voltmeter.id, 1), WireColor.RED)
            addWire(TerminalRef(lamp.id, 1), TerminalRef(voltmeter.id, 0), WireColor.BLACK)
            consumeToast()
            showSchematic()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}
@Preview(
    name = "Electric Lab - four cell battery",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabFourCellBatteryPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val battery = components.first { it.type == InstrumentType.BATTERY }
            val lamp = components.first { it.type == InstrumentType.LAMP }
            setBatteryCells(battery.id, 4)
            setLampSpec(lamp.id, LampSpec.V2_5_0_3)
            consumeToast()
            selectComponent(battery.id)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - instrument spec dialog",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSpecDialogPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val lamp = components.first { it.type == InstrumentType.LAMP }
            openSpec(lamp.id)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - bent wire",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabBentWirePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val wire = wires.first()
            val index = beginWireDeform(wire.id, Offset(500f, 245f))
            if (index != null) updateWireBend(wire.id, index, Offset(520f, 255f))
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - damaged lamp",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabDamagedPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val lamp = components.first { it.type == InstrumentType.LAMP }
            applyDamage(setOf(lamp.id))
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - magnets interaction",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMagnetsPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.MAGNETS)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - experiment templates",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabTemplateDialogPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            updateTemplateDialog(true)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - wire library",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabWireLibraryPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            updateCategory(InstrumentCategory.WIRE)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - student power supply circuit",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPowerSupplyPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.POWER_SUPPLY, androidx.compose.ui.geometry.Offset(360f, 330f))
            val supply = components.last()
            setSupplyVoltage(supply.id, 3f)
            addInstrument(InstrumentType.LAMP, androidx.compose.ui.geometry.Offset(860f, 330f))
            val lamp = components.last()
            addWire(TerminalRef(supply.id, 1), TerminalRef(lamp.id, 0), WireColor.RED)
            addWire(TerminalRef(lamp.id, 1), TerminalRef(supply.id, 0), WireColor.BLACK)
            consumeToast()
            selectComponent(supply.id)
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - student power supply schematic",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPowerSupplySchematicPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.POWER_SUPPLY, androidx.compose.ui.geometry.Offset(360f, 330f))
            val supply = components.last()
            setSupplyVoltage(supply.id, 3f)
            addInstrument(InstrumentType.LAMP, androidx.compose.ui.geometry.Offset(860f, 330f))
            val lamp = components.last()
            addWire(TerminalRef(supply.id, 1), TerminalRef(lamp.id, 0), WireColor.RED)
            addWire(TerminalRef(lamp.id, 1), TerminalRef(supply.id, 0), WireColor.BLACK)
            consumeToast()
            showSchematic()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - reversed battery",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabReversedBatteryPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val battery = components.first { it.type == InstrumentType.BATTERY }
            setPolarityFlipped(battery.id, true)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - meter zoom",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterZoomPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val ammeter = components.first { it.type == InstrumentType.AMMETER }
            openMeterZoom(ammeter.id)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - wire long press panel",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabWirePanelPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            openWirePanel(wires.first().id)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - rotated magnet",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMagnetRotatedPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.MAGNETS)
            val magnet = components.first { it.type == InstrumentType.BAR_MAGNET }
            updateRotationLive(magnet.id, 65f)
            selectComponent(magnet.id)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - battery polarity and details",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSourcePolarityPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val battery = components.first { it.type == InstrumentType.BATTERY }
            setPolarityFlipped(battery.id, true)
            openSpec(battery.id)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - two finger panned board",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPannedBoardPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            panBy(Offset(-110f, -34f))
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor)
    }
}

@Preview(
    name = "Electric Lab - onboarding",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabOnboardingPreview() {
    ElectricLabTheme {
        OnboardingOverlay(step = 0, onNext = {}, onSkip = {})
    }
}

@Preview(
    name = "Electric Lab - onboarding series",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabOnboardingSeriesPreview() {
    ElectricLabTheme {
        OnboardingOverlay(step = 3, onNext = {}, onSkip = {})
    }
}

@Preview(
    name = "Electric Lab - challenge series",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabChallengeSeriesPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            loadTemplate(ExperimentTemplate.SERIES)
            openChallenge()
            selectChallenge(ChallengeTask.SERIES_CIRCUIT.ordinal)
            val solved = simulateCircuit(components, wires)
            val result = evaluateChallenge(
                ChallengeTask.SERIES_CIRCUIT,
                components,
                solved,
                records,
                challengeVisitedWireSamples,
                challengeRheostatPositions,
                challengeRheostatMoved,
                challengeInputs,
            )
            check(result.passed)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - challenge parallel",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabChallengeParallelPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            loadTemplate(ExperimentTemplate.PARALLEL)
            openChallenge()
            selectChallenge(ChallengeTask.PARALLEL_CIRCUIT.ordinal)
            val solved = simulateCircuit(components, wires)
            val result = evaluateChallenge(
                ChallengeTask.PARALLEL_CIRCUIT,
                components,
                solved,
                records,
                challengeVisitedWireSamples,
                challengeRheostatPositions,
                challengeRheostatMoved,
                challengeInputs,
            )
            check(result.passed)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - challenge current table",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabChallengeCurrentTablePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            startChallenge(ChallengeTask.SERIES_CURRENT)
            loadTemplate(ExperimentTemplate.SERIES)
            val task = ChallengeTask.SERIES_CURRENT
            updateChallengeInput(challengeCellKey(task, 0, 0), "0.12")
            updateChallengeInput(challengeCellKey(task, 0, 1), "0.12")
            updateChallengeInput(challengeCellKey(task, 0, 2), "0.12")
            updateChallengeInput(challengeCellKey(task, 1, 0), "0.12")
            updateChallengeInput(challengeCellKey(task, 1, 1), "0.12")
            updateChallengeInput(challengeCellKey(task, 1, 2), "0.12")
            components.firstOrNull { it.type == InstrumentType.LAMP }?.let { lamp ->
                setLampSpec(lamp.id, LampSpec.entries.first { it != lamp.lampSpec })
            }
            setChallengeConclusion(task, 1)
            val solved = simulateCircuit(components, wires)
            submitChallenge(task, solved)
            check(challengeResult != null)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - challenge hud",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabChallengeHudPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            startChallenge(ChallengeTask.SERIES_CURRENT)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - rewire endpoint",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabRewirePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            val wire = wires.first()
            beginWireRewire(wire.id, wire.b, wire.color, Offset(430f, 250f))
            updateWirePointer(Offset(560f, 245f))
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - resistance wire",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabResistanceWirePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(270f, 305f))
            val battery = components.last()
            addInstrument(InstrumentType.SWITCH, Offset(430f, 170f))
            val switch = components.last()
            addInstrument(InstrumentType.RESISTANCE_WIRE, Offset(620f, 155f))
            val wire = components.last()
            addInstrument(InstrumentType.AMMETER, Offset(790f, 300f))
            val ammeter = components.last()
            addWire(TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED)
            addWire(TerminalRef(switch.id, 1), TerminalRef(wire.id, 0), WireColor.BLACK)
            addWire(TerminalRef(wire.id, 1), TerminalRef(ammeter.id, 1), WireColor.WHITE)
            addWire(TerminalRef(ammeter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK)
            toggleSwitch(switch.id)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - resistance wire spec",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabResistanceWireSpecPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.RESISTANCE_WIRE, Offset(540f, 190f))
            openSpec(components.last().id)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - snapshot round trip",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSnapshotRoundTripPreview() {
    val editor = remember {
        val source = LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            addRecord(MeasurementRecord(1_700_000_000_000, 4.5f, 0.235f, listOf(2.35f), listOf(0.235f)))
        }
        val payload = source.exportSnapshot()
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            val loaded = loadSnapshot(payload)
            check(
                loaded && components.size == source.components.size &&
                    wires.size == source.wires.size && records.size == 1,
            )
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - resistance wire spec visible",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabResistanceWireSpecVisiblePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.RESISTANCE_WIRE, Offset(540f, 190f))
            setWireResistanceVisible(components.last().id, true)
            openSpec(components.last().id)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - electromagnet reversed",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabElectromagnetReversedPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.SWITCH, Offset(450f, 260f))
            val switch = components.last()
            addInstrument(InstrumentType.ELECTROMAGNET, Offset(650f, 180f))
            val coil = components.last()
            addInstrument(InstrumentType.COMPASS, Offset(790f, 230f))
            addWire(TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED)
            addWire(TerminalRef(switch.id, 1), TerminalRef(coil.id, 0), WireColor.RED)
            addWire(TerminalRef(coil.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            toggleSwitch(switch.id)
            setPolarityFlipped(battery.id, true)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - solenoid reversed winding",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSolenoidWindingPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.SWITCH, Offset(450f, 260f))
            val switch = components.last()
            addInstrument(InstrumentType.SOLENOID, Offset(660f, 190f))
            val coil = components.last()
            addInstrument(InstrumentType.COMPASS, Offset(800f, 250f))
            addWire(TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED)
            addWire(TerminalRef(switch.id, 1), TerminalRef(coil.id, 0), WireColor.RED)
            addWire(TerminalRef(coil.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            toggleSwitch(switch.id)
            setWindingReversed(coil.id, true)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - electromagnet winding spec",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabElectromagnetWindingSpecPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.ELECTROMAGNET, Offset(540f, 200f))
            setWindingReversed(components.last().id, true)
            openSpec(components.last().id)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - meter reversed damage",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterReversedPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.AMMETER, Offset(500f, 250f))
            val meter = components.last()
            addInstrument(InstrumentType.RESISTOR, Offset(700f, 300f))
            val resistor = components.last()
            // 正极接到电流表的“−”接线柱，模拟正负接线柱接反。
            addWire(TerminalRef(battery.id, 1), TerminalRef(meter.id, 0), WireColor.RED)
            addWire(TerminalRef(meter.id, 1), TerminalRef(resistor.id, 0), WireColor.WHITE)
            addWire(TerminalRef(resistor.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            val solved = simulateCircuit(components, wires)
            check(solved.reversedMeters.contains(meter.id) && solved.negativeOverRangeMeters.contains(meter.id))
            applyDamage(
                solved.newlyDamaged,
                solved.reversedMeters,
                solved.overRangeMeters,
                solved.negativeOverRangeMeters,
            )
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - voltmeter reversed damage",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabVoltmeterReversedPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.RESISTOR, Offset(650f, 160f))
            val resistor = components.last()
            addInstrument(InstrumentType.VOLTMETER, Offset(650f, 300f))
            val meter = components.last()
            addWire(TerminalRef(battery.id, 1), TerminalRef(resistor.id, 0), WireColor.RED)
            addWire(TerminalRef(resistor.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            // 电压表“−”接高电位、“+”接低电位：电流从负接线柱流入。
            addWire(TerminalRef(resistor.id, 0), TerminalRef(meter.id, 0), WireColor.RED)
            addWire(TerminalRef(resistor.id, 1), TerminalRef(meter.id, 1), WireColor.BLACK)
            val solved = simulateCircuit(components, wires)
            check(solved.reversedMeters.contains(meter.id) && solved.negativeOverRangeMeters.contains(meter.id))
            applyDamage(
                solved.newlyDamaged,
                solved.reversedMeters,
                solved.overRangeMeters,
                solved.negativeOverRangeMeters,
            )
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - meter negative scale",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterNegativeScalePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(230f, 285f))
            val battery = components.last()
            addInstrument(InstrumentType.RESISTOR, Offset(475f, 155f))
            val resistor = components.last().also { setResistance(it.id, 40f) }
            addInstrument(InstrumentType.AMMETER, Offset(675f, 285f))
            val ammeter = components.last()
            addWire(TerminalRef(battery.id, 1), TerminalRef(resistor.id, 0), WireColor.RED)
            addWire(TerminalRef(resistor.id, 1), TerminalRef(ammeter.id, 0), WireColor.RED)
            addWire(TerminalRef(ammeter.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            val solved = simulateCircuit(components, wires)
            val reading = solved.readings[ammeter.id] ?: ComponentReading()
            check(reading.meterSignedValue < 0f)
            check(!solved.newlyDamaged.contains(ammeter.id))
            openMeterZoom(ammeter.id)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - meter over range damage",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterOverRangePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.RESISTOR, Offset(650f, 170f))
            val resistor = components.last()
            setResistance(resistor.id, 1f)
            addInstrument(InstrumentType.AMMETER, Offset(650f, 300f))
            val meter = components.last()
            addWire(TerminalRef(battery.id, 1), TerminalRef(resistor.id, 0), WireColor.RED)
            addWire(TerminalRef(resistor.id, 1), TerminalRef(meter.id, 1), WireColor.WHITE)
            addWire(TerminalRef(meter.id, 0), TerminalRef(battery.id, 0), WireColor.BLACK)
            val solved = simulateCircuit(components, wires)
            check(solved.overRangeMeters.contains(meter.id) && solved.newlyDamaged.contains(meter.id))
            applyDamage(solved.newlyDamaged, solved.reversedMeters, solved.overRangeMeters)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - meter reading quiz",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterQuizPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            seedDemo(915f, 412f, 226f)
            components.firstOrNull { it.type == InstrumentType.AMMETER }?.let { openMeterZoom(it.id) }
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = false)
    }
}

@Preview(
    name = "Electric Lab - meter reading quiz small",
    widthDp = 640,
    heightDp = 360,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMeterQuizSmallPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(640f, 360f)
            seedDemo(640f, 360f, 226f)
            components.firstOrNull { it.type == InstrumentType.AMMETER }?.let { openMeterZoom(it.id) }
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = false)
    }
}

@Preview(
    name = "Electric Lab - pinch zoom",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPinchZoomPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            seedDemo(915f, 412f, 26f)
            zoomBy(1.5f)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - independent source short",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabIndependentSourceShortPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(240f, 300f))
            val shorted = components.last()
            addWire(TerminalRef(shorted.id, 1), TerminalRef(shorted.id, 0), WireColor.RED)

            addInstrument(InstrumentType.BATTERY, Offset(650f, 300f))
            val healthy = components.last()
            addInstrument(InstrumentType.LAMP, Offset(650f, 145f))
            val lamp = components.last()
            addWire(TerminalRef(healthy.id, 1), TerminalRef(lamp.id, 0), WireColor.RED)
            addWire(TerminalRef(lamp.id, 1), TerminalRef(healthy.id, 0), WireColor.BLACK)

            val solved = simulateCircuit(components, wires)
            check(solved.newlyDamaged.contains(shorted.id))
            check(!solved.newlyDamaged.contains(healthy.id))
            applyDamage(solved.newlyDamaged, solved.reversedMeters, solved.overRangeMeters)
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - paperclips",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabPaperclipsPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            addInstrument(InstrumentType.BAR_MAGNET, Offset(520f, 170f))
            addInstrument(InstrumentType.PAPERCLIPS, Offset(410f, 220f))
            addInstrument(InstrumentType.PAPERCLIPS, Offset(640f, 240f))
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - record table",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabRecordTablePreview() {
    val records = listOf(
        MeasurementRecord(1_700_000_000_000, 4.5f, 0.235f, listOf(2.35f, 1.9f), listOf(0.235f, 0.118f)),
        MeasurementRecord(1_700_000_030_000, 4.5f, 0.312f, listOf(2.34f, 1.88f), listOf(0.312f, 0.156f)),
        MeasurementRecord(1_700_000_060_000, 4.5f, 0.418f, listOf(2.33f, 1.86f), listOf(0.418f, 0.209f)),
    )
    ElectricLabTheme {
        RecordTableDialog(records = records, onClear = {}, onClose = {})
    }
}

@Preview(
    name = "Electric Lab - snapshot dialog",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSnapshotDialogPreview() {
    val snapshots = listOf(
        SavedSnapshot(1, 1_700_000_000_000, "实验 10-20 22:40", "{}", 6, 6),
        SavedSnapshot(2, 1_700_000_060_000, "实验 10-20 22:46", "{}", 4, 3),
    )
    ElectricLabTheme {
        SnapshotDialog(
            snapshots = snapshots,
            onSave = {},
            onLoad = {},
            onDelete = {},
            onClose = {},
        )
    }
}

@Preview(
    name = "Electric Lab - parallel template",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabParallelTemplatePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.PARALLEL)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - ohm law template",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabOhmLawTemplatePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.OHM_LAW)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - ohm law readings",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabOhmLawReadingsPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.OHM_LAW)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - series template",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabSeriesTemplatePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.SERIES)
            consumeToast()
        }
    }
    ElectricLabTheme {
        LabApp(editorOverride = editor, initialShowReadings = true)
    }
}

@Preview(
    name = "Electric Lab - motor fan",
    widthDp = 915,
    heightDp = 412,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabMotorFanPreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(915f, 412f)
            updateLibraryOpen(false)
            addInstrument(InstrumentType.BATTERY, Offset(260f, 300f))
            val battery = components.last()
            addInstrument(InstrumentType.SWITCH, Offset(470f, 260f))
            val switch = components.last()
            addInstrument(InstrumentType.MOTOR, Offset(700f, 200f))
            val motor = components.last()
            addWire(TerminalRef(battery.id, 1), TerminalRef(switch.id, 0), WireColor.RED)
            addWire(TerminalRef(switch.id, 1), TerminalRef(motor.id, 0), WireColor.BLACK)
            addWire(TerminalRef(motor.id, 1), TerminalRef(battery.id, 0), WireColor.BLACK)
            toggleSwitch(switch.id)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Electric Lab - electromagnet template",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun LabElectromagnetTemplatePreview() {
    val editor = remember {
        LabEditorState().apply {
            setBoardSize(1280f, 800f)
            loadTemplate(ExperimentTemplate.ELECTROMAGNET)
            consumeToast()
        }
    }
    ElectricLabTheme { LabApp(editorOverride = editor) }
}

@Preview(
    name = "Instrument library - all instruments",
    widthDp = 1280,
    heightDp = 760,
    showBackground = true,
    backgroundColor = 0xFF050608,
    showSystemUi = false,
)
@Composable
fun InstrumentGalleryPreview() {
    ElectricLabTheme {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF050608))
                .padding(22.dp),
        ) {
            Text(
                "仪器库 · 立体渲染总览",
                color = Color(0xFFF1F6FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "按 2024 人教版初中物理教材插图风格绘制 · 无需旋转",
                color = Color(0xFF97A4B8),
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(14.dp))
            for (row in InstrumentType.entries.filterNot { it == InstrumentType.STORAGE_BATTERY }.chunked(5)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { type ->
                        GalleryCard(type, Modifier.weight(1f))
                    }
                    repeat(5 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun GalleryCard(type: InstrumentType, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF10151D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283345)),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val artScale = thumbnailScale(type, size.width / density - 18f, size.height / density - 12f) * density
                drawInstrument(
                    type = type,
                    center = Offset(size.width / 2f, size.height / 2f),
                    scale = artScale,
                    visual = InstrumentVisual(
                        closed = type == InstrumentType.SWITCH,
                        lit = type == InstrumentType.LAMP,
                        brightness = 0.75f,
                        slider = 0.58f,
                    ),
                )
            }
            Text(
                type.displayName,
                color = Color(0xFFB9C7DB),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
