package com.routewise.routewise.repository;

import com.routewise.routewise.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findBySourceIgnoreCaseAndDestinationIgnoreCase(
            String source,
            String destination
    );
}