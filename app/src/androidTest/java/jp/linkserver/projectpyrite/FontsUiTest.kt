package jp.linkserver.projectpyrite

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.screen.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FontsUiTest {
    @get:Rule val compose = createComposeRule()

    private fun style(text: String): TextStyle {
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(text, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        return layouts.single().layoutInput.style
    }

    @Test fun catalogCanToggleTitleFontsWithoutChangingOrdinaryText() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val title = context.getString(R.string.catalog_title)
        val control = context.getString(R.string.custom_fonts)
        compose.setContent { PyriteCatalog() }
        val titleBefore = style(title)
        compose.onNodeWithText(control).performScrollTo()
        val bodyBefore = style(control)
        compose.onNodeWithText(control).performClick().assertIsOn()
        assertEquals(FontFamily.Serif, style(title).fontFamily)
        assertEquals(bodyBefore, style(control))
        compose.onNodeWithText(control).performClick().assertIsOff()
        assertEquals(titleBefore, style(title))
    }

    @Test fun optionalRolesPreserveDefaultsAndOnlyReplaceTheirOwnFamilies() {
        var family by mutableStateOf(PyriteStyle.Nothing)
        var fonts by mutableStateOf<PyriteFonts?>(null)
        var about by mutableStateOf(false)
        compose.setContent {
            val content: @Composable () -> Unit = {
                if (about) AboutScreen(AboutInfo("Identity", "Release"),
                    AboutOverviewContent(channel = AboutDetail("Channel", "Preview"), version = AboutDetail("Version", "v0.1")),
                    title = "About title", links = emptyList(), onLinkClick = {})
                else SettingsScreen("Screen title") {
                    SettingsGroup(title = "Section") {
                        item("setting") { SettingsSwitchItem("Ordinary setting", true, {}, summary = "Description") }
                    }
                }
            }
            // Exercise both the original positional signature and the new named-font overload.
            if (fonts == null) PyriteTheme(family, false, false, content)
            else PyriteTheme(fonts = fonts!!, style = family, darkTheme = false, dynamicColor = false, content = content)
        }
        for (visualStyle in PyriteStyle.entries) {
            compose.runOnIdle { family = visualStyle; fonts = null; about = false }
            val defaults = listOf("Screen title", "Section", "Ordinary setting", "Description").associateWith(::style)
            val before = compose.onRoot().captureToImage().asAndroidBitmap()
            val pixels = IntArray(before.width * before.height).also { before.getPixels(it, 0, before.width, 0, 0, before.width, before.height) }
            compose.runOnIdle { fonts = PyriteFonts() }
            val after = compose.onRoot().captureToImage().asAndroidBitmap()
            val actual = IntArray(after.width * after.height).also { after.getPixels(it, 0, after.width, 0, 0, after.width, after.height) }
            assertArrayEquals("Empty fonts preserve pixels: $visualStyle", pixels, actual)
            before.recycle(); after.recycle()
            compose.runOnIdle { fonts = null; about = true }
            val aboutDefaults = listOf("About title", "Identity", "Preview", "v0.1", "Release").associateWith(::style)
            val cases = listOf(
                PyriteFonts(title = FontFamily.Serif),
                PyriteFonts(accent = FontFamily.Cursive),
                PyriteFonts(body = FontFamily.Serif),
                PyriteFonts(FontFamily.Serif, FontFamily.Monospace, FontFamily.Cursive),
                PyriteFonts(title = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)))
            )
            for ((index, custom) in cases.withIndex()) {
                compose.runOnIdle { fonts = custom; about = false }
                for ((label, original) in defaults) {
                    val rendered = style(label)
                    val expected = if (label == "Screen title") custom.title else custom.body
                    assertEquals("$visualStyle/$label font", expected ?: original.fontFamily, rendered.fontFamily)
                    assertEquals("$label size", original.fontSize, rendered.fontSize)
                    assertEquals("$label weight", original.fontWeight, rendered.fontWeight)
                    assertEquals("$label line height", original.lineHeight, rendered.lineHeight)
                }
                compose.runOnIdle { about = true }
                for ((label, original) in aboutDefaults) {
                    val expected = when(label) {
                        "About title", "Identity", "Preview" -> custom.title
                        "v0.1" -> custom.accent
                        else -> custom.body
                    }
                    val rendered = style(label)
                    assertEquals("$visualStyle/$label role", expected ?: original.fontFamily ?: FontFamily.Default,
                        rendered.fontFamily ?: FontFamily.Default)
                    assertEquals(original.fontSize, rendered.fontSize)
                    assertEquals(original.fontWeight, rendered.fontWeight)
                    assertEquals(original.lineHeight, rendered.lineHeight)
                }
                saveScreenshot(InstrumentationRegistry.getInstrumentation().targetContext,
                    "fonts-${visualStyle.name}-$index-about", compose.onRoot().captureToImage().asAndroidBitmap())
            }
        }
    }
}
