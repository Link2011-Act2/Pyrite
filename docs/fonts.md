# ホスト側のフォント指定

`PyriteFonts(title = ..., accent = ..., body = ...)`を`PyriteTheme(fonts = ...)`へ渡します。すべて省略可能なFontFamilyで、nullの役割は元のフォントを使います。色、サイズ、ウェイト、行高、余白、形状、モーションは変更しません。フォントのメトリクスによる改行や文字幅の差は生じます。

```kotlin
PyriteTheme(
    style = PyriteStyle.Nothing,
    fonts = PyriteFonts(title = NTypeFontFamily, accent = NothingDotFontFamily)
) {
    // 通常の設定項目・説明文・メタデータは従来のSansSerifを維持
}
```

| 役割 | 適用箇所 | Glyph Bartyでの指定 |
| --- | --- | --- |
| title | 画面タイトル、Aboutアプリ名、Channelの強調値。テーマのdisplay/headline/titleLarge/titleMedium | NTypeFontFamily |
| accent | AboutのVersion強調値（Nothingの既定値はMonospace） | NothingDotFontFamily |
| body | 通常の設定行、説明、セクション見出し、メタデータ、body/label/titleSmall | 未指定（SansSerifを維持） |

通常の設定行のタイトル、セクション見出し、Channel/Build/Versionの小さなラベルをNTypeへ一括変更しません。Materialスタイルにも同じ役割を適用できます。Serif/Cursive/Monospace、リソースのFont、Typeface由来など任意のFontFamilyが使えます。NType・NDotのファイルはライブラリにもカタログにも含めません。利用アプリが権利を確認して管理します。

既存の`PyriteTheme(style, darkTheme, dynamicColor, content)`はそのまま維持します。新しいオーバーロードはfontsを先頭の名前付き引数として受け取り、指定しない呼び出しとの曖昧さや、4番目へcontentを渡す既存コードの破壊を避けています。fontsを省略する場合は既存シグネチャ、指定する場合は`fonts = PyriteFonts(...)`を使います。

カタログの外観設定に「カスタムフォント」を追加しました。SerifのtitleとMonospaceのaccentを設定し、本文は既定のままにします。スタイル・明暗・既存設定・画面状態の管理方法は維持します。

検証記録は[font-verification.md](font-verification.md)を参照してください。
