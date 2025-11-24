package interface_adapter.deletenote;

import interface_adapter.ViewModel;

public class DeleteNoteViewModel extends ViewModel<DeleteNoteState> {

    public DeleteNoteViewModel() {
        super("delete note");
        setState(new DeleteNoteState());
    }
}
