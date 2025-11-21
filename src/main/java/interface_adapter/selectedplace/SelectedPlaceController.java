package interface_adapter.selectedplace;

import use_case.selectedplace.SelectedPlaceInputBoundary;
import use_case.selectedplace.SelectedPlaceInputData;

public class SelectedPlaceController {

    private final SelectedPlaceInputBoundary interactor;

    // remember the last selected place
    private String lastUsername;
    private String lastLandmarkName;

    public SelectedPlaceController(SelectedPlaceInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void selectPlace(String username, String landmarkName) {
        this.lastUsername = username;
        this.lastLandmarkName = landmarkName;

        SelectedPlaceInputData input = new SelectedPlaceInputData(username, landmarkName);
        interactor.selectPlace(input);
    }

    // Check-in for the *currently selected* place
    public void checkIn() {
        if (lastUsername == null || lastLandmarkName == null) {
            System.out.println("[CONTROLLER] Check In clicked but no place selected yet");
            return;
        }

        SelectedPlaceInputData input = new SelectedPlaceInputData(lastUsername, lastLandmarkName);
        interactor.checkIn(input);
    }

    public void notes() {
        if (lastUsername == null || lastLandmarkName == null) {
            System.out.println("[CONTROLLER] Notes clicked but no place selected yet");
            return;
        }

        SelectedPlaceInputData input = new SelectedPlaceInputData(lastUsername, lastLandmarkName);
        interactor.notes(input);
    }
}
