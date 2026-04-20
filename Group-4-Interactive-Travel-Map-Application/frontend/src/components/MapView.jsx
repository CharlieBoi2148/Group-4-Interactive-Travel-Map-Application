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

export default function MapView({ pins, trips = [], onMapClick }) {
  const tripNameById = new Map(
    trips
      .filter((trip) => trip.id != null)
      .map((trip) => [Number(trip.id), trip.name])
  );

  return (
    <MapContainer
      center={[20, 0]}
      zoom={2}
      style={{ height: '100%', width: '100%', minHeight: 0 }}
    >
      {/* TileLayer fetches map tiles from OpenStreetMap
          In full architecture configured inside mapService.js */}
      <TileLayer
        attribution='&copy; OpenStreetMap contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Event listener — delegates map click to Controller */}
      <PinPlacer onMapClick={onMapClick} />

      {/* Pin markers — use latitude/longitude, with lat/lng fallback (pinService). */}
      {pins
        .map((pin, index) => {
          const lat = Number(pin.latitude ?? pin.lat);
          const lng = Number(pin.longitude ?? pin.lng);
          return { pin, lat, lng, index, ok: Number.isFinite(lat) && Number.isFinite(lng) };
        })
        .filter((x) => x.ok)
        .map(({ pin, lat, lng, index }) => (
          <Marker
            key={pin.id != null ? String(pin.id) : `pin-${index}`}
            position={[lat, lng]}
          >
            <Popup>
              <strong>{pin.locationName}</strong><br />
              {pin.visitDate}
              {pin.tripId != null ? (
                <>
                  <br />
                  <span style={{ color: '#555' }}>
                    Trip: {tripNameById.get(Number(pin.tripId)) || `#${pin.tripId}`}
                  </span>
                </>
              ) : null}
            </Popup>
          </Marker>
        ))}
    </MapContainer>
  );
}
