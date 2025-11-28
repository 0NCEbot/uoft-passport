package interface_adapter.addnotes;

import interface_adapter.ViewModel;

public class AddNotesViewModel extends ViewModel<AddNotesState> {

    public AddNotesViewModel() {
        super("notes");                  // <--- view name
        setState(new AddNotesState());
    }
}
