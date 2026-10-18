package jp.linkserver.pyrite.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.internal.PyriteDefaults

/**
 * Pyrite's connected identity mosaic. Detail cards can open host-owned channel/build dialogs;
 * version is a separate action, never a click target nested inside another clickable card.
 * It stacks at narrow widths or enlarged font scales and does not fix text heights.
 */
@Composable
fun AboutOverviewMosaic(
    info: AboutInfo, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null,
    onVersionClick: (() -> Unit)? = null,
    onDetailClick: ((AboutDetail) -> Unit)? = null
) {
    require(info.details.map { it.key }.distinct().size == info.details.size) {
        "About detail keys must be unique"
    }
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val stack = maxWidth < 420.dp || fontScale >= 1.5f
        val hasDetails = info.details.isNotEmpty()
        Column(verticalArrangement = Arrangement.spacedBy(PyriteDefaults.mosaicGap)) {
            Surface(shape = PyriteDefaults.connectedShape(true, !hasDetails),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(PyriteDefaults.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(PyriteDefaults.contentGap)) {
                    icon?.invoke()
                    Text(info.name, style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() })
                    if (onVersionClick == null) Text(info.version, style = MaterialTheme.typography.titleLarge)
                    else TextButton(onClick = onVersionClick) {
                        Text(info.version, style = MaterialTheme.typography.titleLarge)
                    }
                    info.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (stack) {
                info.details.forEachIndexed { index, detail ->
                    key(detail.key) {
                        DetailCell(detail, index == info.details.lastIndex,
                            onClick = onDetailClick?.let { { it(detail) } })
                    }
                }
            } else {
                // Pairs keep useful widths on tablets without compressing long metadata.
                info.details.chunked(2).forEachIndexed { row, pair ->
                    Row(Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(PyriteDefaults.mosaicGap)) {
                        pair.forEachIndexed { column, detail ->
                            key(detail.key) {
                                DetailCell(detail, row == (info.details.size - 1) / 2,
                                    Modifier.weight(1f).fillMaxHeight(),
                                    onDetailClick?.let { { it(detail) } },
                                    leftEdge = column == 0, rightEdge = column == pair.lastIndex)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCell(
    detail: AboutDetail, last: Boolean, modifier: Modifier = Modifier, onClick: (() -> Unit)?,
    leftEdge: Boolean = true, rightEdge: Boolean = true
) {
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(detail.label, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(detail.value, style = MaterialTheme.typography.titleMedium)
        }
    }
    val connected = PyriteDefaults.connectedShape(false, last)
    val inner = MaterialTheme.shapes.extraSmall
    val shape = connected.copy(
        bottomStart = if (leftEdge) connected.bottomStart else inner.bottomStart,
        bottomEnd = if (rightEdge) connected.bottomEnd else inner.bottomEnd
    )
    val color = MaterialTheme.colorScheme.surfaceContainerHigh
    if (onClick == null) Surface(modifier.fillMaxWidth(), shape = shape, color = color, content = body)
    else Surface(onClick, modifier.fillMaxWidth(), shape = shape, color = color, content = body)
}
