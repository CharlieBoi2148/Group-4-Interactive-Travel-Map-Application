// --- TRIP SERVICE (TripController on the backend) -----------------------------
// Dev: use relative URLs + "proxy" in package.json → CRA forwards /api/* to
// http://localhost:8080 (same-origin in the browser, avoids CORS / "Load failed").
//
// Empty date strings are sent as null so Jackson can bind LocalDate on the server.

const TRIPS_URL = '/api/trips';

const NETWORK_HINT =
  'Start the Java backend (cd backend && mvn spring-boot:run), then restart this app (npm start) so the dev proxy can reach port 8080.';

function wrapNetworkError(err) {
  const msg = err && err.message ? err.message : String(err);
  if (
    msg === 'Load failed' ||
    msg === 'Failed to fetch' ||
    msg === 'NetworkError when attempting to fetch resource.'
  ) {
    return new Error(`${msg} — ${NETWORK_HINT}`);
  }
  return new Error(`${msg} ${NETWORK_HINT}`);
}

function nullIfBlank(value) {
  if (value == null || String(value).trim() === '') return null;
  return String(value).trim();
}

/**
 * FR4 — Load all trips for display (dashboard / sidebar).
 * @returns {Promise<Array>} list of trip objects from the backend
 */
export async function getTrips() {
  let response;
  try {
    response = await fetch(TRIPS_URL);
  } catch (e) {
    throw wrapNetworkError(e);
  }
  if (!response.ok) {
    throw new Error(`Failed to fetch trips (HTTP ${response.status}). ${NETWORK_HINT}`);
  }
  return response.json();
}

/**
 * FR5 — Create a new trip (name required on the server).
 * @param {Object} trip
 * @param {string} trip.name
 * @param {string} [trip.description]
 * @param {string} [trip.startDate] ISO date yyyy-MM-dd or empty
 * @param {string} [trip.endDate] ISO date yyyy-MM-dd or empty
 * @param {string} [trip.privacyLevel] PRIVATE | FRIENDS_ONLY | PUBLIC
 * @returns {Promise<Object>} saved trip including generated id
 */
export async function createTrip({ name, description, startDate, endDate, privacyLevel }) {
  const body = {
    name,
    description: nullIfBlank(description),
    startDate: nullIfBlank(startDate),
    endDate: nullIfBlank(endDate),
    privacyLevel: privacyLevel || 'PRIVATE',
  };

  let response;
  try {
    response = await fetch(TRIPS_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  } catch (e) {
    throw wrapNetworkError(e);
  }

  const text = await response.text();
  let data = {};
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    /* non-JSON error body */
  }

  if (response.status === 400) {
    throw new Error(
      data.message || 'Trip validation failed (name is required).'
    );
  }
  if (!response.ok) {
    throw new Error(
      data.message ||
        data.error ||
        `Could not save trip (HTTP ${response.status}). ${NETWORK_HINT}`
    );
  }
  if (!text) {
    throw new Error(`Server returned an empty response. ${NETWORK_HINT}`);
  }
  return data;
}
