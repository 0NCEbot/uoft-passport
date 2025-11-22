// src/java/data_access/JsonLandmarkDataAccessObject.java
package data_access;

import entity.Landmark;
import entity.LandmarkInfo;
import entity.Location;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JsonLandmarkDataAccessObject implements LandmarkDataAccessInterface {

    private final String filePath;
    private final List<Landmark> landmarks;

    // ----- Google Places config (same style as your views) -----
    private static final String PLACES_API_KEY = "AIzaSyCk9bPskLw7eUI-_Y9G6tW8eDAE-iXI8Ms";
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();

    public JsonLandmarkDataAccessObject(String filePath) {
        this.filePath = filePath;
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

                String id = o.has("id") ? o.getString("id") : name.replace(" ", "_").toLowerCase();
                Location loc = new Location(lat, lng);

                LandmarkInfo info = fetchLandmarkInfoFromPlaces(name, lat, lng);

                result.add(new Landmark(id, name, loc, info, 0));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load landmarks from JSON at: " + filePath, e);
        }
        this.landmarks = result;
    }

    @Override
    public List<Landmark> getLandmarks() {
        return landmarks;
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

    // =====================================================================
    //                       Google Places helpers
    // =====================================================================

    private LandmarkInfo fetchLandmarkInfoFromPlaces(String name,
                                                     double lat,
                                                     double lng) {
        try {
            String url = "https://places.googleapis.com/v1/places:searchText";

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("textQuery", name);
            bodyJson.put("maxResultCount", 1);

            JSONObject center = new JSONObject()
                    .put("latitude", lat)
                    .put("longitude", lng);
            JSONObject circle = new JSONObject()
                    .put("center", center)
                    .put("radius", 300.0); // meters
            JSONObject locationBias = new JSONObject()
                    .put("circle", circle);
            bodyJson.put("locationBias", locationBias);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Goog-Api-Key", PLACES_API_KEY)
                    .addHeader(
                            "X-Goog-FieldMask",
                            "places.formattedAddress,places.primaryType," +
                                    "places.currentOpeningHours,places.editorialSummary"
                    )
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return defaultInfo();
                }
                String resp = response.body().string();
                JSONObject json = new JSONObject(resp);
                JSONArray places = json.optJSONArray("places");
                if (places == null || places.length() == 0) {
                    return defaultInfo();
                }

                JSONObject place = places.getJSONObject(0);

                String address = place.optString(
                        "formattedAddress",
                        "Address not available"
                );

                String type = place.optString(
                        "primaryType",
                        "unknown"
                );

                String description = "No description available";
                JSONObject editorial = place.optJSONObject("editorialSummary");
                if (editorial != null) {
                    String overview = editorial.optString("overview", null);
                    String text = editorial.optString("text", null);
                    if (overview != null && !overview.isBlank()) {
                        description = overview;
                    } else if (text != null && !text.isBlank()) {
                        description = text;
                    }
                }

                // --------- CHANGED PART: collect ALL weekday descriptions ----------
                String openHours = "No hours available";
                JSONObject currentOpening = place.optJSONObject("currentOpeningHours");
                if (currentOpening != null) {
                    JSONArray weekdayDescriptions = currentOpening.optJSONArray("weekdayDescriptions");
                    if (weekdayDescriptions != null && weekdayDescriptions.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < weekdayDescriptions.length(); i++) {
                            sb.append(weekdayDescriptions.getString(i));

                            // add newline between days (but not after the last one)
                            if (i < weekdayDescriptions.length() - 1) {
                                sb.append("\n");
                            }
                        }
                        openHours = sb.toString();
                    }
                }

                // --------------------------------------------------------------------

                return new LandmarkInfo(address, description, openHours, type);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return defaultInfo();
        }
    }

    private LandmarkInfo defaultInfo() {
        return new LandmarkInfo(
                "Address not available",
                "No description available",
                "No hours available",
                "unknown"
        );
    }
}
