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

plugins { id("com.gradle.develocity") version "4.4.0" }

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
    val isCI = providers.environmentVariable("CI").isPresent
    publishing.onlyIf { isCI }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "kaidl"

include(":kaidl-compiler", ":kaidl-runtime", ":example")
