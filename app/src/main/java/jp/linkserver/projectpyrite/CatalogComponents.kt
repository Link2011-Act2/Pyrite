package jp.linkserver.projectpyrite

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.about.*
import jp.linkserver.pyrite.card.*
import jp.linkserver.pyrite.dialog.*
import jp.linkserver.pyrite.settings.*
import jp.linkserver.pyrite.theme.PyriteStyle
import jp.linkserver.pyrite.screen.PyriteBackButton

/** Content only: the catalog root owns the single transition host and all destinations. */
@Composable
internal fun ComponentsPage(
    section: CatalogPage, style: PyriteStyle, onBack: () -> Unit, onStyle: () -> Unit,
    onColorMode: () -> Unit, onAbout: () -> Unit, onSection: (CatalogPage) -> Unit,
    hierarchyEnabled: Boolean, onHierarchyEnabledChange: (Boolean) -> Unit,
    hierarchyUndo: Boolean, onHierarchyUndoChange: (Boolean) -> Unit,
    hierarchyToggles: Int, hierarchyNavigations: Int, onOpenHierarchy: () -> Unit
) {
    SettingsScreen(
        title = stringResource(section.title),
        navigationIcon = { PyriteBackButton(onBack, stringResource(R.string.back)) },
        actions = {
            val styleLabel = stringResource(R.string.design_style)
            val modeLabel = stringResource(R.string.color_mode)
            IconButton(onStyle, Modifier.semantics { contentDescription = styleLabel }) {
                Text("Aa", Modifier.clearAndSetSemantics {})
            }
            IconButton(onColorMode, Modifier.semantics { contentDescription = modeLabel }) {
                Text("◐", Modifier.clearAndSetSemantics {})
            }
        }
    ) {
        if (section != CatalogPage.HierarchyDetails) Text(stringResource(section.description),
            style = MaterialTheme.typography.bodyLarge)
        when (section) {
            CatalogPage.Components -> {
                SettingsGroup(title = stringResource(R.string.catalog_categories)) {
                    CatalogPage.categories.forEach { entry ->
                        item(entry.name) {
                            SettingsNavigationItem(stringResource(entry.title),
                                onClick = { onSection(entry) }, summary = stringResource(entry.description))
                        }
                    }
                }
                SettingsInfoCard(stringResource(R.string.verification_guide),
                    stringResource(R.string.verification_body))
            }
            CatalogPage.Rows -> RowsCatalog()
            CatalogPage.Cards -> CardsCatalog()
            CatalogPage.Dialogs -> DialogsCatalog()
            CatalogPage.Mosaic -> MosaicCatalog(style)
            CatalogPage.Screens -> ScreenPreview(onAbout)
            CatalogPage.Hierarchy, CatalogPage.HierarchyDetails -> HierarchyCatalog(
                detail = section == CatalogPage.HierarchyDetails,
                enabled = hierarchyEnabled, onEnabledChange = onHierarchyEnabledChange,
                undo = hierarchyUndo, onUndoChange = onHierarchyUndoChange,
                toggles = hierarchyToggles, navigations = hierarchyNavigations,
                onOpenDetail = onOpenHierarchy)
            CatalogPage.Official -> OfficialComponentsContent(style)
            else -> error("Not a component destination: $section")
        }
    }
}

