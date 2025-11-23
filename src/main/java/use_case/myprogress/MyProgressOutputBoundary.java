package use_case.myprogress;

public interface MyProgressOutputBoundary {
    void prepareSuccessView(MyProgressOutputData outputData);
    void prepareFailView(String errorMessage);
}
