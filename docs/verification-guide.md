# 検証ガイド

JDK 21とAndroid SDK Platform 37を用意します。SDKは未追跡の`local.properties`または`ANDROID_HOME`で指定してください。ビルド要件は[README](../README.md#ビルド要件)を参照してください。

## ビルド・Unit Test・Lint

リポジトリのルートで実行します。

```powershell
.\gradlew.bat :pyrite:assembleRelease :app:assembleDebug :app:assembleDebugAndroidTest
.\gradlew.bat :pyrite:testDebugUnitTest :app:testDebugUnitTest :pyrite:lintRelease :app:lintDebug
```

レポートは各モジュールの`build/reports/`、APK・AARは`build/outputs/`へ出力されます。

## UI・回帰テスト

API 26以上のエミュレーターまたは端末を接続し、起動が安定してから実行します。接続先が複数ある場合は`ANDROID_SERIAL`で対象を指定してください。

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
# 配色と元コードとの比較に絞る場合
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=jp.linkserver.projectpyrite.SourceParityTest'
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=jp.linkserver.projectpyrite.M3SurfaceColorsTest'
```

| テスト | 主な確認内容 |
| --- | --- |
| `SourceParityTest` | AndroidTest内の元Composableと同条件で描画し、About・設定行の寸法と配置を比較 |
| `M3SurfaceColorsTest` | M3EライトのB配色、Dynamic Color ON/OFF、Nothing・ダークの配色 |
| `FontsUiTest` | フォント未指定と空指定の一致、title/accent/bodyの役割分離、任意FontFamily |
| `CatalogFlowTest` / `NavigationUiTest` | 遷移フレームの参照比較、戻る・往復・連続遷移、テーマ変更、Activity再生成後の値とスクロール位置 |
| `MasterSwitchTest` | ON/OFFで背景色を維持、下位の無効化と値保持、操作対象とcallback回数 |
| その他の部品・設定テスト | 行・カード・Slider・ダイアログの操作、無効状態、確定・取消、semantics |

比較用ソースは`app/src/androidTest/.../reference/`にあります。保存済みPNGを正解画像として読み込まず、その場で参照側と製品側を描画します。M3Eライトでは採用済みB配色を参照側にも適用します。参照比較の範囲は[source-port.md](source-port.md)を参照してください。

テスト結果は`app/build/reports/androidTests/connected/`に出力されます。画像はAPI 29以上では端末の`Pictures/PyriteVerification`、API 26–28ではアプリの外部ファイル領域の`screenshots`に保存します。収集した画像・測定JSON・ログはローカルの`build/verification/`などへ置き、Gitには追加しません。`docs/reviews/`はテスト入力に使いません。

## Maven・JitPack Consumer

```powershell
# ローカルMaven発行、独立consumer、発行AARを使うカタログ
.\scripts/verify-published.ps1
# JitPack座標でのローカル発行と、POMのみを使う独立consumer
.\scripts/verify-jitpack.ps1
# 公開済みの固定SHAを実際に取得する場合
.\scripts/verify-jitpack.ps1 -Revision '<commit-sha>'
```

最後のコマンドには必要APIを含む公開済みSHAを指定します。ローカル検証だけではJitPackサーバーでの成功を確認したことにはなりません。[配布ガイド](distribution.md)に発行構成とConsumerの詳細があります。

公開物を変更する場合は、AAR内の`classes.jar`に`META-INF/pyrite/LICENSE`と`NOTICE.md`が入り、sources JARにAndroidTestの比較用ソースや専有フォントが混入しないことも確認してください。

## 手動確認

[カタログの確認手順](catalog.md#状態と検証手順)に沿って、明暗・M3E/Nothing・Dynamic Color、狭幅・長文・文字拡大、戻る操作と再生成を確認します。実端末のTalkBack、OSの文字・表示サイズ、プロセス終了からの復帰は自動UIテストと別に確認してください。利用アプリの権限・保存・サービス・更新処理は各アプリ側で回帰確認します。
