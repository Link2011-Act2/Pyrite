# 公開API（0.1 開発版）

過去のPyrite APIとの互換性は維持しません。実装・使用例・consumerは以下のAPIを使います。パッケージ別の全一覧は[README](../README.md#公開api)、操作例は[カタログ](catalog.md)を参照してください。

## 削除・統合したAPI

| 削除したAPI | 現在の指定方法 |
| --- | --- |
| SettingsSwitchNavigationItem | SettingsSplitSwitchItem。実体を改名し、委譲用の別名を削除 |
| AboutInfo.details | AboutOverviewContent(channel, build, version, attribution)。省略する情報はnull |
| AboutDetail.key | AboutDetail(label, value)。位置はOverviewContentのプロパティ名で明示 |
| 順序付きdetailsを受けるAboutScreen | overviewを明示する単一のAboutScreen |
| AboutOverview | AboutOverviewMosaic(info, overview, …) |
| overviewなしのAboutOverviewMosaic / onDetailClick | overviewを明示し、onChannelClick / onVersionClickを指定 |
| PyriteTheme.colorScheme / typography / shapes / nothingFontFamily | Pyrite標準値。style / darkTheme / dynamicColorのみを指定 |
| 通常Material 3の独立スタイル | Material3ExpressiveまたはNothing（独立スタイル廃止は前回実施済み） |

detailsの位置から意味を推測する内部adapter、4件目以降を自動描画する互換経路も削除しました。追加情報はAboutScreenのcontentへ明示します。描画補助SourceCardとRowSurfaceはprivate、共有のグループ位置・寸法・テーマ資源はinternalです。未使用の寸法定数を削除し、連結角丸と説明文の文字設定の重複を整理しました。

## 残したAPIと役割

| API | 維持する理由 |
| --- | --- |
| SettingsActionItem / SwitchItem / RadioItemのsummary版とsupportingContent版 | 短い説明文字列と、複数の補足情報を持つ内容の違い。互換用の別名ではない |
| SettingsSwitchItem / MasterSwitch / SplitSwitchItem | 通常の単一操作、画面全体のマスター、独立した遷移＋切替の3種類 |
| SettingsRadioItem / RadioGroupItem / ChoiceDialog | 1行の選択、カード内の複数候補、確定・取消を伴う下書き選択 |
| SettingsInfoItem / StatusItem | 静的情報と、進行状況・Polite通知を持つ状態表示 |
| SettingsInfoCard / ActionCard | 説明・補足contentと進行中の設定操作。About専用の連結・更新レイアウトとは役割が異なる |
| AboutScreen / AboutOverviewMosaic | 専用Scaffold付き画面と、既存画面に埋め込む単体モザイク |
| AboutInfo.version / AboutOverviewContent.version | 上段のホスト整形済みリリース文字列と、下段のラベル付き短縮版情報 |
| AboutLink / AboutAction | URL付き資料リンクと、URLを必要としない汎用操作。URLを開く処理もホストが担当 |
| AboutScreen.content / icon / navigationIconなどのslot | 追加情報・アプリアイコン・画面遷移のための実用的な内容入力 |
| SettingsScreen.scrollState / maxContentWidth / Modifier | スクロール位置の外部管理と、埋め込み先の幅・配置。既存のレスポンシブ用途を維持 |

連結形状、余白、文字階層、アニメーション方針はPyriteで管理します。slotに独立した操作を無秩序に追加せず、分割行・アクションカードを利用してください。

## Aboutの入力

```kotlin
AboutScreen(
    info = AboutInfo("My App", "Beta v1.2.3 (123)"),
    overview = AboutOverviewContent(
        channel = AboutDetail("Channel", "Beta"),
        build = AboutDetail("Build", "123"),
        version = AboutDetail("Version", "v1.2"),
        attribution = AboutAttribution("Forked from", "Upstream", "https://example.org")
    ),
    links = listOf(AboutLink("source", "Source", "https://example.org")),
    onLinkClick = { openUrlInHost(it.url) },
    onAttributionClick = { openUrlInHost(it.url) }
)
```

各コールバックはnullableで、未指定のカードは操作対象になりません。onVersionClickは上段と右下のBuild/Versionカード、onChannelClickは左下、onAttributionClickは上段の帰属リンクです。アプリ識別情報の取得・版の整形・設定保存・遷移・通信はホストで行います。

PyriteThemeの公式テーマ間を移動する処理は互換adapterではなく、テーマ切替時のcompositionと最新callbackを保つために必要です。Nothingで利用するMaterialThemeも公式の基盤として維持します。独立した通常M3スタイルを復活させるものではありません。

## 配布・検証

依存版とMaven/JitPack構成は変更していません。独立consumerもこのAPIへ移行しています。現在の変更を含まない過去のJitPack SHAに対する互換性は提供しません。リモートで新APIを使うには、変更を公開後にその固定SHAを指定します。結果と未検証範囲は[verification.md](verification.md)を参照してください。

## 画面の共通構造

screenパッケージにPyriteScreenScaffold / PyriteBackButton / PyriteScreenTransition / PyriteNavigationDirectionを追加しました。元のShutUp Scaffold・遷移を共有するAPIで、Navigationや画面名はホストが管理します。設定/Aboutの内容slotはそのまま利用できます。[元実装との差分・使用例・状態保持](navigation.md)を参照してください。
