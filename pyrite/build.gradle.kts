plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

// JitPack supplies these coordinates. Local development keeps its existing namespace.
val isJitPack = providers.environmentVariable("JITPACK").orNull == "true"
fun jitPackCoordinate(name: String): String = providers.environmentVariable(name).orNull
    ?.takeIf { it.isNotBlank() }
    ?: throw GradleException("JitPack must supply $name")
group = if (isJitPack) jitPackCoordinate("GROUP") else "jp.linkserver"
version = if (isJitPack) jitPackCoordinate("VERSION") else "0.1.0-SNAPSHOT"
val publishedArtifactId = if (isJitPack) jitPackCoordinate("ARTIFACT") else "pyrite"

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
            artifactId = publishedArtifactId
            afterEvaluate { from(components["release"]) }
            pom {
                name = "Pyrite"
                description = "State-hoisted Compose settings and About UI with Material and Nothing-inspired styles."
                url = "https://github.com/Link2011-Act2/Pyrite"
                scm {
                    url = "https://github.com/Link2011-Act2/Pyrite"
                    connection = "scm:git:https://github.com/Link2011-Act2/Pyrite.git"
                }
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
