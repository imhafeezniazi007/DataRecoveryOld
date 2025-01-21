pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://android-sdk.is.com/")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://android-sdk.is.com/")
        }
    }
}

rootProject.name = "DataRecoveryNew"
include(":app")
 