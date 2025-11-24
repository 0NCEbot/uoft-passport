package use_case.deletenote;

public class DeleteNoteOutputData {
    private final String noteId;
    private final String landmarkName;
    private final boolean success;

    public DeleteNoteOutputData(String noteId, String landmarkName, boolean success) {
        this.noteId = noteId;
        this.landmarkName = landmarkName;
        this.success = success;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public boolean isSuccess() {
        return success;
    }
}