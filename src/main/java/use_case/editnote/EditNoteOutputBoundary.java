package use_case.editnote;

public interface EditNoteOutputBoundary {
    void prepareSuccessView(EditNoteOutputData outputData);
    void prepareFailView(String error);
}