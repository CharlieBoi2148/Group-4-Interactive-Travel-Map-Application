// --- APP (AppController equivalent) -------------------------------------------
// Top-level wiring file only — imports and composes all sub-components.
// Acts as the Controller layer on the frontend:
//   - Holds UI state (pins, form)
//   - Receives View events and routes them to the appropriate service
//   - Passes data and handlers down to View components as props
// Contains no UI of its own.

import { useState } from 'react';
import 'leaflet/dist/leaflet.css';
import './services/mapService';
import MapView from './components/MapView';
import PinForm from './components/PinForm';
import { createPin } from './services/pinService';

function App() {

  // pins → in full architecture fetched from Java backend via GET /api/pins
  // form → pure UI state, stays in React
  const [pins, setPins] = useState([]);
  const [form, setForm] = useState(null);

  // Receives click from MapView, opens the form
  const handleMapClick = (latlng) => {
    setForm(latlng);
  };

  // Receives save from PinForm, delegates to pinService, updates state.
  // async/await so it works the same whether pinService is local or fetch().
  const handleSavePin = async ({ locationName, visitDate }) => {
    if (!form) return;
    const pin = await createPin({ lat: form.lat, lng: form.lng, locationName, visitDate });
    setPins(prev => [...prev, pin]);
    setForm(null);
  };

  // Pure UI cancel — closes form, no backend involvement
  const handleCancel = () => {
    setForm(null);
  };

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