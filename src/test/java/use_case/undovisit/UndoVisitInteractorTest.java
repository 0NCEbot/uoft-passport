package use_case.undovisit;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.User;
import entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UndoVisitInteractor.
 * Tests the undo visit use case including:
 * - Successful visit removal
 * - Attempting to remove non-existent visit
 * - Attempting to remove visit for non-existent user
 * - Verifying remaining visits after removal
 */
class UndoVisitInteractorTest {

    private MockUndoVisitUserDataAccess mockUserDAO;
    private MockUndoVisitPresenter mockPresenter;
    private UndoVisitInteractor interactor;

    private User testUser;
    private Landmark landmark1;
    private Landmark landmark2;
    private Visit visit1;
    private Visit visit2;
    private Visit visit3;

    @BeforeEach
    void setUp() {
        mockUserDAO = new MockUndoVisitUserDataAccess();
        mockPresenter = new MockUndoVisitPresenter();
        interactor = new UndoVisitInteractor(mockUserDAO, mockPresenter);

        // Setup landmarks
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Description 1", "Hours 1", "Library");
        landmark1 = new Landmark("id1", "Robarts Library", new Location(43.6645, -79.3996), info1, 0);

        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Description 2", "Hours 2", "Engineering");
        landmark2 = new Landmark("id2", "Bahen Centre", new Location(43.6596, -79.3975), info2, 0);

        // Setup user with 3 visits
        testUser = new User("testuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());

        visit1 = new Visit("visit1", landmark1, Instant.now());
        visit2 = new Visit("visit2", landmark2, Instant.now());
        visit3 = new Visit("visit3", landmark1, Instant.now());

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDAO.addUser(testUser);
    }

    @Test
    void testSuccessfulUndoVisit() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "visit2");
        int initialVisitCount = testUser.getVisits().size();

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailCalled(), "Fail view should not be called");

        User savedUser = mockUserDAO.get("testuser");
        assertEquals(initialVisitCount - 1, savedUser.getVisits().size(), "Visit count should decrease by 1");

        // Verify the correct visit was removed
        boolean visit2Exists = savedUser.getVisits().stream()
                .anyMatch(v -> v.getVisitId().equals("visit2"));
        assertFalse(visit2Exists, "Visit2 should be removed");

        // Verify other visits still exist
        boolean visit1Exists = savedUser.getVisits().stream()
                .anyMatch(v -> v.getVisitId().equals("visit1"));
        assertTrue(visit1Exists, "Visit1 should still exist");

        boolean visit3Exists = savedUser.getVisits().stream()
                .anyMatch(v -> v.getVisitId().equals("visit3"));
        assertTrue(visit3Exists, "Visit3 should still exist");

        // Verify user was saved
        assertTrue(mockUserDAO.isSaveCalled(), "User should be saved after undo");
    }

    @Test
    void testUndoVisitReturnsRemainingVisits() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "visit1");

        // Act
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(2, outputData.getVisits().size(), "Should return 2 remaining visits");

        // Verify the remaining visits are correct
        List<String> remainingVisitIds = outputData.getVisits().stream()
                .map(dto -> dto.visitId)
                .toList();
        assertTrue(remainingVisitIds.contains("visit2"), "Visit2 should be in remaining visits");
        assertTrue(remainingVisitIds.contains("visit3"), "Visit3 should be in remaining visits");
        assertFalse(remainingVisitIds.contains("visit1"), "Visit1 should not be in remaining visits");
    }

    @Test
    void testUndoVisitWithNonExistentVisitId() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "nonexistent_visit");
        int initialVisitCount = testUser.getVisits().size();

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Visit not found with ID: nonexistent_visit", mockPresenter.getFailMessage(),
                "Error message should match");

        // Verify no visits were removed
        User user = mockUserDAO.get("testuser");
        assertEquals(initialVisitCount, user.getVisits().size(), "Visit count should not change");
    }

    @Test
    void testUndoVisitWithNonExistentUser() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("nonexistent", "visit1");

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("User not found: nonexistent", mockPresenter.getFailMessage(),
                "Error message should match");
    }

    @Test
    void testUndoLastVisit() {
        // Arrange - create user with only 1 visit
        User singleVisitUser = new User("singleuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());
        Visit onlyVisit = new Visit("only_visit", landmark1, Instant.now());
        singleVisitUser.getVisits().add(onlyVisit);
        mockUserDAO.addUser(singleVisitUser);

        UndoVisitInputData inputData = new UndoVisitInputData("singleuser", "only_visit");

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success view should be called");
        User savedUser = mockUserDAO.get("singleuser");
        assertEquals(0, savedUser.getVisits().size(), "User should have no visits");

        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertEquals(0, outputData.getVisits().size(), "Should return empty visit list");
    }

    @Test
    void testUndoVisitOrderIsPreserved() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "visit2");

        // Act
        interactor.execute(inputData);

        // Assert
        User savedUser = mockUserDAO.get("testuser");
        List<Visit> remainingVisits = savedUser.getVisits();

        // Verify the order: visit1 should come before visit3
        assertEquals("visit1", remainingVisits.get(0).getVisitId(), "First visit should be visit1");
        assertEquals("visit3", remainingVisits.get(1).getVisitId(), "Second visit should be visit3");
    }

    @Test
    void testSuccessMessageIsReturned() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "visit1");

        // Act
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertEquals("Visit removed successfully", outputData.getSuccessMessage(),
                "Success message should be returned");
    }

    @Test
    void testVisitDTOContainsCorrectInformation() {
        // Arrange
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "visit1");

        // Act
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        List<UndoVisitOutputData.VisitDTO> visitDTOs = outputData.getVisits();

        // Check that DTOs contain landmark names
        boolean hasBahenVisit = visitDTOs.stream()
                .anyMatch(dto -> dto.landmarkName.equals("Bahen Centre"));
        assertTrue(hasBahenVisit, "Should have visit to Bahen Centre");

        // Check that DTOs have formatted dates
        for (UndoVisitOutputData.VisitDTO dto : visitDTOs) {
            assertNotNull(dto.visitedAt, "Formatted date should not be null");
            assertFalse(dto.visitedAt.isEmpty(), "Formatted date should not be empty");
        }
    }

    // =============== Mock Classes ===============

    private static class MockUndoVisitUserDataAccess implements UndoVisitUserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private boolean saveCalled = false;
        private String currentUsername;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public void save(User user) {
            saveCalled = true;
            users.put(user.getUsername(), user);
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }

        void setCurrentUsername(String username) {
            this.currentUsername = username;
        }

        boolean isSaveCalled() {
            return saveCalled;
        }
    }

    private static class MockUndoVisitPresenter implements UndoVisitOutputBoundary {
        private boolean successCalled = false;
        private boolean failCalled = false;
        private UndoVisitOutputData outputData;
        private String failMessage;

        @Override
        public void prepareSuccessView(UndoVisitOutputData data) {
            successCalled = true;
            outputData = data;
        }

        @Override
        public void prepareFailView(String message) {
            failCalled = true;
            failMessage = message;
        }

        boolean isSuccessCalled() {
            return successCalled;
        }

        boolean isFailCalled() {
            return failCalled;
        }

        UndoVisitOutputData getOutputData() {
            return outputData;
        }

        String getFailMessage() {
            return failMessage;
        }
    }
}
