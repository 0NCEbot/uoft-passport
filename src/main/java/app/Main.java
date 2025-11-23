package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addNotesView()
                .addNotesUseCase()
                .addSelectedPlaceView()
                .addSelectedPlaceUseCase()
                .addBrowseLandmarksView()
                .addMyProgressView()
                .addMyProgressUseCase()
                .addViewHistoryView()
                .addViewProgressView()  // Add View Progress view
                .addHomescreenView()
                .addHomescreenUseCase()
                .addLoginView()
                .addLoginUseCase()
                .addSignupView()
                .addSignupUseCase()
                .addHomescreenUseCase()
                .addPlanRouteView()
                .addPlanRouteUseCase()
                .build();

        application.pack();
        application.setSize(800, 600);
        application.setVisible(true);
    }
}
