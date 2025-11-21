package data_access;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Landmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO for landmark data stored in JSON.
 * This class is read only, meaning that landmark cannot be written into json
 */
public class JSONFileLandmarkDataAccessObject implements LandmarkDataAccessInterface {
    private JsonNode root;

    private final ObjectMapper mapper = new ObjectMapper();
    private final File file;
    private final Map<String, Landmark> landmarks = new HashMap<>();

    public JSONFileLandmarkDataAccessObject(String jsonFilePath) {
        this.file = new File(jsonFilePath);

        if (!file.exists()) {
            throw new RuntimeException("JSON file does not exist: " + jsonFilePath);
        }

        if (file.length() == 0) {
            this.root = mapper.createArrayNode();
            return;
        }

        try {
            this.root = mapper.readTree(file);

            if (!root.isArray()) {
                throw new RuntimeException("Expected root JSON array");
            }

            for (JsonNode node : root) {
                Landmark landmark = mapper.treeToValue(node, Landmark.class);
                landmarks.put(landmark.getLandmarkName(), landmark);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file", e);
        }
    }

    @Override
    public boolean existsByName(String username) {
        return landmarks.containsKey(username);
    }

    @Override
    public Map<String, Landmark> getLandmarks() {
        return landmarks;
    }
}
