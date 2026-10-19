package jp.linkserver.projectpyrite

import android.os.Bundle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.card.SettingsInfoCard
import jp.linkserver.pyrite.screen.PyriteBackButton
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.PyriteStyle
import jp.linkserver.pyrite.theme.PyriteTheme
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Production colors: captures the approved specimen, plus both styles and light/dark controls. */
class M3SurfaceColorsTest {
    @get:Rule val compose = createComposeRule()

    private data class ThemeCase(val style: PyriteStyle, val dark: Boolean, val dynamic: Boolean) {
        val id = (if (style == PyriteStyle.Nothing) "nothing" else "expressive") +
            (if (dark) "-dark" else "-light") + (if (dynamic) "-dynamic" else "-static")
    }

    @Test fun productionSurfacesKeepStateGeometryAndTextContrast() {
        val phase = InstrumentationRegistry.getArguments().getString("palettePhase", "applied")
        var theme by mutableStateOf(ThemeCase(PyriteStyle.Material3Expressive, false, false))
        var about by mutableStateOf(false)
        var checked by mutableStateOf(true)
        var events = 0
        var colors: ColorScheme? = null
        compose.setContent {
            PyriteTheme(theme.style, darkTheme = theme.dark, dynamicColor = theme.dynamic) {
                val current = MaterialTheme.colorScheme
                SideEffect { colors = current }
                Specimen(about, checked, { checked = it; events++ })
            }
        }
        val geometry = mutableMapOf<Boolean, androidx.compose.ui.geometry.Rect>()
        for (style in PyriteStyle.entries) for (dark in listOf(false, true)) for (dynamic in listOf(false, true)) {
            val currentCase = ThemeCase(style, dark, dynamic)
            compose.runOnIdle { theme = currentCase; about = false }
            compose.onNodeWithTag("palette-toggle").assertIsOn()
            val row = compose.onNodeWithTag("palette-toggle").captureToImage().asAndroidBitmap()
            // The middle of the top edge is inside the shape and above text/controls.
            val actualCard = Color(row.getPixel(row.width / 2, 5))
            compose.runOnIdle {
                val scheme = checkNotNull(colors)
                val expectedCard = if (phase != "before" && style == PyriteStyle.Material3Expressive && !dark)
                    scheme.surfaceContainerLowest else scheme.surfaceContainerHigh
                assertEquals("Actual card fill", expectedCard.toArgb(), actualCard.toArgb())
                assertTrue(contrast(scheme.onSurface, actualCard) >= 4.5)
                assertTrue(contrast(scheme.onSurfaceVariant, actualCard) >= 4.5)
                emitMetrics(phase, currentCase.id, scheme, actualCard)
            }
            for (isAbout in listOf(false, true)) {
                compose.runOnIdle { about = isAbout }
                val bounds = compose.onRoot().fetchSemanticsNode().boundsInRoot
                geometry[isAbout]?.let { assertEquals("Viewport must not change", it, bounds) }
                    ?: run { geometry[isAbout] = bounds }
                val name = "palette-b-" + phase + "-" + currentCase.id + "-" + (if (isAbout) "about" else "settings")
                saveScreenshot(InstrumentationRegistry.getInstrumentation().targetContext,
                    name, compose.onRoot().captureToImage().asAndroidBitmap())
            }
        }
        compose.runOnIdle { about = false }
        compose.onNodeWithTag("palette-toggle").performClick().assertIsOff()
        compose.runOnIdle { assertEquals("One callback per toggle", 1, events) }
    }

    @Composable private fun Specimen(about: Boolean, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        if (about) AboutScreen(
            info = AboutInfo("Pyrite", "0.1.0-SNAPSHOT"), title = "アプリについて",
            overview = AboutOverviewContent(
                channel = AboutDetail("Channel", "Preview"), build = AboutDetail("Build", "2026.10.19"),
                version = AboutDetail("Version", "v0.1")
            ), icon = { PyriteMark() }, navigationIcon = { PyriteBackButton({}, "戻る") },
            links = emptyList(), onLinkClick = {}
        ) {
            AboutUpdateCard("アプリを最新の状態に保ちます。", "最新バージョンです", "更新を確認", {})
        } else SettingsScreen("設定", navigationIcon = { PyriteBackButton({}, "戻る") }) {
            SettingsGroup(title = "通知") {
                item("notifications") {
                    SettingsSwitchItem("通知を許可", checked, onCheckedChange,
                        modifier = Modifier.testTag("palette-toggle"), summary = "予定やお知らせを受け取ります")
                }
                item("sound") { SettingsSwitchItem("通知音", false, {}, summary = "音を鳴らしてお知らせします") }
                item("details") { SettingsNavigationItem("詳細設定", {}, summary = "通知が無効な間は変更できません", enabled = false) }
                item("radio") { SettingsRadioItem("システム設定に従う", true, {}) }
            }
            SettingsInfoCard("設定について", "設定した内容は、次にアプリを開いたときも保持されます。")
        }
    }

    private fun contrast(a: Color, b: Color): Double =
        (maxOf(a.luminance(), b.luminance()).toDouble() + .05) /
            (minOf(a.luminance(), b.luminance()).toDouble() + .05)

    private fun hex(color: Color) = "#%06X".format(color.toArgb() and 0xFFFFFF)

    private fun emitMetrics(phase: String, id: String, colors: ColorScheme, card: Color) {
        val json = JSONObject().put("phase", phase).put("theme", id)
            .put("settingsBackground", hex(colors.surface)).put("aboutBackground", hex(colors.background))
            .put("card", hex(card)).put("primary", hex(colors.primary))
            .put("high", hex(colors.surfaceContainerHigh)).put("highest", hex(colors.surfaceContainerHighest))
            .put("bodyContrast", contrast(colors.onSurface, card))
            .put("summaryContrast", contrast(colors.onSurfaceVariant, card))
        InstrumentationRegistry.getInstrumentation().sendStatus(0,
            Bundle().apply { putString("stream", "\nPYRITE_SURFACE " + json + "\n") })
    }
}
