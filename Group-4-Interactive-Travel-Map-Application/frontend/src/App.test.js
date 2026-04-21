// --- APP TESTS ----------------------------------------------------------------
// FR6: Visualize Map — App correctly mounts MapView as top-level View component
// FR3: Delete Travel Pin — confirmation dialog appears and delete/cancel work
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from './App';

// Mock MapView — renders pins and exposes a delete and edit button per pin
jest.mock('./components/MapView', () => ({ pins, onDeletePin, onEditPin }) => (
  <div data-testid="map-view">
    {pins.map(pin => (
      <div key={pin.id}>
        <button
          data-testid={`delete-btn-${pin.id}`}
          onClick={() => onDeletePin(pin)}
        >
          Delete {pin.locationName}
        </button>
        <button
          data-testid={`edit-btn-${pin.id}`}
          onClick={() => onEditPin(pin)}
        >
          Edit {pin.locationName}
        </button>
      </div>
    ))}
  </div>
));

jest.mock('./components/Timeline', () => () => <div data-testid="timeline">Timeline</div>);

jest.mock('./services/mapService', () => {});

// Mock pinService so tests never hit the real backend.
// Implementations are set in beforeEach because resetMocks (CRA default)
// strips jest.fn() implementations between tests.
jest.mock('./services/pinService', () => ({
  __esModule: true,
  getPins: jest.fn(),
  createPin: jest.fn(),
  deletePin: jest.fn(),
  updatePin: jest.fn(),
  setPinPrivacy: jest.fn(),
}));

// Mock tripService because App calls getTrips on mount.
jest.mock('./services/tripService', () => ({
  __esModule: true,
  getTrips: jest.fn(),
  createTrip: jest.fn(),
  setTripPrivacy: jest.fn(),
}));

beforeEach(() => {
  const { getPins, deletePin, updatePin, setPinPrivacy } = require('./services/pinService');
  const { getTrips } = require('./services/tripService');

  getTrips.mockResolvedValue([]);
  getPins.mockResolvedValue([
    { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', latitude: 48.8584, longitude: 2.2945, lat: 48.8584, lng: 2.2945 }
  ]);
  deletePin.mockResolvedValue(true);
  updatePin.mockResolvedValue(
    { id: 1, locationName: 'Eiffel Tower Updated', visitDate: '2024-06-01', notes: '', latitude: 48.8584, longitude: 2.2945, lat: 48.8584, lng: 2.2945 }
  );
  setPinPrivacy.mockResolvedValue(
    { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', notes: '', privacyLevel: 'PUBLIC', latitude: 48.8584, longitude: 2.2945, lat: 48.8584, lng: 2.2945 }
  );
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

// ── FR2: Edit Travel Pin ──────────────────────────────────────────────────────

test('FR2 — clicking Edit on a pin opens the EditPinForm', async () => {
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  // EditPinForm heading appears
  expect(screen.getByText('Edit Pin')).toBeInTheDocument();
});

test('FR2 — clicking Cancel in EditPinForm closes it without calling updatePin', async () => {
  const { updatePin } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.click(screen.getByText('Cancel'));

  // Form is closed
  await waitFor(() =>
    expect(screen.queryByText('Edit Pin')).not.toBeInTheDocument()
  );

  // Backend was never called
  expect(updatePin).not.toHaveBeenCalled();
});

test('FR2 — saving calls updatePin with the correct id and fields', async () => {
  const { updatePin } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.click(screen.getByText('Save Changes'));

  await waitFor(() =>
    expect(updatePin).toHaveBeenCalledWith(1, {
      locationName: 'Eiffel Tower',
      country: '',
      region: '',
      visitDate: '2024-06-01',
      notes: '',
    })
  );
});

test('FR2 — state updates after save and edit form closes', async () => {
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.click(screen.getByText('Save Changes'));

  // Edit form closes after successful save
  await waitFor(() =>
    expect(screen.queryByText('Edit Pin')).not.toBeInTheDocument()
  );
});

test('FR2 — edit form closes cleanly even when updatePin rejects', async () => {
  const { updatePin } = require('./services/pinService');
  updatePin.mockRejectedValueOnce(new Error('Network error'));

  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.click(screen.getByText('Save Changes'));

  // Form still closes — the finally block in handleUpdatePin ensures this
  await waitFor(() =>
    expect(screen.queryByText('Edit Pin')).not.toBeInTheDocument()
  );
});

// ── FR11: Pin Privacy ─────────────────────────────────────────────────────────

test('FR11 — changing privacy dropdown calls setPinPrivacy with correct id and value', async () => {
  const { setPinPrivacy } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  // Change dropdown — EditPinForm renders fully so getByDisplayValue works
  fireEvent.change(screen.getByDisplayValue('Private'), { target: { value: 'PUBLIC' } });

  await waitFor(() =>
    expect(setPinPrivacy).toHaveBeenCalledWith(1, 'PUBLIC')
  );
});

test('FR11 — pins array updates with returned pin after privacy change', async () => {
  const { setPinPrivacy } = require('./services/pinService');
  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.change(screen.getByDisplayValue('Private'), { target: { value: 'PUBLIC' } });

  // setPinPrivacy resolves with updated pin — state should update without crash
  await waitFor(() =>
    expect(setPinPrivacy).toHaveBeenCalledTimes(1)
  );

  // Edit form remains open — privacy change does not close the form
  expect(screen.getByText('Edit Pin')).toBeInTheDocument();
});

test('FR11 — form stays open and does not crash when setPinPrivacy rejects', async () => {
  const { setPinPrivacy } = require('./services/pinService');
  setPinPrivacy.mockRejectedValueOnce(new Error('Network error'));

  render(<App />);

  await waitFor(() => screen.getByTestId('edit-btn-1'));
  fireEvent.click(screen.getByTestId('edit-btn-1'));

  fireEvent.change(screen.getByDisplayValue('Private'), { target: { value: 'PUBLIC' } });

  // Form stays open — handlePrivacyChange has no finally that closes the form
  await waitFor(() =>
    expect(screen.getByText('Edit Pin')).toBeInTheDocument()
  );

});

// ── FR10: View Timeline ─────────────────────────────────────────────────────────

test('FR10 — toggles to timeline view and back to map', () => {
  render(<App />);
  expect(screen.getByTestId('map-view')).toBeInTheDocument();

  fireEvent.click(screen.getByTestId('main-view-timeline'));
  expect(screen.queryByTestId('map-view')).not.toBeInTheDocument();
  expect(screen.getByTestId('timeline')).toBeInTheDocument();

  fireEvent.click(screen.getByTestId('main-view-map'));
  expect(screen.getByTestId('map-view')).toBeInTheDocument();
  });