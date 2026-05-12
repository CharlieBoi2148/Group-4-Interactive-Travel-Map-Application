// --- PIN SERVICE (PinController.java equivalent) ------------------------------
// Frontend service layer — receives View events and decides what to do.
// Sends fetch() calls to the Java PinController REST endpoints:
//   POST   /api/pins              → createPin()
//   GET    /api/pins              → getPins()
//   DELETE /api/pins/{id}         → deletePin()
//   PUT    /api/pins/{id}         → updatePin()
//   PATCH  /api/pins/{id}/privacy → setPinPrivacy()
//
// Field name normalisation:
//   The Java backend returns pins with { latitude, longitude } field names.
//   MapView.jsx renders markers using { lat, lng }.
//   Both functions normalise the backend response by spreading the pin object
//   and adding lat/lng aliases so MapView never needs to change.

const PIN_API_URL = 'http://localhost:8080/api/pins';

/** Clears trip on PUT partial update; matches {@code Pin.NO_TRIP_ASSIGNMENT} in the Java model. */
export const NO_TRIP_ASSIGNMENT = -1;

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
export async function createPin({ lat, lng, locationName, country, region, visitDate, notes, tripId }) {
  try {
    const response = await fetch(PIN_API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        locationName,
        country, 
        region,
        visitDate,
        notes,
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
      id: saved.id ??  Date.now(),
      locationName: saved.locationName ?? locationName,
      visitDate: saved.visitDate ?? visitDate,
      country: saved.country ?? country ?? '',
      region: saved.region ?? region ?? '',
      notes: saved.notes ?? notes ?? '',
      tripId: saved.tripId ?? tripId ?? null,
    };
    return normalizePin(merged, lat, lng);
} catch (err) {
    console.error('Backend not available, using local fallback:', err);
    return normalizePin(
      {
        id: Date.now(),
        locationName: locationName || 'Unnamed Pin',
        country: country || '',
        region: region || '',
        visitDate: visitDate || 'No date',
        notes: notes || '',
        tripId: tripId ?? null,
      },
      lat,
      lng
    );
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

// FR3 — Permanently delete a pin by id.
// Called by App.js after user confirms the deletion dialog.
// Returns true on success so App.js can remove the pin from state.
export async function deletePin(id) {
  const response = await fetch(`${PIN_API_URL}/${id}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error(`Failed to delete pin ${id}`);
  return true;
}

// FR2 — Update an existing pin by id.
// Called by App.js when user saves changes in EditPinForm.
// Sends only the modified fields — PinService.java applies a partial update
// so unmodified fields are preserved on the backend.
export async function updatePin(id, updatedPin) {
  const response = await fetch(`${PIN_API_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedPin)
  });
  if (!response.ok) throw new Error(`Failed to update pin ${id}`);
  const saved = await response.json();
  return normalizePin(saved);
}

// FR11 — Update the privacy level of a pin immediately on change.
// Uses PATCH because only one field is being changed.
// Backend accepts the Privacy enum value as a plain JSON string:
//   "PRIVATE" | "FRIENDS_ONLY" | "PUBLIC"
// Called by App.js when user changes the privacy dropdown in EditPinForm.
// Changes apply immediately per SRS FR11 — no Save button needed.
export async function setPinPrivacy(id, privacyLevel) {
  const response = await fetch(`${PIN_API_URL}/${id}/privacy`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(privacyLevel)
  });
  if (!response.ok) throw new Error(`Failed to set privacy for pin ${id}`);
  const saved = await response.json();
  return normalizePin(saved);
}
