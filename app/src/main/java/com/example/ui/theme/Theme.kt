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

private val DarkColorScheme = darkColorScheme(
  primary = ElegantDarkPrimary,
  onPrimary = ElegantDarkOnPrimary,
  secondary = ElegantDarkSecondary,
  onSecondary = ElegantDarkTextPrimary,
  background = ElegantDarkBg,
  onBackground = ElegantDarkTextPrimary,
  surface = ElegantDarkSurface,
  onSurface = ElegantDarkTextPrimary,
  surfaceVariant = ElegantDarkSecondary,
  onSurfaceVariant = ElegantDarkTextSecondary,
  outline = ElegantDarkTextSecondary,
  primaryContainer = ElegantDarkSecondary,
  onPrimaryContainer = ElegantDarkPrimary,
  tertiary = Purple80
)

private val LightColorScheme = darkColorScheme( // Enforce Elegant Dark on all themes
  primary = ElegantDarkPrimary,
  onPrimary = ElegantDarkOnPrimary,
  secondary = ElegantDarkSecondary,
  onSecondary = ElegantDarkTextPrimary,
  background = ElegantDarkBg,
  onBackground = ElegantDarkTextPrimary,
  surface = ElegantDarkSurface,
  onSurface = ElegantDarkTextPrimary,
  surfaceVariant = ElegantDarkSecondary,
  onSurfaceVariant = ElegantDarkTextSecondary,
  outline = ElegantDarkTextSecondary,
  primaryContainer = ElegantDarkSecondary,
  onPrimaryContainer = ElegantDarkPrimary,
  tertiary = Purple80
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for the Elegant Dark theme look
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our crafted design
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
