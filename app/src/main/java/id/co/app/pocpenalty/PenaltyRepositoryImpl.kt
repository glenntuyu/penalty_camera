package id.co.app.pocpenalty

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.app.pocpenalty.data.WoodPileData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

interface PenaltyRepository {
    fun processImage(imgFile: File): Flow<WoodPileData>
}

class PenaltyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: PenaltyClient
) : PenaltyRepository {

    override fun processImage(imgFile: File): Flow<WoodPileData> = flow {
        val mediaType = "image/webp".toMediaType() // match your curl
        val body = imgFile.asRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData(
            name = "file",               // exactly as your curl: -F 'file=@...'
            filename = imgFile.name,
            body = body
        )

        val url = "https://serviceshub.app.co.id:8443/penalty-detection/process-image"
        emit(client.processImage(url, part))
    }
}