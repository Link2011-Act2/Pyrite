# 検証結果

## 最新：公開API整理（2026-10-18）

互換APIと順序依存のAbout adapterを削除しました。[削除対応表・維持理由・現行使用例](public-api.md)を参照してください。通常M3は独立スタイルに戻していません。標準の形状・余白・文字・状態管理方針、依存版とMaven/JitPack設定は維持しています。

| 検証 | 結果 |
| --- | --- |
| release AAR / カタログdebug APK / androidTest APK | 成功 |
| 既存JVMテスト | 1件成功 |
| ライブラリrelease Lint | 問題なし |
| カタログdebug Lint（テスト修正後も実行） | エラー0、依存更新通知9件 |
| 通常ローカルMaven | 最終AAR発行、新APIの独立consumer、Maven版カタログのビルド成功 |
| JitPack座標のローカルPOM-only consumer | clean / no-build-cache / refresh-dependenciesで成功。Pyriteソースを含まない独立ディレクトリで38タスク実行 |
| 公開API | javapで旧名・旧Aboutプロパティ・互換オーバーロードの削除を確認。実装・カタログ・テスト・consumerの旧API参照0件 |
| 公開物 | MIT / NOTICE同梱。sourcesは13 Kotlinファイル、テストfixture混入0件。専有フォントなし |
| 変更前後の見た目 | 元UI比較用のPyrite画像14枚すべてが前回のPNGハッシュと一致 |

AAR SHA-256：`2F9B062E998099E92AF6B7F2C2CA42617DB4FCBA642E0997A5A9C26F9BDEAA49`。最終通常Maven、JitPack座標のローカル発行、release出力のAARは一致しています。

### 対象を絞ったUI検証

API 35の読み取り専用Pixel_6a AVD、720×1280px / density 240で、**14種類が成功（分割実行）**しました。

- SourceParityTest 6件：元UI画像、分割操作の有効条件、任意情報、Radio、帰属リンクの独立操作、新しいAboutScreenの明示メタ情報・補足slot・URL callback。
- MasterSwitchTest 2件：4テーマの固定背景・下位値/disabled・単一callback、幅280dp/文字200%。
- PyriteComponentsTest 2件：スタイル/明暗切替時のホスト状態、幅280dp/文字200%のAbout（4テーマ）。
- PyriteExtendedComponentsTest 2件：ChannelとVersionの独立操作、テーマ境界でのローカル状態と最新callback。
- CatalogFlowTest 2件：設定→About→部品とActivity再生成、階層画面の共有状態・テーマ切替・再生成。

初回14件は13件成功、マスター背景の待機1件が失敗しました。前のタップのリップルが残ったテーマ切替直後の画像を基準にしていたためです。失敗時のダークON画像はprimary `#8DD3EE`に対し`#95D6EF`で、一時的な合成色でした。ONとOFFの両方で実際のテーマprimaryに収束してから比較するよう修正し、失敗した1件だけを再実行して成功しました（32.578秒）。色の比較精度やcallback/disabledの検証は緩めていません。ライブラリの背景・描画実装は変更していません。

テーマ上書き削除・Nothing typographyの固定化・説明文style共通化が表示に影響しないことを、既存の元UI比較テスト1件（14表示条件）で再確認しました。12条件は元fixtureと完全一致、Nothing Aboutの明暗2条件だけ既知の35px差（約0.012%）です。さらに整理前後のPyrite PNG全14枚は完全一致しました。画像比較以外の成功済みテスト全件は再実行していません。

### 証跡と残る範囲

- build/api-cleanup-build.txt：AAR / APK / unit / Lint。
- build/api-cleanup-final-maven.txt：最終発行 / 独立consumer / Maven版カタログ。
- build/api-cleanup-jitpack-local.txt：JitPack座標 / POM-only clean consumerと推移依存。
- build/api-cleanup-final-checks.txt、api-cleanup-ui-recheck-build.txt：配布版テストAPK / Lint。
- build/api-cleanup-ui.txt：初回14件（13成功、待機1失敗）。
- build/api-cleanup-ui-recheck.txt：待機修正後の1件成功。
- build/api-cleanup-signatures.txt：公開シグネチャ。
- build/verification/api-cleanup-ui/PyriteVerification/：元UI比較・文字拡大・初回マスター画像。
- build/verification/api-cleanup-recheck/PyriteVerification/：修正後のマスター4テーマON/OFF画像。

