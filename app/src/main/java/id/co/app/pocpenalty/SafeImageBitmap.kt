
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import id.co.app.pocpenalty.ScalingInfo

fun Context.createSafeImageBitmap(drawableRes: Int): ImageBitmap {
    // Get screen width in pixels
    val displayMetrics = resources.displayMetrics
    val screenWidthPixels = displayMetrics.widthPixels

    // Load the original drawable as bitmap
    val drawable = ContextCompat.getDrawable(this, drawableRes)
        ?: throw IllegalArgumentException("Drawable resource not found")

    val originalWidth = drawable.intrinsicWidth
    val originalHeight = drawable.intrinsicHeight

    // Calculate the scaled height maintaining aspect ratio
    val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
    val scaledHeight = (screenWidthPixels * aspectRatio).toInt()

    // Create a bitmap with the calculated dimensions
    val scaledBitmap = Bitmap.createBitmap(
        screenWidthPixels,
        scaledHeight,
        Bitmap.Config.ARGB_8888
    )

    // Draw the drawable onto the scaled bitmap
    val canvas = Canvas(scaledBitmap)
    drawable.setBounds(0, 0, screenWidthPixels, scaledHeight)
    drawable.draw(canvas)

    return scaledBitmap.asImageBitmap()
}

// Alternative version using BitmapFactory for better memory efficiency
fun Context.createSafeImageBitmapOptimized(drawableRes: Int): ImageBitmap {
    val displayMetrics = resources.displayMetrics
    val screenWidthPixels = displayMetrics.widthPixels

    // First, get the original dimensions without loading the full bitmap
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, drawableRes, options)

    val originalWidth = options.outWidth
    val originalHeight = options.outHeight

    // Calculate sample size to reduce memory usage
    val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
    val scaledHeight = (screenWidthPixels * aspectRatio).toInt()

    // Calculate inSampleSize for efficient loading
    options.inSampleSize = calculateInSampleSize(options, screenWidthPixels, scaledHeight)
    options.inJustDecodeBounds = false

    // Load the sampled bitmap
    val sampledBitmap = BitmapFactory.decodeResource(resources, drawableRes, options)
        ?: throw RuntimeException("Failed to decode bitmap")

    // Scale to exact screen width if needed
    val finalBitmap = if (sampledBitmap.width != screenWidthPixels) {
        Bitmap.createScaledBitmap(sampledBitmap, screenWidthPixels, scaledHeight, true).also {
            sampledBitmap.recycle() // Free memory of the intermediate bitmap
        }
    } else {
        sampledBitmap
    }

    return finalBitmap.asImageBitmap()
}

fun Context.createSafeImageBitmapWithScaling(drawableRes: Int): Pair<ImageBitmap, ScalingInfo> {
    val displayMetrics = resources.displayMetrics
    val screenWidthPixels = displayMetrics.widthPixels

    // First, get the original dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, drawableRes, options)

    val originalWidth = options.outWidth
    val originalHeight = options.outHeight

    // Calculate scaled dimensions
    val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
    val scaledHeight = (screenWidthPixels * aspectRatio).toInt()

    // Calculate scaling factors
    val scaleX = screenWidthPixels.toFloat() / originalWidth.toFloat()
    val scaleY = scaledHeight.toFloat() / originalHeight.toFloat()

    // Create the scaled bitmap (same logic as before)
    options.inSampleSize = calculateInSampleSize(options, screenWidthPixels, scaledHeight)
    options.inJustDecodeBounds = false

    val sampledBitmap = BitmapFactory.decodeResource(resources, drawableRes, options)
        ?: throw RuntimeException("Failed to decode bitmap")

    val finalBitmap = if (sampledBitmap.width != screenWidthPixels) {
        Bitmap.createScaledBitmap(sampledBitmap, screenWidthPixels, scaledHeight, true).also {
            sampledBitmap.recycle()
        }
    } else {
        sampledBitmap
    }

    val scalingInfo = ScalingInfo(scaleX, scaleY, originalWidth, originalHeight)
    return Pair(finalBitmap.asImageBitmap(), scalingInfo)
}

fun Context.createSafeImageBitmapWithScaling(bitmap: Bitmap): Pair<ImageBitmap, ScalingInfo> {
    val displayMetrics = resources.displayMetrics
    val screenWidthPixels = displayMetrics.widthPixels

    // Convert to Android Bitmap to work with dimensions and resizing
    val originalBitmap: Bitmap = bitmap

    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height

    // Calculate scaled dimensions
    val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
    val scaledHeight = (screenWidthPixels * aspectRatio).toInt()

    // Calculate scaling factors
    val scaleX = screenWidthPixels.toFloat() / originalWidth.toFloat()
    val scaleY = scaledHeight.toFloat() / originalHeight.toFloat()

    // Create the scaled bitmap
    val finalBitmap = if (originalWidth != screenWidthPixels) {
        Bitmap.createScaledBitmap(originalBitmap, screenWidthPixels, scaledHeight, true)
    } else {
        originalBitmap
    }

    val scalingInfo = ScalingInfo(scaleX, scaleY, originalWidth, originalHeight)
    return Pair(finalBitmap.asImageBitmap(), scalingInfo)
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

// Usage example:
// val imageBitmap = context.createSafeImageBitmap(R.drawable.my_large_image)
// or
// val imageBitmap = context.createSafeImageBitmapOptimized(R.drawable.my_large_image)
