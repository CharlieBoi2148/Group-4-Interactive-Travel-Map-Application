# MASTER CONTEXT DOCUMENT — Group 4 Interactive Travel Map Application
### Module of Thought: Prompt 0 — Persistent Session Foundation

**Purpose:** This document is loaded at the start of every agent session to prevent context drift across sessions. Every agent must read this document in full before taking any action.

**Generated:** 2026-04-08 | **Deadline:** 2026-05-12 at 11:00 AM

---

## SECTION 1: PROJECT IDENTITY

### Application
**Interactive Travel Map Application** — a web-based platform enabling users to document, organize, and share travel experiences through a visual geographic interface.

### Team Members and Branches
| Name | Role | Feature Branch |
|---|---|---|
| Charles Burbury (Charlie) | Map View, Pin CRUD, Backend pin layer | `feature/map-and-pins` |
| Wilson Buhendwa | Authorization, Authentication, User Accounts | Phase 2 (in progress) |
| Gage Buckley | Trips, Timeline, Search | Phase 3 (in progress) |

### Repository
**GitHub:** https://github.com/CharlieBoi2148/Group-4-Interactive-Travel-Map-Application.git

### Branch Strategy
- Feature branches → `dev` (integration branch) → `main` (stable/protected)
- Protection rules: no direct push to `main`; all changes via PR through `dev` first
- **Never merge to `main` without running both `mvn test` AND `npm test` passing**

### Statement of Purpose
This document is the authoritative context file for all agent sessions working on this project. It codifies the architecture, implementation state, requirements, and rules that every agent must respect. Deviating from the rules in Section 9 without explicit user instruction is prohibited.

**Final Presentation Deadline: May 12, 2026 at 11:00 AM**

---

## SECTION 2: ARCHITECTURAL LAW

> **MANDATORY:** Every agent must treat this section as non-negotiable rules. No exceptions.

### The Two-Layer MVC Diagram

The project implements **two separate MVC instances** communicating via HTTP. They are **never directly coupled** — React never imports Java and Java never imports React.

```
┌──────────────────────────────────────────────────────────────┐
│                  REACT (Frontend MVC)                        │
│                                                              │
│   View (MapView.jsx, PinForm.jsx)                           │
│     ↑ props/events                                          │
│   Controller (App.js)   ←→   Service (pinService.js)       │
│     ↑ state                         ↑ fetch()               │
└─────────────────────────────────────┼────────────────────────┘
                                      │ HTTP JSON (port 8080)
                                      │ GET/POST/PUT/DELETE/PATCH
┌─────────────────────────────────────┼────────────────────────┐
│                  JAVA (Backend MVC)                          │
│                                     ↓                        │
│   Controller (@RestController)  ←  PinController.java       │
│     ↓ delegates                                              │
│   Service (@Service)            ←  PinService.java          │
│     ↓ delegates                                              │
│   Repository (JpaRepository)    ←  PinRepository.java       │
│     ↓ SQL                                                    │
│   Database (H2 / PostgreSQL)                                 │
└──────────────────────────────────────────────────────────────┘
```

**Request flow (full end-to-end):**
```
User → View (React) → App.js → pinService.js → fetch() →
  PinController → PinService → PinRepository → DB →
  PinRepository → PinService → PinController → JSON →
  pinService.js → App.js → View (React renders markers) → User
```

### MVC Boundary Rules

| Layer | IS allowed to | IS NOT allowed to |
|---|---|---|
| **React View** (MapView.jsx, PinForm.jsx) | Render data passed as props; fire events to App.js | Call fetch() directly; hold business state; import Java code |
| **React Controller** (App.js) | Hold UI state; call pinService.js; pass props to Views | Render map tiles or UI directly; write SQL; call backend directly |
| **React Service** (pinService.js) | Call backend via fetch(); normalise field names | Hold React state; render UI; import Spring classes |
| **Java Controller** (@RestController) | Receive HTTP requests; delegate to Service; return ResponseEntity | Call Repository directly; contain business logic; import React |
| **Java Service** (@Service) | Validate input; apply business rules; call Repository | Contain HTTP annotations; call React; return ResponseEntity |
| **Java Repository** (JpaRepository) | Execute database queries | Contain business logic; return HTTP responses |

**Concrete codebase examples:**
- `PinController.java` receives `POST /api/pins` and immediately calls `pinService.createPin(pin)` — no logic lives there
- `PinService.java` validates `locationName != null` and defaults `privacyLevel` to `PRIVATE` — logic lives here, not in the controller
- `PinRepository.java` extends `JpaRepository` — no method bodies, SQL is generated by Spring Data from method names
- `MapView.jsx` accepts `pins` as a prop and renders markers — it never calls `fetch()` itself
- `App.js` calls `createPin()` from `pinService.js` — the only component that coordinates data flow

### SOLID Principles as Applied in This Codebase

| Principle | Codebase Demonstration |
|---|---|
| **SRP** (Single Responsibility) | `PinController` handles HTTP only. `PinService` handles business logic only. `PinRepository` handles data access only. Each has exactly one reason to change. |
| **OCP** (Open/Closed) | All controllers implement interfaces (`IPinController`, `ITripController`, etc.). New implementations can be added without modifying existing code. |
| **LSP** (Liskov Substitution) | `PinController implements IPinController` — `AppController` depends on `IPinController` and can be substituted with any conforming implementation without breaking the system. |
| **ISP** (Interface Segregation) | Interfaces are small and role-specific: `IPinController` (5 pin methods), `ITripController` (3 trip methods), `IMediaController` (3 media methods — FR7), `ISearchController` (2 search methods), `ISharingController` (1 sharing method). No class is forced to implement methods it does not use. |
| **DIP** (Dependency Inversion) | `AppController` depends on `IPinController` (interface), not `PinController` (concrete). `PinController` depends on `PinService` via constructor injection. `PinService` depends on `PinRepository` via constructor injection. High-level modules depend on abstractions. |

### New Package: `travelmap.media` (added FR7, May 2026)

`MediaService.java` lives in `travelmap.media`, mirroring the `travelmap.map` pattern used for `MapAPIClient`. This is a deliberate short-term location — both `MediaService` and `PinService` should consolidate into `travelmap.service` in the final sprint cleanup pass. `MediaService` is annotated `@Service` (not `@Component`), explicitly avoiding the cleanup item already flagged for `MapAPIClient`.

Media files are stored at `backend/uploads/{uuid}.{ext}` and are gitignored. UUID-based filenames prevent path traversal and name collisions. The frontend accesses them via `GET /api/media/{filename}`. The frontend service (`mediaService.js`) uses relative URLs and the CRA dev proxy — it does NOT use the older hardcoded-localhost pattern from `pinService.js`.

### Full Package Structure (verified against live code)

