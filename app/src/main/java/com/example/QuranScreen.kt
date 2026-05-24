package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun QuranScreen(isSpanish: Boolean) {
    val context = LocalContext.current
    val db = remember { QuranDatabase.getDatabase(context) }
    val surahDao = db.surahDao()
    
    var surahs by remember { mutableStateOf<List<SurahEntity>?>(null) }
    var selectedSurah by remember { mutableStateOf<SurahEntity?>(null) }
    var ayahs by remember { mutableStateOf<List<AyahEntity>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val currentScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (surahs == null) {
            val localSurahs = surahDao.getAllSurahsDirect()
            if (localSurahs.isNotEmpty()) {
                surahs = localSurahs
            } else {
                isLoading = true
                try {
                    val response = RetrofitClient.api.getSurahs()
                    val entities = response.data.map {
                        SurahEntity(
                            number = it.number,
                            name = it.name,
                            englishName = it.englishName,
                            englishNameTranslation = it.englishNameTranslation,
                            numberOfAyahs = it.numberOfAyahs,
                            revelationType = it.revelationType
                        )
                    }
                    surahDao.insertSurahs(entities)
                    surahs = entities
                } catch (e: Exception) {
                    error = "Se requiere conexión a Internet para la primera descarga."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = 80.dp)
    ) {
        if (selectedSurah == null) {
            Text(
                text = if (isSpanish) "EL CORÁN" else "القرآن الكريم",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
                Button(onClick = { 
                    error = null
                    currentScope.launch {
                        isLoading = true
                        try {
                            val response = RetrofitClient.api.getSurahs()
                            val entities = response.data.map {
                                SurahEntity(
                                    number = it.number,
                                    name = it.name,
                                    englishName = it.englishName,
                                    englishNameTranslation = it.englishNameTranslation,
                                    numberOfAyahs = it.numberOfAyahs,
                                    revelationType = it.revelationType
                                )
                            }
                            surahDao.insertSurahs(entities)
                            surahs = entities
                        } catch (e: Exception) {
                            error = "Se requiere conexión a Internet."
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Reintentar")
                }
            } else if (surahs != null) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(surahs!!) { surah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    selectedSurah = surah
                                    currentScope.launch {
                                        ayahs = null
                                        val localAyahs = surahDao.getAyahsForSurahDirect(surah.number)
                                        if (localAyahs.isNotEmpty()) {
                                            ayahs = localAyahs
                                        } else {
                                            try {
                                                val response = RetrofitClient.api.getSurah(surah.number)
                                                val entities = response.data.ayahs.map {
                                                    AyahEntity(
                                                        id = it.number,
                                                        surahNumber = surah.number,
                                                        number = it.number,
                                                        text = it.text,
                                                        numberInSurah = it.numberInSurah,
                                                        juz = it.juz,
                                                        page = it.page
                                                    )
                                                }
                                                surahDao.insertAyahs(entities)
                                                ayahs = entities
                                            } catch (e: Exception) {
                                                selectedSurah = null
                                                error = "Se requiere conexión a Internet para ver esta Surah la primera vez."
                                            }
                                        }
                                    }
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${surah.number}. ${surah.englishName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSpanish) surah.englishNameTranslation else surah.revelationType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Text(
                                text = surah.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // Surah Detail
            Button(
                onClick = { 
                    selectedSurah = null 
                    ayahs = null 
                },
                modifier = Modifier.padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(if (isSpanish) "Atrás" else "الخلف")
            }
            
            Text(
                text = "${selectedSurah!!.englishName} - ${selectedSurah!!.name}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (ayahs == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(ayahs!!) { ayah ->
                        Text(
                            text = "${ayah.text} ﴿${ayah.numberInSurah}﴾",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 40.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}
