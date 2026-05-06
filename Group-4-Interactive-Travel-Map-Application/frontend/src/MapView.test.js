// --- MAPVIEW TESTS (FR6: Visualize Map, FR3: Delete Travel Pin, FR8: Distance) -
// Tests MapView as a pure View component — verifies it renders pins correctly
// and delegates delete and measure actions to the Controller (App.js) via props.
//
// Input partitions:
//   - No pins: empty map renders without errors
//   - One pin: marker and delete button appear with correct label
//   - Multiple pins: all markers and delete buttons render
//   - Delete button: delegates correct pin object upward to Controller
//   - Measure button: renders, labels, styling, and delegation (FR8)
//
// Run with: npm test

import { render, screen, fireEvent } from '@testing-library/react';
import MapView from './components/MapView';

// Mock react-leaflet — MapContainer can't render in jsdom (no real browser map)
jest.mock('react-leaflet', () => ({
  MapContainer: ({ children }) => <div data-testid="map-container">{children}</div>,
  TileLayer: () => null,
  Marker: ({ children }) => <div data-testid="marker">{children}</div>,
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
  useMapEvents: () => null,
}));

const mockOnMapClick = jest.fn();
const mockOnDeletePin = jest.fn();
const mockOnMeasurePin = jest.fn();

beforeEach(() => {
  mockOnMapClick.mockClear();
  mockOnDeletePin.mockClear();
  mockOnMeasurePin.mockClear();
});

const singlePin = [
  { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', latitude: 48.8584, longitude: 2.2945 }
];

const multiplePins = [
  { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', latitude: 48.8584, longitude: 2.2945 },
  { id: 2, locationName: 'Colosseum', visitDate: '2024-07-15', latitude: 41.8902, longitude: 12.4922 },
  { id: 3, locationName: 'Sagrada Familia', visitDate: '2024-08-20', latitude: 41.4036, longitude: 2.1744 }
];

// ── FR6: Map renders ──────────────────────────────────────────────────────────

test('FR6 — renders map container with no pins', () => {
  render(<MapView pins={[]} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  expect(screen.getByTestId('map-container')).toBeInTheDocument();
  expect(screen.queryByTestId('marker')).not.toBeInTheDocument();
});

test('FR6 — renders one marker when given one pin', () => {
  render(<MapView pins={singlePin} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  expect(screen.getAllByTestId('marker')).toHaveLength(1);
  expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
  expect(screen.getByText('2024-06-01')).toBeInTheDocument();
});

test('FR6 — renders correct number of markers for multiple pins', () => {
  render(<MapView pins={multiplePins} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  expect(screen.getAllByTestId('marker')).toHaveLength(3);
  expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
  expect(screen.getByText('Colosseum')).toBeInTheDocument();
  expect(screen.getByText('Sagrada Familia')).toBeInTheDocument();
});

// ── FR3: Delete button ────────────────────────────────────────────────────────

test('FR3 — delete button renders for each pin', () => {
  render(<MapView pins={multiplePins} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  expect(screen.getAllByText('Delete')).toHaveLength(3);
});

test('FR3 — clicking delete button calls onDeletePin with correct pin', () => {
  render(<MapView pins={singlePin} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  fireEvent.click(screen.getByText('Delete'));
  expect(mockOnDeletePin).toHaveBeenCalledWith(singlePin[0]);
});

test('FR3 — clicking delete on one pin does not call onDeletePin with wrong pin', () => {
  render(<MapView pins={multiplePins} onMapClick={mockOnMapClick} onDeletePin={mockOnDeletePin} />);
  const deleteButtons = screen.getAllByText('Delete');

  // Click the second pin's delete button
  fireEvent.click(deleteButtons[1]);

  // Should be called with Colosseum, not Eiffel Tower
  expect(mockOnDeletePin).toHaveBeenCalledWith(multiplePins[1]);
  expect(mockOnDeletePin).not.toHaveBeenCalledWith(multiplePins[0]);
});

// ── FR8: Measure button render ────────────────────────────────────────────────

test('measureButton_rendersInPopupForValidPin_FR8', () => {
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={null}
    />
  );
  expect(screen.getByText('Measure')).toBeInTheDocument();
});

test('measureButton_labelIsMeasure_whenMeasuringFromNull_FR8', () => {
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={null}
    />
  );
  expect(screen.getByText('Measure')).toBeInTheDocument();
  expect(screen.queryByText('Cancel')).toBeNull();
});

test('measureButton_labelIsMeasure_whenMeasuringFromDifferentPin_FR8', () => {
  const differentPin = { id: 99, locationName: 'Other Place', latitude: 0, longitude: 0 };
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={differentPin}
    />
  );
  expect(screen.getByText('Measure')).toBeInTheDocument();
  expect(screen.queryByText('Cancel')).toBeNull();
});

test('measureButton_labelIsCancel_whenMeasuringFromMatchesPin_FR8', () => {
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={singlePin[0]}
    />
  );
  expect(screen.getByText('Cancel')).toBeInTheDocument();
  expect(screen.queryByText('Measure')).toBeNull();
});

// ── FR8: Measure button interaction ──────────────────────────────────────────

test('measureButton_click_callsOnMeasurePinWithCorrectPin_FR8', () => {
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={null}
    />
  );
  fireEvent.click(screen.getByText('Measure'));
  expect(mockOnMeasurePin).toHaveBeenCalledWith(singlePin[0]);
});

test('measureButton_click_doesNotCallForOtherPin_FR8', () => {
  render(
    <MapView
      pins={multiplePins}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={null}
    />
  );
  const measureButtons = screen.getAllByText('Measure');
  fireEvent.click(measureButtons[0]);
  expect(mockOnMeasurePin).toHaveBeenCalledWith(multiplePins[0]);
  expect(mockOnMeasurePin).not.toHaveBeenCalledWith(multiplePins[1]);
});

test('measureButton_click_doesNotThrow_whenOnMeasurePinUndefined_FR8', () => {
  render(
    <MapView
      pins={singlePin}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={undefined}
      measuringFrom={null}
    />
  );
  expect(() => fireEvent.click(screen.getByText('Measure'))).not.toThrow();
});

// ── FR8: Measure button styling ───────────────────────────────────────────────

test('measureButton_backgroundDiffers_purpleForMatchingPinGreyForOther_FR8', () => {
  const twoPins = [
    { id: 1, locationName: 'Eiffel Tower', visitDate: '2024-06-01', latitude: 48.8584, longitude: 2.2945 },
    { id: 2, locationName: 'Colosseum', visitDate: '2024-07-15', latitude: 41.8902, longitude: 12.4922 },
  ];
  render(
    <MapView
      pins={twoPins}
      onMapClick={mockOnMapClick}
      onDeletePin={mockOnDeletePin}
      onMeasurePin={mockOnMeasurePin}
      measuringFrom={twoPins[0]}
    />
  );
  // Pin 0 matches measuringFrom → label "Cancel", background purple
  const cancelButton = screen.getByText('Cancel');
  expect(cancelButton.style.background).toBe('rgb(128, 90, 213)');
  // Pin 1 does not match → label "Measure", background grey
  const measureButton = screen.getByText('Measure');
  expect(measureButton.style.background).toBe('rgb(113, 128, 150)');
});