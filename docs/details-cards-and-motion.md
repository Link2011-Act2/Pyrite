# 操作を含む設定カードとGlyph方式の遷移

## 設定カード

すべて`jp.linkserver.pyrite.settings`にあります。

| API | 責務 |
| --- | --- |
| `SettingsContentCard(modifier, content)` | 非クリック型の面。slotは内側のlayout/paddingと子操作を担当 |
| `SettingsSlider(title, valueText, value, onValueChange, valueRange, …)` | GlyphのParameterSliderから抽出した内容部分。公式Sliderを使う |
| `SettingsSliderItem(…, supportingContent)` | 22dp×18dp内側余白付きSliderカード。下部に補足やリセットを置ける |
| `SettingsButtonItem(title, actionLabel, onClick, …)` | タイトル/説明と公式Button。行自体はクリック不可 |

`SettingsGroup`内の`item`として置くと、可視項目に応じた22dp/6dp角丸と2dp間隔を自動適用します。グループ外は単独形状です。新しい形状・色・文字スタイルの自由指定引数はありません。

```kotlin
SettingsGroup {
    item("parameter") {
        SettingsSliderItem(label, formattedValue, value, onChanged, 0f..1f,
            onValueChangeFinished = onCommitted, enabled = featureEnabled)
    }
    if (showActions) item("actions") {
        SettingsContentCard {
            Row(Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                TextButton(onReset, enabled = featureEnabled) { Text(resetLabel) }
                TextButton(onImport, enabled = featureEnabled) { Text(importLabel) }
            }
        }
    }
}
```

カード面にはonClickや一括enabledを設けず、子の操作・無効条件はホストが指定します。単一の全体クリックには既存SettingsActionItem等を使います。Sliderの値、整形、保存、確定処理もホストが管理します。連続value callbackと終了callbackは別です。通常設定なのでパラメーター名のfontはbody役割で、NType等のtitle指定を広げません。機能固有の開始/停止、メーター、音声・デバイス・保存処理は含みません。

## 標準遷移

基準はGlyph Barty `53e568c`の`VisualizerApp.kt`、`screen_transition`です。幅1/5、Forwardは右、Backwardは左から入り、引数なしのslideIn/OutHorizontallyとfadeIn/Outを使います。

slide/fadeはCompose既定のStiffnessMediumLow spring、SizeTransformは既定のspring/clip=trueを使います。[公式AnimatedContent解説](https://developer.android.com/develop/ui/compose/animation/composables-modifiers)と[公式Slider](https://developer.android.com/develop/ui/compose/components/slider)を基礎にしています。

Surface→Box→AnimatedContentの元配置を使います。固定の終了時刻はなく、OSアニメーション倍率を含むCompose標準に従います。targetState、direction、contentKey、stateHolderの契約と状態保持は[画面遷移](navigation.md)を参照してください。

## 検証

SettingsControlsTestで親クリックなし、子操作の独立性、SliderのSetProgress/ドラッグ終了、disabled、条件表示、再生成・テーマ変更での値保持を検証します。NavigationUiTestは旧Glyph参照と通常サイズ・可変サイズの遷移フレームを同じCompose版で描画して比較します。実行方法は[検証ガイド](verification-guide.md)を参照してください。

## 利用アプリへの影響

固定SHA依存のアプリは、依存を更新するときに遷移の変化を確認してください。以前の固定300ms tweenから更新する場合、固定時間を前提とする独自テストをアニメーション完了待ちへ見直す必要があります。その他の回帰確認は[移行ガイド](migration.md)を参照してください。
