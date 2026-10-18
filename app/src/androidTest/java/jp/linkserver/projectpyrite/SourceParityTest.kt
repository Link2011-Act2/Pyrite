package jp.linkserver.projectpyrite

import android.graphics.Bitmap
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.projectpyrite.reference.*
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

/** Compares extracted source functions, not a hand-drawn expected image. See docs/source-port.md. */
class SourceParityTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sourceAboutAndSettingsMatchInBothStylesAndColorModes() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        var source by mutableStateOf(true)
        var specimen by mutableStateOf("about")
        compose.setContent {
            PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                Box(Modifier.width(360.dp).background(MaterialTheme.colorScheme.background).testTag("specimen")) {
                    val summary = if (specimen == "switch-empty") "" else "Keep this setting enabled"
                    when (specimen) {
                        "about" -> if (source) {
                            if (style == PyriteStyle.Nothing) GlyphAboutReference(
                                "Preview", "Preview", "0.1", "0.1", 1, "2026.10.18", true, {}, {})
                            else SchedulerAboutReference(
                                "Preview", "Preview", "0.1", "0.1", 1, "2026.10.18", {}, {})
                        } else AboutOverviewMosaic(
                            AboutInfo("Pyrite", if (style == PyriteStyle.Nothing)
                                "Preview v0.1\u00a0(1)" else "Preview  v0.1 (1)"),
                            AboutOverviewContent(AboutDetail("Channel", "Preview"),
                                AboutDetail("Build", "2026.10.18"),
                                AboutDetail("Version", "v0.1")),
                            icon = { ReferenceLogo() }, onVersionClick = {}, onChannelClick = {})
                        "switch", "switch-empty" -> if (source) {
                            if (style == PyriteStyle.Nothing) SettingsToggleEntry(
                                "Notifications", summary, true, {}, true, GlyphGroupPosition.Single)
                            else AppSettingsSwitchItem("Notifications", summary, true, {})
                        } else SettingsSwitchItem("Notifications", true, {}, summary = summary)
                        "navigation" -> if (source) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            SettingsEntry("Appearance", "Choose the design style", {}, true, GlyphGroupPosition.Top)
                            SettingsEntry("About", "Version and application details", {}, true, GlyphGroupPosition.Bottom)
                        } else SettingsGroup {
                            item("appearance") { SettingsNavigationItem("Appearance", {}, summary = "Choose the design style") }
                            item("about") { SettingsNavigationItem("About", {}, summary = "Version and application details") }
                        }
                    }
                }
            }
        }
        PyriteStyle.entries.forEach { family ->
            listOf(false, true).forEach { isDark ->
                listOf("about", "switch", "switch-empty").plus(if (family == PyriteStyle.Nothing) listOf("navigation") else emptyList())
                    .forEach { kind ->
                        compose.runOnIdle { style = family; dark = isDark; specimen = kind; source = true }
                        val original = bitmap()
                        compose.runOnIdle { source = false }
                        val actual = bitmap()
                        val name = "source-${family.name}-${if (isDark) "dark" else "light"}-$kind"
                        saveScreenshot(context(), "$name-reference", original)
                        saveScreenshot(context(), "$name-pyrite", actual)
                        assertEquals("$name width", original.width, actual.width)
                        assertEquals("$name height", original.height, actual.height)
                        val heatmap = Bitmap.createBitmap(actual.width, actual.height, Bitmap.Config.ARGB_8888)
                        var changed = 0
                        for (y in 0 until actual.height) for (x in 0 until actual.width) {
                            val a = original.getPixel(x, y); val b = actual.getPixel(x, y)
                            val delta = maxOf(abs(((a shr 16) and 255) - ((b shr 16) and 255)),
                                abs(((a shr 8) and 255) - ((b shr 8) and 255)), abs((a and 255) - (b and 255)))
                            if (delta > 3) changed++
                            heatmap.setPixel(x, y, if (delta > 3) android.graphics.Color.MAGENTA else b)
                        }
                        saveScreenshot(context(), "$name-diff", heatmap)
                        val ratio = changed.toDouble() / (actual.width * actual.height)
                        InstrumentationRegistry.getInstrumentation().sendStatus(0,
                            Bundle().apply { putString("stream", "\nPYRITE_COMPARE $name ${actual.width}x${actual.height} changed=$changed ratio=$ratio\n") })
                        assertTrue("$name changed pixel ratio $ratio (see saved diff)", ratio < .001)
                    }
            }
        }
    }

    @Test fun splitNavigationAndSwitchHaveIndependentTargetsAndEnableConditions() {
        var checked by mutableStateOf(false)
        var navigationEnabled by mutableStateOf(true)
        var switchEnabled by mutableStateOf(false)
        var navigations = 0
        var toggles = 0
        compose.setContent {
            PyriteTheme {
                SettingsSplitSwitchItem("Details", checked, { checked = it; toggles++ },
                    { navigations++ }, navigationEnabled = navigationEnabled, switchEnabled = switchEnabled)
            }
        }
        compose.onNodeWithText("Details").performClick()
        compose.onNode(isToggleable()).assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, navigations); assertEquals(0, toggles)
            navigationEnabled = false; switchEnabled = true }
        compose.onNodeWithText("Details").assertIsNotEnabled()
        compose.onNode(isToggleable()).performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, navigations); assertEquals(1, toggles) }
    }

    @Test fun optionalMetadataAndUpdateStateRemainHostOwned() {
        var visible by mutableStateOf(true)
        var checking by mutableStateOf(false)
        var events = 0
        compose.setContent {
            PyriteTheme {
                SettingsScreen("About") {
                    AboutOverviewMosaic(AboutInfo("Pyrite", "Preview"),
                        AboutOverviewContent(channel = if (visible) AboutDetail("Channel", "Stable") else null,
                            attribution = if (visible) AboutAttribution("Forked from", "Upstream", "https://example.org") else null),
                        icon = { ReferenceLogo() })
                    AboutUpdateCard("Update information", "Idle", "Check", { events++ }, checking = checking)
                }
            }
        }
        compose.onNodeWithText("Stable").assertHasNoClickAction()
        compose.onNodeWithText("Check").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, events); visible = false; checking = true }
        compose.onNodeWithText("Stable").assertDoesNotExist()
        compose.onNodeWithText("Forked from Upstream").assertDoesNotExist()
        compose.onNodeWithText("Check").performScrollTo().assertIsNotEnabled()
    }

    @Test fun compoundRadioKeepsSelectionAndRejectsDisabledChoices() {
        var selected by mutableStateOf(0)
        var enabled by mutableStateOf(true)
        var events = 0
        compose.setContent {
            PyriteTheme {
                SettingsRadioGroupItem("Mode", listOf(
                    jp.linkserver.pyrite.dialog.SettingsChoice("one", 0, "One"),
                    jp.linkserver.pyrite.dialog.SettingsChoice("two", 1, "Two"),
                    jp.linkserver.pyrite.dialog.SettingsChoice("disabled", 2, "Unavailable", enabled = false)
                ), selected, { selected = it; events++ }, enabled = enabled)
            }
        }
        compose.onNodeWithText("Two").performClick().assertIsSelected()
        compose.onNodeWithText("Unavailable").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, events); enabled = false }
        compose.onNodeWithText("One").assertIsNotEnabled().performTouchInput { click() }
        compose.onNodeWithText("Two").assertIsSelected()
        compose.runOnIdle { assertEquals(1, events); enabled = true }
        compose.onNodeWithText("One").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(2, events) }
    }

    @Test fun inlineAttributionDoesNotTriggerTheVersionAction() {
        var attributionEvents = 0
        var versionEvents = 0
        compose.setContent {
            PyriteTheme {
                Box(Modifier.width(360.dp)) {
                    AboutOverviewMosaic(AboutInfo("Pyrite", "Release 1"),
                        AboutOverviewContent(attribution = AboutAttribution("Forked from", "Upstream", "https://example.org")),
                        icon = { ReferenceLogo() }, onVersionClick = { versionEvents++ },
                        onAttributionClick = { attributionEvents++ })
                }
            }
        }
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        val text = compose.onNodeWithText("Forked from Upstream", useUnmergedTree = true)
        text.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(results) }
        val linkPosition = results.single().getBoundingBox("Forked from ".length + 2).center
        text.performTouchInput { click(linkPosition) }
        compose.runOnIdle { assertEquals(1, attributionEvents); assertEquals(0, versionEvents) }
        compose.onNodeWithText("Release 1").performClick()
        compose.runOnIdle { assertEquals(1, attributionEvents); assertEquals(1, versionEvents) }
    }

    @Test fun aboutScreenShowsExplicitMetadataLinksAndSupplementaryContent() {
        var links = 0
        compose.setContent {
            PyriteTheme {
                AboutScreen(AboutInfo("Pyrite", "Preview"),
                    AboutOverviewContent(build = AboutDetail("Build", "1")),
                    links = listOf(AboutLink("upstream", "Repository", "https://example.org")),
                    onLinkClick = { assertEquals("https://example.org", it.url); links++ }) {
                    jp.linkserver.pyrite.card.SettingsInfoCard("Additional information", "Host supplied content")
                }
            }
        }
        compose.onNodeWithText("1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Host supplied content").performScrollTo().assertIsDisplayed()
        compose.onNode(hasText("Repository") and hasClickAction()).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, links) }
    }

    private fun context() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun bitmap() = compose.onNodeWithTag("specimen").captureToImage().asAndroidBitmap()
}
