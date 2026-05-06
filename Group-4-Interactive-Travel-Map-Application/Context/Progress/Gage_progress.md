# [Gage] — Progress Log

## Active Branch
feature/trips-and-search

## Current Implementation State

### Completed
- [x] Set up feature/trips-and-search branch
- [x] Implement Trip Java class — name, description, start/end dates, List<Pin>
- [x] Implement TripRepository and TripService
- [x] Build TripList.jsx and TripForm.jsx
- [x] Wire tripService.js fetch calls to backend
- [x] Pin-to-trip assignment — trip dropdown in PinForm
- [x] Sort pins chronologically by visit date within trip
- [x] Build Timeline.jsx — chronological pin view (FR10)
- [x] Implement trip privacy — PRIVATE / FRIENDS_ONLY / PUBLIC on Trip
- [x] Implement SearchController — GET /api/search (FR9)
- [x] Build FilterSearch.jsx (FR9)
- [x] Wire searchService.js to GET /api/search (FR9)
- [x] Write JUnit tests — TripServiceTest and SearchControllerTest (FR5, FR9, FR10)

## Test Count
Starting floor: 34 — must never decrease

## Pending (my TODOs)

=======
## Cross-Team Dependencies Waiting On
- Pin.java needs ownerId field (Wilson — blocks Charlie's getAllPins filter)
- Trip.java needs tripId on Pin (Gage — blocks Charlie's findByTripId)

## Change Log
(add dated entries as you complete increments)