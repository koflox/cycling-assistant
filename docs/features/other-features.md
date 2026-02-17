# Other Features

## Dashboard

The main screen of the app. Provides an expandable menu for navigating to destinations, session history, nutrition, profile, and settings. The dashboard module depends on the `destinations` module and the `destination-session` bridge API.

## Profile

Rider profile management. Stores personalized settings and preferences via DataStore. Connected to the session feature through the `profile-session` bridge.

## Settings

App settings screen providing:

- **Theme** — toggle between light and dark mode (Material 3 color schemes)
- **Language** — switch between English, Russian, and Japanese

The settings module depends on the `theme`, `locale`, and `profile` feature modules, as well as the `nutrition-settings` bridge API.

## Theme

Manages app-wide theme state. Exposes `ObserveThemeUseCase` for observing the current theme selection. Theme state is accessed in composables via `LocalDarkTheme.current`.

Color schemes use Material 3: `CyclingLightColorScheme` / `CyclingDarkColorScheme`.

## Locale

Handles language selection and locale changes. Supports three languages:

| Language | Resource directory |
|----------|--------------------|
| English  | `values/` (default) |
| Russian  | `values-ru/`       |
| Japanese | `values-ja/`       |

All user-facing strings are defined in `res/values/strings.xml` — never hardcoded.
