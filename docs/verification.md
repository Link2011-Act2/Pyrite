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