@Composable
private fun ColumnScope.RowsCatalog() {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var longText by rememberSaveable { mutableStateOf(false) }
    var showFirst by rememberSaveable { mutableStateOf(true) }
    var showMiddle by rememberSaveable { mutableStateOf(true) }
    var showLast by rememberSaveable { mutableStateOf(true) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    SettingsGroup(title = stringResource(R.string.test_conditions)) {
        item("enabled") { SettingsSwitchItem(stringResource(R.string.enable_examples), enabled, { enabled = it }) }
        item("long") { SettingsSwitchItem(stringResource(R.string.long_text), longText, { longText = it }) }
        item("first") { SettingsSwitchItem(stringResource(R.string.show_first), showFirst, { showFirst = it }) }
        item("middle") { SettingsSwitchItem(stringResource(R.string.show_middle), showMiddle, { showMiddle = it }) }
        item("last") { SettingsSwitchItem(stringResource(R.string.show_last), showLast, { showLast = it }) }
    }
    SettingsGroup(title = stringResource(R.string.connected_rows), modifier = Modifier.animateContentSize()) {
        if (showFirst) item("action") {
            SettingsActionItem(
                title = stringResource(if (longText) R.string.long_title else R.string.navigation_row),
                onClick = { clicks++ }, enabled = enabled,
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(if (longText) R.string.long_description else R.string.action_description))
                        Text(pluralStringResource(R.plurals.counter_value, clicks, clicks))
                    }
                },
                trailingContent = { Text("›", Modifier.clearAndSetSemantics {}) }
            )
        }
        if (showMiddle) item("switch") {
            SettingsSwitchItem(stringResource(R.string.rich_switch), checked, { checked = it },
                enabled = enabled, supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(if (longText) R.string.long_description else R.string.rich_summary))
                        Text(stringResource(if (checked) R.string.state_active else R.string.state_paused),
                            color = MaterialTheme.colorScheme.primary)
                    }
                })
        }
        if (showLast) item("info") {
            SettingsInfoItem(stringResource(R.string.read_only),
                summary = pluralStringResource(R.plurals.counter_value, clicks, clicks))
        }
    }
    SettingsGroup(title = stringResource(R.string.source_settings)) {
        item("navigation") {
            SettingsNavigationItem(stringResource(R.string.navigation_row), { clicks++ },
                summary = pluralStringResource(R.plurals.counter_value, clicks, clicks), enabled = enabled)
        }
        item("split") {
            SettingsSplitSwitchItem(stringResource(R.string.split_navigation), checked, { checked = it },
                onClick = { clicks++ }, navigationEnabled = enabled, switchEnabled = enabled)
        }
        item("compound-radio") {
            SettingsRadioGroupItem(stringResource(R.string.single_selection),
                listOf(SettingsChoice("one", 0, stringResource(R.string.option_one)),
                    SettingsChoice("two", 1, stringResource(R.string.option_two)),
                    SettingsChoice("disabled", 2, stringResource(R.string.disabled_setting), enabled = false)),
                selected, { selected = it }, enabled = enabled,
                summary = stringResource(R.string.rich_summary))
        }
    }
    SettingsGroup(title = stringResource(R.string.single_selection), modifier = Modifier.selectableGroup()) {
        item("first") { SettingsRadioItem(stringResource(R.string.option_one), selected == 0, { selected = 0 },
            enabled = enabled, summary = stringResource(R.string.action_description)) }
        item("second") { SettingsRadioItem(stringResource(R.string.option_two), selected == 1, { selected = 1 },
            enabled = enabled, supportingContent = { Text(stringResource(R.string.rich_summary)) }) }
        item("disabled") { SettingsRadioItem(stringResource(R.string.disabled_setting), false, {}, enabled = false) }
    }
}

