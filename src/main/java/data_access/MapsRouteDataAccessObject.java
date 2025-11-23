package data_access;

import entity.Location;
import entity.RouteStep;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsRouteDataAccessObject implements RouteDataAccessInterface {

    private static final String API_KEY = "AIzaSyAJi30DYnkCZjnXRYpzWa3L1aToUbHDz2Q";
    private static final String ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    private static final String STATIC_MAP_URL = "https://maps.googleapis.com/maps/api/staticmap";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient();
    private final LandmarkDataAccessInterface landmarkDAO;

    public MapsRouteDataAccessObject(LandmarkDataAccessInterface landmarkDAO) {
        this.landmarkDAO = landmarkDAO;
    }

    @Override
    public RouteResponse getRoute(String startLocationName, String destinationName, String[] intermediateStopNames) {
        try {
            Map<String, String> nameResolutions = new HashMap<>();

            Location startLocation = resolveLandmarkToLocation(startLocationName, nameResolutions);
            Location destinationLocation = resolveLandmarkToLocation(destinationName, nameResolutions);

            if (startLocation == null) {
                return new RouteResponse(new ArrayList<>(), 0, 0, false,
                        "<html>Start location '" + startLocationName + "' not found.<br>Please enter a valid landmark name.</html>");
            }
            if (destinationLocation == null) {
                return new RouteResponse(new ArrayList<>(), 0, 0, false,
                        "<html>Destination '" + destinationName + "' not found.<br>Please enter a valid landmark name.</html>");
            }

            String resolvedStartName = nameResolutions.get(startLocationName);
            String resolvedDestName = nameResolutions.get(destinationName);

            List<String> waypointNames = new ArrayList<>();
            List<Location> waypointLocations = new ArrayList<>();

            if (intermediateStopNames != null) {
                for (String intermediateName : intermediateStopNames) {
                    Location loc = resolveLandmarkToLocation(intermediateName, nameResolutions);
                    if (loc != null) {
                        waypointNames.add(nameResolutions.get(intermediateName));
                        waypointLocations.add(loc);
                    } else {
                        return new RouteResponse(new ArrayList<>(), 0, 0, false,
                                "<html>Intermediate stop '" + intermediateName + "' not found.<br>Please enter a valid landmark name.</html>");
                    }
                }
            }

            String requestBody = buildRouteRequest(startLocation, destinationLocation,
                    waypointLocations.toArray(new Location[0]));

            JSONObject responseJson = callRoutesAPI(requestBody);
            if (responseJson == null) {
                return handleManualMode(resolvedStartName, resolvedDestName, waypointNames,
                        startLocation, destinationLocation, waypointLocations);
            }

            List<RouteStep> steps = extractStepsWithLandmarks(responseJson, resolvedStartName,
                    resolvedDestName, waypointNames);

            JSONObject route = responseJson.getJSONArray("routes").getJSONObject(0);
            int totalDistance = route.getInt("distanceMeters");
            String durationStr = route.getString("duration");
            int totalDuration = parseDurationToSeconds(durationStr);

            // Extract polyline
            String encodedPolyline = null;
            if (route.has("polyline")) {
                JSONObject polyline = route.getJSONObject("polyline");
                encodedPolyline = polyline.optString("encodedPolyline", null);
            }

            return new RouteResponse(steps, totalDistance, totalDuration, true, null, false,
                    encodedPolyline, startLocation, destinationLocation, waypointLocations);

        } catch (Exception e) {
            e.printStackTrace();
            return new RouteResponse(new ArrayList<>(), 0, 0, false,
                    "An error occurred while planning the route. Please try again.");
        }
    }

    @Override
    public byte[] getStaticMapImage(String encodedPolyline, Location start, Location end, List<Location> intermediates) {
        try {
            StringBuilder urlBuilder = new StringBuilder(STATIC_MAP_URL);
            urlBuilder.append("?size=400x350");
            urlBuilder.append("&maptype=roadmap");

            // Add polyline path if available
            if (encodedPolyline != null && !encodedPolyline.isEmpty()) {
                urlBuilder.append("&path=weight:4|color:0x4285F4|enc:")
                        .append(URLEncoder.encode(encodedPolyline, StandardCharsets.UTF_8));
            }

            // Start marker (green)
            if (start != null) {
                urlBuilder.append("&markers=color:green|label:S|")
                        .append(start.getLatitude()).append(",").append(start.getLongitude());
            }

            // End marker (red)
            if (end != null) {
                urlBuilder.append("&markers=color:red|label:E|")
                        .append(end.getLatitude()).append(",").append(end.getLongitude());
            }

            // Intermediate markers (blue, numbered)
            if (intermediates != null) {
                int index = 1;
                for (Location loc : intermediates) {
                    urlBuilder.append("&markers=color:blue|label:").append(index++).append("|")
                            .append(loc.getLatitude()).append(",").append(loc.getLongitude());
                }
            }

            urlBuilder.append("&key=").append(API_KEY);

            Request request = new Request.Builder()
                    .url(urlBuilder.toString())
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().bytes();
                }
            }
        } catch (Exception e) {
            System.err.println("[ROUTE DAO] Failed to fetch static map: " + e.getMessage());
        }
        return null;
    }

    private Location resolveLandmarkToLocation(String landmarkName, Map<String, String> nameResolutions) {
        if (landmarkName == null || landmarkName.isBlank()) return null;

        for (var landmark : landmarkDAO.getLandmarks()) {
            if (landmark.getLandmarkName().equalsIgnoreCase(landmarkName.trim())) {
                nameResolutions.put(landmarkName, landmark.getLandmarkName());
                return landmark.getLocation();
            }
        }

        String lowerQuery = landmarkName.toLowerCase().trim();
        for (var landmark : landmarkDAO.getLandmarks()) {
            if (landmark.getLandmarkName().toLowerCase().contains(lowerQuery)) {
                nameResolutions.put(landmarkName, landmark.getLandmarkName());
                return landmark.getLocation();
            }
        }
        return null;
    }

    private String buildRouteRequest(Location start, Location destination, Location[] intermediates) {
        JSONObject request = new JSONObject();

        JSONObject origin = new JSONObject();
        origin.put("location", new JSONObject().put("latLng", createLatLngJson(start)));
        request.put("origin", origin);

        JSONObject dest = new JSONObject();
        dest.put("location", new JSONObject().put("latLng", createLatLngJson(destination)));
        request.put("destination", dest);

        if (intermediates != null && intermediates.length > 0) {
            JSONArray waypoints = new JSONArray();
            for (Location loc : intermediates) {
                if (loc != null) {
                    JSONObject wp = new JSONObject();
                    wp.put("location", new JSONObject().put("latLng", createLatLngJson(loc)));
                    waypoints.put(wp);
                }
            }
            if (waypoints.length() > 0) request.put("intermediates", waypoints);
        }

        request.put("travelMode", "WALK");
        return request.toString();
    }

    private JSONObject createLatLngJson(Location location) {
        JSONObject latLng = new JSONObject();
        latLng.put("latitude", location.getLatitude());
        latLng.put("longitude", location.getLongitude());
        return latLng;
    }

    private JSONObject callRoutesAPI(String requestBody) throws IOException {
        RequestBody body = RequestBody.create(requestBody, JSON);

        Request request = new Request.Builder()
                .url(ROUTES_API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", API_KEY)
                .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs.steps")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            return new JSONObject(response.body().string());
        }
    }

    private RouteResponse handleManualMode(String startName, String destName, List<String> intermediateNames,
                                           Location startLoc, Location destLoc, List<Location> intermediateLocs) {
        List<RouteStep> manualSteps = new ArrayList<>();
        int stepIndex = 0;

        manualSteps.add(new RouteStep(stepIndex++, "📍 " + startName, 0, 0));
        for (String name : intermediateNames) {
            manualSteps.add(new RouteStep(stepIndex++, "Navigate to: " + name, 0, 0));
            manualSteps.add(new RouteStep(stepIndex++, "📍 " + name, 0, 0));
        }
        manualSteps.add(new RouteStep(stepIndex++, "Navigate to: " + destName, 0, 0));
        manualSteps.add(new RouteStep(stepIndex, "📍 " + destName, 0, 0));

        return new RouteResponse(manualSteps, 0, 0, true,
                "API unavailable. Using self-guided mode.", true, null,
                startLoc, destLoc, intermediateLocs);
    }

    private List<RouteStep> extractStepsWithLandmarks(JSONObject responseJson, String startName,
                                                      String destName, List<String> intermediateNames) {
        List<RouteStep> steps = new ArrayList<>();
        try {
            JSONArray routes = responseJson.optJSONArray("routes");
            if (routes == null || routes.length() == 0) return steps;

            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.optJSONArray("legs");
            if (legs == null) return steps;

            int stepIndex = 0;
            steps.add(new RouteStep(stepIndex++, "📍 " + startName, 0, 0));

            for (int legIdx = 0; legIdx < legs.length(); legIdx++) {
                JSONArray stepsArray = legs.getJSONObject(legIdx).optJSONArray("steps");
                if (stepsArray != null) {
                    for (int i = 0; i < stepsArray.length(); i++) {
                        JSONObject stepJson = stepsArray.getJSONObject(i);
                        steps.add(new RouteStep(stepIndex++, extractInstruction(stepJson),
                                stepJson.optInt("distanceMeters", 0), extractDurationSeconds(stepJson)));
                    }
                }
                if (legIdx < intermediateNames.size()) {
                    steps.add(new RouteStep(stepIndex++, "📍 " + intermediateNames.get(legIdx), 0, 0));
                }
            }
            steps.add(new RouteStep(stepIndex, "📍 " + destName, 0, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return steps;
    }

    private String extractInstruction(JSONObject stepJson) {
        JSONObject nav = stepJson.optJSONObject("navigationInstruction");
        if (nav != null) {
            String instr = nav.optString("instructions", "");
            if (!instr.isEmpty()) return instr.replace("\n", " - ");
        }
        JSONObject lv = stepJson.optJSONObject("localizedValues");
        if (lv != null) {
            JSONObject dist = lv.optJSONObject("distance");
            if (dist != null) {
                String text = dist.optString("text", "");
                if (!text.isEmpty()) return "Walk " + text;
            }
        }
        return "Continue";
    }

    private int extractDurationSeconds(JSONObject stepJson) {
        String dur = stepJson.optString("staticDuration", "");
        if (!dur.isEmpty()) return parseDurationToSeconds(dur);
        return 0;
    }

    private int parseDurationToSeconds(String durationStr) {
        if (durationStr == null || durationStr.isBlank()) return 0;
        durationStr = durationStr.trim().toLowerCase();
        try {
            if (durationStr.endsWith("s")) {
                return Integer.parseInt(durationStr.substring(0, durationStr.length() - 1));
            }
            if (durationStr.contains("min")) {
                return Integer.parseInt(durationStr.split("\\s+")[0]) * 60;
            }
            if (durationStr.contains("hour")) {
                return Integer.parseInt(durationStr.split("\\s+")[0]) * 3600;
            }
        } catch (Exception ignored) {}
        return 0;
    }
}