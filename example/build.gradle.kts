plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.github.kr328.kaidl.test"
}

dependencies {
    ksp(projects.kaidlCompiler)
    implementation(projects.kaidlRuntime)

    implementation(libs.coroutine)
    testImplementation(libs.junit.jvm)
    androidTestImplementation(libs.junit.android)
    androidTestImplementation(libs.espresso)
}
