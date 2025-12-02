package interface_adapter.myprogress;

import use_case.myprogress.MyProgressInputBoundary;

public class MyProgressController {

    private final MyProgressInputBoundary interactor;

    public MyProgressController(MyProgressInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute();
    }
}