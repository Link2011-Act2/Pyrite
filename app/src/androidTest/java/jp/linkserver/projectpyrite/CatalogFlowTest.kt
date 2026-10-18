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

    @Test fun catalogThemePickersPreserveStateAndRenderFourThemePairs() {
        compose.onNodeWithText(text(R.string.notifications)).performScrollTo().performClick()
        val styles = listOf(
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

    @Test fun hierarchySharesStateAcrossNavigationThemeAndRecreation() {
        compose.onNodeWithContentDescription(text(R.string.components)).performClick()
        compose.onNodeWithText(text(R.string.catalog_hierarchy)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.autocorrection)).performClick()
        compose.onNodeWithText(text(R.string.undo_correction)).performClick().assertIsOn()
        compose.onNodeWithText(text(R.string.use_autocorrection)).performClick().assertIsOff()
        compose.onNodeWithText(text(R.string.undo_correction)).assertIsNotEnabled().assertIsOn()
        compose.onNodeWithText(compose.activity.getString(R.string.hierarchy_events, 1, 1)).assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(text(R.string.use_autocorrection)).assertIsOff()
        compose.onNodeWithContentDescription(text(R.string.design_style)).performClick()
        compose.onNode(hasText(text(R.string.nothing_style)) and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText(text(R.string.apply)).performClick()
        compose.onNodeWithText(text(R.string.undo_correction)).assertIsNotEnabled().assertIsOn()
        pressBack()
        compose.onNode(isToggleable()).assertIsOff().performClick().assertIsOn()
        compose.onNodeWithText(text(R.string.autocorrection)).performClick()
        compose.onNodeWithText(text(R.string.use_autocorrection)).assertIsOn()
        compose.onNodeWithText(text(R.string.undo_correction)).assertIsEnabled().assertIsOn()
    }

    @Test fun aboutDetailsRetainScrollAndLocalStateAcrossBackAndRecreation() {
        android.util.Log.i("PyriteNavigationTest", "details: opening About")
        compose.onNodeWithText(text(R.string.about)).performScrollTo().performClick()
        android.util.Log.i("PyriteNavigationTest", "details: setting About scroll")
        scrollBy(200f)
        compose.onNode(hasText(text(R.string.integration)) and hasClickAction()).assertIsDisplayed()
        val aboutScroll = scrollPosition()
        android.util.Log.i("PyriteNavigationTest", "details: opening details")
        compose.onNode(hasText(text(R.string.integration)) and hasClickAction()).performClick()
        compose.onNodeWithText(text(R.string.navigation_details_extra)).performClick().assertIsOn()
        scrollBy(10000f)
        compose.onNodeWithText("Build JDK").assertIsDisplayed()
        val detailScroll = scrollPosition()
        logScrollGeometry("before recreation")
        capture("navigation-details-before-recreation")
        android.util.Log.i("PyriteNavigationTest", "details: recreating")
        org.junit.Assert.assertTrue(detailScroll > 0f)
        compose.activityRule.scenario.recreate()
        logScrollGeometry("after recreation")
        capture("navigation-details-after-recreation")
        compose.onNodeWithText(text(R.string.navigation_details_extra)).assertIsOn()
        org.junit.Assert.assertEquals(
            minOf(detailScroll, scrollMaximum()), scrollPosition(), 1f)
        val retainedDetailScroll = scrollPosition()
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        org.junit.Assert.assertEquals(aboutScroll, scrollPosition(), 1f)
        android.util.Log.i("PyriteNavigationTest", "details: reopening details")
        compose.onNode(hasText(text(R.string.integration)) and hasClickAction()).assertIsDisplayed().performClick()
        org.junit.Assert.assertEquals(minOf(retainedDetailScroll, scrollMaximum()), scrollPosition(), 1f)
        scrollBy(-10000f)
        compose.onNodeWithText(text(R.string.navigation_details_extra)).assertIsOn()
        pressBack()
        pressBack()
        compose.onNodeWithText(text(R.string.catalog_intro)).performScrollTo().assertIsDisplayed()
    }

    @Test fun recreationDuringRapidForwardBackKeepsTheCurrentDestination() {
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithContentDescription(text(R.string.components)).performClick()
            compose.mainClock.advanceTimeBy(48)
            pressBack()
            compose.mainClock.advanceTimeBy(48)
            compose.onNodeWithContentDescription(text(R.string.components)).performClick()
            compose.mainClock.advanceTimeBy(48)
            compose.activityRule.scenario.recreate()
            compose.mainClock.advanceTimeBy(400)
            compose.onNodeWithText(text(R.string.catalog_categories)).assertIsDisplayed()
            compose.onAllNodesWithContentDescription(text(R.string.back)).assertCountEquals(1)
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    // Use bounded accessibility scrolling: the test needs an exact retained offset, not a fling.
    private fun scrollBy(delta: Float) {
        compose.onNode(hasScrollAction()).performSemanticsAction(
            androidx.compose.ui.semantics.SemanticsActions.ScrollBy) { it(0f, delta) }
        compose.waitForIdle()
    }

    private fun scrollMaximum() = compose.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange].maxValue()

    private fun logScrollGeometry(label: String) {
        val node = compose.onNode(hasScrollAction()).fetchSemanticsNode()
        android.util.Log.i("PyriteNavigationTest", label + ": scroll=" + scrollPosition() +
            ", max=" + scrollMaximum() + ", bounds=" + node.boundsInRoot +
            ", density=" + compose.activity.resources.displayMetrics.density +
            ", fontScale=" + compose.activity.resources.configuration.fontScale)
    }

    private fun scrollPosition() = compose.onNode(hasScrollAction()).fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange].value()

    private fun choose(title: Int, label: String) {
        compose.onNodeWithText(text(title)).performScrollTo().performClick()
        compose.onNode(hasText(label) and hasAnyAncestor(isDialog())).performClick()
        compose.onNodeWithText(text(R.string.apply)).performClick()
    }

    private fun capture(name: String) {
        saveScreenshot(compose.activity, name, compose.onRoot().captureToImage().asAndroidBitmap())
    }
}
