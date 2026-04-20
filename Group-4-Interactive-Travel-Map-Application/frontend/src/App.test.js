// --- APP TESTS (FR6: Visualize Map) ------------------------------------------
// Tests that App.js correctly mounts MapView as the top-level View component.
import { render, screen, fireEvent } from '@testing-library/react';
import App from './App';

jest.mock('./components/MapView', () => () => <div data-testid="map-view">Map</div>);

test('renders the map view', () => {
  render(<App />);
  expect(screen.getByTestId('map-view')).toBeInTheDocument();
});

test('FR10 — toggles to timeline view and back to map', () => {
  render(<App />);
  expect(screen.getByTestId('map-view')).toBeInTheDocument();

  fireEvent.click(screen.getByTestId('main-view-timeline'));
  expect(screen.queryByTestId('map-view')).not.toBeInTheDocument();
  expect(screen.getByTestId('timeline')).toBeInTheDocument();

  fireEvent.click(screen.getByTestId('main-view-map'));
  expect(screen.getByTestId('map-view')).toBeInTheDocument();
});