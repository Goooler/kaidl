rootProject.name = "kaidl"

include(":kaidl")
include(":kaidl-runtime")
include(":example")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
    }
    versionCatalogs {
        create("kotlinv") {
            version("common", "2.3.20")
            version("coroutine", "1.10.2")
            version("ksp", "2.3.6")
            version("poet", "2.3.0")

            library("gradle", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("common")
            library("coroutine", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutine")
            library("poet", "com.squareup", "kotlinpoet").versionRef("poet")
            library("ksp-api", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
            library("ksp-gradle", "com.google.devtools.ksp", "symbol-processing-gradle-plugin").versionRef("ksp")
        }
        create("androidv") {
            version("plugin", "9.1.1")
            library("gradle", "com.android.tools.build", "gradle").versionRef("plugin")
        }
        create("testingv") {
            version("junit", "4.13.2")
            version("androidJunit", "1.3.0")
            version("espresso", "3.7.0")

            library("junit-jvm", "junit", "junit").versionRef("junit")
            library("junit.android", "androidx.test.ext", "junit").versionRef("androidJunit")
            library("espresso", "androidx.test.espresso", "espresso-core").versionRef("espresso")
        }
    }
}
