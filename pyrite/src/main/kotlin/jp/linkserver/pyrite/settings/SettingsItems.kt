@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.internal.PyriteDefaults
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/** Navigates or performs an action. The host handles navigation, intents and errors. */
@Composable
fun SettingsActionItem(
    title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    summary: String? = null, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, summary, modifier, enabled, onClick, leadingContent, trailingContent)

/** Rich supporting content inherits the row's typography; keep actions on the row itself. */
@Composable
fun SettingsActionItem(
    title: String, onClick: () -> Unit, supportingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, null, modifier, enabled, onClick, leadingContent, trailingContent,
    supportingContent = supportingContent)

/** One toggle target for the entire row, including the visual switch. */
@Composable
fun SettingsSwitchItem(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, summary: String? = null, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) = SwitchRow(title, checked, onCheckedChange, modifier, summary, enabled, leadingContent, null)

/** Supporting content may contain descriptions and status text, without another toggle target. */
@Composable
fun SettingsSwitchItem(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    supportingContent: @Composable () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, leadingContent: @Composable (() -> Unit)? = null
) = SwitchRow(title, checked, onCheckedChange, modifier, null, enabled, leadingContent, supportingContent)

@Composable
private fun SwitchRow(
    title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier,
    summary: String?, enabled: Boolean, leadingContent: (@Composable () -> Unit)?,
    supportingContent: (@Composable () -> Unit)?
) {
    SettingsRow(title, summary, modifier.semantics {
        role = Role.Switch
        toggleableState = ToggleableState(checked)
    }, enabled, { onCheckedChange(!checked) }, leadingContent, trailingContent = {
        Switch(checked = checked, onCheckedChange = null, enabled = enabled,
            modifier = Modifier.clearAndSetSemantics {})
    }, supportingContent = supportingContent)
}

/** A single-select row. Wrap sibling choices in a selectableGroup. */
@Composable
fun SettingsRadioItem(
    title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
    summary: String? = null, enabled: Boolean = true
) = RadioRow(title, selected, onClick, modifier, summary, enabled, null)

/** A single-select row with supporting content; selection remains host-owned. */
@Composable
fun SettingsRadioItem(
    title: String, selected: Boolean, onClick: () -> Unit,
    supportingContent: @Composable () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) = RadioRow(title, selected, onClick, modifier, null, enabled, supportingContent)

@Composable
private fun RadioRow(
    title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier, summary: String?,
    enabled: Boolean, supportingContent: (@Composable () -> Unit)?
) {
    SettingsRow(title, summary,
        modifier.semantics { role = Role.RadioButton; this.selected = selected }, enabled, onClick,
        trailingContent = {
            RadioButton(selected, onClick = null, enabled = enabled,
                modifier = Modifier.clearAndSetSemantics {})
        }, selected = selected, supportingContent = supportingContent)
}

/** Non-interactive information in a settings group. Omit hidden rows from the group DSL. */
@Composable
fun SettingsInfoItem(
    title: String, modifier: Modifier = Modifier, summary: String? = null,
    supportingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, summary, modifier.semantics(mergeDescendants = true) {},
    true, null, supportingContent = supportingContent)

/**
 * Non-interactive status, announced politely when it changes. The host supplies localized status
 * text (including the loading description); no asynchronous work is started by this component.
 */
@Composable
fun SettingsStatusItem(
    title: String, status: String, modifier: Modifier = Modifier,
    summary: String? = null, loading: Boolean = false
) {
    SettingsRow(title, null, modifier.semantics(mergeDescendants = true) {
        liveRegion = LiveRegionMode.Polite
        if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }, true, null, supportingContent = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            summary?.let { Text(it) }
            Text(status, color = MaterialTheme.colorScheme.primary)
        }
    }, trailingContent = if (loading) { {
        if (LocalPyriteStyle.current == PyriteStyle.Material3Expressive)
            LoadingIndicator(Modifier.size(40.dp).clearAndSetSemantics {})
        else CircularProgressIndicator(Modifier.size(28.dp).clearAndSetSemantics {}, strokeWidth = 3.dp)
    } } else null)
}

@Composable
private fun SettingsRow(
    title: String, summary: String?, modifier: Modifier, enabled: Boolean,
    onClick: (() -> Unit)?, leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null, selected: Boolean? = null,
    supportingContent: (@Composable () -> Unit)? = null
) {
    val position = LocalGroupPosition.current
    val support: (@Composable () -> Unit)? = supportingContent ?: summary?.let { { Text(it) } }
    when (LocalPyriteStyle.current) {
        PyriteStyle.Material3Expressive -> {
            val shapes = ListItemDefaults.segmentedShapes(position.index, position.count)
            // The official default is Surface; a shared filled container makes the group visible.
            val colors = ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            when {
                onClick == null -> SegmentedListItem(
                    shapes = shapes, colors = colors, modifier = modifier, enabled = enabled,
                    leadingContent = leadingContent, trailingContent = trailingContent,
                    supportingContent = support
                ) { Text(title) }
                selected != null -> SegmentedListItem(
                    selected = selected, onClick = onClick, shapes = shapes, colors = colors, modifier = modifier,
                    enabled = enabled, leadingContent = leadingContent,
                    trailingContent = trailingContent, supportingContent = support
                ) { Text(title) }
                else -> SegmentedListItem(
                    onClick = onClick, shapes = shapes, colors = colors, modifier = modifier, enabled = enabled,
                    leadingContent = leadingContent, trailingContent = trailingContent,
                    supportingContent = support
                ) { Text(title) }
            }
        }
        PyriteStyle.Material3 -> when {
            onClick == null -> ListItem(
                modifier = modifier, enabled = enabled, leadingContent = leadingContent,
                trailingContent = trailingContent, supportingContent = support
            ) { Text(title) }
            selected != null -> ListItem(
                selected = selected, onClick = onClick, modifier = modifier, enabled = enabled,
                leadingContent = leadingContent, trailingContent = trailingContent,
                supportingContent = support
            ) { Text(title) }
            else -> ListItem(
                onClick = onClick, modifier = modifier, enabled = enabled,
                leadingContent = leadingContent, trailingContent = trailingContent,
                supportingContent = support
            ) { Text(title) }
        }
        PyriteStyle.Nothing -> {
            val shape = PyriteDefaults.connectedShape(position.index == 0, position.index == position.count - 1)
            val body: @Composable () -> Unit = {
                Row(Modifier.padding(horizontal = PyriteDefaults.rowHorizontalPadding,
                    vertical = PyriteDefaults.rowVerticalPadding),
                    horizontalArrangement = Arrangement.spacedBy(PyriteDefaults.contentGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leadingContent?.invoke()
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, style = MaterialTheme.typography.bodyLarge)
                        if (support != null) {
                            CompositionLocalProvider(
                                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = if (enabled) 1f else .38f)
                            ) { ProvideTextStyle(MaterialTheme.typography.bodyMedium, support) }
                        }
                    }
                    trailingContent?.invoke()
                }
            }
            val color = MaterialTheme.colorScheme.surfaceContainerHigh
            val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else .38f)
            if (onClick == null) Surface(modifier.fillMaxWidth(), shape, color, contentColor, content = body)
            else Surface(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth(),
                shape = shape, color = color, contentColor = contentColor, content = body)
        }
    }
}
