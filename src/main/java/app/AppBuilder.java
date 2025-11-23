package app;

import data_access.*;
import entity.UserFactory;
import interface_adapter.ViewManagerModel;
import interface_adapter.browselandmarks.*;
import interface_adapter.homescreen.*;
import interface_adapter.login.*;
import interface_adapter.myprogress.*;
import interface_adapter.selectedplace.*;
import interface_adapter.signup.*;
import interface_adapter.addnotes.*;

import use_case.browselandmarks.BrowseLandmarksInputBoundary;
import use_case.browselandmarks.BrowseLandmarksInteractor;
import use_case.homescreen.HomescreenInputBoundary;
import use_case.homescreen.HomescreenInteractor;
import use_case.homescreen.HomescreenOutputBoundary;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.myprogress.MyProgressInputBoundary;
import use_case.myprogress.MyProgressInteractor;
import use_case.myprogress.MyProgressOutputBoundary;
import use_case.selectedplace.SelectedPlaceInputBoundary;
import use_case.selectedplace.SelectedPlaceInteractor;
import use_case.selectedplace.SelectedPlaceOutputBoundary;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
// NEW imports for Notes use case
import use_case.addnotes.AddNotesInputBoundary;
import use_case.addnotes.AddNotesInteractor;
import use_case.addnotes.AddNotesOutputBoundary;

import view.*;
// NEW Notes view

import javax.swing.*;
import java.awt.*;

public class AppBuilder {

    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();

    // core
    private final UserFactory userFactory = new UserFactory();
    private final ViewManagerModel viewManagerModel = new ViewManagerModel();
    @SuppressWarnings("unused")
    private final ViewManager viewManager =
            new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private final LandmarkDataAccessInterface landmarkDataAccessObject =
            new JsonLandmarkDataAccessObject("minimal_landmarks.json");

    private final UserDataAccessInterface userDataAccessObject =
            new JsonUserDataAccessObject("users.json", userFactory, landmarkDataAccessObject);

    // ---- view models & views ----
    private LoginViewModel loginViewModel;
    private LoginView loginView;

    private SignupViewModel signupViewModel;
    private SignupView signupView;

    private HomescreenViewModel homescreenViewModel;
    private HomescreenView homescreenView;

    private BrowseLandmarksViewModel browseLandmarksViewModel;
    private BrowseLandmarksView browseLandmarksView;

    private SelectedPlaceViewModel selectedPlaceViewModel;
    private SelectedPlaceView selectedPlaceView;

    // NEW: notes VM + view
    private AddNotesViewModel notesViewModel;
    private AddNotesView notesView;

    private MyProgressViewModel myProgressViewModel;
    private MyProgressView myProgressView;


    // ---- use case controllers ----
    private SelectedPlaceController selectedPlaceController;
    private BrowseLandmarksController browseLandmarksController;
    // NEW: notes controller
    private AddNotesController notesController;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    // === VIEW REGISTRATION ===

    public AppBuilder addLoginView() {
        loginViewModel = new LoginViewModel();
        loginView = new LoginView(loginViewModel, viewManagerModel);
        cardPanel.add(loginView, loginView.getViewName());
        return this;
    }

    public AppBuilder addHomescreenView() {
        homescreenViewModel = new HomescreenViewModel();
        homescreenView = new HomescreenView(homescreenViewModel, viewManagerModel);
        cardPanel.add(homescreenView, homescreenView.getViewName());
        return this;
    }

    public AppBuilder addSignupView() {
        signupViewModel = new SignupViewModel();
        signupView = new SignupView(signupViewModel, viewManagerModel);
        cardPanel.add(signupView, signupView.getViewName());
        return this;
    }

    public AppBuilder addNotesView() {
        notesViewModel = new AddNotesViewModel();
        notesView = new AddNotesView(notesViewModel, viewManagerModel);
        cardPanel.add(notesView, notesView.getViewName());
        return this;
    }

    public AppBuilder addSelectedPlaceView() {
        selectedPlaceViewModel = new SelectedPlaceViewModel();
        selectedPlaceView = new SelectedPlaceView(selectedPlaceViewModel, viewManagerModel);
        cardPanel.add(selectedPlaceView, selectedPlaceView.getViewName());
        return this;
    }

