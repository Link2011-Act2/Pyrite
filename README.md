# Project Pyrite - Fool's Material 3
複数のAndroidアプリで共通利用する、Jetpack Composeの設定画面・About画面ライブラリです。

- Android Library `:pyrite`：状態を外部から渡す設定UIとAbout表示。
- サンプル `:app`：M3 / M3 Expressive / Nothing風、ライト・ダーク、文字拡大、画面遷移。
- 独立した利用検証プロジェクト `verification/consumer`：発行済みMavenアーティファクトのみを参照。

Material 3の基本部品は直接利用してください。Pyriteは、設定行の操作・グループ構造・About構成など、アプリをまたぐ価値のある組み合わせを提供します。

## ビルド要件

| 項目 | 基準 |
| --- | --- |
| minSdk | 26 |
| compileSdk | 37 |
| AGP | 9.4.1（Pyriteの配布AARメタデータも確認してください） |
| Gradle | 9.6.0 |
| ビルド用JDK | 21 |
| Android向けJavaバイトコード | 11 |
| Material 3 | **1.5.0-beta01** |
| Compose UI / Foundation / Animation | **1.12.0** |
| Compose Compiler plugin | 2.2.10（AGP組み込みKotlinと同系列） |

Material 3 beta01の公式AAR・ソース・POMを確認しています。Composeのバージョンは同POMに揃えて明示しています。Material 3はベータ版です。利用側でも解決後の依存版を確認してください。古いComposeへ強制的にダウングレードする構成はサポート対象外です。

JDK 21とAndroid SDK Platform 37を用意してください。`JAVA_HOME`はJDKを指す必要があります。SDKパスは通常の`local.properties`または`ANDROID_HOME`で指定します。

```powershell
.\gradlew.bat :pyrite:assembleRelease :app:assembleDebug
```

出力：

- `pyrite/build/outputs/aar/pyrite-release.aar`
- `app/build/outputs/apk/debug/app-debug.apk`

## ローカルMavenへの発行・利用

```powershell
.\gradlew.bat :pyrite:publishReleasePublicationToPyriteLocalRepository
```

`build/repository`に次の座標を発行します。

```kotlin
implementation("jp.linkserver:pyrite:0.1.0-SNAPSHOT")
```

AAR、ソースJAR、POM、Gradle Module Metadataを発行します。SNAPSHOTファイルにはタイムスタンプが付与され、Mavenメタデータから解決されます。

利用する独立プロジェクトの`settings.gradle.kts`：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("D:/apps/pyrite/build/repository")
            content { includeGroup("jp.linkserver") }
        }
        google()
        mavenCentral()
    }
}
```

利用側でもComposeを有効にし、対応するCompose Compiler pluginを適用してください。公開APIに必要なCompose UI / Foundation / Material 3は推移依存として提供します。ActivityやNavigationはアプリで選択します。

AARファイルだけをコピーする方法では推移依存情報が失われるため、Mavenリポジトリからの利用を推奨します。

独立プロジェクトでの検証：

```powershell
# verification/consumerにもSDKパスを指定してから実行
.\gradlew.bat -p verification/consumer :app:assembleDebug
# カタログ全体を発行済みAARでビルド
.\gradlew.bat :app:assembleDebug '-Ppyrite.usePublishedArtifact=true'
```

発行・独立consumer・カタログの検証をまとめる場合：

```powershell
.\scripts/verify-published.ps1
```

標準の`:pyrite:publishToMavenLocal`も利用できます。上記の検証はユーザー共通の`~/.m2`を変更せず、プロジェクト内のMavenリポジトリを使います。JitPack向けの配布設定は下記を参照してください。

## JitPackによる利用（配布準備）

`jitpack.yml`でJDK 21・SDK 37と既存のMaven Publishを使用する構成を用意しています。想定座標は`com.github.Link2011-Act2:Pyrite:<revision>`です。公開対象はライブラリAAR一つです。

**JitPack実サーバーからの取得は未検証です。** 設定を含むcommitのGitHub反映と、JitPackからのソースへのアクセスが必要です。タグ・GitHub Release・公開範囲の変更は行っていません。

```powershell
# 専用ローカルMavenでJitPack座標とPOMの推移依存を検証
.\scripts/verify-jitpack.ps1
# 公開反映後、固定commit SHAまたは承認済み0.x.xタグで取得・ビルド
.\scripts/verify-jitpack.ps1 -Revision '<公開済みrevision>'
```

利用側のリポジトリ設定、公開前の条件、クリーンビルドの手順は[配布ドキュメント](docs/distribution.md)を参照してください。[検証結果](docs/verification.md)に実施済み・未検証の範囲を記載しています。

## 設定画面

```kotlin
var enabled by rememberSaveable { mutableStateOf(true) }

PyriteTheme(
    style = PyriteStyle.Material3Expressive,
    darkTheme = isSystemInDarkTheme()
) {
    SettingsScreen(title = "設定") {
        SettingsGroup(title = "一般") {
            item("notifications") {
                SettingsSwitchItem(
                    title = "通知",
                    summary = "通知の受信を許可します",
                    checked = enabled,
                    onCheckedChange = { enabled = it }
                )
            }
            item("about") {
                SettingsActionItem(
                    title = "アプリについて",
                    onClick = { /* アプリ側で画面遷移 */ }
                )
            }
        }
    }
}
```

主なimport：

```kotlin
import jp.linkserver.pyrite.theme.*
import jp.linkserver.pyrite.settings.*
```

- `SettingsGroup.item(key)`のキーはグループ内で一意にしてください。非表示項目は`item`自体を条件分岐で省略します。
- 行の先頭・末尾位置は、表示されている項目から自動計算します。
- Switch行全体が1つの操作・読み上げ対象になります。子Switchに別のcallbackを追加しないでください。
- `SettingsRadioItem`を並べる場合は親に`Modifier.selectableGroup()`を付けてください。
- 文言は呼び出し側でローカライズします。保存・権限・業務判断はライブラリに含みません。
- 設定画面はスクロールと最大幅を管理します。非常に大量の項目を扱う仮想化リストは初期版の対象外です。

## 選択ダイアログ

`SettingsChoiceDialog<T>`は公式`AlertDialog`に設定用の単一選択を組み合わせたものです。

```kotlin
var draft by rememberSaveable { mutableStateOf(currentValue) }

