# SkyPath Flight Search (Submission-ready)

Spring Boot backend + simple frontend (served by Spring as static files).

## What this implements

- ✅ One API: `POST /api/search` returns sorted itineraries
- ✅ Direct, 1-stop, and 2-stop itineraries (max 3 legs)
- ✅ Layover rules:
  - Min layover **45 minutes** for domestic connections
  - Min layover **90 minutes** for international connections
  - Max layover **6 hours** for all connections
- ✅ Airport-change rejection: a connection requires `arrivalAirport == nextDepartureAirport` exactly (no JFK→LGA)
- ✅ Timezones handled correctly:
  - Input `date` is interpreted as **origin airport local date**
  - Flight local timestamps are converted to UTC using airport timezones
- ✅ Dirty data handling:
  - Invalid airports, malformed timestamps, negative/invalid prices, or arrival before departure are **ignored silently**
- ✅ Output is sorted by:
  1) `totalPrice` ascending  
  2) `totalDurationMinutes` ascending  
  3) fewer `stops`  
  4) earlier `departureUtc`

## Run locally (no Docker)

Requirements: Java 17 + Maven

```bash
cd backend
mvn spring-boot:run
```

Open:
- Frontend: http://localhost:8080/
- Health: http://localhost:8080/api/health

## Run with Docker

```bash
docker compose up --build
```

Open: http://localhost:8080/

## API

### POST /api/search

Body:
```json
{ "origin": "JFK", "destination": "LAX", "date": "2026-02-10" }
```

Response:
- `itineraries[]` with legs, UTC times and also `departureLocal` / `arrivalLocal` strings.

## Data

Data file: `backend/src/main/resources/data.json`

You can replace it with your assignment dataset, keeping the structure:
- `airports[]`: `{code, timezone, country,...}`
- `flights[]`: `{id, origin, destination, departureLocal, arrivalLocal, price, currency}`

Notes:
- `departureLocal` / `arrivalLocal` are local datetime strings like `YYYY-MM-DDTHH:mm`.
