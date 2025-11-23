package data_access;

import entity.Location;
import entity.RouteStep;
import java.util.List;

public interface RouteDataAccessInterface {

    RouteResponse getRoute(String start, String destination, String[] intermediates);

    /**
     * Fetch a static map image showing the route.
     * @param encodedPolyline The encoded polyline from the route response
     * @param start Start location coordinates
     * @param end End location coordinates
     * @param intermediates Optional intermediate waypoints
     * @return Image bytes (PNG format), or null if fetch fails
     */
    byte[] getStaticMapImage(String encodedPolyline, Location start, Location end, List<Location> intermediates);

    class RouteResponse {
        private final List<RouteStep> steps;
        private final int totalDistanceMeters;
        private final int totalDurationSeconds;
        private final boolean successful;
        private final String errorMessage;
        private final boolean manualMode;
        private final String encodedPolyline;
        private final Location startLocation;
        private final Location endLocation;
        private final List<Location> intermediateLocations;

        public RouteResponse(List<RouteStep> steps, int dist, int duration, boolean success) {
            this(steps, dist, duration, success, null, false, null, null, null, null);
        }

        public RouteResponse(List<RouteStep> steps, int dist, int duration, boolean success, String errorMessage) {
            this(steps, dist, duration, success, errorMessage, false, null, null, null, null);
        }

        public RouteResponse(List<RouteStep> steps, int dist, int duration, boolean success,
                             String errorMessage, boolean manualMode) {
            this(steps, dist, duration, success, errorMessage, manualMode, null, null, null, null);
        }

        // Full constructor
        public RouteResponse(List<RouteStep> steps, int dist, int duration, boolean success,
                             String errorMessage, boolean manualMode, String encodedPolyline,
                             Location startLocation, Location endLocation, List<Location> intermediateLocations) {
            this.steps = steps;
            this.totalDistanceMeters = dist;
            this.totalDurationSeconds = duration;
            this.successful = success;
            this.errorMessage = errorMessage;
            this.manualMode = manualMode;
            this.encodedPolyline = encodedPolyline;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.intermediateLocations = intermediateLocations;
        }

        public List<RouteStep> getSteps() { return steps; }
        public int getTotalDistanceMeters() { return totalDistanceMeters; }
        public int getTotalDurationSeconds() { return totalDurationSeconds; }
        public boolean isSuccessful() { return successful; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isManualMode() { return manualMode; }
        public String getEncodedPolyline() { return encodedPolyline; }
        public Location getStartLocation() { return startLocation; }
        public Location getEndLocation() { return endLocation; }
        public List<Location> getIntermediateLocations() { return intermediateLocations; }
    }
}