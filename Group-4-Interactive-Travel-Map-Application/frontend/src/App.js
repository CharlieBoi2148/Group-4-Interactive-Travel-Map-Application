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

import LoginForm from './components/LoginForm';
import MapView from './components/MapView';
import PinForm from './components/PinForm';
import TripList from './components/TripList';
import Timeline from './components/Timeline';
import TripForm from './components/TripForm';
import EditPinForm from './components/EditPinForm';
import DistancePanel from './components/DistancePanel';

import { createPin, getPins, deletePin, updatePin, setPinPrivacy } from './services/pinService';
import { uploadMedia, deleteMedia } from './services/mediaService';
import { createTrip, getTrips, setTripPrivacy } from './services/tripService';
import { getPinToPin, getTripDistance } from './services/mapDistanceService';

function App() {
  // pins → fetched from Java backend via GET /api/pins on mount (FR4, FR15)
  // form/showTripForm -> pure UI state, stays in React
  // editPin → holds the pin currently being edited, null if no edit open
  // confirmDelete → holds the pin pending deletion, null if no dialog open

  const [pins, setPins] = useState([]);
  const [trips, setTrips] = useState([]);
  const [form, setForm] = useState(null);

const [isLoggedIn, setIsLoggedIn] = useState(process.env.NODE_ENV === 'test');
const [editPin, setEditPin] = useState(null);
const [confirmDelete, setConfirmDelete] = useState(null);
const [showTripForm, setShowTripForm] = useState(false);
const [tripSaveError, setTripSaveError] = useState(null);
const [tripPrivacyError, setTripPrivacyError] = useState(null);
const [mainView, setMainView] = useState('map');

const [measuringFrom, setMeasuringFrom] = useState(null);
const [measuredTo, setMeasuredTo] = useState(null);
const [distanceResult, setDistanceResult] = useState(null);
const [distanceError, setDistanceError] = useState(null);
const [distanceLoading, setDistanceLoading] = useState(false);
const [tripDistances, setTripDistances] = useState({});

  // FR4, FR15 — load all pins from backend when the app first mounts.
  // This is what makes pins persist across page refreshes — on every load
  // React fetches all saved pins from H2 via GET /api/pins and renders
  // them as markers. Without this, pins only exist in local state and
  // disappear on refresh.
  useEffect(() => {
    getPins()
      .then((data) => {
        console.log('Loaded pins from backend:', data);
        setPins(data);
      })
      .catch((err) => {
        console.error('Could not load pins from backend:', err);
      });
  }, []); // empty array -> runs once on mount only

  // FR4, FR15 — load trips on mount so dashboard can render saved trips.
  useEffect(() => {
    getTrips()
      .then((data) => {
        console.log('Loaded trips from backend:', data);
        setTrips(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        console.error('Could not load trips from backend:', err);
      });
  }, []);

  // FR8 — fetch trip distance for each trip when trips list changes.
  // Silent catch: distance is supplementary info, not critical to the UI.
  useEffect(() => {
    if (trips.length === 0) return;
    trips.forEach((trip) => {
      if (trip.id == null) return;
      getTripDistance(trip.id, 'km')
        .then((result) => {
          setTripDistances((prev) => ({ ...prev, [Number(trip.id)]: result }));
        })
        .catch(() => {
          // Silently ignore — distance is supplementary info, not critical
        });
    });
  }, [trips, pins]);

  // Receives click from MapView, opens the create form.
  // Closes any open edit form first — prevents both forms rendering simultaneously.
  const handleMapClick = (latlng) => {
    setEditPin(null);
    setForm(latlng);
  };

  // Receives save from PinForm, delegates to pinService, updates state.
  // async/await so it works the same whether pinService is local or fetch().
  const handleSavePin = async ({ locationName, country, region, visitDate, notes, tripId, mediaFile }) => {
    if (!form) return;
    let pin = await createPin({
      lat: form.lat,
      lng: form.lng,
      locationName, country, region, visitDate, tripId, notes
    });
    if (mediaFile) {
      try {
        pin = await uploadMedia(pin.id, mediaFile);
      } catch (err) {
        console.error('Pin saved, but media upload failed:', err);
        // Keep the pin in state without media — user can retry from edit
      }
    }
    console.log('Pin returned from backend:', pin);
    setPins((prev) => [...prev, pin]);
    setForm(null);
  };

  // Pure UI cancel — closes form, no backend involvement

  const handleCancel = () => {
    setForm(null);
  };

  const handleLogout = () => {
  setIsLoggedIn(false);
};
  
if (!isLoggedIn) {
  return <LoginForm onLoginSuccess={() => setIsLoggedIn(true)} />;
}

  // Receives save from TripForm, delegates to tripService, updates state.
  const handleSaveTrip = async (payload) => {
    setTripSaveError(null);
    try {
      const saved = await createTrip(payload);
      console.log('Trip returned from backend:', saved);
      setTrips((prev) => [...prev, saved]);
      setShowTripForm(false);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Could not save trip. Is backend running on :8080?';
      setTripSaveError(message);
      console.error('Could not save trip:', err);
    }
  };

  const handleTripPrivacyChange = async (tripId, privacyLevel) => {
    setTripPrivacyError(null);
    try {
      const updated = await setTripPrivacy(tripId, privacyLevel);
      setTrips((prev) =>
        prev.map((trip) => (trip.id === updated.id ? { ...trip, privacyLevel: updated.privacyLevel } : trip))
      );
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Could not update trip privacy. Is backend running on :8080?';
      setTripPrivacyError(message);
      console.error('Could not update trip privacy:', err);
    }
  };
  
  // FR2 — step 1: MapView calls this when user clicks Edit on a pin.
  // Opens the edit form by storing the pin in editPin state.
  const handleEditPin = (pin) => {
    setEditPin(pin);
  };

  // FR2 — step 2: User saved changes in EditPinForm.
  // Only sends the fields the user can edit — backend preserves all other fields.
  // Updates the pin in local state so the map reflects changes immediately.
  const handleUpdatePin = async ({ locationName, country, region, visitDate, notes, mediaFile }) => {
    if (!editPin) return;
    try {
      let updated = await updatePin(editPin.id, { locationName, country, region, visitDate, notes });
      if (mediaFile) {
        try {
          updated = await uploadMedia(updated.id, mediaFile);
        } catch (err) {
          console.error('Pin updated, but media upload failed:', err);
        }
      }
      setPins(prev => prev.map(p => p.id === updated.id ? updated : p));
    } catch (err) {
      console.error('Failed to update pin:', err);
    } finally {
      setEditPin(null);
    }
  };

  // FR7 — remove a pin's media file and clear its mediaUrl in local state.
  // Updates both the pins list and editPin so the edit form refreshes immediately.
  const handleRemoveMedia = async () => {
    if (!editPin) return;
    try {
      await deleteMedia(editPin.id);
      const cleared = { ...editPin, mediaUrl: null };
      setPins(prev => prev.map(p => p.id === cleared.id ? cleared : p));
      setEditPin(cleared);
    } catch (err) {
      console.error('Failed to remove media:', err);
      // editPin state unchanged — UI still shows the media so user can retry
    }
  };

  // FR2 — user cancelled edit, no backend involvement
  const handleCancelEdit = () => {
    setEditPin(null);
  };

  // FR11 — triggered immediately when user changes privacy dropdown in EditPinForm.
  // Does not wait for Save — changes apply immediately per SRS FR11.
  // Updates both pins array and editPin so state stays fully in sync.
  const handlePrivacyChange = async (privacyLevel) => {
    if (!editPin) return;
    try {
      const updated = await setPinPrivacy(editPin.id, privacyLevel);
      setPins(prev => prev.map(p => p.id === updated.id ? updated : p));
      setEditPin(updated);
    } catch (err) {
      console.error('Failed to update privacy:', err);
    }
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

  // FR8 — pin-to-pin distance measurement.
  // First click sets pin A (measuringFrom). Second click on a different pin
  // triggers the fetch and shows the result in DistancePanel.
  const handleMeasurePin = async (pin) => {
    // Cancel if user clicks the same pin twice
    if (measuringFrom && measuringFrom.id === pin.id) {
      setMeasuringFrom(null);
      setDistanceResult(null);
      setDistanceError(null);
      return;
    }

    // First click — set pin A, wait for pin B
    if (!measuringFrom) {
      setDistanceResult(null);
      setDistanceError(null);
      setMeasuringFrom(pin);
      return;
    }

    // Second click — fetch distance between pin A and pin B
    setMeasuredTo(pin);
    setDistanceLoading(true);
    setDistanceError(null);
    try {
      const result = await getPinToPin(
        measuringFrom.latitude,
        measuringFrom.longitude,
        pin.latitude,
        pin.longitude,
        'km'
      );
      setDistanceResult(result);
    } catch (err) {
      setDistanceError(err.message || 'Could not calculate distance.');
    } finally {
      setDistanceLoading(false);
    }
  };

  // Cancel measuring mode — clears all distance state
  const handleCancelMeasure = () => {
    setMeasuringFrom(null);
    setMeasuredTo(null);
    setDistanceResult(null);
    setDistanceError(null);
    setDistanceLoading(false);
  };
  
  return (
      
    <div style={{ position: 'relative', height: '100vh', display: 'flex' }}>

          <button
      onClick={handleLogout}
      style={{
        position: 'absolute',
        top: '10px',
        right: '10px',
        zIndex: 3000,
        padding: '8px 12px',
        background: '#e53e3e',
        color: 'white',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer'
      }}
    >
      Logout
    </button>


      <div
        style={{
          flex: 1,
          minWidth: 0,
          display: 'flex',
          flexDirection: 'column',
          minHeight: 0,
        }}
      >
        <div
          role="tablist"
          aria-label="Main view"
          style={{
            display: 'flex',
            gap: '8px',
            padding: '8px 12px',
            background: '#fff',
            borderBottom: '1px solid #ddd',
            flexShrink: 0,
          }}
        >
          <button
            type="button"
            role="tab"
            aria-selected={mainView === 'map'}
            data-testid="main-view-map"
            onClick={() => setMainView('map')}
            style={{
              padding: '8px 16px',
              border: '1px solid #ccc',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600,
              background: mainView === 'map' ? '#1D9E75' : '#f5f5f5',
              color: mainView === 'map' ? '#fff' : '#333',
            }}
          >
            Map
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mainView === 'timeline'}
            data-testid="main-view-timeline"
            onClick={() => {
              setForm(null);
              setMainView('timeline');
            }}
            style={{
              padding: '8px 16px',
              border: '1px solid #ccc',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600,
              background: mainView === 'timeline' ? '#1D9E75' : '#f5f5f5',
              color: mainView === 'timeline' ? '#fff' : '#333',
            }}
          >
            Timeline
          </button>
        </div>

        <div style={{ flex: 1, position: 'relative', minHeight: 0, overflow: 'hidden' }}>
          {mainView === 'map' ? (
            <div
              style={{
                position: 'absolute',
                inset: 0,
                minHeight: 0,
              }}
            >
              {confirmDelete && (
                <div
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    background: 'rgba(0,0,0,0.5)',
                    zIndex: 2000,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <div
                    style={{
                      background: 'white',
                      padding: '24px',
                      borderRadius: '8px',
                      minWidth: '280px',
                      boxShadow: '0 2px 10px rgba(0,0,0,0.3)',
                    }}
                  >
                    <h3 style={{ margin: '0 0 12px' }}>Delete Pin</h3>
                    <p style={{ margin: '0 0 20px', color: '#555' }}>
                      Are you sure you want to delete <strong>{confirmDelete.locationName}</strong>? This cannot be
                      undone.
                    </p>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        onClick={handleConfirmDelete}
                        style={{
                          flex: 1,
                          padding: '8px',
                          background: '#e53e3e',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                        }}
                      >
                        Delete
                      </button>
                      <button
                        onClick={handleCancelDelete}
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
                </div>
              )}

              {editPin && (
                <EditPinForm
                  pin={editPin}
                  onSave={handleUpdatePin}
                  onCancel={handleCancelEdit}
                  onPrivacyChange={handlePrivacyChange}
                  onRemoveMedia={handleRemoveMedia}
                />
              )}

              {form && (
                <PinForm
                  latlng={form}
                  onSave={handleSavePin}
                  onCancel={handleCancel}
                  trips={trips}
                />
              )}
              <DistancePanel
                measuringFrom={measuringFrom}
                measuredTo={measuredTo}
                result={distanceResult}
                error={distanceError}
                loading={distanceLoading}
                onCancel={handleCancelMeasure}
              />
              <MapView
                pins={pins}
                trips={trips}
                onMapClick={handleMapClick}
                onDeletePin={handleDeletePin}
                onEditPin={handleEditPin}
                onMeasurePin={handleMeasurePin}
                measuringFrom={measuringFrom}
              />
            </div>
          ) : (
            <div
              style={{
                height: '100%',
                padding: '12px 16px',
                boxSizing: 'border-box',
                background: '#fff',
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column',
                minHeight: 0,
              }}
            >
              <Timeline pins={pins} trips={trips} variant="main" />
            </div>
          )}
        </div>
      </div>

      <aside
        data-testid="trips-sidebar"
        style={{
          width: 320,
          flexShrink: 0,
          borderLeft: '1px solid #ddd',
          background: '#fafafa',
          padding: '12px',
          overflowY: 'auto',
          boxSizing: 'border-box',
        }}
      >
        <TripList trips={trips} pins={pins} onTripPrivacyChange={handleTripPrivacyChange} tripDistances={tripDistances} />
        {tripPrivacyError ? (
          <p
            role="alert"
            style={{
              color: '#b00020',
              fontSize: '13px',
              margin: '0 0 8px',
              padding: '8px',
              background: '#ffebee',
              borderRadius: '6px',
            }}
          >
            {tripPrivacyError}
          </p>
        ) : null}

        {!showTripForm ? (
          <button
            type="button"
            onClick={() => setShowTripForm(true)}
            style={{
              width: '100%',
              padding: '10px',
              background: '#1D9E75',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            New trip
          </button>
        ) : (
          <>
            {tripSaveError ? (
              <p
                role="alert"
                style={{
                  color: '#b00020',
                  fontSize: '13px',
                  margin: '0 0 8px',
                  padding: '8px',
                  background: '#ffebee',
                  borderRadius: '6px',
                }}
              >
                {tripSaveError}
              </p>
            ) : null}
            <TripForm
              onSave={handleSaveTrip}
              onCancel={() => {
                setTripSaveError(null);
                setShowTripForm(false);
              }}
            />
          </>
        )}
      </aside>
    </div>
  );
}

export default App;
