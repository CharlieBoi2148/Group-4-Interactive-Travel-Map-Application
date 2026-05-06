import { render, screen, fireEvent } from '@testing-library/react';
import DistancePanel from './components/DistancePanel';

const PIN_A = { id: 1, locationName: 'Paris' };
const PIN_B = { id: 2, locationName: 'London' };
const RESULT = { distanceKm: 341.6, distanceMi: 212.2, skippedPinIds: [] };

// --- RENDER / VISIBILITY PARTITIONS ------------------------------------------

test('allNull_rendersNothing_FR8', () => {
  const { container } = render(
    <DistancePanel
      measuringFrom={null}
      measuredTo={null}
      result={null}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(container.firstChild).toBeNull();
});

test('measuringFromSet_showsWaitingMessage_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText(/Measuring from: Paris/)).toBeInTheDocument();
  expect(screen.getByText(/Click/)).toBeInTheDocument();
});

test('loading_showsCalculatingText_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error={null}
      loading={true}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText('Calculating distance…')).toBeInTheDocument();
});

test('errorSet_showsAlertWithMessage_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error="Could not calculate distance."
      loading={false}
      onCancel={() => {}}
    />
  );
  const alert = screen.getByRole('alert');
  expect(alert).toHaveTextContent('Could not calculate distance.');
});

test('resultSet_showsKmAndMiValues_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={RESULT}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText(/341\.6 km/)).toBeInTheDocument();
  expect(screen.getByText(/212\.2 mi/)).toBeInTheDocument();
});

// --- RESULT DISPLAY PARTITIONS -----------------------------------------------

test('resultSet_showsPinAAndPinBNames_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={RESULT}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText('Paris to London')).toBeInTheDocument();
});

test('resultSet_kmAndMiRenderedToOneDecimalPlace_FR8', () => {
  const result = { distanceKm: 341.6789, distanceMi: 212.2345, skippedPinIds: [] };
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={result}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText(/341\.7 km/)).toBeInTheDocument();
  expect(screen.getByText(/212\.2 mi/)).toBeInTheDocument();
});

test('resultSet_kmAndMiHaveEqualFontStyling_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={RESULT}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  const kmEl = screen.getByText(/341\.6 km/).closest('div');
  const miEl = screen.getByText(/212\.2 mi/).closest('div');
  expect(kmEl.style.fontWeight).toBe(miEl.style.fontWeight);
  expect(kmEl.style.color).toBe(miEl.style.color);
  expect(kmEl.style.fontSize).toBe(miEl.style.fontSize);
});

test('skippedPinIdsEmpty_noWarningShown_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={{ distanceKm: 100, distanceMi: 62.1, skippedPinIds: [] }}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.queryByText(/skipped/)).toBeNull();
});

test('skippedPinIdsPresent_showsAlertWithCount_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={{ distanceKm: 100, distanceMi: 62.1, skippedPinIds: [3, 7] }}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  const alerts = screen.getAllByRole('alert');
  const skippedAlert = alerts.find((el) => el.textContent.includes('skipped'));
  expect(skippedAlert).toBeTruthy();
  expect(skippedAlert).toHaveTextContent('2 pin(s) skipped');
});

// --- CANCEL BUTTON PARTITIONS ------------------------------------------------

test('cancelButton_calledOnClick_waiting_FR8', () => {
  const onCancel = jest.fn();
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error={null}
      loading={false}
      onCancel={onCancel}
    />
  );
  fireEvent.click(screen.getByText('Cancel'));
  expect(onCancel).toHaveBeenCalledTimes(1);
});

test('cancelButton_calledOnClick_result_FR8', () => {
  const onCancel = jest.fn();
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={RESULT}
      error={null}
      loading={false}
      onCancel={onCancel}
    />
  );
  fireEvent.click(screen.getByText('Cancel'));
  expect(onCancel).toHaveBeenCalledTimes(1);
});

test('cancelButton_calledOnClick_error_FR8', () => {
  const onCancel = jest.fn();
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error="Network error"
      loading={false}
      onCancel={onCancel}
    />
  );
  fireEvent.click(screen.getByText('Cancel'));
  expect(onCancel).toHaveBeenCalledTimes(1);
});

test('cancelButton_visibleInWaitingState_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText('Cancel')).toBeInTheDocument();
});

test('cancelButton_visibleInResultState_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={PIN_B}
      result={RESULT}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText('Cancel')).toBeInTheDocument();
});

test('cancelButton_visibleInErrorState_FR8', () => {
  render(
    <DistancePanel
      measuringFrom={PIN_A}
      measuredTo={null}
      result={null}
      error="Something went wrong"
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(screen.getByText('Cancel')).toBeInTheDocument();
});

test('cancelButton_notRenderedWhenPanelNull_FR8', () => {
  const { container } = render(
    <DistancePanel
      measuringFrom={null}
      measuredTo={null}
      result={null}
      error={null}
      loading={false}
      onCancel={() => {}}
    />
  );
  expect(container.firstChild).toBeNull();
  expect(screen.queryByText('Cancel')).toBeNull();
});
