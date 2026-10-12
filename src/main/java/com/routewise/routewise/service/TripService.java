package com.routewise.routewise.service;

import com.routewise.routewise.entity.Route;
import com.routewise.routewise.entity.TrafficData;
import com.routewise.routewise.entity.Trip;
import com.routewise.routewise.entity.User;
import com.routewise.routewise.repository.RouteRepository;
import com.routewise.routewise.repository.TrafficDataRepository;
import com.routewise.routewise.repository.TripRepository;
import com.routewise.routewise.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final TrafficDataRepository trafficDataRepository;

    public TripService(
            TripRepository tripRepository,
            UserRepository userRepository,
            RouteRepository routeRepository,
            TrafficDataRepository trafficDataRepository) {

        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.trafficDataRepository = trafficDataRepository;
    }

    public Trip createTrip(Trip trip) {

        /*
         * =====================================================
         * STEP 1
         * CREATE / FIND GUEST USER
         * =====================================================
         */

        if (trip.getUser() == null) {

            User user = userRepository
                    .findByEmail("guest@routewise.com")
                    .orElseGet(() -> {

                        User guestUser = new User();

                        guestUser.setName("Guest");
                        guestUser.setEmail("guest@routewise.com");
                        guestUser.setPassword("guest");
                        guestUser.setPhone("0000000000");

                        return userRepository.save(guestUser);
                    });

            trip.setUser(user);
        }


        /*
         * =====================================================
         * STEP 2
         * SAVE TRIP
         * =====================================================
         */

        Trip savedTrip = tripRepository.save(trip);


        /*
         * =====================================================
         * STEP 3
         * FIND ROUTE FOR THIS TRIP
         * =====================================================
         */

        List<Route> routes =
                routeRepository
                        .findBySourceIgnoreCaseAndDestinationIgnoreCase(
                                trip.getSource(),
                                trip.getDestination()
                        );


        /*
         * =====================================================
         * STEP 4
         * CREATE TRAFFIC HISTORY
         * =====================================================
         */

        if (!routes.isEmpty()) {

            Route route = routes.get(0);

            double averageSpeed = 0;

            /*
             * Calculate estimated average speed.
             *
             * distance is stored in kilometres.
             * estimatedTime is stored in minutes.
             */

            if (route.getEstimatedTime() > 0) {

                averageSpeed =
                        (route.getDistance()
                                / route.getEstimatedTime())
                                * 60;
            }


            /*
             * Determine traffic level.
             */

            String trafficLevel;

            if (averageSpeed >= 40) {

                trafficLevel = "Low";

            } else if (averageSpeed >= 25) {

                trafficLevel = "Moderate";

            } else if (averageSpeed > 0) {

                trafficLevel = "Heavy";

            } else {

                trafficLevel = "Unknown";
            }


            /*
             * Create traffic record.
             */

            TrafficData trafficData =
                    new TrafficData();

            trafficData.setTrafficLevel(
                    trafficLevel
            );

            trafficData.setAverageSpeed(
                    averageSpeed
            );

            trafficData.setRecordedAt(
                    LocalDateTime.now()
            );

            trafficData.setRoute(
                    route
            );


            /*
             * Save traffic history.
             */

            trafficDataRepository.save(
                    trafficData
            );
        }


        return savedTrip;
    }


    /*
     * =====================================================
     * GET ALL TRIPS
     * =====================================================
     */

    public List<Trip> getAllTrips() {

        return tripRepository.findAll();
    }


    /*
     * =====================================================
     * GET TRIP BY ID
     * =====================================================
     */

    public Trip getTripById(Long id) {

        return tripRepository
                .findById(id)
                .orElse(null);
    }


    /*
     * =====================================================
     * UPDATE TRIP
     * =====================================================
     */

    public Trip updateTrip(
            Long id,
            Trip trip) {

        Trip existingTrip =
                tripRepository
                        .findById(id)
                        .orElse(null);

        if (existingTrip != null) {

            existingTrip.setSource(
                    trip.getSource()
            );

            existingTrip.setDestination(
                    trip.getDestination()
            );

            existingTrip.setStartTime(
                    trip.getStartTime()
            );

            existingTrip.setEndTime(
                    trip.getEndTime()
            );


            if (trip.getUser() != null) {

                existingTrip.setUser(
                        trip.getUser()
                );
            }


            return tripRepository.save(
                    existingTrip
            );
        }


        return null;
    }


    /*
     * =====================================================
     * DELETE TRIP
     * =====================================================
     */

    public void deleteTrip(Long id) {

        tripRepository.deleteById(id);
    }
}
