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

## Phase 2 — FR8 Distance Layer [COMPLETE — backend]

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
- [x] All 4 commits pushed to `feature/map-controller` remote ✓

### Branch: feature/map-controller — PUSHED, PR to dev pending
Commits pushed:
- Commit 1 — "Add DistanceResult DTO with unit preference and skippedPinIds (14 tests)"
- Commit 2 — "Implement MapAPIClient service layer for FR8 distance calculations (19 tests)"
- Commit 3 — "Implement MapController REST endpoints for FR8 pin-to-pin, trip, and total distance (12 tests)"
- Commit 4 — "Update context files to reflect FR8 implementation and test floor 160"

### Pending (after feature/map-controller merges to dev)
- [ ] `feature/map-distance-ui` — wire FR8 endpoints to React frontend: `mapDistanceService.js`, `DistancePanel.jsx`, tests
- [ ] FR7 — MediaController + media upload UI (overdue)
- [ ] FR6 — Polyline route visualization on map
- [ ] Add Edit Pin UI privacy field (FR11 — backend complete)
- [ ] Move `PinService.java` from `travelmap.repository` → `travelmap.service` before submission
- [ ] Replace `@CrossOrigin` annotation with `WebMvcConfigurer` CORS config class
- [ ] Add `@ControllerAdvice` global exception handler to replace per-method try/catch
- [ ] Remove `Coordinate.java` dead code before submission

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

### Current Test Count: 107 (backend) / 53 (frontend) / 160 (total)

---

## Change Log

| Date | Change |
|---|---|
| 2026-04-08 | Phase 1 complete. All 34 tests passing. Master Context Document generated. |
| 2026-04-08 | Renamed `package.json` name field from `leaflet-test` to `travel-map` |
| 2026-04-13 | FR3 Delete Pin UI complete. FR2 Edit Pin UI complete. Added MapView.test.js (6), updated App.test.js (5). Total: 44 tests. |
| 2026-04-25 | FR8 distance layer: DistanceCalculator (22 tests), DistanceResult (14 tests), MapAPIClient fully implemented, MapAPIClientTest (19 tests). PinRepository.findByTripId + findByOwnerId added. IMapController + MapService updated to FR8 signatures. MapController TODOs corrected. Test floor: 95/53/148. |
| 2026-04-26 | MapController fully implemented (12 tests). All 4 commits pushed to feature/map-controller. PR to dev pending. Test floor: 107/53/160. |
