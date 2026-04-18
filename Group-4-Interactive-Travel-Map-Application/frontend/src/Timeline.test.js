// --- TIMELINE TESTS (FR10: View Timeline) -------------------------------------
import { render, screen, within } from '@testing-library/react';
import Timeline from './components/Timeline';

test('FR10 — shows empty state when no pins', () => {
  render(<Timeline pins={[]} trips={[]} />);
  expect(screen.getByTestId('timeline')).toBeInTheDocument();
  expect(screen.getByText(/No pins yet/i)).toBeInTheDocument();
});

test('FR10 — lists pins in chronological order by visitDate', () => {
  const pins = [
    { id: 1, locationName: 'Later', visitDate: '2024-06-15', latitude: 1, longitude: 1 },
    { id: 2, locationName: 'Earlier', visitDate: '2024-03-01', latitude: 2, longitude: 2 },
    { id: 3, locationName: 'Middle', visitDate: '2024-05-01', latitude: 3, longitude: 3 },
  ];
  render(<Timeline pins={pins} trips={[]} />);

  const list = screen.getByTestId('timeline');
  const items = within(list).getAllByText(/2024-/);
  expect(items[0]).toHaveTextContent('2024-03-01');
  expect(items[1]).toHaveTextContent('2024-05-01');
  expect(items[2]).toHaveTextContent('2024-06-15');
});

test('FR10 — shows trip name when pin has tripId', () => {
  const trips = [{ id: 10, name: 'Summer2024' }];
  const pins = [
    { id: 1, locationName: 'Paris', visitDate: '2024-06-01', tripId: 10, latitude: 1, longitude: 1 },
  ];
  render(<Timeline pins={pins} trips={trips} />);
  expect(screen.getByText('Summer2024')).toBeInTheDocument();
});
