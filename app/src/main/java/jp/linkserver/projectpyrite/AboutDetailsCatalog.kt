package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import jp.linkserver.pyrite.settings.*

/** Local saveable state and scrolling are deliberately inside the retained detail destination. */
@Composable
internal fun ColumnScope.AboutDetailsCatalog() {
    var extra by rememberSaveable { mutableStateOf(false) }
    Text(stringResource(R.string.navigation_details_intro))
    SettingsGroup(title = stringResource(R.string.test_conditions)) {
        item("extra") {
            SettingsSwitchItem(stringResource(R.string.navigation_details_extra), extra, { extra = it })
        }
    }
    SettingsGroup(title = stringResource(R.string.build_info)) {
        listOf(
            "Artifact" to "jp.linkserver:pyrite:0.1.0-SNAPSHOT",
            "Version" to "0.1.0-SNAPSHOT",
            "Material 3" to "1.5.0-beta01",
            "Compose" to "1.12.0",
            "Compile SDK" to "37",
            "Minimum SDK" to "26",
            "AGP" to "9.4.1",
            "Gradle" to "9.6.0"
        ).forEach { (label, value) ->
            item(label) { SettingsInfoItem(label, summary = value) }
        }
    }
    if (extra) SettingsGroup(title = stringResource(R.string.supporting_slot)) {
        item("java") { SettingsInfoItem("Java bytecode", summary = "11") }
        item("jdk") { SettingsInfoItem("Build JDK", summary = "21") }
    }
}
