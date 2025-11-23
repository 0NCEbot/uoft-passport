package interface_adapter.myprogress;

import use_case.myprogress.MyProgressInputBoundary;
import use_case.myprogress.MyProgressInputData;

public class MyProgressController {

    private final MyProgressInputBoundary myProgressInputBoundary;

    public MyProgressController(MyProgressInputBoundary myProgressInputBoundary) {
        this.myProgressInputBoundary = myProgressInputBoundary;
    }

    public void execute(String username) {
        MyProgressInputData inputData = new MyProgressInputData(username);
        myProgressInputBoundary.execute(inputData);
    }
}