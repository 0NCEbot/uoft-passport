// src/java/use_case/notes/AddNoteOutputData.java
package use_case.addnotes;

public class AddNotesOutputData {
    private final String username;
    private final String landmarkName;
    private final String message;

    public AddNotesOutputData(String username, String landmarkName, String message) {
        this.username = username;
        this.landmarkName = landmarkName;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public String getMessage() {
        return message;
    }
}
