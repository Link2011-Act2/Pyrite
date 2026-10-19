# 画面ヘッダーと遷移

## 参照と採用

| 元実装 | 調査結果・採用 |
| --- | --- |
| ShutUp ui/ShutterApp.kt ScreenScaffold、ui/Type.kt | M3Eヘッダーの基準。surface背景、左の戻るIconButton、23sp/29sp・Medium・字間0・SansSerif、標準TopAppBarの配置・insets |
| ShutUp res/drawable/ic_arrow_back.xml | 24dp・autoMirroredの元ベクターをpyrite_ic_arrow_backへ移植。文字の「‹」を廃止 |
| Scheduler ui/components/AppSettingsComponents.kt AppSettingsScaffold | M3E設定も23sp/29sp・Medium・surfaceで一致。元実装は1行省略指定あり |
| Glyph ui/SettingsScreen.kt SettingsHeader | Nothingヘッダーの基準。titleLarge（代替書体で22sp/32sp・Normal）、下2dp、includeFontPadding=false |
| Glyph/Scheduler AboutScreen.kt | 各アプリのAboutはTopAppBarの既定タイトル。Pyriteでは共通ヘッダーを利用し、設定/Aboutのタイトル階層を揃える |
| ShutUp ui/ShutterApp.kt screen_transition | AnimatedContentの式を移植。アプリ固有のscreenDepth・画面ID・backは移植しない |
| Glyph ui/VisualizerApp.kt / Scheduler SettingsScreen.kt | 横方向遷移はあるが、時間指定やスライド量が異なるため今回の基準にはしない |

NothingのNType/Ndotは同梱せず、システムSansSerifを使います。Glyph Aboutの元ヘッダーには設定用の下2dp補正がありませんが、Pyriteの共通ヘッダーはGlyph設定の補正を両画面で使います。M3EはShutUp基準のため、Scheduler Aboutの既定タイトルより細く・小さくなります。独自に考案したタイトル書式ではありません。

PyriteScreenScaffoldの本文背景はsurfaceです。SettingsScreenもこれを使い、ShutUpとScheduler M3E設定に合わせます。AboutScreenの本文は既存のbackground（Nothingはsurface）を維持し、移植済みモザイクの切欠き色と揃えます。モザイクの形状・余白・文字設定は変更していません。色は既存のPyriteThemeで決まり、元ShutUpのパレットそのものへ変更していません。

## 公開API

すべて `jp.linkserver.pyrite.screen` にあります。

- PyriteScreenScaffold：共通ヘッダーと公式Scaffoldのinsets。title・navigationIcon・actions・contentを受け取ります。
- PyriteBackButton：元の矢印と公式IconButton。onClick・翻訳済みcontentDescriptionを受け取ります。
- PyriteScreenTransition：単一の画面遷移ホスト。targetState・direction・contentKey・stateHolder・contentを受け取ります。
- PyriteNavigationDirection：Forward / Backward。Forwardは右から入場、Backwardは左から入場する物理方向です。

SettingsScreenとAboutScreenも共通ヘッダーを使います。タイトル・アイコン等の内容slotは維持し、角丸・文字書式・余白・速度を自由指定する新APIは設けていません。

```kotlin
// 画面・戻る先の管理はアプリ側。appDirectionはアプリの階層を比較する関数です。
var destination by rememberSaveable { mutableStateOf("settings") }

PyriteScreenTransition(
    targetState = destination,
    direction = { initial, target -> appDirection(initial, target) }
) { screen ->
    when (screen) {
        "settings" -> SettingsScreen(title = "設定") {
            SettingsNavigationItem("About", onClick = { destination = "about" })
        }
        "about" -> AboutScreen(
            info = appInfo,
            overview = appOverview,
            links = appLinks,
            onLinkClick = { openUrl(it.url) },
            navigationIcon = {
                PyriteBackButton({ destination = "settings" }, backLabel)
            }
        )
    }
}
```

contentでは必ず渡されたscreenに応じて画面を描画してください。外側のdestinationだけを参照すると、退場中の画面まで次の画面へ置き換わります。これは[公式AnimatedContentの契約](https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedContent.composable)と同じです。

## 遷移仕様

| 対象 | 元ShutUpと共通の値 |
| --- | --- |
| 横スライド | 300ms、画面幅の1/5 |
| 入場fadeIn | 220ms、遅延40ms |
| 退場fadeOut | 140ms |
| easing | FastOutSlowInEasing。fadeのtweenも元の既定値を維持 |
| 向き | 進むときと戻るときで反転 |
| 境界 | fillMaxSize + clipToBounds |
| サイズ変化 | using(null)でSizeTransformを無効化 |
| 画面状態 | 公式rememberSaveableStateHolder / SaveableStateProvider |

方向はAnimatedContentのinitialState / targetStateから呼び出し側が決めます。ライブラリには画面名や順位表を持ちません。横移動は元と同じ物理方向で、RTLでも自動反転はしません。戻る矢印自体はRTLで反転します。

contentKeyは保持する画面ごとに一意・安定、AndroidのBundleへ保存できる型にします。AnimatedContentと状態保存で同じキーを使い、連続したA→B→Aでも同じ画面を重複Providerとして扱わないようにします。data class等を使う場合は保存可能なIDをcontentKeyで返してください。設定値の永続保存やViewModel保持の仕組みではありません。

画面の破棄に合わせて保存状態を消す必要がある場合だけ、stateHolderを外部へ出してremoveState(key)を呼びます。表示中の画面や同じキーを別ホストで共有しないでください。

## カタログ

設定→About→「導入方法」→ビルド情報の詳細を移動できます。詳細にはローカルな表示設定と縦スクロールがあります。カテゴリ一覧→各カテゴリ、階層設定の一覧→詳細も同じホストを使います。

アプリ側のCatalogPageが階層の順位と表示内容を持ち、Aboutを開いた元画面と戻る先を保持します。階層設定の共有値はカタログroot、画面固有値とスクロールは各画面のrememberSaveableで保持します。BackHandlerはrootだけに配置し、カテゴリ内部のAnimatedContentと状態Holderは削除しました。部品自体の展開アニメーションは維持します。

元ShutUpの画面全体APKを同時に動かす比較ではなく、元関数の抽出fixtureを同じCompose版・テーマ・サイズで描画して比較します。実測のフレーム・操作結果と未検証範囲は[検証記録](verification.md)を参照してください。

カタログのActivity再生成で起きた168pxのスクロールずれは、Window初期化と初回Insets配信の順序を修正しています。ライブラリは公式ScaffoldのInsetsと標準の状態保存を使用し、Window操作は利用側Activityの責務です。原因・計測・修正の範囲は[スクロール復元の調査記録](scroll-restoration.md)を参照してください。
