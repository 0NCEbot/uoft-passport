package interface_adapter.login;

import data_access.UserDataAccessInterface;
import interface_adapter.ViewManagerModel;
import interface_adapter.homescreen.HomeScreenState;
import interface_adapter.homescreen.HomescreenViewModel;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/**
 * The Presenter for the Login Use Case.
 */
public class LoginPresenter implements LoginOutputBoundary {

    private final LoginViewModel loginViewModel;
    private final HomescreenViewModel homescreenViewModel;
    private final ViewManagerModel viewManagerModel;
    private final UserDataAccessInterface userDataAccessObject;

    public LoginPresenter(ViewManagerModel viewManagerModel,
                          HomescreenViewModel homescreenViewModel,
                          LoginViewModel loginViewModel,
                          UserDataAccessInterface userDataAccessObject) {
        this.viewManagerModel = viewManagerModel;
        this.homescreenViewModel = homescreenViewModel;
        this.loginViewModel = loginViewModel;
        this.userDataAccessObject = userDataAccessObject;
    }

    @Override
    public void prepareSuccessView(LoginOutputData response) {
        HomeScreenState hsState = homescreenViewModel.getState();
        hsState.setUsername(response.getUsername());   // <── store user here
        userDataAccessObject.setCurrentUsername(response.getUsername());
        homescreenViewModel.setState(hsState);
        homescreenViewModel.firePropertyChange();

        loginViewModel.setState(new LoginState());
        loginViewModel.firePropertyChange();

        this.viewManagerModel.setState("homescreen");
        this.viewManagerModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String error) {
        final LoginState loginState = loginViewModel.getState();
        loginState.setLoginError(error);
        loginViewModel.firePropertyChange();
    }
}
