// src/java/use_case/browselandmarks/BrowseLandmarksInteractor.java
package use_case.browselandmarks;

import data_access.LandmarkDataAccessInterface;
import entity.Landmark;
import use_case.login.LoginUserDataAccessInterface;

import java.util.List;
import java.util.stream.Collectors;

public class BrowseLandmarksInteractor implements BrowseLandmarksInputBoundary {

    private final LandmarkDataAccessInterface landmarkDAO;
    private final BrowseLandmarksOutputBoundary presenter;
    private final LoginUserDataAccessInterface loginUserDAO;

    public BrowseLandmarksInteractor(LandmarkDataAccessInterface landmarkDAO,
                                     BrowseLandmarksOutputBoundary presenter,
                                     LoginUserDataAccessInterface  loginUserDAO) {
        this.landmarkDAO = landmarkDAO;
        this.presenter = presenter;
        this.loginUserDAO = loginUserDAO;
    }

    @Override
    public void loadLandmarks() {
        List<Landmark> landmarks = landmarkDAO.getLandmarks();

        // Get current user and their visits
        String currentUsername = loginUserDAO.getCurrentUsername();
        entity.User currentUser = null;
        if (currentUsername != null && loginUserDAO.existsByName(currentUsername)) {
            currentUser = loginUserDAO.get(currentUsername);
        }

        // Calculate visit counts for each landmark
        final entity.User user = currentUser;
        List<BrowseLandmarksOutputData.LandmarkDTO> dtos = landmarks.stream()
                .map(l -> {
                    int visitCount = 0;
                    if (user != null && user.getVisits() != null) {
                        visitCount = (int) user.getVisits().stream()
                                .filter(visit -> visit.getLandmark().getLandmarkName().equals(l.getLandmarkName()))
                                .count();
                    }

                    String type = "Campus Location";
                    if (l.getLandmarkInfo() != null && l.getLandmarkInfo().getType() != null) {
                        type = l.getLandmarkInfo().getType();
                    }

                    return new BrowseLandmarksOutputData.LandmarkDTO(
                            l.getLandmarkName(),
                            l.getLocation().getLatitude(),
                            l.getLocation().getLongitude(),
                            type,
                            visitCount);
                })
                .collect(Collectors.toList());

        presenter.presentLandmarks(new BrowseLandmarksOutputData(dtos));
    }
}
