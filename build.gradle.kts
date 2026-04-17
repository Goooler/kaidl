@file:Suppress("UNUSED_VARIABLE")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(androidv.gradle)
        classpath(kotlinv.ksp.gradle)
        classpath(kotlinv.gradle)
    }
}

allprojects {
    plugins.withType<AndroidBasePlugin>().configureEach {
        extensions.configure<CommonExtension> {
            compileSdk = 37
            defaultConfig.apply {
                minSdk = 21
                vectorDrawables.useSupportLibrary = true
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions.apply {
                sourceCompatibility(17)
                targetCompatibility(17)
            }
        }
    }

    plugins.withType<JavaBasePlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
            setSourceCompatibility(17)
            setTargetCompatibility(17)
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(17.toString())
        }
    }
}
