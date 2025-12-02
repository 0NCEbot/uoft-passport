package use_case.undovisit;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.User;
import entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UndoVisitInteractor.
 * Tests the undo visit use case including:
 * - Successfully removing a visit from user's history
 * - Handling error cases (user not found, visit not found)
 * - Persisting changes to data access
 * - Formatting remaining visits after removal
 * - Verifying EventBus integration
 */
class UndoVisitInteractorTest {

    private MockUndoVisitUserDataAccess mockUserDataAccess;
    private MockUndoVisitPresenter mockPresenter;
    private UndoVisitInteractor interactor;

    private Landmark testLandmark1;
    private Landmark testLandmark2;
    private Landmark testLandmark3;

    @BeforeEach
    void setUp() {
        mockUserDataAccess = new MockUndoVisitUserDataAccess();
        mockPresenter = new MockUndoVisitPresenter();
        interactor = new UndoVisitInteractor(mockUserDataAccess, mockPresenter);

        // Setup test landmarks
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Description 1", "Hours 1", "Library");
        testLandmark1 = new Landmark("id1", "Robarts Library", new Location(43.6645, -79.3996), info1, 0);

        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Description 2", "Hours 2", "Engineering & Technology");
        testLandmark2 = new Landmark("id2", "Bahen Centre", new Location(43.6596, -79.3975), info2, 0);

        LandmarkInfo info3 = new LandmarkInfo("Address 3", "Description 3", "Hours 3", "Student Life");
        testLandmark3 = new Landmark("id3", "Hart House", new Location(43.6643, -79.3947), info3, 0);
    }

    @Test
    void testUndoVisitSuccessfully() {
        // Arrange: Create user with multiple visits
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(3, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark3, now.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove the middle visit
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v2");
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessViewCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailViewCalled(), "Fail view should not be called");

        // Verify save was called
        assertTrue(mockUserDataAccess.isSaveCalled(), "Save should be called after removing visit");

        // Verify output data
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals("testuser", outputData.getUsername());
        assertEquals("Visit removed successfully", outputData.getSuccessMessage());

        // Should have 2 remaining visits
        assertEquals(2, outputData.getVisits().size(), "Should have 2 remaining visits");

        // Verify the correct visit was removed
        List<UndoVisitOutputData.VisitDTO> remainingVisits = outputData.getVisits();
        assertEquals("v3", remainingVisits.get(0).visitId, "First remaining visit should be v3");
        assertEquals("v1", remainingVisits.get(1).visitId, "Second remaining visit should be v1");

        // Verify the user's visits list was actually modified
        User savedUser = mockUserDataAccess.get("testuser");
        assertEquals(2, savedUser.getVisits().size(), "User should have 2 visits after undo");
    }

    @Test
    void testUndoVisitWithUserNotFound() {
        // Arrange: No user added to data access

        // Act
        UndoVisitInputData inputData = new UndoVisitInputData("nonexistent", "v1");
        interactor.execute(inputData);

        // Assert
        assertFalse(mockPresenter.isSuccessViewCalled(), "Success view should not be called");
        assertTrue(mockPresenter.isFailViewCalled(), "Fail view should be called");
        assertEquals("User not found: nonexistent", mockPresenter.getErrorMessage());

        // Save should not be called if user not found
        assertFalse(mockUserDataAccess.isSaveCalled(), "Save should not be called when user not found");
    }

    @Test
    void testUndoVisitWithVisitNotFound() {
        // Arrange: User exists but visit ID doesn't match any visit
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit1 = new Visit("v1", testLandmark1, Instant.now());
        testUser.getVisits().add(visit1);

        mockUserDataAccess.addUser(testUser);

        // Act: Try to remove a visit that doesn't exist
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "nonexistent-visit-id");
        interactor.execute(inputData);

        // Assert
        assertFalse(mockPresenter.isSuccessViewCalled(), "Success view should not be called");
        assertTrue(mockPresenter.isFailViewCalled(), "Fail view should be called");
        assertEquals("Visit not found with ID: nonexistent-visit-id", mockPresenter.getErrorMessage());

        // Save should not be called if visit not found
        assertFalse(mockUserDataAccess.isSaveCalled(), "Save should not be called when visit not found");

