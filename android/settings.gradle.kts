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
        google()
        mavenCentral()
    }
}

rootProject.name = "ZernikalosDemoApp"
include(":app")

// Local Zernikalos KMP engine: substitute Maven coordinates with the :engine project from the sibling repo.
includeBuild("../../Zernikalos") {
    dependencySubstitution {
        substitute(module("dev.zernikalos:engine:0.9.0")).using(project(":engine"))
    }
}
