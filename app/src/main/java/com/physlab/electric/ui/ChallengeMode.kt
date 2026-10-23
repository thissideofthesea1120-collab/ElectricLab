package com.physlab.electric.ui

import com.physlab.electric.model.CircuitSolution
import com.physlab.electric.model.CircuitStatus
import com.physlab.electric.model.InstrumentType
import com.physlab.electric.model.PlacedInstrument
import com.physlab.electric.ui.render.formatNumber
import kotlin.math.abs
import kotlin.math.max

data class ChallengeConclusion(
    val question: String,
    val options: List<String>,
    val correct: Int,
)

data class ChallengeTableSpec(
    val columns: List<String>,
    val unit: String,
    val hint: String,
    val rowLabels: List<String> = listOf(""),
    val rowHeader: String? = null,
    val choiceColumn: Int? = null,
    val choiceOptions: List<String> = emptyList(),
    val calculatedColumns: Set<Int> = emptySet(),
    val conclusion: ChallengeConclusion? = null,
)

enum class ChallengeTarget {
    FIXED_RESISTOR,
    TEMP_LAMP,
}

enum class ChallengeTask(
    val title: String,
    val level: String,
    val description: String,
    val table: ChallengeTableSpec? = null,
    val target: ChallengeTarget? = null,
) {
    SERIES_CIRCUIT(
        "串联电路",
        "基础",
        "自己从仪器库选择电源、开关和两只小灯泡，连接一个串联电路。",
    ),
    PARALLEL_CIRCUIT(
        "并联电路",
        "基础",
        "自己连接两只小灯泡并联的电路。",
    ),
    SERIES_CURRENT(
        "串联电路电流的规律",
        "进阶",
        "在串联电路中测量 A、B、C 三处电流；更换一次小灯泡规格后再测一组，填写两组数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("A 点电流", "B 点电流", "C 点电流"),
            unit = "A",
            hint = "串联电路中，A、B、C 三处电流应相等；两组数据要求更换过一次小灯泡规格。",
            rowLabels = listOf("原规格", "更换规格后"),
            conclusion = ChallengeConclusion(
                "串联电路中电流有什么规律？",
                listOf(
                    "电流从正极到负极逐渐减小",
                    "电流处处相等",
                    "电流与电阻成正比",
                ),
                1,
            ),
        ),
    ),
    PARALLEL_CURRENT(
        "并联电路电流的规律",
        "进阶",
        "在并联电路中测量干路 A 点和两条支路 B、C 点电流；更换一次小灯泡规格后再测一组，填写两组数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("干路 A 点电流", "支路 B 点电流", "支路 C 点电流"),
            unit = "A",
            hint = "并联电路中，A = B + C；两组数据要求更换过一次小灯泡规格。",
            rowLabels = listOf("原规格", "更换规格后"),
            conclusion = ChallengeConclusion(
                "并联电路中电流有什么规律？",
                listOf(
                    "各支路电流一定相等",
                    "干路电流小于任一支路电流",
                    "干路电流等于各支路电流之和",
                ),
                2,
            ),
        ),
    ),
    SERIES_VOLTAGE(
        "串联电路电压的规律",
        "进阶",
        "测量串联电路总电压和两只灯泡两端电压；更换一次小灯泡规格后再测一组，填写两组数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("总电压 U", "L1 电压 U1", "L2 电压 U2"),
            unit = "V",
            hint = "串联电路中，U = U1 + U2；两组数据要求更换过一次小灯泡规格。",
            rowLabels = listOf("原规格", "更换规格后"),
            conclusion = ChallengeConclusion(
                "串联电路中电压有什么规律？",
                listOf(
                    "各部分电压一定相等",
                    "总电压等于各部分电压之和",
                    "总电压小于各部分电压",
                ),
                1,
            ),
        ),
    ),
    PARALLEL_VOLTAGE(
        "并联电路电压的规律",
        "进阶",
        "测量并联电路两条支路电压和总电压；更换一次小灯泡规格后再测一组，填写两组数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("L1 电压 U1", "L2 电压 U2", "总电压 U"),
            unit = "V",
            hint = "并联电路中，U1 = U2 = U；两组数据要求更换过一次小灯泡规格。",
            rowLabels = listOf("原规格", "更换规格后"),
            conclusion = ChallengeConclusion(
                "并联电路中电压有什么规律？",
                listOf(
                    "支路电压之和等于总电压",
                    "支路电阻越大，电压越小",
                    "各支路电压相等，且等于总电压",
                ),
                2,
            ),
        ),
    ),
    RESISTANCE_FACTORS(
        "电阻大小与什么因素有关",
        "进阶",
        "接入电阻丝实验板，依次测量四种电阻丝的电流，填写表格并选择结论。",
        ChallengeTableSpec(
            columns = listOf("电流 / A"),
            unit = "A",
            hint = "保持电源电压相同，比较不同材料、长度、粗细的电阻丝电流。",
            rowHeader = "电阻丝",
            rowLabels = listOf(
                "长细镍铬合金丝",
                "长细铁铬合金丝",
                "长粗铁铬合金丝",
                "短细铁铬合金丝",
            ),
            conclusion = ChallengeConclusion(
                "电阻大小与什么因素有关？",
                listOf(
                    "只与导体两端的电压有关",
                    "与导体的材料、长度、横截面积有关",
                    "只与通过导体的电流有关",
                ),
                1,
            ),
        ),
    ),
    RHEOSTAT(
        "练习使用滑动变阻器",
        "基础",
        "把滑动变阻器串联接入电路，移动滑片到至少两个不同位置，观察电流变化。",
    ),
    OHM_LAW_VOLTAGE(
        "电流与电压的关系",
        "综合",
        "用定值电阻、滑动变阻器、电压表和电流表，改变定值电阻两端电压，记录至少两组 U、I 数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("电压 U / V", "电流 I / A"),
            unit = "",
            hint = "保持电阻不变，改变电压，观察电流与电压的关系。",
            rowLabels = listOf("第 1 组", "第 2 组"),
            conclusion = ChallengeConclusion(
                "电阻一定时，电流与电压有什么关系？",
                listOf(
                    "电流与电压成反比",
                    "电流与电压无关",
                    "电流与电压成正比",
                ),
                2,
            ),
        ),
    ),
    OHM_LAW_RESISTANCE(
        "电流与电阻的关系",
        "综合",
        "更换不同阻值的定值电阻，每次通过滑动变阻器保持定值电阻两端电压不变，记录至少两组 R、I 数据并选择结论。",
        ChallengeTableSpec(
            columns = listOf("电阻 R / Ω", "电流 I / A"),
            unit = "",
            hint = "保持定值电阻两端电压不变，更换不同电阻，观察电流与电阻的关系。",
            rowLabels = listOf("第 1 组", "第 2 组"),
            calculatedColumns = setOf(0),
            conclusion = ChallengeConclusion(
                "电压一定时，电流与电阻有什么关系？",
                listOf(
                    "电流与电阻成正比",
                    "电流与电阻成反比",
                    "电流与电阻无关",
                ),
                1,
            ),
        ),
    ),
    VOLT_AMMETER_RESISTOR(
        "伏安法测电阻",
        "综合",
        "实验台上已放置一个未知定值电阻（5/10/15/20 Ω 随机）。连接伏安法电路，测一组 U、I，算出 R。",
        ChallengeTableSpec(
            columns = listOf("电压 U / V", "电流 I / A", "电阻 R / Ω"),
            unit = "",
            hint = "根据 R = U / I 计算定值电阻阻值。",
            calculatedColumns = setOf(2),
        ),
        ChallengeTarget.FIXED_RESISTOR,
    ),
    VOLT_AMMETER_LAMP(
        "伏安法测小灯泡电阻",
        "综合",
        "实验台上已放置一个阻值随温度变化的小灯泡（规格随机）。测量较低和较高电压下的 U、I、R。",
        ChallengeTableSpec(
            columns = listOf("电压 U / V", "电流 I / A", "电阻 R / Ω"),
            unit = "",
            hint = "小灯泡电阻随温度升高而增大，较高电压下 R 应更大。",
            rowLabels = listOf(
                "较低电压（低于额定电压）",
                "额定电压",
                "较高电压（额定电压的 1.2 倍）",
            ),
            calculatedColumns = setOf(2),
        ),
        ChallengeTarget.TEMP_LAMP,
    ),
    LAMP_POWER(
        "测量小灯泡电功率",
        "综合",
        "实验台上已放置一个额定电压随机的小灯泡。分别测量低于额定电压、额定电压、1.2 倍额定电压下的 U、I、P，并选择小灯泡亮度。",
        ChallengeTableSpec(
            columns = listOf("电压 U / V", "电流 I / A", "电功率 P / W", "小灯泡亮度"),
            unit = "",
            hint = "P = UI；低于额定电压时亮度低，额定电压时正常发光，1.2 倍额定电压时亮度高。",
            rowLabels = listOf("低于额定电压", "额定电压", "高于额定电压 1.2 倍"),
            choiceColumn = 3,
            choiceOptions = listOf("亮度低", "正常发光", "亮度高"),
            calculatedColumns = setOf(2, 3),
        ),
        ChallengeTarget.TEMP_LAMP,
    ),
}

