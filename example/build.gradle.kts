plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.github.kr328.kaidl.test"
}

dependencies {
    ksp(project(":kaidl"))
    implementation(project(":kaidl-runtime"))

    implementation(kotlinv.coroutine)
    testImplementation(testingv.junit.jvm)
    androidTestImplementation(testingv.junit.android)
    androidTestImplementation(testingv.espresso)
}
