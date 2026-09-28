# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Commands

Single-module Android app (module `app`). Use the wrapper (`./gradlew`), not a global `gradle`.

- Compile check (fast, no tests): `./gradlew :app:compileDebugKotlin`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Single test class: `./gradlew :app:testDebugUnitTest --tests "fully.qualified.ClassName"`
- Debug build: `./gradlew :app:assembleDebug`

## Architecture

Jetpack Compose UI, Hilt for DI, MVVM (`ViewModel` exposing `StateFlow<ViewState>` per screen).

## Changelog

After finishing a Trello ticket (e.g. `BC-30`), always add a changelog entry: run
`./add-changelog-entry.sh` interactively. Upon preparation of a release, entries are later collapsed
into `CHANGELOG.md` by `./release-changelog.sh`.

This file is intentionally minimal for now — add conventions and gotchas here as they come up.
