package com.routewise.routewise.controller;

import com.routewise.routewise.service.RouteRecommendationService;
import com.routewise.routewise.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin
public class RouteController {

    private final RouteRecommendationService routeRecommendationService;
    private final RoutingService routingService;

    public RouteController(
            RouteRecommendationService routeRecommendationService,
            RoutingService routingService) {

        this.routeRecommendationService =
                routeRecommendationService;

        this.routingService =
                routingService;
    }


    /*
     * =========================================================
     * GET RECOMMENDED ROUTES
     * =========================================================
     */

    @GetMapping
    public ResponseEntity<?> getRoutes(
            @RequestParam String source,
            @RequestParam String destination) {

        try {

            List<Map<String, Object>> routes =
                    routeRecommendationService
                            .getRecommendedRoutes(
                                    source,
                                    destination
                            );

            if (routes.isEmpty()) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity.ok(routes);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Unable to calculate routes",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    /*
     * =========================================================
     * GET TRAFFIC
     * =========================================================
     */

    @GetMapping("/traffic")
    public ResponseEntity<?> getTraffic(
            @RequestParam String source,
            @RequestParam String destination) {

        try {

            List<Map<String, Object>> routes =
                    routeRecommendationService
                            .getRecommendedRoutes(
                                    source,
                                    destination
                            );

            if (routes.isEmpty()) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            Map<String, Object> recommendedRoute =
                    routes.get(0);

            return ResponseEntity.ok(
                    Map.of(
                            "source",
                            source,

                            "destination",
                            destination,

                            "traffic",
                            recommendedRoute.get(
                                    "traffic"
                            ),

                            "trafficLevel",
                            recommendedRoute.get(
                                    "traffic"
                            ),

                            "trafficTrend",
                            recommendedRoute.get(
                                    "trafficTrend"
                            ),

                            "reliability",
                            recommendedRoute.get(
                                    "reliability"
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Unable to calculate traffic",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    /*
     * =========================================================
     * GET MAP ROUTE
     *
     * This endpoint directly calls RoutingService.
     *
     * Example:
     *
     * /api/routes/map?source=Madhapur&destination=Gachibowli
     *
     * =========================================================
     */

    @GetMapping("/map")
    public ResponseEntity<?> getMapRoute(
            @RequestParam String source,
            @RequestParam String destination) {

        try {

            Map<String, Object> route =
                    routingService.getRoute(
                            source,
                            destination
                    );

            return ResponseEntity.ok(route);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Unable to calculate map route",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}