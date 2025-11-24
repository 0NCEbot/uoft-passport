package data_access;

import entity.Note;
import entity.Landmark;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of note data access
 * Stores notes in a HashMap
 */
public class InMemoryNoteDataAccessObject implements NoteDataAccessInterface {

    // Store notes by noteId
    private final Map<String, Note> notes = new HashMap<>();

    // ============ ADD NOTE (you might already have this) ============

    @Override
    public void saveNote(Note note) {
        notes.put(note.getNoteId(), note);
    }

    // ============ EDIT NOTE ============

    @Override
    public Note getNoteById(String noteId) {
        return notes.get(noteId);
    }

    @Override
    public void updateNote(Note note) {
        // Replace the old note with the updated one
        notes.put(note.getNoteId(), note);
    }

    @Override
    public boolean noteExists(String noteId) {
        return notes.containsKey(noteId);
    }

    // ============ DELETE NOTE ============

    @Override
    public void deleteNote(String noteId) {
        notes.remove(noteId);
    }

    // ============ QUERY METHODS ============

    @Override
    public List<Note> getNotesForLandmark(String landmarkId) {
        return notes.values().stream()
                .filter(note -> note.getLandmark().getLandmarkName().equals(landmarkId))
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> getAllNotes() {
        return notes.values().stream()
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }
}