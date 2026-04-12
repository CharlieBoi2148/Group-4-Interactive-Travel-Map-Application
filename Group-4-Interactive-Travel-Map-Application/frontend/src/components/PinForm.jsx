// --- VIEW: PIN FORM COMPONENT -------------------------------------------------
// Pure View responsibility — captures FR1 inputs (location name, visit date)
// and delegates Save/Cancel actions to the Controller layer (App.js) via props.
// The View never saves data itself — it only reports what the user did.

import { useState } from 'react';

export default function PinForm({ latlng, onSave, onCancel }) {

  // Controlled inputs — React owns the form state, not the DOM.
  // Using document.getElementById() is a DOM hack that breaks in React.
  const [locationName, setLocationName] = useState('');
  const [visitDate, setVisitDate] = useState('');

  const handleSave = () => {
    if (!locationName.trim()) return;
    onSave({ locationName, visitDate });
  };

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
        placeholder="Location name"
        style={{ width: '100%', padding: '8px', marginBottom: '8px', boxSizing: 'border-box' }}
      />
      <input
        value={visitDate}
        onChange={e => setVisitDate(e.target.value)}
        type="date"
        style={{ width: '100%', padding: '8px', marginBottom: '12px', boxSizing: 'border-box' }}
      />

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