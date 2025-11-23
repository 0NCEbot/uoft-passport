package use_case.planroute;

import java.util.List;

public class PlanRouteOutputData {

    public static class RouteStepDTO {
        public final String instruction;
        public final int distanceMeters;
        public final int durationSeconds;
        public final String landmarkName;  // Non-null if this is a landmark step
        public final boolean isLandmark;

        public RouteStepDTO(String instruction, int distanceMeters,
                            int durationSeconds, String landmarkName, boolean isLandmark) {
            this.instruction = instruction;
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.landmarkName = landmarkName;
            this.isLandmark = isLandmark;
        }
    }

    private final String startLocation;
    private final String destination;
    private final List<RouteStepDTO> steps;
    private final int totalDistanceMeters;
    private final int totalDurationSeconds;
    private final String errorMessage;
    private final boolean success;
    private final boolean manualMode;

    public PlanRouteOutputData(String startLocation, String destination,
                               List<RouteStepDTO> steps, int totalDist,
                               int totalDuration, String error, boolean success,
                               boolean manualMode) {
        this.startLocation = startLocation;
        this.destination = destination;
        this.steps = steps;
        this.totalDistanceMeters = totalDist;
        this.totalDurationSeconds = totalDuration;
        this.errorMessage = error;
        this.success = success;
        this.manualMode = manualMode;
    }

    public String getStartLocation() { return startLocation; }
    public String getDestination() { return destination; }
    public List<RouteStepDTO> getSteps() { return steps; }
    public int getTotalDistanceMeters() { return totalDistanceMeters; }
    public int getTotalDurationSeconds() { return totalDurationSeconds; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSuccess() { return success; }
    public boolean isManualMode() { return manualMode; }
}