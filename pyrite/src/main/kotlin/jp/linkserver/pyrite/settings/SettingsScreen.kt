package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.screen.PyriteScreenScaffold

/**
 * Scrollable, width-constrained settings page. The host supplies navigation and owns state.
 * Content receives a ColumnScope; use SettingsGroup for keyed groups of related items.
 */
@Composable
fun SettingsScreen(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    maxContentWidth: Dp = 960.dp,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    PyriteScreenScaffold(
        title = title, modifier = modifier, navigationIcon = navigationIcon, actions = actions
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = maxContentWidth).fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                content = content
            )
        }
    }
}
