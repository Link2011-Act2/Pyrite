package jp.linkserver.pyrite.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var checked by remember { mutableStateOf(false) }
            PyriteTheme {
                SettingsScreen("Published Pyrite") {
                    SettingsGroup(title = "External Maven consumer") {
                        item("toggle") {
                            SettingsSwitchItem("Published AAR is in use", checked, { checked = it })
                        }
                    }
                }
            }
        }
    }
}
