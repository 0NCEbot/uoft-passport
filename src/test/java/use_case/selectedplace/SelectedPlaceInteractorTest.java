package use_case.selectedplace;

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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SelectedPlaceInteractor.
 * Tests the check-in use case including:
 * - Successful check-in
 * - Check-in with invalid user
 * - Check-in with invalid landmark
 * - Selecting a place to view details
 */
class SelectedPlaceInteractorTest {

    private MockLandmarkDAO mockLandmarkDAO;
    private MockUserDAO mockUserDAO;
    private MockSelectedPlacePresenter mockPresenter;
    private SelectedPlaceInteractor interactor;

    private Landmark testLandmark;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockLandmarkDAO = new MockLandmarkDAO();
        mockUserDAO = new MockUserDAO();
        mockPresenter = new MockSelectedPlacePresenter();
        interactor = new SelectedPlaceInteractor(mockLandmarkDAO, mockUserDAO, mockPresenter);

        // Setup test data
        LandmarkInfo info = new LandmarkInfo(
                "27 King's College Circle",
                "Beautiful historic building",
                "Mon-Fri: 9am-5pm",
                "Historic & Cultural"
        );
        testLandmark = new Landmark(
                "conv_hall",
                "Convocation Hall",
                new Location(43.6609, -79.3957),
                info,
                0
        );
        mockLandmarkDAO.addLandmark(testLandmark);

        testUser = new User("testuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDAO.addUser(testUser);
    }

    @Test
    void testSelectPlace() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Convocation Hall");

        // Act
        interactor.selectPlace(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentPlaceCalled(), "Present place should be called");
        SelectedPlaceOutputData outputData = mockPresenter.getPlaceData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals("testuser", outputData.getUsername(), "Username should match");
        assertEquals("Convocation Hall", outputData.getLandmarkName(), "Landmark name should match");
        assertEquals("Beautiful historic building", outputData.getDescription(), "Description should match");
    }

    @Test
    void testCheckInSuccess() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Convocation Hall");
        int initialVisitCount = testUser.getVisits().size();

        // Act
        interactor.checkIn(inputData);

        // Assert
        assertTrue(mockPresenter.isPresentPlaceCalled(), "Present place should be called after check-in");
        User savedUser = mockUserDAO.get("testuser");
        assertNotNull(savedUser, "User should exist");
        assertEquals(initialVisitCount + 1, savedUser.getVisits().size(), "Visit should be added");

        // Verify the visit details
        Visit lastVisit = savedUser.getVisits().get(savedUser.getVisits().size() - 1);
        assertEquals("Convocation Hall", lastVisit.getLandmark().getLandmarkName(),
                "Visit should be for correct landmark");
    }

    @Test
    void testCheckInWithInvalidUser() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("nonexistent", "Convocation Hall");

        // Act
        interactor.checkIn(inputData);

        // Assert
        assertFalse(mockPresenter.isPresentPlaceCalled(),
                "Present place should not be called when user doesn't exist");
    }

    @Test
    void testCheckInWithInvalidLandmark() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Nonexistent Landmark");

        // Act
        interactor.checkIn(inputData);

        // Assert
        assertFalse(mockPresenter.isPresentPlaceCalled(),
                "Present place should not be called when landmark doesn't exist");

        // Verify no visit was added
        User savedUser = mockUserDAO.get("testuser");
        assertEquals(0, savedUser.getVisits().size(), "No visit should be added for invalid landmark");
    }

    @Test
    void testMultipleCheckInsToSameLandmark() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Convocation Hall");

        // Act - check in twice
        interactor.checkIn(inputData);
        interactor.checkIn(inputData);

        // Assert
        User savedUser = mockUserDAO.get("testuser");
        assertEquals(2, savedUser.getVisits().size(), "Should allow multiple check-ins to same landmark");

        // Both visits should be to the same landmark
        for (Visit visit : savedUser.getVisits()) {
            assertEquals("Convocation Hall", visit.getLandmark().getLandmarkName(),
                    "All visits should be to Convocation Hall");
        }
    }

    @Test
    void testCheckInSavesUser() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Convocation Hall");

        // Act
        interactor.checkIn(inputData);

        // Assert
        assertTrue(mockUserDAO.isSaveCalled(), "User should be saved after check-in");
        assertEquals("testuser", mockUserDAO.getLastSavedUsername(), "Correct user should be saved");
    }

    @Test
    void testSelectPlaceWithInvalidLandmark() {
        // Arrange
        SelectedPlaceInputData inputData = new SelectedPlaceInputData("testuser", "Invalid Landmark");

        // Act
        interactor.selectPlace(inputData);

        // Assert - method should handle gracefully (no exception)
        // In the actual implementation, it prints an error but doesn't throw
        assertFalse(mockPresenter.isPresentPlaceCalled(),
                "Present place should not be called for invalid landmark");
    }

    // =============== Mock Classes ===============

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

    private static class MockUserDAO implements UserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private boolean saveCalled = false;
        private String lastSavedUsername;
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
            lastSavedUsername = user.getUsername();
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
        }

        @Override
        public void setCurrentUsername(String username) {
            this.currentUsername = username;
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }

        boolean isSaveCalled() {
            return saveCalled;
        }

        String getLastSavedUsername() {
            return lastSavedUsername;
        }
    }

    private static class MockSelectedPlacePresenter implements SelectedPlaceOutputBoundary {
        private boolean presentPlaceCalled = false;
        private boolean presentNotesCalled = false;
        private SelectedPlaceOutputData placeData;
        private SelectedPlaceOutputData notesData;

        @Override
        public void presentPlace(SelectedPlaceOutputData outputData) {
            presentPlaceCalled = true;
            placeData = outputData;
        }

        @Override
        public void presentNotes(SelectedPlaceOutputData outputData) {
            presentNotesCalled = true;
            notesData = outputData;
        }

        boolean isPresentPlaceCalled() {
            return presentPlaceCalled;
        }

        boolean isPresentNotesCalled() {
            return presentNotesCalled;
        }

        SelectedPlaceOutputData getPlaceData() {
            return placeData;
        }

        SelectedPlaceOutputData getNotesData() {
            return notesData;
        }
    }
}
