# 設計・公式APIの採否

現在の採用元・寸法・元UIとの差分は[source-port.md](source-port.md)を正とします。

app → pyrite → Compose / Material 3。Android Libraryは1モジュールです。状態保存、Navigation、ネットワーク、権限、設定キー、アプリ固有SDKは持ちません。公開APIはtheme/settings/card/dialog/about、内部の位置・描画補助はinternal/privateです。

Material 3 1.5.0-beta01、Compose 1.12.0の既存基準を維持します。公式配布AAR/POM/sourcesとコンパイルで確認したAPIを使います。

| 用途 | 採否 |
| --- | --- |
| M3Eテーマ | 公式MaterialExpressiveThemeとMotionScheme.expressive。色・文字・テーマ形状はScheduler由来 |
| 設定行 | Scheduler/Glyph由来のSurface/Row、22/6dp・2dp間隔。存在するSegmentedListItemは元デザインを変えるため不採用 |
| Switch / Radio | 公式部品。M3E Switchは公式Check/Closeアイコン。親行を単一操作にする |
| Nothing風 | Glyphの色・文字・連結行。専有フォントなし。通常MaterialThemeを基盤とするが独立した通常M3スタイルは提供しない |
| About | 専用Surfaceと元の48dpブリッジ描画。SettingsGroupへ統合しない |
| 更新・帰属 | ShutUp由来の任意コンテンツ、状態/callbackは外部 |
| 選択・確認 | 公式AlertDialog。ChoiceDialogは下書きと確定を外部管理 |
| 標準Button/Card/Slider/LoadingIndicator | 公式APIを直接利用。同義ラッパーなし |
| 展開・サイズ変化 | 公式AnimatedVisibility / animateContentSize。独自補間エンジンなし |

SettingsGroupは可視行だけを収集し、一意のkeyでremember状態と位置を維持します。設定値はホスト所有です。Switch/Radioの子は表示専用とし、重複読み上げ・二重callbackを防ぎます。分割遷移行のみ独立した2操作です。

PyriteThemeはM3EとNothingで公式テーマの親構造が異なります。movableContentOfとrememberUpdatedStateで呼び出し側のcomposition identityと最新callbackを維持します。テーマ変更で保存値を初期化しません。

PyriteThemeの入力はstyle / darkTheme / dynamicColor / contentです。形状・文字・固定パレットはライブラリで管理します。不要な互換APIは削除しました。現行APIと判断理由は[public-api.md](public-api.md)を参照してください。

公式参照：

- [Material 3 beta01](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-beta01)
- [アクセシビリティの既定動作](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [Androidライブラリ公開variant](https://developer.android.com/build/publish-library/configure-pub-variants)
- [Movable Content設計](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/runtime/design/movable-content.md)
