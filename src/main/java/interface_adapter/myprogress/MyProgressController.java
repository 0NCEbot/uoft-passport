package interface_adapter.myprogress;

import use_case.myprogress.MyProgressInputBoundary;
import use_case.myprogress.MyProgressInputData;

public class MyProgressController {

    private final MyProgressInputBoundary interactor;

    public MyProgressController(MyProgressInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String username) {
        MyProgressInputData inputData = new MyProgressInputData(username);
        interactor.execute(inputData);
    }
}