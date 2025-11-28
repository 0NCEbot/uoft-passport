package interface_adapter.planroute;

import use_case.planroute.PlanRouteInputBoundary;
import use_case.planroute.PlanRouteInputData;
import data_access.UserDataAccessInterface;
import data_access.LandmarkDataAccessInterface;
import entity.User;
import entity.Landmark;
import entity.Visit;

public class PlanRouteController {

    private final PlanRouteInputBoundary interactor;
    private final PlanRouteViewModel viewModel;
    private final UserDataAccessInterface userDAO;
    private final LandmarkDataAccessInterface landmarkDAO;

    public PlanRouteController(PlanRouteInputBoundary interactor,
                               PlanRouteViewModel viewModel,
                               UserDataAccessInterface userDAO,
                               LandmarkDataAccessInterface landmarkDAO) {
        this.interactor = interactor;
        this.viewModel = viewModel;
        this.userDAO = userDAO;
        this.landmarkDAO = landmarkDAO;
    }

    /**
     * Plan a route from start to destination with optional intermediates.
     */
    public void planRoute(String startLocation, String destination,
                          String[] intermediates) {
        String username = userDAO.getCurrentUsername();

        PlanRouteInputData inputData = new PlanRouteInputData(
                username, startLocation, destination, intermediates
        );

        interactor.planRoute(inputData);
    }

    /**
     * Complete the current step (for navigation steps).
     */
    public void completeStep() {
        PlanRouteState state = viewModel.getState();

        if (state.isRouteCompleted()) {
            return; // Already done
        }

        PlanRouteState.StepVM currentStep = state.getCurrentStep();
        if (currentStep != null && !currentStep.isLandmark) {
            // Mark current step as completed
            currentStep.completed = true;

            // Move to next step
            state.setCurrentStepIndex(state.getCurrentStepIndex() + 1);

            viewModel.setState(state);
            viewModel.firePropertyChange();
        }
    }

    /**
     * Check in at a landmark (for landmark steps).
     * Performs check-in directly without navigation side effects.
     * Returns the landmark name for UI feedback.
     */
    public String checkInAtLandmark() {
        PlanRouteState state = viewModel.getState();

        if (state.isRouteCompleted()) {
            return null;
        }

        PlanRouteState.StepVM currentStep = state.getCurrentStep();
        if (currentStep != null && currentStep.isLandmark) {
            String landmarkName = currentStep.landmarkName;
            String username = userDAO.getCurrentUsername();

            // Perform check-in directly without triggering view changes
            if (landmarkName != null && username != null) {
                try {
                    User user = userDAO.get(username);
                    if (user == null) {
                        System.err.println("[PlanRoute] User not found: " + username);
                        return null;
                    }

                    Landmark landmark = landmarkDAO.findByName(landmarkName);
                    if (landmark == null) {
                        System.err.println("[PlanRoute] Landmark not found: " + landmarkName);
                        return null;
                    }

                    // Add visit directly
                    user.getVisits().add(new Visit(landmark));
                    userDAO.save(user);

                    System.out.println("[PlanRoute] Checked in at " + landmarkName + " for user " + username);
                } catch (Exception e) {
                    System.err.println("[PlanRoute] Failed to check in: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Mark current landmark as completed
            currentStep.completed = true;

            // Move to next step
            state.setCurrentStepIndex(state.getCurrentStepIndex() + 1);

            viewModel.setState(state);
            viewModel.firePropertyChange();

            return landmarkName;
        }

        return null;
    }
}