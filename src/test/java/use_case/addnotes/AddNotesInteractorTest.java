package use_case.addnotes;

import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AddNotesInteractor.
 * Tests the add notes use case including:
 * - Creating notes successfully
 * - Validating user existence
 * - Validating landmark existence
 * - Validating note content
 * - Building note DTOs with correct filtering
 */
class AddNotesInteractorTest {

    private MockUserDAO mockUserDAO;
    private MockAddNotesPresenter mockPresenter;
    private AddNotesInteractor interactor;

    private User testUser;
    private Landmark landmark1;
    private Landmark landmark2;

    @BeforeEach
    void setUp() {
        mockUserDAO = new MockUserDAO();
        MockLandmarkDAO mockLandmarkDAO = new MockLandmarkDAO();
        mockPresenter = new MockAddNotesPresenter();
        interactor = new AddNotesInteractor(mockUserDAO, mockLandmarkDAO, mockPresenter);

        // Setup landmarks
        landmark1 = createLandmark("id1", "Bahen Centre");
        landmark2 = createLandmark("id2", "Robarts Library");
        mockLandmarkDAO.addLandmark(landmark1);
        mockLandmarkDAO.addLandmark(landmark2);

        // Setup user with no notes initially
        testUser = new User("testuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDAO.addUser(testUser);
    }

    @Test
    void testAddNote_Success() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String content = "Great study spot!";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(username, data.getUsername(), "Username should match");
        assertEquals(landmarkName, data.getLandmarkName(), "Landmark name should match");
        assertEquals("Note added successfully.", data.getSuccessMessage(), "Success message should be set");
        assertNull(data.getErrorMessage(), "Error message should be null");
        assertEquals(1, data.getNotes().size(), "Should have 1 note");
        assertEquals(content, data.getNotes().getFirst().content, "Note content should match");

        // Verify note was added to user
        assertEquals(1, testUser.getPrivateNotes().size(), "User should have 1 note");
        assertEquals(content, testUser.getPrivateNotes().getFirst().getContent(), "Note content should match in user");
        assertTrue(mockUserDAO.wasSaved, "User should be saved");
    }

