package id.co.app.pocpenalty

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.HttpException
import java.net.SocketTimeoutException

enum class ErrorCodes(val code: Int) {
	SocketTimeOut(-1)
}


fun Throwable.handleExceptionString(): String {
	return when (this) {
		is HttpException -> {
			try {
				val type = Types.newParameterizedType(BaseResponse::class.java, Any::class.java)
				val adapter: JsonAdapter<BaseResponse<Any>> = Moshi.Builder().build().adapter(type)
				val message =
					adapter.fromJson(response()?.errorBody()?.string() ?: "")?.message ?: ""
				getErrorMessage(this.code(), message)
			} catch (ex: Exception) {
				ex.message ?: ""
			}
		}
		is SocketTimeoutException -> getErrorMessage(ErrorCodes.SocketTimeOut.code)
		else -> getErrorMessage(Int.MAX_VALUE, message.orEmpty().ifEmpty { "Gagal terhubung dengan server" })
	}
}

fun Throwable.isUserUnauthorized(): Boolean {
	return this is HttpException && this.code() == 401
}

private fun getErrorMessage(code: Int, serverMessage: String = ""): String {
	return when (code) {
		ErrorCodes.SocketTimeOut.code -> "$code - Koneksi ke server gagal"
		401 -> "$code - $serverMessage"
		404 -> "$code - Server tidak ditemukan"
		else -> "$code - ".plus(serverMessage)
	}
}