# Cinematic View — Architecture Notes

## Layers

```
app/src/main/java/com/example
├── domain
│   ├── model        Media, Genre, Season, favourite/settings models
│   ├── repository   CatalogRepository, LibraryRepository, SettingsRepository
│   └── usecase      Catalog reads, search, details, favourite actions
├── data
│   ├── remote       CinemaApi + defensive JSON parser
│   ├── local        SharedPreferences data source
│   └── repository   implementations of the domain ports
├── di               Hilt modules (network, repositories, app scope)
└── ui               Compose presentation
    ├── navigation   Navigation-Compose graph and typed route helpers
    ├── screens      feature screens
    ├── viewmodel    one ViewModel per screen/feed
    ├── components   reusable design-system pieces
    └── theme        Material 3 cinematic theme
```

Dependencies point inward:

`ui -> domain`, `data -> domain`, `domain -> nothing`.

The UI never imports the data layer. The data layer never imports the UI. Domain
models contain no Android framework types.

## Navigation

`androidx.navigation:navigation-compose` is the single navigation source of
truth. The old code switched tabs through `ScreenTab` state and opened details
by replacing the whole composition.

Routes are defined in `ui/navigation/AppRoutes.kt`:

- `movies`, `series`, `search`, `favorites`, `settings` — bottom-level tabs
- `details/{mediaId}` — details screen
- `player?url=…&title=…&mediaId=…&seasonId=…&episodeId=…` — full-screen player

Top-level navigation uses `popUpTo(startDestination) { saveState = true }`,
`launchSingleTop` and `restoreState`, so tab scroll positions and filters are
preserved and back behaviour is predictable. Details and player are hidden from
the bottom bar automatically.

## State and error handling

- Each screen owns a small immutable UI-state type (`CatalogFeedUiState`,
  `SearchUiState`, `DetailsUiState`, …).
- Loading, empty, full-page error, pull-to-refresh and append/load-more error
  are explicit states instead of being inferred from booleans.
- The data layer converts transport/parse failures into typed `AppException`s.
  `ui/common/ErrorMessage.kt` maps them to Persian, user-facing copy.
- Pagination jobs are cancelled when filters change. Debounced search cancels
  the previous query and checks that the result still matches the active query
  before updating state.
- The library layer serializes and writes favourites, groups and watched
  episodes off the main thread. The old synchronous JSON reads on every
  recomposition are gone.

## UX and performance work

- Coil size-aware loading with medium filter quality and static placeholders;
  removed per-image `ImageRequest` construction and per-card shimmer animators.
- Stable keys and content types in `LazyVerticalGrid`; media cards are reusable
  and skeleton cards no longer start infinite animations.
- Hero pager stops its auto-scroll timer when it leaves the viewport.
- Player timeline polling runs only while playback is playing.
- ExoPlayer pauses when the activity stops, handles audio focus/noisy audio and
  keeps the screen on during playback.
- Detail episode/source lists use lazy items instead of composing an entire
  season inside one list item.
- Predictive back is enabled in the manifest; search resizes for the keyboard.

## Persistence compatibility

The preference file name remains `cinematic_prefs`, and the same keys are used
for favourites, groups, watched episodes and settings. Old favourite JSON
(which did not store genres/sources/country) is still readable; missing arrays
are treated as empty.

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The project uses AGP 9.1.1, Kotlin 2.2.10, Compose BOM 2024.09.00, Hilt and
Navigation Compose 2.8.9.
