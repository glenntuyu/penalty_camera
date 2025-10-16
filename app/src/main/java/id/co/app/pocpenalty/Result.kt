package id.co.app.pocpenalty

import androidx.compose.ui.graphics.ImageBitmap

sealed class Result<out T> {
    object Empty : Result<Nothing>()
    data class Success<out T>(
        val value: T,
        val imageBitmap: ImageBitmap,
        val scalingInfo: ScalingInfo,
        val jsonFile: String?
    ) : Result<T>()

    data class Failure(
        val message: String
    ) : Result<Nothing>()

    data class FailureNetwork(
        val message: String
    ) : Result<Nothing>()

    data class Failures<out T>(
        val message: T
    ) : Result<T>()

    object Loading : Result<Nothing>()

    data class Done (val isFailure: Boolean = false): Result<Nothing>()
}