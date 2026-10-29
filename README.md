# Namager 🎬

Namager is an Android application for browsing movies and TV series, viewing title details, and exploring playback-related options through a Persian-cinema-focused interface.

> **Project context:** This repository is a customized fork of an upstream open-source project. The screenshots show the app’s interface; review the commit history to distinguish upstream functionality from changes made in this fork.

## Screenshots

| Movies | TV series |
| --- | --- |
| ![Movies screen](screenshots/movies.jpg) | ![TV series screen](screenshots/series.jpg) |

| Title details | Settings |
| --- | --- |
| ![Title details screen](screenshots/details.jpg) | ![Settings screen](screenshots/settings.jpg) |


## Features

- **Catalogs:** separate Movies and Series tabs with a featured hero banner, genre chips and paginated loading.
- **Search:** debounced search that cancels outdated queries, so old results never replace newer ones.
- **Title details:** expandable synopsis, genres, rating, and seasons and episodes for series.
- **Playback:** pick a quality from a bottom sheet, then play in the built-in ExoPlayer, which supports configurable seek jumps and subtitle styling.
- **External options:** copy a link, or open a source in a browser, ADM, VLC, MX Player or KMPlayer.
- **Favorites:** save titles into custom groups, and track watched episodes.
- **Settings:** theme (system, dark or light), accent color, seek-jump length, and subtitle size and colors.
- **Persian-first UI:** RTL layout, Vazirmatn font, Persian digits and Persian error messages with retry.
- **Resilient networking:** requests fall back to mirror hosts, and failures are mapped to clear, typed errors.

## Tech stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM, Clean Architecture, use cases |
| Dependency injection | Hilt |
| Async and state | Kotlin Coroutines, Flow, lifecycle-aware collection |
| Networking | OkHttp, Retrofit, Moshi |
| Playback | AndroidX Media3 ExoPlayer |
| Images and animation | Coil, Lottie |
| Storage | SharedPreferences |
| Testing | JUnit, Robolectric, Compose UI Test |


## Attribution

Namager is based on an upstream open-source project. Credit for the original implementation belongs to its original authors. This repository documents the fork and its subsequent changes.

## License

Review the upstream project’s license and the license files included in this repository before redistributing or reusing code. No separate license is declared by this README.
