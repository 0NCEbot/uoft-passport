package use_case.deletenote;

public class DeleteNoteInputData {
    private final String noteId;

    public DeleteNoteInputData(String noteId) {
        this.noteId = noteId;
    }

    public String getNoteId() {
        return noteId;
    }
}