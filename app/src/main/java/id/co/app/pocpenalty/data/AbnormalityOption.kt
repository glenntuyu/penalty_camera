package id.co.app.pocpenalty.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

data class AbnormalityOption(
    val id: String,
    val title: String,
    val circleColor: Color,
    val tagType: TagType,
    val icon: Painter
)