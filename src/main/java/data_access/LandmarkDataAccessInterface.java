// src/java/data_access/LandmarkDataAccessInterface.java
package data_access;

import entity.Landmark;

import java.util.List;

public interface LandmarkDataAccessInterface {

    /**
     * Get a list of landmarks.
     * @return a list of Landmark objects
     */
    List<Landmark> getLandmarks();

    /**
     * Check if the landmark exists.
     * @param landmarkName the landmark name
     * @return True if the landmark associated with the name exists, false otherwise
     */
    boolean existsByName(String landmarkName);
    Landmark findByName(String name);
}
