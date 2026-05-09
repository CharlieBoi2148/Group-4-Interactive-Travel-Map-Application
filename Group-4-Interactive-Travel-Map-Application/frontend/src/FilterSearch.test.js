import { fireEvent, render, screen } from '@testing-library/react';
import FilterSearch from './components/FilterSearch';

test('FR9 — submits trimmed keyword to onSearch', () => {
  const onSearch = jest.fn();
  render(<FilterSearch onSearch={onSearch} />);

  fireEvent.change(screen.getByPlaceholderText('Enter location name'), {
    target: { value: '  eiffel  ' },
  });
  fireEvent.click(screen.getByRole('button', { name: 'Search' }));

  expect(onSearch).toHaveBeenCalledWith({ keyword: 'eiffel' });
});

test('FR9 — clear button resets input and calls onClear', () => {
  const onSearch = jest.fn();
  const onClear = jest.fn();
  render(<FilterSearch onSearch={onSearch} onClear={onClear} />);

  fireEvent.change(screen.getByPlaceholderText('Enter location name'), {
    target: { value: 'rome' },
  });
  fireEvent.click(screen.getByText('Clear'));

  expect(onClear).toHaveBeenCalledTimes(1);
  expect(screen.getByPlaceholderText('Enter location name')).toHaveValue('');
});
