// --- TRIPFORM TESTS (FR5: Organize Trip — create trip metadata) ----------------
import { render, screen, fireEvent } from '@testing-library/react';
import TripForm from './components/TripForm';

test('FR5 — renders trip name input', () => {
  render(<TripForm onSave={jest.fn()} onCancel={jest.fn()} />);
  expect(screen.getByTestId('trip-name-input')).toBeInTheDocument();
});

test('FR5 — calls onSave with trip fields when Save trip is clicked', () => {
  const onSave = jest.fn();
  render(<TripForm onSave={onSave} onCancel={jest.fn()} />);

  fireEvent.change(screen.getByTestId('trip-name-input'), {
    target: { value: 'Summer in Spain' },
  });
  fireEvent.change(screen.getByPlaceholderText('Description (optional)'), {
    target: { value: 'Coastal cities' },
  });
  const dates = document.querySelectorAll('input[type="date"]');
  fireEvent.change(dates[0], { target: { value: '2026-06-01' } });
  fireEvent.change(dates[1], { target: { value: '2026-06-14' } });
  fireEvent.click(screen.getByText('Save trip'));

  expect(onSave).toHaveBeenCalledWith({
    name: 'Summer in Spain',
    description: 'Coastal cities',
    startDate: '2026-06-01',
    endDate: '2026-06-14',
    privacyLevel: 'PRIVATE',
  });
});

test('FR5 — onSave is not called when name is empty', () => {
  const onSave = jest.fn();
  render(<TripForm onSave={onSave} onCancel={jest.fn()} />);
  fireEvent.click(screen.getByText('Save trip'));
  expect(onSave).not.toHaveBeenCalled();
});

test('FR5 — Cancel calls onCancel', () => {
  const onCancel = jest.fn();
  render(<TripForm onSave={jest.fn()} onCancel={onCancel} />);
  fireEvent.click(screen.getByText('Cancel'));
  expect(onCancel).toHaveBeenCalled();
});
