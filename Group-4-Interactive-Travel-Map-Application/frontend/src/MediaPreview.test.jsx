// frontend/src/MediaPreview.test.jsx
//
// FR7 — Unit tests for the MediaPreview component.
// Verifies that the correct HTML element is rendered based on MIME type or
// extension, that the alt prop threads through, that the file prop wins over
// src, and that blob: URLs are revoked on unmount to prevent memory leaks.

import { render, screen } from '@testing-library/react';
import MediaPreview from './components/MediaPreview';

// ── No source — returns null ──────────────────────────────────────────────────

test('FR7 — renders nothing when neither file nor src is provided', () => {
  const { container } = render(<MediaPreview />);
  expect(container.firstChild).toBeNull();
});

test('FR7 — renders nothing when src is an empty string', () => {
  const { container } = render(<MediaPreview src="" />);
  expect(container.firstChild).toBeNull();
});

// ── Extension-based rendering ─────────────────────────────────────────────────

test('FR7 — .jpg src renders an <img> element', () => {
  render(<MediaPreview src="/api/media/photo.jpg" />);
  expect(document.querySelector('img')).toBeInTheDocument();
});

test('FR7 — .png src renders an <img> element', () => {
  render(<MediaPreview src="/api/media/photo.png" />);
  expect(document.querySelector('img')).toBeInTheDocument();
});

test('FR7 — .mp4 src renders a <video controls> element', () => {
  render(<MediaPreview src="/api/media/clip.mp4" />);
  const video = document.querySelector('video');
  expect(video).toBeInTheDocument();
  expect(video.controls).toBe(true);
});

test('FR7 — .mp3 src renders an <audio controls> element', () => {
  render(<MediaPreview src="/api/media/track.mp3" />);
  const audio = document.querySelector('audio');
  expect(audio).toBeInTheDocument();
  expect(audio.controls).toBe(true);
});

test('FR7 — unknown extension renders a "Download media" link', () => {
  render(<MediaPreview src="/api/media/file.xyz" />);
  const link = screen.getByText('Download media');
  expect(link).toBeInTheDocument();
  expect(link.tagName).toBe('A');
});

// ── Accessibility ─────────────────────────────────────────────────────────────

test('FR7 — alt prop is applied to the rendered <img>', () => {
  render(<MediaPreview src="/api/media/photo.jpg" alt="Eiffel Tower" />);
  expect(document.querySelector('img').getAttribute('alt')).toBe('Eiffel Tower');
});

// ── File prop wins over src ───────────────────────────────────────────────────

test('FR7 — file prop wins over src: URL.createObjectURL is called with the file', () => {
  // Verifies the component creates a blob URL from the file rather than using
  // the src string. The blob URL itself is mocked in setupTests.js.
  URL.createObjectURL.mockClear();
  const file = new File(['x'], 'photo.jpg', { type: 'image/jpeg' });
  render(<MediaPreview file={file} src="/api/media/other.jpg" />);
  expect(URL.createObjectURL).toHaveBeenCalledWith(file);
});

// ── Blob URL cleanup ──────────────────────────────────────────────────────────

test('FR7 — URL.revokeObjectURL is called when component unmounts', () => {
  URL.createObjectURL.mockClear();
  URL.revokeObjectURL.mockClear();
  const file = new File(['x'], 'photo.jpg', { type: 'image/jpeg' });
  const { unmount } = render(<MediaPreview file={file} />);
  unmount();
  expect(URL.revokeObjectURL).toHaveBeenCalled();
});
