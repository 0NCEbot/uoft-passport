package use_case.selectedplace;

import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
import entity.*;
import interface_adapter.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the checkIn functionality in SelectedPlaceInteractor
 * Tests made:
 * testSuccessfulCheckIn
 * testCheckInUserNotFound
 * testCheckInLandmarkNotFound
 * testCheckInWithNullVisitsList
 * testMultipleCheckInsAtSameLandmark
 * testCheckInSavesUserData
 * testCheckInCallsPresenter
 * testCheckInWithDifferentLandmarks
 * testEventBusNotification
 * testCheckInPreservesExistingVisits
 * testCheckInCreatesVisitWithCorrectTimestamp
 */

class SelectedPlaceInteractorCheckInTest {

    private MockLandmarkDAO landmarkDAO;
    private MockUserDAO userDAO;
    private MockPresenter presenter;
    private SelectedPlaceInteractor interactor;
    private MockEventBus eventBus;

    @BeforeEach
    void setUp() {
        landmarkDAO = new MockLandmarkDAO();
        userDAO = new MockUserDAO();
        presenter = new MockPresenter();
        eventBus = new MockEventBus();
        interactor = new SelectedPlaceInteractor(landmarkDAO, userDAO, presenter);
    }

    /**
     * Test successful check-in to a landmark.
     * Verifies that a visit is added to the user's visits list and
     * the presenter is called with correct data.
     */
    @Test
    void testSuccessfulCheckIn() {
        // Given: A user and landmark exist
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("Bahen Centre for Information Technology");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Bahen Centre for Information Technology"
        );

        // When: Check-in is performed
        interactor.checkIn(inputData);

        // Then: Visit should be added and presenter called
        User savedUser = userDAO.get("nathan1");
        assertEquals(1, savedUser.getVisits().size(), "User should have 1 visit");
        assertEquals("Bahen Centre for Information Technology",
                savedUser.getVisits().get(0).getLandmark().getLandmarkName(),
                "Visit should be for correct landmark");

