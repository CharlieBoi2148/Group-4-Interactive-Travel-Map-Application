# Charlie Burbury — Progress Log
**Branch:** `feature/map-and-pins`
**Role:** Map View, Pin CRUD, Backend pin layer

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
- [x] `MapView.jsx` — pure View, Leaflet map + markers
- [x] `PinForm.jsx` — pure View, controlled inputs
- [x] `pinService.js` — fetch to `/api/pins`, field name normalisation, local fallback
- [x] `mapService.js` — Leaflet icon configuration
- [x] `PinServiceTest.java` — 15 unit tests, all passing
- [x] `PinControllerTest.java` — 10 integration tests, all passing
- [x] `App.test.js` — 1 test (FR6)
- [x] `PinForm.test.js` — 8 tests (FR1)
- [x] H2 in-memory database configured and working
- [x] CORS configured for `localhost:3000`

### Current Test Count: 34

---

## Pending (my TODOs)

- [ ] Add Edit Pin UI in React (FR2 — backend complete, frontend not wired)
- [ ] Add Delete Pin UI in React (FR3 — backend complete, frontend not wired)
- [ ] Add Privacy setting UI in React (FR11 — backend complete, frontend not wired)
- [ ] Move `PinService.java` from `travelmap.repository` → `travelmap.service` before submission
- [ ] Replace `@CrossOrigin` annotation with a `WebMvcConfigurer` CORS config class
- [ ] Add `@ControllerAdvice` global exception handler to replace per-method try/catch
- [ ] Remove `Coordinate.java` dead code before submission

## Blocked On (waiting for teammates)

- Edit/delete/privacy owner enforcement → blocked on Wilson's auth merge
- `PinRepository.findByOwnerId()` → blocked on Wilson adding `ownerId` to `Pin.java`
- `PinRepository.findByTripId()` → blocked on Gage's `Trip` class merge
- Media deletion in `PinService.deletePin()` → blocked on Gage/MediaController implementation

---

## Change Log

| Date | Change |
|---|---|
| 2026-04-08 | Phase 1 complete. All 34 tests passing. Master Context Document generated. |
| 2026-04-08 | Renamed `package.json` name field from `leaflet-test` to `travel-map` |
