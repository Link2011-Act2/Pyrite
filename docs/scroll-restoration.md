# Activity再生成時の168pxスクロールずれ

2026-10-19に、カタログの設定→About→導入方法の詳細画面で調査しました。既存3アプリは変更していません。公開API・遷移の時間/方向・保存値の形式・依存バージョンは変更していません。

## 再現と計測

API 35、Pixel_6a（`AE3A.240806.036/12592187`）、720×1280px、density 240、fontScale 1。追加ビルド情報をONにし、最下部までスクロールしてActivityScenario.recreate()を実行しました。

| 計測箇所 | systemBars 上/下 | Android content | scroll viewport | content高さ | scroll / max |
| --- | --- | --- | --- | --- | --- |
| 再生成前 | 132 / 36px | 720×1280、y=0 | 1016px | 1723px | 707 / 707px |
| 修正前の初回計測 | Composeでは0 / 0px | 全画面の制約 | 1184px | 1723px | **707→539 / 539px** |
| 修正前の安定後 | Android rootは132 / 36、Composeは0 / 0px | 720×1112、y=132 | 1016px | 1723px | 539 / 707px |
| Window初期化順だけ修正した安定後 | 132 / 36px | 720×1280、y=0 | 1016px | 1723px | 539 / 707px |
| 初期化順＋ON_CREATE後にInsetsを配信した初回計測 | 132 / 36px | 720×1280、y=0 | **1016px** | 1723px | **707 / 707px** |

保存値707pxは正しく読み戻されていました。失われるのは最初のmeasureで、`1723 - 1184 = 539`へ制限される時点です。次のmeasureでviewportが1016px、maxが707pxへ戻っても、失われた位置は戻りません。`1184 - 1016 = 132 + 36 = 168px`です。画面ごとのSaveableStateHolderのキー衝突や、設定値の保存失敗ではありません。

## 原因と修正

二つの問題が重なっていました。

1. 検証AVD世代のAndroid 15では、PhoneWindowがpreservedWindowのDecorを再利用するとき、decorFitsSystemWindowsの値を引き継がず、既定のcontent Insets適用処理をViewRootへ設定します。その後Decor初期化でedge-to-edge強制が有効になると、setDecorFitsSystemWindowsは早期returnし、その処理を切り替えません。結果としてAndroid側のcontentが上下のバー分だけ縮み、Composeへ届くInsetsも0になります。
2. この初期化順を直しても、再生成直後のCompose WindowInsetsHolderは0から始まり、requestApplyInsetsは次のtraversalになります。最初のスクロールmeasureが先に進むと、公式ScrollState.maxValueのsetterが復元値を一時的な小さいmaxへ制限します。

カタログMainActivityでは、`enableEdgeToEdge()`がDecorを初期化する前に`WindowCompat.setDecorFitsSystemWindows(window, false)`を呼びます。さらに`onPostCreate()`で、再利用Windowが既に持つ実際のInsetsを`ViewCompat.getRootWindowInsets` / `dispatchApplyWindowInsets`でcontentへ同期配信します。新規WindowでInsetsがまだなければ通常の初回配信に任せます。

同期配信をonCreate内でsetContent直後に行っても効果はありませんでした。公式ComposeのWrappedCompositionはLifecycleのON_CREATEまで内容を待機するため、その時点ではInsets購読がまだ準備されていません。成功した計測ログでは、再生成ごとにComposition開始→onPostCreateの実Insets配信→最初のviewport計測（1016px）の順序を確認しました。

任意時間のdelay、168pxの加算、独自ScrollState/Saver、テスト期待値の緩和は導入していません。Windowの管理はカタログActivityへ留め、ライブラリは公式ScaffoldのInsetsとrememberSaveableStateHolder/ScrollStateを使います。

前回候補の`LocalPyriteScaffoldInsets`によるホスト/Scaffold間の購読共有と、明示的ComposeView.consumeWindowInsets指定は不要として削除しました。カタログは標準activity-composeのsetContentを使います。measure/global-layout/Compositionの一時的な製品ログも除去し、テスト側の幾何情報ログは残しています。

## ソースの根拠

- [Android 15初期PhoneWindow](https://android.googlesource.com/platform/frameworks/base/+/android-15.0.0_r1/core/java/com/android/internal/policy/PhoneWindow.java)：preservedWindowコンストラクター、onViewRootImplSet、generateLayout、setDecorFitsSystemWindows、applyDecorFitsSystemWindows。
- [Android 15ブランチPhoneWindow](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android15-release/core/java/com/android/internal/policy/PhoneWindow.java)：後の実装にはpreservedWindow.decorFitsSystemWindows()を引き継ぐ処理があります。
- キャッシュ内の実依存ソース：foundation-layout 1.12.0 WindowInsets.android.kt（初期値とlistener）、foundation 1.12.0 Scroll.kt（measureとmaxValueのクランプ）、ui 1.12.0 Wrapper.android.kt（ON_CREATE待機）、activity 1.13.0 EdgeToEdge.kt、core 1.19.1 WindowCompat.java。

抽出した資料は`build/navigation-source-*`です。ライブラリの依存を変更して問題を回避してはいません。

## 証跡と再検証

- `build/navigation-resume-candidate-ui.txt`：昨日最後の候補でも707→539pxの失敗を再確認。
- `build/navigation-insets-probe-geometry.txt`：初回measureでのクランプと、Android親領域の縮小。
- `build/navigation-window-order-geometry.txt`：初期化順だけで親領域は直るがクランプは残ることを確認。
- `build/navigation-insets-delivery-geometry.txt`：onCreate内の配信では早すぎて改善しないことを確認。
- `build/navigation-insets-lifecycle-retry-ui.txt`：計測付きM3Eテスト成功、連続2回再生成。
- `build/navigation-insets-lifecycle-geometry.txt`：Composition→配信→初回measureの順序、707pxの維持。
- `build/verification/navigation-insets-lifecycle/`：成功時の前後画像。

最終整理後のビルド・Maven・UI結果は[検証記録](verification.md)を参照してください。CatalogFlowTestはM3E/Nothing各スタイルで連続2回再生成し、位置・max・viewport・Compose root・Android content・systemBarsと局所設定値の保持を確認します。戻る/再訪時の位置も確認します。元の末端位置との一致を要求し、min(max)での期待値調整は除去しました。

最終Maven版APKの関連UIテストは11件すべて成功（117.513秒）。M3Eは707px、Nothingは737pxを2回連続再生成後も保持しました。両スタイルの前後PNGはそれぞれSHA-256が同一です。最終ログは`build/navigation-restoration-final-ui.txt`と`build/navigation-restoration-final-ui-logcat.txt`、画像は`build/verification/navigation-restoration-final/`にあります。

再実行（Gradle終了後に専用AVDを使用）：

```powershell
& $adb -s emulator-5580 shell am instrument --user 0 -w -e timeout_msec 60000 -e class 'jp.linkserver.projectpyrite.CatalogFlowTest#aboutDetailsRetainScrollAndLocalStateAcrossBackAndRecreation,jp.linkserver.projectpyrite.CatalogFlowTest#nothingAboutDetailsRetainScrollAndLocalStateAcrossBackAndRecreation' jp.linkserver.projectpyrite.test/androidx.test.runner.AndroidJUnitRunner
```

boot直後のinstrumentation起動ANRは、このスクロール失敗とは別です。`build/navigation-insets-lifecycle-startup-logcat.txt`にはProcess failed to complete startupとBOOT処理が残っています。同じAPKを起動安定後に再実行した結果を採用しています。API 26や他メーカー端末、プロセス終了からの復帰は今回未検証です。
