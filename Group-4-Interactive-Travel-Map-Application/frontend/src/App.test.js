// --- APP TESTS ----------------------------------------------------------------
// FR6: Visualize Map — App correctly mounts MapView as top-level View component
// FR3: Delete Travel Pin — confirmation dialog appears and delete/cancel work
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';

// Mock MapView — renders pins and exposes a delete button per pin
jest.mock('./components/MapView', () => ({ pins, onDeletePin }) => (
  <div data-testid="map-view">
    {pins.map(pin => (
      <button
        key={pin.id}
        data-testid={`delete-btn-${pin.id}`}
        onClick={() => onDeletePin(pin)}
      >
        Delete {pin.locationName}
      </button>
    ))}
  </div>
));

jest.mock('./services/mapService', () => {});


// Mock pinService so tests never hit the real backend.
// Implementations are set in beforeEach because resetMocks (CRA default)
// strips jest.fn() implementations between tests.
jest.mock('./services/pinService', () => ({
  __esModule: true,
  getPins: jest.fn(),
  createPin: jest.fn(),
  deletePin: jest.fn(),
}));

beforeEach(() => {
  const { getPins, deletePin } = require('./services/pinService');
  getPins.mockResolvedValue([
    { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', latitude: 48.8584, longitude: 2.2945, lat: 48.8584, lng: 2.2945 }
  ]);
  deletePin.mockResolvedValue(true);
});

// ── FR6: Map renders ──────────────────────────────────────────────────────────

test('FR6 — renders the map view', () => {
  render(<App />);
  expect(screen.getByTestId('map-view')).toBeInTheDocument();
});

// ── FR3: Delete Travel Pin ────────────────────────────────────────────────────

test('FR3 — clicking delete opens confirmation dialog', async () => {
  render(<App />);

  // Wait for pins to load from mocked getPins
  await waitFor(() => screen.getByTestId('delete-btn-1'));

  // User clicks delete button on the pin
  fireEvent.click(screen.getByTestId('delete-btn-1'));

  // Confirmation dialog appears per FR3 step 3
  expect(screen.getByText(/Are you sure you want to delete/)).toBeInTheDocument();
  expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
});

test('FR3 — clicking Cancel in dialog closes it without deleting', async () => {
  const { deletePin } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('delete-btn-1'));
  fireEvent.click(screen.getByTestId('delete-btn-1'));

  // User clicks Cancel
  fireEvent.click(screen.getByText('Cancel'));

  // Dialog is gone
  expect(screen.queryByText(/Are you sure you want to delete/)).not.toBeInTheDocument();

  // Backend was never called
  expect(deletePin).not.toHaveBeenCalled();
});

test('FR3 — confirming delete calls deletePin and removes pin from state', async () => {
  const { deletePin } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('delete-btn-1'));
  fireEvent.click(screen.getByTestId('delete-btn-1'));

  // User confirms deletion
  fireEvent.click(screen.getByText('Delete'));

  // pinService.deletePin was called with the correct id
  await waitFor(() => expect(deletePin).toHaveBeenCalledWith(1));

  // Dialog is closed after deletion — needs waitFor as state update is async
  await waitFor(() =>
    expect(screen.queryByText(/Are you sure you want to delete/)).not.toBeInTheDocument()
  );

  // Pin is removed from state — delete button no longer rendered
  await waitFor(() =>
    expect(screen.queryByTestId('delete-btn-1')).not.toBeInTheDocument()
  );
});

test('FR3 — dialog closes even when backend delete fails', async () => {
  const { deletePin } = require('./services/pinService');

  // Override deletePin to simulate a backend failure for this test only
  deletePin.mockRejectedValueOnce(new Error('Network error'));

  render(<App />);

  await waitFor(() => screen.getByTestId('delete-btn-1'));
  fireEvent.click(screen.getByTestId('delete-btn-1'));
  fireEvent.click(screen.getByText('Delete'));

  // Dialog should still close even though backend failed —
  // the finally block in handleConfirmDelete ensures this
  await waitFor(() =>
    expect(screen.queryByText(/Are you sure you want to delete/)).not.toBeInTheDocument()
  );
});