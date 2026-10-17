package com.example.frontend_indo_kao.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

data class TranslationResponse(
    val status: String,
    val teks_sumber_dikenali: String,
    val teks_terjemahan_akhir: String,
    val url_audio_hasil: String? = null
)

interface TranslationService {
    @FormUrlEncoded
    @POST("/api/translate")
    suspend fun translateText(
        @Field("teks_sumber") teksSumber: String,
        @Field("arah_terjemahan") arahTerjemahan: String
    ): TranslationResponse

    @Multipart
    @POST("/api/v1/translate/audio")
    suspend fun translateAudio(
        @Part audio_file: MultipartBody.Part,
        @Part("arah_terjemahan") arahTerjemahan: RequestBody
    ): TranslationResponse
}

object RetrofitClient {
    const val BASE_URL = "http://10.93.177.175:8000"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: TranslationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(TranslationService::class.java)
    }
}
