# CyclingAssistant

An Android app that helps cyclists discover cycling destinations, track sessions in real time, connect BLE power meters, and review ride history.

![Unit Test Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/coverage.json)
![Lines of Code](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/loc.json)
![Modules](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/modules.json)
![UI Components](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/screens.json)
![CI Workflows](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/workflows.json)

<p align="center">
  <img src="docs/showcase/preview_running_session.jpg" width="245" alt="Running session" />
  <img src="docs/showcase/p_completed_session.jpg" width="245" alt="Running session" />
  <img src="docs/showcase/p_connected_devices.jpg" width="245" alt="Running session" />
</p>

> More screenshots and videos in the [Showcase](https://koflox.github.io/cycling-assistant/product/showcase/).

## Features

- **Session tracking** — foreground service with real-time stats, notification controls, and ride sharing
- **Power meter** — BLE connection for live wattage, cadence, and energy tracking
- **Destination discovery** — randomized cycling POIs based on proximity
- **Configurable stats** — choose which statistics to display during sessions
- **Nutrition tracking** — reminders and intake logging linked to sessions
- **Strava sync** — OAuth 2.0 connect, automatic GPX upload of completed sessions, per-session sync status
- **Localization** — English, Russian, Japanese

## Under the Hood

- **Multi-module Clean Architecture** — 60+ Gradle modules with bridge pattern for cross-feature communication; large features split into bounded sub-modules (e.g., `feature/session/{domain, data, tracking, completion, share, history, stats-display, route-render, init, nav-graph}`)
- **Jetpack Compose** — Material 3, light/dark theme, debounced interactions
- **Hilt DI** — compile-time dependency injection with qualifier-based scoping; `init` modules bridge pure-Kotlin domain code into the Hilt graph
- **Ktor + WorkManager** — Strava REST + OAuth 2.0 with bearer auto-refresh, background upload via Hilt-Work workers
- **Baseline Profiles** — AOT-compiled startup and critical user journey paths
- **Kalman filter** — GPS smoothing with acceleration clamping and median speed buffer
- **SQLCipher** — encrypted Room database in release builds
- **Screenshot testing** — Roborazzi-powered visual regression tests with golden image comparison
- **CI/CD** — automated testing, screenshot verification, baseline profiles, signed releases, coverage badges; module graph regenerated and pushed back into open PRs

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
