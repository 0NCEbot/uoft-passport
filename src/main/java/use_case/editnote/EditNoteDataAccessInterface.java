package use_case.editnote;

import entity.Note;

public interface EditNoteDataAccessInterface {
    /**
     * Gets a note by its ID
     * @param noteId the note's unique identifier
     * @return the Note object, or null if not found
     */
    Note getNoteById(String noteId);

    /**
     * Updates a note (replaces the old note with the new one)
     * @param note the updated Note object
     */
    void updateNote(Note note);

    /**
     * Checks if a note exists
     * @param noteId the note's unique identifier
     * @return true if the note exists, false otherwise
     */
    boolean noteExists(String noteId);
}