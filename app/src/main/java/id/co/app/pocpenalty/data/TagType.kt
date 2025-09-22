package id.co.app.pocpenalty.data

import androidx.compose.ui.graphics.Color
import id.co.app.pocpenalty.R

/**
 * Created by Tuyu on 7/8/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
enum class TagType(val id: String, val color: Color, val icon: Int) {
    RANTING("P03", Color(0xFFEA7711), R.drawable.branch),
    DAUN("P43", Color(0xFF8DB051), R.drawable.leaf),
    TANAH("P44", Color(0xFF4395D4), R.drawable.soil),
    ABAIKAN("N/A", Color(0xFFAF1F25), R.drawable.ignore);

    companion object {
        private val byRuleId = entries.associateBy { it.id }
        fun fromRuleId(id: String?): TagType? {
            return byRuleId[id ?: return null]
        }
    }
}