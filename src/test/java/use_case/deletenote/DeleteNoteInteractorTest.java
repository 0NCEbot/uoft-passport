package use_case.deletenote;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeleteNoteInteractorTest {
    private DeleteNoteDataAccessInterface mockDataAccess;
    private DeleteNoteOutputBoundary mockOutputBoundary;
    private DeleteNoteInteractor interactor;

    // Test data
    private Landmark testLandmark;
    private Note testNote;
    private static final String TEST_NOTE_ID = "test-note-123";
    private static final String TEST_LANDMARK_NAME = "CN Tower";

    @BeforeEach
    void setUp() {
        // Create test landmark
        Location location = new Location(43.6426, -79.3871);
        LandmarkInfo info = new LandmarkInfo(
                "301 Front St W",
                "Famous tower",
                "9 AM - 10 PM",
                "Tourist Attraction"
        );
        testLandmark = new Landmark(
                "landmark-1",
                TEST_LANDMARK_NAME,
                location,
                info,
                100
        );

        // Create test note
        testNote = new Note(
                TEST_NOTE_ID,
                testLandmark,
                "Great views from the top!",
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(1800)
        );

        // Initialize mocks
        mockDataAccess = new MockDeleteNoteDataAccess();
        mockOutputBoundary = new MockDeleteNoteOutputBoundary();
        interactor = new DeleteNoteInteractor(mockDataAccess, mockOutputBoundary);
    }

    /**
     * Test successful deletion of an existing note.
     * Verifies that when a note exists, it is deleted and the success view
     * is prepared with correct output data including note ID and landmark name.
     */
    @Test
    void testSuccessfulNoteDeletion() {
        // Given: A note exists in the system
        ((MockDeleteNoteDataAccess) mockDataAccess).addNote(testNote);
        DeleteNoteInputData inputData = new DeleteNoteInputData(TEST_NOTE_ID);

        // When: We execute the delete operation
        interactor.execute(inputData);

        // Then: The note should be deleted and success view prepared
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertTrue(mockPresenter.isSuccessViewPrepared(), "Success view should be prepared");
        assertFalse(mockPresenter.isFailViewPrepared(), "Fail view should not be prepared");

        DeleteNoteOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(TEST_NOTE_ID, outputData.getNoteId(), "Note ID should match");
        assertEquals(TEST_LANDMARK_NAME, outputData.getLandmarkName(), "Landmark name should match");
        assertTrue(outputData.isSuccess(), "Success flag should be true");

        // Verify the note was actually deleted from data access
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertTrue(mockDAO.wasDeleteCalled(), "Delete method should be called");
        assertEquals(TEST_NOTE_ID, mockDAO.getDeletedNoteId(), "Correct note ID should be deleted");
    }

    /**
     * Test deletion of a non-existent note.
     * Verifies that attempting to delete a note that doesn't exist results
     * in a fail view with appropriate error message.
     */
    @Test
    void testDeleteNonExistentNote() {
        // Given: A note does not exist in the system
        DeleteNoteInputData inputData = new DeleteNoteInputData("non-existent-note");

        // When: We try to delete the non-existent note
        interactor.execute(inputData);

        // Then: Fail view should be prepared with appropriate error message
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertTrue(mockPresenter.isFailViewPrepared(), "Fail view should be prepared");
        assertFalse(mockPresenter.isSuccessViewPrepared(), "Success view should not be prepared");
        assertEquals("Note not found.", mockPresenter.getErrorMessage(), "Error message should match");

        // Verify delete was not called
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertFalse(mockDAO.wasDeleteCalled(), "Delete should not be called for non-existent note");
    }

    /**
     * Test deletion with null note ID.
     * Verifies that providing a null note ID results in a fail view
     * being prepared rather than a null pointer exception.
     */
    @Test
    void testDeleteNoteWithNullId() {
        // Given: Input data with null note ID
        DeleteNoteInputData inputData = new DeleteNoteInputData(null);

        // When: We try to delete with null ID
        interactor.execute(inputData);

        // Then: Fail view should be prepared
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertTrue(mockPresenter.isFailViewPrepared(), "Fail view should be prepared for null ID");
        assertFalse(mockPresenter.isSuccessViewPrepared(), "Success view should not be prepared");
    }

    /**
     * Test that correct landmark name is retrieved for the deleted note.
     * Verifies that when multiple notes exist with different landmarks,
     * the output data contains the correct landmark name for the deleted note.
     */
    @Test
    void testDeleteNoteRetrievesCorrectLandmarkName() {
        // Given: Multiple notes exist with different landmarks
        Location location1 = new Location(43.6426, -79.3871);
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Desc 1", "Hours 1", "Type 1");
        Landmark landmark1 = new Landmark("lm-1", "Robarts Library", location1, info1, 50);
        Note note1 = new Note("note-1", landmark1, "Content 1", Instant.now(), Instant.now());

        Location location2 = new Location(43.6629, -79.3957);
        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Desc 2", "Hours 2", "Type 2");
        Landmark landmark2 = new Landmark("lm-2", "Casa Loma", location2, info2, 75);
        Note note2 = new Note("note-2", landmark2, "Content 2", Instant.now(), Instant.now());

        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        mockDAO.addNote(note1);
        mockDAO.addNote(note2);

        // When: We delete note-2
        DeleteNoteInputData inputData = new DeleteNoteInputData("note-2");
        interactor.execute(inputData);

        // Then: Output should contain Casa Loma as the landmark name
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        DeleteNoteOutputData outputData = mockPresenter.getOutputData();
        assertEquals("Casa Loma", outputData.getLandmarkName(),
                "Should retrieve correct landmark name for the deleted note");
    }

    /**
     * Test that data access methods are called in the correct order.
     * Verifies the interactor follows the expected flow: check existence,
     * retrieve note details, then perform deletion.
     */
    @Test
    void testInteractorCallsDataAccessMethodsInCorrectOrder() {
        // Given: A note exists
        ((MockDeleteNoteDataAccess) mockDataAccess).addNote(testNote);
        DeleteNoteInputData inputData = new DeleteNoteInputData(TEST_NOTE_ID);

        // When: We execute delete
        interactor.execute(inputData);

        // Then: Verify methods were called in correct order
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertTrue(mockDAO.wasNoteExistsCalled(), "noteExists should be called");
        assertTrue(mockDAO.wasGetNoteByIdCalled(), "getNoteById should be called");
        assertTrue(mockDAO.wasDeleteCalled(), "deleteNote should be called");

        // Verify the order: exists check -> get note -> delete
        assertTrue(mockDAO.getCallOrder().indexOf("noteExists") <
                        mockDAO.getCallOrder().indexOf("getNoteById"),
                "noteExists should be called before getNoteById");
        assertTrue(mockDAO.getCallOrder().indexOf("getNoteById") <
                        mockDAO.getCallOrder().indexOf("deleteNote"),
                "getNoteById should be called before deleteNote");
    }

    /**
     * Test that output boundary receives exactly one success call.
     * Verifies that for a successful deletion, only prepareSuccessView
     * is called and prepareFailView is not called.
     */
    @Test
    void testOutputBoundaryOnlyReceivesOneCall() {
        // Given: A note exists
        ((MockDeleteNoteDataAccess) mockDataAccess).addNote(testNote);
        DeleteNoteInputData inputData = new DeleteNoteInputData(TEST_NOTE_ID);

        // When: We execute delete
        interactor.execute(inputData);

        // Then: Only success view should be called, not fail view
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertEquals(1, mockPresenter.getSuccessCallCount(),
                "Success view should be called exactly once");
        assertEquals(0, mockPresenter.getFailCallCount(),
                "Fail view should not be called");
    }

    /**
     * Test that output boundary receives exactly one fail call for non-existent note.
     * Verifies that when deletion fails, only prepareFailView is called
     * and prepareSuccessView is not called.
     */
    @Test
    void testOutputBoundaryOnlyReceivesFailCallForNonExistent() {
        // Given: Note does not exist
        DeleteNoteInputData inputData = new DeleteNoteInputData("non-existent");

        // When: We execute delete
        interactor.execute(inputData);

        // Then: Only fail view should be called
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertEquals(0, mockPresenter.getSuccessCallCount(),
                "Success view should not be called");
        assertEquals(1, mockPresenter.getFailCallCount(),
                "Fail view should be called exactly once");
    }

    /**
     * Test deletion of a note with empty string ID.
     * Verifies that an empty string note ID is treated as a non-existent note
     * and results in a fail view being prepared.
     */
    @Test
    void testDeleteNoteWithEmptyStringId() {
        // Given: Input data with empty string note ID
        DeleteNoteInputData inputData = new DeleteNoteInputData("");

        // When: We try to delete with empty string ID
        interactor.execute(inputData);

        // Then: Fail view should be prepared
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        assertTrue(mockPresenter.isFailViewPrepared(), "Fail view should be prepared for empty string ID");
        assertFalse(mockPresenter.isSuccessViewPrepared(), "Success view should not be prepared");
        assertEquals("Note not found.", mockPresenter.getErrorMessage(), "Error message should indicate note not found");
    }

    /**
     * Test that getNoteById is not called when note doesn't exist.
     * Verifies optimization: if noteExists returns false, the interactor
     * should not waste resources calling getNoteById.
     */
    @Test
    void testGetNoteByIdNotCalledWhenNoteDoesNotExist() {
        // Given: No note exists with the given ID
        DeleteNoteInputData inputData = new DeleteNoteInputData("non-existent-note");

        // When: We try to delete the non-existent note
        interactor.execute(inputData);

        // Then: getNoteById should not be called
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertTrue(mockDAO.wasNoteExistsCalled(), "noteExists should be called");
        assertFalse(mockDAO.wasGetNoteByIdCalled(), "getNoteById should not be called when note doesn't exist");
        assertFalse(mockDAO.wasDeleteCalled(), "deleteNote should not be called when note doesn't exist");
    }

    /**
     * Test deletion of a note with special characters in landmark name.
     * Verifies that landmark names containing special characters, unicode,
     * or non-ASCII characters are correctly included in the output data.
     */
    @Test
    void testDeleteNoteWithSpecialCharactersInLandmarkName() {
        // Given: A note with a landmark containing special characters
        Location location = new Location(43.7, -79.4);
        LandmarkInfo info = new LandmarkInfo("Address", "Desc", "Hours", "Type");
        Landmark specialLandmark = new Landmark(
                "lm-special",
                "Café René's <Test> & \"Quotes\" 中文",
                location,
                info,
                10
        );
        Note specialNote = new Note(
                "special-note-id",
                specialLandmark,
                "Special content",
                Instant.now(),
                Instant.now()
        );

        ((MockDeleteNoteDataAccess) mockDataAccess).addNote(specialNote);
        DeleteNoteInputData inputData = new DeleteNoteInputData("special-note-id");

        // When: We delete the note
        interactor.execute(inputData);

        // Then: The landmark name with special characters should be correctly preserved in output
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        DeleteNoteOutputData outputData = mockPresenter.getOutputData();
        assertEquals("Café René's <Test> & \"Quotes\" 中文", outputData.getLandmarkName(),
                "Landmark name with special characters should be preserved correctly");
        assertTrue(outputData.isSuccess(), "Deletion should succeed");
    }

    // Mock implementation of DeleteNoteDataAccessInterface
    private static class MockDeleteNoteDataAccess implements DeleteNoteDataAccessInterface {
        private Note storedNote;
        private boolean deleteCalled = false;
        private String deletedNoteId;
        private boolean noteExistsCalled = false;
        private boolean getNoteByIdCalled = false;
        private final java.util.List<String> callOrder = new java.util.ArrayList<>();

        void addNote(Note note) {
            this.storedNote = note;
        }

        @Override
        public Note getNoteById(String noteId) {
            getNoteByIdCalled = true;
            callOrder.add("getNoteById");
            if (storedNote != null && storedNote.getNoteId().equals(noteId)) {
                return storedNote;
            }
            return null;
        }

        @Override
        public void deleteNote(String noteId) {
            deleteCalled = true;
            deletedNoteId = noteId;
            callOrder.add("deleteNote");
            if (storedNote != null && storedNote.getNoteId().equals(noteId)) {
                storedNote = null;
            }
        }

        @Override
        public boolean noteExists(String noteId) {
            noteExistsCalled = true;
            callOrder.add("noteExists");
            return storedNote != null && storedNote.getNoteId().equals(noteId);
        }

        boolean wasDeleteCalled() {
            return deleteCalled;
        }

        String getDeletedNoteId() {
            return deletedNoteId;
        }

        boolean wasNoteExistsCalled() {
            return noteExistsCalled;
        }

        boolean wasGetNoteByIdCalled() {
            return getNoteByIdCalled;
        }

        java.util.List<String> getCallOrder() {
            return callOrder;
        }
    }

    // Mock implementation of DeleteNoteOutputBoundary
    private static class MockDeleteNoteOutputBoundary implements DeleteNoteOutputBoundary {
        private boolean successViewPrepared = false;
        private boolean failViewPrepared = false;
        private DeleteNoteOutputData outputData;
        private String errorMessage;
        private int successCallCount = 0;
        private int failCallCount = 0;

        @Override
        public void prepareSuccessView(DeleteNoteOutputData outputData) {
            this.successViewPrepared = true;
            this.outputData = outputData;
            this.successCallCount++;
        }

        @Override
        public void prepareFailView(String error) {
            this.failViewPrepared = true;
            this.errorMessage = error;
            this.failCallCount++;
        }

        boolean isSuccessViewPrepared() {
            return successViewPrepared;
        }

        boolean isFailViewPrepared() {
            return failViewPrepared;
        }

        DeleteNoteOutputData getOutputData() {
            return outputData;
        }

        String getErrorMessage() {
            return errorMessage;
        }

        int getSuccessCallCount() {
            return successCallCount;
        }

        int getFailCallCount() {
            return failCallCount;
        }
    }
}
