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

/**
 * Tests made:
 * testSuccess
 * testNoteNotFound
 * testNullContent
 * testEmptyContent
 * testWhitespaceOnly
 * testContentTooLong
 * testExactly500Characters
 * testSpecialCharacters
 * testMultipleEdits
 * testContentJustUnderLimit
 */


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

    /**
     * Tests the successful edit of a note with valid content.
     * Verifies that the note content is updated and success view is called.
     */
    @Test
    void testSuccess() {
        // Setup
        Location location = new Location(43.6596, -79.3975);
        LandmarkInfo info = new LandmarkInfo(
                "40 St George St",
                "Computer science and engineering building",
                "24 hours",
                "Academic Building"
        );
        Landmark landmark = new Landmark(
                "bahen-centre",
                "Bahen Centre for Information Technology",
                location,
                info,
                0
        );
        Note note = new Note(
                "Bahen Centre for Information Technology2025-11-30T00:34:24.550208Z",
                landmark,
                "Original content",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        // Execute
        interactor.execute(new EditNoteInputData(
                "Bahen Centre for Information Technology2025-11-30T00:34:24.550208Z",
                "This place was Great!"
        ));

        // Verify
        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("This place was Great!", presenter.outputData.getUpdatedContent());
    }

    /**
     * Tests editing a note that doesn't exist in the system.
     * Verifies that fail view is called with "Note not found." error.
     */
    @Test
    void testNoteNotFound() {
        interactor.execute(new EditNoteInputData(
                "Robarts Library2025-99-99T99:99:99.999999Z",
                "Content"
        ));

        assertTrue(presenter.failCalled);
        assertEquals("Note not found.", presenter.errorMessage);
    }

    /**
     * Tests editing a note with null content.
     * Verifies that fail view is called with "Note content cannot be empty." error.
     */
    @Test
    void testNullContent() {
        Location location = new Location(43.6645, -79.3996);
        LandmarkInfo info = new LandmarkInfo(
                "100 St George St",
                "Large academic library",
                "24 hours",
                "Library"
        );
        Landmark landmark = new Landmark(
                "robarts-library",
                "Robarts Library",
                location,
                info,
                0
        );
        Note note = new Note(
                "Robarts Library2025-11-30T01:08:28.962320Z",
                landmark,
                "great place to study",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData(
                "Robarts Library2025-11-30T01:08:28.962320Z",
                null
        ));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    /**
     * Tests editing a note with empty string content.
     * Verifies that fail view is called with "Note content cannot be empty." error.
     */
    @Test
    void testEmptyContent() {
        Location location = new Location(43.6640, -79.3943);
        LandmarkInfo info = new LandmarkInfo(
                "7 Hart House Circle",
                "Historic student center",
                "8 AM - 11 PM",
                "Student Building"
        );
        Landmark landmark = new Landmark(
                "hart-house",
                "Hart House",
                location,
                info,
                0
        );
        Note note = new Note(
                "Hart House2025-11-24T06:56:13.795618Z",
                landmark,
                "Original content",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData(
                "Hart House2025-11-24T06:56:13.795618Z",
                ""
        ));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    /**
     * Tests editing a note with whitespace-only content.
     * Verifies that fail view is called with "Note content cannot be empty." error.
     */
    @Test
    void testWhitespaceOnly() {
        Location location = new Location(43.6627, -79.3979);
        LandmarkInfo info = new LandmarkInfo(
                "100 St George St",
                "Arts and humanities building",
                "7 AM - 11 PM",
                "Academic Building"
        );
        Landmark landmark = new Landmark(
                "sidney-smith",
                "Sidney Smith Hall",
                location,
                info,
                0
        );
        Note note = new Note(
                "Sidney Smith Hall2025-11-24T06:28:23.613285Z",
                landmark,
                "hi h hi",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        interactor.execute(new EditNoteInputData(
                "Sidney Smith Hall2025-11-24T06:28:23.613285Z",
                "   "
        ));

        assertTrue(presenter.failCalled);
        assertEquals("Note content cannot be empty.", presenter.errorMessage);
    }

    /**
     * Tests editing a note with content exceeding 500 characters.
     * Verifies that fail view is called with "Note content too long" error.
     */
    @Test
    void testContentTooLong() {
        Location location = new Location(43.6664, -79.3916);
        LandmarkInfo info = new LandmarkInfo(
                "71 Queen's Park Crescent",
                "Trinity College library",
                "9 AM - 10 PM",
                "Library"
        );
        Landmark landmark = new Landmark(
                "pratt-library",
                "E.J. Pratt Library",
                location,
                info,
                0
        );
        Note note = new Note(
                "E.J. Pratt Library2025-11-24T06:24:58.227482Z",
                landmark,
                "hi",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        String tooLong = "a".repeat(501);
        interactor.execute(new EditNoteInputData(
                "E.J. Pratt Library2025-11-24T06:24:58.227482Z",
                tooLong
        ));

        assertTrue(presenter.failCalled);
        assertEquals("Note content too long (max 500 characters).", presenter.errorMessage);
    }

    /**
     * Tests editing a note with exactly 500 characters (boundary test).
     * Verifies that this is accepted and success view is called.
     */
    @Test
    void testExactly500Characters() {
        Location location = new Location(43.6621, -79.3935);
        LandmarkInfo info = new LandmarkInfo(
                "9 King's College Circle",
                "Science information centre",
                "24 hours",
                "Library"
        );
        Landmark landmark = new Landmark(
                "gerstein",
                "Gerstein Science Information Centre",
                location,
                info,
                0
        );
        Note note = new Note(
                "Gerstein Science Information Centre2025-11-24T18:29:02.016579Z",
                landmark,
                "gihureoubgeor",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        String exactly500 = "a".repeat(500);
        interactor.execute(new EditNoteInputData(
                "Gerstein Science Information Centre2025-11-24T18:29:02.016579Z",
                exactly500
        ));

        assertTrue(presenter.successCalled);
        assertEquals(exactly500, presenter.outputData.getUpdatedContent());
    }

    /**
     * Tests editing a note with content containing special characters.
     * Verifies that special characters are accepted and stored correctly.
     */
    @Test
    void testSpecialCharacters() {
        Location location = new Location(43.6599, -79.3919);
        LandmarkInfo info = new LandmarkInfo(
                "144 College St",
                "Faculty of pharmacy building",
                "8 AM - 8 PM",
                "Academic Building"
        );
        Landmark landmark = new Landmark(
                "leslie-dan",
                "Leslie Dan Pharmacy Building",
                location,
                info,
                0
        );
        Note note = new Note(
                "Leslie Dan Pharmacy Building2025-11-24T06:24:28.680359Z",
                landmark,
                "hibuiib",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        String specialContent = "Content with @#$%^&*()!";
        interactor.execute(new EditNoteInputData(
                "Leslie Dan Pharmacy Building2025-11-24T06:24:28.680359Z",
                specialContent
        ));

        assertTrue(presenter.successCalled);
        assertEquals(specialContent, presenter.outputData.getUpdatedContent());
    }

    /**
     * Tests editing a note multiple times in sequence.
     * Verifies that each edit updates the note correctly.
     */
    @Test
    void testMultipleEdits() {
        Location location = new Location(43.6610, -79.3934);
        LandmarkInfo info = new LandmarkInfo(
                "1 King's College Circle",
                "Medical sciences building",
                "7 AM - 10 PM",
                "Academic Building"
        );
        Landmark landmark = new Landmark(
                "med-sci",
                "Medical Sciences Building",
                location,
                info,
                0
        );
        Note note = new Note(
                "Medical Sciences Building2025-11-24T06:37:58.116087Z",
                landmark,
                "hi",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        // First edit
        interactor.execute(new EditNoteInputData(
                "Medical Sciences Building2025-11-24T06:37:58.116087Z",
                "First edit"
        ));
        assertTrue(presenter.successCalled);
        assertEquals("First edit", presenter.outputData.getUpdatedContent());

        // Second edit
        presenter.successCalled = false; // Reset
        interactor.execute(new EditNoteInputData(
                "Medical Sciences Building2025-11-24T06:37:58.116087Z",
                "Second edit"
        ));
        assertTrue(presenter.successCalled);
        assertEquals("Second edit", presenter.outputData.getUpdatedContent());
    }

    /**
     * Tests editing a note with content at 499 characters (just under limit).
     * Verifies that this is accepted and success view is called.
     */
    @Test
    void testContentJustUnderLimit() {
        Location location = new Location(43.6609, -79.3957);
        LandmarkInfo info = new LandmarkInfo(
                "31 King's College Circle",
                "Historic convocation hall",
                "Varies by event",
                "Event Space"
        );
        Landmark landmark = new Landmark(
                "convocation-hall",
                "Convocation Hall",
                location,
                info,
                0
        );
        Note note = new Note(
                "Convocation Hall2025-11-22T18:56:18.905731Z",
                landmark,
                "hi",
                Instant.now(),
                Instant.now()
        );
        dataAccess.addNote(note);

        String just499 = "a".repeat(499);
        interactor.execute(new EditNoteInputData(
                "Convocation Hall2025-11-22T18:56:18.905731Z",
                just499
        ));

        assertTrue(presenter.successCalled);
        assertEquals(just499, presenter.outputData.getUpdatedContent());
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