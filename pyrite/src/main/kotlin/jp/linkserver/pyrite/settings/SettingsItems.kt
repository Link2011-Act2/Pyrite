@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
// Settings geometry ported from Scheduler and Glyph Barty. Copyright (c) 2026 Azul1nk, MIT.
package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.linkserver.pyrite.dialog.SettingsChoice
import jp.linkserver.pyrite.internal.PyriteDefaults
import jp.linkserver.pyrite.theme.*

/** Host-owned action, using the source apps' connected row geometry. */
@Composable
fun SettingsActionItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    summary: String? = null, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null, trailingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, summary, modifier, enabled, onClick, leadingContent, trailingContent)

@Composable
fun SettingsActionItem(title: String, onClick: () -> Unit, supportingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null, trailingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, null, modifier, enabled, onClick, leadingContent, trailingContent, supportingContent)

/** Navigation row with the source apps' official, automatically mirrored 22dp chevron. */
@Composable
fun SettingsNavigationItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    summary: String? = null, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) = SettingsActionItem(title, onClick, modifier, summary, enabled, leadingContent, { NavigationChevron(enabled) })

/** Exactly one toggle target, including the visual switch. */
@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, summary: String? = null, enabled: Boolean = true,
    leadingContent: @Composable (() -> Unit)? = null
) = SwitchRow(title, checked, onCheckedChange, modifier, summary, enabled, leadingContent, null)

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    supportingContent: @Composable () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, leadingContent: @Composable (() -> Unit)? = null
) = SwitchRow(title, checked, onCheckedChange, modifier, null, enabled, leadingContent, supportingContent)

@Composable private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier, summary: String?, enabled: Boolean, leadingContent: (@Composable () -> Unit)?,
    supportingContent: (@Composable () -> Unit)?
) {
    SettingsRow(title, summary, modifier.semantics {
        role = Role.Switch; toggleableState = ToggleableState(checked)
    }, enabled, { onCheckedChange(!checked) }, leadingContent,
        { SourceSwitch(checked, enabled) }, supportingContent, switchRow = true)
}

/** Scheduler's independent navigation and switch targets. The host owns each enable condition. */
@Composable
fun SettingsSplitSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit, modifier: Modifier = Modifier, summary: String? = null,
    navigationEnabled: Boolean = true, switchEnabled: Boolean = true
) {
    RowSurface(modifier) {
        Row(Modifier.height(IntrinsicSize.Min).heightIn(min = 72.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick, Modifier.weight(1f).fillMaxHeight(), enabled = navigationEnabled,
                color = androidx.compose.ui.graphics.Color.Transparent) {
                Row(Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    RowText(title, summary, null, navigationEnabled, Modifier.weight(1f))
                    NavigationChevron(navigationEnabled)
                }
            }
            VerticalDivider(Modifier.height(32.dp))
            Box(Modifier.fillMaxHeight().toggleable(checked, enabled = switchEnabled, role = Role.Switch, onValueChange = onCheckedChange)
                .semantics { contentDescription = title }.padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center) { SourceSwitch(checked, switchEnabled) }
        }
    }
}

@Composable
fun SettingsRadioItem(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
    summary: String? = null, enabled: Boolean = true
) = RadioRow(title, selected, onClick, modifier, summary, enabled, null)

@Composable
fun SettingsRadioItem(title: String, selected: Boolean, onClick: () -> Unit,
    supportingContent: @Composable () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) = RadioRow(title, selected, onClick, modifier, null, enabled, supportingContent)

@Composable private fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier,
    summary: String?, enabled: Boolean, supportingContent: (@Composable () -> Unit)?
) = SettingsRow(title, summary, modifier.semantics { role = Role.RadioButton; this.selected = selected },
    enabled, onClick, trailingContent = {
        RadioButton(selected, null, enabled = enabled, modifier = Modifier.clearAndSetSemantics {})
    }, supportingContent = supportingContent)

