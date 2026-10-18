# 検証結果

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
