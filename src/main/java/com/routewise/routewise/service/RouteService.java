package com.routewise.routewise.service;

import com.routewise.routewise.entity.Route;
import com.routewise.routewise.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id).orElse(null);
    }

    public Route updateRoute(Long id, Route route) {
        Route existingRoute = routeRepository.findById(id).orElse(null);

        if (existingRoute != null) {
            existingRoute.setDistance(route.getDistance());
            existingRoute.setEstimatedTime(route.getEstimatedTime());
            existingRoute.setRouteType(route.getRouteType());
            existingRoute.setTrip(route.getTrip());

            return routeRepository.save(existingRoute);
        }

        return null;
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}
