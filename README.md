# Spotify Real-Time Trend Scoring & Analytics Service

An event-driven analytics backend inspired by Spotify that ingests user interaction events, performs event-time windowing, and computes deterministic trend scores and rankings.

---

## 1. Project Overview

- **Runtime**: Java 21 & Spring Boot 3.2.5
- **Persistence**: H2 In-Memory Database with Spring Data JPA
- **Validation**: Jakarta Bean Validation
- **Authentication**: Tenant context resolver with `X-API-KEY` or `X-TENANT-ID`
- **Testing**: JUnit 5, Mockito, Spring Boot MockMvc (23 automated tests passing)

---

## 2. Project Architecture & Layers

- `com.spotify.analytics.model`: JPA entities (`UserEvent`) and enums (`EventType`).
- `com.spotify.analytics.dto`: Request/Response DTOs (`EventIngestRequest`, `SongAnalyticsResponse`, `TrendingResponse`, etc.).
- `com.spotify.analytics.repository`: Spring Data JPA interfaces (`UserEventRepository`).
- `com.spotify.analytics.service`: Domain logic (`EventProcessingService`, `AnalyticsService`, `TrendScoringService`).
- `com.spotify.analytics.controller`: REST APIs (`AnalyticsController`).
- `com.spotify.analytics.exception`: Global exception handling (`GlobalExceptionHandler`).
- `com.spotify.analytics.config`: Tenant filter (`TenantAuthenticationFilter`), ThreadLocal context (`TenantContext`), and sample seed loader (`DataInitializer`).

---

## 3. Trend Scoring Formula & Mathematics

```
Trend Score = 0.35 * Play Growth Score
            + 0.25 * Like Rate Score
            + 0.20 * Unique Listener Growth Score
            + 0.20 * Engagement Score
```

### Component Details (Normalized to 0 - 100):

1. **Play Growth Score (35% weight)**:
   - Measures change between current time window and previous equivalent window.
   - If previous plays == 0 and current plays > 0: `min(100.0, max(50.0, 50.0 + currentPlays * 2.0))` to reward new releases without division by zero.
   - For historical songs: `growth% = ((current - previous) / previous) * 100`.
   - Scaled to 0–100: clamped between -100% and +100% via `(growth% + 100) / 2.0`.

2. **Like Rate Score (25% weight)**:
   - `likeRate = totalLikes / totalPlays`.
   - Scaled: `min(100.0, likeRate * 100.0)`. Zero plays yield 0.0.

3. **Unique Listener Growth Score (20% weight)**:
   - Measures growth of distinct listeners (`userId` count) across windows.
   - Same zero-safe scaling logic as Play Growth.

4. **Engagement Score (20% weight)**:
   - Net user intent: `positive = likes + (playlistAdds * 1.5)`, `negative = skips * 1.0`.
   - `netEngagement = (positive - negative) / totalPlays`.
   - Normalized: `50.0 + (netEngagement * 50.0)` clamped to `[0.0, 100.0]`.

5. **Deterministic Tie-Breaking**:
   - `Trend Score (DESC)` -> `Total Plays (DESC)` -> `Unique Listeners (DESC)` -> `Song ID (ASC)`.

---

## 4. REST API Documentation

### Base URL: `http://localhost:8080/api/analytics`

Headers:
- `X-TENANT-ID`: `tenant-a` or `tenant-b` (or `X-API-KEY: key-tenant-a-secret`)
- `Content-Type`: `application/json`

### 1. Ingest Event
- **Endpoint**: `POST /api/analytics/events`
- **Request Body**:
```json
{
  "eventId": "evt-live-101",
  "userId": "usr-88",
  "songId": "song-breakout-hit",
  "artistId": "artist-2",
  "eventType": "SONG_PLAYED",
  "eventTime": "2026-10-30T17:00:00Z",
  "durationSeconds": 210,
  "additionalMetadata": "{\"platform\":\"ios\"}"
}
```
- **Response** (`201 CREATED`):
```json
{
  "eventId": "evt-live-101",
  "tenantId": "tenant-a",
  "status": "ACCEPTED",
  "receivedAt": "2026-10-30T17:41:00Z",
  "message": "Event ingested successfully"
}
```

### 2. Top Trending Songs
- **Endpoint**: `GET /api/analytics/trending?limit=10&window=24h`
- **Response** (`200 OK`):
```json
{
  "tenantId": "tenant-a",
  "window": "24h",
  "count": 5,
  "trendingSongs": [
    {
      "songId": "song-breakout-hit",
      "artistId": "artist-2",
      "tenantId": "tenant-a",
      "totalPlays": 20,
      "uniqueListeners": 20,
      "totalListeningHours": 1.17,
      "totalLikes": 15,
      "totalSkips": 0,
      "likeRate": 0.75,
      "skipRate": 0.0,
      "playGrowthPercent": 100.0,
      "trendScore": 88.25,
      "rank": 1
    }
  ]
}
```

### 3. Song Details
- **Endpoint**: `GET /api/analytics/songs/{songId}`
- **Response** (`200 OK`):
```json
{
  "songId": "song-breakout-hit",
  "artistId": "artist-2",
  "tenantId": "tenant-a",
  "totalPlays": 20,
  "uniqueListeners": 20,
  "totalListeningHours": 1.17,
  "totalLikes": 15,
  "totalSkips": 0,
  "likeRate": 0.75,
  "skipRate": 0.0,
  "searchCount": 0,
  "playlistAdds": 8,
  "trendScore": 88.25
}
```

