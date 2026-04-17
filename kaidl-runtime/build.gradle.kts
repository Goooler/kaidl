plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
}

android {
    namespace = "com.github.kr328.kaidl"
}

dependencies {
    compileOnly(kotlinv.coroutine)
}
