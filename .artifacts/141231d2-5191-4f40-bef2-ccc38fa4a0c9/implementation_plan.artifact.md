# Dagger Hilt & Clean Architecture Integration Plan

Integrate Dagger Hilt for dependency injection and structure the project following Clean Architecture principles (`data`, `domain`, `di`, `ui`).

## User Review Required

> [!IMPORTANT]
> - Adds Dagger Hilt dependencies and KSP compiler configuration.
> - Creates `CinemaApplication` class and updates `AndroidManifest.xml`.
> - Reorganizes repository interfaces into `domain` and implementations into `data`, bound via Hilt modules.

## Proposed Changes

### Build Configuration
#### [MODIFY] [libs.versions.toml](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/gradle/libs.versions.toml)
- Add Hilt version (`2.51.1`) and hilt navigation compose version (`1.2.0`).
- Add `hilt` plugin, `hilt-android`, `hilt-compiler`, and `androidx-hilt-navigation-compose` libraries.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/build.gradle.kts)
- Add Hilt plugin application alias (`apply false`).

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/build.gradle.kts)
- Apply Hilt plugin.
- Add dependencies for `hilt-android`, `hilt-compiler`, and `androidx-hilt-navigation-compose`.

### Application & Entry Points
#### [NEW] [CinemaApplication.kt](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/CinemaApplication.kt)
- Create base Application class annotated with `@HiltAndroidApp`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/AndroidManifest.xml)
- Set `android:name=".CinemaApplication"`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/MainActivity.kt)
- Annotate with `@AndroidEntryPoint`.
- Use `hiltViewModel()` for `CinemaViewModel`.

### Domain Layer (Interfaces & Models)
#### [NEW] [Domain Repositories Package](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/domain/repository/)
- Move/define repository interfaces (`IMovieRepository`, `ISeriesRepository`, `ISeasonsRepository`, `ISearchRepository`, `IGenreRepository`, `ICountryRepository`, `ICountryPostersRepository`) in `com.example.domain.repository`.

### Data Layer (Implementations & Sources)
#### [MODIFY] [Repositories](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/data/repository/)
- Annotate repository implementations with `@Inject constructor()`.

### Dependency Injection (DI)
#### [NEW] [AppModule.kt](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/di/AppModule.kt)
- Provide application-scoped dependencies (e.g., `LocalPreferencesManager`, `OkHttpClient`).

#### [NEW] [RepositoryModule.kt](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/di/RepositoryModule.kt)
- Provide `@Binds` bindings for repository interfaces to their implementations.

### Presentation Layer
#### [MODIFY] [CinemaViewModel.kt](file:///C:/Users/drwit/AndroidStudioProjects/MyMovie/app/src/main/java/com/example/ui/viewmodel/CinemaViewModel.kt)
- Annotate with `@HiltViewModel` and `@Inject constructor(...)` accepting injected repository instances and `Application`.

## Verification Plan

### Automated Tests
- Run Gradle build via `gradle_build("app:assembleDebug")` to ensure compilation and Dagger Hilt dependency graph validation succeed.