/** Glyph Barty's compound radio setting. Choice state and persistence belong to the caller. */
@Composable
fun <T> SettingsRadioGroupItem(title: String, choices: List<SettingsChoice<T>>, selectedValue: T,
    onValueChange: (T) -> Unit, modifier: Modifier = Modifier, summary: String? = null, enabled: Boolean = true
) {
    require(choices.map { it.key }.distinct().size == choices.size) { "Choice keys must be unique" }
    require(choices.map { it.value }.distinct().size == choices.size) { "Choice values must be unique" }
    RowSurface(modifier) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp).selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RowText(title, summary, null, enabled)
            choices.forEach { choice ->
                key(choice.key) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                        selectedValue == choice.value, enabled = enabled && choice.enabled, role = Role.RadioButton, onClick =
                        { onValueChange(choice.value) }), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RadioButton(selectedValue == choice.value, null, enabled = enabled && choice.enabled,
                            modifier = Modifier.clearAndSetSemantics {})
                        val alpha = if (enabled && choice.enabled) 1f else .38f
                        Column(Modifier.weight(1f)) {
                            Text(choice.label, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                            choice.summary?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsInfoItem(title: String, modifier: Modifier = Modifier, summary: String? = null,
    supportingContent: @Composable (() -> Unit)? = null
) = SettingsRow(title, summary, modifier.semantics(mergeDescendants = true) {}, true, null,
    supportingContent = supportingContent)

/** Politely announced caller-owned status; this component starts no asynchronous work. */
@Composable
fun SettingsStatusItem(title: String, status: String, modifier: Modifier = Modifier,
    summary: String? = null, loading: Boolean = false
) {
    SettingsRow(title, null, modifier.semantics(mergeDescendants = true) {
        liveRegion = LiveRegionMode.Polite
        if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }, true, null, trailingContent = if (loading) { {
        if (LocalPyriteStyle.current == PyriteStyle.Material3Expressive)
            LoadingIndicator(Modifier.size(40.dp).clearAndSetSemantics {})
        else CircularProgressIndicator(Modifier.size(28.dp).clearAndSetSemantics {}, strokeWidth = 3.dp)
    } } else null, supportingContent = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            summary?.let { Text(it) }
            Text(status, color = MaterialTheme.colorScheme.primary)
        }
    })
}

@Composable private fun SourceSwitch(checked: Boolean, enabled: Boolean) {
    val expressive = LocalPyriteStyle.current == PyriteStyle.Material3Expressive
    Switch(checked, null, enabled = enabled, modifier = (if (expressive) Modifier else Modifier.heightIn(min = 48.dp)).clearAndSetSemantics {},
        colors = if (expressive) SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedIconColor = MaterialTheme.colorScheme.primary) else SwitchDefaults.colors(),
        thumbContent = if (expressive) { {
            Icon(if (checked) Icons.Default.Check else Icons.Default.Close, null,
                Modifier.size(SwitchDefaults.IconSize))
        } } else null)
}

@Composable private fun NavigationChevron(enabled: Boolean = true) {
    val color = if (LocalPyriteStyle.current == PyriteStyle.Nothing) {
        if (MaterialTheme.colorScheme.background.luminance() < .5f)
            androidx.compose.ui.graphics.Color(0xFFEDEDED)
        else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
        Modifier.size(22.dp).alpha(if (enabled) 1f else .38f), tint = color)
}

@Composable private fun RowSurface(modifier: Modifier, onClick: (() -> Unit)? = null,
    enabled: Boolean = true, content: @Composable () -> Unit
) {
    val position = LocalGroupPosition.current
    val shape = PyriteDefaults.connectedShape(position.index == 0, position.index == position.count - 1)
    val color = MaterialTheme.colorScheme.surfaceContainerHigh
    if (onClick == null) Surface(modifier.fillMaxWidth().heightIn(min = 72.dp), shape, color, content = content)
    else Surface(onClick, modifier.fillMaxWidth().heightIn(min = 72.dp), enabled = enabled,
        shape = shape, color = color, content = content)
}

@Composable private fun SettingsRow(title: String, summary: String?, modifier: Modifier, enabled: Boolean,
    onClick: (() -> Unit)?, leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null, supportingContent: (@Composable () -> Unit)? = null, switchRow: Boolean = false
) {
    RowSurface(modifier, onClick, enabled) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(if (switchRow && LocalPyriteStyle.current == PyriteStyle.Material3Expressive) 16.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            leadingContent?.invoke()
            RowText(title, summary, supportingContent, enabled, Modifier.weight(1f))
            trailingContent?.invoke()
        }
    }
}

@Composable private fun RowText(title: String, summary: String?, supporting: (@Composable () -> Unit)?,
    enabled: Boolean, modifier: Modifier = Modifier
) {
    val expressive = LocalPyriteStyle.current == PyriteStyle.Material3Expressive
    val alpha = if (enabled) 1f else .38f
    val summaryStyle = if (expressive) MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp,
        letterSpacing = .15.sp) else MaterialTheme.typography.bodyMedium
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = if (expressive) MaterialTheme.typography.bodyLarge.copy(
            fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp,
            letterSpacing = .2.sp, fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
        if (!summary.isNullOrBlank() || supporting != null) CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        ) {
            ProvideTextStyle(summaryStyle) {
                if (supporting != null) supporting() else Text(summary.orEmpty(), style = summaryStyle)
            }
        }
    }
}
