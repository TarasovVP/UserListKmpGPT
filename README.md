# User List KMP

Kotlin Multiplatform sample app that loads users from the DummyJSON `/users` endpoint and supports Android, iOS, Desktop, and Web.

## Modules

- `shared`: shared business logic, Ktor networking, SQLDelight cache, Koin DI, and the presenter/state layer.
- `composeApp`: Android, iOS, and Desktop UI built with Compose Multiplatform.
- `webApp`: browser UI built with Compose for Web, reusing the shared module.

## Architecture

- `DummyJsonUsersApi` fetches remote users with Ktor.
- `SqlDelightUserLocalDataSource` stores the latest successful user list locally.
- `DefaultUsersRepository` applies the required behavior:
  - successful fetch -> show remote data and refresh cache
  - fetch failure + cache available -> show cached data
  - fetch failure + empty cache -> show error state
- `UsersPresenter` exposes a simple `StateFlow<UsersUiState>` consumed by the platform UIs.

## Useful Tasks

- `./gradlew :composeApp:compileDebugKotlinAndroid`
- `./gradlew :composeApp:compileKotlinDesktop`
- `./gradlew :composeApp:compileKotlinIosSimulatorArm64`
- `./gradlew :webApp:jsDevelopmentExecutableCompileSync`
- `./gradlew :shared:desktopTest :shared:testDebugUnitTest`
- `xcodebuild -project "iosApp/iosApp.xcodeproj" -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build`

## Notes

- `iosApp/iosApp.xcodeproj` hosts the iOS app and uses `:composeApp:embedAndSignAppleFrameworkForXcode` to build and embed the shared Compose framework.
- Web caching uses SQLDelight's JS worker driver. With the default SQL.js worker, browser persistence is limited and may not survive a full page reload without a custom persistent worker backend.
