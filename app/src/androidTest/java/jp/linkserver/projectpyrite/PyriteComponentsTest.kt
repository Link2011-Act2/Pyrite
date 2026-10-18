package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.dialog.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PyriteComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun switchesHaveOneTargetAndDisabledStateInEveryStyle() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var checked by mutableStateOf(false)
        var events = 0
        compose.setContent {
            PyriteTheme(style, dynamicColor = false) {
                SettingsScreen("Settings") {
                    SettingsGroup(title = "Options") {
                        item("enabled") {
                            SettingsSwitchItem("Enabled toggle", checked, { checked = it; events++ },
                                Modifier.testTag("toggle"))
                        }
                        item("disabled") {
                            SettingsSwitchItem("Disabled toggle", true, { events++ },
                                Modifier.testTag("disabled"), enabled = false)
                        }
                    }
                }
            }
        }
        PyriteStyle.entries.forEach { current ->
            compose.runOnIdle { style = current; checked = false; events = 0 }
            compose.onNodeWithTag("toggle").assertIsOff()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
                .performClick().assertIsOn()
            compose.runOnIdle { assertEquals(1, events) }
            compose.onNodeWithTag("disabled").assertIsNotEnabled().performTouchInput { click() }
            compose.runOnIdle { assertEquals(1, events) }
            compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
                .assertCountEquals(2)
        }
    }

    @Test fun callerStateSurvivesStyleAndExplicitDarkModeChanges() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        var checked by mutableStateOf(false)
        var observed = Color.Unspecified
        compose.setContent {
            PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                val background = MaterialTheme.colorScheme.background
                SideEffect { observed = background }
                SettingsSwitchItem("Persistent", checked, { checked = it }, Modifier.testTag("state"))
            }
        }
        compose.onNodeWithTag("state").performClick()
        PyriteStyle.entries.forEach { current ->
            compose.runOnIdle { style = current; dark = false }
            compose.onNodeWithTag("state").assertIsOn()
            var light = Color.Unspecified
            compose.runOnIdle { light = observed; dark = true }
            compose.onNodeWithTag("state").assertIsOn()
            compose.runOnIdle { assertNotEquals(light, observed) }
        }
    }

    @Test fun choiceDialogSeparatesDraftCancelAndCommit() {
        var show by mutableStateOf(true)
        var draft by mutableStateOf("a")
        var committed = "a"
        compose.setContent {
            PyriteTheme {
                if (show) SettingsChoiceDialog(
                    title = "Choose", choices = listOf(
                        SettingsChoice("a", "a", "First choice"),
                        SettingsChoice("b", "b", "Second choice")
                    ),
                    selectedValue = draft, onValueChange = { draft = it },
                    onConfirm = { committed = draft; show = false },
                    onDismissRequest = { show = false },
                    confirmLabel = "Apply", dismissLabel = "Cancel"
                )
            }
        }
        compose.onNodeWithText("Second choice").performClick().assertIsSelected()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals("a", committed); draft = committed; show = true }
        compose.onNodeWithText("First choice").assertIsSelected()
        compose.onNodeWithText("Second choice").performClick()
        compose.onNodeWithText("Apply").performClick()
        compose.runOnIdle { assertEquals("b", committed) }
    }

    @Test fun keyedRowsKeepLocalStateWhenVisibleRowsAreInserted() {
        var insert by mutableStateOf(false)
        compose.setContent {
            PyriteTheme {
                SettingsGroup {
                    if (insert) item("new") { SettingsActionItem("New row", {}) }
                    item("counter") {
                        var count by remember { mutableIntStateOf(0) }
                        SettingsActionItem("Count $count", { count++ })
                    }
                }
            }
        }
        compose.onNodeWithText("Count 0").performClick()
        compose.runOnIdle { insert = true }
        compose.onNodeWithText("Count 1").assertExists().performClick()
        compose.onNodeWithText("Count 2").assertExists()
    }

    @Test fun aboutSupportsNarrowWidthLargeTextAndAllThemePairs() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        val name = "A long application name that needs wrapping"
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                    Box(Modifier.width(280.dp)) {
                        AboutScreen(
                            info = AboutInfo(name, "2026.10.18-beta01"),
                            overview = AboutOverviewContent(build = AboutDetail("Build", "123456789")),
                            title = "About", links = emptyList(), onLinkClick = {},
                            modifier = Modifier.testTag("about")
                        )
                    }
                }
            }
        }
        PyriteStyle.entries.forEach { current ->
            listOf(false, true).forEach { darkMode ->
                compose.runOnIdle { style = current; dark = darkMode }
                compose.onNodeWithText(name).performScrollTo().assertIsDisplayed()
                val bounds = compose.onNodeWithText(name).getUnclippedBoundsInRoot()
                assertTrue("Text must wrap within the narrow content pane", bounds.right - bounds.left <= 240.dp)
                capture("about-" + current.name + if (darkMode) "-dark" else "-light")
                compose.onNodeWithText("123456789").performScrollTo().assertIsDisplayed()
            }
        }
    }

    private fun capture(name: String) {
        saveScreenshot(
            InstrumentationRegistry.getInstrumentation().targetContext,
            name, compose.onRoot().captureToImage().asAndroidBitmap()
        )
    }
}
