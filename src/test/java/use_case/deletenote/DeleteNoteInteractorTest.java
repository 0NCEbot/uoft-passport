package use_case.deletenote;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests implemented:
 * testSuccessfulNoteDeletion
 * testDeleteNonExistentNote
 * testDeleteNoteWithNullId
 * testDeleteNoteRetrievesCorrectLandmarkName
 * testInteractorCallsDataAccessMethodsInCorrectOrder
 * testOutputBoundaryOnlyReceivesOneCall
 * testOutputBoundaryOnlyReceivesFailCallForNonExistent
 * testDeleteNoteWithEmptyStringId
 * testGetNoteByIdNotCalledWhenNoteDoesNotExist
 * testMockGetNoteByIdReturnsNullForNonExistentNote
 * testMockDeleteNoteWithDifferentId
 * testMockGetNoteByIdWithDifferentId
 * testMockNoteExistsWithDifferentId
 * testCallOrderIndexOfNotFound
 * testCallOrderIndexOfPartialList
 * testDeleteNoteWithSpecialCharactersInLandmarkName
 */

class DeleteNoteInteractorTest {
    private DeleteNoteDataAccessInterface mockDataAccess;
    private DeleteNoteOutputBoundary mockOutputBoundary;
    private DeleteNoteInteractor interactor;

    // Test data
    private Landmark testLandmark;
    private Note testNote;
    private static final String TEST_NOTE_ID = "Bahen Centre for Information Technology2025-11-30T00:34:24.550208Z";
    private static final String TEST_LANDMARK_NAME = "Bahen Centre for Information Technology";