### 4. Tenant Overview
- **Endpoint**: `GET /api/analytics/overview`
- **Response** (`200 OK`):
```json
{
  "tenantId": "tenant-a",
  "totalPlays": 78,
  "uniqueListeners": 46,
  "totalListeningHours": 2.92,
  "totalLikes": 25,
  "totalSkips": 28,
  "totalSearches": 10,
  "totalPlaylistAdds": 8,
  "overallLikeRate": 0.32,
  "skipRate": 0.36
}
```

### 5. Search Trends
- **Endpoint**: `GET /api/analytics/search-trends?limit=10`
- **Response** (`200 OK`):
```json
[
  {
    "songId": "song-indie-gem",
    "artistId": "artist-3",
    "searchCount": 10,
    "rank": 1
  }
]
```

### 6. Emerging Artists
- **Endpoint**: `GET /api/analytics/emerging-artists?limit=10&window=24h`
- **Response** (`200 OK`):
```json
[
  {
    "artistId": "artist-2",
    "currentPlays": 25,
    "previousPlays": 0,
    "currentUniqueListeners": 25,
    "playGrowthPercent": 100.0,
    "listenerGrowthPercent": 100.0,
    "engagementRate": 0.92,
    "emergingScore": 100.0,
    "rank": 1
  }
]
```

---

## 5. Demonstration Scenarios (curl Commands)

### Scenario 1 & 2: Why High Plays with High Skips Ranks Lower than High-Engagement Breakout Hit
```bash
curl -s -H "X-TENANT-ID: tenant-a" "http://localhost:8080/api/analytics/trending?limit=3&window=24h"
```
- `song-breakout-hit`: 20 plays, 20 unique listeners, 15 likes, 0 skips, 8 playlist adds -> **Trend Score: 88.25 (Rank 1)**
- `song-stale-buzz`: 35 plays, 3 unique listeners, 0 likes, 28 skips -> **Trend Score: 48.20 (Rank 3)**

### Scenario 3: Delayed Events Update Historical Analytics
```bash
# Ingest event that occurred 3 days ago
curl -X POST "http://localhost:8080/api/analytics/events" \
  -H "X-TENANT-ID: tenant-a" \
  -H "Content-Type: application/json" \
  -d '{"eventId":"late-evt-1","userId":"usr-late","songId":"song-late","artistId":"art-late","eventType":"SONG_PLAYED","eventTime":"2026-10-27T10:00:00Z","durationSeconds":300}'

# Querying 24h window excludes it:
curl -s -H "X-TENANT-ID: tenant-a" "http://localhost:8080/api/analytics/trending?window=24h"

# Querying 7d window includes it in its historical event-time frame:
curl -s -H "X-TENANT-ID: tenant-a" "http://localhost:8080/api/analytics/trending?window=7d"
```

### Scenario 4: Multi-Tenant Data Isolation
```bash
curl -s -H "X-TENANT-ID: tenant-a" "http://localhost:8080/api/analytics/overview"
curl -s -H "X-TENANT-ID: tenant-b" "http://localhost:8080/api/analytics/overview"
```

### Scenario 5: Duplicate Ingestion Prevention (Idempotency)
```bash
# First submission: 201 Created
curl -i -X POST "http://localhost:8080/api/analytics/events" \
  -H "X-TENANT-ID: tenant-a" \
  -H "Content-Type: application/json" \
  -d '{"eventId":"idemp-1","userId":"u1","songId":"song-1","artistId":"a1","eventType":"SONG_PLAYED","eventTime":"2026-10-30T16:00:00Z","durationSeconds":180}'

# Duplicate submission: 409 Conflict
curl -i -X POST "http://localhost:8080/api/analytics/events" \
  -H "X-TENANT-ID: tenant-a" \
  -H "Content-Type: application/json" \
  -d '{"eventId":"idemp-1","userId":"u1","songId":"song-1","artistId":"a1","eventType":"SONG_PLAYED","eventTime":"2026-10-30T16:00:00Z","durationSeconds":180}'
```

---

## 6. How to Run & Test

### Run All Unit & Integration Tests:
```powershell
.\mvnw.cmd test
```

### Run Application:
```powershell
.\mvnw.cmd spring-boot:run
```

H2 Database Web Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:spotify_analytics`, User: `sa`, Password: empty).

---

## 7. High-Level Design (HLD): Kafka, Flink & ClickHouse Integration

In an enterprise-scale architecture:
1. **Kafka (Ingestion Layer)**:
   - Producers write to topic `user-events` partitioned by `tenantId:songId`.
   - Guarantees ordered consumption per song and handles millions of events per second.
2. **Apache Flink (Stream Processing Layer)**:
   - Processes events using **Event Time** and **Watermarks** (e.g., `BoundedOutOfOrderness(3 days)`).
   - Late-arriving events are routed via **Side Outputs** or trigger window retractions / updates.
   - Computes rolling aggregations over sliding windows (1h, 24h, 7d).
3. **ClickHouse (Analytical Store)**:
   - Flink sinks aggregated window snapshots to ClickHouse `AggregatingMergeTree` tables.
   - Sub-second analytical queries for Top-N trending and overview dashboards.
4. **Spring Boot (Serving Layer)**:
   - Serves cached Top-N results from Redis backed by ClickHouse and handles auth & tenant routing.

---

## 8. 60-Second Pitch for Hackathon Evaluator

> "Traditional charts only tally total raw plays, favoring passive background listening and bots. Our Real-Time Spotify Trend Scoring & Analytics Service solves this by computing a deterministic multi-factor trend score using event time. It rewards true momentum by combining play growth, distinct listener reach, like conversion, and net engagement while penalizing skips. A song with 20 plays and high saves will decisively outrank a stale song with 35 plays and 80% skip rates. With strict tenant isolation, deduplication, and late-event historical recomputation, this service serves as the core serving engine ready to scale to Kafka and Flink."
