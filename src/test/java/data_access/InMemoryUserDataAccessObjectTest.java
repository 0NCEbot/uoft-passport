package data_access;

import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for InMemoryUserDataAccessObject.
 * Tests in-memory user storage without persistence.
 */
class InMemoryUserDataAccessObjectTest {

    private InMemoryUserDataAccessObject userDAO;
    private UserFactory userFactory;

    @BeforeEach
    void setUp() {
        userDAO = new InMemoryUserDataAccessObject();
        userFactory = new UserFactory();
    }

    // ============ BASIC OPERATIONS ============

    @Test
    void testSaveAndGetUser() {
        // Arrange
        User user = userFactory.create("testuser", "password123");

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("testuser");

        // Assert
        assertNotNull(retrieved, "Retrieved user should not be null");
        assertEquals("testuser", retrieved.getUsername(), "Username should match");
        assertEquals("password123", retrieved.getPassword(), "Password should match");
    }

    @Test
    void testGetNonExistentUser() {
        // Act
        User user = userDAO.get("nonexistent");

        // Assert
        assertNull(user, "Non-existent user should return null");
    }

    @Test
    void testExistsByName() {
        // Arrange
        User user = userFactory.create("existinguser", "password");
        userDAO.save(user);

        // Act & Assert
        assertTrue(userDAO.existsByName("existinguser"), "User should exist");
        assertFalse(userDAO.existsByName("nonexistent"), "Non-existent user should not exist");
    }

    @Test
    void testExistsByNameWithEmptyStorage() {
        // Act & Assert
        assertFalse(userDAO.existsByName("anyuser"), "Should return false when storage is empty");
    }

    // ============ CURRENT USERNAME ============

    @Test
    void testSetAndGetCurrentUsername() {
        // Act
        userDAO.setCurrentUsername("currentuser");

        // Assert
        assertEquals("currentuser", userDAO.getCurrentUsername(), "Current username should match");
    }

    @Test
    void testGetCurrentUsernameWhenNotSet() {
        // Act
        String username = userDAO.getCurrentUsername();

        // Assert
        assertNull(username, "Current username should be null when not set");
    }

    @Test
    void testUpdateCurrentUsername() {
        // Arrange
        userDAO.setCurrentUsername("user1");

        // Act
        userDAO.setCurrentUsername("user2");

        // Assert
        assertEquals("user2", userDAO.getCurrentUsername(), "Current username should be updated");
    }

    // ============ MULTIPLE USERS ============

    @Test
    void testSaveMultipleUsers() {
        // Arrange
        User user1 = userFactory.create("user1", "pass1");
        User user2 = userFactory.create("user2", "pass2");
        User user3 = userFactory.create("user3", "pass3");

        // Act
        userDAO.save(user1);
        userDAO.save(user2);
        userDAO.save(user3);

        // Assert
        assertTrue(userDAO.existsByName("user1"), "User1 should exist");
        assertTrue(userDAO.existsByName("user2"), "User2 should exist");
        assertTrue(userDAO.existsByName("user3"), "User3 should exist");

        assertEquals("pass1", userDAO.get("user1").getPassword(), "User1 password should match");
        assertEquals("pass2", userDAO.get("user2").getPassword(), "User2 password should match");
        assertEquals("pass3", userDAO.get("user3").getPassword(), "User3 password should match");
    }

    @Test
    void testOverwriteExistingUser() {
        // Arrange
        User user1 = userFactory.create("sameuser", "password1");
        userDAO.save(user1);

        User user2 = userFactory.create("sameuser", "password2");

        // Act
        userDAO.save(user2);
        User retrieved = userDAO.get("sameuser");

        // Assert
        assertEquals("password2", retrieved.getPassword(), "Password should be overwritten");
    }

    // ============ USER DATA PRESERVATION ============

    @Test
    void testUserWithVisits() {
        // Arrange
        User user = userFactory.create("visitor", "password");
        LandmarkInfo info = new LandmarkInfo("Address", "Description", "Hours", "Library");
        Landmark landmark = new Landmark("id1", "Robarts Library", new Location(43.66, -79.39), info, 0);
        Visit visit = new Visit("v1", landmark, Instant.now());
        user.getVisits().add(visit);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("visitor");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(1, retrieved.getVisits().size(), "Should have 1 visit");
        assertEquals("v1", retrieved.getVisits().get(0).getVisitId(), "Visit ID should match");
    }

