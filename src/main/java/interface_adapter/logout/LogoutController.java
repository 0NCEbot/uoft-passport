package interface_adapter.logout;

import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutInputData;

/**
 * Controller for the Logout Use Case.
 */
public class LogoutController {
    private final LogoutInputBoundary logoutInteractor;

    public LogoutController(LogoutInputBoundary logoutInteractor) {
        this.logoutInteractor = logoutInteractor;
    }

    /**
     * Executes the logout use case.
     * @param username the username of the user logging out
     */
    public void execute(String username) {
        final LogoutInputData logoutInputData = new LogoutInputData(username);
        logoutInteractor.execute(logoutInputData);
    }
}