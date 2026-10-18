package jp.linkserver.projectpyrite

/** Catalog-only ordering. The reusable transition knows neither these screens nor their hierarchy. */
internal enum class CatalogPage(val title: Int, val description: Int, val depth: Int) {
    Settings(R.string.catalog_title, R.string.catalog_intro, 0),
    Components(R.string.components, R.string.catalog_index_intro, 1),
    Rows(R.string.catalog_rows, R.string.rows_description, 2),
    Cards(R.string.catalog_cards, R.string.cards_description, 2),
    Dialogs(R.string.catalog_dialogs, R.string.dialogs_description, 2),
    Mosaic(R.string.catalog_mosaic, R.string.mosaic_description, 2),
    Screens(R.string.catalog_screens, R.string.screens_description, 2),
    Hierarchy(R.string.catalog_hierarchy, R.string.hierarchy_description, 2),
    HierarchyDetails(R.string.autocorrection, R.string.hierarchy_description, 3),
    Official(R.string.catalog_official, R.string.official_components, 2),
    About(R.string.about, R.string.about_description, 3),
    AboutDetails(R.string.build_info, R.string.navigation_details_intro, 4);

    companion object {
        val categories = listOf(Rows, Cards, Dialogs, Mosaic, Screens, Hierarchy, Official)
    }
}
