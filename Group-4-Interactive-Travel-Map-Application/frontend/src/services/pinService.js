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

// FR1 — Create a new pin and return it with lat/lng normalised for MapView.
export async function createPin({ lat, lng, locationName, visitDate }) {
  try {
    const response = await fetch('http://localhost:8080/api/pins', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ locationName, visitDate, latitude: lat, longitude: lng })
    });
    const saved = await response.json();
    return { ...saved, lat: saved.latitude, lng: saved.longitude };
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
    };
  }
}

// FR4, FR15 — Fetch all saved pins from the backend and normalise field names.
// Called by App.js on mount via useEffect so pins reload on every page refresh.
export async function getPins() {
  const response = await fetch('http://localhost:8080/api/pins');
  if (!response.ok) throw new Error('Failed to fetch pins');
  const pins = await response.json();
  return pins.map(p => ({ ...p, lat: p.latitude, lng: p.longitude }));
}

// FR3 — Permanently delete a pin by id.
// Called by App.js after user confirms the deletion dialog.
// Returns true on success so App.js can remove the pin from state.
export async function deletePin(id) {
  const response = await fetch(`http://localhost:8080/api/pins/${id}`, {
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
  const response = await fetch(`http://localhost:8080/api/pins/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedPin)
  });
  if (!response.ok) throw new Error(`Failed to update pin ${id}`);
  const saved = await response.json();
  return { ...saved, lat: saved.latitude, lng: saved.longitude };
}

// FR11 — Update the privacy level of a pin immediately on change.
// Uses PATCH because only one field is being changed.
// Backend accepts the Privacy enum value as a plain JSON string:
//   "PRIVATE" | "FRIENDS_ONLY" | "PUBLIC"
// Called by App.js when user changes the privacy dropdown in EditPinForm.
// Changes apply immediately per SRS FR11 — no Save button needed.
export async function setPinPrivacy(id, privacyLevel) {
  const response = await fetch(`http://localhost:8080/api/pins/${id}/privacy`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(privacyLevel)
  });
  if (!response.ok) throw new Error(`Failed to set privacy for pin ${id}`);
  const saved = await response.json();
  return { ...saved, lat: saved.latitude, lng: saved.longitude };
}
