package interface_adapter.editnote;

import interface_adapter.ViewManagerModel;
import use_case.editnote.EditNoteOutputBoundary;
import use_case.editnote.EditNoteOutputData;

public class EditNotePresenter implements EditNoteOutputBoundary {
    private final EditNoteViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public EditNotePresenter(EditNoteViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void prepareSuccessView(EditNoteOutputData outputData) {
        EditNoteState state = viewModel.getState();
        state.setNoteId(outputData.getNoteId());
        state.setContent(outputData.getUpdatedContent());
        state.setError(null);

        viewModel.setState(state);
        viewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String error) {
        EditNoteState state = viewModel.getState();
        state.setError(error);

        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}