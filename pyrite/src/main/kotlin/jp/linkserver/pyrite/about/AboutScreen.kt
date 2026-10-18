package jp.linkserver.pyrite.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.settings.SettingsActionItem
import jp.linkserver.pyrite.settings.SettingsGroup
import jp.linkserver.pyrite.settings.SettingsScreen
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/** Host-provided app identity; version parsing and PackageManager access stay in the host. */
data class AboutInfo(
    val name: String,
    val version: String,
    val description: String? = null,
    val details: List<AboutDetail> = emptyList()
)

/** Localized label and preformatted value, e.g. build number or release channel. */
data class AboutDetail(val key: String, val label: String, val value: String)

/** A displayable link. Pyrite delegates opening and error handling to [AboutScreen]'s host. */
data class AboutLink(val key: String, val label: String, val url: String, val summary: String? = null)

/**
 * About page with adaptive identity cards, external links and an application-owned content slot.
 * No network, intents, BuildConfig, persistence or application logo is embedded in this UI.
 */
@Composable
fun AboutScreen(
    info: AboutInfo,
    links: List<AboutLink>,
    onLinkClick: (AboutLink) -> Unit,
    modifier: Modifier = Modifier,
    title: String = info.name,
    linksTitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    icon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    SettingsScreen(title, modifier, navigationIcon) {
        AboutOverview(info = info, icon = icon)
        content()
        SettingsGroup(title = linksTitle) {
            links.forEach { link ->
                item(link.key) {
                    SettingsActionItem(
                        title = link.label, summary = link.summary,
                        onClick = { onLinkClick(link) },
                        trailingContent = { Text("↗", Modifier.clearAndSetSemantics {}) }
                    )
                }
            }
        }
    }
}

/**
 * Connected identity mosaic, stacking on narrow screens or with enlarged text.
 * All cards size to their contents; arbitrary app names and versions are allowed.
 */
@Composable
fun AboutOverview(
    info: AboutInfo,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val nothing = LocalPyriteStyle.current == PyriteStyle.Nothing
    val fontScale = LocalDensity.current.fontScale
    require(info.details.map { it.key }.distinct().size == info.details.size) {
        "About detail keys must be unique"
    }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val stack = maxWidth < 480.dp || fontScale >= 1.5f
        val identity: @Composable () -> Unit = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    icon?.invoke()
                    Text(
                        info.name, style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        info.version, style = MaterialTheme.typography.titleLarge
                    )
                    info.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        val details: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(if (nothing) 3.dp else 8.dp)) {
                info.details.forEach { detail ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(detail.label, style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(detail.value, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
        if (stack) {
            Column(verticalArrangement = Arrangement.spacedBy(if (nothing) 3.dp else 12.dp)) {
                identity()
                if (info.details.isNotEmpty()) details()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(if (nothing) 3.dp else 12.dp)) {
                Box(Modifier.weight(1.3f)) { identity() }
                if (info.details.isNotEmpty()) Box(Modifier.weight(1f)) { details() }
            }
        }
    }
}
