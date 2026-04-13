// --- VIEW: MAP COMPONENT ------------------------------------------------------
// Pure View responsibility — renders the map, tiles, and all pin markers.
// Equivalent to View.displayMap() and displayPins() from the UML.
// The View only displays — it never modifies data.
// In the full architecture, the pins prop is populated from a fetch() call
// to the Java backend (GET /api/pins) rather than local state.

import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';

// Pure View event listener — listens for user interaction on the map
// and delegates the event upward to the Controller. Makes no decisions.
function PinPlacer({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng);
    }
  });
  return null;
}

export default function MapView({ pins, onMapClick, onDeletePin, onEditPin }) {
  return (
    <MapContainer
      center={[20, 0]}
      zoom={2}
      style={{ height: '100vh', width: '100%' }}
    >
      {/* TileLayer fetches map tiles from OpenStreetMap
          In full architecture configured inside mapService.js */}
      <TileLayer
        attribution='&copy; OpenStreetMap contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Event listener — delegates map click to Controller */}
      <PinPlacer onMapClick={onMapClick} />

      {/* Pin markers — equivalent to View.displayPins(pinList) from UML.
          In full architecture pins comes from fetch() result, not local state. */}
      {pins.map(pin => (
        <Marker key={pin.id} position={[pin.latitude, pin.longitude]}>
          <Popup>
            <strong>{pin.locationName}</strong><br />
            {pin.visitDate}<br /><br />
            <div style={{ display: 'flex', gap: '6px' }}>
              {/* FR2 — edit button delegates upward to Controller, never acts directly */}
              <button
                onClick={() => onEditPin(pin)}
                style={{
                  flex: 1, padding: '6px',
                  background: '#3182ce', color: 'white',
                  border: 'none', borderRadius: '4px', cursor: 'pointer'
                }}
              >
                Edit
              </button>
              {/* FR3 — delete button delegates upward to Controller, never acts directly */}
              <button
                onClick={() => onDeletePin(pin)}
                style={{
                  flex: 1, padding: '6px',
                  background: '#e53e3e', color: 'white',
                  border: 'none', borderRadius: '4px', cursor: 'pointer'
                }}
              >
                Delete
              </button>
            </div>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
