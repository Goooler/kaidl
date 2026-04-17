plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.github.kr328.kaidl.test"
}

dependencies {
    implementation(libs.coroutine)

    implementation(projects.kaidlRuntime)
    ksp(projects.kaidlCompiler)

    testImplementation(libs.junit.jvm)
    androidTestImplementation(libs.junit.android)
    androidTestImplementation(libs.espresso)
}
