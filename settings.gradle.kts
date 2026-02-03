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
        google()
        mavenCentral()
    }
}

rootProject.name = "CyclingAssistant"
// alphabetically sorted
include(":app")
include(":feature:dashboard")
include(":feature:destination-nutrition:bridge:api")
include(":feature:destination-nutrition:bridge:impl")
include(":feature:destination-session:bridge:api")
include(":feature:destination-session:bridge:impl")
include(":feature:destinations")
include(":feature:locale")
include(":feature:nutrition")
include(":feature:nutrition-session:bridge:api")
include(":feature:nutrition-session:bridge:impl")
include(":feature:profile")
include(":feature:session")
include(":feature:session-settings:bridge:api")
include(":feature:session-settings:bridge:impl")
include(":feature:settings")
include(":feature:settings-nutrition:bridge:api")
include(":feature:settings-nutrition:bridge:impl")
include(":feature:theme")
include(":shared:altitude")
include(":shared:concurrent")
include(":shared:design-system")
include(":shared:di")
include(":shared:distance")
include(":shared:error")
include(":shared:graphics")
include(":shared:id")
include(":shared:location")
include(":shared:testing")
