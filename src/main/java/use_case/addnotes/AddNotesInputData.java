// src/java/use_case/notes/AddNoteInputData.java
package use_case.addnotes;

public class AddNotesInputData {
    private final String username;
    private final String landmarkName;
    private final String content;

    public AddNotesInputData(String username, String landmarkName, String content) {
        this.username = username;
        this.landmarkName = landmarkName;
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public String getContent() {
        return content;
    }
}
