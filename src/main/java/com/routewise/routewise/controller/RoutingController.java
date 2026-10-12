package com.routewise.routewise.controller;

import com.routewise.routewise.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/directions")
@CrossOrigin
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(
            RoutingService routingService) {

        this.routingService =
                routingService;
    }


    @GetMapping
    public ResponseEntity<?> getDirections(

            @RequestParam String source,

            @RequestParam String destination) {

        try {

            Map<String, Object> result =
                    routingService.getRoute(
                            source,
                            destination
                    );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "error",
                                    "Unable to calculate route directions",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}