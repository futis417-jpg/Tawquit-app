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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun QuranScreen(isSpanish: Boolean) {
    var surahs by remember { mutableStateOf<List<Surah>?>(null) }
    var selectedSurah by remember { mutableStateOf<SurahDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val currentScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (surahs == null) {
            try {
                val response = RetrofitClient.api.getSurahs()
                surahs = response.data
            } catch (e: Exception) {
                error = e.message
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

            if (error != null) {
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            } else if (surahs == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(surahs!!) { surah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    currentScope.launch {
                                        try {
                                            selectedSurah = RetrofitClient.api.getSurah(surah.number).data
                                        } catch (e: Exception) {
                                            error = e.message
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
                onClick = { selectedSurah = null },
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
            
            LazyColumn {
                items(selectedSurah!!.ayahs) { ayah ->
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
