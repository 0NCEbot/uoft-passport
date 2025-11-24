package interface_adapter.editnote;

import interface_adapter.ViewModel;

public class EditNoteViewModel extends ViewModel<EditNoteState> {

    public static final String TITLE_LABEL = "Edit Note";
    public static final String SAVE_BUTTON_LABEL = "Save Changes";
    public static final String CANCEL_BUTTON_LABEL = "Cancel";

    public EditNoteViewModel() {
        super("edit note");
        setState(new EditNoteState());
    }
}