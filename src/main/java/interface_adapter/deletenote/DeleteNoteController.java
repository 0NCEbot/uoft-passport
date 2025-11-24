package interface_adapter.deletenote;

import use_case.deletenote.DeleteNoteInputBoundary;
import use_case.deletenote.DeleteNoteInputData;

public class DeleteNoteController {
    private final DeleteNoteInputBoundary deleteNoteInteractor;

    public DeleteNoteController(DeleteNoteInputBoundary deleteNoteInteractor) {
        this.deleteNoteInteractor = deleteNoteInteractor;
    }

    /**
     * Executes the delete note use case
     * @param noteId the ID of the note to delete
     */
    public void execute(String noteId) {
        DeleteNoteInputData inputData = new DeleteNoteInputData(noteId);
        deleteNoteInteractor.execute(inputData);
    }
}