SettingsChoiceDialog(
    title = "テーマ",
    choices = listOf(
        SettingsChoice("light", "light", "ライト"),
        SettingsChoice("dark", "dark", "ダーク")
    ),
    selectedValue = draft,
    onValueChange = { draft = it },
    onConfirm = { saveValue(draft); closeDialog() },
    onDismissRequest = { closeDialog() },
    confirmLabel = "適用",
    dismissLabel = "キャンセル"
)
```

ダイアログを開くたびに呼び出し側でdraftを初期化してください。ライブラリは値を保存せず、取消時にアプリの値を巻き戻す処理も行いません。

## About画面

```kotlin
AboutScreen(
    title = "アプリについて",
    info = AboutInfo(
        name = "My App",
        version = "1.2.3",
        description = "アプリの説明",
        details = listOf(
            AboutDetail("build", "ビルド", "123"),
            AboutDetail("channel", "チャンネル", "Beta")
        )
    ),
    linksTitle = "関連リンク",
    links = listOf(
        AboutLink("website", "Webサイト", "https://example.com")
    ),
    onLinkClick = { link -> openUrlInHost(link.url) },
    navigationIcon = { /* アプリ側の戻るボタン */ },
    icon = { /* アプリ側の画像・ロゴ */ }
) {
    // 更新状態や独自セクションなど、アプリが管理する表示
}
```

`jp.linkserver.pyrite.about.*`をimportします。`AboutOverview`だけを既存画面へ配置することもできます。アプリ名・バージョン取得、更新チェック、URL起動、エラー処理をPyriteは行いません。

## テーマとアクセシビリティ

`PyriteStyle.Material3` / `Material3Expressive` / `Nothing`を選択できます。

M3Eは公式`MaterialExpressiveTheme`、`MotionScheme`、`SegmentedListItem`、`ListItemDefaults.segmentedShapes`を使用します。通常M3も公式の操作可能な`ListItem`を使用します。Nothing風では白黒・赤、22dp/6dpの連結角丸、密なカード配置を維持します。

- `darkTheme`の明示指定とダイナミックカラーに対応します。Nothing風ではダイナミックカラーを使用しません。
- `colorScheme`、`typography`、`shapes`を外部指定できます。
- `nothingFontFamily`でホストが許諾を得た書体を指定できます。専有フォントは同梱していません。
- 設定値・テーマ選択はホストで保持します。テーマの変更でライブラリが設定を初期化することはありません。
- 固定の行高に依存せず、文言を折り返します。Aboutは狭幅・大きい文字で縦並びに切り替わります。
- 見出し、Switch/Radioの操作属性、無効状態を設定します。
- サンプルは画面フェード・展開アニメーションと公式部品の状態遷移を実演します。

TalkBackによる読み上げ全体と多様な端末・フォントでの確認は、各アプリへの導入時にも実施してください。

## 公開API

| パッケージ | API |
| --- | --- |
| `theme` | `PyriteStyle`, `PyriteTheme` |
| `settings` | `SettingsScreen`, `SettingsGroup`, `SettingsGroupScope.item`, `SettingsActionItem`, `SettingsSwitchItem`, `SettingsRadioItem` |
| `dialog` | `SettingsChoice<T>`, `SettingsChoiceDialog<T>` |
| `about` | `AboutInfo`, `AboutDetail`, `AboutLink`, `AboutScreen`, `AboutOverview` |

通常のButton、Card、Slider、Switch、LoadingIndicator等を再公開する独自ラッパーは用意していません。

## 検証

```powershell
.\gradlew.bat :pyrite:testDebugUnitTest :pyrite:lintDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

実行結果と検証範囲は`docs/verification.md`に記録します。接続先が複数ある場合、`ANDROID_SERIAL`でテスト対象を限定してください。

## 既存アプリへの導入・今後の範囲

Glyph Barty、NITTC Scheduler、ShutUpは参照のみで、変更していません。導入時は依存整合→テーマ接続→設定行→Aboutの順に置き換え、既存の設定キー、ViewModel、権限・サービス・更新処理を維持します。

今回の基盤以降の候補：

1. 設定の数値・文字・時刻編集、検証・エラー表示。
2. 遷移とSwitchが独立した複合設定行、展開可能な設定セクション。
3. データ外部指定のOSS一覧、汎用アクション・状態カード。
4. 公式FABメニュー・ツールバーとの接続例、状態モーフの薄い構成。
5. Markdownなど追加依存が必要な表示の採否。

公式APIで十分なものはサンプルや導入例で示し、独自公開APIを増やしません。詳細な公式API採否は`docs/design.md`を参照してください。

## ライセンス

MIT。設計参照元と帰属は`NOTICE.md`をご覧ください。NothingのNdot/NTypeフォント、アプリのロゴ、各アプリ固有SDK・サービスは同梱しません。
