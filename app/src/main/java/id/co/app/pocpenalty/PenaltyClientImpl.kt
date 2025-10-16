package id.co.app.pocpenalty

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.app.pocpenalty.data.WoodPileData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body

interface PenaltyClient {
    suspend fun processImage(url: String, file: MultipartBody.Part): WoodPileData
}

class PenaltyClientImpl(
    @ApplicationContext private val context: Context,
    private val service: PenaltyClientService
) : PenaltyClient {
    override suspend fun processImage(url: String, file: MultipartBody.Part): WoodPileData =
        service.processImage(url, file)
}