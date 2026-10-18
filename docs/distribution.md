# 配布とJitPack

## 配布するもの

既存の`:pyrite`のrelease publicationだけを配布します。サンプルAPKと検証consumerは公開モジュールに含めません。AAR、sources JAR、POM、Gradle Module Metadataを同じMaven Publish構成から生成します。

| 実行環境 | 座標 |
| --- | --- |
| 通常のローカル開発 | `jp.linkserver:pyrite:0.1.0-SNAPSHOT` |
| JitPack | `com.github.Link2011-Act2:Pyrite:<revision>` |

JitPack時は公式の環境変数`JITPACK`、`GROUP`、`ARTIFACT`、`VERSION`を使用します。通常の発行座標は変わりません。将来複数のライブラリを公開する際は座標規則を再確認してください。現在の公開対象はAAR一つです。

## ビルダー

`jitpack.yml`はJDK 21を選択し、`scripts/jitpack-prepare.sh`でSDK Platform 37.0（API 37）とBuild Tools 36.0.0（既存ビルドの既定版）を用意します。使い捨てビルダー内でSDKライセンスを受諾します。SDKはJitPackの`ANDROID_HOME`を使用し、開発者の`local.properties`に依存しません。

JDK自動取得用Foojayプラグインは使用せず、JitPackのJDK 21（ローカルでは事前にインストールしたJDK 21）で実行します。SDK準備後はandroid.jarとBuild Toolsの存在を確認し、未取得ならビルド前に停止します。

固定済みのGradle wrapperで`:pyrite:publishReleasePublicationToMavenLocal`を実行します。`sh gradlew`とLF改行を使うため、Windowsで作成された実行権限に依存しません。GitHub Actions、Maven Central、追加プラグインは使用しません。

## 検証

JDK 21とSDKを設定したWindows開発環境で：

```powershell
# JitPackの座標・発行タスクをローカルで再現
.\scripts/verify-jitpack.ps1

# 公開・反映済みのcommit SHA、または承認済み0.x.xタグを使った実際の取得
.\scripts/verify-jitpack.ps1 -Revision '<current-api-commit-sha>'
```

consumerは現行APIを使用します。下記の公開確認済み4244bedは旧API時点の証跡です。現在のconsumerでは、今回のAPI整理を含むcommitをPush後にそのSHAを指定してください。旧版を利用する互換consumerは維持しません。

省略時は`build/verification/jitpack-m2`を隔離したmavenLocalとして使用します。ユーザー共通の`~/.m2`には発行しません。`0.1.0-localcheck`はローカル検証専用で、JitPack上の公開版ではありません。

両モードともconsumerの最小ソースとwrapperだけを`build/verification/jitpack-consumer-local`または`-remote`へコピーし、clean後にビルドします。Pyriteのソース・project依存・included buildはありません。PyriteのGradle Module Metadataを使用せず、POMのcompile/runtime推移依存だけで利用できることを確認します。依存解決結果は`build/verification/jitpack-consumer-<mode>.txt`に記録します。

JitPackのLinuxビルダーでSDK・wrapperの取得からAAR発行まで成功し、Windowsの独立consumerでも公開POMからのビルドが成功しました。consumerはcleanと--no-build-cacheを使用しますが、共有ダウンロードキャッシュは利用します。consumer側の完全に空の依存キャッシュや全OSでの実行は未検証です。

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
// 現行APIを含むcommitを公開後、その固定SHAへ置換してください。
implementation("com.github.Link2011-Act2:Pyrite:<current-api-commit-sha>")
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


## 実取得の結果

検証済みrevision：`4244bed7b36aa084c89d14338f176dbf87c0f263`。JitPack APIのstatusはok、privateはfalseです。認証情報を与えずにAAR・POM・sources JAR・Gradle Module Metadataを取得し、POMのみの推移依存で独立consumerをビルドしました。

JitPackのビルドは2分10秒、consumerのcleanビルドは2分21秒で成功しました。公開物のSDKメタデータ、Java 11形式、MIT・NOTICE、6つのKotlinソースを確認しています。公開AAR中の16 classファイルはローカルAARと同じハッシュでした。アーカイブ全体のバイト一致を保証するものではありません。

初回の401は公開設定・非公開判定キャッシュ更新後に解消しました。実ビルドでは不要なFoojayプラグインを削除し、SDK package IDをplatforms;android-37.0へ修正しました。続くComposeプラグイン解決失敗は同じ版の再試行で成功したため、一時的な取得失敗と判断しています。M3・Compose・SDK・AGP・Gradleの基準版は変更していません。

[成功したJitPackビルドログ](https://jitpack.io/com/github/Link2011-Act2/Pyrite/4244bed7b36aa084c89d14338f176dbf87c0f263/build.log)。ローカル証跡の場所はdocs/verification.mdを参照してください。