data class ChallengeCheck(val text: String, val done: Boolean)

data class ChallengeResult(
    val checks: List<ChallengeCheck>,
    val score: Int,
) {
    val passed: Boolean get() = checks.isNotEmpty() && checks.all { it.done }
}

fun challengeCellKey(task: ChallengeTask, row: Int, column: Int): String =
    "${task.name}:$row:$column"

private fun parseCell(inputs: Map<String, String>, task: ChallengeTask, row: Int, column: Int): Float? =
    inputs[challengeCellKey(task, row, column)]?.trim()?.replace(',', '.')?.toFloatOrNull()

fun evaluateChallenge(
    task: ChallengeTask,
    components: List<PlacedInstrument>,
    solution: CircuitSolution,
    records: List<MeasurementRecord>,
    visitedWireSamples: Set<Int>,
    rheostatPositions: Set<Int>,
    rheostatMoved: Boolean = false,
    inputs: Map<String, String>,
    conclusionChoice: Int? = null,
    lampSpecChanges: Int = 0,
): ChallengeResult {
    val closed = solution.status == CircuitStatus.CLOSED && solution.totalCurrent > 0.0005f
    val lamps = components.filter { it.type == InstrumentType.LAMP || it.type == InstrumentType.TEMP_LAMP }
    val litLamps = lamps.filter { (solution.readings[it.id]?.power ?: 0f) > 0.005f }
    val ammeters = components.filter { it.type == InstrumentType.AMMETER }
    val voltmeters = components.filter { it.type == InstrumentType.VOLTMETER }
    val ampValues = ammeters.map { solution.readings[it.id]?.current ?: 0f }.filter { it > 0.0005f }
    val checks = mutableListOf<ChallengeCheck>()

    fun seriesCircuitCheck(requireAmmeter: Boolean) {
        checks += ChallengeCheck("电路闭合，开关已闭合", closed)
        checks += ChallengeCheck("至少两只小灯泡发光", litLamps.size >= 2)
        val currents = lamps.map { solution.readings[it.id]?.current ?: 0f }
        checks += ChallengeCheck(
            "两只灯泡电流近似相等（串联特征）",
            litLamps.size >= 2 && currents.maxOrNull()!! - currents.minOrNull()!! <= 0.02f,
        )
        if (requireAmmeter) {
            checks += ChallengeCheck(
                "电流表已串联接入并有示数",
                ammeters.any { (solution.readings[it.id]?.current ?: 0f) > 0.005f },
            )
        }
    }

    fun parallelCircuitCheck(requireAmmeter: Boolean) {
        checks += ChallengeCheck("电路闭合，开关已闭合", closed)
        checks += ChallengeCheck("两只小灯泡并联且都发光", litLamps.size >= 2)
        val voltages = lamps.map { solution.readings[it.id]?.voltage ?: 0f }
        checks += ChallengeCheck(
            "两灯电压近似相等（并联特征）",
            litLamps.size >= 2 && voltages.maxOrNull()!! - voltages.minOrNull()!! <= 0.12f,
        )
        if (requireAmmeter) {
            checks += ChallengeCheck(
                "干路电流表有示数",
                ammeters.any { (solution.readings[it.id]?.current ?: 0f) > 0.005f },
            )
        }
    }

    fun addTableCheck(values: List<Float?>, text: String, predicate: (List<Float>) -> Boolean) {
        if (values.any { it == null }) {
            checks += ChallengeCheck("$text（请填写完整数据）", false)
        } else {
            checks += ChallengeCheck(text, predicate(values.map { it!! }))
        }
    }

    fun addConclusionCheck(spec: ChallengeTableSpec?) {
        val conclusion = spec?.conclusion ?: return
        checks += ChallengeCheck(
            "结论选择正确",
            conclusionChoice == conclusion.correct,
        )
    }

    when (task) {
        ChallengeTask.SERIES_CIRCUIT -> seriesCircuitCheck(requireAmmeter = false)
        ChallengeTask.PARALLEL_CIRCUIT -> parallelCircuitCheck(requireAmmeter = false)

        ChallengeTask.SERIES_CURRENT -> {
            seriesCircuitCheck(requireAmmeter = true)
            checks += ChallengeCheck("至少更换过一次小灯泡规格", lampSpecChanges >= 1)
            addTableCheck(
                listOf(
                    parseCell(inputs, task, 0, 0), parseCell(inputs, task, 0, 1), parseCell(inputs, task, 0, 2),
                    parseCell(inputs, task, 1, 0), parseCell(inputs, task, 1, 1), parseCell(inputs, task, 1, 2),
                ),
                "两组数据：A、B、C 三处电流均近似相等",
            ) { values ->
                val first = values.take(3)
                val second = values.drop(3)
                first.max() - first.min() <= max(0.02f, first.max() * 0.08f) &&
                    second.max() - second.min() <= max(0.02f, second.max() * 0.08f)
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.PARALLEL_CURRENT -> {
            parallelCircuitCheck(requireAmmeter = true)
            checks += ChallengeCheck("至少更换过一次小灯泡规格", lampSpecChanges >= 1)
            addTableCheck(
                listOf(
                    parseCell(inputs, task, 0, 0), parseCell(inputs, task, 0, 1), parseCell(inputs, task, 0, 2),
                    parseCell(inputs, task, 1, 0), parseCell(inputs, task, 1, 1), parseCell(inputs, task, 1, 2),
                ),
                "两组数据：干路 A ≈ 支路 B + 支路 C",
            ) { values ->
                fun law(v: List<Float>) = abs(v[0] - v[1] - v[2]) <= max(0.03f, v[0] * 0.08f)
                law(values.take(3)) && law(values.drop(3))
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.SERIES_VOLTAGE -> {
            seriesCircuitCheck(requireAmmeter = false)
            checks += ChallengeCheck("至少更换过一次小灯泡规格", lampSpecChanges >= 1)
            addTableCheck(
                listOf(
                    parseCell(inputs, task, 0, 0), parseCell(inputs, task, 0, 1), parseCell(inputs, task, 0, 2),
                    parseCell(inputs, task, 1, 0), parseCell(inputs, task, 1, 1), parseCell(inputs, task, 1, 2),
                ),
                "两组数据：总电压 U ≈ U1 + U2",
            ) { values ->
                fun law(v: List<Float>) = abs(v[0] - v[1] - v[2]) <= max(0.10f, v[0] * 0.08f)
                law(values.take(3)) && law(values.drop(3))
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.PARALLEL_VOLTAGE -> {
            parallelCircuitCheck(requireAmmeter = false)
            checks += ChallengeCheck("至少更换过一次小灯泡规格", lampSpecChanges >= 1)
            addTableCheck(
                listOf(
                    parseCell(inputs, task, 0, 0), parseCell(inputs, task, 0, 1), parseCell(inputs, task, 0, 2),
                    parseCell(inputs, task, 1, 0), parseCell(inputs, task, 1, 1), parseCell(inputs, task, 1, 2),
                ),
                "两组数据：U1 ≈ U2 ≈ 总电压 U",
            ) { values ->
                fun law(v: List<Float>) = v.max() - v.min() <= max(0.10f, v.max() * 0.08f)
                law(values.take(3)) && law(values.drop(3))
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.RESISTANCE_FACTORS -> {
            val wire = components.firstOrNull { it.type == InstrumentType.RESISTANCE_WIRE }
            checks += ChallengeCheck("接入电阻丝实验板", wire != null)
            checks += ChallengeCheck("电路闭合且有电流", closed)
            checks += ChallengeCheck("四种电阻丝样品都切换过", visitedWireSamples.size >= 4)
            val values = (0..3).map { parseCell(inputs, task, it, 0) }
            if (values.any { it == null || it <= 0f }) {
                checks += ChallengeCheck("四种电阻丝的电流数据填写完整", false)
            } else {
                checks += ChallengeCheck("四种电阻丝的电流数据填写完整", true)
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.RHEOSTAT -> {
            val rheostat = components.firstOrNull { it.type == InstrumentType.RHEOSTAT }
            checks += ChallengeCheck("接入滑动变阻器", rheostat != null)
            checks += ChallengeCheck("电路闭合，滑动变阻器正常接入", closed)
        }

        ChallengeTask.OHM_LAW_VOLTAGE -> {
            val resistor = components.firstOrNull { it.type == InstrumentType.RESISTOR }
            val hasRheostat = components.any { it.type == InstrumentType.RHEOSTAT }
            checks += ChallengeCheck(
                "接入定值电阻、滑动变阻器、电压表和电流表",
                resistor != null && hasRheostat && voltmeters.isNotEmpty() && ammeters.isNotEmpty(),
            )
            checks += ChallengeCheck("电路闭合", closed)
            val u0 = parseCell(inputs, task, 0, 0)
            val i0 = parseCell(inputs, task, 0, 1)
            val u1 = parseCell(inputs, task, 1, 0)
            val i1 = parseCell(inputs, task, 1, 1)
            if (listOf(u0, i0, u1, i1).any { it == null || it <= 0f }) {
                checks += ChallengeCheck("至少两组 U、I 数据（请填写完整）", false)
                checks += ChallengeCheck("两组 U/I 近似相等，且电压确实改变", false)
            } else {
                checks += ChallengeCheck("至少两组 U、I 数据", true)
                val r0 = u0!! / i0!!
                val r1 = u1!! / i1!!
                checks += ChallengeCheck(
                    "两组 U/I 近似相等，且电压确实改变",
                    abs(r0 - r1) <= max(0.5f, max(r0, r1) * 0.15f) && abs(u0 - u1) > 0.1f,
                )
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.OHM_LAW_RESISTANCE -> {
            val resistor = components.firstOrNull { it.type == InstrumentType.RESISTOR }
            val hasRheostat = components.any { it.type == InstrumentType.RHEOSTAT }
            checks += ChallengeCheck(
                "接入可更换的定值电阻、滑动变阻器、电压表和电流表",
                resistor != null && hasRheostat && voltmeters.isNotEmpty() && ammeters.isNotEmpty(),
            )
            checks += ChallengeCheck("电路闭合", closed)
            val r0 = parseCell(inputs, task, 0, 0)
            val i0 = parseCell(inputs, task, 0, 1)
            val r1 = parseCell(inputs, task, 1, 0)
            val i1 = parseCell(inputs, task, 1, 1)
            if (listOf(r0, i0, r1, i1).any { it == null || it <= 0f }) {
                checks += ChallengeCheck("至少两组 R、I 数据（请填写完整）", false)
                checks += ChallengeCheck("电阻改变时 R×I 近似不变", false)
            } else {
                checks += ChallengeCheck("至少两组 R、I 数据", true)
                checks += ChallengeCheck(
                    "电阻改变时 R×I 近似不变（电压保持不变）",
                    abs(r0!! - r1!!) > 0.5f &&
                        abs(r0 * i0!! - r1 * i1!!) <= max(0.5f, max(r0 * i0, r1 * i1) * 0.15f),
                )
            }
            addConclusionCheck(task.table)
        }

        ChallengeTask.VOLT_AMMETER_RESISTOR -> {
            val resistor = components.firstOrNull { it.type == InstrumentType.RESISTOR }
            val hasRheostat = components.any { it.type == InstrumentType.RHEOSTAT }
            checks += ChallengeCheck(
                "已接入未知定值电阻、滑动变阻器、电压表和电流表",
                resistor != null && hasRheostat && voltmeters.isNotEmpty() && ammeters.isNotEmpty(),
            )
            checks += ChallengeCheck("电路闭合", closed)
            addTableCheck(
                listOf(parseCell(inputs, task, 0, 0), parseCell(inputs, task, 0, 1), parseCell(inputs, task, 0, 2)),
                "数据：R ≈ U / I",
            ) { values ->
                val u = values[0]
                val i = values[1]
                val r = values[2]
                i > 0.0001f && abs(r - u / i) <= max(0.5f, abs(u / i) * 0.10f)
            }
            val expected = resistor?.resistance ?: 0f
            val calculated = parseCell(inputs, task, 0, 2)
            checks += ChallengeCheck(
                "算出的电阻与系统给定阻值相符",
                expected > 0f && calculated != null &&
                    abs(calculated - expected) <= max(0.5f, expected * 0.15f),
            )
        }

        ChallengeTask.VOLT_AMMETER_LAMP -> {
            val lamp = components.firstOrNull { it.type == InstrumentType.TEMP_LAMP }
            val hasRheostat = components.any { it.type == InstrumentType.RHEOSTAT }
            checks += ChallengeCheck(
                "已接入阻值随温度变化的小灯泡、滑动变阻器、电压表和电流表",
                lamp != null && hasRheostat && voltmeters.isNotEmpty() && ammeters.isNotEmpty(),
            )
            checks += ChallengeCheck("电路闭合", closed)
            val lowU = parseCell(inputs, task, 0, 0)
            val lowI = parseCell(inputs, task, 0, 1)
            val lowR = parseCell(inputs, task, 0, 2)
            val ratedU = parseCell(inputs, task, 1, 0)
            val ratedI = parseCell(inputs, task, 1, 1)
            val ratedR = parseCell(inputs, task, 1, 2)
            val highU = parseCell(inputs, task, 2, 0)
            val highI = parseCell(inputs, task, 2, 1)
            val highR = parseCell(inputs, task, 2, 2)
            val expectedRated = lamp?.lampSpec?.voltage ?: 0f
            val all = listOf(lowU, lowI, lowR, ratedU, ratedI, ratedR, highU, highI, highR)
            if (all.any { it == null }) {
                checks += ChallengeCheck("三行数据完整（较低、额定、较高电压）", false)
                checks += ChallengeCheck("额定电压和 1.2 倍额定电压记录正确", false)
                checks += ChallengeCheck("较高电压下电阻明显增大（温度影响）", false)
            } else {
                val r1 = lowR!!
                val r2 = highR!!
                checks += ChallengeCheck(
                    "三行均满足 R ≈ U / I",
                    abs(r1 - lowU!! / lowI!!) <= max(0.4f, abs(lowU / lowI) * 0.12f) &&
                        abs(ratedR!! - ratedU!! / ratedI!!) <= max(0.4f, abs(ratedU / ratedI) * 0.12f) &&
                        abs(r2 - highU!! / highI!!) <= max(0.4f, abs(highU / highI) * 0.12f),
                )
                checks += ChallengeCheck(
                    "额定电压和 1.2 倍额定电压记录正确（额定 ${formatNumber(expectedRated)} V）",
                    expectedRated > 0f &&
                        abs(ratedU!! - expectedRated) <= max(0.2f, expectedRated * 0.10f) &&
                        abs(highU!! - expectedRated * 1.2f) <= max(0.2f, expectedRated * 0.12f),
                )
                checks += ChallengeCheck(
                    "较高电压下电阻明显增大（温度影响）",
                    r2 > r1 * 1.08f,
                )
            }
        }

        ChallengeTask.LAMP_POWER -> {
            val lamp = components.firstOrNull { it.type == InstrumentType.TEMP_LAMP }
            checks += ChallengeCheck(
                "已接入阻值随温度变化的小灯泡和电压表、电流表",
                lamp != null && voltmeters.isNotEmpty() && ammeters.isNotEmpty(),
            )
            checks += ChallengeCheck("电路闭合", closed)
            val values = (0..2).map { row ->
                Triple(
                    parseCell(inputs, task, row, 0),
                    parseCell(inputs, task, row, 1),
                    parseCell(inputs, task, row, 2),
                )
            }
            val statuses = (0..2).map { row ->
                inputs[challengeCellKey(task, row, 3)]?.trim().orEmpty()
            }
            if (values.any { it.first == null || it.second == null || it.third == null }) {
                checks += ChallengeCheck("三行 U、I、P 数据完整", false)
            } else {
                checks += ChallengeCheck(
                    "三行均满足 P ≈ U × I",
                    values.all {
                        val u = it.first!!
                        val i = it.second!!
                        val p = it.third!!
                        abs(p - u * i) <= max(0.05f, abs(u * i) * 0.10f)
                    },
                )
            }
            checks += ChallengeCheck(
                "亮度选择正确：亮度低、正常发光、亮度高",
                statuses[0].contains("亮度低") &&
                    statuses[1].contains("正常发光") &&
                    statuses[2].contains("亮度高"),
            )
        }
    }

    val requiresLampChange = task in setOf(
        ChallengeTask.SERIES_CURRENT,
        ChallengeTask.PARALLEL_CURRENT,
        ChallengeTask.SERIES_VOLTAGE,
        ChallengeTask.PARALLEL_VOLTAGE,
    )
    val score = if (checks.isEmpty()) 0 else checks.count { it.done } * 100 / checks.size
    val finalScore = if (requiresLampChange && lampSpecChanges < 1) minOf(score, 79) else score
    return ChallengeResult(checks, finalScore)
}
