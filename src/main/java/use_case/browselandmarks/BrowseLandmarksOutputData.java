// src/java/use_case/browselandmarks/BrowseLandmarksOutputData.java
package use_case.browselandmarks;

import java.util.List;

public class BrowseLandmarksOutputData {

    public static class LandmarkDTO {
        public final String name;
        public final double latitude;
        public final double longitude;
        public final String type;
        public final int visitCount;

        public LandmarkDTO(String name, double latitude, double longitude, String type, int visitCount) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
            this.visitCount = visitCount;
        }
    }

    private final List<LandmarkDTO> landmarks;

    public BrowseLandmarksOutputData(List<LandmarkDTO> landmarks) {
        this.landmarks = landmarks;
    }

    public List<LandmarkDTO> getLandmarks() {
        return landmarks;
    }
}
