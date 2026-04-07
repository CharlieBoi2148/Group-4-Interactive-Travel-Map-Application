// --- PINFORM TESTS (FR1: Create Travel Pin) -----------------------------------
// Tests PinForm as a View component — verifies it captures user input correctly
// and delegates to the Controller (App.js) via props without saving data itself.
//
// Testing philosophy: we test what the USER does and sees, not internal state.
// These tests directly map to FR1 acceptance criteria from the SRS.
//
// Run with: npm test

import { render, screen, fireEvent } from '@testing-library/react';
import PinForm from './components/PinForm';

// Dummy latlng passed as prop — matches what Leaflet sends on map click
const mockLatlng = { lat: 48.8584, lng: 2.2945 };

// ── FR1: Save Pin ─────────────────────────────────────────────────────────────

test('FR1 — renders location name input', () => {
    render(<PinForm latlng={mockLatlng} onSave={jest.fn()} onCancel={jest.fn()} />);
    expect(screen.getByPlaceholderText('Location name')).toBeInTheDocument();
});

test('FR1 — renders visit date input', () => {
    render(<PinForm latlng={mockLatlng} onSave={jest.fn()} onCancel={jest.fn()} />);
    expect(document.querySelector('input[type="date"]')).toBeInTheDocument();
});

test('FR1 — displays clicked coordinates', () => {
    render(<PinForm latlng={mockLatlng} onSave={jest.fn()} onCancel={jest.fn()} />);
    // Coordinates are shown so the user can confirm they clicked the right spot
    expect(screen.getByText(/48.8584/)).toBeInTheDocument();
    expect(screen.getByText(/2.2945/)).toBeInTheDocument();
});

test('FR1 — calls onSave with locationName when Save Pin is clicked', () => {
  const mockSave = jest.fn();

  render(<PinForm latlng={mockLatlng} onSave={mockSave} onCancel={jest.fn()} />);

  // User types a location name (FR1 step 2)
  fireEvent.change(screen.getByPlaceholderText('Location name'), {
    target: { value: 'Eiffel Tower' }
  });

  // User clicks Save (FR1 step 5)
  fireEvent.click(screen.getByText('Save Pin'));

  // Controller (App.js) receives the correct data
  expect(mockSave).toHaveBeenCalledWith({
    locationName: 'Eiffel Tower',
    visitDate: ''
  });
});

test('FR1 — calls onSave with both locationName and visitDate', () => {
  const mockSave = jest.fn();

  render(<PinForm latlng={mockLatlng} onSave={mockSave} onCancel={jest.fn()} />);

  fireEvent.change(screen.getByPlaceholderText('Location name'), {
    target: { value: 'Eiffel Tower' }
  });

  fireEvent.change(document.querySelector('input[type="date"]'), {
    target: { value: '2024-06-01' }
});

  fireEvent.click(screen.getByText('Save Pin'));

  expect(mockSave).toHaveBeenCalledWith({
    locationName: 'Eiffel Tower',
    visitDate: '2024-06-01'
  });
});

test('FR1 — onSave is not called if locationName is empty', () => {
  const mockSave = jest.fn();

  render(<PinForm latlng={mockLatlng} onSave={mockSave} onCancel={jest.fn()} />);

  // User clicks Save without entering a name
  fireEvent.click(screen.getByText('Save Pin'));

  // Controller should not receive a save call — empty pin is invalid
  expect(mockSave).not.toHaveBeenCalled();
});

// ── FR1: Cancel ───────────────────────────────────────────────────────────────

test('FR1 — calls onCancel when Cancel is clicked', () => {
  const mockCancel = jest.fn();

  render(<PinForm latlng={mockLatlng} onSave={jest.fn()} onCancel={mockCancel} />);

  fireEvent.click(screen.getByText('Cancel'));

  expect(mockCancel).toHaveBeenCalledTimes(1);
});

test('FR1 — onSave is not called when Cancel is clicked', () => {
  const mockSave = jest.fn();

  render(<PinForm latlng={mockLatlng} onSave={mockSave} onCancel={jest.fn()} />);

  fireEvent.click(screen.getByText('Cancel'));

  expect(mockSave).not.toHaveBeenCalled();
});