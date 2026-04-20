// --- VIEW: TIMELINE (FR10 — View Timeline) ------------------------------------
// Pure View — renders pins in chronological order by visit date.
// Receives pins and trips from App.js; does not fetch or mutate backend data.

/**
 * @param {Object} props
 * @param {Array<Object>} props.pins pins from GET /api/pins
 * @param {Array<Object>} props.trips trips from GET /api/trips (for trip name lookup)
 * @param {'sidebar'|'main'} [props.variant] layout — main uses full panel height (FR10 dedicated view)
 */
export default function Timeline({ pins = [], trips = [], variant = 'sidebar' }) {
  const isMain = variant === 'main';
  const railBg = isMain ? '#fff' : '#fafafa';
  const formatDate = (d) => {
    if (!d) return '—';
    return typeof d === 'string' ? d : d;
  };

  const tripNameById = new Map(
    trips.filter((t) => t.id != null).map((t) => [Number(t.id), t.name])
  );

  const sortChronological = (a, b) => {
    const aDate = a?.visitDate ? new Date(a.visitDate).getTime() : Number.POSITIVE_INFINITY;
    const bDate = b?.visitDate ? new Date(b.visitDate).getTime() : Number.POSITIVE_INFINITY;
    if (aDate !== bDate) return aDate - bDate;
    return String(a?.locationName || '').localeCompare(String(b?.locationName || ''));
  };

  const ordered = [...pins].sort(sortChronological);

  return (
    <div
      data-testid="timeline"
      style={{
        marginBottom: isMain ? 0 : '16px',
        borderTop: isMain ? 'none' : '1px solid #e0e0e0',
        paddingTop: isMain ? 0 : '12px',
        height: isMain ? '100%' : undefined,
        display: isMain ? 'flex' : undefined,
        flexDirection: isMain ? 'column' : undefined,
        minHeight: 0,
        flex: isMain ? 1 : undefined,
        boxSizing: 'border-box',
      }}
    >
      <h3 style={{ margin: '0 0 12px', fontSize: isMain ? '18px' : '16px', flexShrink: 0 }}>
        Timeline
      </h3>
      {ordered.length === 0 ? (
        <p style={{ margin: 0, fontSize: '13px', color: '#666' }}>
          No pins yet. Add pins on the map to see your travel history in order.
        </p>
      ) : (
        <ul
          style={{
            listStyle: 'none',
            padding: 0,
            margin: 0,
            maxHeight: isMain ? 'none' : '35vh',
            flex: isMain ? 1 : undefined,
            minHeight: 0,
            overflowY: 'auto',
            borderLeft: '2px solid #1D9E75',
            paddingLeft: '12px',
            marginLeft: '6px',
          }}
        >
          {ordered.map((pin, index) => {
            const tripLabel =
              pin.tripId != null
                ? tripNameById.get(Number(pin.tripId)) || `Trip #${pin.tripId}`
                : null;

            return (
              <li
                key={pin.id != null ? String(pin.id) : `tl-${index}-${pin.locationName || 'pin'}`}
                style={{
                  position: 'relative',
                  paddingBottom: '14px',
                  fontSize: '13px',
                  color: '#333',
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    left: '-19px',
                    top: '4px',
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: '#1D9E75',
                    border: `2px solid ${railBg}`,
                  }}
                  aria-hidden
                />
                <div style={{ fontWeight: 600 }}>{formatDate(pin.visitDate)}</div>
                <div style={{ marginTop: '2px' }}>{pin.locationName || 'Unnamed Pin'}</div>
                {tripLabel ? (
                  <div style={{ fontSize: '11px', color: '#888', marginTop: '2px' }}>
                    {tripLabel}
                  </div>
                ) : null}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
