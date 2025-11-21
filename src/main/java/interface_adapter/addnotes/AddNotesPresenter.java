package interface_adapter.addnotes;

import interface_adapter.ViewManagerModel;
import use_case.addnotes.AddNotesOutputBoundary;
import use_case.addnotes.AddNotesOutputData;

public class AddNotesPresenter implements AddNotesOutputBoundary {

    private final AddNotesViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public AddNotesPresenter(AddNotesViewModel viewModel,
                          ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(AddNotesOutputData outputData) {
        AddNotesState state = viewModel.getState();
        state.setUsername(outputData.getUsername());
        state.setLandmarkName(outputData.getLandmarkName());
        state.setContent("");                    // clear text area
        state.setErrorMessage(null);
        state.setSuccessMessage(outputData.getMessage());

        viewModel.setState(state);
        viewModel.firePropertyChange();

        // you can decide where to stay/redirect; for now stay on "notes"
        viewManagerModel.setState("notes");
        viewManagerModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        AddNotesState state = viewModel.getState();
        state.setErrorMessage(errorMessage);
        state.setSuccessMessage(null);

        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}
