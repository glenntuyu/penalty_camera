package id.co.app.pocpenalty

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  @Singleton
  fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
    val trustAllCerts = arrayOf<TrustManager>(
      object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
      }
    )

    // Install the all-trusting trust manager
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    val sslSocketFactory = sslContext.socketFactory

    val interceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    val okhttp = OkHttpClient.Builder()
      .addInterceptor(interceptor)

      .addInterceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
          .header(ConstantVariable.API_KEY, ConstantVariable.DEFAULT_KEY)
          .build()
        chain.proceed(newRequest)
      }

      // ⚠️ Forcibly closes idle connections quickly (used for dev?)
      .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))

      // ✅ HTTP/1.1 protocol
      .protocols(listOf(Protocol.HTTP_1_1))

      .retryOnConnectionFailure(true)
      .connectTimeout(5, TimeUnit.MINUTES)
      .readTimeout(5, TimeUnit.MINUTES)
      .writeTimeout(5, TimeUnit.MINUTES)

      // ⚠️ Accept any SSL cert — only use in trusted/internal dev environments
      .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
      .hostnameVerifier { _, _ -> true }

    return okhttp.build()
  }

  @Provides @Singleton
  fun provideRetrofit(okHttp: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl("http://172.17.226.44:5554/") // any valid base, you pass full @Url
      .client(okHttp)
      .addConverterFactory(GsonConverterFactory.create())
      .build()

  @Provides
  @Singleton
  fun provideService(retrofit: Retrofit): PenaltyClientService =
    retrofit.create(PenaltyClientService::class.java)

  @Provides
  @Singleton
  fun provideClient(@ApplicationContext context: Context, clientService: PenaltyClientService): PenaltyClient = PenaltyClientImpl(context, clientService)

  @Provides
  @Singleton
  fun provideBaseDispatchers(): Dispatchers = BaseDispatchers()
}