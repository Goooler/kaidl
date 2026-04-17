@file:Suppress("UNUSED_VARIABLE")

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
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    group = "com.github.kr328.kaidl"
    version = "1.15"
}
