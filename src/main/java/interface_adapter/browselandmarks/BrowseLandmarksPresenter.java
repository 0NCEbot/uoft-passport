// src/java/interface_adapter/browse_landmarks/BrowseLandmarksPresenter.java
package interface_adapter.browselandmarks;

import use_case.browselandmarks.BrowseLandmarksOutputBoundary;
import use_case.browselandmarks.BrowseLandmarksOutputData;

import java.util.List;
import java.util.stream.Collectors;

public class BrowseLandmarksPresenter implements BrowseLandmarksOutputBoundary {

    private final BrowseLandmarksViewModel viewModel;

    public BrowseLandmarksPresenter(BrowseLandmarksViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentLandmarks(BrowseLandmarksOutputData outputData) {
        BrowseLandmarksState state = new BrowseLandmarksState();

        List<BrowseLandmarksState.LandmarkVM> vms = outputData.getLandmarks().stream()
                .map(dto -> {
                    BrowseLandmarksState.LandmarkVM vm = new BrowseLandmarksState.LandmarkVM();
                    vm.name = dto.name;
                    vm.latitude = dto.latitude;
                    vm.longitude = dto.longitude;
                    vm.type = dto.type;
                    vm.visitCount = dto.visitCount;
                    return vm;
                })
                .collect(Collectors.toList());

        state.setLandmarks(vms);

        viewModel.setState(state);
        viewModel.firePropertyChange();   // notify the view
    }
}
