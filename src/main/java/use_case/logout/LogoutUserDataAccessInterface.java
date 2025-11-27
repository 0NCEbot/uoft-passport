package use_case.logout;

/**
 * DAO interface for the Logout Use Case.
 */
public interface LogoutUserDataAccessInterface {
    /**
     * Sets the current username (or clears it by setting to null).
     * @param username the username to set as current, or null to clear
     */
    void setCurrentUsername(String username);

    /**
     * Gets the currently logged-in username.
     * @return the current username, or null if no user is logged in
     */
    String getCurrentUsername();
}