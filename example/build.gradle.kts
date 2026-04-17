plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 37
    namespace = "com.github.kr328.kaidl.test"
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    ksp(project(":kaidl"))
    implementation(project(":kaidl-runtime"))

    implementation(kotlinv.coroutine)
    testImplementation(testingv.junit.jvm)
    androidTestImplementation(testingv.junit.android)
    androidTestImplementation(testingv.espresso)
}