    @Test
    void testAddNote_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        String landmarkName = "Bahen Centre";
        String content = "Test note";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(username, data.getUsername(), "Username should match");
        assertEquals(landmarkName, data.getLandmarkName(), "Landmark name should match");
        assertEquals("User not found.", data.getErrorMessage(), "Error message should indicate user not found");
        assertNull(data.getSuccessMessage(), "Success message should be null");
        assertTrue(data.getNotes().isEmpty(), "Notes list should be empty");
        assertFalse(mockUserDAO.wasSaved, "User should not be saved");
    }

    @Test
    void testAddNote_LandmarkNotFound() {
        // Arrange
        String username = "testuser";
        String landmarkName = "NonexistentLandmark";
        String content = "Test note";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(username, data.getUsername(), "Username should match");
        assertEquals(landmarkName, data.getLandmarkName(), "Landmark name should match");
        assertEquals("Landmark not found.", data.getErrorMessage(), "Error message should indicate landmark not found");
        assertNull(data.getSuccessMessage(), "Success message should be null");
        assertTrue(data.getNotes().isEmpty(), "Notes list should be empty");
        assertFalse(mockUserDAO.wasSaved, "User should not be saved");
    }

    @Test
    void testAddNote_EmptyContent() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String emptyContent = "";

        // Add existing note to verify it's preserved
        Note existingNote = new Note(landmark1, "Existing note");
        testUser.getPrivateNotes().add(existingNote);

        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, emptyContent);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(username, data.getUsername(), "Username should match");
        assertEquals(landmarkName, data.getLandmarkName(), "Landmark name should match");
        assertEquals("Note cannot be empty.", data.getErrorMessage(), "Error message should indicate empty content");
        assertNull(data.getSuccessMessage(), "Success message should be null");

        // Verify existing notes are preserved in output
        assertEquals(1, data.getNotes().size(), "Should return existing note");
        assertEquals("Existing note", data.getNotes().getFirst().content, "Existing note content should match");

        // Verify no new note was added to user
        assertEquals(1, testUser.getPrivateNotes().size(), "User should still have only 1 note");
        assertFalse(mockUserDAO.wasSaved, "User should not be saved");
    }

    @Test
    void testAddNote_NullContent() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String content = null;  // Testing null content handling
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals("Note cannot be empty.", data.getErrorMessage(), "Error message should indicate empty content");
        assertNull(data.getSuccessMessage(), "Success message should be null");
        assertFalse(mockUserDAO.wasSaved, "User should not be saved");
    }

    @Test
    void testAddNote_WhitespaceContent() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String whitespaceContent = "   ";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, whitespaceContent);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals("Note cannot be empty.", data.getErrorMessage(), "Error message should indicate empty content");
        assertNull(data.getSuccessMessage(), "Success message should be null");
        assertFalse(mockUserDAO.wasSaved, "User should not be saved");
    }

    @Test
    void testAddNote_MultipleNotesForSameLandmark() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";

        // Add existing note
        Note existingNote = new Note(landmark1, "First note");
        testUser.getPrivateNotes().add(existingNote);

        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, "Second note");

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertNull(data.getErrorMessage(), "Error message should be null");
        assertEquals("Note added successfully.", data.getSuccessMessage(), "Success message should be set");

        // Verify both notes are in output
        assertEquals(2, data.getNotes().size(), "Should have 2 notes in output");
        assertEquals(2, testUser.getPrivateNotes().size(), "User should have 2 notes");
        assertTrue(mockUserDAO.wasSaved, "User should be saved");
    }

    @Test
    void testBuildNoteDTOs_FiltersCorrectly() {
        // Arrange
        String username = "testuser";
        String targetLandmark = "Bahen Centre";

        // Add notes for different landmarks
        testUser.getPrivateNotes().add(new Note(landmark1, "Note for Bahen"));
        testUser.getPrivateNotes().add(new Note(landmark2, "Note for Robarts"));
        testUser.getPrivateNotes().add(new Note(landmark1, "Another note for Bahen"));

        AddNotesInputData inputData = new AddNotesInputData(username, targetLandmark, "New note for Bahen");

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");

        // Should only return notes for target landmark (2 existing + 1 new = 3 for Bahen)
        assertEquals(3, data.getNotes().size(), "Should have 3 notes for Bahen Centre");

        // Verify all notes contain content (filtering worked correctly)
        for (AddNotesOutputData.NoteDTO dto : data.getNotes()) {
            assertNotNull(dto.content, "Note content should not be null");
            assertFalse(dto.content.isEmpty(), "Note content should not be empty");
        }
    }

    @Test
    void testAddNote_PreservesLandmarkInfo() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String content = "Test note";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");

        // Verify landmark info is preserved
        assertEquals("40 St George St", data.getAddress(), "Address should match");
        assertEquals("Computer Science building", data.getLandmarkDescription(), "Description should match");
        assertEquals("Mon-Fri: 8AM-10PM", data.getOpenHours(), "Open hours should match");
    }

    @Test
    void testAddNote_FormatsTimestampCorrectly() {
        // Arrange
        String username = "testuser";
        String landmarkName = "Bahen Centre";
        String content = "Test note with timestamp";
        AddNotesInputData inputData = new AddNotesInputData(username, landmarkName, content);

        // Act
        interactor.addNote(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentCalled(), "Present should be called");
        AddNotesOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(1, data.getNotes().size(), "Should have 1 note");

        // Verify timestamp is formatted (yyyy-MM-dd HH:mm format)
        String createdAt = data.getNotes().getFirst().createdAt;
        assertNotNull(createdAt, "Created at should not be null");
        assertFalse(createdAt.isEmpty(), "Created at should not be empty");
        assertTrue(createdAt.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"),
                "Timestamp should match yyyy-MM-dd HH:mm format");
    }

    // =============== Helper Methods ===============

    private Landmark createLandmark(String id, String name) {
        LandmarkInfo info = new LandmarkInfo(
                "40 St George St",
                "Computer Science building",
                "Mon-Fri: 8AM-10PM",
                "Academic Building"
        );
        return new Landmark(id, name, new Location(43.6629, -79.3957), info, 0);
    }

    // =============== Mock Classes ===============

    private static class MockUserDAO implements UserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        boolean wasSaved = false;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public void save(User user) {
            wasSaved = true;
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String identifier) {
            return users.containsKey(identifier);
        }

        @Override
        public void setCurrentUsername(String name) {
            // Not needed for add notes tests - no implementation required
        }

        @Override
        public String getCurrentUsername() {
            return null;
        }
    }

    private static class MockLandmarkDAO implements LandmarkDataAccessInterface {
        private final List<Landmark> landmarks = new ArrayList<>();

        void addLandmark(Landmark landmark) {
            landmarks.add(landmark);
        }

        @Override
        public List<Landmark> getLandmarks() {
            return new ArrayList<>(landmarks);
        }

        @Override
        public boolean existsByName(String landmarkName) {
            return landmarks.stream().anyMatch(l -> l.getLandmarkName().equalsIgnoreCase(landmarkName));
        }

        @Override
        public Landmark findByName(String name) {
            return landmarks.stream()
                    .filter(l -> l.getLandmarkName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static class MockAddNotesPresenter implements AddNotesOutputBoundary {
        private boolean presentCalled = false;
        private AddNotesOutputData outputData;

        @Override
        public void present(AddNotesOutputData data) {
            presentCalled = true;
            outputData = data;
        }

        boolean isPresentCalled() {
            return presentCalled;
        }

        AddNotesOutputData getOutputData() {
            return outputData;
        }
    }
}