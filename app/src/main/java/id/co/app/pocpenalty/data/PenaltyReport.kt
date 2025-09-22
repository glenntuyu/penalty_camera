package id.co.app.pocpenalty.data

enum class SectionId { KANAN, KIRI, BELAKANG }
data class PenaltyItem(val label: String, val value: Int?)
data class PenaltySection(
    val title: String,
    val items: List<PenaltyItem>,
    val totalLabel: String,
    val totalValue: Int
)
data class PenaltyReport(
    val title: String = "Penalty Report",
    val totalPenalty: Double = 0.0,
    val sections: List<PenaltySection> = listOf()
)