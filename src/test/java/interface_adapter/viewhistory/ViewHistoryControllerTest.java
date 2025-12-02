package interface_adapter.viewhistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.undovisit.UndoVisitInputBoundary;
import use_case.undovisit.UndoVisitInputData;
import use_case.viewhistory.ViewHistoryInputBoundary;
import use_case.viewhistory.ViewHistoryInputData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ViewHistoryController.
 * Tests the controller's ability to:
 * - Delegate to the view history interactor
 * - Delegate to the undo visit interactor
 * - Pass correct input data to interactors
 */
class ViewHistoryControllerTest {

    private MockViewHistoryInteractor mockViewHistoryInteractor;
    private MockUndoVisitInteractor mockUndoVisitInteractor;
    private ViewHistoryController controller;

    @BeforeEach
    void setUp() {
        mockViewHistoryInteractor = new MockViewHistoryInteractor();
        mockUndoVisitInteractor = new MockUndoVisitInteractor();
        controller = new ViewHistoryController(mockViewHistoryInteractor, mockUndoVisitInteractor);
    }

    @Test
    void testExecuteCallsViewHistoryInteractor() {
        // Arrange
        String username = "testuser";

        // Act
        controller.execute(username);

        // Assert
        assertTrue(mockViewHistoryInteractor.isExecuteCalled(),
                "View history interactor execute should be called");
        assertEquals(username, mockViewHistoryInteractor.getReceivedUsername(),
                "Username should be passed to interactor");
    }

    @Test
    void testExecuteWithDifferentUsername() {
        // Arrange
        String username = "johndoe";

        // Act
        controller.execute(username);

        // Assert
        assertTrue(mockViewHistoryInteractor.isExecuteCalled());
        assertEquals("johndoe", mockViewHistoryInteractor.getReceivedUsername());
    }

    @Test
    void testUndoVisitCallsUndoVisitInteractor() {
        // Arrange
        String username = "testuser";
        String visitId = "visit123";

        // Act
        controller.undoVisit(username, visitId);

        // Assert
        assertTrue(mockUndoVisitInteractor.isExecuteCalled(),
                "Undo visit interactor execute should be called");
        assertEquals(username, mockUndoVisitInteractor.getReceivedUsername(),
                "Username should be passed to interactor");
        assertEquals(visitId, mockUndoVisitInteractor.getReceivedVisitId(),
                "Visit ID should be passed to interactor");
    }

    @Test
    void testUndoVisitWithDifferentParameters() {
        // Arrange
        String username = "janedoe";
        String visitId = "v456";

        // Act
        controller.undoVisit(username, visitId);

        // Assert
        assertTrue(mockUndoVisitInteractor.isExecuteCalled());
        assertEquals("janedoe", mockUndoVisitInteractor.getReceivedUsername());
        assertEquals("v456", mockUndoVisitInteractor.getReceivedVisitId());
    }

    @Test
    void testExecuteDoesNotCallUndoVisitInteractor() {
        // Arrange
        String username = "testuser";

        // Act
        controller.execute(username);

        // Assert
        assertTrue(mockViewHistoryInteractor.isExecuteCalled(),
                "View history interactor should be called");
        assertFalse(mockUndoVisitInteractor.isExecuteCalled(),
                "Undo visit interactor should not be called when executing view history");
    }

    @Test
    void testUndoVisitDoesNotCallViewHistoryInteractor() {
        // Arrange
        String username = "testuser";
        String visitId = "v123";

        // Act
        controller.undoVisit(username, visitId);

        // Assert
        assertTrue(mockUndoVisitInteractor.isExecuteCalled(),
                "Undo visit interactor should be called");
        assertFalse(mockViewHistoryInteractor.isExecuteCalled(),
                "View history interactor should not be called when undoing visit");
    }

    @Test
    void testMultipleExecuteCalls() {
        // Act: Call execute multiple times
        controller.execute("user1");
        controller.execute("user2");
        controller.execute("user3");

        // Assert: Last call should be for user3
        assertTrue(mockViewHistoryInteractor.isExecuteCalled());
        assertEquals("user3", mockViewHistoryInteractor.getReceivedUsername(),
                "Should have the username from the last call");
    }

    @Test
    void testMultipleUndoVisitCalls() {
        // Act: Call undoVisit multiple times
        controller.undoVisit("user1", "v1");
        controller.undoVisit("user2", "v2");

        // Assert: Last call should be for user2, v2
        assertTrue(mockUndoVisitInteractor.isExecuteCalled());
        assertEquals("user2", mockUndoVisitInteractor.getReceivedUsername());
        assertEquals("v2", mockUndoVisitInteractor.getReceivedVisitId());
    }

    @Test
    void testExecuteWithEmptyStringUsername() {
        // Arrange
        String emptyUsername = "";

        // Act
        controller.execute(emptyUsername);

        // Assert: Controller should still pass the empty string to the interactor
        // It's the interactor's responsibility to validate
        assertTrue(mockViewHistoryInteractor.isExecuteCalled());
        assertEquals("", mockViewHistoryInteractor.getReceivedUsername());
    }

    @Test
    void testUndoVisitWithEmptyStrings() {
        // Arrange
        String emptyUsername = "";
        String emptyVisitId = "";

        // Act
        controller.undoVisit(emptyUsername, emptyVisitId);

        // Assert: Controller should pass empty strings to interactor
        assertTrue(mockUndoVisitInteractor.isExecuteCalled());
        assertEquals("", mockUndoVisitInteractor.getReceivedUsername());
        assertEquals("", mockUndoVisitInteractor.getReceivedVisitId());
    }

    // =============== Mock Classes ===============

    /**
     * Mock implementation of ViewHistoryInputBoundary for testing.
     */
    private static class MockViewHistoryInteractor implements ViewHistoryInputBoundary {
        private boolean executeCalled = false;
        private String receivedUsername;

        @Override
        public void execute(ViewHistoryInputData inputData) {
            executeCalled = true;
            receivedUsername = inputData.getUsername();
        }

        boolean isExecuteCalled() {
            return executeCalled;
        }

        String getReceivedUsername() {
            return receivedUsername;
        }
    }

    /**
     * Mock implementation of UndoVisitInputBoundary for testing.
     */
    private static class MockUndoVisitInteractor implements UndoVisitInputBoundary {
        private boolean executeCalled = false;
        private String receivedUsername;
        private String receivedVisitId;

        @Override
        public void execute(UndoVisitInputData inputData) {
            executeCalled = true;
            receivedUsername = inputData.getUsername();
            receivedVisitId = inputData.getVisitId();
        }

        boolean isExecuteCalled() {
            return executeCalled;
        }

        String getReceivedUsername() {
            return receivedUsername;
        }

        String getReceivedVisitId() {
            return receivedVisitId;
        }
    }
}
