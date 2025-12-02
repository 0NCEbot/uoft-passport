package interface_adapter.viewhistory;

import interface_adapter.ViewManagerModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.undovisit.UndoVisitOutputData;
import use_case.viewhistory.ViewHistoryOutputData;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ViewHistoryPresenter.
 * Tests the presenter's ability to:
 * - Convert ViewHistoryOutputData to ViewHistoryState
 * - Convert UndoVisitOutputData to ViewHistoryState
 * - Handle failure scenarios
 * - Fire property change events
 * - Update view manager state
 */
class ViewHistoryPresenterTest {

    private ViewHistoryViewModel viewModel;
    private ViewManagerModel viewManagerModel;
    private ViewHistoryPresenter presenter;
    private MockPropertyChangeListener viewModelListener;
    private MockPropertyChangeListener viewManagerListener;

    @BeforeEach
    void setUp() {
        viewModel = new ViewHistoryViewModel();
        viewManagerModel = new ViewManagerModel();
        presenter = new ViewHistoryPresenter(viewModel, viewManagerModel);

        // Add property change listeners to track changes
        viewModelListener = new MockPropertyChangeListener();
        viewManagerListener = new MockPropertyChangeListener();
        viewModel.addPropertyChangeListener(viewModelListener);
        viewManagerModel.addPropertyChangeListener(viewManagerListener);
    }

