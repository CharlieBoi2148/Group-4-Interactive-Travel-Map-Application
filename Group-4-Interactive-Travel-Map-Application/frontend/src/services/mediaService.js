// frontend/src/services/mediaService.js
//
// FR7 — Frontend service layer for the Media REST endpoints.
//
// Uses relative URLs and the CRA dev proxy (matches the FR8
// mapDistanceService.js pattern, not the older hardcoded
// `http://localhost:8080` pinService.js pattern).
//
// Exports:
//   - uploadMedia(pinId, file)  — POST /api/media/{pinId}, returns updated Pin
//   - deleteMedia(pinId)        — DELETE /api/media/{pinId}
//   - mediaUrl(pin)             — pure helper, returns the URL the browser
//                                 should fetch the media from (or null)
//
// Network errors are wrapped with a hint about starting the backend,
// matching the wrapNetworkError pattern in mapDistanceService.js.

const MEDIA_API_URL = '/api/media';

const NETWORK_HINT =
  'Start the Java backend (cd backend && mvn spring-boot:run), then restart this app (npm start) so the dev proxy can reach port 8080.';

/**
 * Wraps a fetch network error with the standard backend-not-running hint.
 * Mirrors the helper in mapDistanceService.js so the user sees the same
 * actionable message regardless of which service threw.
 */
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
 * FR7 — Upload a media file and associate it with an existing pin.
 *
 * @param {number} pinId — id of the pin to attach the file to
 * @param {File}   file  — file from an <input type="file"> change event
 * @returns {Promise<Object>} updated Pin (now has mediaUrl populated)
 * @throws Error on network failure, validation failure (400), pin missing
 *   (404), or server error (500). The error message includes the HTTP
 *   status when the backend responded.
 */
export async function uploadMedia(pinId, file) {
  if (pinId == null) {
    throw new Error('uploadMedia requires a pinId');
  }
  if (!file) {
    throw new Error('uploadMedia requires a file');
  }

  const formData = new FormData();
  formData.append('file', file);

  let response;
  try {
    response = await fetch(`${MEDIA_API_URL}/${pinId}`, {
      method: 'POST',
      body: formData,
      // No Content-Type header — the browser sets the multipart boundary
      // automatically when given a FormData body. Setting it manually
      // breaks the request.
    });
  } catch (err) {
    throw wrapNetworkError(err);
  }

  if (!response.ok) {
    throw new Error(
      `Failed to upload media for pin ${pinId} (HTTP ${response.status})`,
    );
  }

  return response.json();
}

/**
 * FR7 / FR3 — Remove a pin's media file and clear its mediaUrl.
 *
 * @param {number} pinId — id of the pin whose media should be removed
 * @returns {Promise<void>}
 * @throws Error on network failure or server error.
 */
export async function deleteMedia(pinId) {
  if (pinId == null) {
    throw new Error('deleteMedia requires a pinId');
  }

  let response;
  try {
    response = await fetch(`${MEDIA_API_URL}/${pinId}`, {
      method: 'DELETE',
    });
  } catch (err) {
    throw wrapNetworkError(err);
  }

  if (!response.ok) {
    throw new Error(
      `Failed to delete media for pin ${pinId} (HTTP ${response.status})`,
    );
  }
}

/**
 * Pure helper — returns the URL the browser should fetch this pin's
 * media from, or null if the pin has no media.
 *
 * The backend stores mediaUrl as a relative path like "/api/media/abc.jpg".
 * That path is also what we want React to use as <img src>. The CRA
 * dev proxy forwards /api/* to the backend, and in production the
 * backend serves both the static React build and the API on the same
 * origin, so the relative path works in both environments.
 *
 * @param {Object|null} pin — pin object, or null
 * @returns {string|null} URL to use as src, or null when no media
 */
export function mediaUrl(pin) {
  if (!pin || !pin.mediaUrl) {
    return null;
  }
  return pin.mediaUrl;
}
