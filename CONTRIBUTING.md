# Contributing to kaidl

Thanks for your interest in contributing to kaidl.

## Prerequisites

- JDK 17
- Android SDK (required for instrumentation tests)

## Development setup

1. Fork and clone the repository.
2. Create a feature branch from `main`.
3. Run the baseline build:

   ```bash
   ./gradlew build
   ```

## Code style

This project uses Spotless + ktfmt and `.editorconfig`.

- Check formatting:

  ```bash
  ./gradlew spotlessCheck
  ```

- Apply formatting:

  ```bash
  ./gradlew spotlessApply
  ```

## Testing

- Run regular build and unit checks:

  ```bash
  ./gradlew build
  ```

- Run Android instrumentation tests (requires an emulator/device):

  ```bash
  ./gradlew connectedCheck
  ```

## Pull requests

- Keep changes focused and minimal.
- Add or update tests when behavior changes.
- If you add a new feature, include coverage in tests and `example/src/androidTest`.
- Update `README.md` when feature additions change user-facing behavior or usage.
- Ensure formatting and relevant tests pass before opening a PR.
- Write clear commit messages and PR descriptions.
