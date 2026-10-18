// Ported from the reference apps. Copyright (c) 2026 Azul1nk. MIT; see NOTICE.md.
package jp.linkserver.pyrite.theme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
private val LightPrimary = Color(0xFF0E5E76)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFBFE9FF)
private val LightOnPrimaryContainer = Color(0xFF001F29)

private val LightSecondary = Color(0xFF4A626C)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFCDE7F2)
private val LightOnSecondaryContainer = Color(0xFF061E26)

private val LightTertiary = Color(0xFF5D5B7A)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFE4DEFF)
private val LightOnTertiaryContainer = Color(0xFF1A1733)

private val LightBackground = Color(0xFFEAF2F6)
private val LightOnBackground = Color(0xFF141C20)
private val LightSurface = Color(0xFFE7EFF4)
private val LightOnSurface = Color(0xFF141C20)
private val LightSurfaceContainerLowest = Color(0xFFF2F8FB)
private val LightSurfaceContainerLow = Color(0xFFE2EBF0)
private val LightSurfaceContainer = Color(0xFFD5E0E7)
private val LightSurfaceContainerHigh = Color(0xFFC7D4DC)
private val LightSurfaceContainerHighest = Color(0xFFB9C8D1)
private val LightSurfaceVariant = Color(0xFFCCD7DE)
private val LightOnSurfaceVariant = Color(0xFF40484C)
private val LightOutline = Color(0xFF71797E)

private val DarkPrimary = Color(0xFF8DD3EE)
private val DarkOnPrimary = Color(0xFF003544)
private val DarkPrimaryContainer = Color(0xFF004D61)
private val DarkOnPrimaryContainer = Color(0xFFBFE9FF)

private val DarkSecondary = Color(0xFFB2CBD6)
private val DarkOnSecondary = Color(0xFF1C333C)
private val DarkSecondaryContainer = Color(0xFF334A53)
private val DarkOnSecondaryContainer = Color(0xFFCDE7F2)

private val DarkTertiary = Color(0xFFC7C0EA)
private val DarkOnTertiary = Color(0xFF2F2D49)
private val DarkTertiaryContainer = Color(0xFF464364)
private val DarkOnTertiaryContainer = Color(0xFFE4DEFF)

private val DarkBackground = Color(0xFF0A1013)
private val DarkOnBackground = Color(0xFFDEE3E6)
private val DarkSurface = Color(0xFF0E1519)
private val DarkOnSurface = Color(0xFFDEE3E6)
private val DarkSurfaceContainerLowest = Color(0xFF0B1114)
private val DarkSurfaceContainerLow = Color(0xFF161E23)
private val DarkSurfaceContainer = Color(0xFF1E2830)
private val DarkSurfaceContainerHigh = Color(0xFF26313A)
private val DarkSurfaceContainerHighest = Color(0xFF2F3C46)
private val DarkSurfaceVariant = Color(0xFF4A555E)
private val DarkOnSurfaceVariant = Color(0xFFC0C8CD)
private val DarkOutline = Color(0xFF8A9297)

private val Error = Color(0xFFBA1A1A)
private val NothingBlack = Color(0xFF000000)
private val NothingWhite = Color(0xFFFFFFFF)
private val NothingRed = Color(0xFFD71921)
private val NothingLightSurface = Color(0xFFEDEDED)
internal val SourceExpressiveLight = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = Error
)

internal val SourceExpressiveDark = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Error
)

internal val SourceNothingDark = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    primaryContainer = Color(0xFFE7E7E7),
    onPrimaryContainer = NothingBlack,
    secondary = Color(0xFFD8D8D8),
    onSecondary = NothingBlack,
    secondaryContainer = Color(0xFF1B1B1B),
    onSecondaryContainer = NothingWhite,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFF2B1113),
    onTertiaryContainer = Color(0xFFFFDAD9),
    background = NothingBlack,
    surface = NothingBlack,
    surfaceContainer = Color(0xFF050505),
    surfaceContainerHigh = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF181818),
    surfaceDim = Color(0xFF030303),
    onSurface = NothingWhite,
    onSurfaceVariant = Color(0xFFD0D0D0),
    outline = Color(0xFF707070),
    outlineVariant = Color(0xFF3A3A3A)
)

internal val SourceNothingLight = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    primaryContainer = NothingBlack,
    onPrimaryContainer = NothingWhite,
    secondary = NothingBlack,
    onSecondary = NothingWhite,
    secondaryContainer = Color(0xFFE2E2E2),
    onSecondaryContainer = NothingBlack,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFFFFDAD9),
    onTertiaryContainer = Color(0xFF410005),
    background = NothingLightSurface,
    surface = NothingLightSurface,
    surfaceContainer = NothingWhite,
    surfaceContainerHigh = NothingWhite,
    surfaceContainerHighest = NothingWhite,
    surfaceDim = Color(0xFFD6D6D6),
    onSurface = NothingBlack,
    onSurfaceVariant = Color(0xFF565656),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE7E7E7)
)

