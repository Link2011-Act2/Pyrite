# 既存UIの忠実な移植（2026-10-18）

この文書を、従来の「Pyrite独自標準デザインへ変更する」方針より優先します。対象はM3EとNothing風の2種類です。通常Material 3の独立したenum値は削除しました。テーマの形状・余白・文字はPyriteで管理し、互換用のoverride引数は削除しました。

## 再調査と移植対象

元パスの基点は各リポジトリのapp/src/main/javaです。3アプリのソースは読み取りのみです。

| 対象 | 元ファイル | 元の特徴と採用内容 |
| --- | --- | --- |
| Scheduler M3E設定 | jp/linkserver/nittcsc/ui/components/AppSettingsComponents.kt | 22/6dp連結角丸、2dp間隔、22×18dp余白、72dp最小操作高、太字16sp、説明14sp、Check/Close付き公式Switch。Surface/Rowを移植 |
| Schedulerテーマ | jp/linkserver/nittcsc/ui/theme/Color.kt, Theme.kt, Type.kt | 青系の固定明暗パレット、ExpressiveTypography、6/14/22/28/36dpテーマ形状を移植。Dynamic Colorは公式API |
| Glyph Nothing設定 | jp/linkserver/glyphvisualizer/ui/SettingsListComponents.kt | 同じ22/6dpと2dp間隔、通常ウェイト、公式Switch、22dpの遷移矢印、説明と選択肢を持つ複合Radio |
| Glyphテーマ | jp/linkserver/glyphvisualizer/ui/theme | 黒白と赤、surface階層、文字サイズ・行高・字間を移植。再配布制限付きフォントは除外 |
| Scheduler About | jp/linkserver/nittcsc/ui/AboutOverviewMosaic.kt | 上段176dp最小高、左92dp丸アイコン、右30sp/36sp太字、下段2列。狭幅時の64dpアイコン縦配置と内容に応じた高さ |
| Glyph About | jp/linkserver/glyphvisualizer/ui/AboutScreen.kt | 上段176dp、アイコン92dp、名前27sp/32sp、下段正方形2列、48dpブリッジと小さい切欠き。Nothing側の基準 |
| ShutUp About | jp/linkserver/shutter/ui/AboutOverview.kt, AboutScreen.kt | Glyph由来の共通構造。Forked from、上流リンク、更新説明・仕切り・結果・確認ボタン、連結アクションカードを追加 |
| ShutUp設定 | jp/linkserver/shutter/ui/SettingsComponents.kt, AutoMaintainSettingsSection.kt, GalaxyControlSettingsSection.kt, DeveloperUpdateSettingsSection.kt | 連結設定行・説明・有効条件の共通構造を確認。環境判定・確認ガード・保存・ViewModelは移植しない |

参照HEADはGlyph 53e568c、Scheduler 26913ba、ShutUp 34241e9です。SchedulerのSettingsScreen / AppSettingsComponents等には調査開始時点から未コミット変更があり、その実装も基準にしています。NOTICE.mdに帰属を保持します。

## Aboutの構造

元コードは「上段の横長カード1枚＋下段のChannelカードとBuild/Versionカード2枚」です。下段の右カード内部がBuildとVersionに分かれます。4枚の独立した設定カードへ分解しません。

- モザイク角丸20dp、隙間3dp、ブリッジ長48dp、重なり1dp、端の切欠き半径1.5dp。
- 上段は左右余白24dp・上下22dp・アイコンと情報の間22dp。下段余白22dp。
- ChannelとVersionは25sp/29sp、BuildはbodySmall/16sp。表示する値は呼び出し側で整形します。
- About専用のAboutOverviewContentでChannel / Build / Versionを明示します。nullは表示しません。
- AboutAttributionは任意です。URLを開く処理はcallbackのみです。
- AboutActionGroupは元の22/6dp連結カードと全幅Buttonです。SettingsGroupを流用しません。
- AboutUpdateCardはShutUp由来の説明・仕切り・状態・Buttonです。通信・時刻・キャッシュは持ちません。

