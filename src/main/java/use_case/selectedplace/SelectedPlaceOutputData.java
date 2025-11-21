package use_case.selectedplace;

public class SelectedPlaceOutputData {

    private final String username;
    private final String landmarkName;
    private final String description;
    private final String address;
    private final String openHours;

    public SelectedPlaceOutputData(String username,
                                   String landmarkName,
                                   String description,
                                   String address,
                                   String openHours) {
        this.username = username;
        this.landmarkName = landmarkName;
        this.description = description;
        this.address = address;
        this.openHours = openHours;
    }

    public String getUsername() { return username; }
    public String getLandmarkName() { return landmarkName; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getOpenHours() { return openHours; }
}
