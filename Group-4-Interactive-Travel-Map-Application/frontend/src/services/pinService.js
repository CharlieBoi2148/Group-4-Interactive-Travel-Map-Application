// --- PIN SERVICE (PinController.java equivalent) ------------------------------
// Frontend service layer — receives View events and decides what to do.
// In the full architecture these functions become fetch() calls to the
// Java PinController REST endpoints:
//   POST /api/pins  → createPin()
//   GET  /api/pins  → fetchPins()

// createPin is async now so App.js can await it consistently.
// When you swap in the real fetch() call, App.js needs zero changes.
export async function createPin({ lat, lng, locationName, visitDate }) {

  // TODO: replace this block with the fetch() call to Java backend:
  // const response = await fetch('http://localhost:8080/api/pins', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ locationName, visitDate, latitude: lat, longitude: lng })
  // });
  // return await response.json();

  return {
    id: Date.now(),
    lat,
    lng,
    locationName: locationName || 'Unnamed Pin',
    visitDate: visitDate || 'No date',
  };
}