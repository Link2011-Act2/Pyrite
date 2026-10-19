package jp.linkserver.pyrite.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.screen.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import androidx.compose.ui.text.font.FontFamily

/** Standalone binary consumer: no project dependency or Pyrite sources. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var checked by rememberSaveable { mutableStateOf(false) }
            var detail by rememberSaveable { mutableStateOf(true) }
            var screen by rememberSaveable { mutableIntStateOf(0) }
            PyriteTheme(fonts = PyriteFonts(title = FontFamily.Serif, accent = FontFamily.Monospace)) {
                PyriteScreenTransition(screen, { initial, target ->
                    if (target > initial) PyriteNavigationDirection.Forward
                    else PyriteNavigationDirection.Backward
                }) { shown ->
                    if (shown == 0) AboutScreen(
                        info = AboutInfo("Published Pyrite", "0.1.0-SNAPSHOT"),
                        overview = AboutOverviewContent(
                            channel = AboutDetail("Channel", "Development"),
                            build = AboutDetail("Build", "Independent consumer"),
                            version = AboutDetail("Version", "0.1"),
                            attribution = AboutAttribution("Forked from", "Example", "https://example.org")
                        ),
                        links = emptyList(), onLinkClick = {}
                    ) {
                        SettingsGroup(title = "External Maven consumer") {
                            item("feature") {
                                SettingsSplitSwitchItem("Feature", checked, { checked = it },
                                    onClick = { screen = 1 })
                            }
                        }
                    } else SettingsScreen("Feature",
                        navigationIcon = { PyriteBackButton({ screen = 0 }, "Back") }) {
                        SettingsMasterSwitch("Use feature", checked, { checked = it })
                        SettingsGroup(title = "Details") {
                            item("detail") {
                                SettingsSwitchItem("Published AAR is in use", detail, { detail = it },
                                    enabled = checked)
                            }
                        }
                    }
                }
            }
        }
    }
}
