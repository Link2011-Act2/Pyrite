package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/** Navigates or performs an action. The host handles navigation, intents and errors. */
@Composable
fun SettingsActionItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    SettingsRow(title, summary, modifier, enabled, onClick, leadingContent, trailingContent)
}

/** One toggle target for the entire row, including the visual switch. */
@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) {
    SettingsRow(
        title, summary,
        modifier.semantics {
            role = Role.Switch
            toggleableState = ToggleableState(checked)
        },
        enabled, { onCheckedChange(!checked) }, leadingContent,
        trailingContent = {
            Switch(
                checked = checked, onCheckedChange = null, enabled = enabled,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    )
}

/** A single-select row. Wrap sibling choices in a selectableGroup, or use SettingsChoiceDialog. */
@Composable
fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true
) {
    SettingsRow(
        title, summary,
        modifier.semantics { role = Role.RadioButton; this.selected = selected },
        enabled, onClick,
        trailingContent = {
            RadioButton(selected, onClick = null, enabled = enabled,
                modifier = Modifier.clearAndSetSemantics {})
        },
        selected = selected
    )
}

@Composable
private fun SettingsRow(
    title: String,
    summary: String?,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    selected: Boolean? = null
) {
    val style = LocalPyriteStyle.current
    val position = LocalGroupPosition.current
    val support: (@Composable () -> Unit)? = summary?.let { { Text(it) } }
    when (style) {
        PyriteStyle.Material3Expressive -> {
            val shapes = ListItemDefaults.segmentedShapes(position.index, position.count)
            if (selected != null) {
                SegmentedListItem(
                    selected = selected, onClick = onClick, shapes = shapes, modifier = modifier,
                    enabled = enabled, leadingContent = leadingContent,
                    trailingContent = trailingContent, supportingContent = support
                ) { Text(title) }
            } else {
                SegmentedListItem(
                    onClick = onClick, shapes = shapes, modifier = modifier, enabled = enabled,
                    leadingContent = leadingContent, trailingContent = trailingContent,
                    supportingContent = support
                ) { Text(title) }
            }
        }
        PyriteStyle.Material3 -> {
            if (selected != null) {
                ListItem(
                    selected = selected, onClick = onClick, modifier = modifier, enabled = enabled,
                    leadingContent = leadingContent, trailingContent = trailingContent,
                    supportingContent = support
                ) { Text(title) }
            } else {
                ListItem(
                    onClick = onClick, modifier = modifier, enabled = enabled,
                    leadingContent = leadingContent, trailingContent = trailingContent,
                    supportingContent = support
                ) { Text(title) }
            }
        }
        PyriteStyle.Nothing -> {
            val first = position.index == 0
            val last = position.index == position.count - 1
            val outer = MaterialTheme.shapes.large
            val inner = MaterialTheme.shapes.extraSmall
            val rowShape = inner.copy(
                    topStart = if (first) outer.topStart else inner.topStart,
                    topEnd = if (first) outer.topEnd else inner.topEnd,
                    bottomStart = if (last) outer.bottomStart else inner.bottomStart,
                    bottomEnd = if (last) outer.bottomEnd else inner.bottomEnd
            )
            Surface(
                onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth(),
                shape = rowShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else .38f)
            ) {
                Row(
                    Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingContent?.invoke()
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.bodyLarge)
                        if (summary != null) Text(
                            summary, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else .38f)
                        )
                    }
                    trailingContent?.invoke()
                }
            }
        }
    }
}
