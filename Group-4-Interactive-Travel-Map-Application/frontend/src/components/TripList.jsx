// --- VIEW: TRIP LIST (FR4 — View Pins and Trips, trip portion) ---------------
// Pure View — renders trips passed from App.js; does not fetch.

/**
 * @param {Object} props
 * @param {Array<Object>} props.trips trips from GET /api/trips
 * @param {Array<Object>} props.pins pins from GET /api/pins
 * @param {function(number,string):void} [props.onTripPrivacyChange] callback for privacy update
 * @param {function(Object):void} [props.onEditTrip] callback to open edit UI
 */
export default function TripList({ trips, pins = [], onTripPrivacyChange, onEditTrip, tripDistances = {} }) {
  const formatDate = (d) => {
    if (!d) return '—';
    return typeof d === 'string' ? d : d;
  };

  const sortByVisitDate = (a, b) => {
    const aDate = a?.visitDate ? new Date(a.visitDate).getTime() : Number.POSITIVE_INFINITY;
    const bDate = b?.visitDate ? new Date(b.visitDate).getTime() : Number.POSITIVE_INFINITY;
    if (aDate !== bDate) return aDate - bDate;
    return String(a?.locationName || '').localeCompare(String(b?.locationName || ''));
  };

  const pinsByTripId = pins.reduce((acc, pin) => {
    if (pin.tripId == null) return acc;
    const key = Number(pin.tripId);
    if (!Number.isFinite(key)) return acc;
    if (!acc[key]) acc[key] = [];
    acc[key].push(pin);
    return acc;
  }, {});

  return (
    <div
      data-testid="trip-list"
      style={{
        marginBottom: '16px',
      }}
    >
      <h3 style={{ margin: '0 0 8px', fontSize: '16px' }}>Your Trips</h3>
      {trips.length === 0 ? (
        <p style={{ margin: 0, fontSize: '13px', color: '#666' }}>
          No trips yet. Use &quot;New Trip&quot; to create one.
        </p>
      ) : (
        <ul
          style={{
            listStyle: 'none',
            padding: 0,
            margin: 0,
            maxHeight: '40vh',
            overflowY: 'auto',
          }}
        >
          {trips.map((t) => (
            <li
              key={t.id != null ? String(t.id) : `trip-${t.name}-${formatDate(t.startDate)}`}
              style={{
                borderBottom: '1px solid #e0e0e0',
                padding: '10px 0',
                fontSize: '14px',
              }}
            >
              <div style={{ fontWeight: 600 }}>{t.name}</div>
              {t.description ? (
                <div style={{ fontSize: '12px', color: '#555', marginTop: '4px' }}>
                  {t.description}
                </div>
              ) : null}
              <div style={{ fontSize: '12px', color: '#888', marginTop: '4px' }}>
                {formatDate(t.startDate)} → {formatDate(t.endDate)}
                {t.privacyLevel ? ` · ${t.privacyLevel}` : ''}
              </div>
              <div style={{ marginTop: '6px' }}>
                <button
                  type="button"
                  onClick={() => {
                    if (typeof onEditTrip === 'function') onEditTrip(t);
                  }}
                  style={{
                    marginLeft: '8px',
                    padding: '4px 8px',
                    fontSize: '12px',
                    borderRadius: '4px',
                    border: '1px solid #ccc',
                    background: '#fff',
                    cursor: 'pointer',
                  }}
                >
                  Edit
                </button>
              </div>
              {(() => {
                const assigned = [...(pinsByTripId[Number(t.id)] || [])].sort(sortByVisitDate);
                if (assigned.length === 0) {
                  return (
                    <div style={{ fontSize: '12px', color: '#555', marginTop: '6px' }}>
                      Pins: none assigned yet
                    </div>
                  );
                }

                return (
                  <div style={{ marginTop: '6px' }}>
                    <div style={{ fontSize: '12px', color: '#555', marginBottom: '4px' }}>
                      Pins:
                    </div>
                    <ul style={{ margin: 0, paddingLeft: '16px', fontSize: '12px', color: '#555' }}>
                      {assigned.map((p) => (
                        <li key={p.id != null ? String(p.id) : `${p.locationName}-${p.visitDate || 'no-date'}`}>
                          {p.visitDate
                            ? `${formatDate(p.visitDate)} — ${p.locationName || 'Unnamed Pin'}`
                            : (p.locationName || 'Unnamed Pin')}
                        </li>
                      ))}
                    </ul>
                  </div>
                );
              })()}
              {tripDistances[Number(t.id)] != null && (
                <div style={{
                  marginTop: '6px',
                  fontSize: '12px',
                  color: '#1D9E75',
                  fontWeight: 600,
                }}>
                  {tripDistances[Number(t.id)].distanceKm.toFixed(1)} km
                  &nbsp;/&nbsp;
                  {tripDistances[Number(t.id)].distanceMi.toFixed(1)} mi
                  {tripDistances[Number(t.id)].skippedPinIds?.length > 0 && (
                    <span style={{ color: '#b00020', fontWeight: 400, marginLeft: '4px' }}>
                      ({tripDistances[Number(t.id)].skippedPinIds.length} pin(s) missing coords)
                    </span>
                  )}
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
