package jp.linkserver.projectpyrite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import jp.linkserver.projectpyrite.reference.*
import jp.linkserver.pyrite.screen.*
import jp.linkserver.pyrite.theme.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NavigationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sourceHeadersMatchBothStylesLightDarkAndRtl() {
        var style by mutableStateOf(PyriteStyle.Material3Expressive)
        var dark by mutableStateOf(false)
        var rtl by mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides
                if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                PyriteTheme(style, darkTheme = dark, dynamicColor = false) {
                    Column {
                        SourceScreenScaffoldReference(style == PyriteStyle.Nothing, "Settings",
                            Modifier.size(360.dp, 220.dp).testTag("reference"), {})
                        PyriteScreenScaffold("Settings", Modifier.size(360.dp, 220.dp).testTag("actual"),
                            navigationIcon = { PyriteBackButton({ clicks++ }, "Back") }) { padding ->
                            Text("Body", Modifier.padding(padding))
                        }
                    }
                }
            }
        }
        for (family in PyriteStyle.entries) for (isDark in listOf(false, true)) for (isRtl in listOf(false, true)) {
            compose.runOnIdle { style = family; dark = isDark; rtl = isRtl }
            val name = "header-" + family.name + "-" + isDark + "-" + isRtl
            assertFramesEqual(name)
            compose.onAllNodes(hasContentDescription("Back") and hasAnyAncestor(hasTestTag("actual")))
                .assertCountEquals(1)
        }
        compose.onNode(hasContentDescription("Back") and hasAnyAncestor(hasTestTag("actual"))).performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun transitionMatchesShutUpFramesTimingDirectionAndClipping() {
        var screen by mutableStateOf(0)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            PyriteTheme(dynamicColor = false) {
                Column {
                    Frame("reference") {
                        ShutterTransitionReference(screen) { page -> Specimen("ref", page) }
                    }
                    Frame("actual") {
                        PyriteScreenTransition(screen, ::direction,
                            modifier = Modifier.testTag("viewport")) { page -> Specimen("actual", page) }
                    }
                }
            }
        }
        for (destination in listOf(1, 0)) {
            compose.runOnIdle { screen = destination }
            compose.mainClock.advanceTimeByFrame()
            var elapsed = 0L
            for (time in listOf(0L, 32L, 48L, 96L, 144L, 224L, 272L, 304L, 336L)) {
                if (time > elapsed) compose.mainClock.advanceTimeBy(time - elapsed, ignoreFrameDuration = true)
                elapsed = time
                assertFramesEqual("motion-" + destination + "-" + time)
                val bitmap = compose.onNodeWithTag("actual").captureToImage().asAndroidBitmap()
                assertEquals("Content must not paint across the viewport boundary",
                    android.graphics.Color.MAGENTA, bitmap.getPixel(5, bitmap.height / 2))
                val viewport = compose.onNodeWithTag("viewport").getUnclippedBoundsInRoot()
                val incoming = compose.onNodeWithTag("actual-screen-" + destination).getUnclippedBoundsInRoot()
                val offset = incoming.left - viewport.left
                if (time == 0L) {
                    val sign = if (destination == 1) 1 else -1
                    assertEquals("Entry starts at one fifth of the screen width",
                        (viewport.right - viewport.left).value * sign / 5f, offset.value, 1f)
                }
                if (time >= 304L) assertEquals("Slide has finished after 300ms", 0f, offset.value, .1f)
            }
            compose.onNodeWithTag("actual-screen-" + (1 - destination)).assertDoesNotExist()
        }
    }

    @Test fun interruptedTransitionsKeepPerScreenScrollAndSaveableValues() {
        var screen by mutableStateOf(0)
        compose.setContent {
            PyriteTheme(dynamicColor = false) {
                PyriteScreenTransition(screen, ::direction) { page ->
                    val scroll = rememberScrollState()
                    var count by rememberSaveable { mutableIntStateOf(0) }
                    Column(Modifier.fillMaxSize().verticalScroll(scroll).testTag("scroll-" + page)) {
                        Button({ count++ }) { Text("Count " + page + ": " + count) }
                        repeat(25) { Text("Row " + page + "/" + it, Modifier.height(48.dp)) }
                    }
                }
            }
        }
        compose.onNodeWithText("Count 0: 0").performClick()
        compose.onNodeWithText("Row 0/24").performScrollTo()
        val savedScroll = scrollValue("scroll-0")
        assertTrue(savedScroll > 0f)
        compose.mainClock.autoAdvance = false
        for (destination in listOf(1, 2, 0, 1, 0)) {
            compose.runOnIdle { screen = destination }
            compose.mainClock.advanceTimeBy(48)
        }
        compose.mainClock.advanceTimeBy(400)
        compose.mainClock.autoAdvance = true
        compose.onAllNodesWithTag("scroll-0").assertCountEquals(1)
        assertEquals(savedScroll, scrollValue("scroll-0"), .1f)
        compose.onNodeWithText("Count 0: 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Count 0: 1").performClick()
        compose.onNodeWithText("Count 0: 2").assertIsDisplayed()
    }

    private fun direction(initial: Int, target: Int) =
        if (target > initial) PyriteNavigationDirection.Forward else PyriteNavigationDirection.Backward

    @Composable private fun Frame(tag: String, content: @Composable () -> Unit) {
        Box(Modifier.size(360.dp, 200.dp).background(Color.Magenta).testTag(tag)
            .padding(horizontal = 30.dp, vertical = 10.dp)) { content() }
    }

    @Composable private fun Specimen(prefix: String, page: Int) {
        Box(Modifier.fillMaxSize().background(if (page == 0) Color(0xFFCC3311) else Color(0xFF1166CC))
            .testTag(prefix + "-screen-" + page))
    }

    private fun scrollValue(tag: String) = compose.onNodeWithTag(tag).fetchSemanticsNode()
        .config[SemanticsProperties.VerticalScrollAxisRange].value()

    private fun assertFramesEqual(name: String) {
        val original = compose.onNodeWithTag("reference").captureToImage().asAndroidBitmap()
        val actual = compose.onNodeWithTag("actual").captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        saveScreenshot(context, name + "-reference", original)
        saveScreenshot(context, name + "-pyrite", actual)
        assertEquals(name + " width", original.width, actual.width)
        assertEquals(name + " height", original.height, actual.height)
        assertTrue(name + ": source pixels must match", original.sameAs(actual))
    }
}
