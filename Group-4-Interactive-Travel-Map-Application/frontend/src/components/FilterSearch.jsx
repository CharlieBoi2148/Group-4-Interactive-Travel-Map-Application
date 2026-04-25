// --- VIEW: FILTER SEARCH (FR9 — Filter/Search Pins) --------------------------
// Pure View — captures filter input and delegates search/clear actions to App.js.
// This component does not call fetch() or mutate backend state directly.

import { useState } from 'react';

/**
 * @param {Object} props
 * @param {function(Object):void} props.onSearch called with { keyword }
 * @param {function():void} [props.onClear] clears active filter/search state
 * @param {boolean} [props.isLoading] true while search request is pending
 * @param {string|null} [props.error] optional error message to display
 * @param {number|null} [props.resultCount] optional count shown after search
 */
export default function FilterSearch({
  onSearch,
  onClear,
  isLoading = false,
  error = null,
  resultCount = null,
}) {
  const [keyword, setKeyword] = useState('');

  const handleSubmit = (event) => {
    event.preventDefault();
    if (typeof onSearch === 'function') {
      onSearch({ keyword: keyword.trim() });
    }
  };

  const handleClear = () => {
    setKeyword('');
    if (typeof onClear === 'function') {
      onClear();
    } else if (typeof onSearch === 'function') {
      // Fallback: empty keyword means "show all" for FR9 flow.
      onSearch({ keyword: '' });
    }
  };

  return (
    <section
      data-testid="filter-search"
      style={{
        border: '1px solid #e0e0e0',
        borderRadius: '8px',
        padding: '10px',
        background: '#fff',
        marginBottom: '12px',
      }}
    >
      <h3 style={{ margin: '0 0 8px', fontSize: '16px' }}>Filter/Search Pins</h3>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Search by location keyword"
          aria-label="Search pins by location keyword"
          style={{
            width: '100%',
            padding: '8px',
            borderRadius: '6px',
            border: '1px solid #ccc',
            boxSizing: 'border-box',
          }}
        />

        <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
          <button
            type="submit"
            disabled={isLoading}
            style={{
              flex: 1,
              padding: '8px',
              border: 'none',
              borderRadius: '6px',
              cursor: isLoading ? 'default' : 'pointer',
              background: '#1D9E75',
              color: '#fff',
              fontWeight: 600,
              opacity: isLoading ? 0.8 : 1,
            }}
          >
            {isLoading ? 'Searching...' : 'Search'}
          </button>
          <button
            type="button"
            onClick={handleClear}
            disabled={isLoading}
            style={{
              flex: 1,
              padding: '8px',
              border: '1px solid #ccc',
              borderRadius: '6px',
              cursor: isLoading ? 'default' : 'pointer',
              background: '#f5f5f5',
              color: '#333',
              fontWeight: 600,
              opacity: isLoading ? 0.8 : 1,
            }}
          >
            Clear
          </button>
        </div>
      </form>

      {error ? (
        <p
          role="alert"
          style={{
            margin: '8px 0 0',
            fontSize: '12px',
            color: '#b00020',
            background: '#ffebee',
            borderRadius: '6px',
            padding: '6px 8px',
          }}
        >
          {error}
        </p>
      ) : null}

      {resultCount != null ? (
        <p style={{ margin: '8px 0 0', fontSize: '12px', color: '#555' }}>
          {resultCount} result{resultCount === 1 ? '' : 's'}
        </p>
      ) : null}
    </section>
  );
}
