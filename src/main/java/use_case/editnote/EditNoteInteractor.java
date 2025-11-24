package use_case.editnote;

import entity.Note;

public class EditNoteInteractor implements EditNoteInputBoundary {
    private final EditNoteDataAccessInterface dataAccess;
    private final EditNoteOutputBoundary outputBoundary;

    public EditNoteInteractor(EditNoteDataAccessInterface dataAccess,
                              EditNoteOutputBoundary outputBoundary) {
        this.dataAccess = dataAccess;
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute(EditNoteInputData inputData) {
        // Validate note exists
        if (!dataAccess.noteExists(inputData.getNoteId())) {
            outputBoundary.prepareFailView("Note not found.");
            return;
        }

        // Get the existing note
        Note existingNote = dataAccess.getNoteById(inputData.getNoteId());

        // Validate content
        String newContent = inputData.getNewContent();
        if (newContent == null || newContent.trim().isEmpty()) {
            outputBoundary.prepareFailView("Note content cannot be empty.");
            return;
        }

        if (newContent.length() > 500) {
            outputBoundary.prepareFailView("Note content too long (max 500 characters).");
            return;
        }

        // Create updated note using the immutable pattern
        Note updatedNote = existingNote.withUpdatedContent(newContent);

        // Save the updated note
        dataAccess.updateNote(updatedNote);

        // Prepare success response
        EditNoteOutputData outputData = new EditNoteOutputData(
                updatedNote.getNoteId(),
                updatedNote.getContent(),
                updatedNote.getUpdatedAt(),
                true
        );

        outputBoundary.prepareSuccessView(outputData);
    }
}