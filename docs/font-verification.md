# フォントAPIの検証（2026-10-19）

仕様とGlyphでの役割対応は[fonts.md](fonts.md)を参照してください。フォントファイルを含む依存は追加していません。

- AAR、カタログAPK、テストAPK、既存JVMテスト1件、ライブラリ/カタログLint、ローカルMaven発行を検証しました。ライブラリLintは問題なし、カタログはエラー0・既存の依存更新警告9件です。
- 新APIのtitle/accentを使う独立consumerと、発行AARを使うカタログ/テストAPKもビルド成功。`build/fonts-fixed-build.txt`、`fonts-local-consumer.txt`、`fonts-maven-catalog.txt`。
- 全UIテスト31件を実行し、既存29件と新規1件が成功しました。新規の役割分離テストで、M3Eのnullableな既定fontFamilyがbody-only指定をChannelへ継承することを検出。既定ファミリーを明示する分岐で修正し、発行AAR版で新規2件＋M3SurfaceColorsTestの計3件を再実行して成功しています（25.704秒）。[最終ログ](reviews/fonts/fonts-fixed-ui.txt)。初回ログは`build/fonts-ui.txt`です。
- 新規テストは旧4引数の位置指定呼び出しと新しいfonts付き呼び出しをコンパイルし、空のPyriteFontsと未指定の設定画面が両スタイルで画素一致することを確認しています。
- titleのみ、accentのみ、bodyのみ、3役割、任意のTypeface由来FontFamilyをM3E/Nothingで検証。TextLayoutResultから画面タイトル、設定タイトル・説明・見出し、Aboutアプリ名・Channel・Version・補足本文のfamily/size/weight/lineHeightを検査しています。カタログのフォントON/OFFと本文不変も確認しました。
- 既存テストには元コードとの参照比較、設定操作、ダイアログ、マスターSwitch、連続遷移、Activity再生成、テーマ変更が含まれます。参照比較の許容差基準は既存のままです。

## 描画例

| 設定 | 画像 |
| --- | --- |
| Nothing、Serifタイトル / Monospace装飾 / Cursive本文 | [PNG](reviews/fonts/fonts-Nothing-3-about.png) |
| M3E、同じ3役割 | [PNG](reviews/fonts/fonts-Material3Expressive-3-about.png) |
| M3E、任意のTypefaceによるタイトルだけの指定 | [PNG](reviews/fonts/fonts-Material3Expressive-4-about.png) |

NType・NDotは同梱せず、役割別の任意フォントはシステムフォントで検証しています。Glyphの実フォントは利用アプリ側の検証対象です。

## 過去画像との比較の扱い

API 35 / Pixel_6a / 720×1280 / 240dpiで、M3E/Nothing×明暗×Dynamicの設定/About 16枚を採取しました。色値は既存カタログと一致しています。過去の`palette-b-final`画像は同じサイズでも内容の縦位置が90px異なるため、全画面完全一致とは扱っていません。

縦位置を揃えた重なり領域では15枚が差分0。M3Eダーク固定色Aboutの1枚は下端36pxの背景領域に25,920画素の差があります。[比較値](reviews/fonts/fonts-default-aligned-checks.json)。表示範囲を変える製品修正はせず、既定フォント自体の回帰は同一条件の空設定比較と元コード参照比較で確認しました。

JitPack取得はこの変更をPushしたSHAで別途検証し、Glyph Barty側の導入記録に実際の固定SHA・取得/単独ビルド結果を残します。タグ・Releaseは作成しません。
