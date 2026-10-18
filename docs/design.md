# 設計・公式APIの採否

## 依存と責務

app → pyrite → Compose / Material 3。ライブラリは1モジュールにまとめ、状態保存・Navigation・ネットワーク・権限・設定キーを持たせません。公開APIをtheme/settings/dialog/aboutへ配置し、位置計算用CompositionLocalと描画の分岐はinternal/privateです。

## Material 3 beta01の確認

2026年10月7日公開の`1.5.0-beta01`のGoogle Maven配布AAR・POM・sources.jarを確認しました。

- AAR要件：minCompileSdk 37、minAGP 9.1.0。
- POM：Compose UI / Foundation / Runtime 1.12.0、Kotlin stdlib 2.2.20。
- `MaterialExpressiveTheme`、`MotionScheme.expressive()`が存在。
- `SegmentedListItem`、`ListItemDefaults.segmentedShapes(index,count)`、`SegmentedGap`が存在。
- 標準の`AlertDialog`、`Switch`、`RadioButton`を利用。
- Typographyのファミリー指定は`Typography(fontFamily = ...)`。`defaultFontFamily`というコンストラクター引数はありません。

公式参照：
- https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-beta01
- https://developer.android.com/reference/kotlin/androidx/compose/material3/ListItemDefaults
- https://developer.android.com/develop/ui/compose/accessibility/api-defaults
- https://developer.android.com/build/publish-library/configure-pub-variants

## 採否

| 用途 | 方針 |
| --- | --- |
| M3Eの連結設定行 | 公式SegmentedListItemとsegmentedShapes。角丸や押下モーフを再実装しない |
| Switch行 | 公式click版ListItemにSwitchの操作属性を付け、子Switchは表示専用。checked版ListItemはCheckboxのRoleを持つため使用しない |
| Radio行 | 公式selected版ListItemを利用。子RadioButtonは表示専用 |
| 確認・選択ダイアログ | AlertDialog。BasicAlertDialogをExpressive版の代用品として使わない |
| Nothing風の設定行 | Surfaceによる固有の連結角丸と余白。色は明示したテーマから取得 |
| About | アプリ固有構成をデータ・slot化。公式Cardを組み合わせ、狭幅/文字拡大で積み重ねる |
| 標準Button/Card/Slider/LoadingIndicator | 呼び出し側が公式APIを直接使用。ライブラリには同義のラッパーを追加しない |
| 展開 | 公式AnimatedVisibility等を使用。現段階はサンプルで実演 |
| 形状・状態モーフ | 将来もMaterialShapes/Morph/toShape/toPathを利用。独自補間エンジンを作らない |
| FABメニュー・floating toolbar | 将来の導入例では公式APIを使用。現段階はライブラリAPIにしない |
| Markdown | 初回は任意content slot。Markwonを共通基盤に強制しない |

## 状態・操作

SettingsChoiceDialogはdraftを呼び出し側で管理します。確定まで保存しない設計です。Switch行は全体で一度だけcallbackを発火し、子Switchは重複する読み上げノードを持ちません。disabledは親子両方へ反映します。

SettingsGroupのキーは表示される行に対して安定・一意とし、項目追加・削除時に位置を再計算します。別行のremember状態が移動しないようkeyを適用します。

## 初期スコープの制限

設定とAboutの初期実装を優先します。数値・時刻編集、独立した2操作の行、OSS一覧、状態モーフは段階追加です。Aboutは元アプリの画素単位の複製ではなく、長い文言と大きな文字に対応するレイアウトへ再設計しています。専有フォントなしでNothing風の色・角丸・密度を表現します。

## 将来の外部配布

Maven Publishのrelease publicationからAAR/POM/Gradle metadata/sourcesを出力します。配布ライブラリを一つに保ち、利用側にPyriteのGradleモジュールやリポジトリ自体を組み込む必要をなくします。リモート公開先、署名、資格情報は将来設定する項目です。
