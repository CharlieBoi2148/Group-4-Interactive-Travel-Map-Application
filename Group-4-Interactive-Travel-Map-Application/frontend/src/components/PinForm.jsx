// --- VIEW: PIN FORM COMPONENT -------------------------------------------------
// Pure View responsibility — captures FR1 inputs (location name, country,
// region, visit date, notes) and delegates Save/Cancel actions to the
// Controller layer (App.js) via props.
// The View never saves data itself — it only reports what the user did.

import { useState } from 'react';
import MediaPreview from './MediaPreview';

export default function PinForm({ latlng, onSave, onCancel, trips = [] }) {

  // Controlled inputs — React owns the form state, not the DOM.
  // Using document.getElementById() is a DOM hack that breaks in React.
  const [locationName, setLocationName] = useState('');
  const [country, setCountry] = useState('');
  const [region, setRegion] = useState('');
  const [visitDate, setVisitDate] = useState('');
  const [tripId, setTripId] = useState('');
  const [notes, setNotes] = useState('');
  const [mediaFile, setMediaFile] = useState(null);

  const handleSave = () => {
    if (!locationName.trim()) return;
    onSave({
      locationName,
      country,
      region,
      visitDate,
      tripId: tripId === '' ? null : Number(tripId),
      notes,
      mediaFile,
    });
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
      <h3 style={{ margin: '0 0 12px' }}>New Travel Pin</h3>

      {/* Coordinates come from the Model (clicked position) */}
      <p style={{ fontSize: '12px', color: '#666', margin: '0 0 12px' }}>
        {latlng.lat.toFixed(4)}, {latlng.lng.toFixed(4)}
      </p>

      {/* FR1 inputs — controlled via React state, not DOM refs */}
      <input
        value={locationName}
        onChange={e => setLocationName(e.target.value)}
        placeholder="Location name *"
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
      <select
        value={tripId}
        onChange={(e) => setTripId(e.target.value)}
        style={{ width: '100%', padding: '8px', marginBottom: '12px', boxSizing: 'border-box' }}
      >
        <option value="">No trip (unassigned)</option>
        {trips.map((trip) => (
          <option key={trip.id} value={trip.id}>
            {trip.name}
          </option>
        ))}
      </select>
      <textarea
        value={notes}
        onChange={e => setNotes(e.target.value)}
        placeholder="Notes (optional)"
        rows={3}
        style={{ ...inputStyle, marginBottom: '12px', resize: 'vertical' }}
      />


      {/* FR7 — optional media file input */}
      <label style={{ display: 'block', marginBottom: '4px', fontSize: '13px', color: '#555' }}>
        Media (optional)
      </label>
      <input
        type="file"
        accept="image/*,video/*,audio/*"
        onChange={(e) => setMediaFile(e.target.files[0] || null)}
        style={{ marginBottom: '8px' }}
      />
      {mediaFile && (
        <div style={{ marginBottom: '8px' }}>
          <MediaPreview file={mediaFile} />
          <button
            type="button"
            onClick={() => setMediaFile(null)}
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
            Remove file
          </button>
        </div>
      )}

      {/* Buttons delegate to Controller handlers — View never saves data itself */}
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          onClick={handleSave}
          style={{ flex: 1, padding: '8px', background: '#1D9E75', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Save Pin
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
