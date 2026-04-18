// --- APP (AppController equivalent) -------------------------------------------
// Top-level wiring file only — imports and composes all sub-components.
// Acts as the Controller layer on the frontend:
//   - Holds UI state (pins, trips, forms)
//   - Receives View events and routes them to the appropriate service
//   - Passes data and handlers down to View components as props
// Contains no UI of its own.

import { useState, useEffect } from 'react';
import 'leaflet/dist/leaflet.css';
import './services/mapService';
import MapView from './components/MapView';
import PinForm from './components/PinForm';
import TripList from './components/TripList';
import Timeline from './components/Timeline';
import TripForm from './components/TripForm';
import { createPin, getPins } from './services/pinService';
import { createTrip, getTrips } from './services/tripService';

function App() {
  // pins/trips -> fetched from Java backend on mount (FR4, FR15)
  // form/showTripForm -> pure UI state, stays in React
  const [pins, setPins] = useState([]);
  const [trips, setTrips] = useState([]);
  const [form, setForm] = useState(null);
  const [showTripForm, setShowTripForm] = useState(false);
  const [tripSaveError, setTripSaveError] = useState(null);

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

  // Receives click from MapView, opens the form
  const handleMapClick = (latlng) => {
    setForm(latlng);
  };

  // Receives save from PinForm, delegates to pinService, updates state.
  // async/await so it works the same whether pinService is local or fetch().
  const handleSavePin = async ({ locationName, visitDate, tripId }) => {
    if (!form) return;
    const pin = await createPin({ lat: form.lat, lng: form.lng, locationName, visitDate, tripId });
    console.log('Pin returned from backend:', pin);
    setPins((prev) => [...prev, pin]);
    setForm(null);
  };

  // Pure UI cancel — closes form, no backend involvement
  const handleCancel = () => {
    setForm(null);
  };

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

  return (
    <div style={{ position: 'relative', height: '100vh', display: 'flex' }}>
      <div style={{ flex: 1, position: 'relative', minWidth: 0 }}>
        {form && (
          <PinForm
            latlng={form}
            onSave={handleSavePin}
            onCancel={handleCancel}
            trips={trips}
          />
        )}
        <MapView
          pins={pins}
          trips={trips}
          onMapClick={handleMapClick}
        />
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
        <TripList trips={trips} pins={pins} />
        <Timeline pins={pins} trips={trips} />

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
