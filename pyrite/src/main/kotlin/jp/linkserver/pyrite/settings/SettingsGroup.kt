package jp.linkserver.pyrite.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/** Collects visible rows. Keys must be unique within this group and stable across recomposition. */
class SettingsGroupScope internal constructor() {
    internal val entries = mutableListOf<Pair<String, @Composable () -> Unit>>()

    /** Adds one row; conditionally omit this call to hide a row and recompute its group position. */
    fun item(key: String, content: @Composable () -> Unit) {
        require(entries.none { it.first == key }) { "Duplicate settings item key: $key" }
        entries += key to content
    }
}

internal data class GroupPosition(val index: Int = 0, val count: Int = 1)
internal val LocalGroupPosition = staticCompositionLocalOf { GroupPosition() }

/**
 * Related settings with a semantic heading and automatic first/middle/last geometry.
 * The DSL stores visible rows only; it does not read preferences or retain selection.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: SettingsGroupScope.() -> Unit
) {
    val entries = SettingsGroupScope().apply(content).entries
    if (entries.isEmpty()) return
    val expressive = LocalPyriteStyle.current == PyriteStyle.Material3Expressive
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (title != null) Text(
            title, modifier = Modifier.semantics { heading() },
            style = if (expressive) MaterialTheme.typography.titleSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = .1.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.titleSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(
            2.dp
        )) {
            entries.forEachIndexed { index, entry ->
                key(entry.first) {
                    CompositionLocalProvider(LocalGroupPosition provides GroupPosition(index, entries.size)) {
                        entry.second()
                    }
                }
            }
        }
    }
}
