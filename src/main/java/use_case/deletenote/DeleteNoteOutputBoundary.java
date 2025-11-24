package use_case.deletenote;

public interface DeleteNoteOutputBoundary {
    void prepareSuccessView(DeleteNoteOutputData outputData);
    void prepareFailView(String error);
}