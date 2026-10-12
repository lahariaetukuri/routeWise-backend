package com.routewise.routewise.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_data")
public class TrafficData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trafficId;

    private String trafficLevel;

    private double averageSpeed;

    private LocalDateTime recordedAt;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    public TrafficData() {
    }

    public TrafficData(Long trafficId, String trafficLevel,
                       double averageSpeed, LocalDateTime recordedAt,
                       Route route) {
        this.trafficId = trafficId;
        this.trafficLevel = trafficLevel;
        this.averageSpeed = averageSpeed;
        this.recordedAt = recordedAt;
        this.route = route;
    }

    public Long getTrafficId() {
        return trafficId;
    }

    public void setTrafficId(Long trafficId) {
        this.trafficId = trafficId;
    }

    public String getTrafficLevel() {
        return trafficLevel;
    }

    public void setTrafficLevel(String trafficLevel) {
        this.trafficLevel = trafficLevel;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
