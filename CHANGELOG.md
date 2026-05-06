# Changelog

All notable changes to Pipe_cut will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added — PR9
- Pinch-zoom and two-finger pan on the 2D unwrapped development
  canvas via `Modifier.transformable`. Scale clamped to [0.5x, 8x];
  pan limited to keep ≥ 25% of the content on screen.
- Pinch-zoom and two-finger pan on the 3D pipe preview, plus
  one-finger drag rotates the pipe around its axial direction
  (`rotateAroundY`). The cabinet projection respects the rotation
  via the new `pipePreviewGeometry3D(... rotationYDeg)` overload.
- Double-tap on either canvas resets that panel's transform
  (and rotation, on 3D).
- New `CanvasViewModel` holds the per-panel transforms and
  rotation as independent `StateFlow`s. JVM unit tests cover the
  state transitions and the `clampTransform` / `rotateAroundY`
  math.
- `LazyListScope.canvasPanel` signature gains a second parameter
  for the ViewModel.
- No new gradle dependencies, no new public strings, no `:core`
  change.

### Changed — PR10 (fixup 2)
- `InputScreen` pre-flights `lastValidRequest == null` (and empty /
  over-length name) before opening the overwrite-confirm dialog.
  Previously a confirm-overwrite with no valid request set
  `lastSaveError = NoValidRequest` after both dialogs had already
  closed, hiding the failure from the user.
- `DataStorePresetsRepository.saveIfAllowed` now validates and
  normalizes the preset name at the storage boundary: rejects empty
  / whitespace-only / over-length names with the same typed errors
  the ViewModel surfaces, and trims surrounding whitespace before
  persisting. The ViewModel pre-flight stays as a UX shortcut.
- `saveIfAllowed` self-cleans corrupt entries inside `store.edit`
  before counting toward the duplicate / limit checks. Corrupt
  presets used to occupy slots invisibly, eventually triggering
  `LimitReached` on a UI showing fewer than 20 chips. The cleanup
  is silent — user-visible recovery affordances are PR12.
- Preset chip delete button is now a Material delete icon
  (`Icons.Filled.Delete`) with `deleteContentDescription(name)`
  exposed via `Modifier.semantics`. Replaces the temporary
  `Text("✕")` shipped in fixup 1, which announced poorly on
  TalkBack. `Icons.Filled.Delete` resolves on the existing
  classpath; no new gradle dependency was needed.

### Changed — PR6 (fixup 2)
- New `InputViewModel.applyPreset(preset)` atomically rewrites every
  form field, including explicit clears of all four saddle fields
  when the loaded preset has no saddle. Previously the screen called
  the per-field setters one at a time, leaving stale saddle values
  behind after loading a flat preset following a saddle preset.
- `applyPreset` returns a typed `PresetLoadError?` and refuses the
  load when `pointCountValue` does not map to a known `PointCount`.
  Previously a hybrid load could occur silently.

### Changed — PR10 (fixup)
- `PresetsRepository` gains
  `suspend fun saveIfAllowed(preset, maxPresets, allowOverwrite)`
  which performs duplicate / limit checks **inside** `store.edit { ... }`,
  closing a race against DataStore's initial-load window where
  `presets.value` was still the empty initial value.
- `PresetsViewModel.trySave` delegates to the repository for those
  storage-aware checks; it still pre-flights empty/too-long/null-request
  validation client-side.
- Name uniqueness and sort are now case-insensitive.
- New overwrite-confirm flow: a save attempt whose trimmed name
  matches an existing preset case-insensitively first surfaces a
  `"Name already exists. Overwrite?"` dialog. Confirming retries
  with `allowOverwrite = true`.
- Preset chips: `AssistChip(onClick = onLoad)` plus a small trailing
  delete `IconButton`. The previous `combinedClickable` long-press
  handling on the chip and the `LONG_PRESS_HINT` text are removed.
- `PresetsStrings` now owns the delete-confirm and load-error
  strings; no in-line literals in the bar.
- `PresetsRepository.save(preset)` is `@Deprecated` (kept for the
  pre-existing test path); removal scheduled for PR12.

### Added — PR10
- Named presets in `:app/ui/presets`:
  - `Preset` (`@Serializable`) and `CutRequest.toPreset(name)` extension.
  - `PresetsRepository` interface and `DataStorePresetsRepository`
    implementation backed by Jetpack DataStore Preferences.
  - `PresetsViewModel` with `presets: StateFlow<List<Preset>>` and a
    `trySave(name, request)` API that emits typed `PresetSaveError`
    on validation failure (empty name, too long, duplicate, limit
    reached, no valid request).
  - `LazyListScope.presetsBar(...)` extension and stateless
    `PresetSaveDialog` / `PresetDeleteDialog` Composables.
