package interface_adapter.planroute;

import use_case.planroute.PlanRouteInputBoundary;
import use_case.planroute.PlanRouteInputData;

public class PlanRouteController {

    private final PlanRouteInputBoundary interactor;
    private final PlanRouteViewModel viewModel;

    public PlanRouteController(PlanRouteInputBoundary interactor,
                               PlanRouteViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    /**
     * Plan a route from start to destination with optional intermediates.
     * Username is retrieved from the current session.
     */
    public void planRoute(String startLocation, String destination,
                          String[] intermediates) {
        String username = "test_user"; // TODO: Get from UserDataAccessInterface

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
     * Returns the landmark name for the calling code to handle the check-in.
     */
    public String checkInAtLandmark() {
        PlanRouteState state = viewModel.getState();

        if (state.isRouteCompleted()) {
            return null;
        }

        PlanRouteState.StepVM currentStep = state.getCurrentStep();
        if (currentStep != null && currentStep.isLandmark) {
            // Mark current landmark as completed
            currentStep.completed = true;

            // Move to next step
            state.setCurrentStepIndex(state.getCurrentStepIndex() + 1);

            viewModel.setState(state);
            viewModel.firePropertyChange();

            // Return landmark name for check-in
            return currentStep.landmarkName;
        }

        return null;
    }
}