// --- VIEW: EDIT PIN FORM COMPONENT -------------------------------------------
// Pure View responsibility — captures FR2 inputs for editing an existing pin
// and delegates Save/Cancel actions to the Controller layer (App.js) via props.
// The View never saves data itself — it only reports what the user did.
//
// Receives the full pin object as a prop so inputs can be pre-filled with
// existing values. The user only sees and edits text fields — coordinates,
// id, and privacyLevel are never exposed here.
//
// FR11 — privacy dropdown triggers onPrivacyChange immediately on change,
// not on Save. This matches the SRS requirement that changes apply immediately.

import { useState } from 'react';
import MediaPreview from './MediaPreview';
import { mediaUrl } from '../services/mediaService';

const PRIVACY_OPTIONS = [
  { value: 'PRIVATE', label: 'Private' },
  { value: 'FRIENDS_ONLY', label: 'Friends Only' },
  { value: 'PUBLIC', label: 'Public' },
];

export default function EditPinForm({ pin, onSave, onCancel, onPrivacyChange, onRemoveMedia }) {

  // Pre-fill controlled inputs with existing pin data
  const [locationName, setLocationName] = useState(pin.locationName || '');
  const [country, setCountry] = useState(pin.country || '');
  const [region, setRegion] = useState(pin.region || '');
  const [visitDate, setVisitDate] = useState(pin.visitDate || '');
  const [notes, setNotes] = useState(pin.notes || '');
  const [privacyLevel, setPrivacyLevel] = useState(pin.privacyLevel || 'PRIVATE');
  const [mediaFile, setMediaFile] = useState(null);

  const handleSave = () => {
    if (!locationName.trim()) return;
    // Only pass the fields the user can edit — App.js sends these to updatePin()
    // Backend PinService applies a partial update, preserving all other fields
    onSave({ locationName, country, region, visitDate, notes, mediaFile });
  };

  // FR11 — privacy change is immediate, not bundled with Save
  // Updates local display state and delegates upward to App.js
  const handlePrivacyChange = (e) => {
    const newPrivacy = e.target.value;
    setPrivacyLevel(newPrivacy);
    onPrivacyChange(newPrivacy);
  };

  const inputStyle = { width: '100%', padding: '8px', marginBottom: '8px', boxSizing: 'border-box' };

  return (
    <div style={{
      position: 'absolute', top: 20, left: '50%',
      transform: 'translateX(-50%)',
      background: 'white', padding: '20px',
      borderRadius: '8px', zIndex: 1000,
      boxShadow: '0 2px 10px rgba(0,0,0,0.3)',
      minWidth: '280px'
    }}>
      <h3 style={{ margin: '0 0 12px' }}>Edit Pin</h3>

      {/* Show pin name as subtitle so user knows which pin they are editing */}
      <p style={{ fontSize: '12px', color: '#666', margin: '0 0 12px' }}>
        Editing: <strong>{pin.locationName}</strong>
      </p>

      {/* FR2 inputs — pre-filled with existing pin data */}
      <input
        value={locationName}
        onChange={e => setLocationName(e.target.value)}
        placeholder="Location name"
        style={inputStyle}
      />
      <input
        value={country}
        onChange={e => setCountry(e.target.value)}
        placeholder="Country"
        style={inputStyle}
      />
      <input
        value={region}
        onChange={e => setRegion(e.target.value)}
        placeholder="Region / State / Province"
        style={inputStyle}
      />
      <input
        value={visitDate}
        onChange={e => setVisitDate(e.target.value)}
        type="date"
        style={inputStyle}
      />
      <textarea
        value={notes}
        onChange={e => setNotes(e.target.value)}
        placeholder="Notes"
        rows={3}
        style={{ ...inputStyle, marginBottom: '8px', resize: 'vertical' }}
      />

      {/* FR11 — privacy dropdown, triggers immediately on change per SRS */}
      <select
        value={privacyLevel}
        onChange={handlePrivacyChange}
        style={{ ...inputStyle, marginBottom: '12px' }}
      >
        {PRIVACY_OPTIONS.map(opt => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>

      {/* FR7 — media display, replace, and remove */}
      <label style={{ display: 'block', marginBottom: '4px', fontSize: '13px', color: '#555' }}>
        Media
      </label>

      {/* Show current server-side media if one exists AND user hasn't picked a replacement */}
      {!mediaFile && mediaUrl(pin) && (
        <div style={{ marginBottom: '8px' }}>
          <MediaPreview src={mediaUrl(pin)} />
          {onRemoveMedia && (
            <button
              type="button"
              onClick={() => onRemoveMedia()}
              style={{
                marginTop: '4px',
                padding: '4px 8px',
                background: '#e53e3e',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '12px',
                cursor: 'pointer',
              }}
            >
              Remove media
            </button>
          )}
        </div>
      )}

      {/* File picker — always available, lets user replace or add */}
      <input
        type="file"
        accept="image/*,video/*,audio/*"
        onChange={(e) => setMediaFile(e.target.files[0] || null)}
        style={{ marginBottom: '8px' }}
      />

      {/* Local file preview — shown when user has picked a file (replacing or adding) */}
      {mediaFile && (
        <div style={{ marginBottom: '8px' }}>
          <MediaPreview file={mediaFile} />
          <button
            type="button"
            onClick={() => setMediaFile(null)}
            style={{
              marginTop: '4px',
              padding: '4px 8px',
              background: '#718096',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer',
            }}
          >
            Remove file
          </button>
        </div>
      )}

      {/* Buttons delegate to Controller handlers — View never saves data itself */}
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          onClick={handleSave}
          style={{ flex: 1, padding: '8px', background: '#3182ce', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Save Changes
        </button>
        <button
          onClick={onCancel}
          style={{ flex: 1, padding: '8px', background: '#eee', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
