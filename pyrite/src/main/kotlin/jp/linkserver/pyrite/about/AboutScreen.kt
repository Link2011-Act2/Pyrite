package jp.linkserver.pyrite.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.theme.*
import jp.linkserver.pyrite.screen.PyriteScreenScaffold

/** Host-formatted identity. [version] is the hero's release text, distinct from mosaic metadata. */
data class AboutInfo(val name: String, val version: String, val description: String? = null)

/** Localized metadata label and value; its position is named in [AboutOverviewContent]. */
data class AboutDetail(val label: String, val value: String)

/** URL-specific resource. Navigation is performed only by the host's onLinkClick callback. */
data class AboutLink(val key: String, val label: String, val url: String, val summary: String? = null)

/** Dedicated About scaffold, original identity mosaic and connected action cards. */
@Composable
fun AboutScreen(info: AboutInfo, overview: AboutOverviewContent,
    links: List<AboutLink>, onLinkClick: (AboutLink) -> Unit,
    modifier: Modifier = Modifier, title: String = info.name, linksTitle: String? = null,
    navigationIcon: @Composable () -> Unit = {}, icon: @Composable (() -> Unit)? = null,
    onVersionClick: (() -> Unit)? = null, onChannelClick: (() -> Unit)? = null,
    onAttributionClick: ((AboutAttribution) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val nothing = LocalPyriteStyle.current == PyriteStyle.Nothing
    PyriteScreenScaffold(title, modifier, navigationIcon) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)
            .background(if (nothing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = if (nothing) androidx.compose.ui.unit.Dp.Infinity else 840.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = if (nothing) 20.dp else 16.dp, vertical = if (nothing) 18.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (nothing) 28.dp else 24.dp)) {
                AboutOverviewMosaic(info, overview, Modifier.align(Alignment.CenterHorizontally), icon,
                    onVersionClick, onChannelClick, onAttributionClick)
                content()
                AboutActionGroup(links.map { AboutAction(it.key, it.label, it.summary.orEmpty(), it.label) },
                    onAction = { action -> onLinkClick(links.first { it.key == action.key }) }, title = linksTitle)
            }
        }
    }
}
