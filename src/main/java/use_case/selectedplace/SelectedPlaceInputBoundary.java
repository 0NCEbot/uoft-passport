package use_case.selectedplace;

public interface SelectedPlaceInputBoundary {
    void selectPlace(SelectedPlaceInputData inputData);

    // NEW
    void checkIn(SelectedPlaceInputData inputData);

    void notes(SelectedPlaceInputData inputData);
}
