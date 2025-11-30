package use_case.myprogress;

import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MyProgressInteractor.
 * Tests the view progress use case including:
 * - Calculating visit statistics
 * - Calculating streaks
 * - Finding most visited landmark
 * - Calculating completion percentage
 */
class MyProgressInteractorTest {

    private MockUserDAO mockUserDAO;
    private MockLandmarkDAO mockLandmarkDAO;
    private MockMyProgressPresenter mockPresenter;
    private MyProgressInteractor interactor;

    private User testUser;
    private Landmark landmark1;
    private Landmark landmark2;
    private Landmark landmark3;

    @BeforeEach
    void setUp() {
        mockUserDAO = new MockUserDAO();
        mockLandmarkDAO = new MockLandmarkDAO();
        mockPresenter = new MockMyProgressPresenter();
        interactor = new MyProgressInteractor(mockUserDAO, mockLandmarkDAO, mockPresenter);

        // Setup landmarks
        landmark1 = createLandmark("id1", "Robarts Library");
        landmark2 = createLandmark("id2", "Bahen Centre");
        landmark3 = createLandmark("id3", "Convocation Hall");
        mockLandmarkDAO.addLandmark(landmark1);
        mockLandmarkDAO.addLandmark(landmark2);
        mockLandmarkDAO.addLandmark(landmark3);

        // Setup user with no visits initially
        testUser = new User("testuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDAO.addUser(testUser);
        mockUserDAO.setCurrentUsername("testuser");
    }

    @Test
    void testProgressWithNoVisits() {
        // Act
        interactor.execute();

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success should be called");
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertNotNull(data, "Output data should not be null");
        assertEquals(0, data.getUniqueLandmarksVisited(), "Unique visited should be 0");
        assertEquals(3, data.getUniqueLandmarksTotal(), "Total landmarks should be 3");
        assertEquals(0.0, data.getUniqueLandmarksCompletionPercentage(), "Percentage should be 0");
        assertEquals(0, data.getCurrentVisitStreak(), "Current streak should be 0");
        assertEquals(0, data.getLongestVisitStreak(), "Longest streak should be 0");
        assertEquals("", data.getMostVisitedLandmarkName(), "Most visited name should be empty");
    }

    @Test
    void testProgressWithSingleVisit() {
        // Arrange
        Visit visit = new Visit("v1", landmark1, Instant.now());
        testUser.getVisits().add(visit);

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(1, data.getUniqueLandmarksVisited(), "Should have 1 unique visit");
        assertEquals(33.3, data.getUniqueLandmarksCompletionPercentage(), 0.1, "Percentage should be ~33.3%");
        assertEquals(1, data.getTotalVisits(), "Total visits should be 1");
        assertEquals("Robarts Library", data.getMostVisitedLandmarkName(), "Most visited should be Robarts");
        assertEquals(1, data.getMostVisitedLandmarkCount(), "Most visited count should be 1");
    }

    @Test
    void testProgressWithMultipleVisitsToSameLandmark() {
        // Arrange - visit Robarts 3 times
        Instant now = Instant.now();
        testUser.getVisits().add(new Visit("v1", landmark1, now.minus(2, ChronoUnit.DAYS)));
        testUser.getVisits().add(new Visit("v2", landmark1, now.minus(1, ChronoUnit.DAYS)));
        testUser.getVisits().add(new Visit("v3", landmark1, now));

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(1, data.getUniqueLandmarksVisited(), "Should have 1 unique landmark");
        assertEquals(3, data.getTotalVisits(), "Total visits should be 3");
        assertEquals("Robarts Library", data.getMostVisitedLandmarkName(), "Most visited should be Robarts");
        assertEquals(3, data.getMostVisitedLandmarkCount(), "Most visited count should be 3");
    }

    @Test
    void testProgressWithVisitsToMultipleLandmarks() {
        // Arrange - visit all 3 landmarks
        testUser.getVisits().add(new Visit("v1", landmark1, Instant.now()));
        testUser.getVisits().add(new Visit("v2", landmark2, Instant.now()));
        testUser.getVisits().add(new Visit("v3", landmark3, Instant.now()));

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(3, data.getUniqueLandmarksVisited(), "Should have 3 unique landmarks");
        assertEquals(100.0, data.getUniqueLandmarksCompletionPercentage(), "Percentage should be 100%");
        assertEquals(3, data.getTotalVisits(), "Total visits should be 3");
    }

    @Test
    void testVisitTimePeriodCalculations() {
        // Arrange - visits at different times
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Instant lastWeek = now.minus(8, ChronoUnit.DAYS);
        Instant lastMonth = now.minus(35, ChronoUnit.DAYS);

        testUser.getVisits().add(new Visit("v1", landmark1, now)); // Today
        testUser.getVisits().add(new Visit("v2", landmark2, yesterday)); // This week
        testUser.getVisits().add(new Visit("v3", landmark3, lastWeek)); // This month (but not week)
        testUser.getVisits().add(new Visit("v4", landmark1, lastMonth)); // Outside month

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(1, data.getTotalVisitsToday(), "Should have 1 visit today");
        assertEquals(2, data.getTotalVisitsPastWeek(), "Should have 2 visits this week");
        assertEquals(3, data.getTotalVisitsPastMonth(), "Should have 3 visits this month");
        assertEquals(4, data.getTotalVisits(), "Should have 4 total visits");
    }

    @Test
    void testCurrentStreakCalculation() {
        // Arrange - visits on consecutive days including today
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();

        testUser.getVisits().add(new Visit("v1", landmark1,
                today.atStartOfDay(zoneId).toInstant()));
        testUser.getVisits().add(new Visit("v2", landmark2,
                today.minusDays(1).atStartOfDay(zoneId).toInstant()));
        testUser.getVisits().add(new Visit("v3", landmark3,
                today.minusDays(2).atStartOfDay(zoneId).toInstant()));

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(3, data.getCurrentVisitStreak(), "Current streak should be 3 consecutive days");
    }

    @Test
    void testLongestStreakCalculation() {
        // Arrange - two separate streaks: 2 days and 3 days
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();

        // Longest streak: 3 days (5-7 days ago)
        testUser.getVisits().add(new Visit("v1", landmark1,
                today.minusDays(7).atStartOfDay(zoneId).toInstant()));
        testUser.getVisits().add(new Visit("v2", landmark2,
                today.minusDays(6).atStartOfDay(zoneId).toInstant()));
        testUser.getVisits().add(new Visit("v3", landmark3,
                today.minusDays(5).atStartOfDay(zoneId).toInstant()));

        // Current streak: 2 days (today and yesterday)
        testUser.getVisits().add(new Visit("v4", landmark1,
                today.minusDays(1).atStartOfDay(zoneId).toInstant()));
        testUser.getVisits().add(new Visit("v5", landmark2,
                today.atStartOfDay(zoneId).toInstant()));

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals(3, data.getLongestVisitStreak(), "Longest streak should be 3 days");
        assertEquals(2, data.getCurrentVisitStreak(), "Current streak should be 2 days");
    }

    @Test
    void testMostVisitedLandmark() {
        // Arrange - Robarts visited 3 times, others 1 time each
        testUser.getVisits().add(new Visit("v1", landmark1, Instant.now()));
        testUser.getVisits().add(new Visit("v2", landmark1, Instant.now()));
        testUser.getVisits().add(new Visit("v3", landmark1, Instant.now()));
        testUser.getVisits().add(new Visit("v4", landmark2, Instant.now()));
        testUser.getVisits().add(new Visit("v5", landmark3, Instant.now()));

        // Act
        interactor.execute();

        // Assert
        MyProgressOutputData data = mockPresenter.getOutputData();
        assertEquals("Robarts Library", data.getMostVisitedLandmarkName(), "Most visited should be Robarts");
        assertEquals(3, data.getMostVisitedLandmarkCount(), "Most visited count should be 3");
    }

    @Test
    void testProgressWithNonExistentUser() {
        // Arrange - set current username to non-existent user
        mockUserDAO.setCurrentUsername("nonexistent");

        // Act
        interactor.execute();

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail should be called");
        assertEquals("User not found", mockPresenter.getFailMessage(), "Error message should match");
    }

    // =============== Helper Methods ===============

    private Landmark createLandmark(String id, String name) {
        LandmarkInfo info = new LandmarkInfo("Address", "Description", "Hours", "Type");
        return new Landmark(id, name, new Location(43.66, -79.39), info, 0);
    }

    // =============== Mock Classes ===============

    private static class MockUserDAO implements UserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private String currentUsername;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public void setCurrentUsername(String username) {
            this.currentUsername = username;
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public void save(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
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
            return landmarks.stream().anyMatch(l -> l.getLandmarkName().equals(landmarkName));
        }

        @Override
        public Landmark findByName(String name) {
            return landmarks.stream()
                    .filter(l -> l.getLandmarkName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static class MockMyProgressPresenter implements MyProgressOutputBoundary {
        private boolean successCalled = false;
        private boolean failCalled = false;
        private MyProgressOutputData outputData;
        private String failMessage;

        @Override
        public void prepareSuccessView(MyProgressOutputData data) {
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

        MyProgressOutputData getOutputData() {
            return outputData;
        }

        String getFailMessage() {
            return failMessage;
        }
    }
}
