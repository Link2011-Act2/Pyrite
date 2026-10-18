package jp.linkserver.pyrite.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.settings.SettingsGroup
import jp.linkserver.pyrite.settings.SettingsRadioItem

/** A keyed, caller-owned choice. Values and keys must each be unique in one dialog. */
data class SettingsChoice<T>(
    val key: String, val value: T, val label: String,
    val summary: String? = null, val enabled: Boolean = true
)

/**
 * Choice editor built on the official AlertDialog.
 * The caller owns the draft [selectedValue] and commits it in [onConfirm].
 * Dismissing does not implicitly persist or roll back any host state.
 */
@Composable
fun <T> SettingsChoiceDialog(
    title: String,
    choices: List<SettingsChoice<T>>,
    selectedValue: T,
    onValueChange: (T) -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    dismissLabel: String,
    modifier: Modifier = Modifier
) {
    require(choices.map { it.key }.distinct().size == choices.size) { "Choice keys must be unique" }
    require(choices.map { it.value }.distinct().size == choices.size) { "Choice values must be unique" }
    AlertDialog(
        modifier = modifier, onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            SettingsGroup(
                modifier = Modifier.heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()).selectableGroup()
            ) {
                choices.forEach { choice ->
                    item(choice.key) {
                        SettingsRadioItem(
                            title = choice.label, summary = choice.summary,
                            selected = choice.value == selectedValue,
                            enabled = choice.enabled,
                            onClick = { onValueChange(choice.value) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(dismissLabel) } }
    )
}
