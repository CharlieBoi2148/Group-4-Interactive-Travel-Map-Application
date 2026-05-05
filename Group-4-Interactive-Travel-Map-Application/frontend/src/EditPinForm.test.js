import { render, screen, fireEvent } from '@testing-library/react';
import EditPinForm from './components/EditPinForm';

const mockPin = {
  id: 1,
  locationName: 'Eiffel Tower',
  country: 'France',
  region: 'Île-de-France',
  visitDate: '2024-06-01',
  notes: 'Amazing view',
  latitude: 48.8584,
  longitude: 2.2945
};

// FR2 — EditPinForm renders the form with heading and pin subtitle
test('FR2 — renders edit form with heading and pin name subtitle', () => {
  // Arrange
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(screen.getByText('Edit Pin')).toBeInTheDocument();
  expect(screen.getByText('Eiffel Tower')).toBeInTheDocument();
  expect(screen.getByText('Save Changes')).toBeInTheDocument();
  expect(screen.getByText('Cancel')).toBeInTheDocument();
});

// FR2 — inputs are pre-filled with the pin's existing values, not empty
test('FR2 — inputs are pre-filled with existing pin data', () => {
  // Arrange
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(screen.getByPlaceholderText('Location name').value).toBe('Eiffel Tower');
  expect(screen.getByPlaceholderText('Country').value).toBe('France');
  expect(screen.getByPlaceholderText('Region / State / Province').value).toBe('Île-de-France');
  expect(document.querySelector('input[type="date"]').value).toBe('2024-06-01');
  expect(screen.getByPlaceholderText('Notes').value).toBe('Amazing view');
});

// FR2 — date input is empty when pin has no visitDate
test('FR2 — date input is empty when pin has no visitDate', () => {
  // Arrange
  const pinWithoutDate = { ...mockPin, visitDate: null };
  render(<EditPinForm pin={pinWithoutDate} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(document.querySelector('input[type="date"]').value).toBe('');
});

// FR2 — valid save: locationName filled, onSave called with correct fields
test('FR2 — valid save delegates correct fields to onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Act
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).toHaveBeenCalledTimes(1);
  expect(onSave).toHaveBeenCalledWith({
    locationName: 'Eiffel Tower',
    country: 'France',
    region: 'Île-de-France',
    visitDate: '2024-06-01',
    notes: 'Amazing view',
    mediaFile: null,
  });
});

// FR2 — valid save with all fields edited: onSave receives updated values
test('FR2 — valid save with all fields edited calls onSave with new values', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Act
  fireEvent.change(screen.getByPlaceholderText('Location name'), { target: { value: 'Louvre Museum' } });
  fireEvent.change(screen.getByPlaceholderText('Country'), { target: { value: 'France' } });
  fireEvent.change(screen.getByPlaceholderText('Region / State / Province'), { target: { value: 'Paris' } });
  fireEvent.change(screen.getByPlaceholderText('Notes'), { target: { value: 'Great art' } });
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).toHaveBeenCalledWith({
    locationName: 'Louvre Museum',
    country: 'France',
    region: 'Paris',
    visitDate: '2024-06-01',
    notes: 'Great art',
    mediaFile: null,
  });
});

// FR2 — invalid save: empty locationName — onSave must NOT be called
test('FR2 — invalid save with empty locationName does not call onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

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
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

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
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={onCancel} onPrivacyChange={jest.fn()} />);

  // Act
  fireEvent.click(screen.getByText('Cancel'));

  // Assert
  expect(onCancel).toHaveBeenCalledTimes(1);
  expect(onSave).not.toHaveBeenCalled();
});

// ── FR11: Pin Privacy ─────────────────────────────────────────────────────────

