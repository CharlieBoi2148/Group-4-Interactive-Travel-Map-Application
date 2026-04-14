// --- VIEW: TRIP LIST (FR4 — View Pins and Trips, trip portion) ---------------
// Pure View — renders trips passed from App.js; does not fetch.

/**
 * @param {Object} props
 * @param {Array<Object>} props.trips trips from GET /api/trips
 */
export default function TripList({ trips }) {
  const formatDate = (d) => {
    if (!d) return '—';
    return typeof d === 'string' ? d : d;
  };

  return (
    <div
      data-testid="trip-list"
      style={{
        marginBottom: '16px',
      }}
    >
      <h3 style={{ margin: '0 0 8px', fontSize: '16px' }}>Your trips</h3>
      {trips.length === 0 ? (
        <p style={{ margin: 0, fontSize: '13px', color: '#666' }}>
          No trips yet. Use &quot;New trip&quot; to create one (FR5).
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
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
