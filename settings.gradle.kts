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
include(":baselineprofile")
include(":feature:bridge:connection-session:api")
include(":feature:bridge:connection-session:impl")
include(":feature:bridge:destination-nutrition:api")
include(":feature:bridge:destination-nutrition:impl")
include(":feature:bridge:destination-poi:api")
include(":feature:bridge:destination-poi:impl")
include(":feature:bridge:destination-session:api")
include(":feature:bridge:destination-session:impl")
include(":feature:bridge:nutrition-session:api")
include(":feature:bridge:nutrition-session:impl")
include(":feature:bridge:nutrition-settings:api")
include(":feature:bridge:nutrition-settings:impl")
include(":feature:bridge:poi-settings:api")
include(":feature:bridge:poi-settings:impl")
include(":feature:bridge:profile-session:api")
include(":feature:bridge:profile-session:impl")
include(":feature:bridge:session-settings:api")
include(":feature:bridge:session-settings:impl")
include(":feature:connections")
include(":feature:dashboard")
include(":feature:destinations")
include(":feature:locale")
include(":feature:nutrition")
include(":feature:poi")
include(":feature:profile")
include(":feature:sensor:power")
include(":feature:session")
include(":feature:settings")
include(":feature:theme")
include(":shared:altitude")
include(":shared:ble")
include(":shared:concurrent")
include(":shared:design-system")
include(":shared:di")
include(":shared:distance")
include(":shared:error")
include(":shared:graphics")
include(":shared:id")
include(":shared:location")
include(":shared:map")
include(":shared:observability")
include(":shared:sensor-protocol")
include(":shared:testing")
