package interface_adapter.myprogress;

import use_case.myprogress.MyProgressOutputBoundary;
import use_case.myprogress.MyProgressOutputData;

public class MyProgressPresenter implements MyProgressOutputBoundary {

    private final MyProgressViewModel viewModel;

    public MyProgressPresenter(MyProgressViewModel viewModel) {
        this.viewModel = viewModel;
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
    }

    @Override
    public void prepareFailView(String errorMessage) {
        throw new RuntimeException(errorMessage);
    }
}