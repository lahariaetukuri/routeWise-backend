package com.routewise.routewise.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long predictionId;

    private String predictedTrafficLevel;

    private double predictedTravelTime;

    private LocalDateTime predictedAt;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    public Prediction() {
    }

    public Prediction(Long predictionId, String predictedTrafficLevel,
                      double predictedTravelTime, LocalDateTime predictedAt,
                      Route route) {
        this.predictionId = predictionId;
        this.predictedTrafficLevel = predictedTrafficLevel;
        this.predictedTravelTime = predictedTravelTime;
        this.predictedAt = predictedAt;
        this.route = route;
    }

    public Long getPredictionId() {
        return predictionId;
    }

    public void setPredictionId(Long predictionId) {
        this.predictionId = predictionId;
    }

    public String getPredictedTrafficLevel() {
        return predictedTrafficLevel;
    }

    public void setPredictedTrafficLevel(String predictedTrafficLevel) {
        this.predictedTrafficLevel = predictedTrafficLevel;
    }

    public double getPredictedTravelTime() {
        return predictedTravelTime;
    }

    public void setPredictedTravelTime(double predictedTravelTime) {
        this.predictedTravelTime = predictedTravelTime;
    }

    public LocalDateTime getPredictedAt() {
        return predictedAt;
    }

    public void setPredictedAt(LocalDateTime predictedAt) {
        this.predictedAt = predictedAt;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}