    public AppBuilder addMyProgressView() {
        myProgressViewModel = new MyProgressViewModel();
        myProgressView = new MyProgressView(myProgressViewModel, viewManagerModel);
        cardPanel.add(myProgressView, myProgressView.getViewName());
        return this;
    }

    /** BrowseLandmarks depends on selectedPlaceController. */
    public AppBuilder addBrowseLandmarksView() {
        browseLandmarksViewModel = new BrowseLandmarksViewModel();
        BrowseLandmarksPresenter presenter =
                new BrowseLandmarksPresenter(browseLandmarksViewModel);

        BrowseLandmarksInputBoundary interactor =
                new BrowseLandmarksInteractor(landmarkDataAccessObject, presenter, userDataAccessObject);

        browseLandmarksController = new BrowseLandmarksController(interactor);

        browseLandmarksView = new BrowseLandmarksView(
                browseLandmarksViewModel,
                browseLandmarksController,
                selectedPlaceController,     // uses same controller for “select place”
                viewManagerModel
        );

        cardPanel.add(browseLandmarksView, browseLandmarksView.getViewName());
        return this;
    }

    // === USE CASE WIRING ===

    public AppBuilder addSignupUseCase() {
        SignupOutputBoundary output =
                new SignupPresenter(viewManagerModel, signupViewModel, loginViewModel);

        SignupInputBoundary interactor =
                new SignupInteractor(userDataAccessObject, output, userFactory);

        SignupController controller = new SignupController(interactor);
        signupView.setSignupController(controller);
        return this;
    }

    public AppBuilder addHomescreenUseCase() {
        HomescreenOutputBoundary output =
                new HomescreenPresenter(homescreenViewModel,
                        viewManagerModel,
                        browseLandmarksViewModel);

        HomescreenInputBoundary interactor =
                new HomescreenInteractor(output);

        HomescreenController controller =
                new HomescreenController(interactor);

        homescreenView.setHomescreenController(controller);
        return this;
    }

    public AppBuilder addLoginUseCase() {
        LoginOutputBoundary output =
                new LoginPresenter(viewManagerModel, homescreenViewModel, loginViewModel, userDataAccessObject);

        LoginInputBoundary interactor =
                new LoginInteractor(userDataAccessObject, output);

        LoginController controller =
                new LoginController(interactor);

        loginView.setLoginController(controller);
        return this;
    }

    // NEW: Notes use case wiring (for Add Note button)
    public AppBuilder addNotesUseCase() {
        AddNotesOutputBoundary notesOutput =
                new AddNotesPresenter(notesViewModel, viewManagerModel);

        AddNotesInputBoundary notesInteractor =
                new AddNotesInteractor(userDataAccessObject, landmarkDataAccessObject, notesOutput);

        notesController = new AddNotesController(notesInteractor, notesViewModel);
        notesView.setNotesController(notesController);
        return this;
    }

    /** Wire SelectedPlace Use Case + Controller.
     *
     *  NOTE: assumes notesViewModel is already created (call addNotesView() first).
     */
    public AppBuilder addSelectedPlaceUseCase() {
        SelectedPlaceOutputBoundary spPresenter =
                new SelectedPlacePresenter(
                        selectedPlaceViewModel,
                        notesViewModel,
                        viewManagerModel,
                        userDataAccessObject
                );

        SelectedPlaceInputBoundary spInteractor =
                new SelectedPlaceInteractor(landmarkDataAccessObject, userDataAccessObject, spPresenter);

        selectedPlaceController = new SelectedPlaceController(spInteractor);
        selectedPlaceView.setSelectedPlaceController(selectedPlaceController);

        return this;
    }

    public AppBuilder addMyProgressUseCase() {
        MyProgressOutputBoundary presenter = new MyProgressPresenter(myProgressViewModel);

        MyProgressInputBoundary interactor = new MyProgressInteractor(
                userDataAccessObject,
                landmarkDataAccessObject,
                presenter
        );

        MyProgressController controller = new MyProgressController(interactor);
        myProgressView.setMyProgressController(controller);

        return this;
    }

    // === BUILD ===

    public JFrame build() {
        JFrame app = new JFrame("User Login Example");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        app.add(cardPanel);

        viewManagerModel.setState(loginView.getViewName());
        viewManagerModel.firePropertyChange();

        return app;
    }
}
