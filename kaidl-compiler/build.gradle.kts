plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.mavenPublish)
}

dependencies {
  compileOnly(libs.ksp.api)
  implementation(libs.kotlinpoet)

  testImplementation(libs.junit.jvm)
  testImplementation(libs.assertk)
  testImplementation(libs.kct.core)
  testImplementation(libs.kct.ksp)
}
