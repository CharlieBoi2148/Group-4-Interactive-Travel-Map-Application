# Charlie Burbury — Progress Log
**Branch:** `feature/map-and-pins` → `feature/map-controller`
**Role:** Map View, Pin CRUD, Backend pin layer, FR8 Distance layer

---

## Phase 1 — Map View and Pin Layer [COMPLETE]

### Completed
- [x] MVC architecture refactor — React and Java layers decoupled via HTTP
- [x] `Pin.java` — `@Entity` with all FR1 fields + getters/setters
- [x] `Privacy.java` — enum `PRIVATE` / `FRIENDS_ONLY` / `PUBLIC`
- [x] `IPinController.java` — interface with `ResponseEntity<T>` return types
- [x] `PinRepository.java` — `JpaRepository<Pin, Long>` + custom query methods
- [x] `PinService.java` — all 5 methods implemented (createPin, getAllPins, updatePin, deletePin, setPinPrivacy)
- [x] `PinController.java` — all 5 endpoints wired, correct HTTP status codes, error handling
- [x] `App.js` — Controller layer, state management, useEffect for pin persistence
- [x] `MapView.jsx` — pure View, Leaflet map + markers, delete button in popup
- [x] `PinForm.jsx` — pure View, controlled inputs
- [x] `EditPinForm.jsx` — pure View, pre-fills from pin prop, delegates Save/Cancel
- [x] `pinService.js` — fetch to `/api/pins`, field name normalisation, local fallback, deletePin, updatePin
- [x] `mapService.js` — Leaflet icon configuration
- [x] `PinServiceTest.java` — 15 unit tests, all passing
- [x] `PinControllerTest.java` — 10 integration tests, all passing
- [x] `App.test.js` — 5 tests (FR6 mount + FR3 delete flow)
- [x] `PinForm.test.js` — 8 tests (FR1)
- [x] `MapView.test.js` — 6 tests (FR6 markers + FR3 delete button)
- [x] H2 in-memory database configured and working
- [x] CORS configured for `localhost:3000`
- [x] FR3 Delete Pin UI: confirmation dialog in App.js, delete button in MapView popup
- [x] FR2 Edit Pin UI: EditPinForm.jsx fully wired end to end

---

## Phase 2 — FR8 Distance Layer [COMPLETE]

### Completed (2026-04-25 → 2026-04-26)
- [x] `DistanceCalculator.java` — pure-static Haversine utility, `haversineKm()` with coordinate validation (IAE on null/out-of-range), `kmToMiles()`. No Spring annotations. 22 tests.
- [x] `DistanceResult.java` — DTO: `distanceKm`, `distanceMi`, `preferred`, `unit`, `skippedPinIds` (defensive unmodifiable copy). `DistanceResult(double distanceKm, String unit, List<Long> skippedPinIds)`. 14 tests.
- [x] `IMapController.java` — updated from void stubs to 3 `ResponseEntity<DistanceResult>` method signatures with full Javadoc
- [x] `MapService.java` — updated from 2 skeleton methods to 4 full FR8 signatures (`getDistanceBetween`, `getTripDistance`, `getTotalDistance`, `renderMap`)
- [x] `MapAPIClient.java` — fully implemented MapService: `getDistanceBetween()` delegates to DistanceCalculator; `getTripDistance()` loads pins via PinRepository, sorts by visitDate (nullsLast), skips null-coord pins; `getTotalDistance()` accumulates across all owner trips. Private `computeTripDistance()` helper + `TripDistanceComputation` inner class. Constructor injection of PinRepository + TripRepository.
- [x] `MapAPIClientTest.java` — 19 Mockito unit tests, no Spring context, AAA pattern. Covers: coordinate distance, null-coord pins, empty/single/multi-pin trips, date sort verification (asymmetric NY→Paris→London path proves sort fires), null visitDate, trip not found, blank ownerId, no trips.
- [x] `PinRepository.java` — added `findByTripId(Long)` and `findByOwnerId(String)` methods
- [x] `MapController.java` — fully implemented: `@RestController`, `@RequestMapping("/api/map/distance")`, constructor injects `MapService` (DIP). `getDistanceBetween()` → `@GetMapping`, IAE → 400. `getTripDistance()` → `@GetMapping("/trip/{tripId}")`, IAE → 404. `getTotalDistance()` → `@GetMapping("/total")`, IAE → 400. All unit params default to "km".
- [x] `MapControllerTest.java` — 12 `@WebMvcTest` tests, `@MockBean MapService`. Covers: valid 200 responses, missing required params (400), IAE from service (400/404), unit param propagation (FR14), skippedPinIds in response, zero-distance case.

