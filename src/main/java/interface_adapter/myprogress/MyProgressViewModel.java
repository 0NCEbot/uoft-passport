package interface_adapter.myprogress;

import interface_adapter.ViewModel;

public class MyProgressViewModel extends ViewModel<MyProgressState> {

    public MyProgressViewModel() {
        super("my progress");  // View name for CardLayout
        setState(new MyProgressState());
    }
}