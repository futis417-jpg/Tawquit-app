package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val NaturalColorScheme =
  lightColorScheme(
      primary = NaturalGreen,
      secondary = NaturalHeader,
      tertiary = NaturalMuted,
      background = NaturalBg,
      surface = NaturalSurface,
      onPrimary = androidx.compose.ui.graphics.Color.White,
      onSecondary = NaturalText,
      onTertiary = NaturalText,
      onBackground = NaturalText,
      onSurface = NaturalText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = NaturalColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
