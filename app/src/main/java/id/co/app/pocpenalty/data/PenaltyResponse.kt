package id.co.app.pocpenalty.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class PenaltyApiResponse(
    @Json(name = "statusCode") val statusCode: Int,
    @Json(name = "statusDesc") val statusDesc: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: List<PenaltyRuleDto>
)

@JsonClass(generateAdapter = true)
data class PenaltyRuleDto(
    @Json(name = "WoodPltID") val id: String,
    @Json(name = "WoodPltUoM") val uom: String,
    @Json(name = "WoodPltVal") val value: Double,
    @Json(name = "WoodPltName") val name: String,
    @Json(name = "WoodPltGrp") val group: String
)

enum class Uom { TRUK, PERSEN, LOG, PCS, KG, UNKNOWN }
enum class Group { A, B, UNKNOWN }

data class PenaltyRule(
    val id: String,
    val name: String,
    val group: Group,
    val uom: Uom,
    val unitValue: Double   // value per unit or % (when PERSEN)
)

private fun String.toUom(): Uom = when (trim().lowercase()) {
    "truk" -> Uom.TRUK
    "persen" -> Uom.PERSEN
    "log" -> Uom.LOG
    "pcs" -> Uom.PCS
    "kg" -> Uom.KG
    else -> Uom.UNKNOWN
}

private fun String.toGroup(): Group = when (trim().uppercase()) {
    "A" -> Group.A
    "B" -> Group.B
    else -> Group.UNKNOWN
}

fun PenaltyRuleDto.toDomain(): PenaltyRule =
    PenaltyRule(
        id = id,
        name = name,
        group = group.toGroup(),
        uom = uom.toUom(),
        unitValue = value
    )

fun PenaltyRule.toDto(): PenaltyRuleDto =
    PenaltyRuleDto(
        id = id,
        uom = when (uom) {
            Uom.TRUK -> "Truk"
            Uom.PERSEN -> "Persen"
            Uom.LOG -> "Log"
            Uom.PCS -> "Pcs"
            Uom.KG -> "Kg"
            Uom.UNKNOWN -> "N/A"
        },
        value = unitValue,
        name = name,
        group = when (group) {
            Group.A -> "A"
            Group.B -> "B"
            Group.UNKNOWN -> "N/A"
        }
    )

data class PenaltyEntry(
    val ruleId: String,
    val quantity: Double = 1.0  // e.g. 3 logs, 2 pcs, 100 kg, 1 truck, or times the % is applied
)

data class PenaltyLineResult(
    val rule: PenaltyRule,
    val quantity: Double,
    val subtotal: Double
)

data class PenaltySummary(
    val lines: List<PenaltyLineResult>
) {
    val total: Double get() = lines.sumOf { it.subtotal }
    val totalByGroup: Map<Group, Double> get() =
        lines.groupBy { it.rule.group }.mapValues { (_, rows) -> rows.sumOf { it.subtotal } }
}

/**
 * Decode JSON text into a list of [PenaltyRule] using Moshi.
 */
fun decodePenaltyRules(jsonText: String): List<PenaltyRule> {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(PenaltyApiResponse::class.java)
    val parsed = adapter.fromJson(jsonText) ?: return emptyList()
    return parsed.data.map { it.toDomain() }
}
