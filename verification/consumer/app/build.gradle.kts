plugins {
    id("com.android.application") version "9.4.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}
android {
    namespace = "jp.linkserver.pyrite.consumer"
    compileSdk = 37
    defaultConfig {
        applicationId = "jp.linkserver.pyrite.consumer"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    // This is intentionally a Maven coordinate, never a project dependency or included build.
    implementation(providers.gradleProperty("pyrite.coordinate")
        .getOrElse("jp.linkserver:pyrite:0.1.0-SNAPSHOT"))
    implementation("androidx.activity:activity-compose:1.13.0")
}
