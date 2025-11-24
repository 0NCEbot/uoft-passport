package interface_adapter.deletenote;

import interface_adapter.ViewManagerModel;
import use_case.deletenote.DeleteNoteOutputBoundary;
import use_case.deletenote.DeleteNoteOutputData;

public class DeleteNotePresenter implements DeleteNoteOutputBoundary {
    private final DeleteNoteViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public DeleteNotePresenter(DeleteNoteViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(DeleteNoteOutputData outputData) {
        DeleteNoteState state = viewModel.getState();
        state.setNoteId(outputData.getNoteId());
        state.setLandmarkName(outputData.getLandmarkName());
        state.setError(null);

        viewModel.setState(state);
        viewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String error) {
        DeleteNoteState state = viewModel.getState();
        state.setError(error);

        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}