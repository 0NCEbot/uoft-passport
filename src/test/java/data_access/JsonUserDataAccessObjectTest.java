package data_access;

import entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JsonUserDataAccessObject.
 * Tests user persistence, visit tracking, note management, and JSON file operations.
 */
class JsonUserDataAccessObjectTest {

    @TempDir
    Path tempDir;

    private JsonUserDataAccessObject userDAO;
    private File testJsonFile;
    private UserFactory userFactory;
    private MockLandmarkDAO mockLandmarkDAO;
    private Landmark testLandmark1;
    private Landmark testLandmark2;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary JSON file for testing
        testJsonFile = tempDir.resolve("test_users.json").toFile();
        userFactory = new UserFactory();

        // Setup mock landmark DAO with test landmarks
        mockLandmarkDAO = new MockLandmarkDAO();
        LandmarkInfo info1 = new LandmarkInfo("Address 1", "Description 1", "Hours 1", "Library");
        testLandmark1 = new Landmark("id1", "Robarts Library", new Location(43.6645, -79.3996), info1, 0);

        LandmarkInfo info2 = new LandmarkInfo("Address 2", "Description 2", "Hours 2", "Engineering");
        testLandmark2 = new Landmark("id2", "Bahen Centre", new Location(43.6596, -79.3975), info2, 0);

        mockLandmarkDAO.addLandmark(testLandmark1);
        mockLandmarkDAO.addLandmark(testLandmark2);

