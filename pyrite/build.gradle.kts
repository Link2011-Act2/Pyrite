plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "jp.linkserver"
version = "0.1.0-SNAPSHOT"

android {
    namespace = "jp.linkserver.pyrite"
    compileSdk = 37
    resourcePrefix = "pyrite_"
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    testImplementation(libs.junit)
}

publishing {
    repositories {
        maven {
            name = "PyriteLocal"
            url = rootProject.layout.buildDirectory.dir("repository").get().asFile.toURI()
        }
    }
    publications {
        register<MavenPublication>("release") {
            artifactId = "pyrite"
            afterEvaluate { from(components["release"]) }
            pom {
                name = "Pyrite"
                description = "State-hoisted Compose settings and About UI with Material and Nothing-inspired styles."
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit/"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}
