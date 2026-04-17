pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
      google {
        mavenContent {
          includeGroupAndSubgroups("androidx")
          includeGroupAndSubgroups("com.android")
          includeGroupAndSubgroups("com.google")
        }
      }
      mavenCentral()
    }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "kaidl"

include(":kaidl-compiler", ":kaidl-runtime", ":example")