    @BeforeEach
    void setUp() {
        // Create test landmark
        Location location = new Location(43.6596, -79.3975);
        LandmarkInfo info = new LandmarkInfo(
                "40 St George St",
                "Computer science and engineering building",
                "24 hours",
                "Academic Building"
        );
        testLandmark = new Landmark(
                "bahen-centre",
                TEST_LANDMARK_NAME,
                location,
                info,
                150
        );

        // Create test note
        testNote = new Note(
                TEST_NOTE_ID,
                testLandmark,
                "This place was Great!",
                Instant.parse("2025-11-30T00:34:24.550460Z"),
                Instant.parse("2025-11-30T00:35:52.162507Z")
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
        DeleteNoteInputData inputData = new DeleteNoteInputData("Sidney Smith Hall2025-11-24T99:99:99.999999Z");

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
        Location location1 = new Location(43.6645, -79.3996);
        LandmarkInfo info1 = new LandmarkInfo(
                "100 St George St",
                "Large academic library",
                "24 hours",
                "Library"
        );
        Landmark landmark1 = new Landmark("robarts-library", "Robarts Library", location1, info1, 200);
        Note note1 = new Note(
                "Robarts Library2025-11-30T01:08:28.962320Z",
                landmark1,
                "great place to study",
                Instant.parse("2025-11-30T01:08:28.962388Z"),
                Instant.parse("2025-11-30T01:08:28.962388Z")
        );

        Location location2 = new Location(43.6640, -79.3943);
        LandmarkInfo info2 = new LandmarkInfo(
                "7 Hart House Circle",
                "Historic student center",
                "8 AM - 11 PM",
                "Student Building"
        );
        Landmark landmark2 = new Landmark("hart-house", "Hart House", location2, info2, 180);
        Note note2 = new Note(
                "Hart House2025-11-24T06:56:13.795618Z",
                landmark2,
                "hihihihdcwbuibbuiuiubbbi",
                Instant.parse("2025-11-24T06:56:13.795848Z"),
                Instant.parse("2025-11-24T18:30:27.282099Z")
        );

        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        mockDAO.addNote(note1);
        mockDAO.addNote(note2);

        // When: We delete the Hart House note
        DeleteNoteInputData inputData = new DeleteNoteInputData("Hart House2025-11-24T06:56:13.795618Z");
        interactor.execute(inputData);

        // Then: Output should contain Hart House as the landmark name
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        DeleteNoteOutputData outputData = mockPresenter.getOutputData();
        assertEquals("Hart House", outputData.getLandmarkName(),
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

        // Then: Verify all methods were called
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertTrue(mockDAO.wasNoteExistsCalled(), "noteExists should be called");
        assertTrue(mockDAO.wasGetNoteByIdCalled(), "getNoteById should be called");
        assertTrue(mockDAO.wasDeleteCalled(), "deleteNote should be called");

        // Verify the correct order by checking the call order list
        assertEquals("noteExists", mockDAO.getCallOrder().get(0), "First call should be noteExists");
        assertEquals("getNoteById", mockDAO.getCallOrder().get(1), "Second call should be getNoteById");
        assertEquals("deleteNote", mockDAO.getCallOrder().get(2), "Third call should be deleteNote");
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
        DeleteNoteInputData inputData = new DeleteNoteInputData("Convocation Hall2025-99-99T99:99:99.999999Z");

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
        DeleteNoteInputData inputData = new DeleteNoteInputData("E.J. Pratt Library2025-99-99T99:99:99.999999Z");

        // When: We try to delete the non-existent note
        interactor.execute(inputData);

        // Then: getNoteById should not be called
        MockDeleteNoteDataAccess mockDAO = (MockDeleteNoteDataAccess) mockDataAccess;
        assertTrue(mockDAO.wasNoteExistsCalled(), "noteExists should be called");
        assertFalse(mockDAO.wasGetNoteByIdCalled(), "getNoteById should not be called when note doesn't exist");
        assertFalse(mockDAO.wasDeleteCalled(), "deleteNote should not be called when note doesn't exist");
    }

    /**
     * Test that mock getNoteById returns null for non-existent note.
     * Verifies the mock data access implementation correctly handles
     * requests for notes that don't exist.
     */
    @Test
    void testMockGetNoteByIdReturnsNullForNonExistentNote() {
        // Given: Mock data access with no stored note
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();

        // When: We try to get a note that doesn't exist
        Note result = mockDAO.getNoteById("NonExistent2025-99-99T99:99:99.999999Z");

        // Then: Should return null
        assertNull(result, "getNoteById should return null for non-existent note");
    }

    /**
     * Test that mock deleteNote handles deletion of non-matching note ID.
     * Verifies the mock correctly handles deletion attempts when stored note
     * has a different ID than the one being deleted.
     */
    @Test
    void testMockDeleteNoteWithDifferentId() {
        // Given: A note is stored with one ID
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();
        mockDAO.addNote(testNote);

        // When: We try to delete a different note ID
        mockDAO.deleteNote("DifferentNote2025-11-30T00:00:00.000000Z");

        // Then: The delete should be called but stored note remains
        assertTrue(mockDAO.wasDeleteCalled(), "Delete should be called");
        assertNotNull(mockDAO.getNoteById(TEST_NOTE_ID), "Original note should still exist");
    }

    /**
     * Test that mock getNoteById returns null when note ID doesn't match.
     * Verifies the mock correctly handles the false branch of equals().
     */
    @Test
    void testMockGetNoteByIdWithDifferentId() {
        // Given: A note is stored with one ID
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();
        mockDAO.addNote(testNote);

        // When: We try to get a note with a different ID
        Note result = mockDAO.getNoteById("DifferentNote2025-11-30T00:00:00.000000Z");

        // Then: Should return null even though storedNote is not null
        assertNull(result, "getNoteById should return null when ID doesn't match");
    }

    /**
     * Test that mock noteExists returns false when note ID doesn't match.
     * Verifies the mock correctly handles the false branch of equals() in noteExists.
     */
    @Test
    void testMockNoteExistsWithDifferentId() {
        // Given: A note is stored with one ID
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();
        mockDAO.addNote(testNote);

        // When: We check if a different note ID exists
        boolean exists = mockDAO.noteExists("DifferentNote2025-11-30T00:00:00.000000Z");

        // Then: Should return false even though storedNote is not null
        assertFalse(exists, "noteExists should return false when ID doesn't match");
    }

    /**
     * Test call order indexOf when method is not in the list.
     * Verifies indexOf returns -1 for methods not called.
     */
    @Test
    void testCallOrderIndexOfNotFound() {
        // Given: Fresh mock with no method calls
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();

        // When: We check indexOf for a method that hasn't been called
        int index = mockDAO.getCallOrder().indexOf("deleteNote");

        // Then: Should return -1
        assertEquals(-1, index, "indexOf should return -1 for method not in call order");

        // Also test with getNoteById
        int index2 = mockDAO.getCallOrder().indexOf("getNoteById");
        assertEquals(-1, index2, "indexOf should return -1 for getNoteById not in call order");
    }

    /**
     * Test call order indexOf with partial method calls.
     * Verifies indexOf works correctly when list has some but not all methods.
     */
    @Test
    void testCallOrderIndexOfPartialList() {
        // Given: Mock with only noteExists called
        MockDeleteNoteDataAccess mockDAO = new MockDeleteNoteDataAccess();
        mockDAO.noteExists("someId");

        // When: We check indexOf for methods
        int existsIndex = mockDAO.getCallOrder().indexOf("noteExists");
        int getByIdIndex = mockDAO.getCallOrder().indexOf("getNoteById");
        int deleteIndex = mockDAO.getCallOrder().indexOf("deleteNote");

        // Then: noteExists should be found, others should return -1
        assertEquals(0, existsIndex, "noteExists should be at index 0");
        assertEquals(-1, getByIdIndex, "getNoteById should return -1 when not called");
        assertEquals(-1, deleteIndex, "deleteNote should return -1 when not called");
    }

    /**
     * Test deletion of a note with special characters in landmark name.
     * Verifies that landmark names containing special characters, unicode,
     * or non-ASCII characters are correctly included in the output data.
     */
    @Test
    void testDeleteNoteWithSpecialCharactersInLandmarkName() {
        // Given: A note with a landmark containing special characters
        Location location = new Location(43.6664, -79.3916);
        LandmarkInfo info = new LandmarkInfo("Address", "Desc", "Hours", "Type");
        Landmark specialLandmark = new Landmark(
                "pratt-library-special",
                "E.J. Pratt Library & \"Special\" <Test> 中文",
                location,
                info,
                10
        );
        Note specialNote = new Note(
                "E.J. Pratt Library2025-11-24T07:06:27.418800Z",
                specialLandmark,
                "hiu",
                Instant.parse("2025-11-24T07:06:27.418836Z"),
                Instant.parse("2025-11-24T07:06:27.418836Z")
        );

        ((MockDeleteNoteDataAccess) mockDataAccess).addNote(specialNote);
        DeleteNoteInputData inputData = new DeleteNoteInputData("E.J. Pratt Library2025-11-24T07:06:27.418800Z");

        // When: We delete the note
        interactor.execute(inputData);

        // Then: The landmark name with special characters should be correctly preserved in output
        MockDeleteNoteOutputBoundary mockPresenter = (MockDeleteNoteOutputBoundary) mockOutputBoundary;
        DeleteNoteOutputData outputData = mockPresenter.getOutputData();
        assertEquals("E.J. Pratt Library & \"Special\" <Test> 中文", outputData.getLandmarkName(),
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