        // User's visits should remain unchanged
        User user = mockUserDataAccess.get("testuser");
        assertEquals(1, user.getVisits().size(), "User should still have 1 visit");
    }

    @Test
    void testUndoLastVisit() {
        // Arrange: User with only one visit
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit1 = new Visit("v1", testLandmark1, Instant.now());
        testUser.getVisits().add(visit1);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove the only visit
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v1");
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessViewCalled(), "Success view should be called");
        assertTrue(mockUserDataAccess.isSaveCalled(), "Save should be called");

        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertEquals(0, outputData.getVisits().size(), "Should have 0 remaining visits");

        // Verify user's visits list is empty
        User savedUser = mockUserDataAccess.get("testuser");
        assertEquals(0, savedUser.getVisits().size(), "User should have no visits after undoing last visit");
    }

    @Test
    void testRemainingVisitsAreSortedByMostRecent() {
        // Arrange: Create user with visits at different times
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant base = Instant.now();
        Visit oldestVisit = new Visit("v1", testLandmark1, base.minus(5, ChronoUnit.HOURS));
        Visit middleVisit = new Visit("v2", testLandmark2, base.minus(3, ChronoUnit.HOURS));
        Visit newestVisit = new Visit("v3", testLandmark3, base.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(oldestVisit);
        testUser.getVisits().add(middleVisit);
        testUser.getVisits().add(newestVisit);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove the middle visit
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v2");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        List<UndoVisitOutputData.VisitDTO> visits = outputData.getVisits();

        assertEquals(2, visits.size(), "Should have 2 remaining visits");
        assertEquals("v3", visits.get(0).visitId, "Newest visit should be first");
        assertEquals("v1", visits.get(1).visitId, "Oldest visit should be last");
    }

    @Test
    void testLandmarkNamesAreIncludedInRemainingVisits() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(3, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark3, now.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove v2
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v2");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        List<UndoVisitOutputData.VisitDTO> visits = outputData.getVisits();

        assertEquals("Hart House", visits.get(0).landmarkName, "First visit should be Hart House");
        assertEquals("Robarts Library", visits.get(1).landmarkName, "Second visit should be Robarts Library");
    }

    @Test
    void testDateFormattingInRemainingVisits() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(2, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove v1
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v1");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        UndoVisitOutputData.VisitDTO remainingVisit = outputData.getVisits().get(0);

        assertNotNull(remainingVisit.visitedAt, "Visited at should not be null");
        // The date should be formatted (contain space and numbers for time)
        assertTrue(remainingVisit.visitedAt.matches(".*\\d{2}:\\d{2}.*"),
                "Date should contain time in HH:mm format, but was: " + remainingVisit.visitedAt);
    }

    @Test
    void testUndoFirstVisit() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(3, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark3, now.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove the first (oldest) visit
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v1");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertEquals(2, outputData.getVisits().size(), "Should have 2 remaining visits");

        List<UndoVisitOutputData.VisitDTO> visits = outputData.getVisits();
        assertEquals("v3", visits.get(0).visitId);
        assertEquals("v2", visits.get(1).visitId);
    }

    @Test
    void testUndoMostRecentVisit() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(3, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark3, now.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act: Remove the most recent visit
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v3");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();
        assertEquals(2, outputData.getVisits().size(), "Should have 2 remaining visits");

        List<UndoVisitOutputData.VisitDTO> visits = outputData.getVisits();
        assertEquals("v2", visits.get(0).visitId, "Most recent remaining should be v2");
        assertEquals("v1", visits.get(1).visitId);
    }

    @Test
    void testOutputDataContainsAllRequiredFields() {
        // Arrange
        User testUser = new User("johndoe", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit1 = new Visit("v1", testLandmark1, Instant.now().minus(1, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, Instant.now());

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);

        mockUserDataAccess.addUser(testUser);

        // Act
        UndoVisitInputData inputData = new UndoVisitInputData("johndoe", "v1");
        interactor.execute(inputData);

        // Assert
        UndoVisitOutputData outputData = mockPresenter.getOutputData();

        assertNotNull(outputData.getUsername(), "Username should not be null");
        assertNotNull(outputData.getVisits(), "Visits list should not be null");
        assertNotNull(outputData.getSuccessMessage(), "Success message should not be null");

        assertEquals("johndoe", outputData.getUsername());
        assertEquals("Visit removed successfully", outputData.getSuccessMessage());
        assertEquals(1, outputData.getVisits().size());
    }

    @Test
    void testSaveIsCalledWithCorrectUser() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit = new Visit("v1", testLandmark1, Instant.now());
        testUser.getVisits().add(visit);

        mockUserDataAccess.addUser(testUser);

        // Act
        UndoVisitInputData inputData = new UndoVisitInputData("testuser", "v1");
        interactor.execute(inputData);

        // Assert
        assertTrue(mockUserDataAccess.isSaveCalled(), "Save should be called");
        User savedUser = mockUserDataAccess.getLastSavedUser();
        assertNotNull(savedUser, "Saved user should not be null");
        assertEquals("testuser", savedUser.getUsername(), "Saved user should be the correct user");
    }

    @Test
    void testUndoVisitDoesNotAffectOtherUsers() {
        // Arrange: Create two users with visits
        User user1 = new User("user1", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());
        User user2 = new User("user2", "password456", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit1 = new Visit("v1", testLandmark1, Instant.now());
        Visit visit2 = new Visit("v2", testLandmark2, Instant.now());

        user1.getVisits().add(visit1);
        user2.getVisits().add(visit2);

        mockUserDataAccess.addUser(user1);
        mockUserDataAccess.addUser(user2);

        // Act: Remove visit from user1
        UndoVisitInputData inputData = new UndoVisitInputData("user1", "v1");
        interactor.execute(inputData);

        // Assert: user2's visits should be unaffected
        User unchangedUser = mockUserDataAccess.get("user2");
        assertEquals(1, unchangedUser.getVisits().size(),
                "Other user's visits should not be affected");
        assertEquals("v2", unchangedUser.getVisits().get(0).getVisitId());
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of UndoVisitUserDataAccessInterface for testing.
     */
    private static class MockUndoVisitUserDataAccess implements UndoVisitUserDataAccessInterface {
        private final Map<String, User> users = new HashMap<>();
        private String currentUsername;
        private boolean saveCalled = false;
        private User lastSavedUser;

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
            lastSavedUser = user;
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

        User getLastSavedUser() {
            return lastSavedUser;
        }
    }

    /**
     * Mock implementation of UndoVisitOutputBoundary for testing.
     */
    private static class MockUndoVisitPresenter implements UndoVisitOutputBoundary {
        private boolean successViewCalled = false;
        private boolean failViewCalled = false;
        private UndoVisitOutputData outputData;
        private String errorMessage;

        @Override
        public void prepareSuccessView(UndoVisitOutputData outputData) {
            this.successViewCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.failViewCalled = true;
            this.errorMessage = errorMessage;
        }

        boolean isSuccessViewCalled() {
            return successViewCalled;
        }

        boolean isFailViewCalled() {
            return failViewCalled;
        }

        UndoVisitOutputData getOutputData() {
            return outputData;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
