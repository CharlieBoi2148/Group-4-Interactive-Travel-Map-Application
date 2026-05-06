// frontend/src/mediaService.test.js
//
// FR7 — Unit tests for the mediaService.js fetch wrappers.
// fetch is mocked globally so we can control responses precisely
// without making real HTTP calls.

import { uploadMedia, deleteMedia, mediaUrl } from './services/mediaService';

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  jest.restoreAllMocks();
});

// ── uploadMedia ───────────────────────────────────────────────────────────────

test('FR7 — uploadMedia POSTs FormData to /api/media/{pinId}', async () => {
  const file = new File(['data'], 'photo.jpg', { type: 'image/jpeg' });
  const updatedPin = { id: 42, mediaUrl: '/api/media/abc.jpg' };

  global.fetch.mockResolvedValueOnce({
    ok: true,
    status: 201,
    json: async () => updatedPin,
  });

  const result = await uploadMedia(42, file);

  expect(global.fetch).toHaveBeenCalledTimes(1);
  const [url, options] = global.fetch.mock.calls[0];
  expect(url).toBe('/api/media/42');
  expect(options.method).toBe('POST');
  expect(options.body).toBeInstanceOf(FormData);
  expect(options.headers).toBeUndefined();

  expect(result).toEqual(updatedPin);
});

test('FR7 — uploadMedia throws when pinId is null', async () => {
  const file = new File(['data'], 'photo.jpg', { type: 'image/jpeg' });

  await expect(uploadMedia(null, file)).rejects.toThrow('pinId');
  expect(global.fetch).not.toHaveBeenCalled();
});

test('FR7 — uploadMedia throws when file is null', async () => {
  await expect(uploadMedia(42, null)).rejects.toThrow('file');
  expect(global.fetch).not.toHaveBeenCalled();
});

test('FR7 — uploadMedia throws on non-OK response with HTTP status in message', async () => {
  const file = new File(['data'], 'photo.jpg', { type: 'image/jpeg' });

  global.fetch.mockResolvedValueOnce({
    ok: false,
    status: 400,
    json: async () => ({}),
  });

  await expect(uploadMedia(42, file)).rejects.toThrow(/HTTP 400/);
});

// ── deleteMedia ───────────────────────────────────────────────────────────────

test('FR7 — deleteMedia sends DELETE to /api/media/{pinId}', async () => {
  global.fetch.mockResolvedValueOnce({
    ok: true,
    status: 204,
  });

  await deleteMedia(42);

  expect(global.fetch).toHaveBeenCalledTimes(1);
  const [url, options] = global.fetch.mock.calls[0];
  expect(url).toBe('/api/media/42');
  expect(options.method).toBe('DELETE');
});

test('FR7 — deleteMedia throws on non-OK response', async () => {
  global.fetch.mockResolvedValueOnce({
    ok: false,
    status: 404,
  });

  await expect(deleteMedia(42)).rejects.toThrow(/HTTP 404/);
});

// ── mediaUrl helper ───────────────────────────────────────────────────────────

test('FR7 — mediaUrl returns null when pin is null', () => {
  expect(mediaUrl(null)).toBeNull();
});

test('FR7 — mediaUrl returns null when pin has no mediaUrl', () => {
  expect(mediaUrl({ id: 1 })).toBeNull();
  expect(mediaUrl({ id: 1, mediaUrl: null })).toBeNull();
});

test('FR7 — mediaUrl returns the pin mediaUrl when set', () => {
  expect(mediaUrl({ id: 1, mediaUrl: '/api/media/abc.jpg' })).toBe('/api/media/abc.jpg');
});
