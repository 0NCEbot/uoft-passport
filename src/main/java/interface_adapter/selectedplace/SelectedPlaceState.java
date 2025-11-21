package interface_adapter.selectedplace;

public class SelectedPlaceState {

    private String username = "";
    private String landmarkName = "";
    private String description = "";
    private String address = "";
    private String openHours = "";

    public SelectedPlaceState() {}

    public SelectedPlaceState(SelectedPlaceState copy) {
        this.username = copy.username;
        this.landmarkName = copy.landmarkName;
        this.description = copy.description;
        this.address = copy.address;
        this.openHours = copy.openHours;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLandmarkName() { return landmarkName; }
    public void setLandmarkName(String landmarkName) { this.landmarkName = landmarkName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
}
