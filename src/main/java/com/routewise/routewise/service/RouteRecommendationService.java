
        package com.routewise.routewise.service;

import com.routewise.routewise.entity.Route;
import com.routewise.routewise.entity.TrafficData;
import com.routewise.routewise.repository.RouteRepository;
import com.routewise.routewise.repository.TrafficDataRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteRecommendationService {

    private final RouteRepository routeRepository;
    private final TrafficDataRepository trafficDataRepository;
    private final RoutingService routingService;

    public RouteRecommendationService(
            RouteRepository routeRepository,
            TrafficDataRepository trafficDataRepository,
            RoutingService routingService) {

        this.routeRepository = routeRepository;
        this.trafficDataRepository = trafficDataRepository;
        this.routingService = routingService;
    }

    public List<Map<String, Object>> getRecommendedRoutes(
            String source,
            String destination) {

        List<Route> routes =
                routeRepository
                        .findBySourceIgnoreCaseAndDestinationIgnoreCase(
                                source,
                                destination
                        );

        /*
         * If there are no routes in the database,
         * calculate routes dynamically.
         */
        if (routes.isEmpty()) {
            return getDynamicRoutes(source, destination);
        }

        List<TrafficData> allTrafficData =
                trafficDataRepository.findAll();

        List<Map<String, Object>> recommendations =
                new ArrayList<>();

        /*
         * Get turn-by-turn directions from RoutingService.
         *
         * This was missing earlier.
         */
        Map<String, Object> routingResult = null;

        try {

            routingResult =
                    routingService.getRoute(
                            source,
                            destination
                    );

        } catch (Exception e) {

            System.out.println(
                    "Unable to get directions: "
                            + e.getMessage()
            );
        }

        for (Route route : routes) {

            /*
             * Only use REAL traffic records.
             */
            List<TrafficData> routeTraffic =
                    allTrafficData.stream()
                            .filter(data ->
                                    data.getRoute() != null
                                            && data.getRoute()
                                            .getRouteId()
                                            .equals(route.getRouteId())
                                            && data.getAverageSpeed() > 0
                            )
                            .toList();

            double averageSpeed = 0;

            if (!routeTraffic.isEmpty()) {

                averageSpeed =
                        routeTraffic.stream()
                                .mapToDouble(
                                        TrafficData::getAverageSpeed
                                )
                                .average()
                                .orElse(0);
            }

            /*
             * Predicted travel time
             */

            int predictedTravelTime =
                    route.getEstimatedTime();

            if (averageSpeed > 0 &&
                    route.getDistance() > 0) {

                predictedTravelTime =
                        (int) Math.round(
                                (route.getDistance()
                                        / averageSpeed) * 60
                        );
            }

            /*
             * Traffic level
             */

            String trafficLevel = "Unknown";

            if (averageSpeed >= 40) {

                trafficLevel = "Low";

            } else if (averageSpeed >= 25) {

                trafficLevel = "Moderate";

            } else if (averageSpeed > 0) {

                trafficLevel = "Heavy";
            }

            /*
             * Reliability
             */

            int reliability = 70;

            if (trafficLevel.equals("Low")) {

                reliability = 90;

            } else if (trafficLevel.equals("Moderate")) {

                reliability = 80;

            } else if (trafficLevel.equals("Heavy")) {

                reliability = 60;
            }

            /*
             * Historical speeds
             */

            List<Double> historicalSpeeds =
                    routeTraffic.stream()
                            .map(TrafficData::getAverageSpeed)
                            .toList();

            int historicalRecords =
                    routeTraffic.size();

            /*
             * Prediction confidence
             */

            double predictionConfidence;

            if (historicalRecords >= 5) {

                predictionConfidence = 95;

            } else if (historicalRecords >= 4) {

                predictionConfidence = 85;

            } else if (historicalRecords >= 3) {

                predictionConfidence = 75;

            } else if (historicalRecords >= 2) {

                predictionConfidence = 65;

            } else if (historicalRecords == 1) {

                predictionConfidence = 55;

            } else {

                predictionConfidence = 40;
            }

            /*
             * Traffic trend
             */

            String trafficTrend = "Unknown";

            double trendChange = 0;

            if (routeTraffic.size() >= 2) {

                double firstSpeed =
                        routeTraffic.get(0)
                                .getAverageSpeed();

                double lastSpeed =
                        routeTraffic.get(
                                routeTraffic.size() - 1
                        ).getAverageSpeed();

                if (firstSpeed > 0) {

                    trendChange =
                            ((lastSpeed - firstSpeed)
                                    / firstSpeed) * 100;
                }

                if (trendChange <= -10) {

                    trafficTrend = "Worsening";

                } else if (trendChange >= 10) {

                    trafficTrend = "Improving";

                } else {

                    trafficTrend = "Stable";
                }
            }

            /*
             * Base score
             */

            double baseScore = 50;

            if (trafficLevel.equals("Low")) {

                baseScore += 20;

            } else if (trafficLevel.equals("Moderate")) {

                baseScore += 10;

            } else if (trafficLevel.equals("Heavy")) {

                baseScore -= 10;
            }

            baseScore += reliability * 0.20;

            baseScore += predictionConfidence * 0.10;

            /*
             * If there is no historical traffic,
             * use neutral score.
             */

            if (historicalRecords == 0) {

                baseScore =
                        calculateNoHistoryScore(
                                route.getDistance(),
                                route.getEstimatedTime()
                        );
            }

            baseScore =
                    Math.round(baseScore * 100) / 100.0;

            /*
             * Trend adjustment
             */

            double trendAdjustment = 0;

            if (trafficTrend.equals("Worsening")) {

                trendAdjustment = -5;

            } else if (trafficTrend.equals("Improving")) {

                trendAdjustment = 5;
            }

            /*
             * Final score
             */

            double finalScore =
                    baseScore + trendAdjustment;

            finalScore =
                    Math.max(
                            0,
                            Math.min(
                                    100,
                                    finalScore
                            )
                    );

            finalScore =
                    Math.round(finalScore * 100) / 100.0;

            /*
             * Performance
             */

            String performanceRating;

            if (finalScore >= 80) {

                performanceRating = "Excellent";

            } else if (finalScore >= 65) {

                performanceRating = "Good";

            } else if (finalScore >= 50) {

                performanceRating = "Average";

            } else {

                performanceRating = "Poor";
            }

            /*
             * Trend description
             */

            String trendDescription;

            if (trafficTrend.equals("Worsening")) {

                trendDescription =
                        "Traffic conditions are worsening.";

            } else if (trafficTrend.equals("Improving")) {

                trendDescription =
                        "Traffic conditions are improving.";

            } else if (trafficTrend.equals("Stable")) {

                trendDescription =
                        "Traffic conditions are stable.";

            } else {

                trendDescription =
                        "No historical traffic data available for this route.";
            }

            /*
             * Response
             */

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "routeId",
                    route.getRouteId()
            );

            result.put(
                    "name",
                    route.getRouteType()
            );

            result.put(
                    "source",
                    route.getSource()
            );

            result.put(
                    "destination",
                    route.getDestination()
            );

            result.put(
                    "distance",
                    route.getDistance()
            );

            result.put(
                    "travelTime",
                    predictedTravelTime
            );

            result.put(
                    "predictedTravelTime",
                    predictedTravelTime
            );

            result.put(
                    "averageSpeed",
                    averageSpeed
            );

            result.put(
                    "historicalRecords",
                    historicalRecords
            );

            result.put(
                    "historicalSpeeds",
                    historicalSpeeds
            );

            result.put(
                    "traffic",
                    trafficLevel
            );

            result.put(
                    "trafficTrend",
                    trafficTrend
            );

            result.put(
                    "trendDescription",
                    trendDescription
            );

            result.put(
                    "trendChange",
                    Math.round(
                            trendChange * 100
                    ) / 100.0
            );

            result.put(
                    "reliability",
                    reliability
            );

            result.put(
                    "predictionConfidence",
                    predictionConfidence
            );

            result.put(
                    "baseScore",
                    baseScore
            );

            result.put(
                    "trendAdjustment",
                    trendAdjustment
            );

            result.put(
                    "score",
                    finalScore
            );

            result.put(
                    "performanceRating",
                    performanceRating
            );

            /*
             * =================================================
             * TURN-BY-TURN DIRECTIONS
             * =================================================
             *
             * This is the important addition.
             */

            if (routingResult != null) {

                result.put(
                        "directions",
                        routingResult.get("directions")
                );

            } else {

                result.put(
                        "directions",
                        new ArrayList<>()
                );
            }

            recommendations.add(result);
        }

        /*
         * Sort by score
         */

        recommendations.sort(
                (a, b) ->
                        Double.compare(
                                ((Number)
                                        b.get("score"))
                                        .doubleValue(),

                                ((Number)
                                        a.get("score"))
                                        .doubleValue()
                        )
        );

        /*
         * Mark recommended route
         */

        for (int i = 0;
             i < recommendations.size();
             i++) {

            recommendations.get(i)
                    .put(
                            "recommended",
                            i == 0
                    );
        }

        return recommendations;
    }


    /*
     * =========================================================
     * DYNAMIC ROUTES
     * =========================================================
     */

    private List<Map<String, Object>> getDynamicRoutes(
            String source,
            String destination) {

        List<Map<String, Object>> recommendations =
                new ArrayList<>();

        try {

            Map<String, Object> routingResult =
                    routingService.getRoute(
                            source,
                            destination
                    );

            double distance =
                    ((Number)
                            routingResult.get("distance"))
                            .doubleValue();

            int travelTime =
                    ((Number)
                            routingResult.get("estimatedTime"))
                            .intValue();

            Map<String, Object> mainRoute =
                    createDynamicRecommendation(
                            "Recommended",
                            distance,
                            travelTime,
                            0
                    );

            mainRoute.put(
                    "directions",
                    routingResult.get("directions")
            );

            recommendations.add(mainRoute);

            Object alternativeObject =
                    routingResult.get("alternatives");

            if (alternativeObject instanceof List<?>) {

                List<?> alternatives =
                        (List<?>)
                                alternativeObject;

                int alternativeNumber = 1;

                for (Object object : alternatives) {

                    if (!(object instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> alternative =
                            (Map<?, ?>) object;

                    if (!(alternative.get("distance")
                            instanceof Number)
                            || !(alternative.get("estimatedTime")
                            instanceof Number)) {
                        continue;
                    }

                    double alternativeDistance =
                            ((Number)
                                    alternative.get(
                                            "distance"
                                    )).doubleValue();

                    int alternativeTime =
                            ((Number)
                                    alternative.get(
                                            "estimatedTime"
                                    )).intValue();

                    Map<String, Object>
                            alternativeRoute =
                            createDynamicRecommendation(
                                    "Alternative "
                                            + alternativeNumber,
                                    alternativeDistance,
                                    alternativeTime,
                                    alternativeNumber
                            );

                    alternativeRoute.put(
                            "directions",
                            alternative.get("directions")
                    );

                    recommendations.add(
                            alternativeRoute
                    );

                    alternativeNumber++;
                }
            }

            recommendations.sort(
                    (a, b) ->
                            Double.compare(
                                    ((Number)
                                            b.get("score"))
                                            .doubleValue(),

                                    ((Number)
                                            a.get("score"))
                                            .doubleValue()
                            )
            );

            for (int i = 0;
                 i < recommendations.size();
                 i++) {

                recommendations.get(i)
                        .put(
                                "recommended",
                                i == 0
                        );
            }

            return recommendations;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to calculate dynamic routes: "
                            + e.getMessage(),
                    e
            );
        }
    }


    /*
     * =========================================================
     * DYNAMIC RECOMMENDATION
     * =========================================================
     */

    private Map<String, Object>
    createDynamicRecommendation(
            String name,
            double distance,
            int travelTime,
            int alternativeNumber) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        double averageSpeed = 0;

        int historicalRecords = 0;

        double predictionConfidence = 40;

        String traffic = "Unknown";

        String trafficTrend = "Unknown";

        double trendChange = 0;

        int reliability = 70;

        double distanceScore =
                Math.max(
                        0,
                        100 - (distance * 2)
                );

        double timeScore =
                Math.max(
                        0,
                        100 - (travelTime * 2)
                );

        double baseScore =
                (distanceScore * 0.40)
                        +
                        (timeScore * 0.40)
                        +
                        (reliability * 0.20);

        baseScore =
                Math.round(
                        baseScore * 100
                ) / 100.0;

        double trendAdjustment = 0;

        double finalScore =
                baseScore + trendAdjustment;

        finalScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                finalScore
                        )
                );

        finalScore =
                Math.round(
                        finalScore * 100
                ) / 100.0;

        String performanceRating;

        if (finalScore >= 80) {

            performanceRating = "Excellent";

        } else if (finalScore >= 65) {

            performanceRating = "Good";

        } else if (finalScore >= 50) {

            performanceRating = "Average";

        } else {

            performanceRating = "Poor";
        }

        result.put(
                "routeId",
                null
        );

        result.put(
                "name",
                name
        );

        result.put(
                "distance",
                distance
        );

        result.put(
                "travelTime",
                travelTime
        );

        result.put(
                "predictedTravelTime",
                travelTime
        );

        result.put(
                "averageSpeed",
                averageSpeed
        );

        result.put(
                "historicalRecords",
                historicalRecords
        );

        result.put(
                "historicalSpeeds",
                new ArrayList<>()
        );

        result.put(
                "traffic",
                traffic
        );

        result.put(
                "trafficTrend",
                trafficTrend
        );

        result.put(
                "trendDescription",
                "No historical traffic data available for this route."
        );

        result.put(
                "trendChange",
                trendChange
        );

        result.put(
                "reliability",
                reliability
        );

        result.put(
                "predictionConfidence",
                predictionConfidence
        );

        result.put(
                "baseScore",
                baseScore
        );

        result.put(
                "trendAdjustment",
                trendAdjustment
        );

        result.put(
                "score",
                finalScore
        );

        result.put(
                "performanceRating",
                performanceRating
        );

        return result;
    }


    /*
     * =========================================================
     * SCORE WHEN THERE IS NO HISTORICAL DATA
     * =========================================================
     */

    private double calculateNoHistoryScore(
            double distance,
            int estimatedTime) {

        double distanceScore =
                Math.max(
                        0,
                        100 - (distance * 2)
                );

        double timeScore =
                Math.max(
                        0,
                        100 - (estimatedTime * 2)
                );

        double score =
                (distanceScore * 0.50)
                        +
                        (timeScore * 0.50);

        return Math.round(
                score * 100
        ) / 100.0;
    }
}

