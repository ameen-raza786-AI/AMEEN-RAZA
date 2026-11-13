package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val RazaDarkColorScheme =
  darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = ChampagneGold,
    secondary = SterlingSilver,
    onSecondary = ObsidianBlack,
    secondaryContainer = ElevatedGraphite,
    onSecondaryContainer = PlatinumWhite,
    tertiary = ChampagneGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PlatinumWhite,
    surface = VelvetCharcoal,
    onSurface = PlatinumWhite,
    surfaceVariant = SmokedOnyx,
    onSurfaceVariant = MutedSilver,
    outline = HairlineGold,
    outlineVariant = Color(0xFF2A2A34),
    error = CrimsonVelvet,
    onError = Color.White,
  )

private val RazaLightColorScheme =
  lightColorScheme(
    primary = DeepRoyalGold,
    onPrimary = Color.White,
    primaryContainer = WarmParchment,
    onPrimaryContainer = InkObsidian,
    secondary = SlateSilver,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E3DC),
    onSecondaryContainer = InkObsidian,
    tertiary = AntiqueBronze,
    onTertiary = Color.White,
    background = IvoryAlabaster,
    onBackground = InkObsidian,
    surface = SilkWhite,
    onSurface = InkObsidian,
    surfaceVariant = WarmParchment,
    onSurfaceVariant = SlateSilver,
    outline = Color(0xFFD2C5A5),
    outlineVariant = Color(0xFFE5DFD2),
    error = CrimsonVelvet,
    onError = Color.White,
  )

val RazaShapes =
  Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun RazaEthnicWearTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) RazaDarkColorScheme else RazaLightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = RazaTypography,
    shapes = RazaShapes,
    content = content,
  )
}
