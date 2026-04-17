plugins {
    id("com.android.library")
}

android {
    namespace = "com.github.kr328.kaidl"
}

dependencies {
    compileOnly(kotlinv.coroutine)
}
