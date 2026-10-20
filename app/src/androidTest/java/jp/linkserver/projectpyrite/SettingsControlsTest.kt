package jp.linkserver.projectpyrite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SettingsControlsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nestedControlsHaveIndependentTargetsAndSingleCallbacks() {
        var style by mutableStateOf(PyriteStyle.Nothing)
        var dark by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        var visible by mutableStateOf(true)
        var value by mutableFloatStateOf(.5f)
        var changes = 0; var commits = 0; var buttons = 0; var first = 0; var second = 0
        compose.setContent {
            PyriteTheme(style = style, darkTheme = dark, dynamicColor = false) {
                SettingsGroup {
                    if (visible) item("slider") {
                        SettingsSliderItem("Parameter", value.toString(), value, { value = it; changes++ },
                            0f..1f, modifier = Modifier.testTag("parameter-card"), enabled = enabled,
                            onValueChangeFinished = { commits++ })
                    }
                    item("button") { SettingsButtonItem("Action", "Apply", { buttons++ }, enabled = enabled,
                        modifier = Modifier.testTag("button-card")) }
                    item("custom") { SettingsContentCard(Modifier.testTag("custom-card")) {
                        Row { TextButton({ first++ }, enabled = enabled) { Text("First") }
                            TextButton({ second++ }, enabled = enabled) { Text("Second") } }
                    } }
                }
            }
        }
        for (family in PyriteStyle.entries) for (night in listOf(false, true)) {
            compose.runOnIdle { style = family; dark = night; enabled = true; visible = true; changes = 0; commits = 0; value = .5f }
            compose.onNodeWithTag("parameter-card").assert(hasClickAction().not())
            compose.onNodeWithTag("button-card").assert(hasClickAction().not())
            compose.onNodeWithTag("custom-card").assert(hasClickAction().not())
            compose.onNodeWithContentDescription("Parameter")
                .performSemanticsAction(SemanticsActions.SetProgress) { assertTrue(it(.8f)) }
            compose.runOnIdle { assertEquals(.8f, value, .001f); assertEquals(1, changes); assertEquals(1, commits) }
            val before = buttons
            compose.onNodeWithText("Apply").performClick()
            compose.runOnIdle { assertEquals(before + 1, buttons); assertEquals(1, changes) }
            compose.onNodeWithText("First").performClick()
            compose.onNodeWithText("Second").performClick()
            compose.runOnIdle { assertEquals(first, second); enabled = false }
            compose.onNodeWithContentDescription("Parameter").assertIsNotEnabled()
            compose.onNodeWithText("Apply").assertIsNotEnabled().performTouchInput { click() }
            compose.onNodeWithContentDescription("Parameter").performTouchInput { click() }
            compose.runOnIdle { assertEquals(before + 1, buttons); assertEquals(1, changes); visible = false }
            compose.onNodeWithTag("parameter-card").assertDoesNotExist()
            compose.runOnIdle { enabled = true }
            compose.onNodeWithText("Apply").performClick()
            compose.runOnIdle { assertEquals(before + 2, buttons); visible = true }
        }
    }

    @Test fun sliderDragFinishesOnceAndCardPaddingDoesNotChangeValue() {
        var value by mutableFloatStateOf(.5f)
        var commits = 0
        compose.setContent { PyriteTheme {
            SettingsSliderItem("Drag", "Value", value, { value = it }, 0f..1f,
                modifier = Modifier.testTag("card"), onValueChangeFinished = { commits++ })
        } }
        compose.onNodeWithText("Value").performTouchInput { click() }
        compose.runOnIdle { assertEquals(.5f, value, .001f); assertEquals(0, commits) }
        compose.onNodeWithContentDescription("Drag").performTouchInput {
            down(center); moveTo(androidx.compose.ui.geometry.Offset(width * .8f, center.y), 200); up()
        }
        compose.runOnIdle { assertTrue(value > .5f); assertEquals(1, commits) }
    }
}
