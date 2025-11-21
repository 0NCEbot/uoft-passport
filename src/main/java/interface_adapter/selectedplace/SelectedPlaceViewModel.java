package interface_adapter.selectedplace;

import interface_adapter.ViewModel;

public class SelectedPlaceViewModel extends ViewModel<SelectedPlaceState> {

    public SelectedPlaceViewModel() {
        super("selected place");
        setState(new SelectedPlaceState());
    }
}
