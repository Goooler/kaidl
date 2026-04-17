plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.mavenPublish)
}

android { namespace = "com.github.kr328.kaidl.compiler" }

dependencies { compileOnly(libs.coroutine) }
