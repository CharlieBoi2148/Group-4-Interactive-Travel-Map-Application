import { fireEvent, render, screen } from '@testing-library/react';
import FilterSearch from './components/FilterSearch';

test('FR9 — submits trimmed keyword to onSearch', () => {
  const onSearch = jest.fn();
  render(<FilterSearch onSearch={onSearch} />);

  fireEvent.change(screen.getByPlaceholderText('Enter location name'), {
    target: { value: '  eiffel  ' },
  });
  fireEvent.click(screen.getByRole('button', { name: 'Search' }));

  expect(onSearch).toHaveBeenCalledWith({ keyword: 'eiffel', tripId: null });
});

test('FR9 — submits trip filter with keyword', () => {
  const onSearch = jest.fn();
  render(
    <FilterSearch
      onSearch={onSearch}
      trips={[
        { id: 1, name: 'Europe' },
        { id: 2, name: 'Asia' },
      ]}
    />
  );

  fireEvent.change(screen.getByTestId('filter-search-trip'), { target: { value: '2' } });
  fireEvent.change(screen.getByPlaceholderText('Enter location name'), {
    target: { value: 'temple' },
  });
  fireEvent.click(screen.getByRole('button', { name: 'Search' }));

  expect(onSearch).toHaveBeenCalledWith({ keyword: 'temple', tripId: 2 });
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
  expect(screen.getByTestId('filter-search-trip')).toHaveValue('');
});
