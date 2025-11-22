package data_access;

import entity.RouteStep;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsRouteDataAccessObject implements RouteDataAccessInterface {

    private static final String ROUTES_API_KEY = "YOUR_API_KEY";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public RouteResponse getRoute(String start, String destination, String[] intermediates) {
        try {
            String url = "https://routes.googleapis.com/directions/v2:computeRoutes";

            JSONObject waypoint1 = new JSONObject()
                    .put("address", start);

            JSONObject waypointEnd = new JSONObject()
                    .put("address", destination);

            JSONArray waypoints = new JSONArray();
            waypoints.put(waypoint1);

            for (String intermediate : intermediates) {
                waypoints.put(new JSONObject().put("address", intermediate));
            }

            waypoints.put(waypointEnd);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("waypoints", waypoints);
            bodyJson.put("travelMode", "WALK");
            bodyJson.put("routingPreference", "TRAFFIC_AWARE");

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("X-Goog-Api-Key", ROUTES_API_KEY)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;

                JSONObject json = new JSONObject(response.body().string());
                JSONArray routes = json.optJSONArray("routes");

                if (routes == null || routes.length() == 0) return null;

                JSONObject route = routes.getJSONObject(0);
                List<RouteStep> steps = extractSteps(route);

                // Simplified totals (you'd calculate from steps)
                int totalDist = 5000; // TODO: sum from steps
                int totalDuration = 3600;

                return new RouteResponse(steps, totalDist, totalDuration, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<RouteStep> extractSteps(JSONObject route) {
        List<RouteStep> steps = new ArrayList<>();
        // TODO: Parse route JSON into RouteStep entities
        return steps;
    }
}