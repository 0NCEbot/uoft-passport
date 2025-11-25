package interface_adapter.myprogress;

import interface_adapter.ViewManagerModel;
import use_case.myprogress.MyProgressOutputBoundary;
import use_case.myprogress.MyProgressOutputData;

public class MyProgressPresenter implements MyProgressOutputBoundary {

    private final MyProgressViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public MyProgressPresenter(MyProgressViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(MyProgressOutputData outputData) {
        MyProgressState state = viewModel.getState();

        // Copy all stats from OutputData to State
        state.setUsername(outputData.getUsername());
        state.setUniqueLandmarksVisited(outputData.getUniqueLandmarksVisited());
        state.setUniqueLandmarksTotal(outputData.getUniqueLandmarksTotal());
        state.setUniqueLandmarksCompletionPercentage(outputData.getUniqueLandmarksCompletionPercentage());
        state.setTotalVisitsToday(outputData.getTotalVisitsToday());
        state.setTotalVisitsPastWeek(outputData.getTotalVisitsPastWeek());
        state.setTotalVisitsPastMonth(outputData.getTotalVisitsPastMonth());
        state.setTotalVisits(outputData.getTotalVisits());
        state.setCurrentVisitStreak(outputData.getCurrentVisitStreak());
        state.setLongestVisitStreak(outputData.getLongestVisitStreak());
        state.setMostVisitedLandmarkName(outputData.getMostVisitedLandmarkName());
        state.setMostVisitedLandmarkCount(outputData.getMostVisitedLandmarkCount());

        viewModel.setState(state);
        viewModel.firePropertyChange();

        viewManagerModel.setState("my progress");
        viewManagerModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        throw new RuntimeException(errorMessage);
    }
}