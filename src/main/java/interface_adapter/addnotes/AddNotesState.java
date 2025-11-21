package interface_adapter.addnotes;

public class AddNotesState {
    private String username = "";
    private String landmarkName = "";

    // NEW fields for right-side landmark card
    private String landmarkDescription = "";
    private String address = "";
    private String openHours = "";

    // input from user
    private String content = "";

    private String errorMessage = null;
    private String successMessage = null;

    public AddNotesState() {}

    public AddNotesState(AddNotesState copy) {
        this.username = copy.username;
        this.landmarkName = copy.landmarkName;

        this.landmarkDescription = copy.landmarkDescription;
        this.address = copy.address;
        this.openHours = copy.openHours;

        this.content = copy.content;
        this.errorMessage = copy.errorMessage;
        this.successMessage = copy.successMessage;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLandmarkName() { return landmarkName; }
    public void setLandmarkName(String landmarkName) { this.landmarkName = landmarkName; }

    public String getLandmarkDescription() { return landmarkDescription; }
    public void setLandmarkDescription(String landmarkDescription) { this.landmarkDescription = landmarkDescription; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpenHours() { return openHours; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getSuccessMessage() { return successMessage; }
    public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }
}
