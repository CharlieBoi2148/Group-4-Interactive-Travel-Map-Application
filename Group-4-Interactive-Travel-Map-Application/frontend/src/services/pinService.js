// --- PIN SERVICE (PinController.java equivalent) ------------------------------
// Frontend service layer — receives View events and decides what to do.
// Sends fetch() calls to the Java PinController REST endpoints:
//   POST /api/pins  → createPin()
//   GET  /api/pins  → getPins()
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