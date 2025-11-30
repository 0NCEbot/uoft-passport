package use_case.browselandmarks;

import data_access.LandmarkDataAccessInterface;
import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import entity.User;
import entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.login.LoginUserDataAccessInterface;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BrowseLandmarksInteractor.
 * Tests the browse landmarks use case including:
 * - Loading landmarks with visit counts
 * - Filtering by visit status
 * - Filtering by landmark type
 */
class BrowseLandmarksInteractorTest {

    private MockLandmarkDataAccess mockLandmarkDAO;
    private MockLoginUserDataAccess mockUserDAO;
    private MockBrowseLandmarksPresenter mockPresenter;
    private BrowseLandmarksInteractor browseInteractor;

    private Landmark landmark1;
    private Landmark landmark2;
    private Landmark landmark3;

    @BeforeEach
    void setUp() {
        mockLandmarkDAO = new MockLandmarkDataAccess();
        mockUserDAO = new MockLoginUserDataAccess();
        mockPresenter = new MockBrowseLandmarksPresenter();
        browseInteractor = new BrowseLandmarksInteractor(mockLandmarkDAO, mockPresenter, mockUserDAO);

        // Setup test landmarks
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Description 1", "Hours 1", "Library");
        landmark1 = new Landmark("id1", "Robarts Library", new Location(43.6645, -79.3996), info1, 0);

        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Description 2", "Hours 2", "Engineering & Technology");
        landmark2 = new Landmark("id2", "Bahen Centre", new Location(43.6596, -79.3975), info2, 0);

        LandmarkInfo info3 = new LandmarkInfo("Address 3", "Description 3", "Hours 3", "Library");
        landmark3 = new Landmark("id3", "Gerstein Library", new Location(43.6621, -79.3935), info3, 0);

        mockLandmarkDAO.addLandmark(landmark1);
        mockLandmarkDAO.addLandmark(landmark2);
        mockLandmarkDAO.addLandmark(landmark3);
    }

    @Test
    void testLoadLandmarksWithNoUser() {
        // Arrange - no current user set

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Present landmarks should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks = mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should present all 3 landmarks");

        // All visit counts should be 0 when no user is logged in
        for (BrowseLandmarksOutputData.LandmarkDTO dto : landmarks) {
            assertEquals(0, dto.visitCount, "Visit count should be 0 when no user logged in");
        }
    }

    @Test
    void testLoadLandmarksWithUserAndVisits() {
        // Arrange - create user with visits
        User testUser = new User("testuser", "password", Instant.now(), new ArrayList<>(), new ArrayList<>());

        // User has visited landmark1 (Robarts) twice and landmark3 (Gerstein) once
        Visit visit1 = new Visit("v1", landmark1, Instant.now());
        Visit visit2 = new Visit("v2", landmark1, Instant.now());
        Visit visit3 = new Visit("v3", landmark3, Instant.now());
        testUser.getVisits().add(visit1);
        testUser.getVisits().add(visit2);
        testUser.getVisits().add(visit3);

        mockUserDAO.addUser(testUser);
        mockUserDAO.setCurrentUsername("testuser");

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Present landmarks should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks = mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should present all 3 landmarks");

        // Check visit counts
        BrowseLandmarksOutputData.LandmarkDTO robarts = landmarks.stream()
                .filter(l -> l.name.equals("Robarts Library"))
                .findFirst()
                .orElse(null);
        assertNotNull(robarts, "Robarts should be in the list");
        assertEquals(2, robarts.visitCount, "Robarts should have 2 visits");

        BrowseLandmarksOutputData.LandmarkDTO gerstein = landmarks.stream()
                .filter(l -> l.name.equals("Gerstein Library"))
                .findFirst()
                .orElse(null);
        assertNotNull(gerstein, "Gerstein should be in the list");
        assertEquals(1, gerstein.visitCount, "Gerstein should have 1 visit");

        BrowseLandmarksOutputData.LandmarkDTO bahen = landmarks.stream()
                .filter(l -> l.name.equals("Bahen Centre"))
                .findFirst()
                .orElse(null);
        assertNotNull(bahen, "Bahen should be in the list");
        assertEquals(0, bahen.visitCount, "Bahen should have 0 visits");
    }

