package jp.linkserver.projectpyrite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PyriteCatalog() }
    }
}

private enum class Page { Settings, About, Components }
private enum class ColorMode { System, Light, Dark }

@Composable
internal fun PyriteCatalog() {
    var style by rememberSaveable { mutableStateOf(PyriteStyle.Material3Expressive) }
    var colorMode by rememberSaveable { mutableStateOf(ColorMode.System) }
    var dynamicColor by rememberSaveable { mutableStateOf(false) }
    var largeText by rememberSaveable { mutableStateOf(false) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var optionalRow by rememberSaveable { mutableStateOf(true) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    var page by rememberSaveable { mutableStateOf(Page.Settings) }
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
    BackHandler(page != Page.Settings) { page = Page.Settings }

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * if (largeText) 1.5f else 1f)
    ) {
        PyriteTheme(style = style, darkTheme = dark, dynamicColor = dynamicColor) {
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) },
                label = "Catalog navigation"
            ) { currentPage ->
                when (currentPage) {
                    Page.Settings -> SettingsScreen(
                        title = stringResource(R.string.catalog_title),
                        scrollState = settingsScroll,
                        actions = {
                            val label = stringResource(R.string.components)
                            IconButton(
                                onClick = { page = Page.Components },
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
                                    onClick = { page = Page.About }
                                )
                            }
                            item("components") {
                                SettingsActionItem(
                                    title = stringResource(R.string.components),
                                    summary = stringResource(R.string.components_summary),
                                    onClick = { page = Page.Components }
                                )
                            }
                        }
                    }
                    Page.About -> AboutScreen(
                        title = stringResource(R.string.about),
                        info = AboutInfo(
                            name = "Pyrite", version = "0.1.0-SNAPSHOT",
                            description = stringResource(R.string.about_description),
                            details = listOf(
                                AboutDetail("m3", "Material 3", "1.5.0-beta01"),
                                AboutDetail("compose", "Compose", "1.12.0"),
                                AboutDetail("style", stringResource(R.string.design_style), styleLabel(style))
                            )
                        ),
                        icon = { PyriteMark() },
                        navigationIcon = { BackButton { page = Page.Settings } },
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
                        SettingsGroup(title = stringResource(R.string.integration)) {
                            item("artifact") {
                                SettingsActionItem(
                                    title = stringResource(R.string.maven_artifact),
                                    summary = "jp.linkserver:pyrite:0.1.0-SNAPSHOT",
                                    onClick = { Toast.makeText(context, R.string.artifact_hint, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        }
                    }
                    Page.Components -> ComponentsPage(
                        style = style, onBack = { page = Page.Settings }
                    )
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
private fun ComponentsPage(style: PyriteStyle, onBack: () -> Unit) {
    val sliderState = rememberSliderState(.5f)
    val progress = sliderState.value
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    val progressLabel = stringResource(R.string.progress)
    SettingsScreen(
        title = stringResource(R.string.components),
        navigationIcon = { BackButton(onBack) }
    ) {
        Text(stringResource(R.string.official_components), style = MaterialTheme.typography.bodyLarge)
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
                Slider(state = sliderState, onValueChange = { sliderState.value = it },
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
}



@Composable
private fun PyriteMark() {
    Surface(
        modifier = Modifier.size(72.dp), shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Box(Modifier.padding(16.dp)) {
            Text("P", style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    val label = stringResource(R.string.back)
    IconButton(onBack, modifier = Modifier.semantics { contentDescription = label }) {
        Text("‹", modifier = Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun styleLabel(style: PyriteStyle): String = when (style) {
    PyriteStyle.Material3 -> "Material 3"
    PyriteStyle.Material3Expressive -> "Material 3 Expressive"
    PyriteStyle.Nothing -> stringResource(R.string.nothing_style)
}

@Composable
private fun styleDescription(style: PyriteStyle): String = stringResource(when (style) {
    PyriteStyle.Material3 -> R.string.material_description
    PyriteStyle.Material3Expressive -> R.string.expressive_description
    PyriteStyle.Nothing -> R.string.nothing_description
})

@Composable
private fun colorModeLabel(mode: ColorMode): String = stringResource(when (mode) {
    ColorMode.System -> R.string.system_mode
    ColorMode.Light -> R.string.light_mode
    ColorMode.Dark -> R.string.dark_mode
})
