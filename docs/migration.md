# 既存アプリへの導入計画

既存のGlyph Barty・NITTC Scheduler・ShutUpは変更していません。この文書は、各アプリ側の変更許可を得た後の手順です。

## ビルドの前提

PyriteはMaterial 3 1.5.0-beta01 / Compose 1.12.0を使用します。依存関係にはcompileSdk 37 / AGP 9.1.0以上が必要です。利用側の最終的な依存解決結果を確認し、古いComposeやMaterial 3への強制固定を解消します。今回の独立consumer検証はAGP 9.4.1で実施します。

| アプリ | 現在のcompileSdk | 導入時の検討 |
| --- | --- | --- |
| Glyph Barty | 36.1 | compileSdk 37、Compose/M3依存整合、Nothing風テーマ接続 |
| NITTC Scheduler | 37 | beta01へのAPI互換性確認。未コミットの設定UI調整を保持 |
| ShutUp | 36 | compileSdk 37、M3 alpha10からbeta01への依存・API整合 |

targetSdkの変更はこの導入計画に含めません。Kotlin/Compose Compiler/AGPの組み合わせは利用側でもビルド確認します。

## 置き換える範囲

1. Maven座標を追加し、最小の設定画面で依存整合を確認。
2. ホストが所有するテーマ状態からPyriteStyleとdarkThemeを渡す。
3. 設定項目をSettingsGroupとSettingsActionItem/SettingsSwitchItem/SettingsRadioItemへ置換。
4. アプリが取得した情報をAboutInfo/Detail/Linkに変換し、About表示を置換。
5. 設定キー・保存形式・権限・通知・サービス・更新・ログ機能を維持して回帰確認。

| 元の実装 | Pyriteの候補 |
| --- | --- |
| Glyph SettingsEntry / SettingsToggleEntry | SettingsActionItem / SettingsSwitchItem |
| Glyph SettingsRadioEntry | 型付き選択データとSettingsChoiceDialog、またはSettingsRadioItem |
| Scheduler AppSettingsSwitchItem / AppSettingsNavigationItem | SettingsSwitchItem / SettingsActionItem |
| ShutUpの繰り返されるSwitch設定行 | SettingsSwitchItem |
| 各アプリのAboutモザイク | AboutOverview、またはAboutScreen全体の表示部分 |

単なるButton/Card/Slider等は公式APIを維持します。遷移とSwitchが独立した複合行は初期版にないため、既存実装を残してください。画面全体を機械的にコピー・置換しないでください。

## beta01固有の注意

公式配布ソースで必要なAPIを照合しています。beta01はalpha25のソース互換性を一律に保証するものではありません。

- Pyriteはbeta01のSegmentedListItem / segmentedShapes / MotionSchemeを使用。
- Sliderのサンプルは、旧value引数版を避け、SliderStateとonValueChangeを使う公開APIへ更新。
- Typographyのフォント指定はfontFamily引数。
- beta01ソースには公開SplitButtonLayoutがあります。SchedulerのSplitButton呼び出しは、アプリ導入時に採用版のAPIへ合わせて照合・調整が必要です。
- ButtonGroup、FAB menu、floating toolbar、LoadingIndicator、MaterialShapes/toPath/toShapeはbeta01の公開ソースで存在を確認済みです。初期版のPyrite独自APIにはせず、公式APIを必要に応じて直接利用します。

既存アプリ自体のbeta01移行ビルドは、変更禁止のため実行していません。

## 保持するもの

GlyphのSettingsPreferencesのキー、SchedulerのRoom・同期・通知・カレンダー、ShutUpのViewModel・Shizuku・端末固有処理はアプリ側に残します。URL起動、PackageManager/BuildConfig、更新チェック、OSS一覧生成もホストの責務です。

Nothing所有のNdot/NTypeフォントはPyriteへコピーしません。既存アプリでの取り扱いは別途確認事項とし、今回の作業で既存ファイルを変更しません。

既存のSettingsScreen/AboutScreenと名前が衝突する場合は、import aliasで呼び分けます。

```kotlin
import jp.linkserver.pyrite.settings.SettingsScreen as PyriteSettingsScreen
import jp.linkserver.pyrite.about.AboutScreen as PyriteAboutScreen
```
