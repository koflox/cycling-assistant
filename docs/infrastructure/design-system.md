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