    @Test
    void testPrepareSuccessViewWithViewHistoryOutputData() {
        // Arrange: Create output data with visits
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v1", "Robarts Library", "Today, 5:00PM"));
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v2", "Bahen Centre", "Yesterday, 3:30PM"));

        ViewHistoryOutputData outputData = new ViewHistoryOutputData("testuser", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert: Check view model state
        ViewHistoryState state = viewModel.getState();
        assertNotNull(state, "State should not be null");
        assertEquals("testuser", state.getUsername(), "Username should be set");
        assertNull(state.getErrorMessage(), "Error message should be null on success");
        assertNull(state.getSuccessMessage(), "Success message should be null for view history");

        // Check visits were converted correctly
        List<ViewHistoryState.VisitVM> visits = state.getVisits();
        assertEquals(2, visits.size(), "Should have 2 visits");

        assertEquals("v1", visits.get(0).visitId);
        assertEquals("Robarts Library", visits.get(0).landmarkName);
        assertEquals("Today, 5:00PM", visits.get(0).visitedAt);

        assertEquals("v2", visits.get(1).visitId);
        assertEquals("Bahen Centre", visits.get(1).landmarkName);
        assertEquals("Yesterday, 3:30PM", visits.get(1).visitedAt);

        // Check property change was fired
        assertTrue(viewModelListener.wasPropertyChangeFired(), "Property change should be fired on view model");

        // Check view manager state
        assertEquals("view history", viewManagerModel.getState(), "View manager should navigate to view history");
        assertTrue(viewManagerListener.wasPropertyChangeFired(), "Property change should be fired on view manager");
    }

    @Test
    void testPrepareSuccessViewWithEmptyVisitsList() {
        // Arrange: Create output data with no visits
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        ViewHistoryOutputData outputData = new ViewHistoryOutputData("emptyuser", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        ViewHistoryState state = viewModel.getState();
        assertEquals("emptyuser", state.getUsername());
        assertEquals(0, state.getVisits().size(), "Should have 0 visits");
        assertNull(state.getErrorMessage());
        assertNull(state.getSuccessMessage());

        assertTrue(viewModelListener.wasPropertyChangeFired());
        assertEquals("view history", viewManagerModel.getState());
    }

    @Test
    void testPrepareSuccessViewWithUndoVisitOutputData() {
        // Arrange: Create undo visit output data
        List<UndoVisitOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        visitDTOs.add(new UndoVisitOutputData.VisitDTO("v1", "Hart House", "December 01 2024, 14:30"));

        UndoVisitOutputData outputData = new UndoVisitOutputData(
                "testuser",
                visitDTOs,
                "Visit removed successfully"
        );

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert: Check view model state
        ViewHistoryState state = viewModel.getState();
        assertNotNull(state);
        assertEquals("testuser", state.getUsername());
        assertNull(state.getErrorMessage(), "Error message should be null on success");
        assertEquals("Visit removed successfully", state.getSuccessMessage(),
                "Success message should be set for undo visit");

        // Check visits were converted correctly
        List<ViewHistoryState.VisitVM> visits = state.getVisits();
        assertEquals(1, visits.size(), "Should have 1 remaining visit");

        assertEquals("v1", visits.get(0).visitId);
        assertEquals("Hart House", visits.get(0).landmarkName);
        assertEquals("December 01 2024, 14:30", visits.get(0).visitedAt);

        // Check property change was fired
        assertTrue(viewModelListener.wasPropertyChangeFired());

        // Check view manager state stays on view history
        assertEquals("view history", viewManagerModel.getState());
        assertTrue(viewManagerListener.wasPropertyChangeFired());
    }

    @Test
    void testPrepareSuccessViewWithUndoVisitEmptyRemainingVisits() {
        // Arrange: Undo the last visit, leaving empty list
        List<UndoVisitOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        UndoVisitOutputData outputData = new UndoVisitOutputData(
                "testuser",
                visitDTOs,
                "Visit removed successfully"
        );

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        ViewHistoryState state = viewModel.getState();
        assertEquals(0, state.getVisits().size(), "Should have 0 remaining visits");
        assertEquals("Visit removed successfully", state.getSuccessMessage());
        assertNull(state.getErrorMessage());
    }

    @Test
    void testPrepareFailView() {
        // Arrange: Set up initial state with some data
        ViewHistoryState initialState = new ViewHistoryState();
        initialState.setUsername("testuser");
        initialState.setSuccessMessage("Previous success");
        viewModel.setState(initialState);

        // Act
        presenter.prepareFailView("User not found: testuser");

        // Assert
        ViewHistoryState state = viewModel.getState();
        assertEquals("User not found: testuser", state.getErrorMessage(),
                "Error message should be set");
        assertNull(state.getSuccessMessage(), "Success message should be cleared on failure");

        // Username should remain from initial state
        assertEquals("testuser", state.getUsername());

        // Property change should be fired
        assertTrue(viewModelListener.wasPropertyChangeFired());

        // View manager state should NOT be changed on failure
        // (it stays at the initial "" value from setup)
        assertEquals("", viewManagerModel.getState(),
                "View manager should not navigate on failure");
    }

    @Test
    void testPrepareFailViewWithDifferentErrorMessage() {
        // Act
        presenter.prepareFailView("Visit not found with ID: v123");

        // Assert
        ViewHistoryState state = viewModel.getState();
        assertEquals("Visit not found with ID: v123", state.getErrorMessage());
        assertNull(state.getSuccessMessage());
        assertTrue(viewModelListener.wasPropertyChangeFired());
    }

    @Test
    void testMultipleSuccessViewCalls() {
        // Arrange: First call
        List<ViewHistoryOutputData.VisitDTO> firstVisits = new ArrayList<>();
        firstVisits.add(new ViewHistoryOutputData.VisitDTO("v1", "Landmark1", "Today, 1:00PM"));
        ViewHistoryOutputData firstOutput = new ViewHistoryOutputData("user1", firstVisits);

        // Act: First call
        presenter.prepareSuccessView(firstOutput);

        // Assert: First state
        ViewHistoryState firstState = viewModel.getState();
        assertEquals("user1", firstState.getUsername());
        assertEquals(1, firstState.getVisits().size());

        // Arrange: Second call
        List<ViewHistoryOutputData.VisitDTO> secondVisits = new ArrayList<>();
        secondVisits.add(new ViewHistoryOutputData.VisitDTO("v2", "Landmark2", "Yesterday, 2:00PM"));
        secondVisits.add(new ViewHistoryOutputData.VisitDTO("v3", "Landmark3", "Yesterday, 3:00PM"));
        ViewHistoryOutputData secondOutput = new ViewHistoryOutputData("user2", secondVisits);

        // Act: Second call
        presenter.prepareSuccessView(secondOutput);

        // Assert: State should be updated to second call's data
        ViewHistoryState secondState = viewModel.getState();
        assertEquals("user2", secondState.getUsername(), "Username should be updated");
        assertEquals(2, secondState.getVisits().size(), "Should have 2 visits from second call");
        assertEquals("v2", secondState.getVisits().get(0).visitId);
    }

    @Test
    void testPropertyChangeEventContainsCorrectState() {
        // Arrange
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v1", "Landmark", "Today, 1:00PM"));
        ViewHistoryOutputData outputData = new ViewHistoryOutputData("testuser", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        PropertyChangeEvent event = viewModelListener.getLastEvent();
        assertNotNull(event, "Property change event should be captured");
        assertEquals("state", event.getPropertyName(), "Property name should be 'state'");

        ViewHistoryState newState = (ViewHistoryState) event.getNewValue();
        assertNotNull(newState, "New state should not be null");
        assertEquals("testuser", newState.getUsername());
        assertEquals(1, newState.getVisits().size());
    }

    @Test
    void testViewManagerPropertyChangeEventContainsCorrectViewName() {
        // Arrange
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        ViewHistoryOutputData outputData = new ViewHistoryOutputData("testuser", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        PropertyChangeEvent event = viewManagerListener.getLastEvent();
        assertNotNull(event, "Property change event should be captured");
        assertEquals("state", event.getPropertyName());
        assertEquals("view history", event.getNewValue(), "View manager state should be 'view history'");
    }

    @Test
    void testVisitVMFieldsAreCorrectlyPopulated() {
        // Arrange: Create detailed visit data
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO(
                "visit-id-123",
                "University College",
                "Monday, 10:45AM"
        ));

        ViewHistoryOutputData outputData = new ViewHistoryOutputData("johndoe", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert
        ViewHistoryState state = viewModel.getState();
        ViewHistoryState.VisitVM visit = state.getVisits().get(0);

        assertEquals("visit-id-123", visit.visitId, "Visit ID should match");
        assertEquals("University College", visit.landmarkName, "Landmark name should match");
        assertEquals("Monday, 10:45AM", visit.visitedAt, "Visited at should match");
    }

    @Test
    void testConversionPreservesVisitOrder() {
        // Arrange: Create visits in specific order
        List<ViewHistoryOutputData.VisitDTO> visitDTOs = new ArrayList<>();
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v3", "Third", "Today, 3:00PM"));
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v2", "Second", "Today, 2:00PM"));
        visitDTOs.add(new ViewHistoryOutputData.VisitDTO("v1", "First", "Today, 1:00PM"));

        ViewHistoryOutputData outputData = new ViewHistoryOutputData("testuser", visitDTOs);

        // Act
        presenter.prepareSuccessView(outputData);

        // Assert: Order should be preserved
        ViewHistoryState state = viewModel.getState();
        List<ViewHistoryState.VisitVM> visits = state.getVisits();

        assertEquals("v3", visits.get(0).visitId, "First visit should be v3");
        assertEquals("v2", visits.get(1).visitId, "Second visit should be v2");
        assertEquals("v1", visits.get(2).visitId, "Third visit should be v1");
    }

    // =============== Mock Classes ===============

    /**
     * Mock PropertyChangeListener to verify property changes are fired.
     */
    private static class MockPropertyChangeListener implements PropertyChangeListener {
        private boolean propertyChangeFired = false;
        private PropertyChangeEvent lastEvent;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propertyChangeFired = true;
            lastEvent = evt;
        }

        boolean wasPropertyChangeFired() {
            return propertyChangeFired;
        }

        PropertyChangeEvent getLastEvent() {
            return lastEvent;
        }
    }
}
