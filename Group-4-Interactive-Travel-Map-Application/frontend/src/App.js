// --- APP (AppController equivalent) -------------------------------------------
// Top-level wiring file only — imports and composes all sub-components.
// Acts as the Controller layer on the frontend:
//   - Holds UI state (pins, form)
//   - Receives View events and routes them to the appropriate service
//   - Passes data and handlers down to View components as props
// Contains no UI of its own.

import { useState, useEffect } from 'react';
import 'leaflet/dist/leaflet.css';
import './services/mapService';
import MapView from './components/MapView';
import PinForm from './components/PinForm';
import LoginForm from './components/LoginForm';
import { createPin, getPins } from './services/pinService';

function App() {

  // pins → fetched from Java backend via GET /api/pins on mount (FR4, FR15)
  // form → pure UI state, stays in React
  const [pins, setPins] = useState([]);
  const [form, setForm] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

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

  // Receives click from MapView, opens the form
  const handleMapClick = (latlng) => {
    setForm(latlng);
  };

  // Receives save from PinForm, delegates to pinService, updates state.
  // async/await so it works the same whether pinService is local or fetch().
  const handleSavePin = async ({ locationName, visitDate }) => {
    if (!form) return;
    const pin = await createPin({ lat: form.lat, lng: form.lng, locationName, visitDate });
    console.log('Pin returned from backend:', pin);
    setPins(prev => [...prev, pin]);
    setForm(null);
  };

  // Pure UI cancel — closes form, no backend involvement
  const handleCancel = () => {
    setForm(null);
  };
  
if (!isLoggedIn) {
  return <LoginForm onLoginSuccess={() => setIsLoggedIn(true)} />;
}

  return (
    <div style={{ position: 'relative', height: '100vh' }}>
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
      />
    </div>
  );
}

export default App;