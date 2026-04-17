import { render, screen, fireEvent } from '@testing-library/react';
import EditPinForm from './components/EditPinForm';

const mockPin = {
  id: 1,
  locationName: 'Eiffel Tower',
  visitDate: '2024-06-01',
  notes: 'Amazing view',
  latitude: 48.8584,
  longitude: 2.2945
};

// FR2 — EditPinForm renders the form with heading and pin subtitle
test('FR2 — renders edit form with heading and pin name subtitle', () => {
  // Arrange
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} />);

  // Assert
  expect(screen.getByText('Edit Pin')).toBeInTheDocument();
  expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
  expect(screen.getByText('Save Changes')).toBeInTheDocument();
  expect(screen.getByText('Cancel')).toBeInTheDocument();
});

// FR2 — inputs are pre-filled with the pin's existing values, not empty
test('FR2 — inputs are pre-filled with existing pin data', () => {
  // Arrange
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} />);

  // Assert
  expect(screen.getByPlaceholderText('Location name').value).toBe('Eiffel Tower');
  expect(document.querySelector('input[type="date"]').value).toBe('2024-06-01');
  expect(screen.getByPlaceholderText('Notes').value).toBe('Amazing view');
});

// FR2 — date input is empty when pin has no visitDate
test('FR2 — date input is empty when pin has no visitDate', () => {
  // Arrange
  const pinWithoutDate = { ...mockPin, visitDate: null };
  render(<EditPinForm pin={pinWithoutDate} onSave={jest.fn()} onCancel={jest.fn()} />);

  // Assert
  expect(document.querySelector('input[type="date"]').value).toBe('');
});

// FR2 — valid save: locationName filled, onSave called with correct fields
test('FR2 — valid save delegates correct fields to onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} />);

  // Act
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).toHaveBeenCalledTimes(1);
  expect(onSave).toHaveBeenCalledWith({
    locationName: 'Eiffel Tower',
    visitDate: '2024-06-01',
    notes: 'Amazing view'
  });
});

// FR2 — valid save with all fields edited: onSave receives updated values
test('FR2 — valid save with all fields edited calls onSave with new values', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} />);

  // Act
  fireEvent.change(screen.getByPlaceholderText('Location name'), { target: { value: 'Louvre Museum' } });
  fireEvent.change(screen.getByPlaceholderText('Notes'), { target: { value: 'Great art' } });
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).toHaveBeenCalledWith({
    locationName: 'Louvre Museum',
    visitDate: '2024-06-01',
    notes: 'Great art'
  });
});

// FR2 — invalid save: empty locationName — onSave must NOT be called
test('FR2 — invalid save with empty locationName does not call onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} />);

  // Act
  fireEvent.change(screen.getByPlaceholderText('Location name'), { target: { value: '' } });
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).not.toHaveBeenCalled();
});

// FR2 — invalid save: whitespace-only locationName — onSave must NOT be called
test('FR2 — invalid save with whitespace-only locationName does not call onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} />);

  // Act
  fireEvent.change(screen.getByPlaceholderText('Location name'), { target: { value: '   ' } });
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).not.toHaveBeenCalled();
});

// FR2 — cancel: onCancel is called, onSave is NOT called
test('FR2 — cancel calls onCancel and does not call onSave', () => {
  // Arrange
  const onSave = jest.fn();
  const onCancel = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={onCancel} />);

  // Act
  fireEvent.click(screen.getByText('Cancel'));

  // Assert
  expect(onCancel).toHaveBeenCalledTimes(1);
  expect(onSave).not.toHaveBeenCalled();
});
