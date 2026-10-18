# 検証結果

検証日：2026-10-18（JST）。

## 採用構成

Material 3 1.5.0-beta01 / Compose 1.12.0 / AGP 9.4.1 / Gradle 9.6.0 / JDK 21。ライブラリminSdk 26、compileSdk 37、Javaバイトコード11。

Google Mavenのbeta01 AAR/POM/sourcesで公式APIと要件を照合し、この版でコンパイルしています。既存アプリの依存をダウングレードする対応は行っていません。

## ビルド・配布

| 検証 | 結果 |
| --- | --- |
| `:pyrite:assembleRelease` | 成功 |
| `:app:assembleDebug` | 成功 |
| `:app:assembleRelease`（R8最適化、未署名） | 成功 |
| `:pyrite:publishReleasePublicationToPyriteLocalRepository` | 成功 |
| 独立した`verification/consumer`の`:app:assembleDebug` | 成功 |
| `'-Ppyrite.usePublishedArtifact=true'`でカタログをビルド | 成功 |
| `scripts/verify-published.ps1`全体 | 成功 |

独立consumerはPyriteプロジェクトへの依存・included buildを持たず、`jp.linkserver:pyrite:0.1.0-SNAPSHOT`のみを参照しています。カタログのMavenモードでも`:pyrite`へのproject依存を使用しません。

ローカルMaven出力は`build/repository`です。AAR、POM、Gradle Module Metadata、sources JAR、チェックサムを確認しました。発行AARとassembleRelease出力のハッシュ一致、classes.jar内のMIT本文・NOTICE、class major version 55を確認しています。

## 自動テスト

- JVM：1件成功、失敗0（設定グループの重複キーを拒否）。
- Android UI：7件成功、失敗0、エラー0。Android 15 / API 35のPixel_6a AVDを使用。
- 既存の接続実機は使用していません。

Android UIテストの範囲：

1. 3スタイルでSwitchの操作属性、1回だけのcallback、無効状態を確認。
2. スタイルと明示的なライト／ダーク変更で呼び出し側の状態が保持されることを確認。
3. 選択ダイアログのdraft、取消、確定を確認。
4. 設定行を追加した際にkeyに対応するremember状態が維持されることを確認。
5. 幅280dp・文字サイズ200%でAboutの折り返しとスクロールを確認。
6. 設定→About→コンポーネント、戻る操作、Activity再生成後の状態復元を確認。
7. 実際のカタログのテーマ選択から3スタイル×ライト／ダークを操作し、設定状態と表示を確認。

全7件のHTMLレポートは`build/verification/project-tests/report/index.html`に保存しています。テストコードは`app/src/androidTest`、JVMテストは`pyrite/src/test`です。

## Lint

- ライブラリ：エラー0、警告0。
- カタログ：エラー0、警告9。8件のGradleDependencyと1件のNewerVersionAvailableで、依存更新通知のみです。
- 依存を採用基準に固定しているため、更新通知を理由に自動アップグレードしていません。警告の隠蔽・ベースライン化は行っていません。

## 検証中に修正した点

- beta01のTypographyに合わせ、fontFamily引数を使用。
- 公式のSliderStateを使うSlider APIへ更新。
- 公開ComposableのModifier引数位置を修正。
- 翻訳の書式指定、数量文字列、無効Switch、不要な初期テンプレート資源を整理。
- PowerShellではドットを含むGradleプロパティ引数全体を引用。READMEと検証スクリプトの両方で修正。
- 独立consumerのGradle既定512 MiBではメモリが不足したため、同プロジェクトにも2 GiBを明示。
- ホストのメモリ圧迫を避け、ビルドとエミュレーター実行を分離。今回の待機中Gradleデーモンだけを確認して終了。

## 残された範囲

- API 26端末、各メーカーの実機、TalkBack実機操作、性能・バッテリーの定量測定は未検証。
- リモートMavenレジストリへの公開、署名、認証情報は未設定。
- 数値・時刻編集、独立した遷移＋Switch、OSS一覧、状態モーフなどはREADMEの段階追加計画に残しています。
- 既存3アプリへの依存導入・移行ビルドは未実施です。

## 既存リポジトリの保持確認

Glyph Barty：HEAD 53e568c、変更なし。ShutUp：HEAD 34241e9、変更なし。

NITTC Scheduler：HEAD 26913ba。調査開始時から存在する以下の3変更を保持しています。

- ui/QrShareSelectionScreen.kt
- ui/SettingsScreen.kt
- ui/components/AppSettingsComponents.kt

Pyriteの既存未追跡`.idea/.name`も保持しました。
