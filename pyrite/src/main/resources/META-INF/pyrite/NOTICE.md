# Pyrite attribution

Pyrite is licensed under the MIT License; see LICENSE.

The UI patterns and component structure were researched in the following repositories, all carrying MIT License / Copyright (c) 2026 Azul1nk:

- Glyph Barty, glyphvisualizer, reference HEAD 53e568c:
  SettingsListComponents.kt, SettingsScreen.kt, AboutScreen.kt, ui/theme.
- NITTC Scheduler, schedulernittc, reference HEAD 26913ba plus existing uncommitted settings UI changes:
  ui/components/AppSettingsComponents.kt, AppComponents.kt, AboutOverviewMosaic.kt.
- ShutUp, shutup, reference HEAD 34241e9:
  SettingsComponents.kt, AboutOverview.kt, AboutScreen.kt, Theme.kt.

Pyrite reorganizes these shared patterns into host-controlled APIs and uses Google-provided Material 3 APIs for standard Material components. Original app repositories were not modified.

Nothing-owned Ndot/NType font files are NOT included. Their embedded license terms are not treated as MIT. Pyrite supports host-supplied FontFamily values and uses standard system fonts by default. Nothing is referenced only as a visual inspiration; no official affiliation is claimed.

App-specific logos, Glyph SDK binaries, Shizuku/Hidden API integrations, app update/download logic, and Markwon are not bundled in Pyrite.

AndroidX Compose and Material 3 are third-party dependencies under their respective licenses (primarily Apache License 2.0). They are referenced through Maven, not vendored source.
