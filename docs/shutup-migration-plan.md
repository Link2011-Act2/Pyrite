# ShutUp移行計画 — 既存UIを忠実に共通化

更新日：2026-10-18。調査対象：ShutUp 34241e9。**ShutUpのソース・依存・保存データは変更していません。** 導入はユーザーがカタログを確認し、別途許可した後です。

## 方針の変更

最新方針ではScheduler M3E・Glyph Nothingの元コードを基準に、ShutUpのAboutブリッジ・帰属・更新カードもライブラリ化します。アプリ別の自由な形状指定APIは追加しません。詳細は[source-port.md](source-port.md)を参照してください。

既存のNavigation、ViewModel、設定保存、端末判定、確認・復元のガードはアプリに残します。Pyriteは文言・状態・callbackを受け取る表示だけを担当します。新しいテーマ選択設定をShutUpへ追加する計画ではありません。

## 元ファイルと置換候補

パスは D:/apps/shutup/app/src/main/java/jp/linkserver/shutter/ を基準にしています。

| 元ファイル | 移行先と保持する責務 |
| --- | --- |
| ui/ShutterApp.kt SettingsScreen / ScreenScaffold | SettingsScreenとSettingsGroup。Navigation・BackHandler・表示条件はホストのまま。Scaffoldを二重にしない |
| ui/SettingsComponents.kt SettingsEntry | SettingsActionItem。言語設定・Aboutへのcallbackを維持 |
| 同 SettingsSwitch | SettingsSwitchItem。公式SwitchのCheck/Close表示を維持。ON/OFFと無効化・1回のcallbackは維持 |
| ui/AutoMaintainSettingsSection.kt | supportingContent付きSwitchまたはSettingsStatusItem。enabledとarmedを混同しない |
| ui/GalaxyControlSettingsSection.kt | Switch・説明／アクションカード。ON時の確認・復元処理はホストで継続 |
| ui/DeveloperUpdateSettingsSection.kt | SettingsGroupとSwitch。IntDev限定・各行の無効化条件はそのまま |
| ui/AboutScreen.kt | AboutScreen、AboutActionGroup、AboutUpdateCard。更新処理・結果・遷移はホスト |
| ui/AboutOverview.kt AboutOverviewMosaic | AboutOverviewMosaic。VersionとChannel等の補足操作をcallbackへ。上流リンク・帰属はAboutAttributionへ |
| 同 AboutSettingsActionCardContent | AboutActionGroup。サポート・更新・OSSの内容とcallbackを渡す |
| ui/AboutScreen.kt TextDialog | 公式AlertDialog＋SelectionContainer＋スクロールを維持 |
| ui/OssLicensesScreen.kt | 既存画面を維持しPyriteのMIT表示・本文を追加 |
| ui/Theme.kt / Type.kt | M3E画面をPyriteTheme標準へ。OSの明暗・動的色の選択はホストから渡す |

設定グループに表示される行だけを安定keyで登録します。slotは説明・状態等の内容に使用し、角丸・余白・文字スタイルの個別調整には使用しません。共通化後も参照されるSettingsComponentsを機械的に全削除しません。

## 明示するデザイン差分

| 現在のShutUp | 移行後 |
| --- | --- |
| 独自Surfaceによる22/6dpの連結行 | 元コード由来の22/6dp Surfaceと2dp間隔 |
| 太字・余白・見出しの独自指定 | Pyriteの標準文字階層と余白 |
| Check/Close付きSwitch | 公式SwitchのCheck/Close表示を維持 |
| 固有48dpブリッジ・カード寸法のAbout | 元の92dpアイコン・20dp角丸・48dpブリッジ。狭幅はScheduler由来の可変高・縦ヘッダー |
| 独自Typographyと色の細部 | Pyrite標準M3E。ライト／ダーク／Dynamic Color自体は維持 |

上流リンク・ロゴ・Version/Channelの情報と操作は失わないように配置します。操作箇所の配置変更はレビュー画像に示します。固定高さで説明を切ったり、既存の確認操作を省いたりしません。

## 振る舞いと保存の維持

| 対象 | 保持する契約 |
| --- | --- |
| 自動維持 | ONとarmedは別々。ONのまま一時停止を表示できる。初回提案・解除・復元・保存失敗の表示を維持 |
| Galaxy方式 | available時だけ表示。Switchは !busy && !hasChange、復元は !busy && canRestore。ONは確認後のみ保存、取消は保存なし、OFFは既存callback |
| 開発者設定 | IntDev限定。更新用2行と環境模擬3行を区別し、後者のworking条件を前者に勝手に追加しない |
| 言語設定 | API 33以上。アプリ言語画面→アプリ情報のfallbackとToastを保持 |
| 更新確認 | 重複防止、CancellationExceptionの再送出、結果liveRegion、latestTag、更新画面への遷移、URL失敗表示を保持 |
| ナビゲーション | 戻る・更新画面からの復帰・画面状態とダイアログの再生成を維持 |

