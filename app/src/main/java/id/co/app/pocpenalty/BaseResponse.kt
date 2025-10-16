package id.co.app.pocpenalty


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse<T>(
    @Json(name = "statusCode")
    val statusCode: Int? = null,
    @Json(name = "statusDesc")
    val statusDesc: String? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "data")
    val data: T? = null,
    @Json(name = "tokenizer")
    val tokenizer: String? = null,
    @Json(name = "requestId")
    val requestId: String? = null,
)