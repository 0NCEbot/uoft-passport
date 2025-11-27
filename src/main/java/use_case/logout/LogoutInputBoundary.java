package use_case.logout;

/**
 * Input Boundary for the Logout Use Case.
 */
public interface LogoutInputBoundary {
    /**
     * Executes the logout use case.
     * @param inputData the input data
     */
    void execute(LogoutInputData inputData);
}