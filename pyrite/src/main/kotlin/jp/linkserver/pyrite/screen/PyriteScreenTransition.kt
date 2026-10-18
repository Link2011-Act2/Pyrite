// Ported from ShutUp ShutterApp.kt screen_transition.
// Copyright (c) 2026 Azul1nk. MIT; see NOTICE.md.
package jp.linkserver.pyrite.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds

/** Physical horizontal direction, matching ShutUp: forward enters from the right, backward from left. */
enum class PyriteNavigationDirection { Forward, Backward }

/**
 * A single, clipped transition host with ShutUp's timing and per-screen saveable state.
 *
 * [direction] compares the actual initial/target states, including interrupted transitions.
 * Navigation, back handling and hierarchy belong to the caller. Do not nest screen transition hosts.
 * [contentKey] must be unique per retained screen and supported by the Android saved-state Bundle.
 * The same key is used for AnimatedContent and SaveableStateProvider to avoid duplicate providers.
 * Hoist [stateHolder] if the caller needs to remove saved state for screens it permanently discards.
 */
@Composable
fun <T : Any> PyriteScreenTransition(
    targetState: T,
    direction: (initial: T, target: T) -> PyriteNavigationDirection,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any = { it },
    stateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
    content: @Composable (T) -> Unit
) {
    // Keep the official inset listener alive before screen/Scaffold subcomposition measures
    // restored scroll containers, and across outgoing/incoming screen disposal.
    val scaffoldInsets = ScaffoldDefaults.contentWindowInsets
    CompositionLocalProvider(LocalPyriteScaffoldInsets provides scaffoldInsets) {
        Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            AnimatedContent(
                targetState = targetState,
                modifier = Modifier.fillMaxSize().clipToBounds(),
                transitionSpec = {
                    val sign = if (direction(initialState, targetState) == PyriteNavigationDirection.Forward) 1 else -1
                    ((slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { sign * it / 5 } +
                        fadeIn(tween(220, delayMillis = 40))) togetherWith
                        (slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -sign * it / 5 } +
                            fadeOut(tween(140)))).using(null)
                },
                contentKey = contentKey,
                label = "screen_transition"
            ) { screen ->
                stateHolder.SaveableStateProvider(contentKey(screen)) { content(screen) }
            }
        }
    }
}

internal val LocalPyriteScaffoldInsets = staticCompositionLocalOf<WindowInsets?> { null }
