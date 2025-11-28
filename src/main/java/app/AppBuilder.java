package app;

import data_access.*;
import data_access.JsonUserDataAccessObject;
import data_access.JsonLandmarkDataAccessObject;
import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
import data_access.MapsRouteDataAccessObject;
import data_access.RouteDataAccessInterface;

import entity.UserFactory;
import interface_adapter.ViewManagerModel;
import interface_adapter.browselandmarks.BrowseLandmarksController;
import interface_adapter.browselandmarks.BrowseLandmarksPresenter;
import interface_adapter.browselandmarks.BrowseLandmarksViewModel;
import interface_adapter.homescreen.HomescreenController;
import interface_adapter.homescreen.HomescreenPresenter;
import interface_adapter.homescreen.HomescreenViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.logout.LogoutController;
import interface_adapter.logout.LogoutPresenter;
import interface_adapter.myprogress.*;
import interface_adapter.selectedplace.SelectedPlaceController;
import interface_adapter.selectedplace.SelectedPlacePresenter;
import interface_adapter.selectedplace.SelectedPlaceViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
// NEW imports for Notes
import interface_adapter.addnotes.AddNotesController;
import interface_adapter.addnotes.AddNotesPresenter;
import interface_adapter.addnotes.AddNotesViewModel;
// imports for Plan Route
import interface_adapter.planroute.PlanRouteController;
import interface_adapter.planroute.PlanRoutePresenter;
import interface_adapter.planroute.PlanRouteViewModel;

// NEW imports for View History
import interface_adapter.viewhistory.ViewHistoryController;
import interface_adapter.viewhistory.ViewHistoryPresenter;
import interface_adapter.viewhistory.ViewHistoryViewModel;

import use_case.browselandmarks.BrowseLandmarksInputBoundary;
import use_case.browselandmarks.BrowseLandmarksInteractor;
import use_case.homescreen.HomescreenInputBoundary;
import use_case.homescreen.HomescreenInteractor;
import use_case.homescreen.HomescreenOutputBoundary;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.logout.LogoutInputBoundary;
import use_case.logout.LogoutInteractor;
import use_case.logout.LogoutOutputBoundary;
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
// imports for Plan Route
import use_case.planroute.PlanRouteInputBoundary;
import use_case.planroute.PlanRouteInteractor;
import use_case.planroute.PlanRouteOutputBoundary;
// NEW imports for View History use case
import use_case.viewhistory.ViewHistoryInputBoundary;
import use_case.viewhistory.ViewHistoryInteractor;
import use_case.viewhistory.ViewHistoryOutputBoundary;
// NEW imports for Undo Visit use case
import use_case.undovisit.UndoVisitInputBoundary;
import use_case.undovisit.UndoVisitInteractor;
import use_case.undovisit.UndoVisitOutputBoundary;

