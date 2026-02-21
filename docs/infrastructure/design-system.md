# Design System

The `shared/design-system` module provides the app-wide Material 3 theme, color schemes, and layout constants.

## Theme

Color schemes follow Material 3:

- `CyclingLightColorScheme` — light theme palette
- `CyclingDarkColorScheme` — dark theme palette

Theme state is accessed in composables via `LocalDarkTheme.current`.

## Layout Constants

All layout values are defined as constants in `shared/design-system/theme/Spacing.kt`:

| Constant         | Purpose                    |
|------------------|----------------------------|
| `Spacing.*`      | Padding and margin values  |
| `Elevation.*`    | Shadow elevation           |
| `CornerRadius.*` | Rounded corner sizes       |
| `SurfaceAlpha.*` | Surface transparency       |

## Components

The design system provides wrapper composables that should be used instead of raw Material 3 counterparts:

| Design System Component       | Replaces               | Purpose                               |
|-------------------------------|------------------------|---------------------------------------|
| `DebouncedButton`             | `Button`               | Prevents click spamming (400ms debounce) |
| `DebouncedOutlinedButton`     | `OutlinedButton`       | Prevents click spamming (400ms debounce) |
| `LocalizedDialog`             | `Dialog`               | Inherits app-selected locale          |
| `LocalizedAlertDialog`        | `AlertDialog`          | Inherits app-selected locale          |
| `LocalizedDropdownMenu`       | `DropdownMenu`         | Inherits app-selected locale          |
| `LocalizedExposedDropdownMenu`| `ExposedDropdownMenu`  | Inherits app-selected locale          |
