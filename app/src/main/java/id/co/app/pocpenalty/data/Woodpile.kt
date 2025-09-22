package id.co.app.pocpenalty.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by Tuyu on 7/22/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
data class WoodPileData(
    @SerializedName("image_dimensions")
    val imageDimensions: ImageDimensions,
    @SerializedName("px_per_meter")
    val pxPerMeter: Int,
    @SerializedName("segmentation_line")
    val segmentationLine: Line,
    @SerializedName("detection_box")
    val detectionBox: DetectionBox,
    @SerializedName("grid_lines")
    val gridLines: GridLines
)

data class ImageDimensions(
    val width: Int,
    val height: Int
)

data class Line(
    val p1: List<Int>,
    val p2: List<Int>
) {
    fun toOffset1() = androidx.compose.ui.geometry.Offset(p1[0].toFloat(), p1[1].toFloat())
    fun toOffset2() = androidx.compose.ui.geometry.Offset(p2[0].toFloat(), p2[1].toFloat())
}

data class DetectionBox(
    val label: String,
    @SerializedName("top_left")
    val topLeft: List<Int>,
    @SerializedName("bottom_right")
    val bottomRight: List<Int>
)

data class GridLines(
    val vertical: List<Line>,
    val horizontal: List<Line>
)

// JSON Converter utility
object WoodPileDataConverter {
    fun fromJson(jsonString: String): WoodPileData {
        return Gson().fromJson(jsonString, WoodPileData::class.java)
    }

    fun fromJsonFile(context: android.content.Context, fileName: String): WoodPileData {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return fromJson(jsonString)
    }
}