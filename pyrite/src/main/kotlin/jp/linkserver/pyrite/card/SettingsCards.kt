@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package jp.linkserver.pyrite.card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.internal.PyriteDefaults
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/**
 * Explanation with optional supporting content, standard spacing and a semantic heading.
 * Content inherits body typography. The caller owns links and application-specific information.
 */
@Composable
fun SettingsInfoCard(
    title: String, description: String, modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(modifier.fillMaxWidth(), shape = PyriteDefaults.cardShape(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(PyriteDefaults.cardPadding),
            verticalArrangement = Arrangement.spacedBy(PyriteDefaults.contentGap)) {
            Text(title, style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() })
            Text(description, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) { content() }
        }
    }
}

/**
 * Explanation and a full-width action. Loading disables repeat activation and exposes progress;
 * the host supplies a localized action label for each state and starts/cancels any work.
 */
@Composable
fun SettingsActionCard(
    title: String, description: String, actionLabel: String, onAction: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false,
    supportingContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    SettingsInfoCard(title, description, modifier) {
        supportingContent?.invoke(this)
        if (loading) {
            Row(Modifier.fillMaxWidth().semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = actionLabel
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            }, horizontalArrangement = Arrangement.Center) {
                if (LocalPyriteStyle.current == PyriteStyle.Material3Expressive)
                    LoadingIndicator(Modifier.size(40.dp).clearAndSetSemantics {})
                else CircularProgressIndicator(Modifier.size(28.dp).clearAndSetSemantics {}, strokeWidth = 3.dp)
            }
        }
        Button(onClick = onAction, enabled = enabled && !loading, modifier = Modifier.fillMaxWidth()) {
            Text(actionLabel)
        }
    }
}
