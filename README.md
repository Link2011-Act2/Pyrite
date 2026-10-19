# Project Pyrite - Fool's Material 3

アプリを作りすぎて面倒になったので、共通のUIをライブラリ化しました

名前の由来は
- GoogleとかNothingの純正品をわざわざ再実装した偽物である
- 愚者が作ったOSS
- 愚者しか使わないであろうOSS
- トリッカル ピラ

あたりから来てます

以下、真面目なREADME

複数のAndroidアプリで共通利用する、Jetpack Composeの設定画面・About画面ライブラリです。

- Android Library `:pyrite`：状態を外部から渡す設定UIとAbout表示。
- UI検証カタログ `:app`：部品別の操作例、画面プレビュー、M3E／Nothing風の2スタイル、明暗、文字拡大と狭幅。
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

## JitPackによる利用

`jitpack.yml`でJDK 21・SDK 37と既存のMaven Publishを使用する構成を用意しています。公開座標の形式は`com.github.Link2011-Act2:Pyrite:<revision>`です。公開対象はライブラリAAR一つです。

**JitPackの実ビルドと独立consumerの取得・ビルドに成功しました（2026-10-18）。** 検証済みの固定commitは`4244bed7b36aa084c89d14338f176dbf87c0f263`です。タグ・GitHub Releaseは作成していません。

利用側の`settings.gradle.kts`の`repositories`へ追加：

```kotlin
maven {
    url = uri("https://jitpack.io")
    content { includeGroup("com.github.Link2011-Act2") }
}
```

`app/build.gradle.kts`：

```kotlin
implementation("com.github.Link2011-Act2:Pyrite:<current-api-commit-sha>")
```

公開確認済み4244bedは初期APIの証跡です。上のプレースホルダーには、現行APIを含むcommitを公開後にその固定SHAを指定してください。今回のAPI整理を含む変更はソース／ローカルMaven版で検証してください。

利用アプリ側でPyriteのソースを取得する必要はありません。SDK・Compose Compilerの要件は上記を参照してください。

```powershell
# 専用ローカルMavenでJitPack座標とPOMの推移依存を検証
.\scripts/verify-jitpack.ps1
# 公開反映後、固定commit SHAまたは承認済み0.x.xタグで取得・ビルド
.\scripts/verify-jitpack.ps1 -Revision '<current-api-commit-sha>'
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

## 補足情報・状態・アクション

以下の追加APIと標準Aboutモザイクは現在のソース／ローカルMaven版です。上記のJitPack検証済みSHAにはまだ含まれません。利用アプリへの導入は新しい固定SHAの公開・取得確認後に行います。

```kotlin
SettingsSwitchItem(
    title = "自動維持",
    checked = enabled,
    onCheckedChange = onEnabledChange,
    supportingContent = {
        Column {
            Text("設定と実際の稼働状態を別々に表示します")
            Text(if (armed) "稼働中" else "一時停止中")
        }
    }
)
SettingsStatusItem("更新", status = statusText, loading = checking)

