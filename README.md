# Pipe_cut

Android application for calculating pipe-cut development (unfolding) used to mark
pipes for gas/plasma cutting. Supports flat cuts and saddle cuts, including the
rotated cut ellipse (rotation of the cut plane around its own normal by angle β).

The project is a Gradle multi-module build:

- `:core` — pure JVM module with all math and domain models.
- `:app` — Android Compose application that depends on `:core`.

## Build

```bash
./gradlew :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/`.

## Test

```bash
./gradlew :core:test
```

Unit tests live in `:core` and run on the JVM with JUnit 5.
