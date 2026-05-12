// --- SEARCH SERVICE (SearchController.java equivalent) ------------------------
// Frontend service layer — calls the backend SearchController for FR9.

const SEARCH_URL = '/api/search';

/**
 * FR9 — Search pins by optional keyword and optional trip filter.
 *
 * Backend: GET /api/search?keyword=...&tripId=...
 * Returns list of Pin objects (same shape as GET /api/pins).
 *
 * @param {Object} params
 * @param {string} [params.keyword]
 * @param {number|null|undefined} [params.tripId] when set, only pins on that trip
 * @returns {Promise<Array>} pins
 */
export async function searchPins({ keyword = '', tripId } = {}) {
  const params = new URLSearchParams();
  const q = String(keyword || '').trim();
  if (q) params.set('keyword', q);
  if (tripId != null && tripId !== '' && Number.isFinite(Number(tripId))) {
    params.set('tripId', String(Number(tripId)));
  }
  const qs = params.toString();
  const url = qs ? `${SEARCH_URL}?${qs}` : SEARCH_URL;

  const response = await fetch(url);
  if (response.status === 404) {
    throw new Error('Trip not found or you do not have access.');
  }
  if (!response.ok) {
    throw new Error(`Failed to search pins (HTTP ${response.status})`);
  }
  const data = await response.json();
  return Array.isArray(data) ? data : [];
}
