package id.co.app.pocpenalty

import id.co.app.pocpenalty.data.WoodPileData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface PenaltyClientService {

    @Multipart
    @POST
    suspend fun processImage(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): WoodPileData
}