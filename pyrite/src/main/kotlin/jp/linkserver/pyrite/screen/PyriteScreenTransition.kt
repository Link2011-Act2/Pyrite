// Ported from Glyph Barty 53e568c VisualizerApp.kt screen_transition.
// Copyright (c) 2026 Azul1nk. MIT; see NOTICE.md.
package jp.linkserver.pyrite.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier

/** Physical direction: forward enters from the right, backward from the left. */
enum class PyriteNavigationDirection { Forward, Backward }

/**
 * Glyph's default spring slide/fade with per-screen saveable state.
 * Uses the official default SizeTransform (including its clipping), without a second viewport clip.
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
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = targetState,
                transitionSpec = {
                    val sign = if (direction(initialState, targetState) == PyriteNavigationDirection.Forward) 1 else -1
                    (slideInHorizontally { sign * it / 5 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -sign * it / 5 } + fadeOut())
                },
                contentKey = contentKey,
                label = "screen_transition"
            ) { screen ->
                stateHolder.SaveableStateProvider(contentKey(screen)) { content(screen) }
            }
        }
    }
}
