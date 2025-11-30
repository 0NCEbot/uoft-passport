package use_case.login;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LoginInteractor.
 * Tests the login use case with various scenarios including:
 * - Successful login
 * - Empty username
 * - Non-existent account
 * - Incorrect password
 */
class LoginInteractorTest {

    private MockLoginUserDataAccess mockUserDataAccess;
    private MockLoginPresenter mockPresenter;
    private LoginInteractor loginInteractor;

    @BeforeEach
    void setUp() {
        mockUserDataAccess = new MockLoginUserDataAccess();
        mockPresenter = new MockLoginPresenter();
        loginInteractor = new LoginInteractor(mockUserDataAccess, mockPresenter);

        // Setup a test user
        User testUser = new User("testuser", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDataAccess.addUser(testUser);
    }

    @Test
    void testSuccessfulLogin() {
        // Arrange
        LoginInputData inputData = new LoginInputData("testuser", "password123");

        // Act
        loginInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailCalled(), "Fail view should not be called");
        assertEquals("testuser", mockPresenter.getSuccessUsername(), "Username should match");
        assertEquals("testuser", mockUserDataAccess.getCurrentUsername(), "Current username should be set");
    }

    @Test
    void testLoginWithEmptyUsername() {
        // Arrange
        LoginInputData inputData = new LoginInputData("", "password123");

        // Act
        loginInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Username is empty", mockPresenter.getFailMessage(), "Error message should match");
    }

    @Test
    void testLoginWithNonExistentAccount() {
        // Arrange
        LoginInputData inputData = new LoginInputData("nonexistent", "password123");

        // Act
        loginInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("nonexistent: Account does not exist.", mockPresenter.getFailMessage(),
                "Error message should match");
    }

    @Test
    void testLoginWithIncorrectPassword() {
        // Arrange
        LoginInputData inputData = new LoginInputData("testuser", "wrongpassword");

        // Act
        loginInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Incorrect password for \"testuser\".", mockPresenter.getFailMessage(),
                "Error message should match");
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of LoginUserDataAccessInterface for testing
     */
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

    /**
     * Mock implementation of LoginOutputBoundary for testing
     */
    private static class MockLoginPresenter implements LoginOutputBoundary {
        private boolean successCalled = false;
        private boolean failCalled = false;
        private String successUsername;
        private String failMessage;

        @Override
        public void prepareSuccessView(LoginOutputData outputData) {
            successCalled = true;
            successUsername = outputData.getUsername();
        }

        @Override
        public void prepareFailView(String errorMessage) {
            failCalled = true;
            failMessage = errorMessage;
        }

        boolean isSuccessCalled() {
            return successCalled;
        }

        boolean isFailCalled() {
            return failCalled;
        }

        String getSuccessUsername() {
            return successUsername;
        }

        String getFailMessage() {
            return failMessage;
        }
    }
}
