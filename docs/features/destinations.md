# Destinations

The destinations feature handles discovery and selection of cycling points of interest. It follows Clean Architecture with `di/`, `domain/`, `data/`, and `presentation/` layers.

## How It Works

1. On launch, the app gets the user's location
2. The file resolver scans asset files and selects those whose center point is within **100 km** of the user
3. Selected files are parsed and inserted into the local database (each file is loaded only once)
4. The app picks random destinations from the database within a target distance range

## Key Use Cases

| Use Case                     | Purpose                              |
|------------------------------|--------------------------------------|
| `GetDestinationInfoUseCase`  | Get random destinations or by specific ID |
| `GetUserLocationUseCase`     | Get current user location            |
| `InitializeDatabaseUseCase`  | Seed database on first launch        |

## App Recovery Flow

When the app restarts with an active session, `DestinationsViewModel`:

1. Checks for active session via `CyclingSessionUseCase.getActiveSessionDestination()`
2. Fetches destination by ID via `GetDestinationInfoUseCase.getDestinations()`
3. Restores UI state including slider position

## Destination File Format

Destinations are stored as JSON files in the app assets and loaded into a Room database at runtime.

Each file is a JSON array of objects with 4 fields:

| Field   | Type   | Description                           |
|---------|--------|---------------------------------------|
| `id`    | String | Unique identifier (`{city}-{number}`) |
| `title` | String | Display name, local name              |
| `lat`   | Double | Latitude                              |
| `long`  | Double | Longitude                             |

```json
[
  { "id": "tokyo-001", "title": "Imperial Palace Loop (皇居周回)", "lat": 35.6840, "long": 139.7528 },
  { "id": "tokyo-002", "title": "Yoyogi Park", "lat": 35.6728, "long": 139.6949 }
]
```

## File Naming Convention

```
destinations_{city}_{country}_{centerLat}_{centerLon}_tier{N}.json
```

- **city** — lowercase city name (e.g., `tokyo`, `hiroshima`)
- **country** — lowercase country name (e.g., `japan`)
- **centerLat / centerLon** — geographic center of the file's coverage area
- **tier** — integer indicating coverage zone (see below)

Example: `destinations_tokyo_japan_35.6812_139.7671_tier1.json`

## Tiers

Tiers organize destinations by distance from the city center. Lower tiers are loaded first.

| Tier | Coverage        | ID range example     |
|------|-----------------|----------------------|
| 1    | City center     | `tokyo-001` – `050`  |
| 2    | Suburban areas  | `tokyo-051` – `100`  |
| 3    | Wider region    | `tokyo-101` +        |

## Adding a New City

1. Create a JSON file in `feature/destinations/src/main/assets/` following the naming convention
2. Use a unique city prefix for IDs (e.g., `osaka-001`)
3. Set `centerLat` / `centerLon` in the filename to the city center coordinates
4. Start with tier 1 (city center destinations) and add higher tiers as needed
5. Build and run — the app will automatically discover and load the new file when a user is within 100 km
