package use_case.viewhistory;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.User;
import entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ViewHistoryInteractor.
 * Tests the view history use case including:
 * - Retrieving user visit history
 * - Formatting dates in different formats (Today, Yesterday, Day of Week, Full Date)
 * - Sorting visits by most recent first
 * - Handling error cases (user not found, null visits)
 */
class ViewHistoryInteractorTest {

    private MockViewHistoryUserDataAccess mockUserDataAccess;
    private MockViewHistoryPresenter mockPresenter;
    private ViewHistoryInteractor interactor;

    private Landmark testLandmark1;
    private Landmark testLandmark2;
    private Landmark testLandmark3;

    @BeforeEach
    void setUp() {
        mockUserDataAccess = new MockViewHistoryUserDataAccess();
        mockPresenter = new MockViewHistoryPresenter();
        interactor = new ViewHistoryInteractor(mockUserDataAccess, mockPresenter);

        // Setup test landmarks
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Description 1", "Hours 1", "Library");
        testLandmark1 = new Landmark("id1", "Robarts Library", new Location(43.6645, -79.3996), info1, 0);

        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Description 2", "Hours 2", "Engineering & Technology");
        testLandmark2 = new Landmark("id2", "Bahen Centre", new Location(43.6596, -79.3975), info2, 0);

        LandmarkInfo info3 = new LandmarkInfo("Address 3", "Description 3", "Hours 3", "Student Life");
        testLandmark3 = new Landmark("id3", "Hart House", new Location(43.6643, -79.3947), info3, 0);
    }

    @Test
    void testExecuteWithValidUserAndVisits() {
        // Arrange: Create user with visits
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        // Add visits at different times
        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(1, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark3, now.minus(3, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessViewCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailViewCalled(), "Fail view should not be called");

        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals("testuser", outputData.getUsername(), "Username should match");
        assertEquals(3, outputData.getVisits().size(), "Should have 3 visits");

        // Check that visits are sorted by most recent first
        List<ViewHistoryOutputData.VisitDTO> visits = outputData.getVisits();
        assertEquals("v1", visits.get(0).visitId, "First visit should be v1 (most recent)");
        assertEquals("v2", visits.get(1).visitId, "Second visit should be v2");
        assertEquals("v3", visits.get(2).visitId, "Third visit should be v3 (oldest)");
    }

    @Test
    void testExecuteWithUserNotFound() {
        // Arrange: No user added to data access

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("nonexistent");
        interactor.execute(inputData);

        // Assert
        assertFalse(mockPresenter.isSuccessViewCalled(), "Success view should not be called");
        assertTrue(mockPresenter.isFailViewCalled(), "Fail view should be called");
        assertEquals("User not found: nonexistent", mockPresenter.getErrorMessage());
    }

    @Test
    void testExecuteWithUserWithNoVisits() {
        // Arrange: User with empty visits list
        User emptyUser = new User("emptyuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDataAccess.addUser(emptyUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("emptyuser");
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessViewCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailViewCalled(), "Fail view should not be called");

        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals("emptyuser", outputData.getUsername());
        assertEquals(0, outputData.getVisits().size(), "Should have 0 visits");
    }

    @Test
    void testDateFormattingToday() {
        // Arrange: Create visit from today
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant today = Instant.now();
        Visit todayVisit = new Visit("v1", testLandmark1, today);
        testUser.getVisits().add(todayVisit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData);
        assertEquals(1, outputData.getVisits().size());

        String formattedDate = outputData.getVisits().get(0).visitedAt;
        assertTrue(formattedDate.startsWith("Today, "),
                "Date should start with 'Today, ' but was: " + formattedDate);
    }

    @Test
    void testDateFormattingYesterday() {
        // Arrange: Create visit from yesterday
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Visit yesterdayVisit = new Visit("v1", testLandmark1, yesterday);
        testUser.getVisits().add(yesterdayVisit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData);
        assertEquals(1, outputData.getVisits().size());

        String formattedDate = outputData.getVisits().get(0).visitedAt;
        assertTrue(formattedDate.startsWith("Yesterday, "),
                "Date should start with 'Yesterday, ' but was: " + formattedDate);
    }

    @Test
    void testDateFormattingDayOfWeek() {
        // Arrange: Create visit from 3 days ago (within last week)
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        Visit weekVisit = new Visit("v1", testLandmark1, threeDaysAgo);
        testUser.getVisits().add(weekVisit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData);
        assertEquals(1, outputData.getVisits().size());

        String formattedDate = outputData.getVisits().get(0).visitedAt;
        // Should show day of week (e.g., "Monday, 5:00PM")
        // Check that it doesn't start with "Today" or "Yesterday"
        assertFalse(formattedDate.startsWith("Today"),
                "Should not start with 'Today' for 3 days ago");
        assertFalse(formattedDate.startsWith("Yesterday"),
                "Should not start with 'Yesterday' for 3 days ago");
        // Should contain a comma (day of week formatting)
        assertTrue(formattedDate.contains(","),
                "Date should contain comma for day of week format");
    }

    @Test
    void testDateFormattingFullDate() {
        // Arrange: Create visit from 10 days ago (older than a week)
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS);
        Visit oldVisit = new Visit("v1", testLandmark1, tenDaysAgo);
        testUser.getVisits().add(oldVisit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData);
        assertEquals(1, outputData.getVisits().size());

        String formattedDate = outputData.getVisits().get(0).visitedAt;
        // Should show full date (e.g., "Monday, December 1, 5:00PM")
        assertFalse(formattedDate.startsWith("Today"),
                "Should not start with 'Today' for old visits");
        assertFalse(formattedDate.startsWith("Yesterday"),
                "Should not start with 'Yesterday' for old visits");
        assertTrue(formattedDate.contains(","),
                "Full date should contain comma");
    }

