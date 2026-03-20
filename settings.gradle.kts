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

rootProject.name = "AICompanion"

include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:audio")
include(":core:ai")
include(":core:automation")
include(":feature:voice")
include(":feature:chat")
include(":feature:avatar")
include(":feature:memory")
include(":feature:homecontrol")
include(":feature:tasks")
include(":feature:settings")
