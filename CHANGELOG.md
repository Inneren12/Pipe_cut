# Changelog

All notable changes to Pipe_cut will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed — PR7 (fixup)
- `InputScreen` is now a single root `LazyColumn`. The previous
  `Column.verticalScroll` + nested `LazyColumn` combination crashes
  Compose on first measurement; this fix removes the crash.
- `InputForm`'s outer `verticalScroll` was removed for the same reason
  — scrolling is owned by the screen-level `LazyColumn`.
- The result table now has a **real** sticky header via
  `LazyListScope.stickyHeader`, replacing the previous non-sticky
  header that was promised in CHANGELOG.
- `ResultTable` exports a `LazyListScope.resultPanel(state)` extension
  instead of a `@Composable fun ResultTable`. The screen's `LazyColumn`
  invokes it; no Compose API leaks remain.
- Saddle banner text changed from "Saddle cuts arrive in PR5" to
  "Saddle cuts are not available in this build." — PR numbers no
  longer leak into the UI.
- All `(φ, L)` formatting uses `Locale.US`. Engineering output stays
  deterministic regardless of the device locale.
- `ResultUiState.Empty` KDoc corrected: it shows a hint, not nothing.
- Removed an unused `viewModelScope` placeholder from `ResultViewModel`.

### Added — PR7
- `ResultViewModel`, `ResultTable`, `ResultUiState`, `ResultStrings` in
  `:app/ui/result` and `:app/ui/vm`:
  - Subscribes to `InputViewModel.lastValidRequest` and runs
    `PlaneCutCalculator` on every new flat-cut request.
  - Renders the `(φ, L)` points as a table with sticky header.
  - Saddle requests show a placeholder banner ("Saddle cuts arrive in
    PR5"); a follow-up PR will swap to `CutCalculatorDispatcher` once
    PR5 is merged.
  - Calculator exceptions surface as an in-line error banner; the
    previous successful result remains visible beneath it (sticky UX).
- `InputScreen` now scrolls the form and the result table together.
- Unit tests cover Empty / SaddleNotImplemented / Computed / Error
  transitions and the sticky `previous` carry-over.

### Changed — PR6 (fixup)
- CI now runs `:app:testDebugUnitTest` and `:core:check` so the new
  ViewModel and string-mapping tests gate every merge.
- `InputViewModel.lastValidRequest` is now sticky: once a valid
  `CutRequest` is captured, subsequent invalid edits do not clear it.
  Downstream consumers (table, canvas) can keep displaying the previous
  result while the user is mid-edit.
- Keyboard types corrected per field: `KeyboardType.Decimal` for
  unsigned numeric fields, `KeyboardType.Phone` for clocking fields so
  the minus sign is reachable.
- `InputScreen` collects `uiState` via `collectAsStateWithLifecycle()`,
  added `androidx.lifecycle:lifecycle-runtime-compose`.
- CHANGELOG description corrected: Composables are not "fully stateless";
  ephemeral dropdown expansion is local state.
- `FieldKey.POINT_COUNT` annotated as currently un-validatable but kept
  in the touched set for uniformity.

### Added — PR6
- Compose input form for the cut request:
  - `InputScreen` / `InputForm` Composables with business state hoisted to
    the `InputViewModel`; only ephemeral dropdown expansion is kept locally.
  - `InputViewModel` exposing `uiState: StateFlow<InputUiState>` and
    `lastValidRequest: StateFlow<CutRequest?>` for downstream PRs (table,
    canvas) to consume.
  - Per-field touched tracking; errors only show after the user has edited
    a field or tapped "Calculate".
  - Inline mapping from typed `ValidationError` variants to English strings
    in `InputFieldStrings`.
  - Saddle toggle with collapsible group of four saddle-only fields.
  - Decimal separator tolerance (accepts `,` and `.`).
- JUnit 5 wired in `:app` for ViewModel and string-mapping tests.
### Changed — PR5 (fixup)
- `SaddleCutCalculator` now rejects requests where `cut.tiltDeg` or
  `cut.clockingDeg` is non-zero. PR5 implements pure saddle geometry
  only; combined plane-trim is out of scope.
- Tightened the eccentric precondition: branch radius + offset must be
  ≤ partner radius for a full 360° development. The previous check was
  redundant with the `r2 ≥ r1` rule and let invalid eccentric requests
  fail late inside the bisection loop.
- Documented the real-world drawing's geometry bound by adding a test
  that L₀ = 250 with branch Ø114.3 + main Ø914.4 + θ = 62° is rejected
  as offset-too-small. The positive fixture remains at L₀ = 600.
- Eccentric solver now has a back-substitution test that verifies every
  output point lies on the partner cylinder within `1e-9`.
- Removed leftover `assertNotEquals("", message)` line and unused import.

### Added — PR5
- `SaddleCutCalculator` in `:core/math`: implements `CutCalculator` for
  the saddle case (branch pipe fitted onto a partner cylinder).
  - Analytic solution for centered intersections (`saddle.offsetMm == 0`).
  - Bisection-based numeric solver for eccentric intersections.
  - Plane-specific preconditions: rejects `R₂ < R₁`, `R₁ > R₂ + e`, and
    `L₀ < min(z(φ))`.
- `CutCalculatorDispatcher` in `:core/math`: single entry point that
  routes to `PlaneCutCalculator` or `SaddleCutCalculator` based on
  `request.saddle`.
- Reference test fixture: branch Ø114.3, main Ø914.4, θ = 62°, L₀ = 600
  — safe positive-length geometry derived from the user's drawing.
  L₀ = 250 from the drawing itself is too small for this geometry and
  is documented by a dedicated negative test.
- Numeric tests for perpendicular tee, clocking, eccentric offset
  consistency, and dispatcher routing.

### Changed — PR4 (fixup)
- `PlaneCutCalculator` now rejects plane-cut requests whose minimum
  generated length would be negative (`L₀ < R · tan(α)`). The cut would
  need to begin behind the chosen end face, which is geometrically invalid.
  Reported as `IllegalArgumentException`, not as the previous misleading
  `IllegalStateException("This is a bug")`.
- Use `PipeSpec.radiusMm` instead of inline `diameterMm / 2.0` for clarity.

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

### Changed — PR3 (fixup)
- `CutRequestValidator` and `DevelopmentValidator` now reject `Double.NaN`,
  `Double.POSITIVE_INFINITY`, and `Double.NEGATIVE_INFINITY` for every
  numeric field, using the existing error variants (no API change).
- `Validated.invalid(errors: List<…>)` defensively copies the input list so
  later mutations cannot break the non-empty invariant.
- `Development.maxLengthMm` and `Development.minLengthMm` are now `Double?`
  (via `maxOfOrNull` / `minOfOrNull`), matching the PR2 v2 spec. Empty
  developments no longer throw on access.

### Added — PR3
- Validation layer in `:core/validation`:
  - `Validated<T>` sealed type with `Valid` / `Invalid` (accumulating errors).
  - `ValidationError` sealed interface — typed catalog of every rule violation,
    each variant carrying the offending value.
  - `CutRequestValidator` — single-pass validator for `CutRequest` (pipe,
    cut plane, optional saddle). Accumulates all errors.
  - `DevelopmentValidator` — single-pass validator for calculator output
    (non-empty, phi in [0, 360), non-negative length, strictly ascending phi).
- Unit tests covering positive cases, every error variant, and error
  accumulation for both validators.

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
