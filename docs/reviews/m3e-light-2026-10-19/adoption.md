# B配色の採用（2026-10-19）

ユーザーの「Bで」という選択に従い、M3 Expressiveライトの標準配色へ反映しました。[比較時点の16画像と候補](comparison.md)は履歴として保持します。

## 実装

- `PyriteTheme`は既存のパレット選択を維持します。Dynamic ONは公式dynamicLightColorScheme、OFFはSourceExpressiveLightです。
- **M3Eライトだけ**、選択したパレットのsurfaceContainerLowをsurface/backgroundへ割り当てます。ヘッダー、設定本文、About本文とモザイクの隙間が揃います。
- Pyrite共通カードの内部ヘルパー`pyriteCardColor()`は、M3EライトではsurfaceContainerLowest、それ以外では従来のsurfaceContainerHighを返します。
- 設定行（Switch/Radio/分割操作行等）、説明/アクションカード、Aboutモザイク/アクション/更新カードで同じヘルパーを使用します。
- `ColorScheme.surfaceContainerHigh`自体の値は保持し、公式ダイアログ等のロールを一括置換しません。primary、onSurface、onSurfaceVariant、surfaceContainerHighest等も変えていません。
- darkThemeを内部CompositionLocalで渡し、輝度による明暗推測を避けています。公開APIやアプリごとの色指定引数は追加していません。
- SourceColorsの固定値、Nothing/ダークの分岐、形状、余白、文字、アニメーション、設定保存/callbackは変更していません。既存3アプリも変更していません。

| Dynamic | 背景 | カード |
| --- | --- | --- |
| OFF | #E2EBF0 | #F2F8FB |
| ON（検証AVDの配色） | #F4F3FA | #FFFFFF |

色は既存ロールから取得します。上記HEXを製品コードへ直書きしたり、色のalpha/明度を加工したりしていません。Dynamic ONの値は壁紙/システム配色に従います。

## 検証手順

1. 変更前の発行AARでM3SurfaceColorsTestを`palettePhase=before`として実行し、2スタイル×明暗×Dynamic ON/OFFの8条件、設定/Aboutの16枚を保存しました。
2. 変更前のM3Eライト4画像は、比較提示時の「現状」画像とSHA-256が一致しました。同じ画面条件であることを確認しています。
3. BのAAR・カタログ・JVMテスト・Lint・ローカルMaven発行・独立consumerをビルドし、発行AARをカタログへ組み込みました。
4. 製品実装をそのまま描画し、採用済みB画像4枚との一致と、変更対象外の12画像との一致を確認しました。SourceParityTestは承認された色だけを参照側へ適用し、元の形状・配置の比較を維持します。

## 最終結果

- **最終Maven版カタログの関連UIテスト15件成功、失敗0（247.294秒）**。M3SurfaceColorsTest 1件、SourceParityTestの参照比較1件、NavigationUiTest 3件、CatalogFlowTest 8件、MasterSwitchTest 2件です。
- M3Eライトの設定/About×Dynamic ON/OFFの4画像は、ユーザーが承認したB画像とファイルのSHA-256が一致しました。
- M3Eダーク×Dynamic ON/OFFとNothing明暗×Dynamic ON/OFFの12画像は、変更前とファイルのSHA-256が一致しました。
- [画像照合結果16件](applied/image-checks.json)、[変更前の実測値](applied/before-metrics.json)、[反映後の実測値](applied/applied-metrics.json)を保存しています。反映後のM3Eライト4画像は`applied/`にあります。
- 元コードとの参照比較14組は成功。M3E全条件とNothing設定行等の12組は差分0、Nothing/About明暗の各1組は35画素（0.0121%、既定0.1%未満の許容範囲）です。これは元関数の参照描画との比較で、製品画面の変更前後12画像はすべて完全一致しています。
- ヘッダー8条件、前進/後退18フレーム組の比較、途中反転・再生成、テーマ切替、設定状態保持、確認/取消、マスターSwitchの背景不変と無効化、拡大文字/狭幅を確認しました。
- 168pxの復元ずれも再発なし。M3Eは707px、Nothingは737pxを2回連続再生成後も保持しています。
- AAR、サンプルAPK、テストAPK、既存JVMテスト1件、Maven発行、独立consumerのビルドが成功しました。ライブラリLintは問題なし、カタログLintはエラー0・既存の依存更新警告9件です。

AAR SHA-256（ローカルと発行物が一致）：`A7A38916E32F8F2EC271DC2EB927D7ABFE4BC95D553FDE0CA0AE72ECDA8C393F`。

### 反映後の画像

| | Dynamic OFF | Dynamic ON |
| --- | --- | --- |
| 設定 | [原寸PNG](applied/palette-b-applied-expressive-light-static-settings.png) | [原寸PNG](applied/palette-b-applied-expressive-light-dynamic-settings.png) |
| About | [原寸PNG](applied/palette-b-applied-expressive-light-static-about.png) | [原寸PNG](applied/palette-b-applied-expressive-light-dynamic-about.png) |

### ログと制限

- 基準APKのビルドログ：`build/palette-b-baseline-build.txt`。基準撮影：`build/palette-b-before-ui.txt`。基準画像：`build/verification/palette-b-before/`。
- 最終ビルド：`build/palette-b-final-build.txt`、`build/palette-b-final-maven.txt`、`build/palette-b-final-test-apk.txt`。
- 最終UI：`build/palette-b-final-ui.txt`、`build/palette-b-final-ui-logcat.txt`。全131画像は`build/verification/palette-b-final/`に保存しています。
- 検証はAPI 35、Pixel_6a、720×1280px、density 240。実機・API 26・全壁紙/OSコントラスト設定・TalkBack実操作は今回未検証です。
- 検証AVDとGradleは停止済みです。README、依存バージョン、JitPack/Maven Publish設定、既存3アプリは変更していません。コミット・Push・タグ・Releaseの作成は行っていません。
