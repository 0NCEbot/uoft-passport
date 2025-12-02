package use_case.logout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LogoutInteractor.
 * Tests the logout use case including:
 * - Logging out successfully
 * - Clearing current username
 * - Handling logout with valid username
 * - Handling logout with null username
 * - Handling logout with empty username
 */
class LogoutInteractorTest {

    private MockLogoutUserDAO mockUserDAO;
    private MockLogoutPresenter mockPresenter;
    private LogoutInteractor interactor;

    @BeforeEach
    void setUp() {
        mockUserDAO = new MockLogoutUserDAO();
        mockPresenter = new MockLogoutPresenter();
        interactor = new LogoutInteractor(mockUserDAO, mockPresenter);
    }

    @Test
    void testLogout_Success() {
        // Arrange
        String username = "testuser";
        mockUserDAO.setCurrentUsername(username);
        LogoutInputData inputData = new LogoutInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called");
        assertNull(mockUserDAO.getCurrentUsername(),
                "Current username should be cleared (null)");

        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(username, outputData.getUsername(),
                "Output data should contain the logged out username");
        assertTrue(outputData.isSuccess(),
                "Output data should indicate success");
    }

    @Test
    void testLogout_WithNullUsername() {
        // Arrange
        String nullUsername = null;
        mockUserDAO.setCurrentUsername("someuser");
        LogoutInputData inputData = new LogoutInputData(nullUsername);

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called even with null username");
        assertNull(mockUserDAO.getCurrentUsername(),
                "Current username should be cleared");

        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertNull(outputData.getUsername(),
                "Output data should contain null username");
        assertTrue(outputData.isSuccess(), "Logout should still succeed");
    }

    @Test
    void testLogout_WithEmptyUsername() {
        // Arrange
        String emptyUsername = "";
        mockUserDAO.setCurrentUsername("someuser");
        LogoutInputData inputData = new LogoutInputData(emptyUsername);

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called");
        assertNull(mockUserDAO.getCurrentUsername(),
                "Current username should be cleared");

        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(emptyUsername, outputData.getUsername(),
                "Output data should contain empty username");
        assertTrue(outputData.isSuccess(), "Logout should succeed");
    }

    @Test
    void testLogout_WhenNoUserLoggedIn() {
        // Arrange
        String username = "testuser";
        mockUserDAO.setCurrentUsername(null); // No one logged in
        LogoutInputData inputData = new LogoutInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called");
        assertNull(mockUserDAO.getCurrentUsername(),
                "Current username should remain null");

        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(username, outputData.getUsername(),
                "Output data should contain the username from input");
        assertTrue(outputData.isSuccess(),
                "Logout should succeed even when no one was logged in");
    }

    @Test
    void testLogout_ClearsSession() {
        // Arrange
        String username = "activeuser";
        mockUserDAO.setCurrentUsername(username);

        // Verify user is logged in before logout
        assertEquals(username, mockUserDAO.getCurrentUsername(),
                "User should be logged in initially");

        LogoutInputData inputData = new LogoutInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        assertNull(mockUserDAO.getCurrentUsername(),
                "Session should be cleared after logout");
    }

    @Test
    void testLogout_MultipleLogouts() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";

        mockUserDAO.setCurrentUsername(username1);
        LogoutInputData inputData1 = new LogoutInputData(username1);

        // Act - First logout
        interactor.execute(inputData1);

        // Assert - First logout
        assertNull(mockUserDAO.getCurrentUsername(),
                "Session should be cleared after first logout");
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called for first logout");

        // Reset presenter for second logout
        mockPresenter.reset();

        // Set new user and logout again
        mockUserDAO.setCurrentUsername(username2);
        LogoutInputData inputData2 = new LogoutInputData(username2);

        // Act - Second logout
        interactor.execute(inputData2);

        // Assert - Second logout
        assertNull(mockUserDAO.getCurrentUsername(),
                "Session should be cleared after second logout");
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called for second logout");

        LogoutOutputData outputData2 = mockPresenter.getOutputData();
        assertEquals(username2, outputData2.getUsername(),
                "Second logout should have correct username");
    }

    @Test
    void testLogout_WithSpecialCharactersInUsername() {
        // Arrange
        String specialUsername = "user@123!#$";
        mockUserDAO.setCurrentUsername(specialUsername);
        LogoutInputData inputData = new LogoutInputData(specialUsername);

        // Act
        interactor.execute(inputData);

        // Assert
        assertTrue(mockPresenter.isPrepareSuccessViewCalled(),
                "prepareSuccessView should be called");
        assertNull(mockUserDAO.getCurrentUsername(),
                "Current username should be cleared");

        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertEquals(specialUsername, outputData.getUsername(),
                "Output data should contain special character username");
    }

    @Test
    void testLogout_PresenterReceivesCorrectData() {
        // Arrange
        String username = "testuser";
        mockUserDAO.setCurrentUsername(username);
        LogoutInputData inputData = new LogoutInputData(username);

        // Act
        interactor.execute(inputData);

        // Assert
        LogoutOutputData outputData = mockPresenter.getOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals(username, outputData.getUsername(),
                "Username in output should match input");
        assertTrue(outputData.isSuccess(),
                "Success flag should be true");
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of LogoutUserDataAccessInterface for testing.
     */
    private static class MockLogoutUserDAO implements LogoutUserDataAccessInterface {
        private String currentUsername;

        @Override
        public void setCurrentUsername(String username) {
            this.currentUsername = username;
        }

        @Override
        public String getCurrentUsername() {
            return currentUsername;
        }
    }

    /**
     * Mock implementation of LogoutOutputBoundary for testing.
     */
    private static class MockLogoutPresenter implements LogoutOutputBoundary {
        private boolean prepareSuccessViewCalled = false;
        private LogoutOutputData outputData;

        @Override
        public void prepareSuccessView(LogoutOutputData outputData) {
            this.prepareSuccessViewCalled = true;
            this.outputData = outputData;
        }

        boolean isPrepareSuccessViewCalled() {
            return prepareSuccessViewCalled;
        }

        LogoutOutputData getOutputData() {
            return outputData;
        }

        void reset() {
            prepareSuccessViewCalled = false;
            outputData = null;
        }
    }
}