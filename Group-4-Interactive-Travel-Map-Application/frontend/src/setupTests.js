// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// JSDOM does not implement URL.createObjectURL or URL.revokeObjectURL.
// MediaPreview calls createObjectURL(file) inside a useEffect whenever a
// File prop is passed. Without this mock the call throws and crashes any test
// that selects a file in a form containing MediaPreview.
global.URL.createObjectURL = jest.fn(() => 'blob:mock');
global.URL.revokeObjectURL = jest.fn();
