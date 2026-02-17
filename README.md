# CyclingAssistant

An Android app that helps cyclists discover cycling destinations, track sessions in real time, and review ride history.

Built as an experiment in developing a production-ready application from scratch via an AI agent with only architectural directions.

![Unit Test Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/coverage.json)
![Lines of Code](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/loc.json)
![Modules](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/modules.json)
![Screens](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/screens.json)
![CI Workflows](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/koflox/b2cb29f069e3c32f4b1ecf3c007eeba6/raw/workflows.json)

## Highlights

- **Multi-module Clean Architecture** with bridge pattern for cross-feature communication
- **Session tracking** with foreground service, Kalman-filter location smoothing, and notification controls
- **Destination discovery** — randomized cycling POIs loaded from JSON assets based on user proximity
- **Jetpack Compose** UI with Material 3, light/dark theme, and 3-language localization
- **CI/CD** — automated testing, coverage badges, signed releases, and dependency management

## Quick Start

1. Clone the repository
2. Add your Google Maps API key to `secrets.properties`:
   ```
   MAPS_API_KEY=your_key_here
   ```
3. Run `./gradlew installDebug`

## Documentation

Full documentation is available at the [docs site](https://koflox.github.io/CyclingAssistant/), including architecture details, feature guides, and contribution instructions.

## License

This project is dual-licensed:
- Free for non-commercial and educational use
- Commercial use requires a separate license
- Use of this code for training AI/ML models is explicitly prohibited

See [LICENSE](LICENSE) for details.