// imports for Edit & Delete Notes use cases
import interface_adapter.editnote.*;
import interface_adapter.deletenote.*;
import use_case.editnote.*;
import use_case.deletenote.*;


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

    // NEW: view history VM + view
    private ViewHistoryViewModel viewHistoryViewModel;
    private ViewHistoryView viewHistoryView;

    // ---- use case controllers ----
    private SelectedPlaceController selectedPlaceController;
    private BrowseLandmarksController browseLandmarksController;
    // NEW: notes controller
    private AddNotesController notesController;
    // NEW: view history controller
    private ViewHistoryController viewHistoryController;

    private LogoutController logoutController;

    private PlanRouteViewModel planRouteViewModel;
    private PlanRouteView planRouteView;
    private PlanRouteController planRouteController;

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
        notesView = new AddNotesView(
                notesViewModel,
                viewManagerModel,
                editNoteViewModel,      // ADD
                editNoteController,     // ADD
                deleteNoteViewModel,    // ADD
                deleteNoteController    // ADD
        );
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

    public AppBuilder addPlanRouteView() {
        planRouteViewModel = new PlanRouteViewModel();
        planRouteView = new PlanRouteView(planRouteViewModel, viewManagerModel, userDataAccessObject);
        cardPanel.add(planRouteView, planRouteView.getViewName());
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
        HomescreenOutputBoundary presenter =
                new HomescreenPresenter(homescreenViewModel, viewManagerModel, browseLandmarksViewModel);

        HomescreenInputBoundary interactor =
                new HomescreenInteractor(presenter);

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
    public AppBuilder addPlanRouteUseCase() {
        RouteDataAccessInterface routeDAO =
                new MapsRouteDataAccessObject(landmarkDataAccessObject);

        PlanRouteOutputBoundary presenter =
                new PlanRoutePresenter(planRouteViewModel, viewManagerModel);

        PlanRouteInputBoundary interactor =
                new PlanRouteInteractor(routeDAO, landmarkDataAccessObject, presenter);

        // Pass both userDataAccessObject and landmarkDataAccessObject
        planRouteController = new PlanRouteController(
                interactor,
                planRouteViewModel,
                userDataAccessObject,
                landmarkDataAccessObject
        );

        planRouteView.setPlanRouteController(planRouteController);

        return this;
    }
    /**
     * Adds the View History view and wires the use cases.
     * This method creates and configures the view history screen with both
     * view history and undo visit functionality.
     */
    public AppBuilder addViewHistoryView() {
        viewHistoryViewModel = new ViewHistoryViewModel();
        viewHistoryView = new ViewHistoryView(viewHistoryViewModel, viewManagerModel);

        // Create presenter that handles both use cases
        ViewHistoryPresenter presenter = new ViewHistoryPresenter(
                viewHistoryViewModel,
                viewManagerModel
        );

        // Wire View History use case
        ViewHistoryInputBoundary viewHistoryInteractor =
                new ViewHistoryInteractor(userDataAccessObject, presenter);

        // Wire Undo Visit use case
        UndoVisitInputBoundary undoVisitInteractor =
                new UndoVisitInteractor(userDataAccessObject, presenter);

        // Create controller with both interactors
        viewHistoryController = new ViewHistoryController(
                viewHistoryInteractor,
                undoVisitInteractor
        );

        viewHistoryView.setController(viewHistoryController);
        cardPanel.add(viewHistoryView, viewHistoryView.getViewName());

        return this;
    }

    public AppBuilder addMyProgressUseCase() {
        MyProgressOutputBoundary presenter = new MyProgressPresenter(myProgressViewModel, viewManagerModel);

        MyProgressInputBoundary interactor = new MyProgressInteractor(
                userDataAccessObject,
                landmarkDataAccessObject,
                presenter
        );

        MyProgressController controller = new MyProgressController(interactor);
        myProgressView.setMyProgressController(controller);
        return this;
    }

    public AppBuilder addLogoutUseCase() {
        LogoutOutputBoundary logoutPresenter =
                new LogoutPresenter(viewManagerModel, loginViewModel);

        LogoutInputBoundary logoutInteractor =
                new LogoutInteractor(userDataAccessObject, logoutPresenter);

        logoutController = new LogoutController(logoutInteractor);

        // Add null checks and debug logging
        if (homescreenView != null) {
            homescreenView.setLogoutController(logoutController);
            System.out.println("Set logout controller on HomescreenView");
        } else {
            System.err.println("WARNING: homescreenView is null!");
        }

        if (browseLandmarksView != null) {
            browseLandmarksView.setLogoutController(logoutController);
            System.out.println("Set logout controller on BrowseLandmarksView");
        } else {
            System.err.println("WARNING: browseLandmarksView is null!");
        }

        if (selectedPlaceView != null) {
            selectedPlaceView.setLogoutController(logoutController);
            System.out.println("Set logout controller on SelectedPlaceView");
        } else {
            System.err.println("WARNING: selectedPlaceView is null!");
        }

        if (notesView != null) {
            notesView.setLogoutController(logoutController);
            System.out.println("Set logout controller on AddNotesView");
        } else {
            System.err.println("WARNING: notesView is null!");
        }

        if (myProgressView != null) {
            myProgressView.setLogoutController(logoutController);
            System.out.println("Set logout controller on MyProgressView");
        } else {
            System.err.println("WARNING: myProgressView is null!");
        }

        if (viewHistoryView != null) {
            viewHistoryView.setLogoutController(logoutController);
            System.out.println("Set logout controller on ViewHistoryView");
        } else {
            System.err.println("WARNING: viewHistoryView is null!");
        }

        return this;
    }

    private EditNoteViewModel editNoteViewModel;
    private DeleteNoteViewModel deleteNoteViewModel;
    private EditNoteController editNoteController;
    private DeleteNoteController deleteNoteController;

    public AppBuilder addEditDeleteNotesSetup() {
        // Create ViewModels
        editNoteViewModel = new EditNoteViewModel();
        deleteNoteViewModel = new DeleteNoteViewModel();

        // Create Presenters
        EditNotePresenter editNotePresenter = new EditNotePresenter(
                editNoteViewModel,
                viewManagerModel
        );

        DeleteNotePresenter deleteNotePresenter = new DeleteNotePresenter(
                deleteNoteViewModel,
                viewManagerModel
        );

        // Create Interactors (cast to the interface types)
        EditNoteInteractor editNoteInteractor = new EditNoteInteractor(
                (EditNoteDataAccessInterface) userDataAccessObject,
                editNotePresenter
        );

        DeleteNoteInteractor deleteNoteInteractor = new DeleteNoteInteractor(
                (DeleteNoteDataAccessInterface) userDataAccessObject,
                deleteNotePresenter
        );

        // Create Controllers
        editNoteController = new EditNoteController(editNoteInteractor);
        deleteNoteController = new DeleteNoteController(deleteNoteInteractor);

        return this;
    }

    public JFrame build() {
        JFrame app = new JFrame("UofT Passport");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        app.add(cardPanel);

        viewManagerModel.setState(loginView.getViewName());
        viewManagerModel.firePropertyChange();

        return app;
    }
}
