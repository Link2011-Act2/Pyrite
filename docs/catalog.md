# UIカタログと標準デザイン

既存の :app が検証アプリです。最初の設定画面 → 右上の「UI」または「コンポーネント」からカテゴリを選びます。各カテゴリの右上「Aa」はスタイル、「◐」はライト／ダーク／システムの選択です。

## デザインの責務

Pyriteが3スタイルの形状・間隔・文字の階層・モーションを管理します。利用側は文言・状態・callback・補足コンテンツを渡します。今回、角丸・行間・Switchの配色等をアプリ別に指定するAPIは追加していません。既存PyriteThemeの明示的なoverrideは互換性のため維持しますが、今後の移行では標準値を使います。Dynamic Color、システムの文字拡大、ライト／ダークは維持します。

| スタイル | 標準の表現 |
| --- | --- |
| Material 3 | 公式ListItem、通常のMaterialThemeと標準モーション |
| Material 3 Expressive | 公式MaterialExpressiveTheme、segmentedShapesとSegmentedListItemによる連結・押下変形、expressive motion、公式LoadingIndicator |
| Nothing風 | 白黒の面と赤いアクセント、22/6dpの連結角丸、余裕のある独自行。専有フォントなし |

Aboutは従来の左右独立カードから、3dp間隔で角の位置を揃えた連結モザイクに変更しました。ShutUp固有の48dpブリッジを再現するAPIは設けません。幅420dp未満または文字倍率1.5以上では縦積みにして、固定高・行数制限を使わず読みやすさを優先します。広幅の補足情報は2列です。通常のAboutOverviewも同じ標準表示を使います。モザイクのバージョン操作と各補足情報の操作は独立します。

## 公開UIの確認先

| 公開API | カタログの場所と条件 |
| --- | --- |
| PyriteTheme / PyriteStyle | 最初の外観設定、各カテゴリのツールバー。3スタイル・明暗・システム・動的色 |
| SettingsScreen | 最初の設定画面、各カテゴリ、複数グループの画面プレビュー |
| SettingsGroup / item | 設定行：先頭・中間・末尾の表示を個別に切替。可視行だけから形状を計算 |
| SettingsActionItem | 遷移例、カウンター、カテゴリへの導線。無効／長文、String summary |
| SettingsSwitchItem | 通常のString summary、複数TextのsupportingContent。1つの操作対象、ON/OFF／無効 |
| SettingsRadioItem | 単一選択グループ、補足slot、無効候補 |
| SettingsInfoItem | 操作のない情報行 |
| SettingsStatusItem | 状態行。読み込みの有無、Polite live region、進行中semantics |
| SettingsInfoCard | 説明文と追加content |
| SettingsActionCard | 全幅ボタン、補足content、無効／読み込み、操作回数 |
| SettingsChoiceDialog / SettingsChoice | 選択・確認：下書き・確定・取消、長い説明、無効な候補 |
| AboutScreen / AboutOverview / AboutInfo / AboutDetail / AboutLink | 最初のAbout画面：アプリ情報・関連リンク・ホストの追加セクション |
| AboutOverviewMosaic | Aboutモザイク：長文、バージョン・チャネル・ビルドの操作、非操作表示 |

ボタン、Slider、進捗、単一選択ボタン、展開は「公式コントロール」でMaterial 3を直接使用しています。確認ダイアログも公式AlertDialogの使用例です。Pyriteには同義のラッパーを追加していません。

画面プレビューは複数グループ、ONと実効状態の分離、条件付き項目、ON時だけ確認するSwitch、Radio、Aboutへの遷移を組み合わせます。端末設定・ネットワーク・Preferences・既存アプリのロジックは操作しません。

## 状態とslotの契約

- ライブラリは状態を保存しません。確定値、ダイアログ下書き、処理中、表示条件はホストの責任です。
- カタログではrememberSaveableと画面／カテゴリごとのSaveableStateHolderを使います。テーマ・明暗・カテゴリ移動・Activity再生成でも値を保持します。Sliderの値も保存対象です。
- 設定行のsupportingContentは補足情報用です。親行と重複するSwitchやButtonを入れず、独立した操作には別行・アクションカードを使います。
- 説明カードのcontentはライブラリの内側余白と文字階層を使います。独自の画面全体やテーマをslotに入れる用途ではありません。
- 行のkeyは安定・一意にします。非表示にする行はitemを登録しません。非表示中も残す値はホストに保持します。

## 手動で確認する手順

1. 外観で各スタイルとライト／ダークを選び、設定・About・各カテゴリを確認します。Dynamic ColorはAndroid 12以上のM3/M3Eで確認します。
2. 最初の画面で150%文字、360dp幅を切り替えます。実際のOSの文字／表示サイズはAndroid設定で変更し、最大文字・横画面・広幅のエミュレーターでも確認します。アプリ内倍率は端末のfontScaleに乗算されます。
3. 設定行で長文、無効、先頭／中間／末尾の全組合せを試します。素早く切替え、最後の状態・連結形状・操作回数を確認します。
4. 選択ダイアログで選択後に取消／戻る／外側タップを試します。確定まで元の値を保つこと、無効候補を選べないことを確認します。
5. 確認ダイアログは確認済みSwitchをONにするまで確定不可です。画面プレビューでは取消でOFF、確定でON、OFFは即時反映です。
6. カテゴリ内の値を変え、テーマ・別カテゴリ・Activity再生成を往復します。Android開発者向け設定「アクティビティを保持しない」も確認方法の一つです。
7. TalkBackで見出し順、SwitchのON/OFF・無効、Radioの選択、状態変化、ボタン名、戻るとダイアログのフォーカス復帰を確認します。拡大時にスクロールできること、操作が二重発火しないことを確認します。

自動検証と未検証項目の実績は [verification.md](verification.md) を参照してください。semanticsテストはTalkBackの実操作を代替しません。

M3Eの公式SegmentedListItemの既定背景はSurfaceです。カタログ画像では画面背景と同化して連結形状が見えにくかったため、Pyrite標準では公式ListItemDefaults.segmentedColorsで通常・無効のcontainerをsurfaceContainerHighへ揃えました。選択色・押下形状・モーションは公式実装を使用します。外部から色を変える新APIは追加していません。通常M3の平面的なリスト表示と区別できます。

PyriteThemeは公式のM3Eテーマと通常テーマを切り替えるため内部の親構造が異なります。公式movableContentOfとrememberUpdatedStateで内容のidentityと最新lambdaを保ち、カテゴリや局所UI状態が切替で失われない構成にしています。設定保存の責務はホストのままです。参照：[AndroidX Movable Content設計](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/runtime/design/movable-content.md)。


説明・アクションカードはSettingsScreenの独立した節として配置します。連結グループの行にはSettings…Itemを使用してください。
