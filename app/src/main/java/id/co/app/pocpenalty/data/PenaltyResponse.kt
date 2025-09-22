package id.co.app.pocpenalty.data

import com.google.gson.Gson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

@Serializable
data class PenaltyApiResponse(
    val statusCode: Int,
    val statusDesc: String,
    val message: String,
    val data: List<PenaltyRuleDto>
)

@Serializable
data class PenaltyRuleDto(
    @SerialName("WoodPltID")  val id: String,
    @SerialName("WoodPltUoM") val uom: String,
    @SerialName("WoodPltVal") val value: Double,
    @SerialName("WoodPltName") val name: String,
    @SerialName("WoodPltGrp") val group: String
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
    PenaltyRule(id = id, name = name, group = group.toGroup(), uom = uom.toUom(), unitValue = value)

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
        group = when (group) { Group.A -> "A"; Group.B -> "B"; Group.UNKNOWN -> "N/A" }
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

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}
fun decodePenaltyRules(jsonText: String): List<PenaltyRule> =
    json.decodeFromString(PenaltyApiResponse.serializer(),jsonText).data.map { it.toDomain() }
