package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.theme.*

/**
 * Page-level master control, separate from SettingsGroup. Its primary background is state invariant.
 * The regular 72dp control is a pill; 36dp corners prevent clipping when enlarged text grows taller.
 * The host controls dependent rows through their enabled parameters; no setting is disabled implicitly.
 * Place it as a separate SettingsScreen section to retain the standard 28dp separation.
 */
@Composable
fun SettingsMasterSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val nothing = LocalPyriteStyle.current == PyriteStyle.Nothing
    Surface(onClick = { onCheckedChange(!checked) }, enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp).semantics {
            role = Role.Switch; toggleableState = ToggleableState(checked)
        }, shape = RoundedCornerShape(36.dp), color = colors.primary, contentColor = colors.onPrimary) {
        Row(Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (nothing) FontWeight.Normal else FontWeight.Medium,
                color = colors.onPrimary.copy(alpha = if (enabled) 1f else .38f))
            Switch(checked, null, enabled = enabled, modifier = Modifier.clearAndSetSemantics {},
                colors = SwitchDefaults.colors(
                    checkedTrackColor = if (nothing) lerp(colors.primary, colors.onPrimary, .18f) else lerp(colors.primary, Color.White, .45f),
                    checkedThumbColor = colors.onPrimary, checkedIconColor = colors.primary,
                    uncheckedTrackColor = colors.onPrimary, uncheckedThumbColor = colors.outline,
                    uncheckedIconColor = colors.onPrimary, uncheckedBorderColor = colors.outline),
                thumbContent = {
                    Icon(if (checked) Icons.Default.Check else Icons.Default.Close, null,
                        Modifier.size(SwitchDefaults.IconSize))
                })
        }
    }
}

