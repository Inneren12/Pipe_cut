# Pipe_cut

Android application for calculating pipe-cut developments (unfoldings) used to mark pipes for manual cutting, grinding, gas/plasma cutting, or template preparation.

## Planned scope

- Flat pipe cuts.
- Saddle cuts (intersection with a partner pipe).
- **Rotated and offset cut-plane developments** — the distinguishing feature this app aims to support and existing calculators omit.

## Architecture

Gradle multi-module build:

- `:core` — pure JVM module with all geometry, math, and domain models. **Must not** depend on Android APIs (enforced by the `checkNoAndroidImports` Gradle task).
- `:app` — Android Compose application that depends on `:core`.

## Build

```bash
./gradlew :core:test :app:assembleDebug
```

## Test

```bash
./gradlew :core:test
```

## Verify core isolation

```bash
./gradlew :core:check
```
