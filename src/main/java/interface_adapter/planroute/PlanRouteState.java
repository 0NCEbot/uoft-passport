package interface_adapter.planroute;

import java.util.ArrayList;
import java.util.List;

public class PlanRouteState {

    public static class StepVM {
        public String instruction;
        public String distance;
        public String duration;
        public String landmarkName;
        public boolean isLandmark;
        public boolean completed;
    }

    private String startLocation = "";
    private String destination = "";
    private List<StepVM> steps = new ArrayList<>();
    private String totalDistance = "";
    private String totalDuration = "";
    private boolean manualMode = false;
    private String errorMessage = null;
    private int currentStepIndex = 0;
    private byte[] mapImageBytes = null;  // NEW

    public PlanRouteState() {}

    public PlanRouteState(PlanRouteState copy) {
        this.startLocation = copy.startLocation;
        this.destination = copy.destination;
        this.steps = new ArrayList<>(copy.steps);
        this.totalDistance = copy.totalDistance;
        this.totalDuration = copy.totalDuration;
        this.manualMode = copy.manualMode;
        this.errorMessage = copy.errorMessage;
        this.currentStepIndex = copy.currentStepIndex;
        this.mapImageBytes = copy.mapImageBytes;
    }

    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String s) { this.startLocation = s; }

    public String getDestination() { return destination; }
    public void setDestination(String d) { this.destination = d; }

    public List<StepVM> getSteps() { return steps; }
    public void setSteps(List<StepVM> s) { this.steps = s; }

    public String getTotalDistance() { return totalDistance; }
    public void setTotalDistance(String d) { this.totalDistance = d; }

    public String getTotalDuration() { return totalDuration; }
    public void setTotalDuration(String d) { this.totalDuration = d; }

    public boolean isManualMode() { return manualMode; }
    public void setManualMode(boolean m) { this.manualMode = m; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String e) { this.errorMessage = e; }

    public int getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(int i) { this.currentStepIndex = i; }

    public byte[] getMapImageBytes() { return mapImageBytes; }  // NEW
    public void setMapImageBytes(byte[] bytes) { this.mapImageBytes = bytes; }  // NEW

    public StepVM getCurrentStep() {
        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            return steps.get(currentStepIndex);
        }
        return null;
    }

    public boolean isRouteCompleted() {
        return currentStepIndex >= steps.size();
    }
}