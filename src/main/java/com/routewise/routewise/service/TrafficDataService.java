package com.routewise.routewise.service;

import com.routewise.routewise.entity.TrafficData;
import com.routewise.routewise.repository.TrafficDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrafficDataService {

    private final TrafficDataRepository trafficDataRepository;

    public TrafficDataService(TrafficDataRepository trafficDataRepository) {
        this.trafficDataRepository = trafficDataRepository;
    }

    public TrafficData createTrafficData(TrafficData trafficData) {
        return trafficDataRepository.save(trafficData);
    }

    public List<TrafficData> getAllTrafficData() {
        return trafficDataRepository.findAll();
    }

    public TrafficData getTrafficDataById(Long id) {
        return trafficDataRepository.findById(id).orElse(null);
    }

    public TrafficData updateTrafficData(Long id, TrafficData trafficData) {
        TrafficData existingTrafficData =
                trafficDataRepository.findById(id).orElse(null);

        if (existingTrafficData != null) {
            existingTrafficData.setTrafficLevel(trafficData.getTrafficLevel());
            existingTrafficData.setAverageSpeed(trafficData.getAverageSpeed());
            existingTrafficData.setRecordedAt(trafficData.getRecordedAt());
            existingTrafficData.setRoute(trafficData.getRoute());

            return trafficDataRepository.save(existingTrafficData);
        }

        return null;
    }

    public void deleteTrafficData(Long id) {
        trafficDataRepository.deleteById(id);
    }
}
