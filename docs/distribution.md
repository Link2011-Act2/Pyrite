# 配布とJitPack

## 配布するもの

`:pyrite`のrelease publicationだけを配布します。サンプルAPKと検証Consumerは公開モジュールに含めません。AAR、sources JAR、POM、Gradle Module Metadataを同じMaven Publish構成から生成します。

| 実行環境 | 座標 |
| --- | --- |
| 通常のローカル開発 | `jp.linkserver:pyrite:0.1.0-SNAPSHOT` |
| JitPack | `com.github.Link2011-Act2:Pyrite:<revision>` |

JitPack時は環境変数`JITPACK`、`GROUP`、`ARTIFACT`、`VERSION`を使用します。通常の発行座標は変わりません。現在の公開対象はAAR一つです。

MIT本文とNOTICEは`pyrite/src/main/resources/META-INF/pyrite/`からAAR内の`classes.jar`へ同梱します。AndroidTestの比較用ソースや専有フォントは配布しません。

## ビルダー

`jitpack.yml`はJDK 21を選択し、`scripts/jitpack-prepare.sh`でSDK Platform 37.0（API 37）とBuild Tools 36.0.0を用意します。SDKはJitPackの`ANDROID_HOME`を使い、開発者の`local.properties`に依存しません。準備スクリプトはSDKライセンスを受諾し、必要なパッケージの存在を確認します。

固定済みのGradle wrapperで`:pyrite:publishReleasePublicationToMavenLocal`を実行します。`sh gradlew`とLF改行を使うため、Windowsで作成された実行権限に依存しません。ローカルでもJDK 21とSDKを事前に用意してください。

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
implementation("com.github.Link2011-Act2:Pyrite:<commit-sha>")
```

必要APIを含む公開済みコミットの固定SHAを指定します。JitPackがGitHubからそのコミットを取得できる必要があります。タグやGitHub Releaseは必須ではありません。再現性が必要な利用には可変の`master-SNAPSHOT`を避けてください。

Composeの有効化、対応するCompose Compiler plugin、SDK要件は[README](../README.md#ビルド要件)を参照してください。UI / Foundation / Material 3 / AnimationはPOMで配布します。利用側でPyriteのソースをCloneする必要はありません。

## 独立Consumerでの検証

JDK 21とSDKを設定したWindows環境で実行します。

```powershell
# 通常のローカルMaven発行・独立Consumer・発行AAR版カタログ
.\scripts/verify-published.ps1
# JitPack座標と発行タスクをローカルで再現
.\scripts/verify-jitpack.ps1
# 公開済みの固定SHAをJitPackから取得
.\scripts/verify-jitpack.ps1 -Revision '<commit-sha>'
```

Revision省略時は`build/verification/jitpack-m2`を隔離したmavenLocalとして使い、ユーザー共通の`~/.m2`へ発行しません。`0.1.0-localcheck`はローカル検証専用です。

両モードとも`verification/consumer`の最小ソースとwrapperだけを`build/verification/jitpack-consumer-local`または`-remote`へコピーし、clean後にビルドします。Pyriteのソース・project依存・included buildを使わず、Gradle Module MetadataではなくPOMの推移依存で利用できることを確認します。

依存解決結果は`build/verification/jitpack-consumer-<mode>.txt`に出力します。Consumerは`--no-build-cache`を使いますが、共有ダウンロードキャッシュは利用します。ローカル発行の成功とJitPackサーバーでのビルド成功は別に確認してください。その他の確認方法は[検証ガイド](verification-guide.md)を参照してください。

## 公式資料

- [ビルド・環境変数・カスタムコマンド](https://docs.jitpack.io/building/)
- [Android LibraryのMaven Publish](https://docs.jitpack.io/android/)
- [commit SHAでの利用](https://docs.jitpack.io/)
- [公開範囲・認証に関するFAQ](https://docs.jitpack.io/faq/)
