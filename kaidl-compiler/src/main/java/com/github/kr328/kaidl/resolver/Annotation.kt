package com.github.kr328.kaidl.resolver

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName

fun KSDeclaration.getAnnotationByName(name: ClassName): KSAnnotation? {
  return annotations.firstOrNull {
    checkNotNull(it.annotationType.resolve().declaration.qualifiedName).asString() ==
      name.canonicalName
  }
}

inline fun <reified T> KSAnnotation.getValue(key: String?): T? {
  // Try to find by parameter name first (for named arguments)
  var argument = arguments.firstOrNull { it.name?.asString() == key }

  // If not found and key is null, take the first argument (for positional arguments)
  if (argument == null && key == null && arguments.isNotEmpty()) {
    argument = arguments.first()
  }

  val rawValue = argument?.value ?: return null

  return when (T::class) {
    Int::class -> (rawValue as? Int) as? T
    String::class -> (rawValue as? String) as? T
    Boolean::class -> (rawValue as? Boolean) as? T
    else -> rawValue as? T
  }
}
