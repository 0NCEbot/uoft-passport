// src/java/use_case/notes/AddNoteInteractor.java
package use_case.addnotes;

import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
import entity.Landmark;
import entity.Note;
import entity.User;

import java.util.List;

public class AddNotesInteractor implements AddNotesInputBoundary {

    private final UserDataAccessInterface userDAO;
    private final LandmarkDataAccessInterface landmarkDAO;
    private final AddNotesOutputBoundary presenter;

    public AddNotesInteractor(UserDataAccessInterface userDAO,
                              LandmarkDataAccessInterface landmarkDAO,
                              AddNotesOutputBoundary presenter) {
        this.userDAO = userDAO;
        this.landmarkDAO = landmarkDAO;
        this.presenter = presenter;
    }

    @Override
    public void addNote(AddNotesInputData inputData) {
        // 1. Basic validation
        String content = inputData.getContent();
        if (content == null || content.trim().isEmpty()) {
            presenter.prepareFailView("Note cannot be empty.");
            return;
        }

        // 2. Load user
        User user = userDAO.get(inputData.getUsername());
        if (user == null) {
            presenter.prepareFailView("User not found: " + inputData.getUsername());
            return;
        }

        // 3. Load landmark (using your existing LandmarkDataAccessInterface)
        Landmark landmark;
        try {
            landmark = landmarkDAO.findByName(inputData.getLandmarkName());
        } catch (RuntimeException ex) {
            presenter.prepareFailView("Landmark not found: " + inputData.getLandmarkName());
            return;
        }

        // 4. Create and attach Note
        Note note = new Note(landmark, content);

        // privateNotes list is final but mutable
        List<Note> notes = user.getPrivateNotes();
        notes.add(note);

        // 5. Persist updated user
        userDAO.save(user);

        // 6. Notify presenter
        AddNotesOutputData outputData = new AddNotesOutputData(
                user.getUsername(),
                landmark.getLandmarkName(),
                "Note added successfully."
        );
        presenter.prepareSuccessView(outputData);
    }
}
