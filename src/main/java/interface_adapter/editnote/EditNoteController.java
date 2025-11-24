package interface_adapter.editnote;

import use_case.editnote.EditNoteInputBoundary;
import use_case.editnote.EditNoteInputData;

public class EditNoteController {
    private final EditNoteInputBoundary editNoteInteractor;

    public EditNoteController(EditNoteInputBoundary editNoteInteractor) {
        this.editNoteInteractor = editNoteInteractor;
    }

    /**
     * Executes the edit note use case
     * @param noteId the ID of the note to edit
     * @param newContent the new content for the note
     */
    public void execute(String noteId, String newContent) {
        EditNoteInputData inputData = new EditNoteInputData(noteId, newContent);
        editNoteInteractor.execute(inputData);
    }
}