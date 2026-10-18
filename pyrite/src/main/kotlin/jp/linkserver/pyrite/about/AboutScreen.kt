package jp.linkserver.pyrite.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import jp.linkserver.pyrite.settings.SettingsActionItem
import jp.linkserver.pyrite.settings.SettingsGroup
import jp.linkserver.pyrite.settings.SettingsScreen

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
    AboutOverviewMosaic(info = info, modifier = modifier, icon = icon)
}
