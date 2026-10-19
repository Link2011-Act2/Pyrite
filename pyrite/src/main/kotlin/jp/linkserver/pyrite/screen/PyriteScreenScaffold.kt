// Ported from ShutUp ScreenScaffold and Glyph SettingsHeader.
// Copyright (c) 2026 Azul1nk. MIT; see NOTICE.md.
package jp.linkserver.pyrite.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.linkserver.pyrite.R
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/**
 * Source-style page chrome. The host owns navigation and supplies the localized title/actions.
 * M3E uses ShutUp's 23sp/29sp Medium title; Nothing uses Glyph's settings header.
 * Content receives the official Scaffold insets. This component performs no screen transition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyriteScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val nothing = LocalPyriteStyle.current == PyriteStyle.Nothing
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(title,
                        modifier = if (nothing) Modifier.padding(bottom = 2.dp) else Modifier,
                        style = if (nothing) MaterialTheme.typography.titleLarge.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal
                        ) else ShutterTitleStyle)
                },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        content = content
    )
}

/** Original mirrored 24dp arrow in the official IconButton target; provide a localized label. */
@Composable
fun PyriteBackButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    IconButton(onClick, modifier) {
        Icon(painterResource(R.drawable.pyrite_ic_arrow_back), contentDescription)
    }
}

private val ShutterTitleStyle = TextStyle(
    fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
    fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = 0.sp
)