    @Test
    void testUserWithNotes() {
        // Arrange
        User user = userFactory.create("noteuser", "password");
        LandmarkInfo info = new LandmarkInfo("Address", "Description", "Hours", "Library");
        Landmark landmark = new Landmark("id1", "Robarts Library", new Location(43.66, -79.39), info, 0);
        Note note = new Note("n1", landmark, "Great place!", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("noteuser");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(1, retrieved.getPrivateNotes().size(), "Should have 1 note");
        assertEquals("Great place!", retrieved.getPrivateNotes().get(0).getContent(), "Note content should match");
    }

    @Test
    void testUserWithMultipleVisitsAndNotes() {
        // Arrange
        User user = userFactory.create("fulluser", "password");
        LandmarkInfo info = new LandmarkInfo("Address", "Description", "Hours", "Library");
        Landmark landmark = new Landmark("id1", "Robarts Library", new Location(43.66, -79.39), info, 0);

        Visit visit1 = new Visit("v1", landmark, Instant.now());
        Visit visit2 = new Visit("v2", landmark, Instant.now());
        user.getVisits().add(visit1);
        user.getVisits().add(visit2);

        Note note1 = new Note("n1", landmark, "Note 1", Instant.now(), Instant.now());
        Note note2 = new Note("n2", landmark, "Note 2", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note1);
        user.getPrivateNotes().add(note2);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("fulluser");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(2, retrieved.getVisits().size(), "Should have 2 visits");
        assertEquals(2, retrieved.getPrivateNotes().size(), "Should have 2 notes");
    }

    // ============ EDGE CASES ============

    @Test
    void testEmptyUsernameThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userFactory.create("", "password");
        }, "Creating user with empty username should throw exception");
    }

    @Test
    void testNullUsername() {
        // Act & Assert
        assertNull(userDAO.get(null), "Getting null username should return null");
        assertFalse(userDAO.existsByName(null), "Null username should not exist");
    }

    @Test
    void testEmptyPasswordThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userFactory.create("user", "");
        }, "Creating user with empty password should throw exception");
    }

    @Test
    void testUserCreationTimestampPreserved() {
        // Arrange
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        User user = userFactory.create("timestampuser", "password", createdAt,
            new ArrayList<>(), new ArrayList<>());

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("timestampuser");

        // Assert
        assertEquals(createdAt, retrieved.getCreatedAt(), "Creation timestamp should be preserved");
    }

    // ============ DATA ISOLATION ============

    @Test
    void testDataNotSharedBetweenInstances() {
        // Arrange
        InMemoryUserDataAccessObject dao1 = new InMemoryUserDataAccessObject();
        InMemoryUserDataAccessObject dao2 = new InMemoryUserDataAccessObject();

        User user = userFactory.create("isolateduser", "password");

        // Act
        dao1.save(user);

        // Assert
        assertTrue(dao1.existsByName("isolateduser"), "User should exist in dao1");
        assertFalse(dao2.existsByName("isolateduser"), "User should NOT exist in dao2");
    }

    @Test
    void testCurrentUsernameNotSharedBetweenInstances() {
        // Arrange
        InMemoryUserDataAccessObject dao1 = new InMemoryUserDataAccessObject();
        InMemoryUserDataAccessObject dao2 = new InMemoryUserDataAccessObject();

        // Act
        dao1.setCurrentUsername("user1");
        dao2.setCurrentUsername("user2");

        // Assert
        assertEquals("user1", dao1.getCurrentUsername(), "dao1 should have user1");
        assertEquals("user2", dao2.getCurrentUsername(), "dao2 should have user2");
    }

    // ============ STATE MODIFICATIONS ============

    @Test
    void testModifyRetrievedUser() {
        // Arrange
        User user = userFactory.create("modifyuser", "password");
        userDAO.save(user);

        // Act - modify the retrieved user
        User retrieved = userDAO.get("modifyuser");
        LandmarkInfo info = new LandmarkInfo("Address", "Description", "Hours", "Library");
        Landmark landmark = new Landmark("id1", "Test Landmark", new Location(43.66, -79.39), info, 0);
        Visit newVisit = new Visit("v1", landmark, Instant.now());
        retrieved.getVisits().add(newVisit);

        // Save the modified user back
        userDAO.save(retrieved);

        // Assert - retrieve again to verify modification
        User retrievedAgain = userDAO.get("modifyuser");
        assertEquals(1, retrievedAgain.getVisits().size(), "Modified visit should persist");
    }

    @Test
    void testClearCurrentUsername() {
        // Arrange
        userDAO.setCurrentUsername("someuser");

        // Act
        userDAO.setCurrentUsername(null);

        // Assert
        assertNull(userDAO.getCurrentUsername(), "Current username should be cleared");
    }
}
