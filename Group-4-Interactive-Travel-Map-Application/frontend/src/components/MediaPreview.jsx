// frontend/src/components/MediaPreview.jsx
//
// FR7 — Pure View component for rendering uploaded media inline.
//
// Renders one of <img>, <video>, or <audio> based on the file's MIME
// type or extension. No state, no fetch, no business logic. The
// containing component (PinForm, EditPinForm) decides what to show
// and passes the URL down.
//
// Two display modes:
//   1. Local file preview (before upload) — pass a File object; the
//      component creates a temporary blob: URL via URL.createObjectURL.
//   2. Server file preview (already uploaded) — pass the URL string
//      directly via the `src` prop.
//
// The component cleans up blob: URLs on unmount or when the file
// prop changes, to avoid memory leaks.

import React, { useEffect, useState } from 'react';

const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'];
const VIDEO_EXTS = ['mp4', 'webm', 'ogv', 'mov'];
const AUDIO_EXTS = ['mp3', 'wav', 'ogg', 'm4a', 'aac', 'flac'];

/**
 * Returns 'image', 'video', 'audio', or 'unknown' for a given source.
 * Tries the explicit MIME type first, falls back to the extension.
 *
 * @param {string} mimeType — File.type, or null
 * @param {string} src      — URL or filename to inspect
 * @returns {'image' | 'video' | 'audio' | 'unknown'}
 */
function detectMediaKind(mimeType, src) {
  if (mimeType) {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'video';
    if (mimeType.startsWith('audio/')) return 'audio';
  }
  if (src) {
    const lower = src.toLowerCase();
    const dot = lower.lastIndexOf('.');
    if (dot >= 0) {
      const ext = lower.substring(dot + 1);
      if (IMAGE_EXTS.includes(ext)) return 'image';
      if (VIDEO_EXTS.includes(ext)) return 'video';
      if (AUDIO_EXTS.includes(ext)) return 'audio';
    }
  }
  return 'unknown';
}

/**
 * MediaPreview — renders a media file inline.
 *
 * Props (one of `file` or `src` should be provided; if both, `file` wins):
 *   - file?: File          — File object from an <input type="file">
 *   - src?: string         — URL string to render (e.g. pin.mediaUrl)
 *   - alt?: string         — accessible label, used as alt/title text
 *   - maxWidth?: number    — max display width in px (default 200)
 *
 * Returns null when neither file nor src is provided, or when the media
 * type cannot be determined.
 */
export default function MediaPreview({ file, src, alt = 'Media preview', maxWidth = 200 }) {
  const [blobUrl, setBlobUrl] = useState(null);

  // Generate a blob: URL for File objects so the browser can render
  // them without a network round trip. Revoke the URL on unmount or
  // when the file changes — required to avoid memory leaks per the
  // URL.createObjectURL spec.
  useEffect(() => {
    if (!file) {
      setBlobUrl(null);
      return undefined;
    }
    const url = URL.createObjectURL(file);
    setBlobUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const effectiveSrc = blobUrl || src;
  if (!effectiveSrc) {
    return null;
  }

  const kind = detectMediaKind(file ? file.type : null, effectiveSrc);
  const style = {
    maxWidth: `${maxWidth}px`,
    maxHeight: `${maxWidth}px`,
    display: 'block',
    marginTop: '8px',
    borderRadius: '4px',
  };

  if (kind === 'image') {
    return <img src={effectiveSrc} alt={alt} title={alt} style={style} />;
  }

  if (kind === 'video') {
    return (
      <video
        src={effectiveSrc}
        controls
        style={style}
        aria-label={alt}
        title={alt}
      />
    );
  }

  if (kind === 'audio') {
    return (
      <audio
        src={effectiveSrc}
        controls
        style={{ marginTop: '8px', width: '100%' }}
        aria-label={alt}
        title={alt}
      />
    );
  }

  // Unknown type — give the user a download link rather than rendering
  // nothing, so they can still access the file.
  return (
    <a
      href={effectiveSrc}
      target="_blank"
      rel="noopener noreferrer"
      style={{ marginTop: '8px', display: 'inline-block' }}
    >
      Download media
    </a>
  );
}
