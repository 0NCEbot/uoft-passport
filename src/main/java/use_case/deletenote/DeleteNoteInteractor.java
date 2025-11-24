package use_case.deletenote;

import entity.Note;

public class DeleteNoteInteractor implements DeleteNoteInputBoundary {
    private final DeleteNoteDataAccessInterface dataAccess;
    private final DeleteNoteOutputBoundary outputBoundary;

    public DeleteNoteInteractor(DeleteNoteDataAccessInterface dataAccess,
                                DeleteNoteOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(DeleteNoteInputData inputData) {
        // Validate note exists
        if (!dataAccess.noteExists(inputData.getNoteId())) {
            outputBoundary.prepareFailView("Note not found.");
            return;
        }

        // Get the note to get landmark info for the success message
        Note note = dataAccess.getNoteById(inputData.getNoteId());
        String landmarkName = note.getLandmark().getLandmarkName();

        // Delete the note
        dataAccess.deleteNote(inputData.getNoteId());

        // Prepare success response
        DeleteNoteOutputData outputData = new DeleteNoteOutputData(
                inputData.getNoteId(),
                landmarkName,
                true
        );

        outputBoundary.prepareSuccessView(outputData);
    }
}