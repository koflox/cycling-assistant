# POI Data Format

Place your points of interest (POI) data in this directory as `destinations.json`.

## Expected JSON Format

```json
[
  {
    "id": "unique-identifier-1",
    "title": "Scenic Lake Park",
    "lat": 37.7749,
    "long": -122.4194
  },
  {
    "id": "unique-identifier-2",
    "title": "Mountain View Point",
    "lat": 37.3861,
    "long": -122.0839
  }
]
```

## Fields Description

- **id** (string, required): Unique identifier for the destination
- **title** (string, required): Display name of the destination
- **lat** (number, required): Latitude in decimal degrees
- **long** (number, required): Longitude in decimal degrees

## Notes

- The file must be named exactly `destinations.json`
- It should contain an array of destination objects
- All fields are required for each destination
- The app will load this data into the local database on first launch