// FR11 — dropdown defaults to PRIVATE when pin has no privacyLevel
test('FR11 — dropdown defaults to PRIVATE when pin has no privacyLevel', () => {
  // Arrange
  const pinWithoutPrivacy = { ...mockPin, privacyLevel: undefined };
  render(<EditPinForm pin={pinWithoutPrivacy} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(screen.getByDisplayValue('Private')).toBeInTheDocument();
});

// FR11 — dropdown pre-fills with pin's existing privacyLevel
test('FR11 — dropdown pre-fills with existing privacyLevel from pin', () => {
  // Arrange
  const pinWithPrivacy = { ...mockPin, privacyLevel: 'PUBLIC' };
  render(<EditPinForm pin={pinWithPrivacy} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(screen.getByDisplayValue('Public')).toBeInTheDocument();
});

// FR11 — all three privacy options are present in the dropdown
test('FR11 — all three privacy options render in the dropdown', () => {
  // Arrange
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert
  expect(screen.getByText('Private')).toBeInTheDocument();
  expect(screen.getByText('Friends Only')).toBeInTheDocument();
  expect(screen.getByText('Public')).toBeInTheDocument();
});

// FR11 — changing the dropdown calls onPrivacyChange immediately with new value
test('FR11 — changing dropdown calls onPrivacyChange immediately with new value', () => {
  // Arrange
  const onPrivacyChange = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={onPrivacyChange} />);

  // Act
  fireEvent.change(screen.getByDisplayValue('Private'), { target: { value: 'PUBLIC' } });

  // Assert
  expect(onPrivacyChange).toHaveBeenCalledTimes(1);
  expect(onPrivacyChange).toHaveBeenCalledWith('PUBLIC');
});

// FR11 — changing the dropdown does NOT call onSave
test('FR11 — changing dropdown does not call onSave', () => {
  // Arrange
  const onSave = jest.fn();
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Act
  fireEvent.change(screen.getByDisplayValue('Private'), { target: { value: 'FRIENDS_ONLY' } });

  // Assert
  expect(onSave).not.toHaveBeenCalled();
});

// ── FR7: Media Display, Replace, and Remove ───────────────────────────────────

// FR7 — existing media renders when pin has mediaUrl
test('FR7 — existing media renders when pin has mediaUrl', () => {
  // Arrange
  const pinWithMedia = { ...mockPin, mediaUrl: '/api/media/abc.jpg' };
  render(<EditPinForm pin={pinWithMedia} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} onRemoveMedia={jest.fn()} />);

  // Assert — image should be in the document with the correct src
  const img = document.querySelector('img');
  expect(img).toBeInTheDocument();
  expect(img.getAttribute('src')).toBe('/api/media/abc.jpg');
});

// FR7 — no media display when pin has no mediaUrl
test('FR7 — no media display renders when pin has no mediaUrl', () => {
  // Arrange — mockPin has no mediaUrl by default
  render(<EditPinForm pin={mockPin} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} onRemoveMedia={jest.fn()} />);

  // Assert — no img tag should render
  expect(document.querySelector('img')).not.toBeInTheDocument();
  // And no Remove media button either
  expect(screen.queryByText('Remove media')).not.toBeInTheDocument();
});

// FR7 — Remove media button renders when both mediaUrl and onRemoveMedia are present
test('FR7 — Remove media button calls onRemoveMedia when clicked', () => {
  // Arrange
  const onRemoveMedia = jest.fn();
  const pinWithMedia = { ...mockPin, mediaUrl: '/api/media/abc.jpg' };
  render(<EditPinForm pin={pinWithMedia} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} onRemoveMedia={onRemoveMedia} />);

  // Act
  fireEvent.click(screen.getByText('Remove media'));

  // Assert
  expect(onRemoveMedia).toHaveBeenCalledTimes(1);
});

// FR7 — Remove media button does NOT render when onRemoveMedia prop is missing
test('FR7 — Remove media button does not render when onRemoveMedia is omitted', () => {
  // Arrange — pin has media but no onRemoveMedia handler passed
  const pinWithMedia = { ...mockPin, mediaUrl: '/api/media/abc.jpg' };
  render(<EditPinForm pin={pinWithMedia} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} />);

  // Assert — image still shows, but no remove button
  expect(document.querySelector('img')).toBeInTheDocument();
  expect(screen.queryByText('Remove media')).not.toBeInTheDocument();
});

// FR7 — picking a new file hides existing media and shows local preview
test('FR7 — picking a new file replaces existing media display', () => {
  // Arrange
  const file = new File(['fake content'], 'newphoto.jpg', { type: 'image/jpeg' });
  const pinWithMedia = { ...mockPin, mediaUrl: '/api/media/old.jpg' };
  render(<EditPinForm pin={pinWithMedia} onSave={jest.fn()} onCancel={jest.fn()} onPrivacyChange={jest.fn()} onRemoveMedia={jest.fn()} />);

  // Verify existing media is shown initially
  expect(document.querySelector('img').getAttribute('src')).toBe('/api/media/old.jpg');

  // Act — pick a new file
  const fileInput = document.querySelector('input[type="file"]');
  fireEvent.change(fileInput, { target: { files: [file] } });

  // Assert — Remove media button (for server-side delete) is gone, Remove file button (local) is present
  expect(screen.queryByText('Remove media')).not.toBeInTheDocument();
  expect(screen.getByText('Remove file')).toBeInTheDocument();
});

// FR7 — save with mediaFile picked passes it through to onSave
test('FR7 — save with picked file includes mediaFile in onSave payload', () => {
  // Arrange
  const onSave = jest.fn();
  const file = new File(['fake content'], 'photo.jpg', { type: 'image/jpeg' });
  render(<EditPinForm pin={mockPin} onSave={onSave} onCancel={jest.fn()} onPrivacyChange={jest.fn()} onRemoveMedia={jest.fn()} />);

  // Act — pick a file, then save
  const fileInput = document.querySelector('input[type="file"]');
  fireEvent.change(fileInput, { target: { files: [file] } });
  fireEvent.click(screen.getByText('Save Changes'));

  // Assert
  expect(onSave).toHaveBeenCalledWith({
    locationName: 'Eiffel Tower',
    country: 'France',
    region: 'Île-de-France',
    visitDate: '2024-06-01',
    notes: 'Amazing view',
    mediaFile: file,
  });
});
