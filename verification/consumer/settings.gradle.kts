pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("../../build/repository")
            content { includeGroup("jp.linkserver") }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "PyritePublishedConsumer"
include(":app")
