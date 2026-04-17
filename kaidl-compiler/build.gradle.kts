plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  compileOnly(libs.ksp.api)
  implementation(libs.kotlinpoet)
}
