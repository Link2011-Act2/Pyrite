// About action/update layouts ported from Glyph Barty and ShutUp. Copyright (c) 2026 Azul1nk, MIT.
package jp.linkserver.pyrite.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.linkserver.pyrite.theme.*
import jp.linkserver.pyrite.internal.PyriteDefaults

/** One source-style action card. Keys must be unique inside an AboutActionGroup. */
data class AboutAction(val key: String, val title: String, val description: String,
    val actionLabel: String, val enabled: Boolean = true)

/** Dedicated connected About cards; not settings rows. Opening links remains host-owned. */
@Composable
fun AboutActionGroup(actions: List<AboutAction>, onAction: (AboutAction) -> Unit,
    modifier: Modifier = Modifier, title: String? = null
) {
    require(actions.map { it.key }.distinct().size == actions.size) { "About action keys must be unique" }
    if (actions.isEmpty()) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        title?.let {
            Text(it, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            actions.forEachIndexed { index, action ->
                androidx.compose.runtime.key(action.key) {
                    Surface(shape = PyriteDefaults.connectedShape(index == 0, index == actions.lastIndex),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(action.title, style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (LocalPyriteStyle.current == PyriteStyle.Nothing)
                                        FontWeight.Normal else FontWeight.Bold)
                                if (action.description.isNotBlank()) Text(action.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button({ onAction(action) }, Modifier.fillMaxWidth(), enabled = action.enabled) {
                                Text(action.actionLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ShutUp's update card, without update logic. The caller supplies localized result/loading text.
 * Checking disables the action and announces status politely; no coroutine or network is owned here.
 */
@Composable
fun AboutUpdateCard(description: String, status: String, actionLabel: String, onCheck: () -> Unit,
    modifier: Modifier = Modifier, checking: Boolean = false, hasResult: Boolean = false,
    enabled: Boolean = true
) {
    Surface(modifier.fillMaxWidth(), RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column {
            Text(description, Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(horizontal = 22.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)) {
                val stacked = maxWidth / androidx.compose.ui.platform.LocalDensity.current.fontScale < 220.dp
                val statusContent: @Composable (Modifier) -> Unit = { textModifier ->
                    Text(status, textModifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
                        color = if (checking || hasResult) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val action: @Composable () -> Unit = {
                    Button(onCheck, enabled = enabled && !checking) { Text(actionLabel) }
                }
                if (stacked) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    statusContent(Modifier); action()
                } else Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    statusContent(Modifier.weight(1f)); action()
                }
            }
        }
    }
}
