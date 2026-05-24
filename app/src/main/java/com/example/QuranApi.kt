package com.example

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class QuranResponse(
    val code: Int,
    val status: String,
    val data: List<Surah>
)

@JsonClass(generateAdapter = true)
data class SurahResponse(
    val code: Int,
    val status: String,
    val data: SurahDetail
)

@JsonClass(generateAdapter = true)
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val ayahs: List<Ayah>
)

@JsonClass(generateAdapter = true)
data class Ayah(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int
)

interface QuranApi {
    @GET("surah")
    suspend fun getSurahs(): QuranResponse

    @GET("surah/{id}")
    suspend fun getSurah(@Path("id") id: Int): SurahResponse
}

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.alquran.cloud/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: QuranApi = retrofit.create(QuranApi::class.java)
}
