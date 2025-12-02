package use_case.editnote;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EditNoteInteractorTest {

    private TestDataAccess dataAccess;
    private TestPresenter presenter;
    private EditNoteInteractor interactor;

    @BeforeEach
    void setUp() {
        dataAccess = new TestDataAccess();
        presenter = new TestPresenter();
        interactor = new EditNoteInteractor(dataAccess, presenter);
    }

    @Test
    void testSuccess() {
        // Setup
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test Landmark", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        // Execute
        interactor.execute(new EditNoteInputData("N1", "Updated"));

        // Verify
        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("Updated", presenter.outputData.getUpdatedContent());
    }

    @Test
    void testNoteNotFound() {
        interactor.execute(new EditNoteInputData("FAKE", "Content"));

        assertTrue(presenter.failCalled);
        assertEquals("Note not found.", presenter.errorMessage);
    }

    @Test
    void testNullContent() {
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData("N1", null));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    @Test
    void testEmptyContent() {
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData("N1", ""));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    @Test
    void testWhitespaceOnly() {
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData("N1", "   "));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    @Test
    void testContentTooLong() {
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        String tooLong = "a".repeat(501);
        interactor.execute(new EditNoteInputData("N1", tooLong));

        assertTrue(presenter.failCalled);
        assertEquals("Note content too long (max 500 characters).", presenter.errorMessage);
    }

    @Test
    void testExactly500Characters() {
        Location location = new Location(43.6629, -79.3957);
        LandmarkInfo info = new LandmarkInfo("123 St", "Desc", "9-5", "Museum");
        Landmark landmark = new Landmark("L1", "Test", location, info, 0);
        Note note = new Note("N1", landmark, "Original", Instant.now(), Instant.now());
        dataAccess.addNote(note);

        String exactly500 = "a".repeat(500);
        interactor.execute(new EditNoteInputData("N1", exactly500));

        assertTrue(presenter.successCalled);
        assertEquals(exactly500, presenter.outputData.getUpdatedContent());
    }

    // Mock Data Access
    private static class TestDataAccess implements EditNoteDataAccessInterface {
        private final Map<String, Note> notes = new HashMap<>();

        void addNote(Note note) {
            notes.put(note.getNoteId(), note);
        }

        @Override
        public boolean noteExists(String noteId) {
            return notes.containsKey(noteId);
        }

        @Override
        public Note getNoteById(String noteId) {
            return notes.get(noteId);
        }

        @Override
        public void updateNote(Note note) {
            notes.put(note.getNoteId(), note);
        }
    }

    // Mock Presenter
    private static class TestPresenter implements EditNoteOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        EditNoteOutputData outputData;
        String errorMessage;

        @Override
        public void prepareSuccessView(EditNoteOutputData outputData) {
            this.successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String error) {
            this.failCalled = true;
            this.errorMessage = error;
        }
    }
}