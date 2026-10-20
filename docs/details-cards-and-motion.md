# 操作を含む設定カードとGlyph方式の遷移（2026-10-20）

依存版、Maven Publish/JitPack、フォントAPIは維持しています。READMEは今回編集していません。

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

## 標準遷移の仕様変更

正本はGlyph Barty `53e568c`の`VisualizerApp.kt`、`screen_transition`です。幅1/5、Forwardは右、Backwardは左から入り、引数なしのslideIn/OutHorizontallyとfadeIn/Outを使います。旧Pyriteの300ms tween、入場220ms/40ms遅延、退場140ms、独自easing指定を削除しました。

Google MavenのCompose 1.7.2と1.12.0の公式sourcesで、slide/fadeのStiffnessMediumLow springと既定SizeTransformのspring/clip=trueが一致することを確認しました。[比較条件](reviews/details-2026-10-20/official-defaults.json)。[公式AnimatedContent解説](https://developer.android.com/develop/ui/compose/animation/composables-modifiers)と[公式Slider](https://developer.android.com/develop/ui/compose/components/slider)を基礎にしています。

Surface→Box→AnimatedContentの元配置へ合わせ、独自clipToBoundsとusing(null)を除去しました。固定の終了時刻はなく、OSアニメーション倍率を含むCompose標準に従います。targetState、direction、contentKey、stateHolderのAPI、SaveableStateHolder、スクロール・画面固有値の保持は維持しています。

## 検証

- AAR、カタログAPK/テストAPK、既存JVMテスト、Lint、ローカルMaven発行、新APIを呼ぶ独立consumerのビルド成功。ライブラリLintは問題なし、カタログはエラー0・既存の依存更新警告9件です。
- 全35 UIケースを確認。初回28件成功、AVDのMessages ANRによるフォーカス妨害6件と、新デモに伴うselectorの重複1件を修正後に再実行しました。親クリックなし、子操作の独立性、SliderのSetProgress/ドラッグ終了、disabled、条件表示、再生成・テーマ変更での値保持を検証しています。
- 旧Glyph参照と通常サイズ22組・可変サイズ24組の計46組で画素一致。[数値](reviews/details-2026-10-20/frame-comparison.json)、[フレーム図](reviews/details-2026-10-20/glyph-motion-frames.png)。旧版の既定値は公式ソースで確認し、参照fixtureは同じCompose版で比較しています。旧APKとの実機同時撮影比較ではありません。
- カタログ：[M3E light](reviews/details-2026-10-20/cards-m3e-light.png)・[dark](reviews/details-2026-10-20/cards-m3e-dark.png)、[Nothing light](reviews/details-2026-10-20/cards-nothing-light.png)・[dark](reviews/details-2026-10-20/cards-nothing-dark.png)。API35/720×1280/240dpi/通常文字倍率です。全実機・倍率・実TalkBackは未検証です。

## 利用アプリへの影響

ShutUpの実リポジトリは固定SHA`43557c8`を維持します。更新しない限り動作は変わりません。将来新SHAへ更新すると、ヘッダー・テーマ・API契約は維持されますが、画面全体の遷移はspringへ変わります。固定300msを前提とする独自テストは完了待ちへ見直す必要があります。

公開SHAでのJitPack取得と、Glyph導入後・ShutUp独立コピーの具体的結果は、利用側の導入記録に残します。タグ・Releaseは作成しません。