```
Group-4-Interactive-Travel-Map-Application/
├── backend/
│   ├── pom.xml                                         Spring Boot 3.2.0, Java 17, H2, JPA
│   └── src/
│       ├── main/
│       │   ├── java/travelmap/
│       │   │   ├── TravelMapApplication.java           @SpringBootApplication entry point
│       │   │   ├── account/
│       │   │   │   ├── AccountFacade.java              @Component orchestrator (STUBBED)
│       │   │   │   ├── AuthService.java                @Service (STUBBED)
│       │   │   │   ├── PasswordValidator.java          @Component (STUBBED)
│       │   │   │   ├── ProfileService.java             @Service (STUBBED)
│       │   │   │   ├── RegistrationService.java        @Service (STUBBED)
│       │   │   │   └── SessionManager.java             @Component (STUBBED)
│       │   │   ├── controller/
│       │   │   │   ├── AppController.java              @RestController orchestrator (STUBBED)
│       │   │   │   ├── PinController.java              @RestController /api/pins (IMPLEMENTED)
│       │   │   │   ├── TripController.java             @RestController /api/trips (STUBBED)
│       │   │   │   ├── MapController.java              @RestController /api/map/distance (IMPLEMENTED — FR8)
│       │   │   │   ├── MediaController.java            @RestController /api/media (IMPLEMENTED — FR7)
│       │   │   │   ├── SearchController.java           @RestController /api/search (STUBBED)
│       │   │   │   └── SharingController.java          @RestController /api/share (STUBBED)
│       │   │   ├── interfaces/
│       │   │   │   ├── IPinController.java             (IMPLEMENTED)
│       │   │   │   ├── ITripController.java            (STUBBED)
│       │   │   │   ├── IMapController.java             (IMPLEMENTED — FR8, 3 ResponseEntity<DistanceResult> methods)
│       │   │   │   ├── IMediaController.java           (IMPLEMENTED — FR7, 3 ResponseEntity methods: upload/serve/delete)
│       │   │   │   ├── ISearchController.java          (STUBBED)
│       │   │   │   ├── ISharingController.java         (STUBBED)
│       │   │   │   ├── IAuthService.java               (STUBBED)
│       │   │   │   ├── IProfileService.java            (STUBBED)
│       │   │   │   ├── IRegistrationService.java       (STUBBED)
│       │   │   │   ├── MapService.java                 interface (IMPLEMENTED — FR8, 4 methods)
│       │   │   │   └── UserRepository.java             interface (STUBBED)
│       │   │   ├── map/
│       │   │   │   ├── DistanceCalculator.java         Pure-math Haversine utility, no Spring (IMPLEMENTED)
│       │   │   │   ├── DistanceResult.java             DTO: distanceKm, distanceMi, unit, skippedPinIds (IMPLEMENTED)
│       │   │   │   └── MapAPIClient.java               @Component implements MapService (IMPLEMENTED)
│       │   │   ├── media/
│       │   │   │   └── MediaService.java               @Service — FR7 upload/read/delete, UUID filenames, NFR4 path-traversal defense (IMPLEMENTED)
│       │   │   ├── model/
│       │   │   │   ├── Pin.java                        @Entity (IMPLEMENTED — mediaUrl field stores /api/media/{filename})
│       │   │   │   ├── Privacy.java                    enum PRIVATE/FRIENDS_ONLY/PUBLIC (IMPLEMENTED)
│       │   │   │   ├── User.java                       POJO, no JPA annotations (STUBBED)
│       │   │   │   ├── Trip.java                       POJO, no JPA annotations (STUBBED)
│       │   │   │   ├── Media.java                      POJO, no JPA annotations (STUBBED)
│       │   │   │   ├── Statistics.java                 POJO, no JPA annotations (STUBBED)
│       │   │   │   └── Coordinate.java                 POJO — DEAD CODE (superseded by Pin.latitude/longitude)
│       │   │   ├── repository/
│       │   │   │   ├── PinRepository.java              JpaRepository<Pin,Long> (IMPLEMENTED)
│       │   │   │   ├── PinService.java                 @Service — WRONG PACKAGE (IMPLEMENTED)
│       │   │   │   └── UserRepositoryImpl.java         @Repository (STUBBED)
│       │   │   └── service/                            DIRECTORY DOES NOT EXIST YET
│       │   │       └── (PinService.java and MediaService.java should move here before final submission)
│       │   └── resources/
│       │       └── application.properties              H2 config; PostgreSQL commented out; multipart upload config + travelmap.media.upload-dir property (FR7)
│       └── test/java/travelmap/
│           ├── PinServiceTest.java                     15 unit tests (IMPLEMENTED)
│           ├── PinControllerTest.java                  10 integration tests (IMPLEMENTED)
│           ├── DistanceCalculatorTest.java             22 unit tests, input partitioning (IMPLEMENTED)
│           ├── DistanceResultTest.java                 14 unit tests (IMPLEMENTED)
│           ├── MapAPIClientTest.java                   19 unit tests, Mockito (IMPLEMENTED)
│           ├── MapControllerTest.java                  12 integration tests, @WebMvcTest (IMPLEMENTED — FR8)
│           ├── MediaServiceTest.java                   23 unit tests — FR7 upload/read/delete + NFR4 path traversal (IMPLEMENTED)
│           └── MediaControllerTest.java                14 integration tests — FR7 HTTP layer + NFR3/NFR4 status mapping (IMPLEMENTED)
│
├── uploads/                                            Runtime media storage — gitignored; UUID filenames prevent path traversal
└── frontend/
    ├── package.json                                    React 19.2.4, Leaflet 1.9.4
    └── src/
        ├── App.js                                      Controller layer (IMPLEMENTED — FR7: chained upload + handleRemoveMedia)
        ├── App.test.js                                 24 tests — FR6, FR3, FR2, FR11, FR10, FR8
        ├── PinForm.test.js                             11 tests — FR1 (8) + FR7 file input (3)
        ├── EditPinForm.test.js                         18 tests — FR2 (8), FR11 privacy (5), FR7 media (6) (IMPLEMENTED)
        ├── mediaService.test.js                        9 tests — FR7 fetch wrappers (IMPLEMENTED)
        ├── MediaPreview.test.jsx                       10 tests — FR7 element rendering + blob URL cleanup (IMPLEMENTED)
        ├── index.js                                    React entry point
        ├── setupTests.js                               Jest config + JSDOM mocks for URL.createObjectURL / URL.revokeObjectURL (FR7)
        ├── reportWebVitals.js                          Performance monitoring
        ├── components/
        │   ├── MapView.jsx                             View: map + markers + media thumbnail in popup (IMPLEMENTED — FR7 + FR8)
        │   ├── PinForm.jsx                             View: pin creation form + file input + local preview (IMPLEMENTED — FR7)
        │   ├── EditPinForm.jsx                         View: pre-fills from pin prop, media display/replace/remove flow (IMPLEMENTED — FR7)
        │   ├── DistancePanel.jsx                       View: FR8 distance overlay panel (IMPLEMENTED)
        │   └── MediaPreview.jsx                        View: inline media rendering — img/video/audio/download by MIME+ext, blob URL cleanup (IMPLEMENTED — FR7)
        └── services/
            ├── pinService.js                           Fetch to /api/pins (IMPLEMENTED)
            ├── mapService.js                           Leaflet icon config (IMPLEMENTED)
            ├── mapDistanceService.js                   Fetch wrapper for FR8 distance endpoints (IMPLEMENTED)
            └── mediaService.js                         Fetch wrappers for FR7 — uploadMedia, deleteMedia, mediaUrl helper (IMPLEMENTED)
```

