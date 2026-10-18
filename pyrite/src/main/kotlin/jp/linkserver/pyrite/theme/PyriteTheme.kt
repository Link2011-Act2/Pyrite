package jp.linkserver.pyrite.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/** Visual families. Persistence and selection belong to the host application. */
enum class PyriteStyle { Material3, Material3Expressive, Nothing }

internal val LocalPyriteStyle = staticCompositionLocalOf { PyriteStyle.Material3 }

/**
 * Applies a visual family without owning application state.
 *
 * Nothing uses a fixed monochrome/red palette and ignores [dynamicColor].
 * [nothingFontFamily] is supplied by the host; no Nothing-owned fonts are bundled.
 * Explicit [colorScheme], [typography] and [shapes] take precedence over defaults.
 */
@Composable
fun PyriteTheme(
    style: PyriteStyle = PyriteStyle.Material3Expressive,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    colorScheme: ColorScheme? = null,
    typography: Typography? = null,
    shapes: Shapes? = null,
    nothingFontFamily: FontFamily = FontFamily.SansSerif,
    content: @Composable () -> Unit
) {
    // The official themes occupy different composition branches. Keep caller UI identity
    // when moving between them, including nested saveable state holders and focus state.
    val currentContent = rememberUpdatedState(content)
    val stableContent = remember { movableContentOf { currentContent.value() } }
    val context = LocalContext.current
    val colors = colorScheme ?: when {
        style == PyriteStyle.Nothing -> nothingColors(darkTheme)
        dynamicColor && Build.VERSION.SDK_INT >= 31 ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        style == PyriteStyle.Material3Expressive -> expressiveLightColorScheme()
        else -> lightColorScheme()
    }
    CompositionLocalProvider(LocalPyriteStyle provides style) {
        if (style == PyriteStyle.Material3Expressive) {
            MaterialExpressiveTheme(
                colorScheme = colors, typography = typography, shapes = shapes,
                motionScheme = MotionScheme.expressive(), content = stableContent
            )
        } else {
            MaterialTheme(
                colorScheme = colors,
                typography = typography ?: if (style == PyriteStyle.Nothing) Typography(fontFamily = nothingFontFamily) else Typography(),
                shapes = shapes ?: if (style == PyriteStyle.Nothing) NothingShapes else Shapes(),
                motionScheme = MotionScheme.standard(),
                content = stableContent
            )
        }
    }
}

private val NothingShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private fun nothingColors(dark: Boolean): ColorScheme {
    val black = Color(0xFF101010)
    val white = Color(0xFFF5F5F5)
    val red = Color(0xFFB3261E)
    return if (dark) darkColorScheme(
        primary = white, onPrimary = black, primaryContainer = Color(0xFF303030),
        onPrimaryContainer = white, secondary = white, onSecondary = black,
        secondaryContainer = Color(0xFF282828), onSecondaryContainer = white,
        tertiary = Color(0xFFFFB4AB), onTertiary = Color(0xFF690005),
        background = Color.Black, surface = Color.Black,
        surfaceContainerLowest = Color(0xFF080808), surfaceContainerLow = black,
        surfaceContainer = Color(0xFF151515), surfaceContainerHigh = Color(0xFF202020),
        surfaceContainerHighest = Color(0xFF292929), onSurface = white,
        onSurfaceVariant = Color(0xFFCACACA), outline = Color(0xFF929292),
        outlineVariant = Color(0xFF454545)
    ) else lightColorScheme(
        primary = black, onPrimary = Color.White, primaryContainer = Color(0xFFE2E2E2),
        onPrimaryContainer = black, secondary = black, onSecondary = Color.White,
        secondaryContainer = Color(0xFFE6E6E6), onSecondaryContainer = black,
        tertiary = red, onTertiary = Color.White,
        background = Color(0xFFF2F2F2), surface = Color(0xFFF2F2F2),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF8F8F8),
        surfaceContainer = Color.White, surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color(0xFFE7E7E7), onSurface = black,
        onSurfaceVariant = Color(0xFF535353), outline = Color(0xFF747474),
        outlineVariant = Color(0xFFC8C8C8)
    )
}