### Frontend (complete — 2026-04-26)
- [x] `mapDistanceService.js` — fetch wrapper for all 3 FR8 endpoints using CRA proxy; explicit 404 handling for trip endpoint; network error wrapping
- [x] `DistancePanel.jsx` — pure View overlay panel: waiting state (pin A name), loading spinner, error with `role="alert"`, result showing "Pin A to Pin B" with km/mi side-by-side equal styling, skipped pin warning
- [x] `DistancePanel.test.jsx` — 17 tests: panel visibility (5 partitions), result display (5 partitions), cancel button (7 partitions)
- [x] `MapView.jsx` — Measure button added as third popup action alongside Edit/Delete; purple (#805ad5) when pin is selected as A, grey (#718096) otherwise; Cancel label when active
- [x] `MapView.test.js` — 8 new FR8 tests added: render (4), interaction (3), styling (1); 6 existing tests unchanged
- [x] `TripList.jsx` — trip distance display below each trip's pin list: km / mi, skipped pin warning; `tripDistances = {}` default keeps existing renders safe
- [x] `App.js` — `measuringFrom`, `measuredTo`, `distanceResult`, `distanceError`, `distanceLoading`, `tripDistances` state; `handleMeasurePin` (two-click state machine, same-pin-twice cancels); `handleCancelMeasure`; `tripDistances` useEffect fires on `[trips, pins]` change, silently ignores failures
- [x] `App.test.js` — 10 new FR8 tests added: DistancePanel visibility (2), measure flow (5), cancel (1), trip distances (2); 14 existing tests unchanged

### Branch: feature/map-controller — ALL COMMITS PUSHED, PR to dev pending
Commits (in order):
1. "Implemented distance calculator and tests"
2. "Add DistanceResult DTO with unit preference and skippedPinIds (14 tests)"
3. "Implement MapAPIClient service layer for FR8 distance calculations (19 tests)"
4. "Implement MapController REST endpoints for FR8 pin-to-pin, trip, and total distance (12 tests)"
5. "Update context files to reflect FR8 implementation and test floor 160"
6. "Add mapDistanceService.js - fetch wrapper for FR8 distance endpoints"
7. "Add DistancePanel component for FR8 pin-to-pin distance display (17 tests)"
8. "Add Measure button to MapView popup for FR8 pin selection (8 new tests)"
9. "Add trip distance display to TripList sidebar for FR8"
10. "Wire FR8 distance flow into App.js - measuringFrom state, handlers, trip distance effect (10 new tests)"

### Next steps after PR merges
- [ ] FR7 — MediaController + media upload UI (overdue)
- [ ] FR6 — Polyline route visualization on map
- [ ] Final sprint cleanup: `PinService.java` package move, `@CrossOrigin` → `WebMvcConfigurer`, `@Component` → `@Service` on `MapAPIClient`, `Coordinate.java` removal, `@ControllerAdvice` global handler

## Known Tech Debt (deferred to final sprint)
- `MapAPIClient.java` annotated `@Component` — should be `@Service` per Spring convention
- `MapController.java` has `@CrossOrigin` — replace with CORS config class before production
- `PinService.java` in wrong package (`travelmap.repository` instead of `travelmap.service`)

## Blocked On (waiting for teammates)
- Edit/delete/privacy owner enforcement → blocked on Wilson's auth merge
- `PinRepository.findByOwnerId()` already added — blocked on Wilson adding `ownerId` to `Pin.java` itself
- Media deletion in `PinService.deletePin()` → blocked on Gage/MediaController implementation
- `MapAPIClient.getTotalDistance()` — ownerId verification blocked on Wilson's AuthController merge (NFR4)

---

### Current Test Count: 107 (backend) / 88 (frontend) / 195 (total)

---

## Change Log

| Date | Change |
|---|---|
| 2026-04-08 | Phase 1 complete. All 34 tests passing. Master Context Document generated. |
| 2026-04-08 | Renamed `package.json` name field from `leaflet-test` to `travel-map` |
| 2026-04-13 | FR3 Delete Pin UI complete. FR2 Edit Pin UI complete. Added MapView.test.js (6), updated App.test.js (5). Total: 44 tests. |
| 2026-04-25 | FR8 distance layer: DistanceCalculator (22 tests), DistanceResult (14 tests), MapAPIClient fully implemented, MapAPIClientTest (19 tests). PinRepository.findByTripId + findByOwnerId added. IMapController + MapService updated to FR8 signatures. MapController TODOs corrected. Test floor: 95/53/148. |
| 2026-04-26 | FR8 frontend complete: mapDistanceService.js, DistancePanel.jsx (17 tests), MapView Measure button (8 new tests), TripList trip distance display, App.js wiring (10 new tests). Test floor: 107/88/195. All 10 commits pushed to feature/map-controller. PR to dev pending. |
| 2026-04-26 | MapController fully implemented (12 tests). All 4 commits pushed to feature/map-controller. PR to dev pending. Test floor: 107/53/160. |
