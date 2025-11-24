package data_access;

import entity.Note;
import use_case.edit_note.EditNoteDataAccessInterface;
import use_case.delete_note.DeleteNoteDataAccessInterface;

import java.util.List;

/**
 * Interface for Note data access operations
 * Combines all note-related data access methods
 */
public interface NoteDataAccessInterface extends
        EditNoteDataAccessInterface,
        DeleteNoteDataAccessInterface {

    // Add note (you probably already have this)
    void saveNote(Note note);

    // Get notes by landmark
    List<Note> getNotesForLandmark(String landmarkId);

    // Get all notes
    List<Note> getAllNotes();
}