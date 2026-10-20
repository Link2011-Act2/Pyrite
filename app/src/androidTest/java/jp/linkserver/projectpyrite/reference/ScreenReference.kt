// Extracted from ShutUp ShutterApp.kt / Type.kt and Glyph SettingsScreen.kt SettingsHeader.
// Copyright (c) 2026 Azul1nk. MIT. App-specific routes/resources/fonts are substituted in tests.
package jp.linkserver.projectpyrite.reference

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SourceScreenScaffoldReference(
    nothing: Boolean, title: String, modifier: Modifier, onBack: () -> Unit
) {
    Scaffold(modifier, containerColor = MaterialTheme.colorScheme.surface, topBar = {
        TopAppBar(
            title = {
                if (nothing) Text(title, modifier = Modifier.padding(bottom = 2.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    fontWeight = FontWeight.Normal, fontFamily = FontFamily.SansSerif)
                else Text(title, style = TextStyle(
                    fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
                    fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = 0.sp))
            },
            navigationIcon = {
                IconButton(onBack) {
                    // The original XML's 24dp path matches Google's mirrored ArrowBack.
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface)
        )
    }) { padding -> Text("Body", Modifier.padding(padding)) }
}

@Composable
internal fun ShutterTransitionReference(screen: Int, content: @Composable (Int) -> Unit) {
    val holder = rememberSaveableStateHolder()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.fillMaxSize().clipToBounds(),
            transitionSpec = {
                // Test integer order replaces only ShutUp's app-specific screenDepth mapping.
                val direction = if (targetState > initialState) 1 else -1
                ((slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { direction * it / 5 } +
                    fadeIn(tween(220, delayMillis = 40))) togetherWith
                    (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -direction * it / 5 } +
                        fadeOut(tween(140)))).using(null)
            },
            label = "screen_transition"
        ) { targetScreen ->
            holder.SaveableStateProvider(targetScreen) { content(targetScreen) }
        }
    }
}

/** Exact transitionSpec/layout from Glyph 53e568c; Int ordering replaces its Screen.ordinal only. */
@Composable
internal fun GlyphTransitionReference(screen: Int, content: @Composable (Int) -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(targetState = screen, transitionSpec = {
                val forward = targetState > initialState
                if (forward) {
                    (slideInHorizontally { it / 5 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 5 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 5 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 5 } + fadeOut())
                }
            }, label = "screen_transition") { content(it) }
        }
    }
}
