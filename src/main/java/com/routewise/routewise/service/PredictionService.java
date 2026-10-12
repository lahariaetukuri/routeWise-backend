package com.routewise.routewise.service;

import com.routewise.routewise.entity.Prediction;
import com.routewise.routewise.repository.PredictionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PredictionService {

    private final PredictionRepository predictionRepository;

    public PredictionService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public Prediction createPrediction(Prediction prediction) {
        return predictionRepository.save(prediction);
    }

    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAll();
    }

    public Prediction getPredictionById(Long id) {
        return predictionRepository.findById(id).orElse(null);
    }

    public Prediction updatePrediction(Long id, Prediction prediction) {
        Prediction existingPrediction =
                predictionRepository.findById(id).orElse(null);

        if (existingPrediction != null) {
            existingPrediction.setPredictedTrafficLevel(
                    prediction.getPredictedTrafficLevel());

            existingPrediction.setPredictedTravelTime(
                    prediction.getPredictedTravelTime());

            existingPrediction.setPredictedAt(
                    prediction.getPredictedAt());

            existingPrediction.setRoute(prediction.getRoute());

            return predictionRepository.save(existingPrediction);
        }

        return null;
    }

    public void deletePrediction(Long id) {
        predictionRepository.deleteById(id);
    }
}
