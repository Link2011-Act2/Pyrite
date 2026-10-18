// Source M3E settings extraction, HEAD 26913ba plus pre-existing working tree; copyright (c) 2026 Azul1nk, MIT.
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package jp.linkserver.projectpyrite.reference

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LocalInsideSettingsItem = staticCompositionLocalOf { false }

private enum class SchedulerGroupPosition { Single, First, Middle, Last }

private fun settingsRowShape(position: SchedulerGroupPosition, expressive: Boolean): Shape {
    val outerRadius = if (expressive) 22.dp else 20.dp
    val innerRadius = if (expressive) 6.dp else 4.dp
    return when (position) {
        SchedulerGroupPosition.Single -> RoundedCornerShape(outerRadius)
        SchedulerGroupPosition.First -> RoundedCornerShape(
            topStart = outerRadius, topEnd = outerRadius, bottomEnd = innerRadius, bottomStart = innerRadius
        )
        SchedulerGroupPosition.Middle -> RoundedCornerShape(innerRadius)
        SchedulerGroupPosition.Last -> RoundedCornerShape(
            topStart = innerRadius, topEnd = innerRadius, bottomEnd = outerRadius, bottomStart = outerRadius
        )
    }
}

private fun settingsRowPosition(index: Int, count: Int): SchedulerGroupPosition = when {
    count <= 1 -> SchedulerGroupPosition.Single
    index == 0 -> SchedulerGroupPosition.First
    index == count - 1 -> SchedulerGroupPosition.Last
    else -> SchedulerGroupPosition.Middle
}

@Composable
private fun SettingsRowSurface(
    position: SchedulerGroupPosition,
    modifier: Modifier = Modifier,
    expressiveStyle: Boolean = true,
    content: @Composable () -> Unit
) {
    val expressive = true
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(
            min = if (expressive) 72.dp else 0.dp
        ),
        shape = settingsRowShape(position, expressiveStyle),
        color = if (expressiveStyle) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 72.dp), contentAlignment = Alignment.CenterStart) {
            CompositionLocalProvider(LocalInsideSettingsItem provides true, content = content)
        }
    }
}

/** 1項目だけのPreference surface。複数項目にはAppSettingsGroupを使用する。 */
@Composable
fun PreferenceGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    SettingsRowSurface(SchedulerGroupPosition.Single, modifier) {
        Column(content = content)
    }
}

@Composable
fun AppSettingsItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    if (LocalInsideSettingsItem.current) {
        Column(modifier.padding(contentPadding)) { content() }
    } else {
        SettingsRowSurface(SchedulerGroupPosition.Single, modifier) {
            Column(Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Composable
private fun SettingsItemText(title: String, summary: String?, modifier: Modifier = Modifier) {
    val expressive = true
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (expressive) 6.dp else 2.dp)) {
        Text(
            text = title,
            style = if (expressive) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            } else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!summary.isNullOrEmpty()) {
            Text(
                summary,
                style = if (expressive) {
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif, fontSize = 14.sp,
                        lineHeight = 21.sp, letterSpacing = 0.15.sp
                    )
                } else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 参照アプリの配色を設定画面内だけに適用する。操作は親の行へ委譲する。 */
@Composable
private fun SettingsSwitch(checked: Boolean, enabled: Boolean = true) {
    if (true) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedIconColor = MaterialTheme.colorScheme.primary
            ),
            thumbContent = {
                Icon(
                    if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    } else {
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
fun AppSettingsSwitchItem(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val expressive = true
    AppSettingsItem {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = if (expressive) 72.dp else 80.dp)
                .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = if (expressive) 22.dp else 20.dp,
                    vertical = if (expressive) 18.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (expressive) 16.dp else 20.dp)
        ) {
            SettingsItemText(title, summary, Modifier.weight(1f).alpha(if (enabled) 1f else 0.38f))
            // 行全体を1つのSwitchとして読み上げ、thumbも同じクリック領域で操作する。
            SettingsSwitch(checked = checked, enabled = enabled)
        }
    }
}

