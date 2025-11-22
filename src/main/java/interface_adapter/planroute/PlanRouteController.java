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
     * Mark a step as completed when user checks it off.
     */
    public void completeStep(int stepIndex) {
        PlanRouteState state = viewModel.getState();
        if (stepIndex < state.getSteps().size()) {
            state.getSteps().get(stepIndex).completed = true;
            viewModel.setState(state);
            viewModel.firePropertyChange();
        }
    }
}