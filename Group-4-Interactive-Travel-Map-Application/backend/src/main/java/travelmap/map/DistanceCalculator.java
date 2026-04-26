package travelmap.map;

/**
 * DistanceCalculator — utility class for the Haversine great-circle distance.
 *
 * Pure math, no Spring annotations, no DB access, no HTTP concerns. Lives in
 * travelmap.map alongside MapAPIClient because both are infrastructure for
 * map-related computation. Stateless — every method is static.
 *
 * The Haversine formula computes the shortest distance between two points on
 * the surface of a sphere (the Earth, modelled as a perfect sphere of radius
 * 6371 km). It is accurate enough for travel-map use cases — error vs the
 * true ellipsoidal Earth is under 0.5% for any pair of points.
 *
 * SRS coverage:
 *   FR8 — pin-to-pin, total trip, and accumulated travel distance.
 *
 * NFR2 — pure arithmetic, completes in microseconds. Well within the
 *        2.5-second response time requirement.
 */
public final class DistanceCalculator {

    /** Earth's mean radius in kilometres (used by Haversine). */
    public static final double EARTH_RADIUS_KM = 6371.0;

    /** Earth's mean radius in miles (used for the mi conversion in responses). */
    public static final double EARTH_RADIUS_MI = 3958.8;

    /**
     * Private constructor — this is a utility class and must not be instantiated.
     */
    private DistanceCalculator() {
        throw new AssertionError("DistanceCalculator is a utility class and cannot be instantiated");
    }

    /**
     * FR8 — Compute the great-circle distance between two points in kilometres.
     *
     * Uses the Haversine formula:
     *   a = sin²(Δφ/2) + cos(φ₁) · cos(φ₂) · sin²(Δλ/2)
     *   c = 2 · atan2(√a, √(1−a))
     *   d = R · c
     *
     * @param lat1 latitude of the first point in degrees, in range [-90, 90]
     * @param lon1 longitude of the first point in degrees, in range [-180, 180]
     * @param lat2 latitude of the second point in degrees, in range [-90, 90]
     * @param lon2 longitude of the second point in degrees, in range [-180, 180]
     * @return the distance in kilometres, always non-negative
     * @throws IllegalArgumentException if any coordinate is null or out of range
     */
    public static double haversineKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        validateCoordinate(lat1, lon1);
        validateCoordinate(lat2, lon2);

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0)
                 + Math.cos(phi1) * Math.cos(phi2)
                 * Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Convert a distance value from kilometres to miles.
     *
     * @param km distance in kilometres, must be non-negative
     * @return equivalent distance in miles
     * @throws IllegalArgumentException if km is negative
     */
    public static double kmToMiles(double km) {
        if (km < 0.0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + km);
        }
        return km * (EARTH_RADIUS_MI / EARTH_RADIUS_KM);
    }

    /**
     * Validate that a single (latitude, longitude) pair is non-null and in range.
     *
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     * @throws IllegalArgumentException if either value is null or out of range
     */
    private static void validateCoordinate(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude must not be null");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude out of range [-90, 90]: " + lat);
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("Longitude out of range [-180, 180]: " + lon);
        }
    }
}