SettingsActionCard(
    title = "更新を確認",
    description = "処理と結果はアプリが管理します",
    actionLabel = if (checking) "確認中…" else "今すぐ確認",
    onAction = onCheck,
    loading = checking
)
```

カードのimportは `jp.linkserver.pyrite.card.*` です。SettingsInfoItemは操作のない情報、SettingsInfoCardは説明と追加contentを表示します。Action/Switch/Radio行には既存のString summary版を維持し、補足Composable用のoverloadを追加しています。補足slotに親と競合する操作対象を入れないでください。

AboutOverviewMosaicとAboutOverviewContentでChannel / Build / Versionを明示し、任意のForked fromを表示できます。元の92dpアイコン・48dpブリッジを維持します。情報は名前付きのchannel/build/versionへ渡し、未設定のフィールドは省略します。Build/Versionの操作は右カード共通のonVersionClickとなります。[元UIとAPIの差分](docs/source-port.md)を確認してください。

## UI検証アプリ

既存の :app は、設定行／カード／ダイアログ／Aboutモザイク／画面プレビュー／公式コントロールを操作できるカタログです。カテゴリ内の右上でテーマ・明暗を切り替え、最初の設定画面で150%文字・360dp幅を選べます。状態は画面移動・テーマ切替・Activity再生成後も保持します。

Pyriteが形状・余白・文字階層を管理します。アプリ別の意匠指定を増やさず、SchedulerのM3E／Glyph BartyのNothing風UIを基準に忠実に共通化します。元コード・差分は [移植記録](docs/source-port.md) を参照してください。検証条件と全公開UIの対応表は [カタログガイド](docs/catalog.md)、ShutUpの振る舞いを保つ手順は [移行計画](docs/shutup-migration-plan.md) を参照してください。

## About画面

```kotlin
AboutScreen(
    title = "アプリについて",
    info = AboutInfo(name = "My App", version = "Beta  v1.2.3 (123)"),
    overview = AboutOverviewContent(
        channel = AboutDetail("Channel", "Beta"),
        build = AboutDetail("Build", "123"),
        version = AboutDetail("Version", "v1.2"),
        attribution = AboutAttribution("Forked from", "Upstream", "https://example.com/upstream")
    ),
    links = listOf(AboutLink("website", "Webサイト", "https://example.com")),
    onLinkClick = { openUrlInHost(it.url) },
    onAttributionClick = { openUrlInHost(it.url) },
    navigationIcon = { /* アプリ側の戻るボタン */ },
    icon = { /* 92dpの丸い領域を満たすアプリアイコン */ }
) {
    AboutUpdateCard(
        description = "アプリの更新情報を確認します",
        status = statusText,
        actionLabel = if (checking) "確認中…" else "更新を確認",
        onCheck = onCheck,
        checking = checking,
        hasResult = hasResult
    )
}
```

`jp.linkserver.pyrite.about.*`をimportします。`AboutOverviewMosaic`だけを既存画面へ配置することもできます。アプリ名・バージョン取得、更新チェック、URL起動、エラー処理をPyriteは行いません。

階層型設定にはSettingsMasterSwitchとSettingsSplitSwitchItemを利用できます。背景色はON/OFFで固定し、下位のenabledはホストが渡します。[API例と参考画像との差分](docs/hierarchical-settings.md)を参照してください。

## テーマとアクセシビリティ

`PyriteStyle.Material3Expressive` / `PyriteStyle.Nothing`を選択できます。

M3Eは公式MaterialExpressiveTheme、MotionScheme、Switchを使用し、Scheduler由来の22/6dp連結行・Check/Closeの意匠を維持します。Nothing風はGlyph由来の白黒・赤、通常ウェイト、連結カードです。SegmentedListItemは元の形状・余白を変えるため設定行には使いません。

- `darkTheme`の明示指定とダイナミックカラーに対応します。Nothing風ではダイナミックカラーを使用しません。
- 色・形状・文字はPyrite標準値を使います。テーマ入力はstyle、darkTheme、dynamicColorです。
- Nothing風はシステム書体を使い、専有フォントは同梱していません。OSの文字拡大に対応します。
- 設定値・テーマ選択はホストで保持します。テーマの変更でライブラリが設定を初期化することはありません。
- 固定の行高に依存せず、文言を折り返します。Aboutは狭幅・大きい文字で縦並びに切り替わります。
- 見出し、Switch/Radioの操作属性、無効状態を設定します。
- サンプルは画面フェード・展開アニメーションと公式部品の状態遷移を実演します。

TalkBackによる読み上げ全体と多様な端末・フォントでの確認は、各アプリへの導入時にも実施してください。

## 公開API

| パッケージ | API |
| --- | --- |
| `theme` | `PyriteStyle`, `PyriteTheme` |
| `settings` | `SettingsScreen`, `SettingsGroup`, `SettingsGroupScope.item`, `SettingsActionItem`, `SettingsSwitchItem`, `SettingsRadioItem`, `SettingsInfoItem`, `SettingsStatusItem`, `SettingsNavigationItem`, `SettingsRadioGroupItem`, `SettingsMasterSwitch`, `SettingsSplitSwitchItem` |
| `card` | `SettingsInfoCard`, `SettingsActionCard` |
| `dialog` | `SettingsChoice<T>`, `SettingsChoiceDialog<T>` |
| `about` | `AboutInfo`, `AboutDetail`, `AboutLink`, `AboutScreen`, `AboutOverviewMosaic`, `AboutOverviewContent`, `AboutAttribution`, `AboutAction`, `AboutActionGroup`, `AboutUpdateCard` |

通常のButton、Card、Slider、Switch、LoadingIndicator等を再公開する独自ラッパーは用意していません。[APIの使い分けと削除対応表](docs/public-api.md)を参照してください。

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
2. 展開可能な設定セクション（分割Switch行・マスターSwitchは実装済み）。
3. データ外部指定のOSS一覧。アクション・状態カードは実装済みです。
4. 公式FABメニュー・ツールバーとの接続例、状態モーフの薄い構成。
5. Markdownなど追加依存が必要な表示の採否。

公式APIで十分なものはサンプルや導入例で示し、独自公開APIを増やしません。詳細な公式API採否は`docs/design.md`を参照してください。

## ライセンス

MIT。設計参照元と帰属は`NOTICE.md`をご覧ください。NothingのNdot/NTypeフォント、アプリのロゴ、各アプリ固有SDK・サービスは同梱しません。
