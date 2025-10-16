package id.co.app.pocpenalty

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import id.co.app.pocpenalty.data.PenaltyEntry
import id.co.app.pocpenalty.data.PenaltyLineResult
import id.co.app.pocpenalty.data.PenaltyRule
import id.co.app.pocpenalty.data.PenaltySummary
import id.co.app.pocpenalty.data.Uom
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.core.net.toUri

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

enum class ImgFormat { JPEG, WEBP }

fun saveImageBitmapToFile(
    context: Context,
    image: Bitmap,
    fileName: String = "penalty_image_${System.currentTimeMillis()}",
    format: ImgFormat = ImgFormat.JPEG,
    quality: Int = 92
): File {
    val ext = when (format) { ImgFormat.JPEG -> "jpg"; ImgFormat.WEBP -> "webp" }
    val outFile = File(context.cacheDir, "$fileName.$ext")
    FileOutputStream(outFile).use { out ->
        val bmp = image
        val compressFormat = when (format) {
            ImgFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImgFormat.WEBP -> Bitmap.CompressFormat.WEBP
        }
        bmp.compress(compressFormat, quality.coerceIn(0, 100), out)
        out.flush()
    }
    return outFile
}

fun refreshGallery(context: Context) {
    val root = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        "Camera"
    )

    if (!root.exists() || !root.isDirectory) return

    val imagePaths = root.walkTopDown()
        .filter { it.isFile && (it.extension.equals("jpg", true) || it.extension.equals("png", true) || it.extension.equals("webp", true)) }
        .map { it.absolutePath }
        .toList()

    if (imagePaths.isNotEmpty()) {
        MediaScannerConnection.scanFile(
            context,
            imagePaths.toTypedArray(),
            null
        ) { path, uri ->
            Log.d("MediaScanner", "Scanned file: $path -> $uri")
        }
    }
}

fun scanFolderWithContentResolver(context: Context, folder: File) {
    if (!folder.exists() || !folder.isDirectory) return

    val files = folder.listFiles() ?: return
    val resolver = context.contentResolver

    for (file in files) {
        if (file.isFile) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DATA, file.absolutePath)  // deprecated in API 29+, but still works for direct rescan
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
                put(MediaStore.MediaColumns.SIZE, file.length())
            }

            // Try insert; if already exists, it will fail silently
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}

// Helper: determine MIME type from extension
private fun getMimeType(file: File): String {
    return when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}