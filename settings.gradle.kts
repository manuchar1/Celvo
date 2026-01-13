enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Celvo"

include(":composeApp")
include(":core:data")
include(":core:domain")
include(":core:designsystem")
include(":core:presentation")
include(":feature:auth:domain")
include(":feature:auth:presentation")
include(":feature:chat:data")
include(":feature:chat:database")
include(":feature:chat:domain")
include(":feature:chat:presentation")
include(":feature:store")

includeBuild("build-logic")