新APIを含むcommitのリモートJitPack取得は未実施です。今回の変更は未コミット・未Pushで、タグ・Release・Actions・自動実行は変更していません。旧公開SHAの取得記録は過去の証跡として保持しています。TalkBack実操作、API 26・メーカー実機、プロセス終了後の復元は未実施です。接続実機は操作せず、AVDとGradleは順次実行しました。

既存3アプリは変更していません。Glyph/ShutUpはclean、Schedulerの既存変更3ファイルは保持しています。以下は前回までの記録であり、旧APIへの互換性を表すものではありません。


## 前回：既存UI移植と階層型設定（2026-10-18）

現行はM3EとNothingの2スタイルです。以下の旧3スタイル・SegmentedListItemの記録は過去の実装時点の履歴であり、現在の方針は[source-port.md](source-port.md)を正とします。

### ビルド・配布

| 確認 | 結果 |
| --- | --- |
| release AAR / debugカタログAPK / androidTest APK | 成功 |
| JVM | 既存1件成功。以降、対象のグループDSLロジックは変更しておらず不要な再実行を省略 |
| ライブラリrelease Lint | 問題なし |
| カタログdebug Lint | エラー0、依存更新通知9件。通常M3廃止に伴う未使用リソース2件は削除 |
| 最終ローカルMaven | AAR・POM・sources・module metadataを発行。独立consumerとMaven版カタログのビルド成功 |
| JitPack座標のPOM-only独立consumer | ローカル形式のclean取得・ビルド成功。Material 3 beta01とicons-core 1.7.8の推移依存を確認 |
| ライセンス・アセット | AARのclasses.jarにMIT / NOTICE。フォント0件。sources JARは13 Kotlinファイル、比較fixture混入0件 |

最終AAR SHA-256：`5A3EDE8AAA0A4FE91FF8708390525FF5D522DBF6643CDBB70714262B60BB43E9`。

Material 3 1.5.0-beta01 / Compose 1.12.0 / SDK 37 / AGP 9.4.1 / JDK 21は維持しています。Check/Closeと自動反転矢印のためGoogle material-icons-core 1.7.8を追加しました。

JitPackのPOM取得検証後の最終変更は表示上限・旧Aboutの補足情報・文字拡大時の角丸・空の説明の省略であり、依存と配布設定は変えていません。最終AARは通常のローカルMavenで再発行・利用確認しました。新しい固定SHAのリモート取得は未実施です。既存の公開確認済み4244bedの記録は下に保持します。今回の変更は未コミット・未Pushで、タグ・Release・Actions・自動実行の変更はありません。

### UIテスト

API 35 Pixel_6a、720×1280px、density 240（画面幅480dp）の読み取り専用AVDです。接続実機を操作せず、GradleとAVDを同時実行していません。

**計22種類のUIテストが成功（分割実行）**：

- PyriteComponentsTest 5件、PyriteExtendedComponentsTest 4件：既存の操作・状態・無効化・キー・ダイアログ・アニメーション・幅280dp/文字200%のAbout。
- CatalogFlowTest 5件：設定/About/部品、2スタイル×明暗、Activity再生成、カテゴリ復元、確認ガード、新しい階層画面の共有状態。
- SourceParityTest 6件：元関数との画像比較、分割行の独立操作、情報省略と更新中の無効化、複合Radio、帰属リンクとVersion操作の分離、旧Aboutの4件目以降の保持。
- MasterSwitchTest 2件：4テーマの固定背景・1回のcallback・下位値保持/無効化・見出し維持・操作ノード数、幅280dp/文字200%の長文。

初回の既存テスト14件中4件はAVD内のMessagesのANRダイアログによりフォーカスを失いました。該当AVD上だけでMessagesを停止するとCatalogFlowTest全5件が成功しました（47.050秒）。ライブラリ側の9件は初回から成功しています。

