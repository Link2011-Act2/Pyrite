package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.card.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PyriteExtendedComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun richRowsStatusAndBusyActionsRespectSemanticsInFourThemes() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        var checked by mutableStateOf(false)
        var loading by mutableStateOf(false)
        var callbacks = 0
        compose.setContent {
            PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                SettingsScreen("Pyrite") {
                    SettingsGroup(title = "Connected settings") {
                        item("toggle") {
                            SettingsSwitchItem("Rich toggle", checked, { checked = it; callbacks++ },
                                supportingContent = { Column { Text("Description"); Text("Effective status") } },
                                modifier = Modifier.testTag("rich"))
                        }
                        item("info") { SettingsInfoItem("Read only", summary = "No action", modifier = Modifier.testTag("info")) }
                        item("status") {
                            SettingsStatusItem("Operation", if (loading) "Working" else "Ready",
                                loading = loading, modifier = Modifier.testTag("status"))
                        }
                    }
                    SettingsActionCard("Updates", "A host-owned operation", "Check", { callbacks++ },
                        loading = loading, modifier = Modifier.testTag("card"))
                    SettingsInfoCard("Explanation", "Supporting text") { Text("Extra content") }
                }
            }
        }
        PyriteStyle.entries.forEach { family ->
            listOf(false, true).forEach { darkMode ->
                compose.runOnIdle { style = family; dark = darkMode; checked = false; loading = false; callbacks = 0 }
                compose.onNodeWithTag("rich").performScrollTo().performClick().assertIsOn()
                compose.runOnIdle { assertEquals(1, callbacks) }
                compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertCountEquals(1)
                compose.onNodeWithTag("info").assertHasNoClickAction()
                compose.onNodeWithTag("status").assertHasNoClickAction()
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
                compose.onNodeWithText("Check").performScrollTo().performClick()
                compose.runOnIdle { assertEquals(2, callbacks); loading = true }
                compose.onNodeWithText("Check").performScrollTo().assertIsNotEnabled().performTouchInput { click() }
                compose.runOnIdle { assertEquals(2, callbacks) }
                compose.onNodeWithTag("status").assert(SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
                capture("extended-${family.name}-${if (darkMode) "dark" else "light"}")
            }
        }
    }

    @Test fun mosaicKeepsVersionAndMetadataActionsIndependent() {
        var detailEvents = 0
        var versionEvents = 0
        compose.setContent {
            PyriteTheme {
                SettingsScreen("About") {
                    AboutOverviewMosaic(
                        AboutInfo("Application", "Version 1"),
                        AboutOverviewContent(channel = AboutDetail("Channel", "Preview")),
                        onVersionClick = { versionEvents++ }, onChannelClick = { detailEvents++ })
                }
            }
        }
        compose.onNodeWithText("Version 1").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, versionEvents); assertEquals(0, detailEvents) }
        compose.onNodeWithText("Preview").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, detailEvents); assertEquals(1, versionEvents) }
    }
    @Test fun conditionalRowsSettleAfterInterruptedSizeAnimation() {
        var edges by mutableStateOf(true)
        var checked by mutableStateOf(false)
        var callbacks = 0
        compose.setContent {
            PyriteTheme {
                SettingsScreen("Conditional settings") {
                    SettingsGroup(modifier = Modifier.animateContentSize()) {
                        if (edges) item("first") { SettingsInfoItem("First") }
                        item("middle") { SettingsSwitchItem("Retained", checked,
                            { checked = it; callbacks++ }, Modifier.testTag("retained")) }
                        if (edges) item("last") { SettingsInfoItem("Last") }
                    }
                }
            }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { edges = false }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { edges = true }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { edges = false }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithText("First").assertDoesNotExist()
        compose.onNodeWithText("Last").assertDoesNotExist()
        compose.onNodeWithTag("retained").assertIsDisplayed().performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, callbacks) }
    }

    @Test fun localUiStateSurvivesExpressiveThemeBoundaryAndUsesLatestCallback() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var callbackVersion by mutableIntStateOf(0)
        var observedVersion = -1
        compose.setContent {
            val currentVersion = callbackVersion
            PyriteTheme(style, dynamicColor = false) {
                var count by rememberSaveable { mutableIntStateOf(0) }
                SettingsActionItem("Local count $count", { count++; observedVersion = currentVersion })
            }
        }
        compose.onNodeWithText("Local count 0").performClick()
        PyriteStyle.entries.forEachIndexed { index, next ->
            compose.runOnIdle { style = next; callbackVersion = index + 1 }
            compose.onNodeWithText("Local count ${index + 1}").performClick()
            compose.runOnIdle { assertEquals(index + 1, observedVersion) }
        }
        compose.onNodeWithText("Local count 3").assertExists()
    }

    private fun capture(name: String) = saveScreenshot(
        InstrumentationRegistry.getInstrumentation().targetContext,
        name, compose.onRoot().captureToImage().asAndroidBitmap())
}