Preferencesの名前・キー・形式を変更しません。対象は auto_maintain（enabled/armed/releaseOfferとSamsung同意）、galaxy_control（forceAudioPolicy）、warning_developer_settings（forceSamsung/forceNonMainUser/emulateCsc）、developer_csc_maintenance、github_release_updates（開発用2フラグ、cached_update/last_check_ms/last_check_version/dismissed_tag）です。模擬状態と実環境を混ぜず、既存の8時間制限や無視した版も維持します。

保存値の正本はViewModel/StateFlowです。UIが独立した保存用Booleanを持たず、確認ダイアログには下書きだけを保持します。最終ガードは既存Runtime/ViewModelに残します。Aboutの上流ShutterSoundToggleリンクと既存NYSL表示も残します。

## 依存の移行

| 項目 | 現状 → 検討内容 |
| --- | --- |
| compileSdk | 36 → 37が必要。targetSdk 36とminSdk 28の変更は含めない |
| AGP / Gradle / JDK | 9.4.1 / 9.6.0 / 21は維持候補。PyriteのJava 11バイトコードはShutUpのJVM 17から利用可能 |
| Material 3 | 明示1.5.0-alpha10 → 1.5.0-beta01 |
| Compose BOM | 2025.08.01がUI等1.9.0を指定。実際の解決結果を確認し、使用するCompose本体・tooling・テストを1.12.0へ揃える |
| Icons | BOMを外すならmaterial-icons-core 1.7.8を明示。存在確認なしに1.12.0へ揃えない |
| Compose Compiler plugin | 2.3.10を維持できるかAGP組込みKotlinとの実際の整合を確認。Pyriteの2.2.10に機械的に下げない |
| Activity / Lifecycle / WorkManager / Shizuku / Markwon等 | 関連しない更新は避け、依存グラフと既存機能を確認 |

beta01にはMaterialExpressiveTheme、LoadingIndicator、SwitchのthumbContent、MaterialShapes、Morph.toPath等が存在します。Homeや更新画面にも描画・アニメーションの影響があるため、設定画面以外も回帰確認します。ShutUpのbeta01移行コンパイルは未実施です。署名・BUILD_NUMBER生成・Release/Beta/IntDev判定も変えません。

## JitPack導入手順

配布設定のみの検証済みrevisionは 4244bed7b36aa084c89d14338f176dbf87c0f263 です。匿名取得・POM推移依存・独立consumerは検証済みですが、**今回追加したAPIをこのrevisionから取得することはできません**。

1. カタログの見た目・操作性をユーザーが確認する。
2. 必要APIを含むPyriteの固定commitについてJitPack取得と独立consumerを検証し、採用SHAを記録する。タグ・Releaseは許可なしで作らない。
3. ShutUp変更の別途許可後、JitPack repositoryと固定SHAの依存を追加する。
4. 依存整合だけの変更を先にビルド・テストし、その後設定とAboutを段階置換する。
5. Pyriteソースを含まない独立フォルダーへShutUpだけをCloneし、JDK 21とSDK 37でassembleDebugを確認する。完全な空キャッシュでの取得も別途確認する。

```kotlin
// settings.gradle.kts: Google/Maven Centralは維持
maven {
    url = uri("https://jitpack.io")
    content { includeGroup("com.github.Link2011-Act2") }
}
// app/build.gradle.kts: 実取得確認後のSHAに置換
implementation("com.github.Link2011-Act2:Pyrite:<必要APIを含む検証済みSHA>")
```

通常の第三者ビルドにproject依存・includeBuild・隣接Pyrite・ローカルMavenは要求しません。master-SNAPSHOTは採用せず固定SHAを使います。Pyrite開発用のローカルMaven手順は維持します。ShutUpのRelease署名には既存の正規環境を使い、debug鍵等へのfallbackは行いません。

## 受入条件と段階

依存整合 → 設定UI → Aboutカード・モザイク → 単独Cloneの順でレビューします。移行前のビルド・テスト・画像を記録し、変更後はデザイン差分と機能差分を別々に確認します。

既存のAutoMaintainSettingsTest、DeveloperUpdateSettingsTest、GalaxyControlSettingsTest、WarningDeveloperSettingsIntegrationTest、SamsungUserWarningPreferencesTest、SamsungUserWarningUiTest、NavigationTest、AppUpdateUiTest、HomeStatusEmblemTest、Controller/Repositoryのテストを維持します。Version/Channel、更新成功・失敗・取消、Activity再生成、保存失敗、1操作対象と読み上げも確認します。

API 28/31/33以降、Samsung/その他、Release/Beta/IntDev、日本語/英語、明暗/動的色、通常/150%/200%文字、狭幅/横画面を対象にします。未実施のアプリ移行テストをPyriteのカタログ検証で代替した扱いにはしません。

不具合時はUI変更と固定Pyrite revisionを組でrevertします。保存キーの変更、履歴の書き換えは行いません。フォント等の制限付きアセットは導入しません。
