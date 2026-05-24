package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration

@Composable
fun HorarioScreen(isSpanish: Boolean, onLanguageChange: () -> Unit, prefs: android.content.SharedPreferences) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(isSpanish, onLanguageChange)
        }
        item {
            NextPrayerCard(isSpanish)
        }
        item {
            PrayerTimesList(isSpanish)
        }
        item {
            QuranReminderCard(isSpanish, prefs)
        }
    }
}

@Composable
fun HeaderSection(isSpanish: Boolean, onLanguageChange: () -> Unit) {
    val today = LocalDate.now()
    val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val dateText = if (isSpanish) "${today.dayOfMonth} de $monthName | ١٤ رجب" else "١٤ رجب | ${today.dayOfMonth} $monthName"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = dateText.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tawquit, ES",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(percent = 50))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSpanish) Color.White else Color.Transparent)
                    .clickable { if (!isSpanish) onLanguageChange() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ES",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSpanish) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (!isSpanish) Color.White else Color.Transparent)
                    .clickable { if (isSpanish) onLanguageChange() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "AR",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!isSpanish) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getNextPrayer(times: List<String>, now: LocalTime): Pair<Int, Duration> {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    
    for (i in times.indices) {
        val prayerTime = LocalTime.parse(times[i], formatter)
        if (now.isBefore(prayerTime)) {
            val duration = Duration.between(now, prayerTime)
            return Pair(i, duration)
        }
    }
    
    val firstPrayerTime = LocalTime.parse(times[0], formatter)
    val midnightDuration = Duration.between(now, LocalTime.MAX)
    val morningDuration = Duration.between(LocalTime.MIN, firstPrayerTime)
    return Pair(0, midnightDuration.plus(morningDuration))
}

@Composable
fun NextPrayerCard(isSpanish: Boolean) {
    val today = LocalDate.now()
    val times = PrayerTimesRepository.getTimesForDate(today.monthValue, today.dayOfMonth)
        ?: listOf("06:50", "08:15", "12:59", "15:24", "17:43", "19:13")

    val namesEs = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    val namesAr = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
    
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }
    
    val nextPrayerInfo = getNextPrayer(times, currentTime)
    val nextIndex = nextPrayerInfo.first
    val currentDuration = nextPrayerInfo.second
    
    val hours = currentDuration.toHours()
    val minutes = currentDuration.toMinutes() % 60
    val seconds = currentDuration.seconds % 60

    val nextNameEs = namesEs[nextIndex]
    val nextNameAr = namesAr[nextIndex]
    
    val nameText = if (isSpanish) "$nextNameEs | $nextNameAr" else "$nextNameAr | $nextNameEs"
    
    val durationText = if (isSpanish) "En $hours h y $minutes m" else "بعد $hours س و $minutes د"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = (-16).dp)
                .size(96.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSpanish) "PRÓXIMA ORACIÓN • NEXT PRAYER" else "الصلاة القادمة • NEXT PRAYER",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = nameText,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Light,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = times[nextIndex],
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PrayerTimesList(isSpanish: Boolean) {
    val today = LocalDate.now()
    val times = PrayerTimesRepository.getTimesForDate(today.monthValue, today.dayOfMonth)
        ?: listOf("06:50", "08:15", "12:59", "15:24", "17:43", "19:13")
        
    val namesEs = listOf("Fajr | الفجر", "Sunrise", "Dhuhr | الظهر", "Asr | العصر", "Maghrib | المغرب", "Isha | العشاء")
    val subEs = listOf("Alba", "Amanecer", "Mediodía", "Tarde", "Ocaso", "Noche")
    val subAr = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
    val icons = listOf("🌅", "☀️", "☀️", "🌤️", "🌇", "🌙")

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = LocalTime.now()
            delay(60000) // Update minute
        }
    }
    
    val nextIndex = getNextPrayer(times, currentTime).first

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0, 2, 3, 4, 5).forEach { index ->
            val isNext = index == nextIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isNext) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icons[index])
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = namesEs[index],
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isSpanish) subEs[index] else subAr[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = times[index],
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuranReminderCard(isSpanish: Boolean, prefs: android.content.SharedPreferences) {
    var checked by remember { mutableStateOf(prefs.getBoolean("quran_read_today", false)) }

    val currentDay = LocalDate.now().dayOfMonth
    val savedDay = prefs.getInt("quran_saved_day", -1)
    if (savedDay != currentDay) {
        checked = false
        prefs.edit().putBoolean("quran_read_today", false).putInt("quran_saved_day", currentDay).apply()
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { 
                checked = !checked 
                prefs.edit().putBoolean("quran_read_today", checked).apply()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📖", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isSpanish) "RECORDATORIO DE LECTURA" else "تذكير القراءة",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Surah Al-Mulk",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isSpanish) "Recomendado antes de dormir" else "موصى به قبل النوم",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
            } else {
                Icon(Icons.Outlined.Circle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f))
            }
        }
    }
}
