# Activity再生成時のスクロール復元

## 原因

Android 15の再利用Windowでは、edge-to-edge初期化とInsets配信の順序によって、再生成直後のスクロール領域が一時的に広がることがあります。Composeの`ScrollState`は最初のmeasureで復元値をその時点のmaxへ制限するため、後から正しいviewportに戻っても末端の位置が失われます。保存値や画面キーが正しくても発生します。

二つの条件に注意してください。

1. 一部のAndroid 15の`PhoneWindow`は、preservedWindowのDecorを再利用する際に`decorFitsSystemWindows`を引き継ぎません。Decor初期化後にedge-to-edgeが強制されると、設定を切り替えられない場合があります。
2. ComposeのInsetsは初期値0から始まります。通常の`requestApplyInsets`だけでは、最初のスクロールmeasureより配信が遅れる場合があります。

## カタログの対策

`MainActivity`では、`enableEdgeToEdge()`によるDecor初期化より前に`WindowCompat.setDecorFitsSystemWindows(window, false)`を呼びます。`onPostCreate()`で再利用Windowの実Insetsを`ViewCompat.getRootWindowInsets`から取得し、`dispatchApplyWindowInsets`でcontentへ同期配信します。新規WindowでInsetsがまだなければ、通常の初回配信に任せます。

`setContent`直後の`onCreate`内では、Composeの内容がLifecycleの`ON_CREATE`を待つため、Insets購読の準備前になる場合があります。同期配信はcomposition開始後かつ最初のmeasure前に行う必要があります。

Window管理はホストActivityの責務です。ライブラリは公式ScaffoldのInsetsと標準のScrollState・SaveableStateHolderを使います。固定pxの加算、任意時間のdelay、独自Saver、期待値を`min(max)`へ緩める対処は行いません。

## 再発防止

`CatalogFlowTest`はM3E/Nothingの詳細画面を末端までスクロールし、Activityを連続2回再生成して、位置・max・viewport・ルート領域・systemBarsと局所設定値を検査します。戻る・再訪時の復元も確認します。実行方法は[検証ガイド](verification-guide.md)を参照してください。

利用アプリでもActivity再生成、OSごとのInsets、プロセス終了からの復帰を確認してください。

## 実装上の根拠

- [Android 15初期PhoneWindow](https://android.googlesource.com/platform/frameworks/base/+/android-15.0.0_r1/core/java/com/android/internal/policy/PhoneWindow.java)：preservedWindowコンストラクター、Decor初期化、Insets設定。
- [Android 15ブランチPhoneWindow](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android15-release/core/java/com/android/internal/policy/PhoneWindow.java)：後の実装にはpreservedWindowの`decorFitsSystemWindows()`を引き継ぐ処理があります。
- 使用中のAndroidXソース：foundation-layoutの`WindowInsets.android.kt`、foundationの`Scroll.kt`、uiの`Wrapper.android.kt`、activityの`EdgeToEdge.kt`、coreの`WindowCompat.java`。
