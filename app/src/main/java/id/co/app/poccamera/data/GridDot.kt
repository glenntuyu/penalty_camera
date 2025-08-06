package id.co.app.poccamera.data

import androidx.compose.ui.geometry.Offset

/**
 * Created by Tuyu on 7/8/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
data class GridDot(
    val grid: GridRect,
    val percentOffset: Offset,
    val tagType: TagType
)