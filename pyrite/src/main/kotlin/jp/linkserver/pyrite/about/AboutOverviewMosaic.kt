// Ported from Scheduler AboutOverviewMosaic and Glyph/ShutUp AboutOverview.
// Copyright (c) 2026 Azul1nk. MIT; see NOTICE.md.
package jp.linkserver.pyrite.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.linkserver.pyrite.theme.*

/** Optional, already localized metadata. Null fields are omitted, not replaced with invented values. */
data class AboutOverviewContent(
    val channel: AboutDetail? = null,
    val build: AboutDetail? = null,
    val version: AboutDetail? = null,
    val attribution: AboutAttribution? = null
)

/** Attribution content only. Opening a link remains a host callback. */
data class AboutAttribution(val prefix: String, val name: String, val url: String)

/**
 * Original spanning hero and two-column connected mosaic (20dp corners, 3dp gaps, 48dp bridges).
 * The icon slot fills the source's circular 92dp viewport. Release text is formatted by the host.
 * Scheduler's narrow-header fallback and content-sized lower cards prevent clipping at large fonts.
 */
@Composable
fun AboutOverviewMosaic(
    info: AboutInfo,
    overview: AboutOverviewContent,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    onVersionClick: (() -> Unit)? = null,
    onChannelClick: (() -> Unit)? = null,
    onAttributionClick: ((AboutAttribution) -> Unit)? = null
) {
    val nothing = LocalPyriteStyle.current == PyriteStyle.Nothing
    val cardColor = pyriteCardColor()
    val gutterColor = if (nothing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
    val gap = 3.dp
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.widthIn(max = 520.dp).fillMaxWidth()) {
        val squareSize = (maxWidth - gap) * .5f
        val stackedHeader = maxWidth / fontScale < 320.dp
        val channel = overview.channel
        val hasRight = overview.build != null || overview.version != null
        val hasLower = channel != null || hasRight
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            SourceCard(onVersionClick, Modifier.fillMaxWidth().heightIn(min = 176.dp)) {
                val identity: @Composable () -> Unit = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(info.name,
                                modifier = Modifier.semantics { heading() },
                                style = if (nothing) LocalTextStyle.current else MaterialTheme.typography.headlineMedium,
                                fontFamily = LocalPyriteFonts.current.title ?: (if (nothing) FontFamily.SansSerif
                                    else if (LocalPyriteFonts.current.body != null) FontFamily.Default else null),
                                fontWeight = if (nothing) FontWeight.Normal else FontWeight.Bold,
                                fontSize = if (nothing) 27.sp else 30.sp,
                                lineHeight = if (nothing) 32.sp else 36.sp)
                            overview.attribution?.let { attribution ->
                                val active = onAttributionClick != null
                                val linkColor = MaterialTheme.colorScheme.primary
                                Text(buildAnnotatedString {
                                    append(attribution.prefix); append(" ")
                                    if (active) withLink(LinkAnnotation.Clickable(
                                        attribution.url,
                                        TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                                        { onAttributionClick?.invoke(attribution) }
                                    )) { append(attribution.name) }
                                    else append(attribution.name)
                                }, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                            }
                        }
                        if (info.version.isNotBlank()) Text(info.version,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = if (nothing) 21.sp else MaterialTheme.typography.bodyMedium.lineHeight)
                        info.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                val appIcon: @Composable (Modifier) -> Unit = { sizeModifier ->
                    if (icon != null) Surface(sizeModifier, CircleShape,
                        color = if (nothing) Color.White else MaterialTheme.colorScheme.surface,
                        border = if (nothing) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                    ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { icon() } }
                }
                if (stackedHeader) Column(Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    appIcon(Modifier.size(64.dp)); identity()
                } else Row(Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    appIcon(Modifier.size(92.dp))
                    Box(Modifier.weight(1f)) { identity() }
                }
            }
            if (hasLower) Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min).drawWithContent {
                    drawContent()
                    val gapPx = gap.toPx()
                    val overlap = 1.dp.toPx()
                    val bridge = 48.dp.toPx()
                    val firstWidth = if (channel != null && hasRight) (size.width - gapPx) * .5f else size.width
                    val start = (firstWidth - bridge) * .5f
                    drawRect(cardColor, Offset(start, -gapPx - overlap), Size(bridge, gapPx + overlap * 2f))
                    drawCircle(gutterColor, gapPx * .5f, Offset(start, -gapPx * .5f))
                    drawCircle(gutterColor, gapPx * .5f, Offset(start + bridge, -gapPx * .5f))
                    if (channel != null && hasRight) {
                        val verticalStart = (size.height - bridge) * .5f
                        drawRect(cardColor, Offset(firstWidth - overlap, verticalStart), Size(gapPx + overlap * 2f, bridge))
                        drawCircle(gutterColor, gapPx * .5f, Offset(firstWidth + gapPx * .5f, verticalStart))
                        drawCircle(gutterColor, gapPx * .5f, Offset(firstWidth + gapPx * .5f, verticalStart + bridge))
                    }
                }, horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                channel?.let {
                    SourceCard(onChannelClick, Modifier.weight(1f).heightIn(min = squareSize).fillMaxHeight()) {
                        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(it.label, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StrongValue(it.value, nothing, Modifier.padding(top = 14.dp))
                        }
                    }
                }
                if (hasRight) SourceCard(onVersionClick, Modifier.weight(1f).heightIn(min = squareSize).fillMaxHeight()) {
                    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        overview.build?.let {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(it.label, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(it.value, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
                            }
                        }
                        overview.version?.let {
                            Column(Modifier.padding(top = if (overview.build != null) 14.dp else 0.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(it.label, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                StrongValue(it.value, nothing, dotted = nothing, accent = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun StrongValue(value: String, nothing: Boolean, modifier: Modifier = Modifier, dotted: Boolean = false,
    accent: Boolean = false) {
    val fonts = LocalPyriteFonts.current
    // M3E's source uses a nullable family (inherit). Do not let a body-only override
    // bleed into the two identity roles through LocalTextStyle's merge.
    val sourceFamily = (if (nothing) SourceNothingTypography.bodyLarge.fontFamily else SourceExpressiveTypography.bodyLarge.fontFamily)
        ?: if (fonts.body != null) FontFamily.Default else null
    Text(value, modifier, fontFamily = if (accent) fonts.accent ?: (if (dotted) FontFamily.Monospace else sourceFamily) else fonts.title ?: sourceFamily,
        fontWeight = if (nothing) FontWeight.Normal else FontWeight.Bold, fontSize = 25.sp, lineHeight = 29.sp)
}

@Composable private fun SourceCard(onClick: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val color = pyriteCardColor()
    val shape = RoundedCornerShape(20.dp)
    if (onClick == null) Surface(modifier, shape, color, content = content)
    else Surface(onClick, modifier, shape = shape, color = color, content = content)
}
