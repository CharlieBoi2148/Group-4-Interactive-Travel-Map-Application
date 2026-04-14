// --- MAP SERVICE (MapAPIClient.java equivalent) --------------------------------
// Frontend equivalent of MapAPIClient implementing MapService interface.
// Responsible for configuring the map library.
// In the full architecture this is called once at app startup before any
// map components are rendered.

import L from 'leaflet';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});
