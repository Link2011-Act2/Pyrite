# 階層型設定UI

SettingsMasterSwitchは機能全体のON/OFFを表す専用部品です。通常の連結設定行を置き換えるものではありません。

## APIと責務

```kotlin
SettingsMasterSwitch("授業前に通知", enabled, onEnabledChange)
SettingsGroup(title = "通知の詳細") {
    item("sound") {
        SettingsSwitchItem("音を鳴らす", sound, onSoundChange, enabled = enabled)
    }
}
SettingsSplitSwitchItem(
    title = "授業前に通知",
    summary = "通知時刻や音を設定します",
    checked = enabled,
    onCheckedChange = onEnabledChange,
    onClick = openDetails
)
```

- マスターはprimary/onPrimaryの全幅カプセル、最小72dp、横32dp・縦20dp余白。通常は完全なカプセルで、文字拡大・長文で高くなる場合も角丸36dpを保ち、輪郭による文字切れを防ぎます。背景色はcheckedを参照しません。
- Switchは公式Material 3で、Check/Closeを公式アイコンとして配置します。カード全体を1操作・1つのSwitch semanticsにまとめ、子の操作ノードは除きます。
- 下位項目のenabledは利用側が指定します。マスターOFFで下位の値を消去せず、ONへ戻したときに同じ値を再利用します。見出しは無効化しません。
- SettingsSplitSwitchItemは左側が遷移、右側がSwitchです。区切り線と独立した有効条件を持ちます。公開名はSettingsSplitSwitchItemに統一しています。
- 外観はテーマ内で管理し、背景・角丸・余白の新しい引数は設けません。M3Eは有彩色、NothingはGlyph由来の白黒パレットと通常ウェイトです。
- 通常のSettingsScreenの節間は28dp。階層型の検証画面ではマスターと下位グループを44dp離します。

## Google公式APIの確認

Material 3 1.5.0-beta01の実sources JARではSwitch.ktを確認し、専用のMaster/MainSwitchは確認できませんでした。[Material 3リリース](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-beta01)も参照しています。

[AOSP SettingsLib SPAのMainSwitchPreference](https://android.googlesource.com/platform/frameworks/base/+/caa1bd175752ac9e6170d5dde3aa5241f8f8c9c4/packages/SettingsLib/Spa/spa/src/com/android/settingslib/spa/widget/preference/MainSwitchPreference.kt)にはCompose実装があります。確認した版ではcheckedに応じてprimaryContainer/secondaryContainerを切り替え、SettingsTheme・EntryHighlight・SwitchPreferenceModel等を使います。Pyriteの既存AndroidX依存だけで直接呼べるAPIではなく、今回の固定背景要件とも異なるため導入していません。ソース自体のコピーも行わず、公式SurfaceとSwitchで必要な構造だけを実装しました。

## 参考画像との差分と確認方法

提供されたGoogle「自動修正」の画像は691px幅、カードがおおむね639×119pxで、端末幅を420dpと仮定すると約388×72dpです。このdp換算は画面からの推定です。カプセル形状、左タイトル／右52×32dpのSwitch、Check/Close、固定背景、OFF時の下位設定グレーアウトを採用しています。

端末のDynamic Colorが不明なため色の画素一致は比較対象にしません。PyriteのM3EではSchedulerの青系テーマ、NothingではGlyphの白黒になります。検証画面の外側余白はSettingsScreen共通の20dpで、参考画像の推定16dpより4dp広くなります。上部バー・文字・通常設定行も既存アプリ由来のテーマ規則を維持します。実際の文字サイズ・色・OSシステムバーまで完全一致させたものではありません。大きい文字で高くなる場合は完全なStadiumから36dp角丸へ移行します。初回画像で長い文字の端が切れたため、読みやすさを優先した差分です。

カタログの「階層型設定」から一覧→自動修正を開きます。一覧右Switchと詳細マスターは同じrememberSaveable値です。下位値も別に保存し、ON/OFF・明暗・M3E/Nothing・Activity再生成・往復後に維持します。callback回数を画面下へ表示します。

MasterSwitchTestで背景ピクセルのON/OFF一致、下位の無効化と値保持、親/子Switchのノード数、カード／Switch位置のタップで1回だけ発火することを検証します。CatalogFlowTestで一覧詳細と再生成・テーマ変更の経路を確認します。実績はverification.mdを参照してください。TalkBack実機の読み上げは別途必要です。
