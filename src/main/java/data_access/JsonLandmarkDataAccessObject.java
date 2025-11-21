// src/java/data_access/JsonLandmarkDataAccessObject.java
package data_access;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JsonLandmarkDataAccessObject implements LandmarkDataAccessInterface {

    private final String filePath;   // absolute or relative path

    public JsonLandmarkDataAccessObject(String filePath) {
        this.filePath = filePath;   // e.g. "minimal_landmarks.json"
    }

    @Override
    public List<Landmark> getLandmarks() {
        List<Landmark> result = new ArrayList<>();

        try (InputStream is = new FileInputStream(filePath)) {

            String jsonText;
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                jsonText = scanner.useDelimiter("\\A").next();
            }

            JSONArray arr = new JSONArray(jsonText);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                String name = o.getString("name");
                double lat = o.getDouble("latitude");
                double lng = o.getDouble("longitude");
                // optional id, fallback if missing
                String id = o.has("id") ? o.getString("id") : name.replace(" ", "_").toLowerCase();
                Location loc = new Location(lat, lng);
                // Your constructor requires LandmarkInfo so create a dummy one
                LandmarkInfo info = new LandmarkInfo(" ", " ", " ", " ");

                // Your Landmark constructor: (id, name, Location, LandmarkInfo, visitCount)
                result.add(new Landmark(id, name, loc, info, 0));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load landmarks from JSON at: " + filePath, e);
        }

        return result;
    }

    @Override
    public boolean existsByName(String landmarkName) {
        if (landmarkName == null) {
            return false;
        }

        for (Landmark lm : getLandmarks()) {
            if (lm.getLandmarkName().equalsIgnoreCase(landmarkName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Landmark findByName(String name) {
        for (Landmark lm : getLandmarks()) {
            if (lm.getLandmarkName().equalsIgnoreCase(name)) {
                return lm;
            }
        }
        throw new RuntimeException("Landmark not found: " + name);
    }

}
