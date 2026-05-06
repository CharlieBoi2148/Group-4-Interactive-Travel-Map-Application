# Charlie Burbury — Progress Log
**Branch:** `feature/map-and-pins` → `feature/map-controller` → `feature/media-controller`
**Role:** Map View, Pin CRUD, Backend pin layer, FR8 Distance layer, FR7 Media Upload

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
- [x] ~~FR7 — MediaController + media upload UI~~ **COMPLETE** (see Phase 3 below)
- [ ] FR6 — Polyline route visualization on map
- [ ] Final sprint cleanup: `PinService.java` + `MediaService.java` package move, `@CrossOrigin` → `WebMvcConfigurer`, `@Component` → `@Service` on `MapAPIClient`, `Coordinate.java` removal, `@ControllerAdvice` global handler

---

## Phase 3 — FR7 Media Upload [COMPLETE]

### Completed (2026-05-05)

**Backend:**
- [x] `IMediaController.java` — replaced 1-method void stub with 3-method FR7 contract: `uploadMedia` (POST /{pinId}), `serveMedia` (GET /{filename}), `deleteMedia` (DELETE /{pinId}). All return `ResponseEntity<?>`. Full Javadoc.
- [x] `MediaService.java` (travelmap.media) — `@Service`. `uploadMedia(pinId, file)`: validates inputs, looks up pin, saves file as `{uuid}.{ext}` to uploads dir, sets `pin.mediaUrl`, persists. `readMedia(filename)`: loads bytes from disk, derives Content-Type from extension. `deleteMedia(pinId)`: looks up pin, deletes file, clears `pin.mediaUrl`, persists. NFR4: rejects filenames containing `..` or `/` (path traversal defense). 23 unit tests (`MediaServiceTest.java`).
- [x] `MediaController.java` — replaced stub with full FR7 HTTP layer. `@CrossOrigin`. Constructor injection of `MediaService` and `PinService`. `uploadMedia`: POST, returns 201 + updated Pin. `serveMedia`: GET, reads bytes + Content-Type, returns 200 Resource. `deleteMedia`: DELETE, returns 204. `IAE` with "not found" → 404; other `IAE` → 400; `IOException` / `ISE` → 500 (NFR3 disk failure mapping). 14 integration tests (`MediaControllerTest.java`).
- [x] `application.properties` — added `spring.servlet.multipart.max-file-size`, `max-request-size`, and `travelmap.media.upload-dir` property. Upload directory auto-created on startup.
- [x] `.gitignore` — added `backend/uploads/` to keep runtime-generated media files out of version control.

**Frontend:**
- [x] `mediaService.js` (frontend/src/services) — FR7 fetch wrappers. `uploadMedia(pinId, file)`: POSTs `FormData` with no manual `Content-Type` header (browser sets multipart boundary — setting it manually breaks the request). `deleteMedia(pinId)`: sends DELETE. `mediaUrl(pin)`: returns `pin.mediaUrl` or null. Relative URLs + CRA proxy. `wrapNetworkError` helper. 9 tests.
- [x] `MediaPreview.jsx` (frontend/src/components) — pure View. Renders `<img>`, `<video controls>`, `<audio controls>`, or `<a>Download media</a>` based on MIME type (checked first) then extension. Accepts `file` (File object → blob URL created via `URL.createObjectURL`) or `src` (URL string); file wins when both given. `useEffect` returns cleanup `URL.revokeObjectURL` to prevent memory leaks. Returns null when no source. 10 tests.
- [x] `PinForm.jsx` — added `mediaFile` state, `<input type="file" accept="image/*,video/*,audio/*">`, local `MediaPreview`, Remove file button, `mediaFile` in `onSave` payload. 3 new FR7 tests added.
- [x] `EditPinForm.jsx` — added `mediaFile` state; `onRemoveMedia` optional prop; existing server-media display block (`<img>` from `pin.mediaUrl` + "Remove media" button when handler present); file input for replace/add; local `MediaPreview`; Remove file button; `mediaFile` in `onSave` payload. 6 new FR7 tests added. 2 pre-existing test assertions updated to include `mediaFile: null`.
- [x] `App.js` — `handleSavePin`: chains `createPin → uploadMedia` with inner try/catch (pin enters state even if upload fails). `handleUpdatePin`: same pattern. New `handleRemoveMedia`: calls `deleteMedia`, clears `mediaUrl` from `pins` and `editPin` state. `onRemoveMedia={handleRemoveMedia}` wired to `EditPinForm`.
- [x] `MapView.jsx` — media thumbnail block inside pin popup: `{pin.mediaUrl && <MediaPreview src={mediaUrl(pin)} maxWidth={180} alt={pin.locationName} />}`.
- [x] `setupTests.js` — added JSDOM mocks: `global.URL.createObjectURL = jest.fn(() => 'blob:mock')` and `global.URL.revokeObjectURL = jest.fn()` — required because JSDOM does not implement these APIs; any test that renders `MediaPreview` with a `File` prop would otherwise crash.

