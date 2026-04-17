# Change Log


## [Unreleased](https://github.com/Goooler/kaidl) - 2026-xx-xx

### Fixed

- Fix generated `Parcelable` reads to use `parcelableCreator`. Generated code now imports `kotlinx.parcelize.parcelableCreator`, so consumers using `Parcelable` types must have the Kotlin Parcelize plugin enabled (or the parcelize runtime dependency on the classpath); otherwise, the generated code will not compile.
