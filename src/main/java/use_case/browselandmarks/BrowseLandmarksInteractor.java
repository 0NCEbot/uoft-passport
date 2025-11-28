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
        System.out.println("===== BrowseLandmarksInteractor Debug =====");
        System.out.println("Current username from DAO: " + currentUsername);

        entity.User currentUser = null;
        if (currentUsername != null && loginUserDAO.existsByName(currentUsername)) {
            currentUser = loginUserDAO.get(currentUsername);
            System.out.println("User found: " + currentUser.getUsername());
            System.out.println("Total visits in user object: " + (currentUser.getVisits() != null ? currentUser.getVisits().size() : "null"));

            if (currentUser.getVisits() != null && !currentUser.getVisits().isEmpty()) {
                System.out.println("First few visit landmark names:");
                currentUser.getVisits().stream()
                    .limit(5)
                    .forEach(v -> System.out.println("  - " + v.getLandmark().getLandmarkName()));
            }
        } else {
            System.out.println("User NOT found or username is null");
        }

        // Calculate visit counts for each landmark
        final entity.User user = currentUser;

        // DEBUG: Print all visit landmark names first
        if (user != null && user.getVisits() != null) {
            System.out.println("\n*** All visit landmark names from user object: ***");
            user.getVisits().stream()
                .filter(v -> v != null && v.getLandmark() != null)
                .map(v -> v.getLandmark().getLandmarkName())
                .distinct()
                .sorted()
                .forEach(name -> System.out.println("  Visit: '" + name + "'"));
            System.out.println("*** End of visit names ***\n");
        }

        List<BrowseLandmarksOutputData.LandmarkDTO> dtos = landmarks.stream()
                .map(l -> {
                    int visitCount = 0;
                    if (user != null && user.getVisits() != null) {
                        String landmarkName = l.getLandmarkName().trim();
                        visitCount = (int) user.getVisits().stream()
                                .filter(visit -> {
                                    if (visit == null || visit.getLandmark() == null) {
                                        return false;
                                    }
                                    String visitLandmarkName = visit.getLandmark().getLandmarkName();
                                    if (visitLandmarkName == null) {
                                        return false;
                                    }
                                    boolean matches = visitLandmarkName.trim().equals(landmarkName);
                                    if (matches) {
                                        System.out.println("MATCH: '" + landmarkName + "' = '" + visitLandmarkName + "'");
                                    }
                                    return matches;
                                })
                                .count();
                    }

                    String type = "Campus Location";
                    if (l.getLandmarkInfo() != null && l.getLandmarkInfo().getType() != null) {
                        type = l.getLandmarkInfo().getType();
                    }

                    if (visitCount > 0) {
                        System.out.println("Landmark with visits: " + l.getLandmarkName() + " = " + visitCount);
                    }

                    return new BrowseLandmarksOutputData.LandmarkDTO(
                            l.getLandmarkName(),
                            l.getLocation().getLatitude(),
                            l.getLocation().getLongitude(),
                            type,
                            visitCount);
                })
                .collect(Collectors.toList());

        System.out.println("Total landmarks with >0 visits: " + dtos.stream().filter(d -> d.visitCount > 0).count());
        System.out.println("==========================================");

        presenter.presentLandmarks(new BrowseLandmarksOutputData(dtos));
    }
}
