# Change Log


## [Unreleased](https://github.com/Goooler/kaidl/compare/0.1.0...HEAD) - 2026-xx-xx

### Added

- Support `java.util.Date` parcelization.

## [0.1.0](https://github.com/Goooler/kaidl/releases/tag/0.1.0) - 2026-04-17

### Fixed

- Fix generated `Parcelable` reads to use `parcelableCreator`.  
  Generated code now imports `kotlinx.parcelize.parcelableCreator`, so consumers using `Parcelable`
  types must have the Kotlin Parcelize plugin enabled (or the parcelize runtime dependency on the
  classpath); otherwise, the generated code will not compile.