    @Test
    void testLandmarkTypesAreIncluded() {
        // Arrange
        mockUserDAO.setCurrentUsername("testuser");

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks = mockPresenter.getLandmarksPresented();

        // Check that types are correctly set
        long libraryCount = landmarks.stream()
                .filter(l -> l.type.equals("Library"))
                .count();
        assertEquals(2, libraryCount, "Should have 2 libraries");

        long engineeringCount = landmarks.stream()
                .filter(l -> l.type.equals("Engineering & Technology"))
                .count();
        assertEquals(1, engineeringCount, "Should have 1 engineering building");
    }

    @Test
    void testLoadLandmarksWithNonExistentUser() {
        // Arrange - set current username to user that doesn't exist
        mockUserDAO.setCurrentUsername("nonexistent");

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Present landmarks should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks = mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should present all 3 landmarks");

        // All visit counts should be 0 when user doesn't exist
        for (BrowseLandmarksOutputData.LandmarkDTO dto : landmarks) {
            assertEquals(0, dto.visitCount, "Visit count should be 0 when user doesn't exist");
        }
    }

    @Test
    void testLoadLandmarksWithExistingUserButNoVisits() {
        // Arrange: user exists, but has an EMPTY visits list
        User emptyUser = new User("emptyuser", "password",
                Instant.now(), new ArrayList<>(), new ArrayList<>());

        // NOTE: we do NOT add any visits to emptyUser.getVisits()
        mockUserDAO.addUser(emptyUser);
        mockUserDAO.setCurrentUsername("emptyuser");

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Presenter should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks =
                mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should still present all 3 landmarks");

        // All visit counts should still be 0 (branch where currentUser exists
        // but currentUser.getVisits().isEmpty() is covered)
        for (BrowseLandmarksOutputData.LandmarkDTO dto : landmarks) {
            assertEquals(0, dto.visitCount,
                    "Visit count should be 0 when user has no visits");
        }
    }

    @Test
    void testLandmarkWithNullInfoUsesDefaultTypeCampusLocation() {
        // Arrange: add a landmark whose LandmarkInfo is null
        Landmark landmarkWithNullInfo = new Landmark(
                "id4",
                "Mystery Spot",
                new Location(43.0, -79.0),
                null,      // LandmarkInfo is null
                0
        );
        mockLandmarkDAO.addLandmark(landmarkWithNullInfo);

        // No current user needed for this test
        browseInteractor.loadLandmarks();

        // Assert
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks =
                mockPresenter.getLandmarksPresented();

        BrowseLandmarksOutputData.LandmarkDTO mystery = landmarks.stream()
                .filter(l -> l.name.equals("Mystery Spot"))
                .findFirst()
                .orElse(null);

        assertNotNull(mystery, "Mystery Spot DTO should be present");
        assertEquals("Campus Location", mystery.type,
                "Landmark with null LandmarkInfo/type should default to 'Campus Location'");
    }