### Branch: feature/media-controller — 22 commits, ready for PR to dev

Key commits:
1. Replace IMediaController stub with three-method FR7 contract
2. Add MediaService with FR7 upload/read/delete logic and NFR4 path-traversal defense
3. Add MediaServiceTest — 23 tests covering FR7 upload/read/delete and NFR4 path traversal
4. Implement MediaController with FR7 upload/serve/delete endpoints and IAE → 400/404 mapping
5. Add MediaControllerTest — 14 tests covering FR7 HTTP layer and NFR3/NFR4 status mapping
6. (frontend) Create mediaService.js, MediaPreview.jsx, wire PinForm, EditPinForm, App.js, MapView.jsx
7. (frontend) Add 28 new tests: PinForm +3, EditPinForm +6, mediaService.test.js (9), MediaPreview.test.jsx (10)

## Known Tech Debt (deferred to final sprint)
- `MapAPIClient.java` annotated `@Component` — should be `@Service` per Spring convention
- `MapController.java` has `@CrossOrigin` — replace with CORS config class before production
- `PinService.java` in wrong package (`travelmap.repository` instead of `travelmap.service`)
- `MediaService.java` in `travelmap.media` — should move to `travelmap.service` in same pass as PinService (May 2026)
- **Orphan file cleanup:** `PinService.deletePin()` does not call `MediaService.deleteMedia()` — deleting a pin with media leaves the file on disk. Easy follow-up if time allows before May 12 demo.
- **Multi-media per pin:** `Pin.mediaUrl` is a single `String`. FR7 SRS allows multiple files per pin. Upgrade to `@OneToMany Media` entity if time permits — not blocking for demo.

## Blocked On (waiting for teammates)
- Edit/delete/privacy owner enforcement → blocked on Wilson's auth merge
- `PinRepository.findByOwnerId()` already added — blocked on Wilson adding `ownerId` to `Pin.java` itself
- Media deletion in `PinService.deletePin()` → MediaController and MediaService are now implemented; the call to `MediaService.deleteMedia()` inside `PinService.deletePin()` is tech debt, not a blocker
- `MapAPIClient.getTotalDistance()` — ownerId verification blocked on Wilson's AuthController merge (NFR4)

---

### Current Test Count: 144 (backend) / 116 (frontend) / 260 (total)

---

## Change Log

| Date | Change |
|---|---|
| 2026-04-08 | Phase 1 complete. All 34 tests passing. Master Context Document generated. |
| 2026-04-08 | Renamed `package.json` name field from `leaflet-test` to `travel-map` |
| 2026-04-13 | FR3 Delete Pin UI complete. FR2 Edit Pin UI complete. Added MapView.test.js (6), updated App.test.js (5). Total: 44 tests. |
| 2026-04-25 | FR8 distance layer: DistanceCalculator (22 tests), DistanceResult (14 tests), MapAPIClient fully implemented, MapAPIClientTest (19 tests). PinRepository.findByTripId + findByOwnerId added. IMapController + MapService updated to FR8 signatures. MapController TODOs corrected. Test floor: 95/53/148. |
| 2026-05-05 | FR7 Media Upload complete — backend + frontend. Backend: IMediaController (3-method contract), MediaService (23 tests — FR7 + NFR4), MediaController (14 tests — FR7 HTTP + NFR3/NFR4), application.properties multipart config, uploads/ gitignored. Frontend: mediaService.js (9 tests), MediaPreview.jsx (10 tests), PinForm.jsx (+3 tests), EditPinForm.jsx (+6 tests), App.js chained upload + handleRemoveMedia, MapView.jsx media thumbnail, setupTests.js JSDOM mocks. Test floor: 144/116/260. Branch: feature/media-controller. 22 commits, PR ready. |
| 2026-04-26 | FR8 frontend complete: mapDistanceService.js, DistancePanel.jsx (17 tests), MapView Measure button (8 new tests), TripList trip distance display, App.js wiring (10 new tests). Test floor: 107/88/195. All 10 commits pushed to feature/map-controller. PR to dev pending. |
| 2026-04-26 | MapController fully implemented (12 tests). All 4 commits pushed to feature/map-controller. PR to dev pending. Test floor: 107/53/160. |
