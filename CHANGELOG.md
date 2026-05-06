# Changelog

All notable changes to Pipe_cut will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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
