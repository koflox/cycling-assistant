# CyclingAssistant

An Android app that helps cyclists discover cycling destinations, track sessions in real time, connect BLE power meters, and review ride history.

![Unit Test Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/coverage.json)
![Lines of Code](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/loc.json)
![Modules](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/modules.json)
![UI Components](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/screens.json)
![CI Workflows](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/workflows.json)

<p align="center">
  <img src="docs/showcase/preview_dest_selected.jpg" width="245" alt="Selected destination" />
  <img src="docs/showcase/preview_running_session.jpg" width="245" alt="Running session" />
  <img src="docs/showcase/preview_session_completed.jpg" width="245" alt="Session completed" />
  <img src="docs/showcase/preview_dest_search.gif" width="245" alt="Destination search" />
  <img src="docs/showcase/preview_session.gif" width="245" alt="Session tracking" />
  <img src="docs/showcase/preview_settings.gif" width="245" alt="Settings" />
</p>

## Features

- **Session tracking** — foreground service with real-time stats, notification controls, and ride sharing
- **Power meter** — BLE connection for live wattage, cadence, and energy tracking
- **Destination discovery** — randomized cycling POIs based on proximity
- **Configurable stats** — choose which statistics to display during sessions
- **Nutrition tracking** — reminders and intake logging linked to sessions
- **Localization** — English, Russian, Japanese

## Under the Hood

- **Multi-module Clean Architecture** — 40+ Gradle modules with bridge pattern for cross-feature communication
- **Jetpack Compose** — Material 3, light/dark theme, debounced interactions
- **Hilt DI** — compile-time dependency injection with qualifier-based scoping
- **Baseline Profiles** — AOT-compiled startup and critical user journey paths
- **Kalman filter** — GPS smoothing with acceleration clamping and median speed buffer
- **SQLCipher** — encrypted Room database in release builds
- **Screenshot testing** — Roborazzi-powered visual regression tests with golden image comparison
- **CI/CD** — automated testing, screenshot verification, baseline profiles, signed releases, coverage badges

## Quick Start

See the [Setup Guide](https://koflox.github.io/cycling-assistant/product/setup/) to get up and running.

## Documentation

Full documentation is available at the [docs site](https://koflox.github.io/cycling-assistant/), including architecture details, feature guides, and contribution instructions.

## License

This project is dual-licensed:
- Free for non-commercial and educational use
- Commercial use requires a separate license
- Use of this code for training AI/ML models is explicitly prohibited

See [LICENSE](LICENSE) for details.
