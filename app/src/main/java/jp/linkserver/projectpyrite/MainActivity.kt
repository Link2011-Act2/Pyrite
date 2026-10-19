package jp.linkserver.projectpyrite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.dialog.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.*
import jp.linkserver.pyrite.screen.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set this before enableEdgeToEdge initializes a preserved decor. Android 15's
        // initial PhoneWindow can otherwise keep its old content-insets applier on recreation.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent { PyriteCatalog() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Compose starts after lifecycle ON_CREATE. A preserved window already has insets;
        // deliver them now, before the first measure can clamp a restored ScrollState.
        val content = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.getRootWindowInsets(content)?.let { ViewCompat.dispatchApplyWindowInsets(content, it) }
    }
}

private enum class ColorMode { System, Light, Dark }

@Composable
internal fun PyriteCatalog() {
    var style by rememberSaveable { mutableStateOf(PyriteStyle.Material3Expressive) }
    var colorMode by rememberSaveable { mutableStateOf(ColorMode.System) }
    var dynamicColor by rememberSaveable { mutableStateOf(false) }
    var largeText by rememberSaveable { mutableStateOf(false) }
    var narrow by rememberSaveable { mutableStateOf(false) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var hierarchyEnabled by rememberSaveable { mutableStateOf(true) }
    var hierarchyUndo by rememberSaveable { mutableStateOf(false) }
    var hierarchyToggles by rememberSaveable { mutableIntStateOf(0) }
    var hierarchyNavigations by rememberSaveable { mutableIntStateOf(0) }
    var optionalRow by rememberSaveable { mutableStateOf(true) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    var aboutOrigin by rememberSaveable { mutableStateOf(CatalogPage.Settings) }
    var page by rememberSaveable { mutableStateOf(CatalogPage.Settings) }
    var showStylePicker by rememberSaveable { mutableStateOf(false) }
    var draftStyle by rememberSaveable { mutableStateOf(style) }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var draftColorMode by rememberSaveable { mutableStateOf(colorMode) }
    val density = LocalDensity.current
    val dark = when (colorMode) {
        ColorMode.System -> isSystemInDarkTheme()
        ColorMode.Light -> false
        ColorMode.Dark -> true
    }
    val settingsScroll = rememberScrollState()
    val context = LocalContext.current
    val view = LocalView.current
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    val back = {
        page = when (page) {
            CatalogPage.About -> aboutOrigin
            CatalogPage.AboutDetails -> CatalogPage.About
            CatalogPage.Components -> CatalogPage.Settings
            CatalogPage.HierarchyDetails -> CatalogPage.Hierarchy
            else -> CatalogPage.Components
        }
    }
    BackHandler(page != CatalogPage.Settings, onBack = back)

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * if (largeText) 1.5f else 1f)
    ) {
        PyriteTheme(style = style, darkTheme = dark, dynamicColor = dynamicColor) {
            CatalogViewport(narrow) {
                PyriteScreenTransition(
                    targetState = page,
                    direction = { initial, target ->
                        if (target.depth > initial.depth) PyriteNavigationDirection.Forward
                        else PyriteNavigationDirection.Backward
                    }
                ) { currentPage ->
                    when (currentPage) {
                        CatalogPage.Settings -> SettingsScreen(
                            title = stringResource(R.string.catalog_title),
                            scrollState = settingsScroll,
                            actions = {
                                val label = stringResource(R.string.components)
                                IconButton(
                                    onClick = { page = CatalogPage.Components },
                                    modifier = Modifier.semantics { contentDescription = label }
                                ) {
                                    Text("UI", modifier = Modifier.clearAndSetSemantics {},
                                        style = MaterialTheme.typography.labelLarge, fontFamily = FontFamily.Monospace)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.catalog_intro), style = MaterialTheme.typography.bodyLarge)
                            SettingsGroup(title = stringResource(R.string.appearance)) {
                                item("style") {
                                    SettingsActionItem(
                                        title = stringResource(R.string.design_style),
                                        summary = styleLabel(style),
                                        onClick = { draftStyle = style; showStylePicker = true }
                                    )
                                }
                                item("color") {
                                    SettingsActionItem(
                                        title = stringResource(R.string.color_mode),
                                        summary = colorModeLabel(colorMode),
                                        onClick = { draftColorMode = colorMode; showColorPicker = true }
                                    )
                                }
                                item("dynamic") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.dynamic_color), checked = dynamicColor,
                                        onCheckedChange = { dynamicColor = it },
                                        summary = stringResource(R.string.dynamic_summary),
                                        enabled = style != PyriteStyle.Nothing && android.os.Build.VERSION.SDK_INT >= 31
                                    )
                                }
                                item("narrow") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.narrow_preview), checked = narrow,
                                        onCheckedChange = { narrow = it },
                                        summary = stringResource(R.string.narrow_summary)
                                    )
                                }
                                item("large") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.large_text), checked = largeText,
                                        onCheckedChange = { largeText = it },
                                        summary = stringResource(R.string.large_text_summary)
                                    )
                                }
                            }
                            SettingsGroup(title = stringResource(R.string.sample_settings)) {
                                item("notifications") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.notifications), checked = notifications,
                                        onCheckedChange = { notifications = it },
                                        summary = stringResource(R.string.notifications_summary)
                                    )
                                }
                                item("optional") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.optional_row), checked = optionalRow,
                                        onCheckedChange = { optionalRow = it }
                                    )
                                }
                                if (optionalRow) item("counter") {
                                    SettingsActionItem(
                                        title = stringResource(R.string.action_counter),
                                        summary = pluralStringResource(R.plurals.counter_value, clicks, clicks),
                                        onClick = { clicks++ }
                                    )
                                }
                                item("disabled") {
                                    SettingsSwitchItem(
                                        title = stringResource(R.string.disabled_setting), checked = true,
                                        onCheckedChange = {}, enabled = false,
                                        summary = stringResource(R.string.disabled_summary)
                                    )
                                }
                            }
                            SettingsGroup(title = stringResource(R.string.explore)) {
                                item("about") {
                                    SettingsActionItem(
                                        title = stringResource(R.string.about),
                                        summary = stringResource(R.string.about_summary),
                                        onClick = { aboutOrigin = CatalogPage.Settings; page = CatalogPage.About }
                                    )
                                }
                                item("components") {
                                    SettingsActionItem(
                                        title = stringResource(R.string.components),
                                        summary = stringResource(R.string.components_summary),
                                        onClick = { page = CatalogPage.Components }
                                    )
                                }
                            }
                        }
                        CatalogPage.About -> AboutScreen(
                            title = stringResource(R.string.about),
                            info = AboutInfo(
                                name = "Pyrite", version = "0.1.0-SNAPSHOT"
                            ),
                            overview = AboutOverviewContent(
                                channel = AboutDetail("Channel", "Preview"),
                                build = AboutDetail("Build", "2026.10.18"),
                                version = AboutDetail("Version", "v0.1")
                            ),
                            icon = { PyriteMark() },
                            navigationIcon = { PyriteBackButton(back, stringResource(R.string.back)) },
                            linksTitle = stringResource(R.string.resources),
                            links = listOf(
                                AboutLink("material", "Material 3", "https://developer.android.com/develop/ui/compose/designsystems/material3"),
                                AboutLink("compose", "Jetpack Compose", "https://developer.android.com/develop/ui/compose")
                            ),
                            onLinkClick = { link ->
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, link.url.toUri())) }
                                    .onFailure { Toast.makeText(context, R.string.no_browser, Toast.LENGTH_SHORT).show() }
                            }
                        ) {
                            AboutActionGroup(listOf(AboutAction("artifact", stringResource(R.string.maven_artifact), "jp.linkserver:pyrite:0.1.0-SNAPSHOT", stringResource(R.string.integration))), onAction = { page = CatalogPage.AboutDetails })
                        }
                        CatalogPage.AboutDetails -> SettingsScreen(
                            title = stringResource(R.string.build_info),
                            navigationIcon = { PyriteBackButton(back, stringResource(R.string.back)) }
                        ) { AboutDetailsCatalog() }
                        else -> ComponentsPage(
                            section = currentPage, style = style, onBack = back,
                            onStyle = { draftStyle = style; showStylePicker = true },
                            onColorMode = { draftColorMode = colorMode; showColorPicker = true },
                            onAbout = { aboutOrigin = currentPage; page = CatalogPage.About },
                            onSection = { page = it },
                            hierarchyEnabled = hierarchyEnabled,
                            onHierarchyEnabledChange = { hierarchyEnabled = it; hierarchyToggles++ },
                            hierarchyUndo = hierarchyUndo, onHierarchyUndoChange = { hierarchyUndo = it },
                            hierarchyToggles = hierarchyToggles, hierarchyNavigations = hierarchyNavigations,
                            onOpenHierarchy = { hierarchyNavigations++; page = CatalogPage.HierarchyDetails }
                        )
                    }
                }
            }
            if (showStylePicker) SettingsChoiceDialog(
                title = stringResource(R.string.design_style),
                choices = PyriteStyle.entries.map {
                    SettingsChoice(it.name, it, styleLabel(it), styleDescription(it))
                },
                selectedValue = draftStyle, onValueChange = { draftStyle = it },
                onConfirm = { style = draftStyle; showStylePicker = false },
                onDismissRequest = { showStylePicker = false },
                confirmLabel = stringResource(R.string.apply),
                dismissLabel = stringResource(R.string.cancel)
            )
            if (showColorPicker) SettingsChoiceDialog(
                title = stringResource(R.string.color_mode),
                choices = ColorMode.entries.map { SettingsChoice(it.name, it, colorModeLabel(it)) },
                selectedValue = draftColorMode, onValueChange = { draftColorMode = it },
                onConfirm = { colorMode = draftColorMode; showColorPicker = false },
                onDismissRequest = { showColorPicker = false },
                confirmLabel = stringResource(R.string.apply),
                dismissLabel = stringResource(R.string.cancel)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.OfficialComponentsContent(style: PyriteStyle) {
    var progress by rememberSaveable { mutableFloatStateOf(.5f) }
    val sliderState = rememberSliderState(progress)
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    val progressLabel = stringResource(R.string.progress)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.buttons), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { clicks++ }) { Text(pluralStringResource(R.plurals.counter_value, clicks, clicks)) }
            val labels = listOf(stringResource(R.string.option_one), stringResource(R.string.option_two))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                labels.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selected == index, onClick = { selected = index },
                        shape = SegmentedButtonDefaults.itemShape(index, labels.size)
                    ) { Text(label) }
                }
            }
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.progress), style = MaterialTheme.typography.titleMedium)
            if (style == PyriteStyle.Material3Expressive) {
                LoadingIndicator()
            } else {
                CircularProgressIndicator()
            }
            Text(stringResource(R.string.progress_value, (progress * 100).toInt()))
            Slider(state = sliderState, onValueChange = { sliderState.value = it; progress = it },
                modifier = Modifier.semantics { contentDescription = progressLabel })
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) R.string.collapse else R.string.expand))
            }
            AnimatedVisibility(
                visible = expanded, enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) { Text(stringResource(R.string.expansion_body)) }
        }
    }
}

@Composable
internal fun PyriteMark() {
    Surface(
        modifier = Modifier.fillMaxSize(), shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("P", style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
internal fun styleLabel(style: PyriteStyle): String = when (style) {
    PyriteStyle.Material3Expressive -> "Material 3 Expressive"
    PyriteStyle.Nothing -> stringResource(R.string.nothing_style)
}

@Composable
private fun styleDescription(style: PyriteStyle): String = stringResource(when (style) {
    PyriteStyle.Material3Expressive -> R.string.expressive_description
    PyriteStyle.Nothing -> R.string.nothing_description
})

@Composable
private fun colorModeLabel(mode: ColorMode): String = stringResource(when (mode) {
    ColorMode.System -> R.string.system_mode
    ColorMode.Light -> R.string.light_mode
    ColorMode.Dark -> R.string.dark_mode
})

/** Preview width is app-only; public screen geometry remains library-owned. */
@Composable
private fun CatalogViewport(narrow: Boolean, content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = if (narrow) 360.dp else Dp.Infinity).fillMaxSize()) {
                content()
            }
        }
    }
}
