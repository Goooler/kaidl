plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.github.kr328.kaidl.compiler"
}

dependencies {
    compileOnly(libs.coroutine)
}
