# 配布とJitPack

## 配布するもの

既存の`:pyrite`のrelease publicationだけを配布します。サンプルAPKと検証consumerは公開モジュールに含めません。AAR、sources JAR、POM、Gradle Module Metadataを同じMaven Publish構成から生成します。

| 実行環境 | 座標 |
| --- | --- |
| 通常のローカル開発 | `jp.linkserver:pyrite:0.1.0-SNAPSHOT` |
| JitPack | `com.github.Link2011-Act2:Pyrite:<revision>` |

JitPack時は公式の環境変数`JITPACK`、`GROUP`、`ARTIFACT`、`VERSION`を使用します。通常の発行座標は変わりません。将来複数のライブラリを公開する際は座標規則を再確認してください。現在の公開対象はAAR一つです。

## ビルダー

`jitpack.yml`はJDK 21を選択し、`scripts/jitpack-prepare.sh`でSDK Platform 37とBuild Tools 36.0.0（既存ビルドの既定版）を用意します。使い捨てビルダー内でSDKライセンスを受諾します。SDKはJitPackの`ANDROID_HOME`を使用し、開発者の`local.properties`に依存しません。

固定済みのGradle wrapperで`:pyrite:publishReleasePublicationToMavenLocal`を実行します。`sh gradlew`とLF改行を使うため、Windowsで作成された実行権限に依存しません。GitHub Actions、Maven Central、追加プラグインは使用しません。

## 検証

JDK 21とSDKを設定したWindows開発環境で：

```powershell
# JitPackの座標・発行タスクをローカルで再現
.\scripts/verify-jitpack.ps1

# 公開・反映済みのcommit SHA、または承認済み0.x.xタグを使った実際の取得
.\scripts/verify-jitpack.ps1 -Revision '<公開済みrevision>'
```

省略時は`build/verification/jitpack-m2`を隔離したmavenLocalとして使用します。ユーザー共通の`~/.m2`には発行しません。`0.1.0-localcheck`はローカル検証専用で、JitPack上の公開版ではありません。

両モードともconsumerの最小ソースとwrapperだけを`build/verification/jitpack-consumer-local`または`-remote`へコピーし、clean後にビルドします。Pyriteのソース・project依存・included buildはありません。PyriteのGradle Module Metadataを使用せず、POMのcompile/runtime推移依存だけで利用できることを確認します。依存解決結果は`build/verification/jitpack-consumer-<mode>.txt`に記録します。

Gradleの共有ダウンロードキャッシュは使用します。完全なキャッシュ未保有のLinux環境とJitPack実サーバーでの成功は、リモート検証の完了後に記録します。ローカル検証をJitPack上での成功として扱わないでください。

通常のローカル開発には引き続き`scripts/verify-published.ps1`を使用します。

## 利用側

`settings.gradle.kts`：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            content { includeGroup("com.github.Link2011-Act2") }
        }
    }
}
```

`app/build.gradle.kts`：

```kotlin
// 公開済みの固定revisionに置き換えてください。
implementation("com.github.Link2011-Act2:Pyrite:<revision>")
```

Composeの有効化、対応するCompose Compiler plugin、SDK要件はREADMEを参照してください。UI / Foundation / Material 3 / AnimationはPOMで配布します。利用側でPyriteのソースをCloneする必要はありません。可変の`master-SNAPSHOT`は再現性を求める利用には推奨しません。

## リモート検証前の条件

設定を含むcommitがGitHubへ反映され、JitPackからソースを取得できる必要があります。タグやGitHub Releaseは必須ではなく、commit SHAによる検証も公式に対応しています。安定した配布版のタグは当面`0.x.x`とし、承認後に作成します。

2026-10-18、ユーザーがGitHubをPublicへ変更した後、認証なしGitHub APIで`private: false`・`visibility: public`とcommit `04d2c84`の取得を確認しました。ソースとAARをともに公開する方針です。認証情報は不要で、コミットもしないでください。

## 公式資料

- [ビルド・環境変数・カスタムコマンド](https://docs.jitpack.io/building/)
- [Android LibraryのMaven Publish](https://docs.jitpack.io/android/)
- [commit SHAでの利用](https://docs.jitpack.io/)
- [公開範囲・認証に関するFAQ](https://docs.jitpack.io/faq/)

masterへの反映はcommit `04d2c84`で完了しました。Public変更後もJitPack APIと同commitのPOM取得はHTTP 401で「Repo not found or no token provided」を返しました。公式FAQでは非公開判定を1時間キャッシュすると説明されています。現在はこのキャッシュ更新待ちと考えられます。GitHub側の公開設定は確認済みで、JitPackへの認証情報追加や依存バージョン変更は行いません。キャッシュ更新後に同じ固定commitで取得・consumerビルドを再試行します。