## 公式APIの採否

[公式リリース](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0-beta01)と実際に解決するbeta01のコンパイルで確認します。MaterialExpressiveTheme / MotionScheme.expressive / Switch / RadioButton / AlertDialog / LoadingIndicatorを利用します。Check/CloseとRTL矢印はGoogleのmaterial-icons-core 1.7.8です。既存のMaterial 3・Compose版は変えていません。

SegmentedListItemは存在しますが、余白・角丸・押下モーフが元アプリのSurface/Rowと異なります。そのため設定行を同APIへ置換しません。独自Switchや補間エンジンは作りません。連結形状とAboutブリッジだけが元コード由来の独自描画です。

## 公開API

現行APIの主な構成です。削除・統合と使い分けは[public-api.md](public-api.md)を参照してください。

- SettingsNavigationItem：公式の自動反転矢印付き。
- SettingsSplitSwitchItem：左の遷移と右のSwitchが独立した操作。有効条件も独立。
- SettingsRadioGroupItem：1つの設定カードに説明と選択肢。
- AboutOverviewContent / AboutAttributionと、明示モデルを受け取るAboutScreen / AboutOverviewMosaic。
- AboutAction / AboutActionGroup / AboutUpdateCard。
- SettingsMasterSwitch / SettingsSplitSwitchItem：[階層型設定](hierarchical-settings.md)の追加要件。

AboutはAboutOverviewContentの名前付きchannel / build / versionを使います。任意の追加情報はAboutScreenのcontentへ明示します。ChannelはonChannelClick、右のBuild/Versionカードと上段はonVersionClick、帰属リンクはonAttributionClickで操作を分離します。

## 元UIからの意図的な差分

1. NothingのNType/Ndotは同梱しません。名前はシステムSansSerif、VersionはMonospaceです。文字設定はPyriteで管理し、元フォントとの画素一致は保証しません。
2. Glyphの固定高は大きい文字・長文で切れるため、Schedulerに存在する最小高＋内容サイズと狭幅ヘッダーを共通化します。通常寸法は保持します。
3. GlyphのSwitch/Radioはthumbだけが操作対象でしたが、Pyriteでは行全体を単一の意味・48dp以上の操作対象とします。分割操作だけは遷移とSwitchを分けます。
4. 更新カードは文字拡大・狭幅で状態とボタンを縦配置します。通常の横配置はShutUp準拠です。
5. M3Eの名前・テーマはScheduler基準で、ShutUpの27spとは異なります。NothingはGlyphの27spです。
6. アプリアイコン、版・Channel・文言、サポート先、既知の問題などの実データはホストから渡します。更新取得、Markdownエンジン、ログ書き出し、Shizuku、Glyph SDKは含めません。

広幅のM3E設定はSchedulerと同じ最大960dp、Aboutは840dp、モザイクは520dpです。NothingのAbout本文は幅を制限しませんが、モザイクだけは共通の520dp上限を使います。Glyphの元モザイクは無制限だったため、タブレット等ではこの上限が差分になります。

SettingsInfoCard / SettingsActionCardは説明・補足slot・進行中の設定操作に使います。About専用の連結配置・更新結果カードとは役割が異なるため維持します。忠実なAbout移植には専用のAboutActionGroup / AboutUpdateCardを使用します。

## 比較検証

app/src/androidTest/.../referenceは元関数の抽出fixtureです。package・関数名、アプリ資源参照、元テーマのモード参照のみをテスト用に置換します。Aboutのアイコンは両側同じ仮アイコン、制限付きフォントは両側同じシステムフォントです。ライブラリ本体・公開AARへfixtureは入りません。

SourceParityTestで360dp・fontScale 1・明暗を揃え、Scheduler M3EとGlyph NothingのAboutとSwitch、Glyphの連結遷移行を撮影し、ピクセル差分を保存します。これは元Composableとの比較であり、元アプリのAPK全体を同時実行した比較ではありません。文言・アセット・アプリ固有状態まで一致したと誤解しないでください。最終結果と未検証範囲はverification.mdへ記録します。
