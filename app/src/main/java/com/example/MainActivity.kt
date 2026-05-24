package com.example

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Ensure alarms are scheduled initially
        AlarmScheduler.scheduleAlarmsForToday(this)
        
        setContent {
            MyApplicationTheme {
                TawquitApp()
            }
        }
    }
}

enum class Screen { HORARIO, CORAN, QIBLA, AJUSTES }

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TawquitApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var isSpanish by remember { mutableStateOf(prefs.getString("language", "es") == "es") }
    var currentScreen by remember { mutableStateOf(Screen.HORARIO) }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    LaunchedEffect(Unit) {
        if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
        }
    }
    
    val onLanguageChange = {
        isSpanish = !isSpanish
        prefs.edit().putString("language", if (isSpanish) "es" else "ar").apply()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(isSpanish, currentScreen) { currentScreen = it } }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = currentScreen,
                label = "screen_transition",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally { width -> width }) togetherWith
                            (fadeOut(animationSpec = tween(300)) + slideOutHorizontally { width -> -width })
                }
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HORARIO -> HorarioScreen(isSpanish, onLanguageChange, prefs)
                    Screen.CORAN -> QuranScreen(isSpanish)
                    Screen.QIBLA -> QiblaScreen(isSpanish)
                    Screen.AJUSTES -> AjustesScreen(isSpanish, onLanguageChange)
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(isSpanish: Boolean, currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            icon = "🕌", 
            label = if (isSpanish) "Horario" else "جدول", 
            isActive = currentScreen == Screen.HORARIO,
            onClick = { onScreenSelected(Screen.HORARIO) }
        )
        NavBarItem(
            icon = "📖", 
            label = if (isSpanish) "Corán" else "قرآن", 
            isActive = currentScreen == Screen.CORAN,
            onClick = { onScreenSelected(Screen.CORAN) }
        )
        NavBarItem(
            icon = "🧭", 
            label = if (isSpanish) "Qibla" else "قبلة", 
            isActive = currentScreen == Screen.QIBLA,
            onClick = { onScreenSelected(Screen.QIBLA) }
        )
        NavBarItem(
            icon = "⚙️", 
            label = if (isSpanish) "Ajustes" else "إعدادات", 
            isActive = currentScreen == Screen.AJUSTES,
            onClick = { onScreenSelected(Screen.AJUSTES) }
        )
    }
}

@Composable
fun NavBarItem(icon: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isActive) 1f else 0.4f)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
