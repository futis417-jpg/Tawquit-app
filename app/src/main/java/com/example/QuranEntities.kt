package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: Int, // Just a unique id (number in total) or artificial. We can use autogenerate 
    val surahNumber: Int,
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int
)
