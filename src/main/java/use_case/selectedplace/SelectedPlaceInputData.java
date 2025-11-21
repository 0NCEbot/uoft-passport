package use_case.selectedplace;

public class SelectedPlaceInputData {
    private final String username;
    private final String landmarkName;

    public SelectedPlaceInputData(String username, String landmarkName) {
        this.username = username;
        this.landmarkName = landmarkName;
    }

    public String getUsername() {
        return username;
    }

    public String getLandmarkName() {
        return landmarkName;
    }
}
