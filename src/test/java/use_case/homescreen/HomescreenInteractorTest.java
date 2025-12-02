package use_case.homescreen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests:
 * - testBrowseLandmarksAction
 * - testPlanARouteAction
 * - testMyProgressAction
 * - testUnknownAction
 * - testEmptyAction
 * - testNullAction
 * - testActionWithExtraSpaces
 * - testCaseSensitiveAction
 * - testSpecialCharactersAction
 * - testMultipleActions
 */
class HomescreenInteractorTest {

    private TestPresenter presenter;
    private HomescreenInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new TestPresenter();
        interactor = new HomescreenInteractor(presenter);
    }

    /**
     * Tests "browse landmarks" action.
     * Verifies that success view is called with correct target view.
     */
    @Test
    void testBrowseLandmarksAction() {
        interactor.execute(new HomescreenInputData("browse landmarks"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("browse landmarks", presenter.outputData.getViewToNavigateTo());
        assertTrue(presenter.outputData.isSuccess());
    }

    /**
     * Tests "plan a route" action.
     * Verifies that success view is called with correct target view.
     */
    @Test
    void testPlanARouteAction() {
        interactor.execute(new HomescreenInputData("plan a route"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("plan a route", presenter.outputData.getViewToNavigateTo());
        assertTrue(presenter.outputData.isSuccess());
    }

    /**
     * Tests "my progress" action.
     * Verifies that success view is called with correct target view.
     */
    @Test
    void testMyProgressAction() {
        interactor.execute(new HomescreenInputData("my progress"));

        assertTrue(presenter.successCalled);
        assertFalse(presenter.failCalled);
        assertEquals("my progress", presenter.outputData.getViewToNavigateTo());
        assertTrue(presenter.outputData.isSuccess());
    }

    /**
     * Tests unknown/invalid action.
     * Verifies that fail view is called with "Unknown action" error.
     */
    @Test
    void testUnknownAction() {
        interactor.execute(new HomescreenInputData("invalid action"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action: invalid action", presenter.errorMessage);
    }

    /**
     * Tests empty string action.
     * Verifies that fail view is called with "Unknown action" error.
     */
    @Test
    void testEmptyAction() {
        interactor.execute(new HomescreenInputData(""));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action: ", presenter.errorMessage);
    }

    /**
     * Tests null action.
     * Verifies that fail view is called with "Unknown action: null" error.
     */
    @Test
    void testNullAction() {
        interactor.execute(new HomescreenInputData(null));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action: null", presenter.errorMessage);
    }

    /**
     * Tests action with extra spaces around it.
     * Verifies that spaces are not trimmed and action fails.
     */
    @Test
    void testActionWithExtraSpaces() {
        interactor.execute(new HomescreenInputData("  browse landmarks  "));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action:   browse landmarks  ", presenter.errorMessage);
    }

    /**
     * Tests that actions are case-sensitive.
     * Verifies that "Browse Landmarks" (capitalized) fails.
     */
    @Test
    void testCaseSensitiveAction() {
        interactor.execute(new HomescreenInputData("Browse Landmarks"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action: Browse Landmarks", presenter.errorMessage);
    }

    /**
     * Tests action with special characters.
     * Verifies that invalid characters cause action to fail.
     */
    @Test
    void testSpecialCharactersAction() {
        interactor.execute(new HomescreenInputData("browse@landmarks!"));

        assertTrue(presenter.failCalled);
        assertFalse(presenter.successCalled);
        assertEquals("Unknown action: browse@landmarks!", presenter.errorMessage);
    }

    /**
     * Tests multiple actions executed in sequence.
     * Verifies that interactor can handle multiple consecutive calls.
     */
    @Test
    void testMultipleActions() {
        // First action
        interactor.execute(new HomescreenInputData("browse landmarks"));
        assertTrue(presenter.successCalled);
        assertEquals("browse landmarks", presenter.outputData.getViewToNavigateTo());

        // Reset presenter
        presenter.reset();

        // Second action
        interactor.execute(new HomescreenInputData("my progress"));
        assertTrue(presenter.successCalled);
        assertEquals("my progress", presenter.outputData.getViewToNavigateTo());

        // Reset presenter
        presenter.reset();

        // Third action - invalid
        interactor.execute(new HomescreenInputData("unknown"));
        assertTrue(presenter.failCalled);
        assertEquals("Unknown action: unknown", presenter.errorMessage);
    }

    // Mock Presenter
    private static class TestPresenter implements HomescreenOutputBoundary {
        boolean successCalled = false;
        boolean failCalled = false;
        HomescreenOutputData outputData;
        String errorMessage;

        @Override
        public void prepareSuccessView(HomescreenOutputData outputData) {
            this.successCalled = true;
            this.outputData = outputData;
        }

        @Override
        public void prepareFailView(String error) {
            this.failCalled = true;
            this.errorMessage = error;
        }

        public void reset() {
            successCalled = false;
            failCalled = false;
            outputData = null;
            errorMessage = null;
        }
    }
}