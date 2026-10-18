package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.settings.*

/** Catalog-owned shared state, separate from the reusable library components. */
@Composable
internal fun HierarchyCatalog(
    detail: Boolean, enabled: Boolean, onEnabledChange: (Boolean) -> Unit,
    undo: Boolean, onUndoChange: (Boolean) -> Unit, toggles: Int, navigations: Int,
    onOpenDetail: () -> Unit
) {
    if (!detail) {
        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
            SettingsGroup(title = stringResource(R.string.hierarchy_features)) {
                item("correction") {
                    SettingsSplitSwitchItem(
                        title = stringResource(R.string.autocorrection), checked = enabled,
                        onCheckedChange = onEnabledChange,
                        onClick = onOpenDetail,
                        summary = stringResource(R.string.hierarchy_summary))
                }
            }
            Text(stringResource(R.string.hierarchy_events, toggles, navigations),
                style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(44.dp)) {
            SettingsMasterSwitch(stringResource(R.string.use_autocorrection), enabled,
                onEnabledChange)
            SettingsGroup(title = stringResource(R.string.undo_section)) {
                item("undo") {
                    SettingsSwitchItem(stringResource(R.string.undo_correction), undo, onUndoChange,
                        enabled = enabled)
                }
            }
            Text(stringResource(R.string.hierarchy_events, toggles, navigations),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
