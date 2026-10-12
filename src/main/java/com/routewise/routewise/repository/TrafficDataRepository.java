package com.routewise.routewise.repository;

import com.routewise.routewise.entity.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrafficDataRepository
        extends JpaRepository<TrafficData, Long> {

    List<TrafficData> findByRouteRouteIdOrderByRecordedAtAsc(
            Long routeId
    );
}