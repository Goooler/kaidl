# kaidl

[![Maven Central](https://img.shields.io/maven-central/v/io.github.goooler.kaidl/kaidl-compiler)](https://central.sonatype.com/artifact/io.github.goooler.kaidl/kaidl-compiler)

Kotlin-first Binder interface generation for Android.

`kaidl` gives you an AIDL-like workflow using plain Kotlin interfaces and KSP. You define an
interface once, annotate it, and use generated proxy/stub glue with strongly-typed APIs.

## Why kaidl

- AIDL-like IPC without maintaining `.aidl` files
- Kotlin interface-centric workflow
- Works with suspend functions
- Handles common Android IPC types, collections, parcelables, and nested binder interfaces

## Installation

1. Apply KSP in your module.
   ```kotlin
   plugins {
     id("com.google.devtools.ksp") version "<ksp-version>"
   }
   ```
2. Add the compiler and runtime dependencies.
   ```kotlin
   dependencies {
     ksp("io.github.goooler.kaidl:kaidl-compiler:<latest>")
     implementation("io.github.goooler.kaidl:kaidl-runtime:<latest>")
   }
   ```
3. Sync and build.
   ```bash
   ./gradlew build
   ```

**Notes**:

- Use a KSP version that matches your Kotlin version.
- If generated symbols are not visible in IDE, follow KSP guidance:
  https://github.com/google/ksp#make-ide-aware-of-generated-code

## Quick Start

Define an interface:

```kotlin
import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface EchoService {
  fun echoInt(value: Int): Int
}
```

Use generated helpers in service/client code:

```kotlin
class EchoImpl : EchoService {
  override fun echoInt(value: Int): Int = value
}

val localImpl = EchoImpl()
val binder = localImpl.wrap()
val remote = binder.unwrap(EchoService::class)

val result = remote.echoInt(42)
```

## Supported Types

- Primitives: `Int`, `Long`, `Boolean`, `Float`, `Double`, `String`, `Byte`, `Char`
- Primitive arrays: `BooleanArray`, `ByteArray`, `CharArray`, `DoubleArray`, `FloatArray`,
  `IntArray`, `LongArray`
- Android framework types: `Bundle`, `IBinder`, `SparseBooleanArray`
- Generic containers: `List<T>`, `Array<T>`, `Map<K, V>`, `Set<T>`, `Pair<A, B>`
- Parcelables: custom `Parcelable`
- Binder interfaces: other interfaces annotated with `@BinderInterface`
- Nullable variants where applicable
- Suspend functions

## Example Project

See the example interfaces and instrumentation tests:

- [example/src/main](https://github.com/Goooler/kaidl/tree/main/example/src/main)
- [example/src/androidTest](https://github.com/Goooler/kaidl/tree/main/example/src/androidTest)

For contribution workflow, see [CONTRIBUTING.md](./CONTRIBUTING.md).

## Credits

- [Kotlin Symbol Processing](https://github.com/google/ksp)
- [KotlinPoet](https://github.com/square/kotlinpoet)