    /**
     * NEW: User exists but getVisits() returns null.
     * This hits the branches where:
     * - currentUser.getVisits() == null (inside the debug if)
     * - user != null && user.getVisits() != null  -> FALSE because second part is false
     *   in both the "print all visits" block and the DTO visitCount block.
     */
    @Test
    void testLoadLandmarksWithUserAndNullVisitsList() {
        // Arrange: user exists but visits list is null
        User nullVisitsUser = new User(
                "nullvisits",
                "password",
                Instant.now(),
                new ArrayList<>(),
                null              // visits list is null
        );

        mockUserDAO.addUser(nullVisitsUser);
        mockUserDAO.setCurrentUsername("nullvisits");

        // Act
        browseInteractor.loadLandmarks();

        // Assert: no crash, still presents all landmarks with visitCount = 0
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Presenter should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks =
                mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should present the original 3 landmarks");

        for (BrowseLandmarksOutputData.LandmarkDTO dto : landmarks) {
            assertEquals(0, dto.visitCount,
                    "When visits list is null, visit count should be 0 for all landmarks");
        }
    }
    private static class NullVisitsUser extends User {
        NullVisitsUser(String username) {
            super(username, "password", Instant.now(),
                    new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public List<Visit> getVisits() {
            return null;
        }
    }


    @Test
    void testLoadLandmarksWithUserAndNullVisitsList2() {
        // Arrange: user exists but getVisits() returns null
        User nullVisitsUser = new NullVisitsUser("nullvisits");

        mockUserDAO.addUser(nullVisitsUser);
        mockUserDAO.setCurrentUsername("nullvisits");

        // Act
        browseInteractor.loadLandmarks();

        // Assert: no crash, still presents all landmarks with visitCount = 0
        assertTrue(mockPresenter.isPresentLandmarksCalled(), "Presenter should be called");
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks =
                mockPresenter.getLandmarksPresented();
        assertEquals(3, landmarks.size(), "Should present the original 3 landmarks");

        for (BrowseLandmarksOutputData.LandmarkDTO dto : landmarks) {
            assertEquals(0, dto.visitCount,
                    "When visits list is null, visit count should be 0 for all landmarks");
        }
    }


    /**
     * NEW: LandmarkInfo exists, but its type is null.
     * This covers the branch where:
     * - l.getLandmarkInfo() != null  (true)
     * - l.getLandmarkInfo().getType() == null (so the overall if condition is false)
     */
    @Test
    void testLandmarkWithNullTypeUsesDefaultCampusLocation() {
        // Arrange: LandmarkInfo with a null type
        LandmarkInfo infoWithNullType = new LandmarkInfo(
                "Address X",
                "Description X",
                "Hours X",
                null              // type is null
        );
        Landmark landmarkWithNullType = new Landmark(
                "id5",
                "Another Mystery",
                new Location(44.0, -79.1),
                infoWithNullType,
                0
        );
        mockLandmarkDAO.addLandmark(landmarkWithNullType);

        // Act
        browseInteractor.loadLandmarks();

        // Assert
        List<BrowseLandmarksOutputData.LandmarkDTO> landmarks =
                mockPresenter.getLandmarksPresented();

        BrowseLandmarksOutputData.LandmarkDTO anotherMystery = landmarks.stream()
                .filter(l -> l.name.equals("Another Mystery"))
                .findFirst()
                .orElse(null);

        assertNotNull(anotherMystery, "Another Mystery DTO should be present");
        assertEquals("Campus Location", anotherMystery.type,
                "Landmark with null type in LandmarkInfo should default to 'Campus Location'");
    }

    // =============== Mock Classes ===============

    private static class MockLandmarkDataAccess implements LandmarkDataAccessInterface {
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

    private static class MockLoginUserDataAccess implements LoginUserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();
        private String currentUsername;

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
        }

        @Override
        public User get(String username) {
            return users.get(username);
        }

        @Override
        public void setCurrentUsername(String name) {
            this.currentUsername = name;
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }
    }

    private static class MockBrowseLandmarksPresenter implements BrowseLandmarksOutputBoundary {
        private boolean presentLandmarksCalled = false;
        private List<BrowseLandmarksOutputData.LandmarkDTO> landmarksPresented;

        @Override
        public void presentLandmarks(BrowseLandmarksOutputData outputData) {
            presentLandmarksCalled = true;
            landmarksPresented = outputData.getLandmarks();
        }

        boolean isPresentLandmarksCalled() {
            return presentLandmarksCalled;
        }

        List<BrowseLandmarksOutputData.LandmarkDTO> getLandmarksPresented() {
            return landmarksPresented;
        }
    }
    @Test
    void testLoadLandmarksCrashesWhenVisitsContainNull() {
        // Arrange: user exists and visits list contains a null Visit
        User badUser = new User(
                "baduser",
                "password",
                Instant.now(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        // Insert a null visit; this will cause the debug printing loop
        // in BrowseLandmarksInteractor to throw a NullPointerException.
        badUser.getVisits().add(null);

        mockUserDAO.addUser(badUser);
        mockUserDAO.setCurrentUsername("baduser");

        // Act + Assert: we explicitly assert that the current implementation
        // throws a NullPointerException when it encounters a null Visit.
        assertThrows(NullPointerException.class, () -> browseInteractor.loadLandmarks());
    }

}
