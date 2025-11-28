package use_case.signup;

import entity.User;
import entity.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SignupInteractor.
 * Tests the signup use case with various scenarios including:
 * - Successful signup
 * - Empty username
 * - Username already exists
 * - Password too short
 * - Passwords don't match
 */
class SignupInteractorTest {

    private MockSignupUserDataAccess mockUserDataAccess;
    private MockSignupPresenter mockPresenter;
    private UserFactory userFactory;
    private SignupInteractor signupInteractor;

    @BeforeEach
    void setUp() {
        mockUserDataAccess = new MockSignupUserDataAccess();
        mockPresenter = new MockSignupPresenter();
        userFactory = new UserFactory();
        signupInteractor = new SignupInteractor(mockUserDataAccess, mockPresenter, userFactory);

        // Setup an existing user for testing
        User existingUser = new User("existing", "password123", Instant.now(), new ArrayList<>(), new ArrayList<>());
        mockUserDataAccess.addUser(existingUser);
    }

    @Test
    void testSuccessfulSignup() {
        // Arrange
        SignupInputData inputData = new SignupInputData("newuser", "password123", "password123");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success view should be called");
        assertFalse(mockPresenter.isFailCalled(), "Fail view should not be called");
        assertEquals("newuser", mockPresenter.getSuccessUsername(), "Username should match");
        assertTrue(mockUserDataAccess.existsByName("newuser"), "User should be saved");
    }

    @Test
    void testSignupWithEmptyUsername() {
        // Arrange
        SignupInputData inputData = new SignupInputData("", "password123", "password123");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Username cannot be empty.", mockPresenter.getFailMessage(), "Error message should match");
    }

    @Test
    void testSignupWithExistingUsername() {
        // Arrange
        SignupInputData inputData = new SignupInputData("existing", "password123", "password123");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Account \"existing\" already exists.", mockPresenter.getFailMessage(),
                "Error message should match");
    }

    @Test
    void testSignupWithShortPassword() {
        // Arrange
        SignupInputData inputData = new SignupInputData("newuser", "short", "short");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Password must be at least 8 characters.", mockPresenter.getFailMessage(),
                "Error message should match");
    }

    @Test
    void testSignupWithMismatchedPasswords() {
        // Arrange
        SignupInputData inputData = new SignupInputData("newuser", "password123", "password456");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isFailCalled(), "Fail view should be called");
        assertFalse(mockPresenter.isSuccessCalled(), "Success view should not be called");
        assertEquals("Passwords don't match.", mockPresenter.getFailMessage(), "Error message should match");
    }

    @Test
    void testSignupWithExactly8CharacterPassword() {
        // Arrange - test boundary condition
        SignupInputData inputData = new SignupInputData("newuser", "pass1234", "pass1234");

        // Act
        signupInteractor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isSuccessCalled(), "Success view should be called for 8-char password");
        assertFalse(mockPresenter.isFailCalled(), "Fail view should not be called");
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of SignupUserDataAccessInterface for testing
     */
    private static class MockSignupUserDataAccess implements SignupUserDataAccessInterface {
        private final java.util.Map<String, User> users = new java.util.HashMap<>();

        void addUser(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public boolean existsByName(String username) {
            return users.containsKey(username);
        }

        @Override
        public void save(User user) {
            users.put(user.getUsername(), user);
        }
    }

    /**
     * Mock implementation of SignupOutputBoundary for testing
     */
    private static class MockSignupPresenter implements SignupOutputBoundary {
        private boolean successCalled = false;
        private boolean failCalled = false;
        private String successUsername;
        private String failMessage;

        @Override
        public void prepareSuccessView(SignupOutputData outputData) {
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
