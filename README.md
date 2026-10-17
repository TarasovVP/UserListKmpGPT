# User List KMP — GPT Implementation

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.9.1-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/compose-multiplatform/)
[![AI model](https://img.shields.io/badge/Generated_with-GPT-10A37F?logo=openai&logoColor=white)](https://openai.com/)

A Kotlin Multiplatform user-list application generated with GPT. It loads users from the [DummyJSON `/users` endpoint](https://dummyjson.com/users), provides an offline cache, and supports Android, iOS, Desktop, and Web.

## About the experiment

This repository is one of three implementations produced from the same task for the article [**AI Models for a KMP Developer**](https://levelup.gitconnected.com/ai-models-for-a-kmp-developer-fe4c8de26336).

Each model received the same KMP application brief and a second pass for issue fixing. The generated projects are intentionally preserved as separate results instead of being normalized into one architecture. This makes their architectural choices and different levels of completeness directly comparable.

| Implementation | Model | Repository |
| --- | --- | --- |
| **This project** | **GPT** | **UserListKmpGPT** |
| Alternative | Claude Sonnet | [UserListKmpSonnet](https://github.com/TarasovVP/UserListKmpSonnet) |
| Alternative | Gemini | [UsersAppGemini (private)](https://github.com/TarasovVP/UsersAppGemini) |

## Features

- Shared business logic and presentation state for all targets.
- Compose Multiplatform UI for Android, iOS, and Desktop.
- A dedicated Compose HTML application for Web.
- Ktor networking with Kotlin Serialization.
- SQLDelight caching and offline fallback.
- Koin dependency injection.
- Shared repository tests covering remote, cached, and error scenarios.

## Architecture and modules

```text
.
├── shared/       # Networking, SQLDelight cache, repository, DI, and presenter/state
├── composeApp/   # Android, iOS, and Desktop Compose UI
├── webApp/       # Browser UI built with Compose HTML
└── iosApp/       # Native iOS host application
```

- `DummyJsonUsersApi` fetches remote users with Ktor.
- `SqlDelightUserLocalDataSource` stores the latest successful user list.
- `DefaultUsersRepository` refreshes the cache after a successful request, returns cached data when the network fails, and exposes an error when both sources are empty.
- `UsersPresenter` publishes a `StateFlow<UsersUiState>` consumed by the platform UIs.

## Technology stack

- Kotlin Multiplatform
- Compose Multiplatform and Compose HTML
- Ktor
- SQLDelight
- Koin
- Kotlin Coroutines and Serialization

## Build and test

```bash
# Platform compilation
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :webApp:jsDevelopmentExecutableCompileSync

# Shared tests
./gradlew :shared:desktopTest :shared:testDebugUnitTest

# Native iOS host
xcodebuild -project "iosApp/iosApp.xcodeproj" \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO build
```

## Implementation notes

- `iosApp/iosApp.xcodeproj` hosts the iOS app and uses `:composeApp:embedAndSignAppleFrameworkForXcode` to build and embed the shared Compose framework.
- Web caching uses SQLDelight's JavaScript worker driver. With the default SQL.js worker, browser persistence may not survive a full page reload without a custom persistent worker backend.
- This repository preserves the generated and issue-fixing passes used in the comparison; it is an experiment and reference implementation rather than a production application.
