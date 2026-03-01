# Other Features

## Dashboard

The main screen of the app. Provides an expandable menu for navigating to destinations, session history, nutrition, profile, and settings. The dashboard module depends on the `destinations` module and the `destination-session` bridge API.

## Profile

Rider profile management. Stores personalized settings and preferences via DataStore. Connected to the session feature through the `profile-session` bridge.

## Connections

BLE device connection and management. Currently supports cycling power meters, extensible for future sensor types.

- **Device List** — view saved devices with swipe-to-delete, toggle session usage per device
- **BLE Scanning** — scan for nearby BLE devices (modal bottom sheet with 30s timeout)
- **Permission Handling** — runtime BLE permission requests (API 31+ split permissions)
- **Test Mode** — navigate to power test mode screen for verifying device connectivity

Navigation uses a nested graph (`connections_graph`) containing device list, scanning sheet, and power test mode screens.

Connected to sessions through the `connection-session` bridge (`SessionPowerMeterUseCase`). See [Power Meter](power-meter.md) for full details.

## Settings

App settings screen providing:

- **Theme** — toggle between light and dark mode (Material 3 color schemes)
- **Language** — switch between English, Russian, and Japanese
- **Stats Display** — configure which statistics are shown during active sessions, on the completion screen, and when sharing (via `session-settings` bridge)
- **POI Selection** — configure active POI types (via `poi-settings` bridge)

The settings module depends on the `theme`, `locale`, and `profile` feature modules, as well as the `nutrition-settings`, `poi-settings`, and `session-settings` bridge APIs.

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