**MISSING classes (referenced but not yet created):**
- `AuthController.java` — needed for FR14 (Wilson's task)
- `travelmap/service/` directory — `PinService.java` must be moved here before submission
- DTOs for auth (login request, registration request) — Wilson's task
- `TripService.java` / `TripRepository.java` — Gage's task

---

## SECTION 3: TECH STACK AND COMMANDS

### Backend
| Component | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Build tool | Maven |
| ORM | Spring Data JPA (Hibernate) |
| Database (dev) | H2 in-memory |
| Database (prod) | PostgreSQL (config commented out, ready to swap) |
| Test framework | JUnit 5 + Mockito |
| JSON | Jackson (bundled with Spring Boot) |

### Frontend
| Component | Version |
|---|---|
| React | 19.2.4 |
| react-leaflet | 5.0.0 |
| Leaflet | 1.9.4 |
| Test framework | Jest + React Testing Library |
| Build tool | react-scripts (Create React App) |

### Key Commands (copy exactly)

```bash
# Run backend (from repo root)
cd Group-4-Interactive-Travel-Map-Application/backend && mvn spring-boot:run

# Run frontend (from repo root)
cd Group-4-Interactive-Travel-Map-Application/frontend && npm start

# Run backend tests
cd Group-4-Interactive-Travel-Map-Application/backend && mvn test

# Run frontend tests
cd Group-4-Interactive-Travel-Map-Application/frontend && npm test

# Generate Javadoc
cd Group-4-Interactive-Travel-Map-Application/backend && mvn javadoc:javadoc
```

### CORS Configuration
- **Development (current):** `@CrossOrigin(origins = "http://localhost:3000")` on `PinController.java`
- **Production (TODO):** Replace with a Spring `WebMvcConfigurer` CORS configuration class. The `@CrossOrigin` annotation on individual controllers is dev-only and must be removed before deployment.

### H2 Console (Development)
Available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:travelmap`
- Username: `sa`
- Password: (empty)
- Data resets every time the backend restarts (in-memory)

### Port Assignments
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- All backend API calls from React target `http://localhost:8080/api/...`

---

## SECTION 4: CURRENT IMPLEMENTATION STATE

> Primary source: CLAUDE.md (verified against live code 2026-04-08). Discrepancies from live code are noted explicitly.

### Backend — Java Classes

#### `travelmap` (root package)
| Class | Status | Notes |
|---|---|---|
| `TravelMapApplication.java` | [IMPLEMENTED] | `@SpringBootApplication`, `main()` entry point |

#### `travelmap.model`
| Class | Status | Notes |
|---|---|---|
| `Pin.java` | [IMPLEMENTED] | `@Entity`, JPA mapped, all fields + getters/setters. Fields: `id` (Long, auto-generated), `locationName`, `country`, `region`, `visitDate` (LocalDate), `notes`, `latitude` (Double), `longitude` (Double), `privacyLevel` (Privacy enum, defaults PRIVATE), `mediaUrl` |
| `Privacy.java` | [IMPLEMENTED] | Enum: `PRIVATE`, `FRIENDS_ONLY`, `PUBLIC` |
| `User.java` | [STUBBED] | POJO only — no `@Entity`, no JPA annotations, no getters/setters. Fields: `userId`, `username`, `password`, `profilePicture`, `homeLocation`, `measurementPreference`, `trips` (List<Trip>), `statistics` |
| `Trip.java` | [STUBBED] | POJO only — no `@Entity`, no JPA annotations, no getters/setters. Fields: `tripId`, `name`, `description`, `startDate`, `endDate`, `pins` (List<Pin>), `privacyLevel`, `ownerId` |
| `Media.java` | [STUBBED] | POJO only — no `@Entity`, no JPA annotations, no getters/setters. Fields: `mediaId`, `fileType`, `filePath`, `pinId`, `uploadDate` |
| `Statistics.java` | [STUBBED] | POJO only — no `@Entity`, no JPA annotations. Fields: `totalCountriesVisited`, `totalCitiesVisited`, `totalDistanceTraveled`, `totalPins`, `totalTrips` |
| `Coordinate.java` | [DEAD CODE] | Superseded by inline `latitude`/`longitude` on `Pin.java`. Do not use. |

#### `travelmap.controller`
| Class | Status | Notes |
|---|---|---|
| `PinController.java` | [IMPLEMENTED] | `@RestController`, `@RequestMapping("/api/pins")`, `@CrossOrigin("http://localhost:3000")`. Implements `IPinController`. All 5 endpoints working with correct HTTP status codes. Constructor injection of `PinService`. |
| `AppController.java` | [STUBBED] | `@RestController`, no `@RequestMapping`. Holds references to all controller/service interfaces via field injection (not constructor). No methods implemented. Not a REST entry point — structural orchestrator only. |
| `TripController.java` | [STUBBED] | `@RestController`, `@RequestMapping("/api/trips")`. Implements `ITripController`. All 3 methods present but return void with empty bodies. |
| `MapController.java` | [STUBBED] | `@RestController`, `@RequestMapping("/api/map")`, `@CrossOrigin`. Implements `IMapController`. 3 endpoints (`/distance/pins`, `/distance/trip`, `/distance/total`) all return HTTP 501. MapService injection pending (Step 11). |
| `MediaController.java` | [IMPLEMENTED] | `@RestController`, `@RequestMapping("/api/media")`, `@CrossOrigin`. Implements `IMediaController`. 3 endpoints: `uploadMedia` (POST /{pinId}), `serveMedia` (GET /{filename}), `deleteMedia` (DELETE /{pinId}). IAE → 400 or 404; ISE → 500. Constructor injection of `MediaService` and `PinService`. |
| `SearchController.java` | [STUBBED] | `@RestController`, `@RequestMapping("/api/search")`. Implements `ISearchController`. 2 methods, empty bodies. |
| `SharingController.java` | [STUBBED] | `@RestController`, `@RequestMapping("/api/share")`. Implements `ISharingController`. 1 method, empty body. |

#### `travelmap.repository`
| Class | Status | Notes |
|---|---|---|
| `PinRepository.java` | [IMPLEMENTED] | `@Repository` interface, extends `JpaRepository<Pin, Long>`. Custom methods: `findByPrivacyLevel(Privacy)`, `findByLocationNameContainingIgnoreCase(String)`, `findByTripId(Long)`, `findByOwnerId(String)`. Spring Data JPA generates SQL from method names. |
| `PinService.java` | [IMPLEMENTED] | `@Service`. **WRONG PACKAGE** — currently in `travelmap.repository`, must move to `travelmap.service` before final submission. Constructor injection of `PinRepository`. All 5 methods fully implemented: `createPin()`, `getAllPins()`, `updatePin()`, `deletePin()`, `setPinPrivacy()`. |
| `UserRepositoryImpl.java` | [STUBBED] | `@Repository`, implements `UserRepository`. All 3 methods return `null`/do nothing. |

#### `travelmap.account`
| Class | Status | Notes |
|---|---|---|
| `AccountFacade.java` | [STUBBED] | `@Component`. Orchestrates auth/profile services. All methods return `false`/`null` or do nothing. `getCurrentUser()` returns `null`. |
| `AuthService.java` | [STUBBED] | `@Service`, implements `IAuthService`. All 4 methods return `false`/`null` or do nothing. |
| `PasswordValidator.java` | [STUBBED] | `@Component`. All 3 methods return `false`/`null`. |
| `ProfileService.java` | [STUBBED] | `@Service`, implements `IProfileService`. `updateProfile()` does nothing. |
| `RegistrationService.java` | [STUBBED] | `@Service`, implements `IRegistrationService`. `register()` returns `null`. |
| `SessionManager.java` | [STUBBED] | `@Component`. All 4 methods return `null`/`false` or do nothing. |

#### `travelmap.map`
| Class | Status | Notes |
|---|---|---|
| `DistanceCalculator.java` | [IMPLEMENTED] | Pure-static math utility. `haversineKm(lat1,lon1,lat2,lon2)` with coordinate validation (IAE on null/out-of-range). `kmToMiles(km)`. EARTH_RADIUS_KM=6371.0. No Spring annotations. |
| `DistanceResult.java` | [IMPLEMENTED] | DTO. Constructor: `DistanceResult(double distanceKm, String unit, List<Long> skippedPinIds)`. Fields: `distanceKm`, `distanceMi`, `preferred`, `unit`, `skippedPinIds` (defensive unmodifiable copy). `"mi".equalsIgnoreCase(unit)` → unit="mi", else "km". |
| `MapAPIClient.java` | [IMPLEMENTED] | `@Component`, implements `MapService`. Constructor injection of `PinRepository` and `TripRepository`. `getDistanceBetween()` delegates to `DistanceCalculator`. `getTripDistance()` sorts pins by visitDate (nullsLast), skips null-coord pins, sums Haversine between consecutive usable pairs. `getTotalDistance()` accumulates across all owner trips. Private `computeTripDistance()` + `TripDistanceComputation` inner class. |

#### `travelmap.interfaces`
| Interface | Status | Notes |
|---|---|---|
| `IPinController.java` | [IMPLEMENTED] | Full Javadoc, all 5 method signatures defined with `ResponseEntity<T>` return types |
| `ITripController.java` | [STUBBED] | 3 methods, void returns |
| `IMapController.java` | [IMPLEMENTED] | 3 methods with `ResponseEntity<DistanceResult>` return types: `getDistanceBetween`, `getTripDistance`, `getTotalDistance`. Full Javadoc. |
| `IMediaController.java` | [IMPLEMENTED] | 3 methods: `uploadMedia` (POST /{pinId}), `serveMedia` (GET /{filename}), `deleteMedia` (DELETE /{pinId}). All return `ResponseEntity<?>`. Full Javadoc. |
| `ISearchController.java` | [STUBBED] | 2 methods, void returns |
| `ISharingController.java` | [STUBBED] | 1 method, void return |
| `IAuthService.java` | [STUBBED] | 2 methods: `isLoggedIn()`, `getCurrentUser()` |
| `IProfileService.java` | [STUBBED] | 1 method: `updateProfile()` |
| `IRegistrationService.java` | [STUBBED] | 1 method: `register()` |
| `MapService.java` | [IMPLEMENTED] | 4 methods: `getDistanceBetween()`, `getTripDistance()`, `getTotalDistance()`, `renderMap()`. Full Javadoc. Replaces skeleton `calculateDistance()`. |
| `UserRepository.java` | [STUBBED] | 3 methods: `findByUsername()`, `save()`, `update()` |

### REST Endpoints

| Endpoint | HTTP Method | Controller Method | Status | Notes |
|---|---|---|---|---|
| `/api/pins` | POST | `createPin()` | [WORKING] | Returns HTTP 201 with saved Pin |
| `/api/pins` | GET | `getAllPins()` | [WORKING] | Returns HTTP 200 with all pins |
| `/api/pins/{id}` | PUT | `updatePin()` | [WORKING] | Returns HTTP 200; partial update |
| `/api/pins/{id}` | DELETE | `deletePin()` | [WORKING] | Returns HTTP 204 |
| `/api/pins/{id}/privacy` | PATCH | `setPinPrivacy()` | [WORKING] | Returns HTTP 200 |
| `/api/trips` | all | `TripController` | [STUBBED] | Methods mapped but not implemented |
| `/api/map/distance` | GET | `MapController.getDistanceBetween()` | [WORKING] | Returns 200 DistanceResult; 400 on IAE |
| `/api/map/distance/trip/{tripId}` | GET | `MapController.getTripDistance()` | [WORKING] | Returns 200 DistanceResult; 404 on trip not found |
| `/api/map/distance/total` | GET | `MapController.getTotalDistance()` | [WORKING] | Returns 200 DistanceResult; 400 on missing ownerId |
| `/api/media/{pinId}` | POST | `MediaController.uploadMedia()` | [WORKING] | Multipart upload; returns 201 + updated Pin; 400/404 on IAE |
| `/api/media/{filename}` | GET | `MediaController.serveMedia()` | [WORKING] | Serves raw bytes with correct Content-Type; 404 on missing file |
| `/api/media/{pinId}` | DELETE | `MediaController.deleteMedia()` | [WORKING] | Removes file + clears Pin.mediaUrl; returns 204; 404 on IAE "not found" |
| `/api/search` | all | `SearchController` | [STUBBED] | Methods mapped but not implemented |
| `/api/share` | all | `SharingController` | [STUBBED] | Methods mapped but not implemented |
| `/api/auth/**` | all | `AuthController` | [MISSING] | Not yet created (Wilson's task) |

### Frontend — Components and Services

| File | Status | Notes |
|---|---|---|
| `App.js` | [IMPLEMENTED] | Controller layer. Holds `pins`, `form`, `editPin`, `confirmDelete`, distance, and `tripDistances` state. `handleSavePin` chains `createPin → uploadMedia` with inner try/catch (pin saved even if upload fails). `handleUpdatePin` same pattern. `handleRemoveMedia` calls `deleteMedia` and clears `mediaUrl` from state. |
| `MapView.jsx` | [IMPLEMENTED] | Pure View. Renders Leaflet markers with popup: location name, visit date, media thumbnail (`MediaPreview` with 180px cap), Edit/Delete/Measure buttons. `PinPlacer` inner component delegates map click events upward. |
| `PinForm.jsx` | [IMPLEMENTED] | Pure View. Controlled inputs for all pin fields + file input (`accept="image/*,video/*,audio/*"`), local `MediaPreview`, Remove file button. `mediaFile` included in `onSave` payload. |
| `EditPinForm.jsx` | [IMPLEMENTED] | Pure View. Pre-fills from `pin` prop. Shows existing server media (`<img>` + "Remove media" button when `onRemoveMedia` prop is present), file input for replace, local `MediaPreview`. `mediaFile` in `onSave` payload. |
| `MediaPreview.jsx` | [IMPLEMENTED] | Pure View (FR7). Renders `<img>`, `<video controls>`, `<audio controls>`, or `<a>Download media</a>` based on MIME type then extension. Accepts `file` (File object — blob URL) or `src` (URL string). `useEffect` creates/revokes blob URL on mount/unmount to prevent memory leaks. Returns null when no source. |
| `pinService.js` | [IMPLEMENTED] | `createPin()`, `getPins()`, `updatePin()`, `deletePin()`. All normalise `latitude`/`longitude` → adds `lat`/`lng` aliases. Local fallback if backend down. |
| `mediaService.js` | [IMPLEMENTED] | FR7 fetch wrappers. `uploadMedia(pinId, file)`: POST FormData — no Content-Type header (browser sets multipart boundary). `deleteMedia(pinId)`: DELETE. `mediaUrl(pin)`: returns `pin.mediaUrl` or null. Relative URLs + CRA proxy. |
| `mapService.js` | [IMPLEMENTED] | One-time Leaflet default icon configuration. Imported by `App.js` as a side-effect import. |
| `mapDistanceService.js` | [IMPLEMENTED] | FR8 fetch wrappers: `getPinToPin()`, `getTripDistance()`, `getTotalDistance()`. Relative URLs + CRA proxy. |
| `index.js` | [IMPLEMENTED] | React entry point, mounts `App` into `#root` |
| `setupTests.js` | [IMPLEMENTED] | Jest setup: `@testing-library/jest-dom` + JSDOM mocks for `URL.createObjectURL` (returns `'blob:mock'`) and `URL.revokeObjectURL` — required by `MediaPreview` tests because JSDOM does not implement these APIs. |
| `reportWebVitals.js` | [IMPLEMENTED] | CRA-generated performance monitoring stub |

### ⚠️ Live Code Discrepancy: MapView Field Names

**CLAUDE.md states:** `MapView.jsx` uses `pin.lat` / `pin.lng`

**Actual code in `MapView.jsx` (line 41):**
```jsx
<Marker key={pin.id} position={[pin.latitude, pin.longitude]}>
```

`MapView.jsx` uses `pin.latitude` and `pin.longitude` directly — not `pin.lat`/`pin.lng`. This works because `pinService.js` spreads the backend response and adds both sets of properties (`lat`/`lng` aliases plus the original `latitude`/`longitude`). The CLAUDE.md claim is incorrect. **Do not change `MapView.jsx` to use `pin.lat`/`pin.lng` — the current code is correct and working.**

### ⚠️ Live Code Discovery: PinControllerTest Import Fixed

**CLAUDE.md states:** `PinControllerTest.java` — **Needs fix:** missing `import travelmap.controller.PinController;`

**Actual code in `PinControllerTest.java` (line 3):**
```java
import travelmap.controller.PinController;
```

The import is already present. This TODO item has been resolved and should be removed from cross-team TODO lists.

### Test Inventory

| Test File | Type | Tests | FRs Covered |
|---|---|---|---|
| `PinServiceTest.java` | Backend unit | 15 | FR1 (6), FR4 (2), FR2 (3), FR3 (2), FR11 (2) |
| `PinControllerTest.java` | Backend integration | 10 | FR1 (2), FR4 (2), FR2 (2), FR3 (2), FR11 (2) |
| `TripServiceTest.java` | Backend unit | 11 | FR5 — trip CRUD and timeline sort |
| `TripControllerTest.java` | Backend integration | 4 | FR5 — trip endpoints |
| `DistanceCalculatorTest.java` | Backend unit | 22 | FR8 — Haversine math, coordinate validation, unit conversion |
| `DistanceResultTest.java` | Backend unit | 14 | FR8 — DTO construction, unit selection, skipped pins, defensive copy |
| `MapAPIClientTest.java` | Backend unit (Mockito) | 19 | FR8 — all 3 distance modes, null coords, sort order, ownerId validation |
| `MapControllerTest.java` | Backend integration | 12 | FR8 — all 3 endpoints, 200/400/404 paths, unit param, skippedPinIds |
| `MediaServiceTest.java` | Backend unit | 23 | FR7 — upload/read/delete logic; NFR4 — path traversal defense, filename sanitisation |
| `MediaControllerTest.java` | Backend integration | 14 | FR7 — HTTP layer (201/200/204); NFR3 — ISE → 500 disk failure mapping; NFR4 — IAE → 400/404 |
| `App.test.js` | Frontend | 24 | FR6, FR3, FR2, FR11, FR10, FR8 |
| `PinForm.test.js` | Frontend | 11 | FR1 — form inputs, save, cancel (8); FR7 — file input accept, save includes file, Remove clears payload (3) |
| `EditPinForm.test.js` | Frontend | 18 | FR2 — form pre-fill and save (8); FR11 — privacy dropdown (5); FR7 — media display, replace, remove, save payload (6) (note: 2 pre-existing tests updated to include `mediaFile: null`) |
| `MapView.test.js` | Frontend | 14 | FR6 markers, FR3 delete button, FR8 Measure button (8 new) |
| `DistancePanel.test.jsx` | Frontend | 17 | FR8 — panel visibility, result display, cancel button |
| `mediaService.test.js` | Frontend | 9 | FR7 — uploadMedia (4: success, null pinId, null file, non-OK); deleteMedia (2: success, non-OK); mediaUrl helper (3) |
| `MediaPreview.test.jsx` | Frontend | 10 | FR7 — no source → null (2); extension-based rendering img/video/audio/download (5); alt prop (1); file wins over src (1); blob URL cleanup on unmount (1) |
| **Backend Total** | | **144** | Confirmed: `mvn test` BUILD SUCCESS |
| **Frontend Total** | | **116** | Confirmed: `npm test` 116 passed, 0 failed |
| **Grand Total** | | **260** | |

> **⚠️ Floor:** 144 backend / 116 frontend / 260 total — this count must never decrease. Run both `mvn test` and `npm test` before any merge.

---

## SECTION 5: FUNCTIONAL REQUIREMENTS COVERAGE

| FR | Name (exact from SRS) | Status | Backend Classes | Frontend Components | Test Files |
|---|---|---|---|---|---|
| FR1 | Create Travel Pin | **Complete** | `PinController`, `PinService`, `PinRepository`, `Pin` | `App.js`, `PinForm.jsx`, `pinService.js` | `PinServiceTest.java` (6), `PinControllerTest.java` (2), `PinForm.test.js` (8) |
| FR2 | Edit Travel Pin | **In Progress** | `PinController`, `PinService`, `PinRepository`, `Pin` | (UI for edit not yet wired in React) | `PinServiceTest.java` (3), `PinControllerTest.java` (2) |
| FR3 | Delete Travel Pin | **In Progress** | `PinController`, `PinService`, `PinRepository` | (UI for delete not yet wired in React) | `PinServiceTest.java` (2), `PinControllerTest.java` (2) |
| FR4 | View Pins and Trips | **In Progress** | `PinController.getAllPins()`, `PinService.getAllPins()` | `App.js` (useEffect), `MapView.jsx` | `PinServiceTest.java` (2), `PinControllerTest.java` (2) |
| FR5 | Organize Trip | **Not Started** | `TripController` (stub), `TripService` (MISSING), `TripRepository` (MISSING), `Trip` (stub POJO) | (not started) | none |
| FR6 | Visualize Map | **Complete** | (map tiles served by OpenStreetMap externally) | `MapView.jsx`, `mapService.js`, `App.js` | `App.test.js` (1) |
| FR7 | Upload Media | **Complete** (May 2026) | `MediaController` (IMPL — 3 endpoints), `MediaService` (IMPL — upload/read/delete, UUID filenames, NFR4 path-traversal), `IMediaController` (IMPL), `Pin.mediaUrl` (stores relative URL) | `mediaService.js` (fetch wrappers), `MediaPreview.jsx` (rendering), `PinForm.jsx` (file input + preview), `EditPinForm.jsx` (replace/remove flow), `App.js` (chained upload + handleRemoveMedia), `MapView.jsx` (thumbnail in popup) | `MediaServiceTest.java` (23), `MediaControllerTest.java` (14), `PinForm.test.js` (+3), `EditPinForm.test.js` (+6), `mediaService.test.js` (9), `MediaPreview.test.jsx` (10) |
| FR8 | Calculate Distances | **Fully Complete** | `DistanceCalculator.java` (IMPL), `DistanceResult.java` (IMPL), `MapAPIClient.java` (IMPL — all 3 modes), `MapController.java` (IMPL — 3 endpoints, 12 tests) | `mapDistanceService.js` (fetch wrapper), `DistancePanel.jsx` (result panel, 17 tests), `MapView.jsx` (Measure button, 8 new tests), `TripList.jsx` (trip distance in sidebar), `App.js` (measure flow + tripDistances useEffect, 10 new tests) | `DistanceCalculatorTest.java` (22), `DistanceResultTest.java` (14), `MapAPIClientTest.java` (19), `MapControllerTest.java` (12), `DistancePanel.test.jsx` (17), `MapView.test.js` (+8), `App.test.js` (+10) |
| FR9 | Filter and Search | **Not Started** | `SearchController.handleFilterSearch()` (stub), `PinRepository.findByLocationNameContainingIgnoreCase()` (IMPLEMENTED) | (not started) | none |
| FR10 | View Timeline | **Not Started** | `TripController.handleViewTimeline()` (stub) | (not started) | none |
| FR11 | Set Pin Privacy | **In Progress** | `PinController.setPinPrivacy()`, `PinService.setPinPrivacy()`, `Privacy` enum | (UI for privacy not yet wired in React) | `PinServiceTest.java` (2), `PinControllerTest.java` (2) |
| FR12 | Generate Share Link | **Not Started** | `SharingController.handleGenerateShareLink()` (stub) | (not started) | none |
| FR13 | View Travel Statistics | **Not Started** | `SearchController.handleViewStatistics()` (stub), `Statistics` (stub POJO) | (not started) | none |
| FR14 | Manage User Account | **Not Started** | `ProfileService` (stub), `AccountFacade` (stub), `AuthController` (MISSING) | (not started) | none |
| FR15 | Persist User Data | **In Progress** | `PinRepository` (H2 persists across refreshes, resets on restart; PostgreSQL not yet active) | `App.js` useEffect → `getPins()` reloads pins on mount | `PinServiceTest.java` (indirectly) |

> **FR2/FR3/FR11 note:** Backend endpoints are fully implemented and tested, but the React frontend UI does not yet have edit/delete/privacy buttons wired up. The endpoints work via direct HTTP calls; the React interface needs UI elements added.

> **FR8 user-facing behavior:** Pin-to-pin: user clicks Measure on pin A (DistancePanel appears in waiting state), then Measure on pin B — panel updates to show "Pin A to Pin B" with km and mi equally styled side-by-side. Clicking the same pin twice or pressing Cancel clears all measuring state. Trip total: sidebar automatically shows km / mi under each trip's pin list, re-fetching whenever trips or pins change; pins with null coordinates are skipped and the count is reported. Accumulated total across all trips: endpoint implemented but ownerId verification is pending Wilson's auth merge.

> **FR4/FR15 note:** Pins are returned for ALL users currently (no owner filtering). Full FR4 compliance requires Wilson's auth merge to filter by `ownerId`.

---

## SECTION 6: NON-FUNCTIONAL REQUIREMENTS

### NFR1: Usability
**SRS threshold:** A new user should be able to successfully create a pin within 2 minutes of launching the application for the first time.

**Current compliance:** Partial. The map renders immediately and clicking places a pin form. The workflow (click map → fill form → save) is intuitive. However, the application requires no authentication currently (no login screen yet), which simplifies the first-run experience. Full compliance requires Wilson's auth flow to also be within the 2-minute window.

**Remaining work:** Ensure login/registration UI (Wilson's task) does not add friction that pushes first pin creation beyond 2 minutes.

---

### NFR2: Performance
**SRS thresholds:**
- Application responds to user input within **2.5 seconds or less**
- Creating a pin takes no more than **3 seconds** with visual confirmation
- Pin/trip data loads within **2 seconds** (FR4)
- Search results display within **2 seconds** (FR9)
- Statistics load within **2 seconds** (FR13)
- Timeline loads within **2 seconds** (FR10)
- Updated information reflected within **1 second** after edit (FR2)
- Deletion completes within **1 second** (FR3)
- Privacy changes apply immediately (FR11)
- Share link becomes active within **1 second** (FR12)
- Account update completes within **1 second** (FR14)
- Data reloads within **3 seconds** after login (FR15)

**Current compliance:** H2 + Spring Data JPA is fast enough for development. No formal performance testing has been run. PostgreSQL with indexing will be needed for production load.

**Remaining work:** Run performance tests with realistic data volumes before final submission. Add loading indicators in React for visual confirmation per NFR2.

---

### NFR3: Reliability
**SRS thresholds:**
- Application operates without frequently crashing or losing data during normal usage
- Maintains uptime of at least **99% monthly**

**Current compliance:** H2 in-memory database means all data is lost on backend restart — not production-ready. No uptime monitoring or deployment infrastructure exists yet.

**Remaining work:** Switch to PostgreSQL for persistence. Set up deployment infrastructure before final submission if live hosting is required.

---

### NFR4: Security
**SRS thresholds:**
- Pins and trips may only be edited by their owner or those with edit permissions
- User accounts shall be protected with a username and password

**Current compliance:** Not compliant. `PinService.getAllPins()` returns all pins for all users. No authentication exists. No owner enforcement on create/edit/delete/privacy.

**Remaining work:** Wilson's auth merge is the blocker. After merge: add `ownerId` to `Pin`, filter by owner in `getAllPins()`, enforce owner check in `updatePin()`, `deletePin()`, `setPinPrivacy()`.

---

### NFR5: Portability
**SRS thresholds:**
- Functional across all major modern web browsers
- Functions correctly across desktop and mobile devices

**Current compliance:** React + Leaflet are cross-browser and responsive by design. No mobile-specific testing has been documented.

**Remaining work:** Test on Firefox, Safari, Chrome, and mobile viewports before final submission. Verify map renders correctly at mobile breakpoints.

---

### NFR6: Maintainability
**SRS thresholds:**
- Software must be easy to debug and maintain for future updates
- Components must be individually updatable or replaceable without affecting the rest of the system

**Current compliance:** Good. SOLID principles are applied throughout. Constructor injection makes all classes independently testable. Interfaces decouple implementations. MVC separation means database, business logic, and HTTP layers can each change independently.

**Remaining work:** Move `PinService.java` from `travelmap.repository` to `travelmap.service`. Replace per-method `try/catch` in `PinController` with a `@ControllerAdvice` global exception handler. Add Javadoc to all new public methods.

---

## SECTION 7: CRITICAL TODOS AND CROSS-TEAM DEPENDENCIES

### Charlie's TODOs
- [ ] Move `PinService.java` from `travelmap.repository` to `travelmap.service` package before final submission
- [ ] Move `MediaService.java` from `travelmap.media` to `travelmap.service` package in the same pass (tech debt — mirrors the existing PinService note)
- [ ] Create `travelmap/service/` directory
- [ ] Replace `@CrossOrigin("http://localhost:3000")` with a proper CORS configuration class before production
- [ ] Add a `@ControllerAdvice` global exception handler to replace per-method try/catch in `PinController`
- [ ] Add wire-up for Edit Pin UI in React (FR2 backend is complete, React UI not wired)
- [ ] Add wire-up for Delete Pin UI in React (FR3 backend is complete, React UI not wired)
- [ ] Add wire-up for Privacy setting UI in React (FR11 backend is complete, React UI not wired)
- [ ] **Orphan file cleanup:** `PinService.deletePin()` does not call `MediaService.deleteMedia()` — deleting a pin with media leaves the file on disk. Add the call before removing the DB record (easy follow-up if time allows before May 12 demo).
- [ ] **Multi-media per pin:** `Pin.mediaUrl` is a single `String`. FR7 SRS allows multiple files per pin. If time permits, upgrade to `@OneToMany` with `Media` entity. Not blocking for demo.
- [ ] Remove `Coordinate.java` dead code before final submission

### Wilson's TODOs (auth merge impacts)
- [ ] Implement `AuthController.java` with `login()` and `logout()` endpoints
- [ ] Add Spring Security + JWT configuration
- [ ] Add `login()` and `logout()` to `IAuthService` interface
- [ ] Implement `AuthService.login()`, `AuthService.logout()`, `AuthService.isLoggedIn()`, `AuthService.getCurrentUser()`
- [ ] Implement `RegistrationService.register()`
- [ ] Implement `SessionManager.generateSessionToken()`, `isTokenValid()`
- [ ] Implement `PasswordValidator.validatePassword()`, `hashPassword()`, `checkMatch()`
- [ ] Implement `ProfileService.updateProfile()`
- [ ] Implement `AccountFacade.login()`, `logout()`, `isLoggedIn()`, `getCurrentUser()`, `register()`, `updateProfile()`
- [ ] Implement `UserRepositoryImpl.findByUsername()`, `save()`, `update()`
- [ ] Add `@Entity` and JPA annotations to `User.java`; add getters/setters
- [ ] Create DTOs for login/registration requests and responses
- [ ] **After merge: Add `ownerId` (Long) field to `Pin.java`**
- [ ] **After merge: Add `findByOwnerId(Long ownerId)` to `PinRepository`**
- [ ] **After merge: Update `PinService.getAllPins()` to accept `userId` and filter by owner**
- [ ] **After merge: Update `PinController.getAllPins()` to extract authenticated user and pass id to service**
- [ ] **After merge: Add owner verification to `PinService.updatePin()`, `deletePin()`, `setPinPrivacy()`**

### Gage's TODOs (trips merge impacts)
- [ ] Add `@Entity` and JPA annotations to `Trip.java`; add getters/setters
- [ ] Create `TripRepository.java` (extends `JpaRepository<Trip, Long>` or appropriate type)
- [ ] Create `TripService.java` in `travelmap.service` package
- [ ] Implement `TripController.handleOrganizeTrip()`, `handleViewTimeline()`, `handleSetTripPrivacy()`
- [ ] Define proper REST endpoints in `TripController` (currently methods return void with no HTTP mapping)
- [ ] **After merge: Add `tripId` (Long) foreign key to `Pin.java`**
- [ ] **After merge: Add `findByTripId(Long tripId)` to `PinRepository`**
- [ ] Implement FR5 (Organize Trip), FR10 (View Timeline)

### Shared TODOs
- [ ] Switch database from H2 to PostgreSQL before final submission: uncomment PostgreSQL dependency in `pom.xml` and PostgreSQL config in `application.properties`, remove H2 blocks
- [ ] Add `@Entity` to `Media.java`, `Trip.java`, `Statistics.java` and create corresponding JPA repositories for persistence
- [ ] Implement `SearchController.handleFilterSearch()` (FR9) — Gage's task, depends on Trip merge
- [ ] Implement `SearchController.handleViewStatistics()` (FR13)
- [ ] Implement `MapController.handleCalculateDistance()` (FR8) — implement `MapAPIClient.calculateDistance()`
- [ ] Implement `SharingController.handleGenerateShareLink()` (FR12)
- [ ] Run performance testing against NFR2 thresholds before final submission
- [ ] Test across Firefox, Safari, Chrome, and mobile viewports (NFR5)

---

## SECTION 8: PERFORMANCE AND SUCCESS METRICS

### Per-FR Response Time Thresholds (from SRS)
| FR | Operation | Threshold |
|---|---|---|
| FR1 | Pin create (POST /api/pins) | ≤ 3 seconds with visual confirmation |
| FR2 | Pin update reflected in UI | ≤ 1 second |
| FR3 | Pin deletion completes | ≤ 1 second |
| FR4 | Pins and trips display after loading | ≤ 2 seconds |
| FR7 | Media preview displayed after upload | ≤ 2 seconds |
| FR9 | Filtered search results display | ≤ 2 seconds |
| FR10 | Timeline loads | ≤ 2 seconds |
| FR11 | Privacy change applies | Immediately |
| FR12 | Share link becomes active | ≤ 1 second |
| FR13 | Statistics load | ≤ 2 seconds |
| FR14 | Account update completes | ≤ 1 second |
| FR15 | All data reloads after login | ≤ 3 seconds |
| NFR2 | Any user input response | ≤ 2.5 seconds |

### Test Count Floor
- **Backend: 144** | **Frontend: 116** | **Total: 260**
- **This number must never decrease.** Every increment must run both `mvn test` and `npm test` before being declared complete.
- Every new REST endpoint requires a corresponding JUnit test in `PinControllerTest.java` (or a new test file).
- Every new public service method requires a corresponding unit test in the relevant test file.

### NFR Quantitative Thresholds
| NFR | Threshold | Current State |
|---|---|---|
| NFR1 Usability | New user creates first pin within 2 minutes | Partial — no auth friction currently |
| NFR2 Performance | All operations ≤ 2.5 seconds; pin create ≤ 3 seconds | Not formally tested |
| NFR3 Reliability | 99% uptime monthly | Not deployed |
| NFR4 Security | Owner-only edit/delete; password-protected accounts | Not compliant — auth not yet implemented |
| NFR5 Portability | All major browsers; desktop and mobile | Not formally tested |
| NFR6 Maintainability | Components independently updatable | Compliant by design |

---

## SECTION 9: AGENT RULES

> **MANDATORY:** These rules apply to every agent in every session without exception.

1. **Read this entire document before taking any action.**

2. **Read `CLAUDE.md` before writing any code.** It is the authoritative description of the current implementation state. This Master Context Document supplements it — both must be read.

3. **Never reduce the passing test count below 144 (backend) / 116 (frontend) / 260 (total).** Run `mvn test` AND `npm test` and confirm both pass before declaring any increment complete.

4. **Never move a class to a different package without explicit instruction.** In particular, do not move `PinService.java` to `travelmap.service` unless the user explicitly requests it, even though CLAUDE.md marks it as a TODO.

5. **Never put business logic in a Controller class.** Validation, defaults, conditional logic, and calculations belong in the Service layer. Controllers receive, delegate, and return.

6. **Never put HTTP annotations (`@GetMapping`, `@PostMapping`, `@RequestBody`, `ResponseEntity`, etc.) in a Service class.** Services are unaware of HTTP.

7. **Never let React import Java classes or Java import React components.** The only connection between frontend and backend is HTTP over the network.

8. **Run `mvn test` AND `npm test` before declaring any increment complete.** Both must pass. Do not mark work done until both test suites are green.

9. **Every new public method in the backend requires a Javadoc comment.** Follow the style already established in `PinController.java`, `PinService.java`, `PinRepository.java`, and `IPinController.java`.

10. **Every new REST endpoint requires a corresponding JUnit test** in `PinControllerTest.java` or a new `*ControllerTest.java` file using `@WebMvcTest`. The test must cover at minimum: success case (correct HTTP status + response body) and not-found/invalid case (4xx status).

11. **Before modifying `Pin.java` or `User.java`, confirm with the assigned teammate.** These are shared model classes. Changes to `Pin.java` affect Charlie's entire pin layer. Changes to `User.java` affect Wilson's auth layer.

12. **`PinService.getAllPins()` must filter by `ownerId` after Wilson's auth merge — do not implement this without that merge.** The method signature and body must change together with Wilson's `ownerId` field addition to `Pin.java` and `findByOwnerId()` addition to `PinRepository`.

13. **Mark any deviation from this document as a `// TODO` comment in the relevant file,** with the reason for the deviation and which agent session made it.

14. **After completing any increment, update `CLAUDE.md` to reflect:**
    - Change the status of any class you implemented from `[STUBBED]` or `[MISSING]` to `[IMPLEMENTED]`
    - Add any new files you created to the project structure section
    - Update the test count
    - Add any new TODOs you discovered
    - Add a dated entry at the bottom of `CLAUDE.md` summarizing what changed, e.g.:
      ```
      Apr 9 2026 — Implemented PUT /api/pins/{id}, updated PinController and PinService, added 3 tests
      ```

15. **The `travelmap.service` directory does not exist yet.** Do not create files in it until the package migration is explicitly requested and coordinated.

16. **`Coordinate.java` is dead code.** Do not use it in any new implementation. Do not add fields of type `Coordinate` to any class. Use `latitude`/`longitude` (Double) directly on entity classes as established in `Pin.java`.

17. **MapView.jsx uses `pin.latitude` and `pin.longitude` directly** (not `pin.lat`/`pin.lng` as stated in CLAUDE.md). The `pinService.js` normalisation adds both sets of fields, so both work. Do not change `MapView.jsx` marker rendering unless the user explicitly requests it.

---

## APPENDIX: CHANGE LOG

| Date | Author | Change |
|---|---|---|
| 2026-05-05 | Charlie | FR7 Media Upload complete — backend + frontend. Backend: `IMediaController` (3-method contract), `MediaService` (23 tests — FR7 + NFR4 path traversal), `MediaController` (14 tests — FR7 HTTP layer + NFR3/NFR4 status mapping), `application.properties` multipart config. Frontend: `mediaService.js` (9 tests), `MediaPreview.jsx` (10 tests), `PinForm.jsx` (+3 tests), `EditPinForm.jsx` (+6 tests), `App.js` chained upload + handleRemoveMedia, `MapView.jsx` media thumbnail, `setupTests.js` JSDOM mocks. New floors: 144 backend / 116 frontend / 260 total. Branch: `feature/media-controller`. 22 commits, ready for PR. |
| 2026-04-26 | Charlie | FR8 frontend complete: `mapDistanceService.js`, `DistancePanel.jsx` (17 tests), `MapView.jsx` Measure button (8 new tests), `TripList.jsx` trip distance display, `App.js` wiring (10 new tests). Frontend floor: 88. Total floor: 195. All commits on `feature/map-controller` pushed, PR to dev pending. `feature/map-distance-ui` branch not needed — frontend implemented on this branch directly. Next: FR7 and FR6 after merge. |
| 2026-04-08 | Master Context Generator | Initial document created from live code audit. Verified 34 tests (not 36 as CLAUDE.md claims). Confirmed `PinControllerTest` import already fixed. Noted MapView uses `pin.latitude`/`pin.longitude` (not `pin.lat`/`pin.lng`). |
| 2026-04-08 | Charlie | Renamed package.json name field from "leaflet-test" to "travel-map" |
| 2026-04-25 | Charlie | FR8 distance layer complete (service only — controller Step 11 pending). New files: DistanceCalculator, DistanceResult, MapAPIClientTest. Updated: IMapController (3 ResponseEntity methods), MapService (4 FR8 signatures), MapAPIClient (fully implemented), PinRepository (findByTripId + findByOwnerId). Test floor: 95 backend / 53 frontend / 148 total. |
| 2026-04-26 | Charlie | Implemented MapController (12 tests), all four commits pushed to feature/map-controller. Backend floor: 107. Total floor: 160. PR to dev pending. |
