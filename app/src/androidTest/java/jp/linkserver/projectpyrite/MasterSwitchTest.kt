package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MasterSwitchTest {
    @get:Rule val compose = createComposeRule()

    @Test fun backgroundIsInvariantAndDependentValueIsRetainedInBothStylesAndColorModes() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        var checked by mutableStateOf(true)
        var dependent by mutableStateOf(true)
        var events = 0
        var dependentEvents = 0
        var expectedBackground = 0
        compose.setContent {
            PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                val primary = MaterialTheme.colorScheme.primary.toArgb()
                SideEffect { expectedBackground = primary }
                SettingsScreen("Auto-correction") {
                    SettingsMasterSwitch("Use auto-correction", checked, { checked = it; events++ },
                        Modifier.testTag("master"))
                    SettingsGroup(title = "Undo") {
                        item("undo") { SettingsSwitchItem("Undo with Backspace", dependent,
                            { dependent = it; dependentEvents++ }, enabled = checked, modifier = Modifier.testTag("dependent")) }
                    }
                }
            }
        }
        PyriteStyle.entries.forEach { family ->
            listOf(false, true).forEach { mode ->
                compose.runOnIdle { style = family; dark = mode; checked = true; dependent = true; events = 0; dependentEvents = 0 }
                val master = compose.onNodeWithTag("master").performScrollTo()
                master.assertIsOn()
                // Theme changes and the preceding touch ripple must settle before taking a baseline.
                // Comparing only to an immediate screenshot can preserve a transient blended color.
                compose.mainClock.advanceTimeBy(1000)
                compose.waitForIdle()
                val onImage = awaitBackground(master, expectedBackground, "$family/$mode ON")
                val primaryPixel = onImage.getPixel(20, onImage.height / 2)
                capture("master-${family.name}-$mode-on")
                master.performClick().assertIsOff()
                compose.runOnIdle { assertEquals(1, events); assertTrue(dependent) }
                compose.mainClock.advanceTimeBy(1000)
                compose.waitForIdle()
                val offImage = awaitBackground(master, expectedBackground, "$family/$mode OFF")
                assertEquals("Master background must remain fixed", primaryPixel, offImage.getPixel(20, offImage.height / 2))
                compose.onNodeWithTag("dependent").assertIsNotEnabled().performTouchInput { click() }
                compose.onNodeWithText("Undo").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
                compose.runOnIdle { assertEquals(0, dependentEvents) }
                capture("master-${family.name}-$mode-off")
                master.performTouchInput { click(Offset(width - 48.dp.toPx(), height / 2f)) }.assertIsOn()
                compose.onNodeWithTag("dependent").assertIsEnabled().assertIsOn()
                compose.runOnIdle { assertEquals(2, events); assertEquals(0, dependentEvents) }
                compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertCountEquals(2)
            }
        }
    }

    @Test fun masterWrapsAtLargeFontInNarrowPane() {
        compose.setContent {
            val density = androidx.compose.ui.platform.LocalDensity.current
            CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides
                androidx.compose.ui.unit.Density(density.density, 2f)) {
                PyriteTheme(dynamicColor = false) {
                    Box(Modifier.width(280.dp)) {
                        SettingsMasterSwitch("Use auto-correction for all typed words", true, {},
                            Modifier.testTag("large-master"))
                    }
                }
            }
        }
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        compose.onNodeWithText("Use auto-correction for all typed words", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertFalse(results.single().hasVisualOverflow)
        saveScreenshot(InstrumentationRegistry.getInstrumentation().targetContext,
            "master-large-narrow", compose.onNodeWithTag("large-master").captureToImage().asAndroidBitmap())
    }

    private fun awaitBackground(master: SemanticsNodeInteraction, expected: Int, condition: String): android.graphics.Bitmap {
        var actual = 0
        var settled: android.graphics.Bitmap? = null
        val started = android.os.SystemClock.uptimeMillis()
        var stableSince = 0L
        try {
            // A first matching frame can precede a queued native ripple. Require wall-clock
            // stability and return that same image, rather than racing a second screenshot.
            compose.waitUntil(5000) {
                val image = master.captureToImage().asAndroidBitmap()
                actual = image.getPixel(20, image.height / 2)
                val now = android.os.SystemClock.uptimeMillis()
                if (actual != expected) stableSince = 0L
                else if (stableSince == 0L) stableSince = now
                settled = image
                actual == expected && now - started >= 500 && now - stableSince >= 150
            }
        } catch (timeout: ComposeTimeoutException) {
            throw AssertionError(condition + " background: expected " + expected.toUInt().toString(16) +
                ", actual " + actual.toUInt().toString(16), timeout)
        }
        return checkNotNull(settled)
    }

    private fun capture(name: String) = saveScreenshot(InstrumentationRegistry.getInstrumentation().targetContext,
        name, compose.onRoot().captureToImage().asAndroidBitmap())
}
