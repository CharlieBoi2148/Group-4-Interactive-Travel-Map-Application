// --- VIEW: DISTANCE PANEL (FR8) -----------------------------------------------
// Pure View — displays pin-to-pin distance result passed from App.js.
// Never fetches data — all fetch calls happen in App.js via mapDistanceService.

/**
 * @param {Object}   props
 * @param {Object}   props.measuringFrom  pin A currently selected, or null
 * @param {Object}   props.measuredTo     pin B (only set once result is ready), or null
 * @param {Object}   props.result         DistanceResult from backend, or null
 * @param {string}   props.error          error message, or null
 * @param {boolean}  props.loading        true while fetch is in flight
 * @param {function} props.onCancel       cancel measuring mode
 */
export default function DistancePanel({
  measuringFrom,
  measuredTo,
  result,
  error,
  loading,
  onCancel,
}) {
  if (!measuringFrom && !result && !error) return null;

  return (
    <div
      data-testid="distance-panel"
      style={{
        position: 'absolute',
        bottom: '24px',
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 1000,
        background: 'white',
        borderRadius: '8px',
        boxShadow: '0 2px 12px rgba(0,0,0,0.2)',
        padding: '16px 20px',
        minWidth: '280px',
        maxWidth: '400px',
        fontSize: '14px',
      }}
    >
      {/* Waiting for pin B */}
      {measuringFrom && !result && !loading && !error && (
        <>
          <div style={{ fontWeight: 600, marginBottom: '6px' }}>
            Measuring from: {measuringFrom.locationName}
          </div>
          <div style={{ color: '#555', fontSize: '13px' }}>
            Click <strong>Measure</strong> on another pin to calculate the distance.
          </div>
        </>
      )}

      {/* Loading */}
      {loading && (
        <div style={{ color: '#555' }}>Calculating distance…</div>
      )}

      {/* Error */}
      {error && !loading && (
        <div role="alert" style={{ color: '#b00020' }}>
          {error}
        </div>
      )}

      {/* Result */}
      {result && !loading && !error && (
        <>
          <div style={{ fontWeight: 600, marginBottom: '10px' }}>
            {measuringFrom?.locationName} to {measuredTo?.locationName}
          </div>
          <div style={{ display: 'flex', gap: '16px' }}>
            <div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#333' }}>
                {result.distanceKm.toFixed(1)} km
              </div>
            </div>
            <div style={{ color: '#ccc', fontSize: '20px' }}>|</div>
            <div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#333' }}>
                {result.distanceMi.toFixed(1)} mi
              </div>
            </div>
          </div>
          {result.skippedPinIds && result.skippedPinIds.length > 0 && (
            <div
              role="alert"
              style={{
                marginTop: '8px',
                fontSize: '12px',
                color: '#b00020',
                background: '#ffebee',
                padding: '6px 8px',
                borderRadius: '4px',
              }}
            >
              {result.skippedPinIds.length} pin(s) skipped — missing coordinates.
            </div>
          )}
        </>
      )}

      <button
        onClick={onCancel}
        style={{
          marginTop: '12px',
          width: '100%',
          padding: '6px',
          background: '#eee',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '13px',
        }}
      >
        Cancel
      </button>
    </div>
  );
}
