package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addEditDeleteNotesSetup()
                .addNotesView()
                .addNotesUseCase()
                .addSelectedPlaceView()
                .addSelectedPlaceUseCase()
                .addBrowseLandmarksView()
                .addViewHistoryView()
                .addMyProgressView()
                .addMyProgressUseCase()
                .addHomescreenView()
                .addLoginView()
                .addSignupView()
                .addLoginUseCase()
                .addSignupUseCase()
                .addHomescreenUseCase()
                .addPlanRouteView()
                .addPlanRouteUseCase()
                .addLogoutUseCase()
                .build();

        application.pack();
        application.setSize(800, 600);
        application.setVisible(true);
    }
}
