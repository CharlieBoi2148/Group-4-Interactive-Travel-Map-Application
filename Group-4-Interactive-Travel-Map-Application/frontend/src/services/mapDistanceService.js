// --- MAP DISTANCE SERVICE (MapController on the backend) ---------------------
// Wraps the three FR8 distance endpoints exposed by MapController.java.
// Uses relative URLs + the CRA proxy so fetch calls never hardcode port 8080.
// Mirrors the pattern established in tripService.js and pinService.js.

const DISTANCE_URL = '/api/map/distance';

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

/**
 * FR8 mode 1 — pin-to-pin great-circle distance.
 *
 * @param {number} lat1 latitude of point A
 * @param {number} lon1 longitude of point A
 * @param {number} lat2 latitude of point B
 * @param {number} lon2 longitude of point B
 * @param {string} [unit='km'] 'km' or 'mi'
 * @returns {Promise<{distanceKm: number, distanceMi: number, preferred: number, unit: string, skippedPinIds: number[]}>}
 */
export async function getPinToPin(lat1, lon1, lat2, lon2, unit = 'km') {
  const params = new URLSearchParams({
    lat1: String(lat1),
    lon1: String(lon1),
    lat2: String(lat2),
    lon2: String(lon2),
    unit,
  });

  let response;
  try {
    response = await fetch(`${DISTANCE_URL}?${params}`);
  } catch (e) {
    throw wrapNetworkError(e);
  }

  if (!response.ok) {
    throw new Error(
      `Could not calculate distance (HTTP ${response.status}). ${NETWORK_HINT}`
    );
  }
  return response.json();
}

/**
 * FR8 mode 2 — total distance of a single trip in chronological order.
 *
 * @param {number|string} tripId database id of the trip
 * @param {string} [unit='km'] 'km' or 'mi'
 * @returns {Promise<{distanceKm: number, distanceMi: number, preferred: number, unit: string, skippedPinIds: number[]}>}
 */
export async function getTripDistance(tripId, unit = 'km') {
  const params = new URLSearchParams({ unit });

  let response;
  try {
    response = await fetch(`${DISTANCE_URL}/trip/${tripId}?${params}`);
  } catch (e) {
    throw wrapNetworkError(e);
  }

  if (response.status === 404) {
    throw new Error(`Trip ${tripId} not found.`);
  }
  if (!response.ok) {
    throw new Error(
      `Could not calculate trip distance (HTTP ${response.status}). ${NETWORK_HINT}`
    );
  }
  return response.json();
}

/**
 * FR8 mode 3 — accumulated travel distance across all of a user's trips.
 *
 * @param {string} ownerId user id — placeholder until Wilson's auth lands
 * @param {string} [unit='km'] 'km' or 'mi'
 * @returns {Promise<{distanceKm: number, distanceMi: number, preferred: number, unit: string, skippedPinIds: number[]}>}
 */
export async function getTotalDistance(ownerId, unit = 'km') {
  const params = new URLSearchParams({ ownerId, unit });

  let response;
  try {
    response = await fetch(`${DISTANCE_URL}/total?${params}`);
  } catch (e) {
    throw wrapNetworkError(e);
  }

  if (!response.ok) {
    throw new Error(
      `Could not calculate total distance (HTTP ${response.status}). ${NETWORK_HINT}`
    );
  }
  return response.json();
}
