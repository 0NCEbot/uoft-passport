package use_case.planroute;

import data_access.LandmarkDataAccessInterface;
import data_access.RouteDataAccessInterface;
import entity.RouteStep;

import java.util.ArrayList;
import java.util.List;

public class PlanRouteInteractor implements PlanRouteInputBoundary {

    private final RouteDataAccessInterface routeDAO;
    private final LandmarkDataAccessInterface landmarkDAO;
    private final PlanRouteOutputBoundary presenter;

    public PlanRouteInteractor(RouteDataAccessInterface routeDAO,
                               LandmarkDataAccessInterface landmarkDAO,
                               PlanRouteOutputBoundary presenter) {
        this.routeDAO = routeDAO;
        this.landmarkDAO = landmarkDAO;
        this.presenter = presenter;
    }

    @Override
    public void planRoute(PlanRouteInputData inputData) {
        String start = inputData.getStartLocation();
        String destination = inputData.getDestination();
        String[] intermediates = inputData.getIntermediateStops();

        if (start == null || start.isBlank()) {
            presenter.presentError("Start location cannot be empty.");
            return;
        }
        if (destination == null || destination.isBlank()) {
            presenter.presentError("Destination cannot be empty.");
            return;
        }

        try {
            RouteDataAccessInterface.RouteResponse response = routeDAO.getRoute(start, destination, intermediates);

            if (response == null) {
                presenter.presentError("Failed to plan route. Please try again.");
                return;
            }

            if (!response.isSuccessful()) {
                String errorMsg = response.getErrorMessage();
                presenter.presentError(errorMsg != null && !errorMsg.isEmpty()
                        ? errorMsg : "Failed to plan route. Please try again.");
                return;
            }

            // Fetch static map image
            byte[] mapImageBytes = routeDAO.getStaticMapImage(
                    response.getEncodedPolyline(),
                    response.getStartLocation(),
                    response.getEndLocation(),
                    response.getIntermediateLocations()
            );

            List<PlanRouteOutputData.RouteStepDTO> steps = convertStepsToDTO(response.getSteps());

            PlanRouteOutputData output = new PlanRouteOutputData(
                    start,
                    destination,
                    steps,
                    response.getTotalDistanceMeters(),
                    response.getTotalDurationSeconds(),
                    null,
                    true,
                    response.isManualMode(),
                    mapImageBytes  // Pass map image
            );

            presenter.presentRoute(output);

        } catch (Exception e) {
            e.printStackTrace();
            presenter.presentError("An error occurred while planning the route. Please try again.");
        }
    }

    private List<PlanRouteOutputData.RouteStepDTO> convertStepsToDTO(List<RouteStep> steps) {
        List<PlanRouteOutputData.RouteStepDTO> dtos = new ArrayList<>();
        for (RouteStep step : steps) {
            dtos.add(new PlanRouteOutputData.RouteStepDTO(
                    step.getInstruction(),
                    step.getDistance(),
                    step.getDuration(),
                    step.getLandmarkName(),
                    step.isLandmark()
            ));
        }
        return dtos;
    }
}