    @Test
    void testVisitSortingByMostRecent() {
        // Arrange: Create user with visits at different times
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant base = Instant.now();
        Visit oldestVisit = new Visit("v1", testLandmark1, base.minus(10, ChronoUnit.DAYS));
        Visit middleVisit = new Visit("v2", testLandmark2, base.minus(5, ChronoUnit.DAYS));
        Visit newestVisit = new Visit("v3", testLandmark3, base.minus(1, ChronoUnit.DAYS));

        // Add in random order
        testUser.getVisits().add(middleVisit);
        testUser.getVisits().add(oldestVisit);
        testUser.getVisits().add(newestVisit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        List<ViewHistoryOutputData.VisitDTO> visits = outputData.getVisits();

        assertEquals(3, visits.size(), "Should have 3 visits");
        assertEquals("v3", visits.get(0).visitId, "Newest visit should be first");
        assertEquals("v2", visits.get(1).visitId, "Middle visit should be second");
        assertEquals("v1", visits.get(2).visitId, "Oldest visit should be last");
    }

    @Test
    void testMultipleVisitsToSameLandmark() {
        // Arrange: Create user with multiple visits to the same landmark
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant base = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, base.minus(3, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark1, base.minus(2, ChronoUnit.HOURS));
        Visit visit3 = new Visit("v3", testLandmark1, base.minus(1, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        List<ViewHistoryOutputData.VisitDTO> visits = outputData.getVisits();

        assertEquals(3, visits.size(), "Should have 3 visits");
        // All should be to the same landmark
        assertEquals("Robarts Library", visits.get(0).landmarkName);
        assertEquals("Robarts Library", visits.get(1).landmarkName);
        assertEquals("Robarts Library", visits.get(2).landmarkName);
        // But with different visit IDs
        assertEquals("v3", visits.get(0).visitId);
        assertEquals("v2", visits.get(1).visitId);
        assertEquals("v1", visits.get(2).visitId);
    }

    @Test
    void testLandmarkNamesAreIncludedInOutput() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Instant now = Instant.now();
        Visit visit1 = new Visit("v1", testLandmark1, now.minus(1, ChronoUnit.HOURS));
        Visit visit2 = new Visit("v2", testLandmark2, now.minus(2, ChronoUnit.HOURS));

        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        List<ViewHistoryOutputData.VisitDTO> visits = outputData.getVisits();

        assertEquals("Robarts Library", visits.get(0).landmarkName);
        assertEquals("Bahen Centre", visits.get(1).landmarkName);
    }

    @Test
    void testAllVisitDataFieldsArePopulated() {
        // Arrange
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());

        Visit visit = new Visit("v1", testLandmark1, Instant.now());
        testUser.getVisits().add(visit);

        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("testuser");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        ViewHistoryOutputData.VisitDTO visitDTO = outputData.getVisits().get(0);

        assertNotNull(visitDTO.visitId, "Visit ID should not be null");
        assertNotNull(visitDTO.landmarkName, "Landmark name should not be null");
        assertNotNull(visitDTO.visitedAt, "Visited at should not be null");

        assertEquals("v1", visitDTO.visitId);
        assertEquals("Robarts Library", visitDTO.landmarkName);
        assertTrue(visitDTO.visitedAt.contains(","), "Formatted date should contain comma");
    }

    @Test
    void testUsernameIsIncludedInOutputData() {
        // Arrange
        User testUser = new User("johndoe", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDataAccess.addUser(testUser);

        // Act
        ViewHistoryInputData inputData = new ViewHistoryInputData("johndoe");
        interactor.execute(inputData);

        // Assert
        ViewHistoryOutputData outputData = mockPresenter.getOutputData();
        assertEquals("johndoe", outputData.getUsername(), "Username should be included in output");
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of ViewHistoryUserDataAccessInterface for testing.
     */
    private static class MockViewHistoryUserDataAccess implements ViewHistoryUserDataAccessInterface {
        private final Map<String, User> users = new HashMap<>();
        private String currentUsername;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }

        void setCurrentUsername(String username) {
            this.currentUsername = username;
        }
    }

    /**
     * Mock implementation of ViewHistoryOutputBoundary for testing.
     */
    private static class MockViewHistoryPresenter implements ViewHistoryOutputBoundary {
        private boolean successViewCalled = false;
        private boolean failViewCalled = false;
        private ViewHistoryOutputData outputData;
        private String errorMessage;

        @Override
        public void prepareSuccessView(ViewHistoryOutputData outputData) {
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

        ViewHistoryOutputData getOutputData() {
            return outputData;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