- `InputScreen` shows the preset bar above the form. Tap a preset to
  load it into every form field; tap the delete icon next to a chip
  to delete.
- Application-level service-locator pattern (`PipeCutApplication`)
  wires the DataStore to the ViewModel without DI.
- New gradle dependencies: `androidx.datastore:datastore-preferences`
  and `org.jetbrains.kotlinx:kotlinx-serialization-json`, plus the
  `kotlin-serialization` plugin.
- Unit tests: `Preset` round-trip, `DataStorePresetsRepository`
  round-trip and overwrite, `PresetsViewModel` validation matrix,
  copy-leak guard.
- Storage caps: max name length 40, max preset count 20.
### Added — PR8
- New canvas panel in `:app/ui/canvas`:
  - `DevelopmentCanvas2D` — unwrapped pipe surface with the cut line
    drawn as an open polyline with a virtual seam point at the right
    edge (φ = 0 is on the left, φ = 360 is on the right of the
    unwrapped sheet). The shape the welder traces onto a paper
    template, wraps around the pipe, and scribes.
  - `PipePreviewCanvas3D` — small isometric thumbnail of the pipe
    with the cut highlighted, for sanity-checking the entered
    geometry.
- `LazyListScope.canvasPanel(resultState, request)` extension wires
  both visuals into the screen-level `LazyColumn`, beneath the result
  table.
- `CanvasMath.kt` — pure JVM conversions (development-to-canvas,
  cabinet projection, unified `pipePreviewGeometry3D`) covered by
  unit tests including degenerate y-range, single-point edge cases,
  and a 3D fit-bounds invariant.
- `CanvasStrings` with a unit test that no constant leaks PR numbers.
- No new gradle dependencies, no new ViewModels.

### Changed — PR7 (fixup 3)
- `ResultUiState.Computed` now carries the `CutRequest` that
  produced its `Development`. `ResultUiState.Error` carries the
  `previousRequest` that produced `previous`. This closes a hidden
  bug where downstream consumers (canvas, future labels) could
  scale the displayed development by a different request after the
  user kept editing the form mid-error.
- `ResultViewModel` populates the new fields on every transition;
  unit tests assert the carry-over across Computed → Error chains.
- `resultPanel(state)` reads everything from `ResultUiState` (no
  separate `request` parameter); identical visual output.

### Changed — PR8 (fixup)
- 2D unwrapped development now draws as an **open polyline** plus a
  virtual seam point at the right canvas edge. The previous
  `lineTo(first)` close drew a diagonal across the full template
  that does not exist on the real cut.
- `canvasPanel` no longer takes a separate `request` argument; it
  reads the `CutRequest` from `ResultUiState` so canvas and table
  share the same source of truth and cannot drift.
- The duplicate canvas-empty hint is removed; the result panel's
  empty hint now serves both. `CanvasStrings.EMPTY_HINT` is
  `@Deprecated`, scheduled for removal in PR12.
- `CanvasMath` exposes one `pipePreviewGeometry3D(...)` returning a
  `PipePreviewGeometry` DTO, replacing the duplicated
  `cutCurve3D`/`rimCurves3D` public functions and ensuring the cut
  and rims share a single fit pass.
- `PipePreviewCanvas3D` consumes the DTO; the 3D cut **stays
  closed** (φ = 0 and φ = 360 are the same point on a cylinder), the
  2D unwrapped cut stays open. Different topology, different rule.

### Changed — PR7 (fixup 2)
- `ResultViewModel` now routes every request through
  `CutCalculatorDispatcher` (added in PR5). The previous direct
  `PlaneCutCalculator` call and the `SaddleNotImplemented` short-circuit
  are gone; saddle requests with valid geometry now produce real
  `Computed` results.
- `ResultUiState.SaddleNotImplemented` and
  `ResultStrings.SADDLE_NOT_IMPLEMENTED` are `@Deprecated`. They remain
  in the sealed hierarchy for one release to keep the diff focused;
  PR12 (polish) removes them along with the `resultPanel` branch.
- `ResultUiState` KDoc no longer references PR5 — the project history
  belongs in CHANGELOG, not in production code comments.
- New `ResultFormattersTest`: pins `formatPhi`/`formatLength` to
  `Locale.US` output regardless of the JVM default locale, so a
  device set to `ru-RU` or `de-DE` still prints `100.50`, not
  `100,50`, on the marking sheet.
- `ResultViewModelTest` updated: the saddle test now expects
  `Computed`, the helper builds a saddle request with safe positive
  geometry (`L₀ = 600`), matching the PR5 reference fixture.

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
