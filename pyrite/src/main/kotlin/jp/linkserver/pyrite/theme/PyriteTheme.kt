package jp.linkserver.pyrite.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Visual families. Persistence and selection belong to the host application. */
enum class PyriteStyle { Material3Expressive, Nothing }

internal val LocalPyriteStyle = staticCompositionLocalOf { PyriteStyle.Material3Expressive }
private val LocalPyriteDarkTheme = staticCompositionLocalOf { false }

/** Shared neutral fill, while keeping the official ColorScheme container roles intact. */
@Composable
internal fun pyriteCardColor(): Color {
    val colors = MaterialTheme.colorScheme
    return if (LocalPyriteStyle.current == PyriteStyle.Material3Expressive && !LocalPyriteDarkTheme.current)
        colors.surfaceContainerLowest else colors.surfaceContainerHigh
}

/**
 * Applies a visual family without owning application state.
 *
 * Nothing uses a fixed monochrome/red palette and ignores [dynamicColor].
 * M3E light uses surfaceContainerLow for the canvas and surfaceContainerLowest for Pyrite cards.
 * Shape and typography belong to Pyrite; no Nothing-owned fonts are bundled.
 */
@Composable
fun PyriteTheme(
    style: PyriteStyle = PyriteStyle.Material3Expressive,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) = PyriteTheme(PyriteFonts(), style, darkTheme, dynamicColor, content)

/** Applies optional semantic font roles; all other design rules remain owned by Pyrite. */
@Composable
fun PyriteTheme(
    fonts: PyriteFonts,
    style: PyriteStyle = PyriteStyle.Material3Expressive,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // The official themes occupy different composition branches. Keep caller UI identity
    // when moving between them, including nested saveable state holders and focus state.
    val currentContent = rememberUpdatedState(content)
    val stableContent = remember { movableContentOf { currentContent.value() } }
    val context = LocalContext.current
    val baseColors = when {
        style == PyriteStyle.Nothing -> if (darkTheme) SourceNothingDark else SourceNothingLight
        dynamicColor && Build.VERSION.SDK_INT >= 31 ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> SourceExpressiveDark
        else -> SourceExpressiveLight
    }
    val colors = if (style == PyriteStyle.Material3Expressive && !darkTheme) {
        baseColors.copy(surface = baseColors.surfaceContainerLow, background = baseColors.surfaceContainerLow)
    } else baseColors
    CompositionLocalProvider(LocalPyriteStyle provides style, LocalPyriteDarkTheme provides darkTheme,
        LocalPyriteFonts provides fonts) {
        if (style == PyriteStyle.Material3Expressive) {
            MaterialExpressiveTheme(
                colorScheme = colors, typography = fonts.applyTo(SourceExpressiveTypography), shapes = SourceExpressiveShapes,
                motionScheme = MotionScheme.expressive(), content = stableContent
            )
        } else {
            MaterialTheme(
                colorScheme = colors,
                typography = fonts.applyTo(SourceNothingTypography),
                shapes = Shapes(),
                motionScheme = MotionScheme.standard(),
                content = stableContent
            )
        }
    }
}

private val SourceExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp), large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)
