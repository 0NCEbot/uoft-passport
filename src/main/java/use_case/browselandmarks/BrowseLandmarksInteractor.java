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

        List<BrowseLandmarksOutputData.LandmarkDTO> dtos = landmarks.stream()
                .map(l -> new BrowseLandmarksOutputData.LandmarkDTO(
                        l.getLandmarkName(), l.getLocation().getLatitude(), l.getLocation().getLongitude()))
                .collect(Collectors.toList());

        presenter.presentLandmarks(new BrowseLandmarksOutputData(dtos));
    }
}
