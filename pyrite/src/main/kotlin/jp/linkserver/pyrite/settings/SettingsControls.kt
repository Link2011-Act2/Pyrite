package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.theme.*

/**
 * Non-clickable surface for custom settings content. In SettingsGroup, corners follow visible items.
 * The slot owns its inner layout and individual enabled controls; there is no parent click target
 * or implicit padding. Prefer the standard items when their layout fits.
 */
@Composable
fun SettingsContentCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    RowSurface(modifier, content = content)

/**
 * Glyph's parameter layout, using Google's Slider for gestures, keyboard input and semantics.
 * No card or persistence is owned here. Value formatting and commit handling belong to the host.
 */
@Composable
fun SettingsSlider(
    title: String, valueText: String, value: Float, onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>, modifier: Modifier = Modifier,
    summary: String? = null, onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true, steps: Int = 0
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < .5f
    val inactive = if (LocalPyriteStyle.current == PyriteStyle.Nothing) {
        if (dark) Color(0xFF2A2A2A) else Color(0xFFF2F2F2)
    } else if (dark) colors.surfaceContainerHighest else colors.surfaceVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().alpha(if (enabled) 1f else .38f),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = LocalPyriteFonts.current.body ?: MaterialTheme.typography.bodyLarge.fontFamily))
            Text(valueText, style = MaterialTheme.typography.labelLarge, color = colors.primary)
        }
        if (!summary.isNullOrBlank()) Text(summary, Modifier.alpha(if (enabled) 1f else .38f),
            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Slider(value, onValueChange, Modifier.fillMaxWidth().semantics { contentDescription = title },
            enabled = enabled, valueRange = valueRange, steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary,
                inactiveTrackColor = inactive, activeTickColor = colors.onPrimary,
                inactiveTickColor = colors.surfaceContainerHighest))
    }
}

/** Standard padded, connected parameter card. Supporting controls remain separate interaction targets. */
@Composable
fun SettingsSliderItem(
    title: String, valueText: String, value: Float, onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>, modifier: Modifier = Modifier,
    summary: String? = null, onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true, steps: Int = 0,
    supportingContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    SettingsContentCard(modifier) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SettingsSlider(title, valueText, value, onValueChange, valueRange, summary = summary,
                onValueChangeFinished = onValueChangeFinished, enabled = enabled, steps = steps)
            supportingContent?.invoke(this)
        }
    }
}

/** Title/summary plus an independent official Button; the card itself never intercepts clicks. */
@Composable
fun SettingsButtonItem(
    title: String, actionLabel: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, summary: String? = null, enabled: Boolean = true
) {
    SettingsContentCard(modifier) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).alpha(if (enabled) 1f else .38f),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (!summary.isNullOrBlank()) Text(summary, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick, enabled = enabled) { Text(actionLabel) }
        }
    }
}
