package com.routewise.routewise.controller;

import com.routewise.routewise.entity.TrafficData;
import com.routewise.routewise.service.TrafficDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin(origins = "*")
public class TrafficDataController {

    private final TrafficDataService trafficDataService;

    public TrafficDataController(TrafficDataService trafficDataService) {
        this.trafficDataService = trafficDataService;
    }

    @PostMapping
    public ResponseEntity<TrafficData> createTrafficData(
            @RequestBody TrafficData trafficData) {
        return ResponseEntity.ok(
                trafficDataService.createTrafficData(trafficData)
        );
    }

    @GetMapping
    public ResponseEntity<List<TrafficData>> getAllTrafficData() {
        return ResponseEntity.ok(
                trafficDataService.getAllTrafficData()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrafficData> getTrafficDataById(
            @PathVariable Long id) {

        TrafficData trafficData =
                trafficDataService.getTrafficDataById(id);

        if (trafficData == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(trafficData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrafficData> updateTrafficData(
            @PathVariable Long id,
            @RequestBody TrafficData trafficData) {

        TrafficData updatedTrafficData =
                trafficDataService.updateTrafficData(id, trafficData);

        if (updatedTrafficData == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedTrafficData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrafficData(
            @PathVariable Long id) {

        trafficDataService.deleteTrafficData(id);

        return ResponseEntity.noContent().build();
    }
}