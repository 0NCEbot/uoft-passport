package use_case.logout;

/**
 * Input Data for the Logout Use Case.
 */
public class LogoutInputData {
    private final String username;

    public LogoutInputData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}