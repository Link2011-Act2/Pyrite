# UIカタログ

既存の :app が検証アプリです。最初の設定画面から「About」で画面プレビュー、「UI」または「コンポーネント」で部品カテゴリを開きます。右上「Aa」はスタイル、「◐」は明暗です。

## 現在のデザイン

M3EはScheduler、Nothing風はGlyph Bartyを基準とします。ShutUpの帰属・更新カードも利用できます。形状・余白・文字階層はPyriteが管理し、恣意的な外観指定APIは増やしません。通常M3の独立スタイルは削除しました。詳細・元ファイル・元UIとの差分は[source-port.md](source-port.md)を参照してください。

Aboutは左92dpアイコン・右アプリ情報の上段と、Channel／Build・Versionの下段2列、48dpの接続ブリッジです。SettingsGroupを流用していません。Nothing固有フォントは同梱していません。狭幅／文字拡大時はScheduler由来の縦ヘッダー・可変高へ切り替わります。

## 公開UIの確認先

| API | 確認先 |
| --- | --- |
| PyriteTheme / PyriteStyle | 外観設定、カテゴリ右上。M3E/Nothing、ライト/ダーク/システム、M3E Dynamic Color |
| PyriteScreenScaffold / PyriteBackButton / PyriteScreenTransition | 全画面のヘッダーと単一遷移ホスト。[元UI・時間・API](navigation.md) |
| SettingsScreen / SettingsGroup | 最初の画面、設定行、複数グループの画面プレビュー。先頭/中間/末尾を動的に省略 |
| SettingsActionItem / SettingsNavigationItem | 設定行の遷移・カウンター・矢印 |
| SettingsSwitchItem | String summaryと補足slot、ON/OFF・無効・長文 |
| SettingsSplitSwitchItem | 設定行「元アプリの設定部品」。遷移とSwitchが別の操作 |
| SettingsRadioItem / SettingsRadioGroupItem | 設定行。連結単一選択とGlyph由来の複合Radio |
| SettingsInfoItem / SettingsStatusItem | 情報行、カードカテゴリの読み込み状態 |
| SettingsInfoCard / SettingsActionCard | カードカテゴリ。補足内容・無効・進行中 |
| SettingsChoice / SettingsChoiceDialog | ダイアログカテゴリ。下書き・確定・取消・無効候補 |
| AboutScreen / AboutOverviewMosaic | About画面とAboutモザイクカテゴリ |
| AboutOverviewContent / AboutAttribution | Aboutモザイク。Forked from表示・非表示、メタ情報未設定、長文、操作可否 |
| AboutAction / AboutActionGroup | Aboutモザイク下部。連結した説明・全幅アクション |
| AboutUpdateCard | Aboutモザイク下部。更新前/確認中/確認結果・操作不可 |
| 公式Button/Slider/LoadingIndicator等 | 公式コントロール。再実装・ラッパーなし |

画面プレビューはONと実効状態、条件付き項目、確認後だけONにするSwitch、Radio、About遷移を組み合わせます。端末設定・ネットワーク・Preferences・既存アプリのロジックは操作しません。

階層型設定カテゴリは一覧の分割Switchから詳細へ進み、マスターと下位項目を操作します。状態を共有し、OFFでも背景を維持します。APIと参考画像との差分は[hierarchical-settings.md](hierarchical-settings.md)を参照してください。

## 状態と検証手順

- ホストが確定値・下書き・進行中・表示条件を所有します。カタログはrememberSaveableと単一のPyriteScreenTransition内の画面キーごとのSaveableStateHolderを使用します。
- 外観でM3E/Nothingと明暗を往復し、操作状態が維持されることを確認します。Dynamic ColorはAndroid 12以降のM3Eで試せます。
- 最初の画面の150%文字・360dp幅を試します。OSの文字/表示サイズもAndroid設定から変更してください。アプリ内文字倍率はOS倍率に乗算されます。
- 設定行の長文、無効、先頭/中間/末尾を素早く切り替え、形状と値を確認します。
- 選択ダイアログの取消/戻る/確定、確認ダイアログのガードとOFF即時反映を試します。
- AboutモザイクでForked fromとメタ情報を省略し、長文・リンク無効・確認中を試します。
- テーマ変更、カテゴリ往復、画面回転、Activity再生成で値を確認します。
- TalkBackで見出し・各Role・選択/無効・状態通知・リンク・ダイアログフォーカスを確認します。自動semanticsテストは実際のTalkBack操作を代替しません。

補足slotへ親と競合する操作を入れず、独立操作には専用分割行・アクションカードを使ってください。可視行のkeyは一意・安定にします。

元関数を抽出した比較fixtureはandroidTest内だけにあります。SourceParityTestが同条件の画像と差分を保存します。比較範囲と実績は[verification.md](verification.md)を参照してください。

設定→About→「導入方法」からビルド情報詳細を開けます。進む／戻るのスライド、スクロール・表示設定の復元、連続した画面操作とActivity再生成を確認してください。カテゴリ内にも別の画面遷移ホストを置く構成は廃止しました。
