pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "PyriteLocal"
            url = uri(layout.settingsDirectory.dir("build/repository"))
            content { includeGroup("jp.linkserver") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Project Pyrite"
include(":app")
include(":pyrite")
