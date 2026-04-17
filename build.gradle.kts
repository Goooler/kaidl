@file:Suppress("UNUSED_VARIABLE")

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.ksp) apply false
}

allprojects {
    plugins.withType<AndroidBasePlugin>().configureEach {
        extensions.configure<CommonExtension> {
            compileSdk = 37
            defaultConfig.apply {
                minSdk = 21
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions.apply {
                sourceCompatibility(libs.versions.jvmTarget.get().toInt())
                targetCompatibility(libs.versions.jvmTarget.get().toInt())
            }
        }
    }

    plugins.withType<JavaBasePlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
            setSourceCompatibility(libs.versions.jvmTarget.get().toInt())
            setTargetCompatibility(libs.versions.jvmTarget.get().toInt())
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
        }
    }
}
