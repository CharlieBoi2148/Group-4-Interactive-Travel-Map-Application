// --- APP TESTS (FR6: Visualize Map) ------------------------------------------
// Tests that App.js correctly mounts MapView as the top-level View component.
import { render, screen } from '@testing-library/react';
import App from './App';

jest.mock('./components/MapView', () => () => <div data-testid="map-view">Map</div>);

test('renders the map view', () => {
  render(<App />);
  expect(screen.getByTestId('map-view')).toBeInTheDocument();
});