package use_case.signup;

import entity.User;
import entity.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests:
 * - testSuccessfulSignup
 * - testEmptyUsername
 * - testUsernameAlreadyExists
 * - testPasswordTooShort
 * - testPasswordExactly8Characters
 * - testPasswordExactly7Characters
 * - testPasswordsDoNotMatch
 * - testMultipleValidations
 * - testPasswordWithSpecialCharacters
 * - testLongUsername
 */
class SignupInteractorTest {

    private TestDataAccess dataAccess;
    private TestPresenter presenter;
    private UserFactory userFactory;
    private SignupInteractor interactor;

    @BeforeEach
    void setUp() {
        dataAccess = new TestDataAccess();
        presenter = new TestPresenter();
        userFactory = new UserFactory();
        interactor = new SignupInteractor(dataAccess, presenter, userFactory);
    }

    /**
     * Tests successful signup with valid username and matching passwords.
     * Verifies that user is saved and success view is called.
     */
    @Test
    void testSuccessfulSignup() {
        interactor.execute(new SignupInputData("newuser", "password123", "password123"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("newuser", presenter.outputData.getUsername());
        assertTrue(dataAccess.existsByName("newuser"));
    }

    /**
     * Tests signup with empty username.
     * Verifies that fail view is called with "Username cannot be empty." error.
     */
    @Test
    void testEmptyUsername() {
        interactor.execute(new SignupInputData("", "password123", "password123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Username cannot be empty.", presenter.errorMessage);
        assertEquals(0, dataAccess.getSaveCount());
    }

    /**
     * Tests signup with username that already exists.
     * Verifies that fail view is called with "Account already exists." error.
     */
    @Test
    void testUsernameAlreadyExists() {
        // Create existing user
        User existingUser = userFactory.create("existinguser", "password123");
        dataAccess.save(existingUser);

        // Try to signup with same username
        interactor.execute(new SignupInputData("existinguser", "newpass123", "newpass123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Account \"existinguser\" already exists.", presenter.errorMessage);
        assertEquals(1, dataAccess.getSaveCount()); // Only the first user should be saved
    }

    /**
     * Tests signup with password shorter than 8 characters.
     * Verifies that fail view is called with "Password must be at least 8 characters." error.
     */
    @Test
    void testPasswordTooShort() {
        interactor.execute(new SignupInputData("newuser", "pass", "pass"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Password must be at least 8 characters.", presenter.errorMessage);
        assertEquals(0, dataAccess.getSaveCount());
    }

    /**
     * Tests signup with password exactly 8 characters (boundary test).
     * Verifies that this is accepted and success view is called.
     */
    @Test
    void testPasswordExactly8Characters() {
        interactor.execute(new SignupInputData("newuser", "pass1234", "pass1234"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("newuser", presenter.outputData.getUsername());
        assertTrue(dataAccess.existsByName("newuser"));
    }

    /**
     * Tests signup with password exactly 7 characters (boundary test).
     * Verifies that fail view is called with password too short error.
     */
    @Test
    void testPasswordExactly7Characters() {
        interactor.execute(new SignupInputData("newuser", "pass123", "pass123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Password must be at least 8 characters.", presenter.errorMessage);
        assertEquals(0, dataAccess.getSaveCount());
    }

    /**
     * Tests signup with passwords that don't match.
     * Verifies that fail view is called with "Passwords don't match." error.
     */
    @Test
    void testPasswordsDoNotMatch() {
        interactor.execute(new SignupInputData("newuser", "password123", "password456"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Passwords don't match.", presenter.errorMessage);
        assertEquals(0, dataAccess.getSaveCount());
    }

    /**
     * Tests that validation checks happen in correct order.
     * Empty username should be caught before password validation.
     */
    @Test
    void testMultipleValidations() {
        // Empty username with short password - should fail on username first
        interactor.execute(new SignupInputData("", "short", "short"));

        assertTrue(presenter.failCalled);
        assertEquals("Username cannot be empty.", presenter.errorMessage);

        // Reset presenter
        presenter.reset();

        // Existing user with mismatched passwords - should fail on existing user first
        User existingUser = userFactory.create("existing", "password123");
        dataAccess.save(existingUser);

        interactor.execute(new SignupInputData("existing", "password123", "password456"));

        assertTrue(presenter.failCalled);
        assertEquals("Account \"existing\" already exists.", presenter.errorMessage);
    }

    /**
     * Tests signup with password containing special characters.
     * Verifies that special characters are accepted in passwords.
     */
    @Test
    void testPasswordWithSpecialCharacters() {
        interactor.execute(new SignupInputData("newuser", "p@ssw0rd!#$", "p@ssw0rd!#$"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("newuser", presenter.outputData.getUsername());
        assertTrue(dataAccess.existsByName("newuser"));
    }

    /**
     * Tests signup with very long username and password.
     * Verifies that long credentials are handled correctly.
     */
    @Test
    void testLongUsername() {
        String longUsername = "a".repeat(100);
        String longPassword = "b".repeat(100);

        interactor.execute(new SignupInputData(longUsername, longPassword, longPassword));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals(longUsername, presenter.outputData.getUsername());
        assertTrue(dataAccess.existsByName(longUsername));
    }

    // Mock Data Access
    private static class TestDataAccess implements SignupUserDataAccessInterface {
        private final Map<String, User> users = new HashMap<>();
        private int saveCount = 0;

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
        }

        @Override
        public void save(User user) {
            users.put(user.getUsername(), user);
            saveCount++;
        }

        public int getSaveCount() {
            return saveCount;
        }
    }

    // Mock Presenter
    private static class TestPresenter implements SignupOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        SignupOutputData outputData;
        String errorMessage;

        @Override
        public void prepareSuccessView(SignupOutputData outputData) {
            this.successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.failCalled = true;
            this.errorMessage = errorMessage;
        }

        public void reset() {
            successCalled = false;
            failCalled = false;
            outputData = null;
            errorMessage = null;
        }
    }
}