// PlanRouteOutputBoundary.java
package use_case.planroute;

public interface PlanRouteOutputBoundary {
    void presentRoute(PlanRouteOutputData outputData);
    void presentError(String errorMessage);
}