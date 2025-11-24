package use_case.editnote;

import java.time.Instant;

public class EditNoteOutputData {
    private final String noteId;
    private final String updatedContent;
    private final Instant updatedAt;
    private final boolean success;

    public EditNoteOutputData(String noteId, String updatedContent, Instant updatedAt, boolean success) {
        this.noteId = noteId;
        this.updatedContent = updatedContent;
        this.updatedAt = updatedAt;
        this.success = success;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getUpdatedContent() {
        return updatedContent;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isSuccess() {
        return success;
    }
}