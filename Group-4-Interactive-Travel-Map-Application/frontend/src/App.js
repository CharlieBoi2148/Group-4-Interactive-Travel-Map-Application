// --- APP (AppController equivalent) -------------------------------------------
// Top-level wiring file only — imports and composes all sub-components.
// Acts as the Controller layer on the frontend:
//   - Holds UI state (pins, form, editPin, confirmDelete)
//   - Receives View events and routes them to the appropriate service
//   - Passes data and handlers down to View components as props
// Contains no UI of its own.

import { useState, useEffect } from 'react';
import 'leaflet/dist/leaflet.css';
import './services/mapService';
import MapView from './components/MapView';
import PinForm from './components/PinForm';
import EditPinForm from './components/EditPinForm';
import { createPin, getPins, deletePin, updatePin } from './services/pinService';

function App() {

  // pins → fetched from Java backend via GET /api/pins on mount (FR4, FR15)
  // form → pure UI state for new pin creation, stays in React
  // editPin → holds the pin currently being edited, null if no edit open
  // confirmDelete → holds the pin pending deletion, null if no dialog open
  const [pins, setPins] = useState([]);
  const [form, setForm] = useState(null);
  const [editPin, setEditPin] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);

  // FR4, FR15 — load all pins from backend when the app first mounts.
  // This is what makes pins persist across page refreshes — on every load
  // React fetches all saved pins from H2 via GET /api/pins and renders
  // them as markers. Without this, pins only exist in local state and
  // disappear on refresh.
  useEffect(() => {
    getPins()
      .then(data => {
        console.log('Loaded pins from backend:', data);
        setPins(data);
      })
      .catch(err => {
        console.error('Could not load pins from backend:', err);
      });
  }, []); // empty array — runs once on mount only

  // Receives click from MapView, opens the create form.
  // Closes any open edit form first — prevents both forms rendering simultaneously.
  const handleMapClick = (latlng) => {
    setEditPin(null);
    setForm(latlng);
  };

  // Receives save from PinForm, delegates to pinService, updates state.
  const handleSavePin = async ({ locationName, visitDate }) => {
    if (!form) return;
    const pin = await createPin({ lat: form.lat, lng: form.lng, locationName, visitDate });
    console.log('Pin returned from backend:', pin);
    setPins(prev => [...prev, pin]);
    setForm(null);
  };

  // Pure UI cancel for create form — closes form, no backend involvement
  const handleCancel = () => {
    setForm(null);
  };

  // FR2 — step 1: MapView calls this when user clicks Edit on a pin.
  // Opens the edit form by storing the pin in editPin state.
  const handleEditPin = (pin) => {
    setEditPin(pin);
  };

  // FR2 — step 2: User saved changes in EditPinForm.
  // Only sends the fields the user can edit — backend preserves all other fields.
  // Updates the pin in local state so the map reflects changes immediately.
  const handleUpdatePin = async ({ locationName, visitDate, notes }) => {
    if (!editPin) return;
    try {
      const updated = await updatePin(editPin.id, { locationName, visitDate, notes });
      setPins(prev => prev.map(p => p.id === updated.id ? updated : p));
    } catch (err) {
      console.error('Failed to update pin:', err);
    } finally {
      setEditPin(null);
    }
  };

  // FR2 — user cancelled edit, no backend involvement
  const handleCancelEdit = () => {
    setEditPin(null);
  };

  // FR3 — step 1: MapView calls this when user clicks Delete.
  // Opens the confirmation dialog by storing the pin in confirmDelete state.
  // No backend call yet — SRS requires confirmation before deletion.
  const handleDeletePin = (pin) => {
    setConfirmDelete(pin);
  };

  // FR3 — step 2: User confirmed deletion.
  // Delegates to pinService, removes pin from local state on success.
  const handleConfirmDelete = async () => {
    if (!confirmDelete) return;
    try {
      await deletePin(confirmDelete.id);
      setPins(prev => prev.filter(p => p.id !== confirmDelete.id));
    } catch (err) {
      console.error('Failed to delete pin:', err);
    } finally {
      setConfirmDelete(null);
    }
  };

  // FR3 — user cancelled deletion dialog
  const handleCancelDelete = () => {
    setConfirmDelete(null);
  };

  return (
    <div style={{ position: 'relative', height: '100vh' }}>

      {/* FR3 — confirmation dialog per SRS requirement */}
      {confirmDelete && (
        <div style={{
          position: 'absolute', top: 0, left: 0,
          width: '100%', height: '100%',
          background: 'rgba(0,0,0,0.5)',
          zIndex: 2000, display: 'flex',
          alignItems: 'center', justifyContent: 'center'
        }}>
          <div style={{
            background: 'white', padding: '24px',
            borderRadius: '8px', minWidth: '280px',
            boxShadow: '0 2px 10px rgba(0,0,0,0.3)'
          }}>
            <h3 style={{ margin: '0 0 12px' }}>Delete Pin</h3>
            <p style={{ margin: '0 0 20px', color: '#555' }}>
              Are you sure you want to delete <strong>{confirmDelete.locationName}</strong>? This cannot be undone.
            </p>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                onClick={handleConfirmDelete}
                style={{ flex: 1, padding: '8px', background: '#e53e3e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
              >
                Delete
              </button>
              <button
                onClick={handleCancelDelete}
                style={{ flex: 1, padding: '8px', background: '#eee', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* FR2 — edit form, only rendered when a pin is selected for editing */}
      {editPin && (
        <EditPinForm
          pin={editPin}
          onSave={handleUpdatePin}
          onCancel={handleCancelEdit}
        />
      )}

      {form && (
        <PinForm
          latlng={form}
          onSave={handleSavePin}
          onCancel={handleCancel}
        />
      )}

      <MapView
        pins={pins}
        onMapClick={handleMapClick}
        onDeletePin={handleDeletePin}
        onEditPin={handleEditPin}
      />
    </div>
  );
}

export default App;
