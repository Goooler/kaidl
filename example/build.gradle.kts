plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "com.github.kr328.kaidl.test"
}

dependencies {
    ksp(project(":kaidl"))
    implementation(project(":kaidl-runtime"))

    implementation(libs.coroutine)
    testImplementation(libs.junit.jvm)
    androidTestImplementation(libs.junit.android)
    androidTestImplementation(libs.espresso)
}
