pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(providers.gradleProperty("pyrite.repository")
                .getOrElse("../../build/repository"))
            content {
                includeGroup("jp.linkserver")
                includeGroup("com.github.Link2011-Act2")
            }
            // Also test consumers that only understand Maven POMs (as JitPack clients may).
            if (providers.gradleProperty("pyrite.pomOnly").orNull == "true") {
                metadataSources { mavenPom(); artifact(); ignoreGradleMetadataRedirection() }
            }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "PyritePublishedConsumer"
include(":app")
