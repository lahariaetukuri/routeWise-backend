package com.routewise.routewise.controller;

import com.routewise.routewise.entity.Trip;
import com.routewise.routewise.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody Trip trip) {

        try {

            Trip createdTrip = tripService.createTrip(trip);

            return ResponseEntity.ok(createdTrip);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Unable to create trip: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTrips() {

        try {

            return ResponseEntity.ok(tripService.getAllTrips());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Unable to fetch trips: " + e.getMessage());
        }
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(@PathVariable Long id) {

        try {

            Trip trip = tripService.getTripById(id);

            if (trip == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(trip);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Unable to fetch trip: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long id,
            @RequestBody Trip trip) {

        try {

            Trip updatedTrip = tripService.updateTrip(id, trip);

            if (updatedTrip == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedTrip);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Unable to update trip: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long id) {

        try {

            tripService.deleteTrip(id);

            return ResponseEntity.ok("Trip deleted successfully");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Unable to delete trip: " + e.getMessage());
        }
    }
}