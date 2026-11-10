# 既存アプリへの導入・更新

PyriteはShutUp、NITTC Scheduler、Glyph Bartyで利用されています。ここでは新しいアプリへの導入と、固定SHAを更新する際の確認事項をまとめます。

## 依存とテーマ

1. [README](../README.md)のSDK・JDK・Compose要件を確認し、必要APIを含む固定SHAのMaven依存を追加します。取得方法は[配布ガイド](distribution.md)を参照してください。
2. 利用側の解決済み依存を確認し、古いComposeやMaterial 3への強制固定を解消します。Compose CompilerとAGPの組み合わせも利用側でビルド確認します。
3. ホストが所有するテーマ状態から`PyriteStyle`・`darkTheme`・`dynamicColor`を渡します。Glyph由来のNothing風、Scheduler由来のM3E、ShutUp由来のAbout構造と意図的な差分は[source-port.md](source-port.md)を参照してください。

Pyriteのソース、隣接チェックアウト、ローカルMavenは通常の利用に不要です。再現性が必要な依存には可変の`master-SNAPSHOT`ではなく固定SHAを使います。targetSdkや関連しないライブラリは、UI導入と合わせて機械的に変更しないでください。

## UIの置き換え

| 元の構造 | PyriteのAPI |
| --- | --- |
| 設定の遷移行 / 切替行 | `SettingsNavigationItem` / `SettingsSwitchItem` |
| 説明と複数のRadio候補 | `SettingsRadioGroupItem` |
| 確定・取消のある単一選択 | `SettingsChoiceDialog`。下書きはホストが管理 |
| 遷移とSwitchが独立した行 | `SettingsSplitSwitchItem` |
| 詳細画面全体のON/OFF | `SettingsMasterSwitch`。下位のenabledはホストが渡す |
| Slider・独立Buttonを含む連結カード | `SettingsSliderItem` / `SettingsButtonItem` / `SettingsContentCard` |
| Aboutモザイク / 帰属 / 更新結果 | `AboutOverviewMosaic` / `AboutAttribution` / `AboutUpdateCard` |
| 共通ヘッダー / 画面遷移 | `PyriteScreenScaffold` / `PyriteScreenTransition` |

表示される項目だけを`SettingsGroup.item`に安定した一意のキーで登録します。通常のButton・Cardなどは公式APIを直接利用します。Scaffoldと画面遷移ホストを二重にしないでください。既存画面と名前が衝突する場合はimport aliasを使えます。

```kotlin
import jp.linkserver.pyrite.settings.SettingsScreen as PyriteSettingsScreen
import jp.linkserver.pyrite.about.AboutScreen as PyriteAboutScreen
```

## ホストで維持する契約

- 設定キー・保存形式・ViewModel/StateFlowを維持し、確定値をUIの別Booleanへ複製しません。確認ダイアログには下書きだけを保持します。
- ON/OFFと実際の稼働状態は分けます。ShutUpの自動維持のようにONのまま一時停止できる場合は、補足slotや`SettingsStatusItem`へ状態を渡します。
- 端末判定、権限、サービス、確認・復元ガード、保存失敗時の処理はアプリ側に残します。確認が必要なONは確定後だけ保存し、取消では保存しません。
- 各行の表示・無効条件を個別に維持します。開発者限定項目や模擬環境の条件を、通常項目へ広げないでください。
- 更新確認の重複防止、キャンセルの伝播、取得間隔・無視した版、結果通知、URL失敗時の処理を維持します。通信や更新ファイル管理はPyriteの責務ではありません。
- 上流リンク・帰属・OSSライセンスを維持します。PyriteのMIT本文とNOTICEはAARの`META-INF/pyrite/`に同梱されます。
- Ndot/NTypeはPyriteに同梱しません。権利を確認した利用アプリが管理し、[PyriteFonts](fonts.md)で役割ごとに渡します。

## 更新時の回帰確認

設定行・Aboutを段階的に置き換え、既存の保存・権限・通知・サービス・更新テストを維持します。Pyriteソースに依存しないアプリ単独のビルドでも取得を確認してください。

画面遷移はGlyph由来のCompose標準springです。固定300msを前提とするテストはアニメーション完了を待つ形にします。[画面遷移](navigation.md)と[カードの仕様](details-cards-and-motion.md)を参照してください。

Activity再生成時はWindowの初期化とInsets配信の順序に注意します。スクロールの保存値だけでなくviewportと末端位置も確認してください。[スクロール復元](scroll-restoration-guide.md)に原因と対策があります。

明暗・Dynamic Color、文字拡大・狭幅、戻る・再生成、更新の成功/失敗/取消、保存失敗を利用アプリでも確認します。Pyriteの[検証ガイド](verification-guide.md)はアプリ固有の回帰テストを代替しません。
