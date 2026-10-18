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