最終Maven版の追加8件は78.953秒で成功。その後、実画像の目視でマスターの長文がStadium輪郭で切れる問題を発見し、通常72dpではPillとなる36dp固定角丸へ修正しました。修正後にMaster2件・元UI画像比較1件・階層画面1件を選んで再実行し、すべて成功しました（83.427秒）。同じ条件の修正前/後画像を回収し、切れの解消を目視確認しました。TextLayoutResultのoverflowテストだけでは輪郭によるクリップを検出できないため、画像確認も維持します。

マスター背景の比較はリップル終了後に行います。Compose時計と独立したAndroidリップルが終了したことを条件に待ち、状態による背景変更と区別しています。

### 元UIとの画像比較

元ComposableをandroidTest専用に抽出し、360dp・fontScale 1・同じテーマ/色/データ/仮アイコン/代替フォントで撮影しています。元アプリのAPK全体の比較ではありません。

| 比較 | 明暗の結果 |
| --- | --- |
| Scheduler M3E About / Switch / 空説明Switch | 全6条件でPNGのSHA-256まで一致 |
| Glyph Nothing Switch / 空説明Switch / 連結遷移行 | 全6条件でPNGのSHA-256まで一致 |
| Glyph Nothing About | 両条件とも540×537pxのうち35px（約0.012%）に差。ブリッジ切欠きの丸め・AA差を目視確認 |

14条件、元/移植/差分の42画像です。自動差分はRGB各成分3以下を許容し、差分率0.1%未満を要求します。上記12条件については許容差に頼らずPNG全体のハッシュ一致も確認しました。

NothingのNType/Ndotは同梱せず、両側同じSansSerif/Monospaceで比較しています。元フォント・各アプリのアイコンとの完全一致は未再現です。ShutUpのM3E名27spに対し、共通M3EはSchedulerの30spです。Glyphの固定高はSchedulerの可変高・狭幅フォールバックへ統合しています。

Google「自動修正」の提供画像との寸法・色・文字拡大時の差分は[hierarchical-settings.md](hierarchical-settings.md)に記載しました。端末のDynamic Color不明のため同画像との画素一致は主張しません。

### 証跡・残る確認

- build/faithful-accessibility-maven.txt：最終AAR発行・独立consumer・Mavenカタログ。
- build/faithful-accessibility-checks.txt：最終テストAPK・Lint。
- build/faithful-jitpack-local.txt：JitPack座標・POM-only clean consumer。
- build/faithful-existing-ui.txt：既存9件成功とANRによる4件失敗の元ログ。
- build/faithful-catalog-retry.txt：カタログ5件成功。
- build/faithful-final-ui.txt：追加8件成功。
- build/faithful-accessibility-ui.txt：修正後4件成功、最終14条件の画像差分。
- build/verification/faithful-ui/PyriteVerification/：最終比較画像・マスター8状態・文字拡大。
- build/verification/faithful-ui/master-large-before-fix.png：輪郭クリップの修正前。
- pyrite/build/reports/tests/testDebugUnitTest/、各module/build/reports/lint-results-*：単体/Lint。

TalkBack実操作、API 26やメーカー実機、プロセス終了後の復元、元3アプリの移行ビルド、最終変更のリモートJitPack公開取得は未実施です。ShutUp/Glyphの作業ツリーはclean、Schedulerは調査開始時の3ファイルの変更を保持しています。今回の検証AVDは終了しました。

## 以下は以前の実装時点の記録


検証日：2026-10-18（JST）。Phase 1とPhase 2の結果です。

## 採用構成

Material 3 1.5.0-beta01 / Compose 1.12.0 / AGP 9.4.1 / Gradle 9.6.0 / JDK 21。ライブラリminSdk 26、compileSdk 37、Javaバイトコード11。

beta01のGoogle Maven AAR/POM/sourcesで公式API・要件を照合し、この版でコンパイルしています。SDKの正式なpackage IDはplatforms;android-37.0です。JDK 21を事前に用意し、JDK自動取得用のFoojayプラグインは使用しません。

## ビルド・配布

