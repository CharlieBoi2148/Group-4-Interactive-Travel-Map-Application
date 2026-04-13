// --- VIEW: EDIT PIN FORM COMPONENT -------------------------------------------
// Pure View responsibility — captures FR2 inputs for editing an existing pin
// and delegates Save/Cancel actions to the Controller layer (App.js) via props.
// The View never saves data itself — it only reports what the user did.
//
// Receives the full pin object as a prop so inputs can be pre-filled with
// existing values. The user only sees and edits text fields — coordinates,
// id, and privacyLevel are never exposed here.

import { useState } from 'react';

export default function EditPinForm({ pin, onSave, onCancel }) {

  // Pre-fill controlled inputs with existing pin data
  const [locationName, setLocationName] = useState(pin.locationName || '');
  const [visitDate, setVisitDate] = useState(pin.visitDate || '');
  const [notes, setNotes] = useState(pin.notes || '');

  const handleSave = () => {
    if (!locationName.trim()) return;
    // Only pass the fields the user can edit — App.js sends these to updatePin()
    // Backend PinService applies a partial update, preserving all other fields
    onSave({ locationName, visitDate, notes });
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
        style={{ width: '100%', padding: '8px', marginBottom: '8px', boxSizing: 'border-box' }}
      />
      <input
        value={visitDate}
        onChange={e => setVisitDate(e.target.value)}
        type="date"
        style={{ width: '100%', padding: '8px', marginBottom: '8px', boxSizing: 'border-box' }}
      />
      <textarea
        value={notes}
        onChange={e => setNotes(e.target.value)}
        placeholder="Notes"
        rows={3}
        style={{ width: '100%', padding: '8px', marginBottom: '12px', boxSizing: 'border-box', resize: 'vertical' }}
      />

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