        // Create DAO with temporary file
        userDAO = new JsonUserDataAccessObject(testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);
    }

    @AfterEach
    void tearDown() {
        // Clean up test file if it exists
        if (testJsonFile.exists()) {
            testJsonFile.delete();
        }
    }

    // ============ BASIC USER OPERATIONS ============

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
    void testExistsByName() {
        // Arrange
        User user = userFactory.create("existinguser", "password");
        userDAO.save(user);

        // Act & Assert
        assertTrue(userDAO.existsByName("existinguser"), "User should exist");
        assertFalse(userDAO.existsByName("nonexistent"), "Non-existent user should not exist");
    }

    @Test
    void testGetNonExistentUser() {
        // Act
        User user = userDAO.get("nonexistent");

        // Assert
        assertNull(user, "Non-existent user should return null");
    }

    @Test
    void testCurrentUsername() {
        // Act
        userDAO.setCurrentUsername("currentuser");

        // Assert
        assertEquals("currentuser", userDAO.getCurrentUsername(), "Current username should match");
    }

    // ============ JSON FILE PERSISTENCE ============

    @Test
    void testEmptyFileInitialization() {
        // Assert - file should be created and initialized
        assertTrue(testJsonFile.exists(), "JSON file should be created");
        assertTrue(testJsonFile.length() > 0, "JSON file should not be empty");
    }

    @Test
    void testDataPersistsAcrossInstances() {
        // Arrange
        User user = userFactory.create("persistuser", "password");
        userDAO.save(user);

        // Act - create new DAO instance with same file
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);
        User retrieved = newDAO.get("persistuser");

        // Assert
        assertNotNull(retrieved, "User should be loaded from file");
        assertEquals("persistuser", retrieved.getUsername(), "Username should match");
        assertEquals("password", retrieved.getPassword(), "Password should match");
    }

    @Test
    void testMultipleUsersPersistence() {
        // Arrange
        User user1 = userFactory.create("user1", "pass1");
        User user2 = userFactory.create("user2", "pass2");
        User user3 = userFactory.create("user3", "pass3");

        // Act
        userDAO.save(user1);
        userDAO.save(user2);
        userDAO.save(user3);

        // Create new instance to verify persistence
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);

        // Assert
        assertTrue(newDAO.existsByName("user1"), "User1 should exist");
        assertTrue(newDAO.existsByName("user2"), "User2 should exist");
        assertTrue(newDAO.existsByName("user3"), "User3 should exist");
    }

    // ============ VISIT PERSISTENCE ============

    @Test
    void testSaveUserWithVisits() {
        // Arrange
        User user = userFactory.create("visitor", "password");
        Visit visit1 = new Visit("v1", testLandmark1, Instant.now());
        Visit visit2 = new Visit("v2", testLandmark2, Instant.now());
        user.getVisits().add(visit1);
        user.getVisits().add(visit2);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("visitor");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(2, retrieved.getVisits().size(), "Should have 2 visits");

        List<String> visitIds = retrieved.getVisits().stream()
            .map(Visit::getVisitId)
            .toList();
        assertTrue(visitIds.contains("v1"), "Should contain visit v1");
        assertTrue(visitIds.contains("v2"), "Should contain visit v2");
    }

    @Test
    void testVisitsPersistWithLandmarkData() {
        // Arrange
        User user = userFactory.create("landmarkvisitor", "password");
        Visit visit = new Visit("v1", testLandmark1, Instant.now());
        user.getVisits().add(visit);

        // Act
        userDAO.save(user);
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);
        User retrieved = newDAO.get("landmarkvisitor");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(1, retrieved.getVisits().size(), "Should have 1 visit");
        Visit retrievedVisit = retrieved.getVisits().get(0);
        assertEquals("Robarts Library", retrievedVisit.getLandmark().getLandmarkName(),
            "Landmark name should be preserved");
    }

    @Test
    void testMultipleVisitsToSameLandmark() {
        // Arrange
        User user = userFactory.create("multiplevisitor", "password");
        Visit visit1 = new Visit("v1", testLandmark1, Instant.now().minusSeconds(3600));
        Visit visit2 = new Visit("v2", testLandmark1, Instant.now());
        user.getVisits().add(visit1);
        user.getVisits().add(visit2);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("multiplevisitor");

        // Assert
        assertEquals(2, retrieved.getVisits().size(), "Should have 2 visits");
        assertTrue(retrieved.getVisits().stream()
            .allMatch(v -> v.getLandmark().getLandmarkName().equals("Robarts Library")),
            "All visits should be to Robarts Library");
    }

    // ============ NOTE MANAGEMENT ============

    @Test
    void testSaveUserWithNotes() {
        // Arrange
        User user = userFactory.create("noteuser", "password");
        Note note1 = new Note("n1", testLandmark1, "Great library!", Instant.now(), Instant.now());
        Note note2 = new Note("n2", testLandmark2, "Cool building", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note1);
        user.getPrivateNotes().add(note2);

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("noteuser");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertEquals(2, retrieved.getPrivateNotes().size(), "Should have 2 notes");
    }

    @Test
    void testGetNoteById() {
        // Arrange
        User user = userFactory.create("noteuser", "password");
        Note note = new Note("note123", testLandmark1, "Test note", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);
        userDAO.save(user);

        // Act
        Note retrieved = userDAO.getNoteById("note123");

        // Assert
        assertNotNull(retrieved, "Note should be found");
        assertEquals("note123", retrieved.getNoteId(), "Note ID should match");
        assertEquals("Test note", retrieved.getContent(), "Note content should match");
    }

    @Test
    void testGetNonExistentNote() {
        // Act
        Note note = userDAO.getNoteById("nonexistent");

        // Assert
        assertNull(note, "Non-existent note should return null");
    }

    @Test
    void testNoteExists() {
        // Arrange
        User user = userFactory.create("noteuser", "password");
        Note note = new Note("existingnote", testLandmark1, "Content", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);
        userDAO.save(user);

        // Act & Assert
        assertTrue(userDAO.noteExists("existingnote"), "Note should exist");
        assertFalse(userDAO.noteExists("nonexistent"), "Non-existent note should not exist");
    }

    @Test
    void testUpdateNote() {
        // Arrange
        User user = userFactory.create("updateuser", "password");
        Note originalNote = new Note("updatenote", testLandmark1, "Original", Instant.now(), Instant.now());
        user.getPrivateNotes().add(originalNote);
        userDAO.save(user);

        // Act - create updated note with same ID
        Note updatedNote = new Note("updatenote", testLandmark1, "Updated content",
            originalNote.getCreatedAt(), Instant.now());
        userDAO.updateNote(updatedNote);

        // Assert
        Note retrieved = userDAO.getNoteById("updatenote");
        assertNotNull(retrieved, "Updated note should exist");
        assertEquals("Updated content", retrieved.getContent(), "Content should be updated");
    }

    @Test
    void testUpdateNotePersists() {
        // Arrange
        User user = userFactory.create("persistupdateuser", "password");
        Note note = new Note("persistnote", testLandmark1, "Original", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);
        userDAO.save(user);

        // Act - update note
        Note updatedNote = new Note("persistnote", testLandmark1, "Updated",
            note.getCreatedAt(), Instant.now());
        userDAO.updateNote(updatedNote);

        // Create new DAO instance to verify persistence
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);
        Note retrieved = newDAO.getNoteById("persistnote");

        // Assert
        assertEquals("Updated", retrieved.getContent(), "Updated content should persist");
    }

    @Test
    void testDeleteNote() {
        // Arrange
        User user = userFactory.create("deleteuser", "password");
        Note note = new Note("deleteme", testLandmark1, "To be deleted", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);
        userDAO.save(user);

        // Act
        userDAO.deleteNote("deleteme");

        // Assert
        assertFalse(userDAO.noteExists("deleteme"), "Note should no longer exist");
        assertNull(userDAO.getNoteById("deleteme"), "Deleted note should not be retrievable");
    }

    @Test
    void testDeleteNotePersists() {
        // Arrange
        User user = userFactory.create("persistdeleteuser", "password");
        Note note = new Note("persistdeletenote", testLandmark1, "Content", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note);
        userDAO.save(user);

        // Act
        userDAO.deleteNote("persistdeletenote");

        // Create new DAO instance to verify persistence
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);

        // Assert
        assertFalse(newDAO.noteExists("persistdeletenote"), "Deletion should persist");
    }

    @Test
    void testGetNotesForUser() {
        // Arrange
        User user = userFactory.create("userwithnotes", "password");
        Note note1 = new Note("n1", testLandmark1, "Note 1", Instant.now(), Instant.now());
        Note note2 = new Note("n2", testLandmark2, "Note 2", Instant.now(), Instant.now());
        user.getPrivateNotes().add(note1);
        user.getPrivateNotes().add(note2);
        userDAO.save(user);

        // Act
        List<Note> notes = userDAO.getNotesForUser("userwithnotes");

        // Assert
        assertEquals(2, notes.size(), "Should return 2 notes for user");
    }

    @Test
    void testGetNotesForNonExistentUser() {
        // Act
        List<Note> notes = userDAO.getNotesForUser("nonexistent");

        // Assert
        assertTrue(notes.isEmpty(), "Should return empty list for non-existent user");
    }

    @Test
    void testGetNotesForLandmark() {
        // Arrange
        User user1 = userFactory.create("user1", "password");
        User user2 = userFactory.create("user2", "password");

        Note note1 = new Note("n1", testLandmark1, "User1's note", Instant.now(), Instant.now());
        Note note2 = new Note("n2", testLandmark1, "User2's note", Instant.now(), Instant.now());
        Note note3 = new Note("n3", testLandmark2, "Different landmark", Instant.now(), Instant.now());

        user1.getPrivateNotes().add(note1);
        user2.getPrivateNotes().add(note2);
        user1.getPrivateNotes().add(note3);

        userDAO.save(user1);
        userDAO.save(user2);

        // Act
        List<Note> robartsNotes = userDAO.getNotesForLandmark("Robarts Library");

        // Assert
        assertEquals(2, robartsNotes.size(), "Should find 2 notes for Robarts Library");
    }

    // ============ EDGE CASES ============

    @Test
    void testUserWithNoVisitsOrNotes() {
        // Arrange
        User user = userFactory.create("emptyuser", "password");

        // Act
        userDAO.save(user);
        User retrieved = userDAO.get("emptyuser");

        // Assert
        assertNotNull(retrieved, "User should be retrieved");
        assertTrue(retrieved.getVisits().isEmpty(), "Visits should be empty");
        assertTrue(retrieved.getPrivateNotes().isEmpty(), "Notes should be empty");
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

    @Test
    void testTimestampPreservation() {
        // Arrange
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        User user = userFactory.create("timestampuser", "password", createdAt,
            new ArrayList<>(), new ArrayList<>());

        // Act
        userDAO.save(user);
        JsonUserDataAccessObject newDAO = new JsonUserDataAccessObject(
            testJsonFile.getAbsolutePath(), userFactory, mockLandmarkDAO);
        User retrieved = newDAO.get("timestampuser");

        // Assert
        assertEquals(createdAt, retrieved.getCreatedAt(), "Creation timestamp should be preserved");
    }

    @Test
    void testDeleteNonExistentNote() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> userDAO.deleteNote("nonexistent"),
            "Deleting non-existent note should not throw exception");
    }

    @Test
    void testUpdateNonExistentNote() {
        // Arrange
        Note note = new Note("nonexistent", testLandmark1, "Content", Instant.now(), Instant.now());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> userDAO.updateNote(note),
            "Updating non-existent note should not throw exception");
    }

    // ============ MOCK LANDMARK DAO ============

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
            return landmarks.stream()
                .anyMatch(l -> l.getLandmarkName().equals(landmarkName));
        }

        @Override
        public Landmark findByName(String name) {
            return landmarks.stream()
                .filter(l -> l.getLandmarkName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Landmark not found: " + name));
        }
    }
}
