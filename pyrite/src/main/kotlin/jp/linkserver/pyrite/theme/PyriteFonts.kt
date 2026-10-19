package jp.linkserver.pyrite.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle

/**
 * Optional host-owned font families. Null retains each component's original family.
 * [title] covers screen/identity titles and strong Channel values; [accent] covers
 * decorative Version values. [body] covers ordinary text. No font files are bundled.
 */
@Immutable
data class PyriteFonts(
    val title: FontFamily? = null,
    val accent: FontFamily? = null,
    val body: FontFamily? = null
)

internal val LocalPyriteFonts = staticCompositionLocalOf { PyriteFonts() }

internal fun PyriteFonts.applyTo(source: Typography): Typography {
    if (title == null && body == null) return source
    fun titleStyle(style: TextStyle) = style.copy(fontFamily = title ?: style.fontFamily
        ?: if (body != null) FontFamily.Default else null)
    return source.copy(
        displayLarge = titleStyle(source.displayLarge),
        displayMedium = titleStyle(source.displayMedium),
        displaySmall = titleStyle(source.displaySmall),
        headlineLarge = titleStyle(source.headlineLarge),
        headlineMedium = titleStyle(source.headlineMedium),
        headlineSmall = titleStyle(source.headlineSmall),
        titleLarge = titleStyle(source.titleLarge),
        titleMedium = titleStyle(source.titleMedium),
        titleSmall = source.titleSmall.copy(fontFamily = body ?: source.titleSmall.fontFamily),
        bodyLarge = source.bodyLarge.copy(fontFamily = body ?: source.bodyLarge.fontFamily),
        bodyMedium = source.bodyMedium.copy(fontFamily = body ?: source.bodyMedium.fontFamily),
        bodySmall = source.bodySmall.copy(fontFamily = body ?: source.bodySmall.fontFamily),
        labelLarge = source.labelLarge.copy(fontFamily = body ?: source.labelLarge.fontFamily),
        labelMedium = source.labelMedium.copy(fontFamily = body ?: source.labelMedium.fontFamily),
        labelSmall = source.labelSmall.copy(fontFamily = body ?: source.labelSmall.fontFamily)
    )
}
