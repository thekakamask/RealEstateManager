// Configures repositories used to resolve Gradle plugins declared in `plugins {}` blocks
pluginManagement {
    // Define plugin repositories
    repositories {
        /*google {
            content {
                // Limit plugin resolution to specific groups to improve performance and clarity
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }*/
        google()
        mavenCentral()
        gradlePluginPortal() // Provides access to community plugins
    }
}

dependencyResolutionManagement {
    // Centralizes dependency repositories and version catalog usage across all subprojects
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // Prevents project modules from defining their own repositories
    repositories {
        google() // Google's Maven repository (Android libraries)
        mavenCentral()  // Central Maven repository
    }
}

// Declare root project name and include app module
rootProject.name = "RealEstateManager"
include(":app")