| 検証 | 結果 |
| --- | --- |
| :pyrite:assembleRelease | 成功 |
| :app:assembleDebug | 成功 |
| :app:assembleRelease（R8、未署名） | 成功（Phase 1） |
| PyriteLocalへのrelease publication | 成功 |
| 通常の独立Maven consumer | 成功 |
| Maven版カタログのビルド | 成功 |
| scripts/verify-published.ps1 | JitPack修正後も全段階成功 |
| JitPack用座標のローカル発行＋POM consumer | 成功 |
| JitPack LinuxビルダーでのAAR発行 | 成功、2分10秒 |
| JitPackから匿名取得するPOM consumer | 成功、clean後38タスク、2分21秒 |

公開成功revision：**4244bed7b36aa084c89d14338f176dbf87c0f263**。

```kotlin
implementation("com.github.Link2011-Act2:Pyrite:4244bed7b36aa084c89d14338f176dbf87c0f263")
```

JitPack APIはstatus=ok、private=false、JDK=21です。[実ビルドログ](https://jitpack.io/com/github/Link2011-Act2/Pyrite/4244bed7b36aa084c89d14338f176dbf87c0f263/build.log)。タグ・GitHub Release・GitHub Actions・Maven Central公開は作成していません。

## 公開物とconsumer

AAR、POM、sources JAR、Gradle Module Metadataを認証なしでダウンロードしました。

- POMのcompile依存：Compose UI / Foundation 1.12.0、Material 3 1.5.0-beta01、Kotlin stdlib 2.2.10。
- POMのruntime依存：Compose Animation 1.12.0。consumerでstdlibは2.2.20へ解決。
- 公開AAR：minCompileSdk 37、class major 55（Java 11）、classes.jarにMIT本文・NOTICEを保持。
- sources JAR：6つのKotlinソースを確認。制限付きフォントは同梱していません。
- 公開AARの16 classファイルはローカルrelease AARと個別ハッシュが一致。
- 公開AARのSHA-256：`0FC0C191605DE872B8CC1D54B257130E5F2AAF876C61F7AE59175C9400CAE1AA`。

ローカルの通常座標は`jp.linkserver:pyrite:0.1.0-SNAPSHOT`を維持します。プロジェクト内のbuild/repositoryと専用のbuild/verification/jitpack-m2を使用し、ユーザー共通の~/.m2には発行していません。

consumerは最小ソースとwrapperだけを別ディレクトリへコピーし、Pyriteのソース・project依存・included buildを持ちません。JitPackのPOMのみによる推移依存を使い、Gradle Module Metadataへのリダイレクトも無効にして確認しました。cleanと--no-build-cacheで出力を再生成しています。Gradleの共有ダウンロードキャッシュは使用しており、consumer側の完全なコールドダウンロードは未検証です。

主なローカル証跡：

- `build/verification/jitpack-consumer-remote.txt`：公開POM consumerの依存ツリーと成功ログ。
- `build/verification/jitpack-consumer-remote/app/build/outputs/apk/debug/app-debug.apk`：公開AARから生成したAPK。
- `build/verification/jitpack-public/`：匿名取得したAAR・POM・sources・moduleとAPI応答。
- `build/verification/jitpack-4244bed-live.log`：成功したJitPackビルドログ。

## UI・JVM・Lint（Phase 1）

- JVMテスト1件成功、失敗0：設定グループの重複キー拒否。
- Android UIテスト7件成功、失敗0・エラー0：API 35のPixel_6a AVD。
- Maven発行版カタログで追加UIテスト2件成功、失敗0（78.667秒）。
- ライブラリLint：エラー0・警告0。カタログ：エラー0・警告9、すべて依存更新通知。

UI検証は、3スタイルでのSwitchのrole・checked・無効化・1回のcallback、テーマ切替時の状態保持、ダイアログ取消／確定、安定key、幅280dp・文字200%のAbout、画面遷移とActivity再生成、実際のカタログでの3スタイル×ライト／ダークを含みます。

全7件のHTMLレポートは`build/verification/project-tests/report/index.html`、Maven版UIログは`build/verification/maven-ui-tests.txt`、18枚の画像は`build/verification/maven-screenshots/`にあります。画像のテスト後回収も確認済みです。接続実機は使用せず、今回起動したテスト用エミュレーターは終了しました。

予約再検証では製品Composable・公開API・依存版を変更していないため、成功済みUI/JVM全件テストを繰り返していません。公開AARとローカルAARのclass一致も確認しています。公開consumerのAPKはビルド確認までで、今回あらためて端末へインストールしていません。

## 発見・修正した問題

- beta01のTypographyのfontFamily引数、公式SliderState、Modifier引数位置、翻訳書式・数量文字列、無効Switch等をPhase 1で修正。
- PowerShellのドットを含むGradleプロパティを引用し、consumerのGradleヒープを2 GiBへ設定。ビルドとエミュレーター実行を分離。
- consumerの失敗時も標準エラーをログへ保存するよう修正。
- GitHubをPublicへ変更後、JitPackの非公開判定キャッシュ更新により401が解消。
- 04d2c84の実ビルドでFoojay resolver conventionの解決失敗を確認。JDK 21を明示済みのため、この不要な補助プラグインを削除。
- SDK package IDのplatforms;android-37をplatforms;android-37.0へ修正。パッケージマネージャーが警告だけで成功扱いにする場合も、android.jarとBuild Toolsの確認で早期停止するよう修正。
- aa277c6ではCompose Compiler pluginの解決失敗を確認。公開POMの存在を照合し、同じ版・設定で4244bedが成功したため、一時的な取得失敗と判断。--info/--stacktraceをJitPackログへ追加。バージョンの変更は行っていません。

## 保持・未検証の範囲

既存3アプリは変更していません。Glyph Bartyは53e568c、ShutUpは34241e9で変更なし。Schedulerは26913baで、開始時からのQrShareSelectionScreen.kt・SettingsScreen.kt・AppSettingsComponents.ktの3変更を保持しています。Pyriteの既存.idea/.nameと、先行調査で作成した未追跡docs/shutup-migration-plan.mdも保持し、この予約では計画書を編集・ステージしていません。

API 26端末、各メーカー実機、TalkBack実機操作、定量的な性能・バッテリー測定、consumerの完全な空キャッシュでのダウンロードは未検証です。既存アプリへの依存導入・移行ビルドも未実施です。数値・時刻編集などの追加部品は今後の計画のままです。

Phase 3は別途許可された調査と計画提示まで完了しており、承認待ちです。今回の予約で移行実装やPhase 4の調査へ進んでいません。追加の自動実行は設定していません。

## 標準デザイン・カタログ拡充（今回）

同日、UI設計方針の変更に対応しました。上記のPhase 1/2の公開SHAとは別の、現在の作業ツリーの検証記録です。既存3アプリは引き続き変更していません。

### 実装とデザイン

- String summary版の既存APIを維持し、Action/Switch/RadioのsupportingContent overloadを追加。
- SettingsInfoItem / SettingsStatusItem、SettingsInfoCard / SettingsActionCard、AboutOverviewMosaicを追加。
- M3Eは公式SegmentedListItem・segmentedShapes・LoadingIndicatorを使用。通常／無効行の背景だけを公式segmentedColorsでsurfaceContainerHighへ揃え、連結形状を見やすくしました。
- AboutOverviewは標準の連結モザイクへ統一。狭幅・拡大文字で縦積み、広幅で補足情報を2列にします。
- 外部に角丸・行間・文字階層等を指定する新APIは追加していません。配色・明暗・Dynamic Colorと既存テーマoverrideは維持。
- カタログは6カテゴリ、複数グループの画面例、長文・有効／無効・読み込み・動的表示・確定／取消を操作できます。

### ビルド・Maven・Lint

| 確認 | 結果 |
| --- | --- |
| AAR assembleRelease | 成功、107,891 bytes |
| カタログ assembleDebug / assembleDebugAndroidTest | 成功 |
| カタログ assembleRelease | 成功、R8適用・未署名 |
| verify-published.ps1 | 最終修正後も発行・独立consumer・Maven版カタログの全段階成功 |
| 発行AARとローカルrelease AAR | SHA-256一致 |
| sources JAR | 9 Kotlinファイル |
| JVM | 既存1件成功、失敗0。設定グループ重複キーのテストを維持 |
| ライブラリLint release | 問題なし |
| カタログLint debug | エラー0・警告9。既存と同じ依存更新通知のみ |

AAR SHA-256：`5429EED796CC6014E3910A29A3BE7DFB33594C8912D1E044B023BC8A6893EDD5`。

通常のローカル座標jp.linkserver:pyrite:0.1.0-SNAPSHOTと、JitPack向けpublication・jitpack.yml・検証スクリプトは維持しています。今回の追加APIは、既存のJitPack公開確認済み4244bedには含まれません。現在の作業ツリーは未コミット・未Pushです。新しい固定SHAのリモート公開取得は未実施です。

### UI検証

API 35のPixel_6aを読み取り専用で起動し、720×1280 px・density 240（幅480dp）で実行しました。Gradleの終了後にAVDを起動し、ビルドと同時実行していません。接続実機は操作していません。

**全13件成功、失敗0（147.113秒）**。既存7件を維持し、6件を追加しています。

- 3スタイルのSwitchのrole、ON/OFF、無効化、callbackが1回だけであること。
- 明暗・スタイル変更でホスト状態を維持すること。
- 選択の下書き・取消・確定、安定keyでの行追加。
- 幅280dp・文字200%のAboutを6テーマで表示。
- 設定→About→公式コントロール、Activity再生成。
- カタログの3スタイル×ライト／ダーク。
- カテゴリ間・テーマ境界・再生成後の操作状態の復元。
- 確認ダイアログの確定条件・取消、ON時だけ確認する設定例。
- 補足slot、非クリック情報、Politeな状態行、読み込み中の操作防止を6テーマで確認。
- Aboutのバージョンと補足情報のcallbackを独立して確認。
- 表示切替アニメーションを途中で反転しても最終状態・操作回数が正しいこと。
- テーマ境界を越えて局所UI状態と最新callbackが維持されること。

M3Eから通常テーマの内部構造へ切り替えると、カテゴリとその局所状態を失う不具合を追加テストで発見しました。公式movableContentOf＋rememberUpdatedStateで内容のidentityを維持して修正し、切替直後とActivity再生成後の両方を確認しています。

24枚のテーマ画像を回収し、各スタイルの通常／明暗、M3Eの連結背景とモザイクを確認しました。追加の手動操作では、日本語・横画面への実回転後のSwitch値、OS文字200%・幅360dpでの長い画面名・行の折返しとスクロール、読み込み中ボタンの無効化を確認しました。

### 証跡と未検証

- build/catalog-maven-final.txt：最終発行・独立consumer・Mavenカタログ。
- build/catalog-release-final.txt：Release・Lint。
- build/verification/catalog/ui-tests.txt：13件成功のログ。
- build/verification/catalog/ui-tests-before-fix.txt：状態保持不具合の初回失敗。
- build/verification/catalog/screenshots-final/：24画像。
- build/verification/catalog/japanese-landscape.png、japanese-large-cards-settled.png、japanese-large-action-checked.png：手動確認。
- pyrite/build/reports/tests/testDebugUnitTest/index.html：JVM。
- app/build/reports/lint-results-debug.html、pyrite/build/reports/lint-results-release.html：Lint。

TalkBackの実操作、API 26端末・各メーカー実機、プロセス終了からの復帰、定量的な性能測定、既存アプリ側の依存更新・移行ビルドは未実施です。semanticsテストをTalkBack実操作の代替とはしていません。UI変更に伴う公開API・使用例・手動チェックは [catalog.md](catalog.md)、ShutUpの計画は [shutup-migration-plan.md](shutup-migration-plan.md) を参照してください。

予約済みJitPack自動検証の設定・時刻・promptは変更・削除していません。追加の自動実行・タグ・Release・GitHub Actionsは作成していません。


最終Maven版APKでは、カテゴリ／テーマ／Activity再生成の状態復元、条件付き確認の確定・取消、局所UI状態と最新callbackの3件を追加実行し、すべて成功しました（61.586秒）。ログは build/verification/catalog/maven-ui-tests.txt です。全件13件の後の最終整理は、重複説明・数量文言・未使用importの整理のみで、ライブラリの振る舞いは変えていません。全件の不要な再実行は避け、最終配布形態に関係するケースを選んで確認しました。
最終AARのclasses.jarにMIT本文とNOTICEを確認し、AAR内のフォントファイルは0件でした。今回起動した検証AVDは終了しました。
