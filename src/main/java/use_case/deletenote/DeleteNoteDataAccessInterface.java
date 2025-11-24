package use_case.deletenote;

import entity.Note;

public interface DeleteNoteDataAccessInterface {
    /**
     * Gets a note by its ID
     * @param noteId the note's unique identifier
     * @return the Note object, or null if not found
     */
    Note getNoteById(String noteId);

    /**
     * Deletes a note
     * @param noteId the note's unique identifier
     */
    void deleteNote(String noteId);

    /**
     * Checks if a note exists
     * @param noteId the note's unique identifier
     * @return true if the note exists, false otherwise
     */
    boolean noteExists(String noteId);
}
