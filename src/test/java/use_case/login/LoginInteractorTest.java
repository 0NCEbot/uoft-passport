package use_case.login;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginInteractorTest {

    private TestDataAccess dataAccess;
    private TestPresenter presenter;
    private LoginInteractor interactor;

    @BeforeEach
    void setUp() {
        dataAccess = new TestDataAccess();
        presenter = new TestPresenter();
        interactor = new LoginInteractor(dataAccess, presenter);
    }

    /**
     * Tests successful login with valid username and password.
     * Verifies that success view is called and current username is set.
     */
    @Test
    void testSuccessfulLogin() {
        User user = new User("testuser", "password123", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData("testuser", "password123"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("testuser", presenter.outputData.getUsername());
        assertEquals("testuser", dataAccess.getCurrentUsername());
    }

    /**
     * Tests login attempt with username that doesn't exist.
     * Verifies that fail view is called with appropriate error message.
     */
    @Test
    void testUserDoesNotExist() {
        interactor.execute(new LoginInputData("nonexistent", "password123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("nonexistent: Account does not exist.", presenter.errorMessage);
    }

    /**
     * Tests login attempt with incorrect password.
     * Verifies that fail view is called with "Incorrect password" error.
     */
    @Test
    void testIncorrectPassword() {
        User user = new User("testuser", "correctpassword", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData("testuser", "wrongpassword"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Incorrect password for \"testuser\".", presenter.errorMessage);
    }

    /**
     * Tests login with empty username.
     * Verifies that fail view is called with "Username is empty" error.
     */
    @Test
    void testEmptyUsername() {
        interactor.execute(new LoginInputData("", "password123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Username is empty", presenter.errorMessage);
    }

    /**
     * Tests login with correct username but empty password.
     * Verifies that fail view is called with incorrect password error.
     */
    @Test
    void testEmptyPassword() {
        User user = new User("testuser", "password123", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData("testuser", ""));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Incorrect password for \"testuser\".", presenter.errorMessage);
    }

    /**
     * Tests multiple successful logins with different users.
     * Verifies that current username is updated correctly each time.
     */
    @Test
    void testMultipleLogins() {
        User user1 = new User("user1", "pass1", new ArrayList<>(), new ArrayList<>());
        User user2 = new User("user2", "pass2", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user1);
        dataAccess.addUser(user2);

        // First login
        interactor.execute(new LoginInputData("user1", "pass1"));
        assertTrue(presenter.successCalled);
        assertEquals("user1", dataAccess.getCurrentUsername());

        // Second login
        presenter.successCalled = false;
        interactor.execute(new LoginInputData("user2", "pass2"));
        assertTrue(presenter.successCalled);
        assertEquals("user2", dataAccess.getCurrentUsername());
    }

    /**
     * Tests login with username containing special characters.
     * Verifies that special characters in usernames are handled correctly.
     */
    @Test
    void testUsernameWithSpecialCharacters() {
        User user = new User("user@123", "password", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData("user@123", "password"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("user@123", presenter.outputData.getUsername());
    }

    /**
     * Tests login with password containing special characters.
     * Verifies that special characters in passwords are handled correctly.
     */
    @Test
    void testPasswordWithSpecialCharacters() {
        User user = new User("testuser", "p@ssw0rd!#$", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData("testuser", "p@ssw0rd!#$"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("testuser", presenter.outputData.getUsername());
    }

    /**
     * Tests login with case-sensitive username.
     * Verifies that "TestUser" is different from "testuser".
     */
    @Test
    void testCaseSensitiveUsername() {
        User user = new User("TestUser", "password123", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        // Try logging in with lowercase
        interactor.execute(new LoginInputData("testuser", "password123"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("testuser: Account does not exist.", presenter.errorMessage);
    }

    /**
     * Tests login with very long username.
     * Verifies that long usernames are handled correctly.
     */
    @Test
    void testLongUsername() {
        String longUsername = "a".repeat(100);
        User user = new User(longUsername, "password123", new ArrayList<>(), new ArrayList<>());
        dataAccess.addUser(user);

        interactor.execute(new LoginInputData(longUsername, "password123"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals(longUsername, presenter.outputData.getUsername());
    }

    // Mock Data Access
    private static class TestDataAccess implements LoginUserDataAccessInterface {
        private final Map<String, User> users = new HashMap<>();
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

    // Mock Presenter
    private static class TestPresenter implements LoginOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        LoginOutputData outputData;
        String errorMessage;

        @Override
        public void prepareSuccessView(LoginOutputData outputData) {
            this.successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            this.failCalled = true;
            this.errorMessage = errorMessage;
        }
    }
}