@Composable
private fun ColumnScope.CardsCatalog() {
    var slider by rememberSaveable { mutableFloatStateOf(.5f) }
    var commits by rememberSaveable { mutableIntStateOf(0) }
    var slotActions by rememberSaveable { mutableIntStateOf(0) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    SettingsGroup(title = stringResource(R.string.test_conditions)) {
        item("loading") { SettingsSwitchItem(stringResource(R.string.loading_preview), loading, { loading = it }) }
        item("enabled") { SettingsSwitchItem(stringResource(R.string.enable_examples), enabled, { enabled = it }) }
    }
    SettingsGroup(title = stringResource(R.string.status_row)) {
        item("status") {
            SettingsStatusItem(title = stringResource(R.string.update_status),
                status = stringResource(if (loading) R.string.checking else R.string.state_ready),
                summary = stringResource(R.string.status_description), loading = loading)
        }
    }
    SettingsGroup(title = stringResource(R.string.interactive_cards)) {
        item("slider") {
            SettingsSliderItem(stringResource(R.string.slider_setting),
                stringResource(R.string.slider_value, (slider * 100).toInt()), slider, { slider = it }, 0f..1f,
                summary = stringResource(R.string.slider_description), enabled = enabled,
                onValueChangeFinished = { commits++ }) {
                Text(stringResource(R.string.slider_commits, commits))
            }
        }
        item("button") {
            SettingsButtonItem(stringResource(R.string.button_setting), stringResource(R.string.run_action),
                { slotActions++ }, summary = stringResource(R.string.slot_action_count, slotActions), enabled = enabled)
        }
        item("custom") {
            SettingsContentCard {
                Row(Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TextButton({ slider = .5f }, enabled = enabled) { Text(stringResource(R.string.reset_slider)) }
                    TextButton({ slotActions++ }, enabled = enabled) { Text(stringResource(R.string.apply)) }
                }
            }
        }
    }
    SettingsActionCard(
        title = stringResource(R.string.update_card), description = stringResource(R.string.action_card_description),
        actionLabel = stringResource(if (loading) R.string.checking else R.string.check_now),
        onAction = { clicks++ }, enabled = enabled, loading = loading,
        supportingContent = { Text(pluralStringResource(R.plurals.counter_value, clicks, clicks)) }
    )
    SettingsInfoCard(stringResource(R.string.info_card), stringResource(R.string.long_description)) {
        Text(stringResource(R.string.supporting_slot))
    }
}

@Composable
private fun ColumnScope.DialogsCatalog() {
    var committed by rememberSaveable { mutableIntStateOf(0) }
    var draft by rememberSaveable { mutableIntStateOf(0) }
    var choiceOpen by rememberSaveable { mutableStateOf(false) }
    var confirmationOpen by rememberSaveable { mutableStateOf(false) }
    var acknowledged by rememberSaveable { mutableStateOf(false) }
    var confirmed by rememberSaveable { mutableIntStateOf(0) }
    SettingsGroup {
        item("choice") { SettingsActionItem(stringResource(R.string.open_choice),
            onClick = { draft = committed; choiceOpen = true },
            summary = stringResource(if (committed == 0) R.string.option_one else R.string.option_two)) }
        item("confirm") { SettingsActionItem(stringResource(R.string.open_confirmation),
            onClick = { acknowledged = false; confirmationOpen = true },
            summary = pluralStringResource(R.plurals.confirmation_count, confirmed, confirmed)) }
    }
    SettingsInfoCard(stringResource(R.string.dialog_contract), stringResource(R.string.dialog_contract_body))
    if (choiceOpen) SettingsChoiceDialog(
        title = stringResource(R.string.single_selection),
        choices = listOf(
            SettingsChoice("one", 0, stringResource(R.string.option_one), stringResource(R.string.long_description)),
            SettingsChoice("two", 1, stringResource(R.string.option_two)),
            SettingsChoice("disabled", 2, stringResource(R.string.disabled_setting), enabled = false)
        ), selectedValue = draft, onValueChange = { draft = it },
        onConfirm = { committed = draft; choiceOpen = false }, onDismissRequest = { choiceOpen = false },
        confirmLabel = stringResource(R.string.apply), dismissLabel = stringResource(R.string.cancel)
    )
    if (confirmationOpen) AlertDialog(
        onDismissRequest = { confirmationOpen = false },
        title = { Text(stringResource(R.string.open_confirmation)) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.confirmation_body))
                SettingsSwitchItem(stringResource(R.string.acknowledged), acknowledged, { acknowledged = it })
            }
        },
        confirmButton = {
            TextButton(enabled = acknowledged, onClick = {
                if (acknowledged) { confirmed++; confirmationOpen = false }
            }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = { confirmationOpen = false }) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun ColumnScope.MosaicCatalog(style: PyriteStyle) {
    var longText by rememberSaveable { mutableStateOf(false) }
    var interactive by rememberSaveable { mutableStateOf(true) }
    var attribution by rememberSaveable { mutableStateOf(true) }
    var metadata by rememberSaveable { mutableStateOf(true) }
    var checking by rememberSaveable { mutableStateOf(false) }
    var checked by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    SettingsGroup(title = stringResource(R.string.test_conditions)) {
        item("long") { SettingsSwitchItem(stringResource(R.string.long_text), longText, { longText = it }) }
        item("interactive") { SettingsSwitchItem(stringResource(R.string.interactive_metadata), interactive, { interactive = it }) }
        item("fork") { SettingsSwitchItem(stringResource(R.string.show_attribution), attribution, { attribution = it }) }
        item("metadata") { SettingsSwitchItem(stringResource(R.string.show_metadata), metadata, { metadata = it }) }
        item("checking") { SettingsSwitchItem(stringResource(R.string.loading_preview), checking, { checking = it }) }
    }
    AboutOverviewMosaic(
        info = AboutInfo(
            name = if (longText) stringResource(R.string.long_title) else "Pyrite",
            version = "Preview  v0.1.0-SNAPSHOT (1)"
        ),
        overview = AboutOverviewContent(
            channel = if (metadata) AboutDetail("Channel",
                if (longText) stringResource(R.string.long_title) else "Preview") else null,
            build = if (metadata) AboutDetail("Build",
                if (longText) stringResource(R.string.long_description) else "2026.10.18") else null,
            version = if (metadata) AboutDetail("Version", "v0.1") else null,
            attribution = if (attribution) AboutAttribution("Forked from", "Upstream project", "https://example.org/upstream") else null
        ),
        icon = { PyriteMark() },
        onVersionClick = if (interactive) { { detail = "0.1.0-SNAPSHOT" } } else null,
        onChannelClick = if (interactive) { { detail = "Preview" } } else null,
        onAttributionClick = if (interactive) { { detail = it.name + "\n" + it.url } } else null
    )
    AboutUpdateCard(
        description = stringResource(if (longText) R.string.long_description else R.string.action_card_description),
        status = stringResource(if (checking) R.string.checking else if (checked) R.string.state_ready else R.string.status_description),
        actionLabel = stringResource(if (checking) R.string.checking else R.string.check_now),
        onCheck = { checked = true }, checking = checking, hasResult = checked, enabled = interactive
    )
    AboutActionGroup(
        actions = listOf(
            AboutAction("support", stringResource(R.string.resources), stringResource(R.string.about_description),
                stringResource(R.string.resources), interactive),
            AboutAction("licenses", stringResource(R.string.integration),
                if (longText) stringResource(R.string.long_description) else stringResource(R.string.supporting_slot),
                stringResource(R.string.integration), interactive)
        ), onAction = { detail = it.title }
    )
    if (detail != null) AlertDialog(onDismissRequest = { detail = null },
        title = { Text(stringResource(R.string.build_info)) }, text = { Text(detail.orEmpty()) },
        confirmButton = { TextButton(onClick = { detail = null }) { Text(stringResource(R.string.close)) } })
}

@Composable
private fun ColumnScope.ScreenPreview(onAbout: () -> Unit) {
    var automatic by rememberSaveable { mutableStateOf(true) }
    var armed by rememberSaveable { mutableStateOf(false) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    SettingsInfoCard(stringResource(R.string.screen_preview_title), stringResource(R.string.screen_preview_body))
    SettingsGroup(title = stringResource(R.string.maintenance), modifier = Modifier.animateContentSize()) {
        item("automatic") {
            SettingsSwitchItem(stringResource(R.string.auto_maintain), automatic, { automatic = it },
                supportingContent = {
                    Column {
                        Text(stringResource(R.string.rich_summary))
                        Text(stringResource(if (automatic && armed) R.string.state_active else R.string.state_paused),
                            color = MaterialTheme.colorScheme.primary)
                    }
                })
        }
        item("armed") {
            SettingsSwitchItem(stringResource(R.string.environment_ready), armed, { armed = it },
                enabled = automatic)
        }
        item("show") {
            SettingsSwitchItem(stringResource(R.string.show_advanced), showAdvanced, { showAdvanced = it })
        }
        if (showAdvanced) item("advanced") {
            SettingsSwitchItem(stringResource(R.string.guarded_change), advanced,
                onCheckedChange = { if (it) pending = true else advanced = false },
                summary = stringResource(R.string.guarded_summary))
        }
    }
    SettingsGroup(title = stringResource(R.string.single_selection), modifier = Modifier.selectableGroup()) {
        item("one") { SettingsRadioItem(stringResource(R.string.option_one), selected == 0, { selected = 0 }) }
        item("two") { SettingsRadioItem(stringResource(R.string.option_two), selected == 1, { selected = 1 }) }
    }
    SettingsGroup(title = stringResource(R.string.explore)) {
        item("about") { SettingsActionItem(stringResource(R.string.about), onAbout,
            summary = stringResource(R.string.about_summary)) }
    }
    if (pending) AlertDialog(onDismissRequest = { pending = false },
        title = { Text(stringResource(R.string.guarded_change)) },
        text = { Text(stringResource(R.string.confirmation_body)) },
        confirmButton = { TextButton(onClick = { advanced = true; pending = false }) {
            Text(stringResource(R.string.confirm))
        } },
        dismissButton = { TextButton(onClick = { pending = false }) { Text(stringResource(R.string.cancel)) } })
}
