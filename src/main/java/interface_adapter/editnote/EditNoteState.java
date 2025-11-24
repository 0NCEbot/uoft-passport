package interface_adapter.editnote;

public class EditNoteState {
    private String noteId = "";
    private String content = "";
    private String error = null;

    public EditNoteState() {}

    public EditNoteState(EditNoteState copy) {
        this.noteId = copy.noteId;
        this.content = copy.content;
        this.error = copy.error;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}