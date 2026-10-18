package jp.linkserver.projectpyrite

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun text(id: Int) = compose.activity.getString(id)

    @Test fun settingsAboutComponentsAndRecreationUseTheLibrary() {
        compose.onNodeWithText(text(R.string.notifications)).performScrollTo().performClick()
            .assertIsOff()
        compose.onNodeWithText(text(R.string.about)).performScrollTo().performClick()
        compose.onNodeWithText("0.1.0-SNAPSHOT").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("0.1.0-SNAPSHOT").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText(text(R.string.notifications)).performScrollTo().assertIsOff()
        compose.onNodeWithContentDescription(text(R.string.components)).performClick()
        compose.onNodeWithText(text(R.string.catalog_official)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.buttons)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(R.string.expand)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.expansion_body)).assertExists()
        pressBack()
        pressBack()
        compose.onNodeWithText(text(R.string.design_style)).performScrollTo().assertIsDisplayed()
    }

    @Test fun catalogThemePickersPreserveStateAndRenderSixThemePairs() {
        compose.onNodeWithText(text(R.string.notifications)).performScrollTo().performClick()
        val styles = listOf(
            "Material 3" to "material",
            "Material 3 Expressive" to "expressive",
            text(R.string.nothing_style) to "nothing"
        )
        styles.forEach { (label, name) ->
            choose(R.string.design_style, label)
            listOf(R.string.light_mode to "light", R.string.dark_mode to "dark").forEach { (mode, modeName) ->
                choose(R.string.color_mode, text(mode))
                compose.onNodeWithText(text(R.string.notifications)).performScrollTo().assertIsOff()
                compose.onNodeWithText(text(R.string.catalog_intro)).performScrollTo()
                capture("catalog-$name-$modeName-settings")
                compose.onNodeWithText(text(R.string.about)).performScrollTo().performClick()
                compose.onNodeWithText("0.1.0-SNAPSHOT").assertIsDisplayed()
                capture("catalog-$name-$modeName-about")
                compose.onNodeWithContentDescription(text(R.string.back)).performClick()
            }
        }
    }

    @Test fun categoryStateSurvivesNavigationThemeAndActivityRecreation() {
        compose.onNodeWithContentDescription(text(R.string.components)).performClick()
        compose.onNodeWithText(text(R.string.catalog_cards)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.check_now)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.loading_preview)).performScrollTo().performClick()
        compose.onNode(hasText(text(R.string.checking)) and hasClickAction()).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithContentDescription(text(R.string.design_style)).performClick()
        compose.onNode(hasText(text(R.string.nothing_style)) and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText(text(R.string.apply)).performClick()
        compose.onNodeWithText(text(R.string.loading_preview)).performScrollTo().assertIsOn()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(text(R.string.loading_preview)).performScrollTo().assertIsOn()
        compose.onNodeWithText(text(R.string.loading_preview)).performClick()
        compose.onNodeWithText(text(R.string.check_now)).performScrollTo().assertIsEnabled()
        compose.onNodeWithText(compose.activity.resources.getQuantityString(R.plurals.counter_value, 1, 1))
            .assertExists()
        pressBack()
        compose.onNodeWithText(text(R.string.catalog_rows)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.rich_switch)).performScrollTo().performClick().assertIsOn()
        pressBack()
        compose.onNodeWithText(text(R.string.catalog_cards)).performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.resources.getQuantityString(R.plurals.counter_value, 1, 1))
            .performScrollTo().assertIsDisplayed()
        pressBack()
        compose.onNodeWithText(text(R.string.catalog_rows)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.rich_switch)).performScrollTo().assertIsOn()
    }

    @Test fun confirmationAndConditionalScreenKeepDraftAndCommittedValuesSeparate() {
        compose.onNodeWithContentDescription(text(R.string.components)).performClick()
        compose.onNodeWithText(text(R.string.catalog_dialogs)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.open_confirmation)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.confirm)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.acknowledged)).performClick()
        compose.onNodeWithText(text(R.string.cancel)).performClick()
        compose.onNodeWithText(compose.activity.resources.getQuantityString(R.plurals.confirmation_count, 0, 0)).assertExists()
        compose.onNodeWithText(text(R.string.open_confirmation)).performClick()
        compose.onNodeWithText(text(R.string.acknowledged)).performClick()
        compose.onNodeWithText(text(R.string.confirm)).performClick()
        compose.onNodeWithText(compose.activity.resources.getQuantityString(R.plurals.confirmation_count, 1, 1)).assertExists()
        pressBack()
        compose.onNodeWithText(text(R.string.catalog_screens)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.show_advanced)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.guarded_change)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.cancel)).performClick()
        compose.onNodeWithText(text(R.string.guarded_change)).assertIsOff().performClick()
        compose.onNodeWithText(text(R.string.confirm)).performClick()
        compose.onNodeWithText(text(R.string.guarded_change)).assertIsOn()
        compose.onNodeWithText(text(R.string.guarded_change)).performClick().assertIsOff()
    }

    private fun choose(title: Int, label: String) {
        compose.onNodeWithText(text(title)).performScrollTo().performClick()
        compose.onNode(hasText(label) and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText(text(R.string.apply)).performClick()
    }

    private fun capture(name: String) {
        saveScreenshot(compose.activity, name, compose.onRoot().captureToImage().asAndroidBitmap())
    }
}
