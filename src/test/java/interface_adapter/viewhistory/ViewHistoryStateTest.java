package interface_adapter.viewhistory;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ViewHistoryState.
 * Tests the state object including:
 * - Default constructor initialization
 * - Copy constructor (deep copy)
 * - Getters and setters
 * - VisitVM nested class
 * - Null handling
 */
class ViewHistoryStateTest {

    @Test
    void testDefaultConstructor() {
        // Act
        ViewHistoryState state = new ViewHistoryState();

        // Assert: Check default values
        assertEquals("", state.getUsername(), "Default username should be empty string");
        assertNotNull(state.getVisits(), "Visits list should not be null");
        assertEquals(0, state.getVisits().size(), "Visits list should be empty");
        assertNull(state.getErrorMessage(), "Default error message should be null");
        assertNull(state.getSuccessMessage(), "Default success message should be null");
    }

    @Test
    void testSetAndGetUsername() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();

        // Act
        state.setUsername("testuser");

        // Assert
        assertEquals("testuser", state.getUsername());
    }

    @Test
    void testSetAndGetVisits() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();
        List<ViewHistoryState.VisitVM> visits = new ArrayList<>();
        visits.add(new ViewHistoryState.VisitVM("v1", "Landmark1", "Today, 1:00PM"));

        // Act
        state.setVisits(visits);

        // Assert
        assertEquals(1, state.getVisits().size());
        assertEquals("v1", state.getVisits().get(0).visitId);
    }

    @Test
    void testSetAndGetErrorMessage() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();

        // Act
        state.setErrorMessage("Error occurred");

        // Assert
        assertEquals("Error occurred", state.getErrorMessage());
    }

    @Test
    void testSetAndGetSuccessMessage() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();

        // Act
        state.setSuccessMessage("Success!");

        // Assert
        assertEquals("Success!", state.getSuccessMessage());
    }

    @Test
    void testCopyConstructorCreatesDeepCopy() {
        // Arrange: Create original state with data
        ViewHistoryState original = new ViewHistoryState();
        original.setUsername("originaluser");
        original.setErrorMessage("Original error");
        original.setSuccessMessage("Original success");

        List<ViewHistoryState.VisitVM> originalVisits = new ArrayList<>();
        ViewHistoryState.VisitVM visit1 = new ViewHistoryState.VisitVM("v1", "Landmark1", "Today, 1:00PM");
        originalVisits.add(visit1);
        original.setVisits(originalVisits);

        // Act: Create copy
        ViewHistoryState copy = new ViewHistoryState(original);

        // Assert: Values should be copied
        assertEquals("originaluser", copy.getUsername());
        assertEquals("Original error", copy.getErrorMessage());
        assertEquals("Original success", copy.getSuccessMessage());
        assertEquals(1, copy.getVisits().size());
        assertEquals("v1", copy.getVisits().get(0).visitId);

        // Assert: Deep copy - modifying copy should not affect original
        copy.setUsername("modifieduser");
        assertEquals("originaluser", original.getUsername(), "Original username should not change");

        copy.getVisits().get(0).visitId = "modified";
        assertEquals("v1", original.getVisits().get(0).visitId,
                "Original visit ID should not change (deep copy)");
    }

    @Test
    void testCopyConstructorWithNullVisitsList() {
        // Arrange: Create state with null visits list
        ViewHistoryState original = new ViewHistoryState();
        original.setUsername("testuser");
        original.setVisits(null);

        // Act: Create copy
        ViewHistoryState copy = new ViewHistoryState(original);

        // Assert: Copy should have empty list, not null
        assertNotNull(copy.getVisits(), "Copy should have non-null visits list");
        assertEquals(0, copy.getVisits().size(), "Copy should have empty visits list");
        assertEquals("testuser", copy.getUsername());
    }

    @Test
    void testCopyConstructorWithMultipleVisits() {
        // Arrange: Create state with multiple visits
        ViewHistoryState original = new ViewHistoryState();
        List<ViewHistoryState.VisitVM> visits = new ArrayList<>();
        visits.add(new ViewHistoryState.VisitVM("v1", "Landmark1", "Today, 1:00PM"));
        visits.add(new ViewHistoryState.VisitVM("v2", "Landmark2", "Yesterday, 2:00PM"));
        visits.add(new ViewHistoryState.VisitVM("v3", "Landmark3", "Monday, 3:00PM"));
        original.setVisits(visits);

        // Act: Create copy
        ViewHistoryState copy = new ViewHistoryState(original);

        // Assert: All visits should be copied
        assertEquals(3, copy.getVisits().size());
        assertEquals("v1", copy.getVisits().get(0).visitId);
        assertEquals("v2", copy.getVisits().get(1).visitId);
        assertEquals("v3", copy.getVisits().get(2).visitId);

        // Verify deep copy - modifying copy doesn't affect original
        copy.getVisits().add(new ViewHistoryState.VisitVM("v4", "Landmark4", "Today, 4:00PM"));
        assertEquals(3, original.getVisits().size(),
                "Original should still have 3 visits");
    }

    @Test
    void testVisitVMDefaultConstructor() {
        // Act
        ViewHistoryState.VisitVM visitVM = new ViewHistoryState.VisitVM();

        // Assert: Fields should be null (default for object fields)
        assertNull(visitVM.visitId);
        assertNull(visitVM.landmarkName);
        assertNull(visitVM.visitedAt);
    }

    @Test
    void testVisitVMParameterizedConstructor() {
        // Act
        ViewHistoryState.VisitVM visitVM = new ViewHistoryState.VisitVM(
                "visit-123",
                "Robarts Library",
                "Today, 5:00PM"
        );

        // Assert
        assertEquals("visit-123", visitVM.visitId);
        assertEquals("Robarts Library", visitVM.landmarkName);
        assertEquals("Today, 5:00PM", visitVM.visitedAt);
    }

    @Test
    void testVisitVMFieldsArePublic() {
        // Arrange
        ViewHistoryState.VisitVM visitVM = new ViewHistoryState.VisitVM();

        // Act: Direct field access (should compile if fields are public)
        visitVM.visitId = "v1";
        visitVM.landmarkName = "Hart House";
        visitVM.visitedAt = "Yesterday, 3:30PM";

        // Assert
        assertEquals("v1", visitVM.visitId);
        assertEquals("Hart House", visitVM.landmarkName);
        assertEquals("Yesterday, 3:30PM", visitVM.visitedAt);
    }

    @Test
    void testStateCanHoldEmptyVisitsList() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();
        state.setVisits(new ArrayList<>());

        // Assert
        assertNotNull(state.getVisits());
        assertEquals(0, state.getVisits().size());
    }

    @Test
    void testStateCanHoldNullMessages() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();

        // Act
        state.setErrorMessage(null);
        state.setSuccessMessage(null);

        // Assert
        assertNull(state.getErrorMessage());
        assertNull(state.getSuccessMessage());
    }

    @Test
    void testMultipleSettersOnSameState() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();

        // Act: Set multiple values
        state.setUsername("user1");
        state.setErrorMessage("Error 1");
        state.setUsername("user2");  // Overwrite username
        state.setSuccessMessage("Success!");
        state.setErrorMessage(null);  // Clear error

        // Assert: Latest values should be retained
        assertEquals("user2", state.getUsername());
        assertNull(state.getErrorMessage());
        assertEquals("Success!", state.getSuccessMessage());
    }

    @Test
    void testCopyConstructorWithEmptyVisitsList() {
        // Arrange
        ViewHistoryState original = new ViewHistoryState();
        original.setVisits(new ArrayList<>());
        original.setUsername("testuser");

        // Act
        ViewHistoryState copy = new ViewHistoryState(original);

        // Assert
        assertEquals("testuser", copy.getUsername());
        assertNotNull(copy.getVisits());
        assertEquals(0, copy.getVisits().size());
    }

    @Test
    void testVisitVMCanBeModifiedAfterCreation() {
        // Arrange
        ViewHistoryState.VisitVM visitVM = new ViewHistoryState.VisitVM(
                "original-id",
                "Original Landmark",
                "Original Time"
        );

        // Act: Modify fields
        visitVM.visitId = "modified-id";
        visitVM.landmarkName = "Modified Landmark";
        visitVM.visitedAt = "Modified Time";

        // Assert
        assertEquals("modified-id", visitVM.visitId);
        assertEquals("Modified Landmark", visitVM.landmarkName);
        assertEquals("Modified Time", visitVM.visitedAt);
    }

    @Test
    void testStatePreservesVisitsListReference() {
        // Arrange
        ViewHistoryState state = new ViewHistoryState();
        List<ViewHistoryState.VisitVM> visitsList = new ArrayList<>();
        visitsList.add(new ViewHistoryState.VisitVM("v1", "Landmark1", "Time1"));

        // Act
        state.setVisits(visitsList);

        // Assert: Same list reference should be returned
        assertSame(visitsList, state.getVisits(),
                "State should return the same list reference");

        // Modifying the returned list should affect the state
        state.getVisits().add(new ViewHistoryState.VisitVM("v2", "Landmark2", "Time2"));
        assertEquals(2, state.getVisits().size(),
                "State should reflect changes to the list");
    }

    @Test
    void testCopyConstructorCreatesNewVisitsList() {
        // Arrange
        ViewHistoryState original = new ViewHistoryState();
        List<ViewHistoryState.VisitVM> originalVisits = new ArrayList<>();
        originalVisits.add(new ViewHistoryState.VisitVM("v1", "Landmark1", "Time1"));
        original.setVisits(originalVisits);

        // Act
        ViewHistoryState copy = new ViewHistoryState(original);

        // Assert: Lists should be different objects
        assertNotSame(original.getVisits(), copy.getVisits(),
                "Copy should have a different list object");

        // But contain the same data
        assertEquals(original.getVisits().size(), copy.getVisits().size());
        assertEquals(original.getVisits().get(0).visitId, copy.getVisits().get(0).visitId);
    }

    @Test
    void testCopyConstructorDeepCopiesVisitVMFields() {
        // Arrange
        ViewHistoryState original = new ViewHistoryState();
        List<ViewHistoryState.VisitVM> visits = new ArrayList<>();
        ViewHistoryState.VisitVM originalVisit = new ViewHistoryState.VisitVM(
                "v1", "Original Landmark", "Original Time"
        );
        visits.add(originalVisit);
        original.setVisits(visits);

        // Act
        ViewHistoryState copy = new ViewHistoryState(original);

        // Modify the copied visit
        copy.getVisits().get(0).landmarkName = "Modified Landmark";

        // Assert: Original should not be affected
        assertEquals("Original Landmark", original.getVisits().get(0).landmarkName,
                "Original visit should not be affected by changes to copy");
        assertEquals("Modified Landmark", copy.getVisits().get(0).landmarkName,
                "Copy should have the modified value");
    }
}
