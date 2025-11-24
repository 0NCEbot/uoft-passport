public class DeleteNoteState {
    private String noteId = "";
    private String landmarkName = "";
    private String error = null;

    public DeleteNoteState() {}

    public DeleteNoteState(DeleteNoteState copy) {
        this.noteId = copy.noteId;
        this.landmarkName = copy.landmarkName;
        this.error = copy.error;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getLandmarkName() {
        return landmarkName;
    }

    public void setLandmarkName(String landmarkName) {
        this.landmarkName = landmarkName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}