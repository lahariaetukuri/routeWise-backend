package com.routewise.routewise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutingService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RoutingService() {

        httpClient = HttpClient.newHttpClient();

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

        Map<String, Double> sourceCoordinates =
                geocode(source);

        Map<String, Double> destinationCoordinates =
                geocode(destination);

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

        double sourceLat =
                sourceCoordinates.get("lat");

        double sourceLon =
                sourceCoordinates.get("lon");

        double destinationLat =
                destinationCoordinates.get("lat");

        double destinationLon =
                destinationCoordinates.get("lon");

        /*
         * OSRM uses:
         * longitude,latitude
         */

        String coordinates =
                sourceLon + "," + sourceLat
                        + ";"
                        + destinationLon + "," + destinationLat;

        /*
         * IMPORTANT
         *
         * overview=full
         * gives the complete road geometry.
         *
         * geometries=geojson
         * gives coordinates in GeoJSON format.
         *
         * steps=true
         * gives turn-by-turn directions.
         *
         * alternatives=true
         * asks OSRM for alternative routes.
         */

        String url =
                "https://router.project-osrm.org/route/v1/driving/"
                        + coordinates
                        + "?overview=full"
                        + "&geometries=geojson"
                        + "&steps=true"
                        + "&alternatives=true";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "User-Agent",
                                "RouteWise-College-Project/1.0"
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "Routing service returned status: "
                            + response.statusCode()
            );
        }

        String body =
                response.body();

        JsonNode root =
                objectMapper.readTree(body);

        String code =
                root.path("code").asText();

        if (!"Ok".equals(code)) {

            throw new RuntimeException(
                    "Unable to calculate route"
            );
        }

        /*
         * =================================================
         * ROUTES
         * =================================================
         */

        JsonNode routes =
                root.path("routes");

        if (!routes.isArray() ||
                routes.isEmpty()) {

            throw new RuntimeException(
                    "No routes found"
            );
        }

        /*
         * =================================================
         * MAIN ROUTE
         * =================================================
         */

        JsonNode mainRoute =
                routes.get(0);

        double distance =
                mainRoute
                        .path("distance")
                        .asDouble();

        double duration =
                mainRoute
                        .path("duration")
                        .asDouble();

        double distanceKm =
                Math.round(
                        (distance / 1000.0) * 100
                ) / 100.0;

        int durationMinutes =
                (int) Math.round(
                        duration / 60.0
                );

        /*
         * =================================================
         * DIRECTIONS
         * =================================================
         */

        List<Map<String, Object>> directions =
                extractDirections(mainRoute);

        /*
         * =================================================
         * GEOMETRY
         * =================================================
         *
         * Converts:
         *
         * {
         *   "type": "LineString",
         *   "coordinates": [
         *      [longitude, latitude],
         *      [longitude, latitude]
         *   ]
         * }
         *
         * into:
         *
         * [
         *   [longitude, latitude],
         *   [longitude, latitude]
         * ]
         */

        List<List<Double>> geometry =
                extractGeometry(mainRoute);

        /*
         * =================================================
         * RESULT
         * =================================================
         */

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "source",
                source
        );

        result.put(
                "destination",
                destination
        );

        result.put(
                "sourceLatitude",
                sourceLat
        );

        result.put(
                "sourceLongitude",
                sourceLon
        );

        result.put(
                "destinationLatitude",
                destinationLat
        );

        result.put(
                "destinationLongitude",
                destinationLon
        );

        result.put(
                "distance",
                distanceKm
        );

        result.put(
                "estimatedTime",
                durationMinutes
        );

        result.put(
                "directions",
                directions
        );

        /*
         * ACTUAL MAP ROUTE LINE
         */

        result.put(
                "geometry",
                geometry
        );

        /*
         * =================================================
         * ALTERNATIVE ROUTES
         * =================================================
         */

        if (routes.size() > 1) {

            List<Map<String, Object>>
                    alternatives =
                    new ArrayList<>();

            for (int i = 1;
                 i < routes.size();
                 i++) {

                JsonNode alternative =
                        routes.get(i);

                double alternativeDistance =
                        alternative
                                .path("distance")
                                .asDouble();

                double alternativeDuration =
                        alternative
                                .path("duration")
                                .asDouble();

                double alternativeDistanceKm =
                        Math.round(
                                (alternativeDistance / 1000.0)
                                        * 100
                        ) / 100.0;

                int alternativeTimeMinutes =
                        (int) Math.round(
                                alternativeDuration / 60.0
                        );

                Map<String, Object>
                        alternativeResult =
                        new LinkedHashMap<>();

                alternativeResult.put(
                        "distance",
                        alternativeDistanceKm
                );

                alternativeResult.put(
                        "estimatedTime",
                        alternativeTimeMinutes
                );

                /*
                 * Alternative directions
                 */

                alternativeResult.put(
                        "directions",
                        extractDirections(
                                alternative
                        )
                );

                /*
                 * Alternative map geometry
                 */

                alternativeResult.put(
                        "geometry",
                        extractGeometry(
                                alternative
                        )
                );

                alternatives.add(
                        alternativeResult
                );
            }

            result.put(
                    "alternatives",
                    alternatives
            );
        }

        return result;
    }

    /*
     * =====================================================
     * GEOCODING
     * =====================================================
     */

    private Map<String, Double> geocode(
            String location) throws Exception {

        String cleanedLocation =
                location.trim();

        String[] searches = {

                cleanedLocation
                        + ", Hyderabad, Telangana, India",

                cleanedLocation
                        + ", Telangana, India",

                cleanedLocation
                        + ", India",

                cleanedLocation
        };

        for (String searchLocation : searches) {

            String encodedLocation =
                    URLEncoder.encode(
                            searchLocation,
                            StandardCharsets.UTF_8
                    );

            String url =
                    "https://nominatim.openstreetmap.org/search"
                            + "?q="
                            + encodedLocation
                            + "&format=json"
                            + "&limit=1";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "User-Agent",
                                    "RouteWise-College-Project/1.0"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                continue;
            }

            String body =
                    response.body();

            if (body == null ||
                    body.isBlank() ||
                    body.equals("[]")) {

                continue;
            }

            String latitude =
                    extractString(
                            body,
                            "\"lat\":\""
                    );

            String longitude =
                    extractString(
                            body,
                            "\"lon\":\""
                    );

            if (latitude == null ||
                    longitude == null) {

                continue;
            }

            try {

                double lat =
                        Double.parseDouble(
                                latitude
                        );

                double lon =
                        Double.parseDouble(
                                longitude
                        );

                Map<String, Double>
                        coordinates =
                        new LinkedHashMap<>();

                coordinates.put(
                        "lat",
                        lat
                );

                coordinates.put(
                        "lon",
                        lon
                );

                return coordinates;

            } catch (NumberFormatException e) {

                continue;
            }
        }

        return null;
    }

    /*
     * =====================================================
     * DIRECTIONS
     * =====================================================
     */

    private List<Map<String, Object>>
    extractDirections(JsonNode route) {

        List<Map<String, Object>>
                directions =
                new ArrayList<>();

        JsonNode legs =
                route.path("legs");

        if (!legs.isArray()) {

            return directions;
        }

        int stepNumber = 1;

        for (JsonNode leg : legs) {

            JsonNode steps =
                    leg.path("steps");

            if (!steps.isArray()) {
                continue;
            }

            for (JsonNode step : steps) {

                JsonNode maneuver =
                        step.path("maneuver");

                String type =
                        maneuver
                                .path("type")
                                .asText(null);

                String modifier =
                        maneuver
                                .path("modifier")
                                .asText(null);

                String road =
                        step.path("name")
                                .asText("");

                double distance =
                        step.path("distance")
                                .asDouble();

                double duration =
                        step.path("duration")
                                .asDouble();

                if (type == null) {
                    continue;
                }

                String instruction =
                        createInstruction(
                                type,
                                modifier,
                                road
                        );

                if (instruction == null) {
                    continue;
                }

                Map<String, Object>
                        direction =
                        new LinkedHashMap<>();

                direction.put(
                        "step",
                        stepNumber
                );

                direction.put(
                        "instruction",
                        instruction
                );

                direction.put(
                        "road",
                        road
                );

                direction.put(
                        "distance",
                        Math.round(
                                distance * 100
                        ) / 100.0
                );

                direction.put(
                        "duration",
                        (int) Math.round(
                                duration / 60.0
                        )
                );

                directions.add(
                        direction
                );

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

    private List<List<Double>>
    extractGeometry(JsonNode route) {

        List<List<Double>>
                coordinates =
                new ArrayList<>();

        JsonNode geometry =
                route.path("geometry");

        if (!geometry.isObject()) {
            return coordinates;
        }

        JsonNode coordinatesNode =
                geometry.path("coordinates");

        if (!coordinatesNode.isArray()) {
            return coordinates;
        }

        for (JsonNode point :
                coordinatesNode) {

            if (!point.isArray() ||
                    point.size() < 2) {

                continue;
            }

            List<Double>
                    coordinate =
                    new ArrayList<>();

            /*
             * IMPORTANT:
             *
             * GeoJSON uses:
             *
             * [longitude, latitude]
             */

            coordinate.add(
                    point.get(0).asDouble()
            );

            coordinate.add(
                    point.get(1).asDouble()
            );

            coordinates.add(
                    coordinate
            );
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

                return "Turn left"
                        + roadText(road);
            }

            if ("right".equals(modifier)) {

                return "Turn right"
                        + roadText(road);
            }

            return "Turn"
                    + roadText(road);
        }

        if (type.equals("continue")) {

            return "Continue"
                    + roadText(road);
        }

        if (type.equals("merge")) {

            return "Merge"
                    + roadText(road);
        }

        if (type.equals("fork")) {

            return "Take the fork"
                    + roadText(road);
        }

        if (type.equals("roundabout")) {

            return "Enter the roundabout"
                    + roadText(road);
        }

        if (type.equals("new name")) {

            return "Continue"
                    + roadText(road);
        }

        if (type.equals("on ramp")) {

            return "Take the ramp"
                    + roadText(road);
        }

        if (type.equals("off ramp")) {

            return "Take the exit ramp"
                    + roadText(road);
        }

        return "Continue"
                + roadText(road);
    }

    /*
     * =====================================================
     * ROAD TEXT
     * =====================================================
     */

    private String roadText(
            String road) {

        if (road == null ||
                road.isBlank()) {

            return "";
        }

        return " onto " + road;
    }

    /*
     * =====================================================
     * STRING EXTRACTION
     * =====================================================
     */

    private String extractString(
            String body,
            String key) {

        int index =
                body.indexOf(key);

        if (index == -1) {
            return null;
        }

        int start =
                index + key.length();

        int end =
                body.indexOf(
                        "\"",
                        start
                );

        if (end == -1) {
            return null;
        }

        return body.substring(
                start,
                end
        );
    }
}