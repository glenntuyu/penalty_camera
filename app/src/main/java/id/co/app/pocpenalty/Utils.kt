package id.co.app.pocpenalty

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import id.co.app.pocpenalty.data.PenaltyEntry
import id.co.app.pocpenalty.data.PenaltyLineResult
import id.co.app.pocpenalty.data.PenaltyRule
import id.co.app.pocpenalty.data.PenaltySummary
import id.co.app.pocpenalty.data.Uom
import java.util.Locale

/**
 * Created by Tuyu on 6/19/2025.
 * Sinarmas APP
 * christian_tuyu@app.co.id
 */
fun decodeSampledBitmapFromResource(
    res: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(res, resId, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

fun decodeSampledBitmapFromResource(
    res: Resources,
    resId: Int,
    maxSize: Int // e.g., 1080 or 2048
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(res, resId, options)

    val (originalWidth, originalHeight) = options.outWidth to options.outHeight

    // Calculate the scale factor to fit within maxSize
    val scale = if (originalWidth > originalHeight) {
        originalWidth.toFloat() / maxSize
    } else {
        originalHeight.toFloat() / maxSize
    }
    val reqWidth = (originalWidth / scale).toInt()
    val reqHeight = (originalHeight / scale).toInt()

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight &&
            (halfWidth / inSampleSize) >= reqWidth
        ) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun sentence(s: String, locale: Locale = Locale.getDefault()): String {
    val trimmed = s.trim()
    if (trimmed.isEmpty()) return trimmed
    val lower = trimmed.lowercase(locale)
    return lower.replaceFirstChar { c -> c.titlecase(locale) }
}

fun computePenalties(
    rules: Map<String, PenaltyRule>,
    entries: List<PenaltyEntry>,
    baseForPercent: Double = 0.0
): PenaltySummary {
    val lines = entries.mapNotNull { e ->
        val rule = rules[e.ruleId] ?: return@mapNotNull null
        val subtotal = when (rule.uom) {
            Uom.PERSEN -> baseForPercent * (rule.unitValue / 100.0) * e.quantity
            Uom.UNKNOWN -> 0.0
            else -> rule.unitValue * e.quantity
        }
        PenaltyLineResult(rule, e.quantity, subtotal)
    }
    return PenaltySummary(lines)
}