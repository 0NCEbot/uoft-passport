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
                .addHomescreenView()
                .addHomescreenUseCase()
                .addLoginView()
                .addLoginUseCase()
                .addSignupView()
                .addSignupUseCase()
                .build();

        application.pack();
        application.setSize(800, 600);
        application.setVisible(true);
    }
}
