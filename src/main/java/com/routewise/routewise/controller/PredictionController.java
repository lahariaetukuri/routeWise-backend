package com.routewise.routewise.controller;

import com.routewise.routewise.entity.Prediction;
import com.routewise.routewise.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<Prediction> createPrediction(
            @RequestBody Prediction prediction) {

        return ResponseEntity.ok(
                predictionService.createPrediction(prediction)
        );
    }

    @GetMapping
    public ResponseEntity<List<Prediction>> getAllPredictions() {
        return ResponseEntity.ok(
                predictionService.getAllPredictions()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prediction> getPredictionById(
            @PathVariable Long id) {

        Prediction prediction =
                predictionService.getPredictionById(id);

        if (prediction == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(prediction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prediction> updatePrediction(
            @PathVariable Long id,
            @RequestBody Prediction prediction) {

        Prediction updatedPrediction =
                predictionService.updatePrediction(id, prediction);

        if (updatedPrediction == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedPrediction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrediction(
            @PathVariable Long id) {

        predictionService.deletePrediction(id);

        return ResponseEntity.noContent().build();
    }
}