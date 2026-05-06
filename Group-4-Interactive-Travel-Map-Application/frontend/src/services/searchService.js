// --- SEARCH SERVICE (SearchController.java equivalent) ------------------------
// Frontend service layer — calls the backend SearchController for FR9.

const SEARCH_URL = '/api/search';

/**
 * FR9 — Search pins by keyword.
 *
 * Backend: GET /api/search?keyword=...
 * Returns list of Pin objects (same shape as GET /api/pins).
 *
 * @param {Object} params
 * @param {string} [params.keyword]
 * @returns {Promise<Array>} pins
 */
export async function searchPins({ keyword = '' } = {}) {
  const q = String(keyword || '').trim();
  const url = q ? `${SEARCH_URL}?keyword=${encodeURIComponent(q)}` : SEARCH_URL;

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to search pins (HTTP ${response.status})`);
  }
  const data = await response.json();
  return Array.isArray(data) ? data : [];
}
