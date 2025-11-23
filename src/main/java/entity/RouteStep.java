package entity;

public class RouteStep {
    private final int index;
    private final String instruction;
    private final int distance;
    private final int duration;

    public RouteStep(int index, String instruction, int distance, int duration) {
        this.index = index;
        this.instruction = instruction;
        this.distance = distance;
        this.duration = duration;
    }

    public int getIndex() { return index; }
    public String getInstruction() { return instruction; }
    public int getDistance() { return distance; }
    public int getDuration() { return duration; }

    /**
     * Check if this step is a landmark check-in point.
     * Landmark steps are identified by the 📍 emoji prefix.
     */
    public boolean isLandmark() {
        return instruction != null && instruction.startsWith("📍 ");
    }

    /**
     * Get the landmark name (without the emoji prefix).
     * Returns null if this is not a landmark step.
     */
    public String getLandmarkName() {
        if (isLandmark()) {
            return instruction.substring(2).trim(); // Skip "📍 "
        }
        return null;
    }
}