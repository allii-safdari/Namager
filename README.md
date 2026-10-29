# Namager

### Discover movies and TV series through a modern Android experience.

Namager is an Android application for browsing movies and TV series, exploring title details, and accessing media-related features through a content-focused interface.

The project focuses on Android development, user interface design, and the practical implementation of media browsing experiences.

<p align="center">
  <a href="https://github.com/king-naval/Namager/releases">Releases</a> ·
  <a href="https://github.com/king-naval/Namager/issues">Issues</a> ·
  <a href="https://github.com/king-naval/Namager">Source Code</a>
</p>

---

## Contents

- [Overview](#overview)
- [Screenshots](#screenshots)
- [Features](#features)
- [UX/UI Design](#uxui-design)
- [Architecture and Code Quality](#architecture-and-code-quality)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Building the APK](#building-the-apk)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## Overview

Namager is a movie and TV series browsing application built for Android. It brings media discovery, title information, and playback-related actions together in a single interface.

The project provides a practical foundation for exploring Android UI development, state-driven screens, lifecycle-aware data collection, and maintainable application code.

### Project Goals

- Build a cohesive interface for browsing movies and TV series.
- Present media information in a clear and structured way.
- Organize UI logic and screen state for easier maintenance.
- Improve the usability of browsing, details, and settings screens.
- Provide a codebase that can be explored, tested, and extended by Android developers.

> **Project status:** An ongoing Android development project. Refer to the repository and Releases page for the latest available source code and builds.

## Screenshots

The following screenshots illustrate key areas of the application.

| Movie browsing | TV series browsing |
|---|---|
| ![Movie browsing](screenshots/movies.jpg) | ![TV series browsing](screenshots/series.jpg) |

| Title details | Settings |
|---|---|
| ![Title details](screenshots/details.jpg) | ![Settings](screenshots/settings.jpg) |

*Ensure these image paths match the actual files committed to the repository.*

## Features

### Media Discovery
- Browse movie and TV series catalogs.
- Explore media cards and title information.
- Navigate between browsing and details screens.

### Title Details
- View title information and descriptions.
- Access available media actions.
- Explore information associated with a selected title.

### Playback and Downloads
- Access available online playback and download actions.
- Work with supported media sources and quality options.
- Use playback-related preferences where supported.

*Actual availability depends on the selected title and the capabilities of the configured media sources.*

### TV Series
- Explore series information.
- Navigate season and episode-related content where available.
- Access supported episode actions.

### Personalization and Settings
- Customize supported appearance preferences.
- Configure available playback options.
- Adjust subtitle and time-skipping settings where supported.

## UX/UI Design

The interface aims to make media discovery straightforward while keeping title information and available actions easy to find.

### Design Principles

**Content-first presentation**

Media artwork, titles, descriptions, and relevant metadata take priority over unnecessary interface elements.

**Clear visual hierarchy**

Related information and actions are grouped to help users understand a screen and navigate its content.

**Consistent interaction patterns**

Browsing, selecting a title, reviewing details, and accessing settings follow recognizable interaction patterns.

**Theme consistency**

The visual appearance is designed around a cohesive theme, with attention to colors, contrast, and component consistency.

**Responsive layouts**

Layouts should remain usable across supported screen sizes, orientations, and Android display configurations.

**Accessibility**

Accessibility improvements should include readable text, adequate contrast, meaningful descriptions for interactive elements, and touch targets suitable for different users.

### UX/UI Review Checklist

- [ ] Verify layouts on multiple screen sizes.
- [ ] Check text scaling and long-title behavior.
- [ ] Validate contrast in supported themes.
- [ ] Check TalkBack labels for interactive elements.
- [ ] Verify loading, empty, and error states.
- [ ] Test navigation and screen-state restoration.

## Architecture and Code Quality

Namager uses Android's modern UI development approach with Kotlin and Jetpack Compose.

The codebase includes screen-level state management and ViewModel-based UI logic. The details screen, for example, uses a `DetailsViewModel`, a `DetailsUiState`, and lifecycle-aware state collection.

### UI and State Management

- **Jetpack Compose:** Declarative UI construction.
- **ViewModel:** Screen-related state and logic.
- **State-driven rendering:** UI content responds to the current screen state.
- **Lifecycle-aware collection:** Collecting observable state with lifecycle awareness where implemented.
- **Dependency injection:** Hilt-based ViewModel integration where configured.

### Clean Code Principles

The following principles are useful standards for maintaining and extending the project:

| Principle | Objective |
|---|---|
| Single responsibility | Keep each class and composable focused on a clear purpose. |
| Separation of concerns | Avoid mixing UI rendering, business rules, and data access unnecessarily. |
| Readability | Prefer descriptive names and straightforward control flow. |
| Reusability | Extract genuinely shared UI components and behavior. |
| Testability | Keep important logic independently testable where practical. |
| Lifecycle awareness | Handle screen state and asynchronous work according to Android lifecycle requirements. |
| Maintainability | Keep changes focused and avoid unnecessary coupling. |

These principles describe the intended engineering standards; they should not be interpreted as a claim that every part of the codebase already follows a complete Clean Architecture implementation.

## Technology Stack

| Technology | Role |
|---|---|
| Kotlin | Primary application language |
| Jetpack Compose | Declarative UI |
| Android ViewModel | Screen-level state and logic |
| Kotlin Flow | Asynchronous state and data streams where used |
| Hilt | Dependency injection where configured |
| Lottie | Animation support introduced in the project's development history |
| Gradle | Build and dependency management |
| Android Studio | Development environment |

The exact dependency versions and Android SDK requirements are defined by the project's Gradle configuration.

## Getting Started

### Prerequisites

Before building the project, make sure you have:

- Android Studio installed.
- A compatible JDK for the project's Gradle configuration.
- The Android SDK versions required by the project.
- Git installed.
- An Android emulator or physical device for testing.

### 1. Clone the Repository

```bash
git clone https://github.com/king-naval/Namager.git
cd Namager
```

### 2. Open the Project

Open the cloned directory in Android Studio and allow Gradle synchronization to complete.

If synchronization fails, check the configured JDK, Android SDK, and dependency repositories.

### 3. Run the Application

Select the `app` run configuration, choose an emulator or connected Android device, and click **Run**.

Some application features may depend on external services or media sources being available and correctly configured.

## Building the APK

To build a debug APK from the project root:

**Windows**

```bash
gradlew.bat assembleDebug
```

**macOS / Linux**

```bash
./gradlew assembleDebug
```

If the build succeeds, the APK is typically generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

For a distributable release, configure the appropriate signing settings and build the release variant. Never commit signing keys or passwords to the public repository.

Check the [Releases page](https://github.com/king-naval/Namager/releases) for published builds.

## Project Structure

The following is a conceptual guide to the main Android project areas, not a guaranteed representation of every directory in the repository.

```text
Namager/
├── app/
│   └── src/
│       └── main/
│           ├── java/ or kotlin/
│           │   ├── ui/
│           │   │   ├── components/
│           │   │   ├── screens/
│           │   │   └── theme/
│           │   ├── viewmodel/
│           │   └── data/
│           ├── res/
│           └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

Directory names and module boundaries may differ in the actual implementation. Refer to the repository for the authoritative project structure.

## Contributing

Contributions, bug reports, and suggestions are welcome.

### Suggested Workflow

1. Check existing issues before starting a substantial change.
2. Create a branch for your work.
3. Keep changes focused and use descriptive commit messages.
4. Test the affected functionality on an emulator or device.
5. Include screenshots when a contribution changes the UI.
6. Open a Pull Request explaining the change and its motivation.

For bug reports, include reproduction steps, expected behavior, actual behavior, and relevant logs where possible.

## Acknowledgements

Namager is based on an existing open-source project and includes subsequent development and modifications.

Credit for the original work belongs to its original authors and contributors. Please consult the repository history and applicable license terms for attribution and redistribution requirements.

## License

Review the repository's existing license and the upstream project's licensing terms before redistributing the application or its source code.

If the project does not contain a valid license file, clarify the licensing status before adding a new license or claiming that the project is released under particular terms.

---

<p align="center">
  <sub>Built with Kotlin and Jetpack Compose.</sub>
</p>
