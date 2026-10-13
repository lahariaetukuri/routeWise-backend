
package com.routewise.routewise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutingService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RoutingService() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        objectMapper = new ObjectMapper();
    }

    /*
     * =====================================================
     * GET ROUTE
     * =====================================================
     */

    public Map<String, Object> getRoute(
            String source,
            String destination) throws Exception {

        Map<String, Double> sourceCoordinates = geocode(source);
        Map<String, Double> destinationCoordinates = geocode(destination);

        if (sourceCoordinates == null) {
            throw new RuntimeException(
                    "Unable to find source: " + source
            );
        }

        if (destinationCoordinates == null) {
            throw new RuntimeException(
                    "Unable to find destination: " + destination
            );
        }

        double sourceLat = sourceCoordinates.get("lat");
        double sourceLon = sourceCoordinates.get("lon");

        double destinationLat = destinationCoordinates.get("lat");
        double destinationLon = destinationCoordinates.get("lon");

        // OSRM requires longitude,latitude coordinate order.
        String coordinates =
                sourceLon + "," + sourceLat
                        + ";" + destinationLon + "," + destinationLat;

        String url =
                "https://router.project-osrm.org/route/v1/driving/"
                        + coordinates
                        + "?overview=full"
                        + "&geometries=geojson"
                        + "&steps=true"
                        + "&alternatives=true";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .header(
                        "User-Agent",
                        "RouteWise-College-Project/1.0"
                )
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Routing service returned status: "
                            + response.statusCode()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        String code = root.path("code").asText();

        if (!"Ok".equals(code)) {
            throw new RuntimeException(
                    "Unable to calculate route: "
                            + root.path("message").asText(code)
            );
        }

        JsonNode routes = root.path("routes");

        if (!routes.isArray() || routes.isEmpty()) {
            throw new RuntimeException("No routes found");
        }

        /*
         * MAIN ROUTE
         */

        JsonNode mainRoute = routes.get(0);

        double distance = mainRoute.path("distance").asDouble();
        double duration = mainRoute.path("duration").asDouble();

        double distanceKm =
                Math.round((distance / 1000.0) * 100) / 100.0;

        int durationMinutes =
                (int) Math.round(duration / 60.0);

        List<Map<String, Object>> directions =
                extractDirections(mainRoute);

        List<List<Double>> geometry =
                extractGeometry(mainRoute);

        /*
         * RESULT
         */

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("source", source);
        result.put("destination", destination);

        result.put("sourceLatitude", sourceLat);
        result.put("sourceLongitude", sourceLon);

        result.put("destinationLatitude", destinationLat);
        result.put("destinationLongitude", destinationLon);

        result.put("distance", distanceKm);
        result.put("estimatedTime", durationMinutes);

        result.put("directions", directions);
        result.put("geometry", geometry);

        /*
         * ALTERNATIVE ROUTES
         */

        if (routes.size() > 1) {

            List<Map<String, Object>> alternatives =
                    new ArrayList<>();

            for (int i = 1; i < routes.size(); i++) {

                JsonNode alternative = routes.get(i);

                double alternativeDistance =
                        alternative.path("distance").asDouble();

                double alternativeDuration =
                        alternative.path("duration").asDouble();

                double alternativeDistanceKm =
                        Math.round(
                                (alternativeDistance / 1000.0) * 100
                        ) / 100.0;

                int alternativeTimeMinutes =
                        (int) Math.round(
                                alternativeDuration / 60.0
                        );

                Map<String, Object> alternativeResult =
                        new LinkedHashMap<>();

                alternativeResult.put(
                        "distance",
                        alternativeDistanceKm
                );

                alternativeResult.put(
                        "estimatedTime",
                        alternativeTimeMinutes
                );

                alternativeResult.put(
                        "directions",
                        extractDirections(alternative)
                );

                alternativeResult.put(
                        "geometry",
                        extractGeometry(alternative)
                );

                alternatives.add(alternativeResult);
            }

            result.put("alternatives", alternatives);
        }

        return result;
    }

    /*
     * =====================================================
     * GEOCODING WITH DIAGNOSTIC LOGGING
     * =====================================================
     */

    private Map<String, Double> geocode(
            String location) throws Exception {

        if (location == null || location.isBlank()) {
            System.out.println(
                    "[RouteWise Geocode] Location is empty."
            );
            return null;
        }

        String cleanedLocation = location.trim();

        String[] searches = {
                cleanedLocation + ", Hyderabad, Telangana, India",
                cleanedLocation + ", Telangana, India",
                cleanedLocation + ", India",
                cleanedLocation
        };

        for (String searchLocation : searches) {

            String encodedLocation = URLEncoder.encode(
                    searchLocation,
                    StandardCharsets.UTF_8
            );

            String url =
                    "https://nominatim.openstreetmap.org/search"
                            + "?q=" + encodedLocation
                            + "&format=jsonv2"
                            + "&limit=3"
                            + "&countrycodes=in"
                            + "&accept-language=en";

            System.out.println(
                    "[RouteWise Geocode] Searching: "
                            + searchLocation
            );

            // Avoid sending rapid consecutive requests to Nominatim.
            Thread.sleep(1100);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header(
                            "User-Agent",
                            "RouteWise-College-Project/1.0"
                    )
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response;

            try {
                response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
            } catch (IOException e) {

                System.out.println(
                        "[RouteWise Geocode] Request failed: "
                                + e.getClass().getSimpleName()
                                + " - " + e.getMessage()
                );

                continue;
            }

            String body = response.body();

            System.out.println(
                    "[RouteWise Geocode] HTTP status: "
                            + response.statusCode()
                            + ", response length: "
                            + (body == null ? 0 : body.length())
            );

            if (response.statusCode() != 200) {

                System.out.println(
                        "[RouteWise Geocode] Provider returned "
                                + "a non-200 status."
                );

                continue;
            }

            if (body == null || body.isBlank()) {

                System.out.println(
                        "[RouteWise Geocode] Empty response."
                );

                continue;
            }

            JsonNode results;

            try {
                results = objectMapper.readTree(body);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {

                System.out.println(
                        "[RouteWise Geocode] Invalid JSON response: "
                                + e.getOriginalMessage()
                );

                continue;
            }

            if (results == null
                    || !results.isArray()
                    || results.isEmpty()) {

                System.out.println(
                        "[RouteWise Geocode] No matching locations returned."
                );

                continue;
            }

            for (JsonNode item : results) {

                JsonNode latitudeNode = item.get("lat");
                JsonNode longitudeNode = item.get("lon");

                if (latitudeNode == null || longitudeNode == null) {
                    continue;
                }

                try {
                    double lat = Double.parseDouble(
                            latitudeNode.asText()
                    );

                    double lon = Double.parseDouble(
                            longitudeNode.asText()
                    );

                    if (!Double.isFinite(lat)
                            || !Double.isFinite(lon)
                            || lat < -90 || lat > 90
                            || lon < -180 || lon > 180) {

                        continue;
                    }

                    Map<String, Double> coordinates =
                            new LinkedHashMap<>();

                    coordinates.put("lat", lat);
                    coordinates.put("lon", lon);

                    System.out.println(
                            "[RouteWise Geocode] SUCCESS for "
                                    + location + ": "
                                    + lat + ", " + lon
                    );

                    return coordinates;

                } catch (NumberFormatException e) {

                    System.out.println(
                            "[RouteWise Geocode] Invalid coordinates returned."
                    );
                }
            }
        }

        System.out.println(
                "[RouteWise Geocode] FAILED after all attempts for: "
                        + location
        );

        return null;
    }

    /*
     * =====================================================
     * TURN-BY-TURN DIRECTIONS
     * =====================================================
     */

    private List<Map<String, Object>> extractDirections(
            JsonNode route) {

        List<Map<String, Object>> directions =
                new ArrayList<>();

        JsonNode legs = route.path("legs");

        if (!legs.isArray()) {
            return directions;
        }

        int stepNumber = 1;

        for (JsonNode leg : legs) {

            JsonNode steps = leg.path("steps");

            if (!steps.isArray()) {
                continue;
            }

            for (JsonNode step : steps) {

                JsonNode maneuver = step.path("maneuver");

                String type = maneuver.path("type").asText(null);
                String modifier = maneuver.path("modifier").asText(null);
                String road = step.path("name").asText("");

                double distance = step.path("distance").asDouble();
                double duration = step.path("duration").asDouble();

                if (type == null) {
                    continue;
                }

                String instruction = createInstruction(
                        type,
                        modifier,
                        road
                );

                if (instruction == null) {
                    continue;
                }

                Map<String, Object> direction =
                        new LinkedHashMap<>();

                direction.put("step", stepNumber);
                direction.put("instruction", instruction);
                direction.put("road", road);

                direction.put(
                        "distance",
                        Math.round(distance * 100) / 100.0
                );

                direction.put(
                        "duration",
                        (int) Math.round(duration / 60.0)
                );

                directions.add(direction);
                stepNumber++;
            }
        }

        return directions;
    }

    /*
     * =====================================================
     * MAP GEOMETRY
     * =====================================================
     */

    private List<List<Double>> extractGeometry(
            JsonNode route) {

        List<List<Double>> coordinates =
                new ArrayList<>();

        JsonNode geometry = route.path("geometry");

        if (!geometry.isObject()) {
            return coordinates;
        }

        JsonNode coordinatesNode = geometry.path("coordinates");

        if (!coordinatesNode.isArray()) {
            return coordinates;
        }

        for (JsonNode point : coordinatesNode) {

            if (!point.isArray() || point.size() < 2) {
                continue;
            }

            List<Double> coordinate = new ArrayList<>();

            // GeoJSON coordinates are [longitude, latitude].
            coordinate.add(point.get(0).asDouble());
            coordinate.add(point.get(1).asDouble());

            coordinates.add(coordinate);
        }

        return coordinates;
    }

    /*
     * =====================================================
     * HUMAN-READABLE INSTRUCTIONS
     * =====================================================
     */

    private String createInstruction(
            String type,
            String modifier,
            String road) {

        if (type == null) {
            return null;
        }

        if (type.equals("depart")) {
            return "Start your journey";
        }

        if (type.equals("arrive")) {
            return "Arrive at your destination";
        }

        if (type.equals("turn")) {

            if ("left".equals(modifier)) {
                return "Turn left" + roadText(road);
            }

            if ("right".equals(modifier)) {
                return "Turn right" + roadText(road);
            }

            return "Turn" + roadText(road);
        }

        if (type.equals("continue")) {
            return "Continue" + roadText(road);
        }

        if (type.equals("merge")) {
            return "Merge" + roadText(road);
        }

        if (type.equals("fork")) {
            return "Take the fork" + roadText(road);
        }

        if (type.equals("roundabout")) {
            return "Enter the roundabout" + roadText(road);
        }

        if (type.equals("new name")) {
            return "Continue" + roadText(road);
        }

        if (type.equals("on ramp")) {
            return "Take the ramp" + roadText(road);
        }

        if (type.equals("off ramp")) {
            return "Take the exit ramp" + roadText(road);
        }

        return "Continue" + roadText(road);
    }

    /*
     * =====================================================
     * ROAD TEXT
     * =====================================================
     */

    private String roadText(String road) {

        if (road == null || road.isBlank()) {
            return "";
        }

        return " onto " + road;
    }

    /*
     * =====================================================
     * STRING EXTRACTION - RETAINED FOR COMPATIBILITY
     * =====================================================
     */

    private String extractString(
            String body,
            String key) {

        int index = body.indexOf(key);

        if (index == -1) {
            return null;
        }

        int start = index + key.length();

        int end = body.indexOf("\"", start);

        if (end == -1) {
            return null;
        }

        return body.substring(start, end);
    }
}
