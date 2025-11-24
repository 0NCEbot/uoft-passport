package use_case.editnote;

public class EditNoteInputData {
    private final String noteId;
    private final String newContent;

    public EditNoteInputData(String noteId, String newContent) {
        this.noteId = noteId;
        this.newContent = newContent;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getNewContent() {
        return newContent;
    }
}