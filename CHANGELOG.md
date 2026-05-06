# Changelog

All notable changes to Pipe_cut will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added — PR4
- `PlaneCutCalculator` in `:core/math`: implements `CutCalculator` for the
  flat (non-saddle) cut case using `L(φ) = L₀ + R · tan(α) · cos(φ − β)`.
  Validates input via `CutRequestValidator` and self-checks output via
  `DevelopmentValidator`. Rejects saddle requests with a clear error.
- Comprehensive numeric tests:
  - Reference drawing case (D=114.3, α=28°, β=12°, L₀=100).
  - Straight, classic-non-clocked, and extreme-tilt cases.
  - Parameterized invariants over a (α, β, L₀) grid.
  - Output-size coverage for every `PointCount` entry.

### Added — PR3
- Validation in `:core/validation`:
  - `Validated<T>` sealed result type with `Valid` / `Invalid`.
  - `ValidationError` sealed hierarchy covering pipe, cut plane, saddle, and
    development invariants.
  - `CutRequestValidator` enforcing diameter > 0, tilt in [0°, 90°), clocking
    in [-360°, 360°], non-negative offset, and saddle-side rules when present.
  - `DevelopmentValidator` enforcing non-empty points, ascending unique phi in
    [0°, 360°), and non-negative lengths.

### Added — PR2
- Domain model in `:core/model`:
  - `PipeSpec` with derived `radiusMm` and `circumferenceMm`.
  - `CutPlane` with `tiltDeg` (α), `clockingDeg` (β — angular direction of
    plane tilt around the pipe axis), and `offsetMm` (L₀).
  - `SaddleSpec` covering partner diameter, intersection angle, clocking,
    and eccentric offset — complete enough that PR5 can implement saddle
    math without breaking the API.
  - `CutRequest` bundling pipe, cut, optional saddle, and point count.
  - `DevPoint` with explicit phi-direction documentation.
  - `Development` with nullable min/max length helpers.
  - `PointCount` enum: P12, P24, P36, P72, P120, P180, P360.
- `CutCalculator` interface in `:core/math` (no implementation yet).
- Unit tests for every model: PipeSpec, CutPlane, SaddleSpec, CutRequest,
  DevPoint, Development, PointCount.

### Removed — PR2
- PR1 `PipeCutCore` marker and its smoke test, replaced by real domain models.

### Added — PR1
- Initial Gradle multi-module project (`:core`, `:app`).
- Pure JVM `:core` module with `PipeCutCore` marker and JUnit 5 setup.
- `checkNoAndroidImports` Gradle task wired into `:core:check`, fails the build
  on any `import android.*` or `import androidx.*` in `:core/src/main`.
- Android `:app` module with Compose Material3, deterministic theme
  (no dynamic color), and a placeholder hello screen.
- GitHub Actions CI: build, test, and core-isolation check on push to `main`
  and on pull requests; debug APK uploaded as a 7-day artifact.
- Version catalog (`gradle/libs.versions.toml`) approved for this project.
- Project README and `.gitignore`.
