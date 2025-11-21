package interface_adapter.addnotes;

import use_case.addnotes.AddNotesInputBoundary;
import use_case.addnotes.AddNotesInputData;

public class AddNotesController {

    private final AddNotesInputBoundary interactor;
    private final AddNotesViewModel viewModel;

    public AddNotesController(AddNotesInputBoundary interactor,
                              AddNotesViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    // called by the View when "Add Note" button is clicked
    public void addNote(String content) {
        AddNotesState state = viewModel.getState();
        AddNotesInputData inputData = new AddNotesInputData(
                state.getUsername(),
                state.getLandmarkName(),
                content
        );
        interactor.addNote(inputData);
    }
}