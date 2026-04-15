// --- PIN SERVICE (PinController.java equivalent) ------------------------------
// Frontend service layer — receives View events and decides what to do.
// Sends fetch() calls to the Java PinController REST endpoints:
//   POST /api/pins  → createPin()
//   GET  /api/pins  → getPins()
//
// Field name normalisation:
//   The Java backend may return pins with { latitude, longitude }.
//   UI components may use { lat, lng }.
//   Both functions normalise the backend response by spreading the pin object
//   and adding lat/lng aliases so map rendering code never needs to change.

const PIN_API_URL = 'http://localhost:8080/api/pins';

/**
 * Ensure a pin object has both lat/lng and latitude/longitude.
 * @param {Object} p raw pin
 * @param {number} [fallbackLat] used when server omits coordinates
 * @param {number} [fallbackLng]
 */
function normalizePin(p, fallbackLat, fallbackLng) {
  const latitude = p.latitude ?? p.lat ?? fallbackLat;
  const longitude = p.longitude ?? p.lng ?? fallbackLng;
  return {
    ...p,
    latitude,
    longitude,
    lat: latitude,
    lng: longitude,
  };
}

// FR1 — Create a new pin and return it with lat/lng normalised for the UI.
export async function createPin({ lat, lng, locationName, visitDate, tripId }) {
  try {
    const response = await fetch(PIN_API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        locationName,
        visitDate,
        latitude: lat,
        longitude: lng,
        tripId: tripId ?? null,
      }),
    });

    if (!response.ok) {
      throw new Error(`createPin failed: HTTP ${response.status}`);
    }
    const saved = await response.json();

    const merged = {
      ...saved,
      id: saved.id != null ? saved.id : Date.now(),
      locationName: saved.locationName ?? locationName,
      visitDate: saved.visitDate ?? visitDate,
      tripId: saved.tripId ?? tripId ?? null,
    };
    return normalizePin(merged, lat, lng);
  } catch (err) {
    console.error('Backend not available, using local fallback:', err);
    return {
      id: Date.now(),
      latitude: lat,
      longitude: lng,
      lat,
      lng,
      locationName: locationName || 'Unnamed Pin',
      visitDate: visitDate || 'No date',
      tripId: tripId ?? null,
    };
  }
}

// FR4, FR15 — Fetch all saved pins from backend and normalise field names.
// Called by App.js on mount via useEffect so pins reload on page refresh.
export async function getPins() {
  const response = await fetch(PIN_API_URL);
  if (!response.ok) throw new Error('Failed to fetch pins');
  const pins = await response.json();
  if (!Array.isArray(pins)) return [];
  return pins.map((p) => normalizePin(p));
}
