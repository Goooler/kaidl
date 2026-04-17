# kaidl

[![Maven Central](https://img.shields.io/maven-central/v/io.github.goooler.kaidl/kaidl-compiler)](https://central.sonatype.com/artifact/io.github.goooler.kaidl/kaidl-compiler)

Generate [AIDL](https://developer.android.com/guide/components/aidl)-like android binder interface with **Kotlin**



### Available Types

- Primitives

  - `Int` 
  - `Long`
  - `Boolean`
  - `Float`
  - `Double`
  - `String`
  - `Byte`
  - `Char`

- Primitive Arrays

  - `BooleanArray`
  - `ByteArray`
  - `CharArray`
  - `DoubleArray`
  - `FloatArray`
  - `IntArray`
  - `LongArray`
  - `SparseBooleanArray`

- Containers with Generic

  - `List<T>`
  - `Array<T>`
  - `Map<K, V>`
  - `Set<T>`

- Parcelables

  - Custom `Parcelable`
  - `Bundle`
  
- Active Objects
  
  - `Binder`
  - Other kaidl interfaces
  
  

### Usage

- Add 'KSP' to your project

  + Add ksp plugin repository in your project's `setting.gradle(.kts)`

     ```kotlin
     pluginManagement {
         repositories {
             gradlePluginPortal()
             google()
         }
     }
     ```

  + Apply plugin `kotlin-processing`
   
     ```kotlin
     plugins {
         id("com.google.devtools.ksp") version "latest"
         // ...other plugins
     }
     ```

- Add 'Kaidl' to your project

  + Add 'ksp' and runtime dependencies

    ```kotlin
     dependencies {
         ksp("io.github.goooler.kaidl:kaidl-compiler:latest")
         implementation("io.github.goooler.kaidl:kaidl-runtime:latest")
     }
     ```

- Example

  See also [test module](https://github.com/Goooler/kaidl/tree/main/example/src)

- Make IDE Aware Of Generated Code

  See also [ksp](https://github.com/google/ksp#make-ide-aware-of-generated-code)

### Credit

- [Kotlin Symbol Processing](https://github.com/google/ksp)
- [Kotlinpoet](https://github.com/square/kotlinpoet)
