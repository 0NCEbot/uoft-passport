package use_case.planroute;

import data_access.LandmarkDataAccessInterface;
import data_access.RouteDataAccessInterface;
import entity.Location;
import entity.RouteStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlanRouteInteractor.
 * Achieves 100% coverage of the interactor class.
 */
class PlanRouteInteractorTest {

    private PlanRouteInteractor interactor;
    private MockRouteDAO mockRouteDAO;
    private MockLandmarkDAO mockLandmarkDAO;
    private MockPlanRoutePresenter mockPresenter;

    @BeforeEach
    void setUp() {
        mockRouteDAO = new MockRouteDAO();
        mockLandmarkDAO = new MockLandmarkDAO();
        mockPresenter = new MockPlanRoutePresenter();
        interactor = new PlanRouteInteractor(mockRouteDAO, mockLandmarkDAO, mockPresenter);
    }

    // ==================== SUCCESS CASES ====================

    @Test
    void testPlanRoute_Success_WithNoIntermediates() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        Location start = new Location(43.6645, -79.3996);
        Location end = new Location(43.6596, -79.3975);
        List<RouteStep> steps = createMockSteps();

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                steps,
                500,  // 500 meters
                360,  // 6 minutes
                true,
                null,
                false,
                "encodedPolyline123",
                start,
                end,
                new ArrayList<>()
        ));

        mockRouteDAO.setMockMapImage(new byte[]{1, 2, 3, 4});

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertTrue(mockPresenter.wasRoutePresented(), "Route should be presented");
        assertFalse(mockPresenter.wasErrorPresented(), "No error should be presented");

        PlanRouteOutputData outputData = mockPresenter.getLastOutputData();
        assertNotNull(outputData, "Output data should not be null");
        assertEquals("Robarts Library", outputData.getStartLocation());
        assertEquals("Bahen Centre", outputData.getDestination());
        assertEquals(3, outputData.getSteps().size());
        assertEquals(500, outputData.getTotalDistanceMeters());
        assertEquals(360, outputData.getTotalDurationSeconds());
        assertTrue(outputData.isSuccess());
        assertFalse(outputData.isManualMode());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, outputData.getMapImageBytes());
    }

    @Test
    void testPlanRoute_Success_WithIntermediates() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[]{"Gerstein Library", "Medical Sciences"}
        );

        Location start = new Location(43.6645, -79.3996);
        Location end = new Location(43.6596, -79.3975);
        List<Location> intermediates = List.of(
                new Location(43.6621, -79.3935),
                new Location(43.6610, -79.3934)
        );
        List<RouteStep> steps = createMockSteps();

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                steps,
                1200,  // 1.2 km
                900,   // 15 minutes
                true,
                null,
                false,
                "encodedPolyline456",
                start,
                end,
                intermediates
        ));

        mockRouteDAO.setMockMapImage(new byte[]{5, 6, 7, 8});

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertTrue(mockPresenter.wasRoutePresented());
        assertFalse(mockPresenter.wasErrorPresented());

        PlanRouteOutputData outputData = mockPresenter.getLastOutputData();
        assertEquals(1200, outputData.getTotalDistanceMeters());
        assertEquals(900, outputData.getTotalDurationSeconds());
        assertArrayEquals(new byte[]{5, 6, 7, 8}, outputData.getMapImageBytes());
    }

    @Test
    void testPlanRoute_Success_ManualMode() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        List<RouteStep> manualSteps = List.of(
                new RouteStep(0, "📍 Robarts Library", 0, 0),
                new RouteStep(1, "Navigate to: Bahen Centre", 0, 0),
                new RouteStep(2, "📍 Bahen Centre", 0, 0)
        );

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                manualSteps,
                0,
                0,
                true,
                "API unavailable. Using self-guided mode.",
                true,  // Manual mode
                null,
                new Location(43.6645, -79.3996),
                new Location(43.6596, -79.3975),
                new ArrayList<>()
        ));

        mockRouteDAO.setMockMapImage(null);  // No map in manual mode

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertTrue(mockPresenter.wasRoutePresented());
        assertFalse(mockPresenter.wasErrorPresented());

        PlanRouteOutputData outputData = mockPresenter.getLastOutputData();
        assertTrue(outputData.isManualMode());
        assertEquals(3, outputData.getSteps().size());
        assertNull(outputData.getMapImageBytes());
    }

    // ==================== VALIDATION ERROR CASES ====================

    @Test
    void testPlanRoute_EmptyStartLocation() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "",  // Empty start
                "Bahen Centre",
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Start location cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_NullStartLocation() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                null,  // Null start
                "Bahen Centre",
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Start location cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_BlankStartLocation() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "   ",  // Whitespace only
                "Bahen Centre",
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Start location cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_EmptyDestination() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "",  // Empty destination
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Destination cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_NullDestination() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                null,  // Null destination
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Destination cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_BlankDestination() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "   ",  // Whitespace only
                new String[0]
        );

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Destination cannot be empty.", mockPresenter.getLastErrorMessage());
    }

    // ==================== DAO ERROR CASES ====================

    @Test
    void testPlanRoute_NullResponseFromDAO() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        mockRouteDAO.setMockResponse(null);  // Simulate DAO failure

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Failed to plan route. Please try again.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_UnsuccessfulResponse() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Invalid Location",
                "Bahen Centre",
                new String[0]
        );

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                new ArrayList<>(),
                0,
                0,
                false,  // Not successful
                "Start location 'Invalid Location' not found.",
                false,
                null,
                null,
                null,
                null
        ));

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Start location 'Invalid Location' not found.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_UnsuccessfulResponseWithNullErrorMessage() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                new ArrayList<>(),
                0,
                0,
                false,  // Not successful
                null,   // Null error message
                false,
                null,
                null,
                null,
                null
        ));

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Failed to plan route. Please try again.", mockPresenter.getLastErrorMessage());
    }

    @Test
    void testPlanRoute_UnsuccessfulResponseWithEmptyErrorMessage() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        mockRouteDAO.setMockResponse(new RouteDataAccessInterface.RouteResponse(
                new ArrayList<>(),
                0,
                0,
                false,  // Not successful
                "",     // Empty error message
                false,
                null,
                null,
                null,
                null
        ));

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("Failed to plan route. Please try again.", mockPresenter.getLastErrorMessage());
    }

    // ==================== EXCEPTION HANDLING ====================

    @Test
    void testPlanRoute_ExceptionDuringProcessing() {
        // Arrange
        PlanRouteInputData inputData = new PlanRouteInputData(
                "testuser",
                "Robarts Library",
                "Bahen Centre",
                new String[0]
        );

        mockRouteDAO.setThrowException(true);  // Simulate exception

        // Act
        interactor.planRoute(inputData);

        // Assert
        assertFalse(mockPresenter.wasRoutePresented());
        assertTrue(mockPresenter.wasErrorPresented());
        assertEquals("An error occurred while planning the route. Please try again.",
                mockPresenter.getLastErrorMessage());
    }

    // ==================== HELPER METHODS ====================

    private List<RouteStep> createMockSteps() {
        List<RouteStep> steps = new ArrayList<>();
        steps.add(new RouteStep(0, "📍 Robarts Library", 0, 0));
        steps.add(new RouteStep(1, "Head north on St. George St", 250, 180));
        steps.add(new RouteStep(2, "📍 Bahen Centre", 0, 0));
        return steps;
    }

    // ==================== MOCK CLASSES ====================

    /**
     * Mock implementation of RouteDataAccessInterface for testing.
     */
    private static class MockRouteDAO implements RouteDataAccessInterface {
        private RouteResponse mockResponse;
        private byte[] mockMapImage;
        private boolean throwException = false;

        public void setMockResponse(RouteResponse response) {
            this.mockResponse = response;
        }

        public void setMockMapImage(byte[] image) {
            this.mockMapImage = image;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

        @Override
        public RouteResponse getRoute(String start, String destination, String[] intermediates) {
            if (throwException) {
                throw new RuntimeException("Simulated DAO exception");
            }
            return mockResponse;
        }

        @Override
        public byte[] getStaticMapImage(String encodedPolyline, Location start,
                                        Location end, List<Location> intermediates) {
            return mockMapImage;
        }
    }

    /**
     * Mock implementation of LandmarkDataAccessInterface for testing.
     */
    private static class MockLandmarkDAO implements LandmarkDataAccessInterface {
        @Override
        public List<entity.Landmark> getLandmarks() {
            return new ArrayList<>();
        }

        @Override
        public boolean existsByName(String landmarkName) {
            return false;
        }

        @Override
        public entity.Landmark findByName(String name) {
            return null;
        }
    }

    /**
     * Mock implementation of PlanRouteOutputBoundary for testing.
     */
    private static class MockPlanRoutePresenter implements PlanRouteOutputBoundary {
        private boolean routePresented = false;
        private boolean errorPresented = false;
        private PlanRouteOutputData lastOutputData;
        private String lastErrorMessage;

        @Override
        public void presentRoute(PlanRouteOutputData outputData) {
            this.routePresented = true;
            this.lastOutputData = outputData;
        }

        @Override
        public void presentError(String errorMessage) {
            this.errorPresented = true;
            this.lastErrorMessage = errorMessage;
        }

        public boolean wasRoutePresented() {
            return routePresented;
        }

        public boolean wasErrorPresented() {
            return errorPresented;
        }

        public PlanRouteOutputData getLastOutputData() {
            return lastOutputData;
        }

        public String getLastErrorMessage() {
            return lastErrorMessage;
        }
    }
}