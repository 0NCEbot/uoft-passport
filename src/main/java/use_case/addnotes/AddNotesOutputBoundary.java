// src/java/use_case/notes/AddNoteOutputBoundary.java
package use_case.addnotes;

public interface AddNotesOutputBoundary {
    void prepareSuccessView(AddNotesOutputData outputData);
    void prepareFailView(String errorMessage);
}
