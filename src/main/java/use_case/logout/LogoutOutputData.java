package use_case.logout;

/**
 * Output Data for the Logout Use Case.
 */
public class LogoutOutputData {
    private final String username;
    private final boolean success;

    public LogoutOutputData(String username, boolean success) {
        this.username = username;
        this.success = success;
    }

    public String getUsername() {
        return username;
    }

    public boolean isSuccess() {
        return success;
    }
}