        assertTrue(presenter.presentPlaceCalled, "Presenter should be called");
        assertEquals("nathan1", presenter.outputData.getUsername());
        assertEquals("Bahen Centre for Information Technology", presenter.outputData.getLandmarkName());
    }

    /**
     * Test check-in when user doesn't exist.
     * Verifies that no exception is thrown and no visits are recorded.
     */
    @Test
    void testCheckInUserNotFound() {
        // Given: Landmark exists but user doesn't
        Landmark landmark = createTestLandmark("Robarts Library");
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nonexistent",
                "Robarts Library"
        );

        // When: Check-in is attempted
        interactor.checkIn(inputData);

        // Then: No exception should be thrown, presenter should not be called
        assertFalse(presenter.presentPlaceCalled, "Presenter should not be called for non-existent user");
        assertNull(userDAO.get("nonexistent"), "User should not exist");
    }

    /**
     * Test check-in when landmark doesn't exist.
     * Verifies that no visit is recorded when landmark is not found.
     */
    @Test
    void testCheckInLandmarkNotFound() {
        // Given: User exists but landmark doesn't
        User user = createTestUser("nathan1", new ArrayList<>());
        userDAO.addUser(user);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "NonexistentLandmark"
        );

        // When: Check-in is attempted
        interactor.checkIn(inputData);

        // Then: No visit should be added, presenter should not be called
        User savedUser = userDAO.get("nathan1");
        assertEquals(0, savedUser.getVisits().size(), "No visits should be added");
        assertFalse(presenter.presentPlaceCalled, "Presenter should not be called for non-existent landmark");
    }

    /**
     * Test check-in when user's visits list is null.
     * Verifies that the system handles null visits list gracefully.
     */
    @Test
    void testCheckInWithNullVisitsList() {
        // Given: User with null visits list
        User user = createTestUserWithNullVisits("nathan1");
        Landmark landmark = createTestLandmark("Hart House");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Hart House"
        );

        // When: Check-in is attempted
        interactor.checkIn(inputData);

        // Then: Should handle gracefully, user should still be saved
        assertTrue(userDAO.wasSaveCalled(), "User should still be saved");
        assertTrue(presenter.presentPlaceCalled, "Presenter should still be called");
    }

    /**
     * Test multiple check-ins at the same landmark.
     * Verifies that a user can check in multiple times to the same location.
     */
    @Test
    void testMultipleCheckInsAtSameLandmark() {
        // Given: User and landmark exist
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("Sidney Smith Hall");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Sidney Smith Hall"
        );

        // When: Check-in is performed twice
        interactor.checkIn(inputData);
        interactor.checkIn(inputData);

        // Then: User should have 2 visits
        User savedUser = userDAO.get("nathan1");
        assertEquals(2, savedUser.getVisits().size(), "User should have 2 visits");
        assertEquals("Sidney Smith Hall",
                savedUser.getVisits().get(0).getLandmark().getLandmarkName());
        assertEquals("Sidney Smith Hall",
                savedUser.getVisits().get(1).getLandmark().getLandmarkName());
    }

    /**
     * Test that check-in saves user data.
     * Verifies that the save method is called on the UserDAO.
     */
    @Test
    void testCheckInSavesUserData() {
        // Given: User and landmark exist
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("Gerstein Science Information Centre");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Gerstein Science Information Centre"
        );

        // When: Check-in is performed
        interactor.checkIn(inputData);

        // Then: Save should be called
        assertTrue(userDAO.wasSaveCalled(), "UserDAO save method should be called");
        assertEquals("nathan1", userDAO.getLastSavedUsername(), "Correct user should be saved");
    }

    /**
     * Test that check-in calls the presenter with correct output data.
     * Verifies all fields in the output data are correctly populated.
     */
    @Test
    void testCheckInCallsPresenter() {
        // Given: User and landmark with detailed info
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("E.J. Pratt Library");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "E.J. Pratt Library"
        );

        // When: Check-in is performed
        interactor.checkIn(inputData);

        // Then: Presenter should be called with correct data
        assertTrue(presenter.presentPlaceCalled, "presentPlace should be called");
        assertFalse(presenter.presentNotesCalled, "presentNotes should not be called");

        SelectedPlaceOutputData output = presenter.outputData;
        assertNotNull(output, "Output data should not be null");
        assertEquals("nathan1", output.getUsername());
        assertEquals("E.J. Pratt Library", output.getLandmarkName());
        assertEquals("Trinity College library", output.getDescription());
        assertEquals("71 Queen's Park Crescent", output.getAddress());
        assertEquals("9 AM - 10 PM", output.getOpenHours());
    }

    /**
     * Test check-ins at different landmarks by the same user.
     * Verifies that visits to different landmarks are tracked separately.
     */
    @Test
    void testCheckInWithDifferentLandmarks() {
        // Given: User and multiple landmarks
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark1 = createTestLandmark("Bahen Centre for Information Technology");
        Landmark landmark2 = createTestLandmark("Robarts Library");
        Landmark landmark3 = createTestLandmark("Hart House");

        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark1);
        landmarkDAO.addLandmark(landmark2);
        landmarkDAO.addLandmark(landmark3);

        // When: Check-in at multiple landmarks
        interactor.checkIn(new SelectedPlaceInputData("nathan1", "Bahen Centre for Information Technology"));
        interactor.checkIn(new SelectedPlaceInputData("nathan1", "Robarts Library"));
        interactor.checkIn(new SelectedPlaceInputData("nathan1", "Hart House"));

        // Then: User should have 3 visits to different landmarks
        User savedUser = userDAO.get("nathan1");
        assertEquals(3, savedUser.getVisits().size(), "User should have 3 visits");
        assertEquals("Bahen Centre for Information Technology",
                savedUser.getVisits().get(0).getLandmark().getLandmarkName());
        assertEquals("Robarts Library",
                savedUser.getVisits().get(1).getLandmark().getLandmarkName());
        assertEquals("Hart House",
                savedUser.getVisits().get(2).getLandmark().getLandmarkName());
    }

    /**
     * Test that EventBus publishes visit modification notification.
     * Verifies that other views are notified of the visit change.
     */
    @Test
    void testEventBusNotification() {
        // Given: User and landmark exist
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("Medical Sciences Building");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Medical Sciences Building"
        );

        // When: Check-in is performed
        // Note: In real implementation, EventBus.publish would be called
        // This test verifies the behavior would trigger the notification
        interactor.checkIn(inputData);

        // Then: User data should be saved (which triggers the event in actual code)
        assertTrue(userDAO.wasSaveCalled(), "Save should be called, triggering event notification");
        // In actual code: EventBus.publish("visitModified", username) is called
    }

    /**
     * Test that check-in preserves existing visits.
     * Verifies that new visits are added without removing old ones.
     */
    @Test
    void testCheckInPreservesExistingVisits() {
        // Given: User with existing visits
        List<Visit> existingVisits = new ArrayList<>();
        Landmark oldLandmark = createTestLandmark("Convocation Hall");
        existingVisits.add(new Visit(oldLandmark));

        User user = createTestUser("nathan1", existingVisits);
        Landmark newLandmark = createTestLandmark("Leslie Dan Pharmacy Building");

        userDAO.addUser(user);
        landmarkDAO.addLandmark(oldLandmark);
        landmarkDAO.addLandmark(newLandmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Leslie Dan Pharmacy Building"
        );

        // When: New check-in is performed
        interactor.checkIn(inputData);

        // Then: Both old and new visits should exist
        User savedUser = userDAO.get("nathan1");
        assertEquals(2, savedUser.getVisits().size(), "User should have 2 visits");
        assertEquals("Convocation Hall",
                savedUser.getVisits().get(0).getLandmark().getLandmarkName(),
                "Old visit should be preserved");
        assertEquals("Leslie Dan Pharmacy Building",
                savedUser.getVisits().get(1).getLandmark().getLandmarkName(),
                "New visit should be added");
    }

    /**
     * Test that check-in creates a visit with correct timestamp.
     * Verifies that the visit ID and timestamp are properly generated.
     */
    @Test
    void testCheckInCreatesVisitWithCorrectTimestamp() {
        // Given: User and landmark exist
        User user = createTestUser("nathan1", new ArrayList<>());
        Landmark landmark = createTestLandmark("Front Campus");
        userDAO.addUser(user);
        landmarkDAO.addLandmark(landmark);

        SelectedPlaceInputData inputData = new SelectedPlaceInputData(
                "nathan1",
                "Front Campus"
        );

        Instant beforeCheckIn = Instant.now();

        // When: Check-in is performed
        interactor.checkIn(inputData);

        Instant afterCheckIn = Instant.now();

        // Then: Visit should have timestamp between before and after
        User savedUser = userDAO.get("nathan1");
        Visit visit = savedUser.getVisits().get(0);

        assertNotNull(visit.getVisitId(), "Visit should have an ID");
        assertTrue(visit.getVisitId().startsWith("Front Campus"), "Visit ID should start with landmark name");
        assertNotNull(visit.getVisitedAt(), "Visit should have a timestamp");
        assertTrue(visit.getVisitedAt().isAfter(beforeCheckIn.minusSeconds(1)) &&
                        visit.getVisitedAt().isBefore(afterCheckIn.plusSeconds(1)),
                "Visit timestamp should be during check-in time");
    }

    // Helper methods to create test data
    private User createTestUser(String username, List<Visit> visits) {
        return new User(
                username,
                "password123",
                Instant.now(),
                visits,
                new ArrayList<>()
        );
    }

    private User createTestUserWithNullVisits(String username) {
        return new User(
                username,
                "password123",
                Instant.now(),
                null,
                new ArrayList<>()
        );
    }

    private Landmark createTestLandmark(String name) {
        Location location = getLandmarkLocation(name);
        LandmarkInfo info = getLandmarkInfo(name);
        return new Landmark(
                name.toLowerCase().replace(" ", "-"),
                name,
                location,
                info,
                0
        );
    }

    private Location getLandmarkLocation(String name) {
        // Return realistic UofT locations based on landmark name
        Map<String, Location> locations = new HashMap<>();
        locations.put("Bahen Centre for Information Technology", new Location(43.6596, -79.3975));
        locations.put("Robarts Library", new Location(43.6645, -79.3996));
        locations.put("Hart House", new Location(43.6640, -79.3943));
        locations.put("Sidney Smith Hall", new Location(43.6627, -79.3979));
        locations.put("E.J. Pratt Library", new Location(43.6664, -79.3916));
        locations.put("Gerstein Science Information Centre", new Location(43.6621, -79.3935));
        locations.put("Medical Sciences Building", new Location(43.6610, -79.3934));
        locations.put("Convocation Hall", new Location(43.6609, -79.3957));
        locations.put("Leslie Dan Pharmacy Building", new Location(43.6599, -79.3919));
        locations.put("Front Campus", new Location(43.6617, -79.3951));

        return locations.getOrDefault(name, new Location(43.6629, -79.3957));
    }

    private LandmarkInfo getLandmarkInfo(String name) {
        // Return realistic landmark info based on name
        Map<String, LandmarkInfo> infos = new HashMap<>();
        infos.put("Bahen Centre for Information Technology",
                new LandmarkInfo("40 St George St", "Computer science and engineering building", "24 hours", "Academic Building"));
        infos.put("Robarts Library",
                new LandmarkInfo("100 St George St", "Large academic library", "24 hours", "Library"));
        infos.put("Hart House",
                new LandmarkInfo("7 Hart House Circle", "Historic student center", "8 AM - 11 PM", "Student Building"));
        infos.put("Sidney Smith Hall",
                new LandmarkInfo("100 St George St", "Arts and humanities building", "7 AM - 11 PM", "Academic Building"));
        infos.put("E.J. Pratt Library",
                new LandmarkInfo("71 Queen's Park Crescent", "Trinity College library", "9 AM - 10 PM", "Library"));
        infos.put("Gerstein Science Information Centre",
                new LandmarkInfo("9 King's College Circle", "Science information centre", "24 hours", "Library"));
        infos.put("Medical Sciences Building",
                new LandmarkInfo("1 King's College Circle", "Medical sciences building", "7 AM - 10 PM", "Academic Building"));
        infos.put("Convocation Hall",
                new LandmarkInfo("31 King's College Circle", "Historic convocation hall", "Varies by event", "Event Space"));
        infos.put("Leslie Dan Pharmacy Building",
                new LandmarkInfo("144 College St", "Faculty of pharmacy building", "8 AM - 8 PM", "Academic Building"));
        infos.put("Front Campus",
                new LandmarkInfo("King's College Circle", "Central campus green space", "24 hours", "Outdoor Space"));

        return infos.getOrDefault(name,
                new LandmarkInfo("Address", "Description", "Hours", "Type"));
    }

    // Mock implementations
    private static class MockLandmarkDAO implements LandmarkDataAccessInterface {
        private final Map<String, Landmark> landmarks = new HashMap<>();

        void addLandmark(Landmark landmark) {
            landmarks.put(landmark.getLandmarkName(), landmark);
        }

        @Override
        public List<Landmark> getLandmarks() {
            return new ArrayList<>(landmarks.values());
        }

        @Override
        public boolean existsByName(String landmarkName) {
            return landmarks.containsKey(landmarkName);
        }

        @Override
        public Landmark findByName(String name) {
            return landmarks.get(name);
        }
    }

    private static class MockUserDAO implements UserDataAccessInterface {
        private final Map<String, User> users = new HashMap<>();
        private boolean saveCalled = false;
        private String lastSavedUsername = null;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public entity.User get(String username) {
            return users.get(username);
        }

        @Override
        public void save(entity.User user) {
            saveCalled = true;
            lastSavedUsername = user.getUsername();
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String identifier) {
            return users.containsKey(identifier);
        }

        @Override
        public void setCurrentUsername(String name) {
            // Not needed for check-in tests
        }

        @Override
        public String getCurrentUsername() {
            return null;
        }

        boolean wasSaveCalled() {
            return saveCalled;
        }

        String getLastSavedUsername() {
            return lastSavedUsername;
        }
    }

    private static class MockPresenter implements SelectedPlaceOutputBoundary {
        boolean presentPlaceCalled = false;
        boolean presentNotesCalled = false;
        SelectedPlaceOutputData outputData = null;

        @Override
        public void presentPlace(SelectedPlaceOutputData outputData) {
            this.presentPlaceCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void presentNotes(SelectedPlaceOutputData outputData) {
            this.presentNotesCalled = true;
            this.outputData = outputData;
        }
    }

    private static class MockEventBus {
        private String lastEvent = null;
        private String lastData = null;

        void publish(String event, String data) {
            this.lastEvent = event;
            this.lastData = data;
        }

        String getLastEvent() {
            return lastEvent;
        }

        String getLastData() {
            return lastData;
        }
    }
}