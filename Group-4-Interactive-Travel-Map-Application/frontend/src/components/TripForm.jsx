// --- VIEW: TRIP FORM (FR5 — Organize Trip: create trip metadata) --------------
// Pure View — collects name, description, dates, privacy; Save/Cancel via props.

import { useState } from 'react';

/**
 * @param {Object} props
 * @param {function(Object): void} props.onSave called with { name, description, startDate, endDate, privacyLevel }
 * @param {function(): void} props.onCancel
 */
export default function TripForm({
  onSave,
  onCancel,
  initialValues = null,
  title = 'New Trip',
  saveLabel = 'Save trip',
}) {
  const [name, setName] = useState(initialValues?.name || '');
  const [description, setDescription] = useState(initialValues?.description || '');
  const [startDate, setStartDate] = useState(initialValues?.startDate || '');
  const [endDate, setEndDate] = useState(initialValues?.endDate || '');
  const [privacyLevel, setPrivacyLevel] = useState(initialValues?.privacyLevel || 'PRIVATE');

  const handleSave = () => {
    if (!name.trim()) return;
    onSave({
      name: name.trim(),
      description: description.trim(),
      startDate: startDate || '',
      endDate: endDate || '',
      privacyLevel,
    });
  };

  return (
    <div
      data-testid="trip-form"
      style={{
        marginTop: '12px',
        padding: '16px',
        background: 'white',
        borderRadius: '8px',
        border: '1px solid #ddd',
        boxShadow: '0 1px 6px rgba(0,0,0,0.08)',
      }}
    >
      <h4 style={{ margin: '0 0 12px', fontSize: '15px' }}>{title}</h4>
      <input
        data-testid="trip-name-input"
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Trip name (required)"
        style={{
          width: '100%',
          padding: '8px',
          marginBottom: '8px',
          boxSizing: 'border-box',
        }}
      />
      <textarea
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Description (optional)"
        rows={2}
        style={{
          width: '100%',
          padding: '8px',
          marginBottom: '8px',
          boxSizing: 'border-box',
          resize: 'vertical',
        }}
      />
      <div style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
        <label style={{ flex: 1, fontSize: '12px', color: '#555' }}>
          Start
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={{ width: '100%', marginTop: '4px', padding: '6px', boxSizing: 'border-box' }}
          />
        </label>
        <label style={{ flex: 1, fontSize: '12px', color: '#555' }}>
          End
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={{ width: '100%', marginTop: '4px', padding: '6px', boxSizing: 'border-box' }}
          />
        </label>
      </div>
      <label style={{ display: 'block', fontSize: '12px', color: '#555', marginBottom: '10px' }}>
        Privacy
        <select
          value={privacyLevel}
          onChange={(e) => setPrivacyLevel(e.target.value)}
          style={{ width: '100%', marginTop: '4px', padding: '8px', boxSizing: 'border-box' }}
        >
          <option value="PRIVATE">Private</option>
          <option value="FRIENDS_ONLY">Friends only</option>
          <option value="PUBLIC">Public</option>
        </select>
      </label>
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          type="button"
          onClick={handleSave}
          style={{
            flex: 1,
            padding: '8px',
            background: '#1D9E75',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          {saveLabel}
        </button>
        <button
          type="button"
          onClick={onCancel}
          style={{
            flex: 1,
            padding: '8px',
            background: '#eee',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
