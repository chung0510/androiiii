package com.smartbox.backend.service;

import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LockerGeneratorService {

    private final LockerRepository lockerRepository;

    private static final double EARTH_RADIUS = 6371000.0;
    private static final double MAX_RADIUS_METERS = 2000.0;
    private static final int LOCKER_COUNT = 5;
    private static final double MIN_DISTANCE_BETWEEN_LOCKERS = 100.0;

    private final Random random = new Random();

    public List<Locker> generate(double userLat, double userLng) {

        lockerRepository.deleteAll();

        List<Locker> lockers = new ArrayList<>();

        int maxAttempts = 2000;
        int attempts = 0;

        String[] lockerNames = {
                "A",
                "B",
                "C",
                "D",
                "E"
        };

        while (lockers.size() < LOCKER_COUNT && attempts < maxAttempts) {

            attempts++;

            double[] point = generateRandomPoint(userLat, userLng);

            double lat = point[0];
            double lng = point[1];

            if (tooClose(lat, lng, lockers)) {
                continue;
            }

            Locker locker = new Locker();

            locker.setLockerId(lockerNames[lockers.size()]);
            List<String> slots = new ArrayList<>();

            String prefix =
                    lockerNames[lockers.size()];

            for(int i = 1; i <= 5; i++)
            {
                slots.add(
                        prefix + i
                );
            }

            locker.setSlots(slots);

            locker.setLatitude(lat);
            locker.setLongitude(lng);

            locker.setAddress("Locker " + (lockers.size() + 1));

            locker.setStatus("ACTIVE");

            locker.setTotalSlots(5);

            locker.setAvailableSlots(
                    1 + random.nextInt(5)
            );

            lockers.add(locker);
        }
        if(lockers.size() < LOCKER_COUNT)
        {
            System.out.println(
                    "WARNING: only generated "
                            + lockers.size()
                            + " lockers"
            );
        }
        lockerRepository.saveAll(lockers);

        return lockers;
    }

    /**
     * Random point within 2km circle
     */
    private double[] generateRandomPoint(
            double userLat,
            double userLng
    ) {

        double distance =
                Math.sqrt(random.nextDouble()) * MAX_RADIUS_METERS;

        double angle =
                random.nextDouble() * 2 * Math.PI;

        double latRad =
                Math.toRadians(userLat);

        double lngRad =
                Math.toRadians(userLng);

        double angularDistance =
                distance / EARTH_RADIUS;

        double newLat =
                Math.asin(
                        Math.sin(latRad)
                                * Math.cos(angularDistance)
                                +
                                Math.cos(latRad)
                                        * Math.sin(angularDistance)
                                        * Math.cos(angle)
                );

        double newLng =
                lngRad +
                        Math.atan2(
                                Math.sin(angle)
                                        * Math.sin(angularDistance)
                                        * Math.cos(latRad),

                                Math.cos(angularDistance)
                                        -
                                        Math.sin(latRad)
                                                * Math.sin(newLat)
                        );

        return new double[]{
                Math.toDegrees(newLat),
                Math.toDegrees(newLng)
        };
    }

    /**
     * Prevent lockers from stacking together
     */
    private boolean tooClose(
            double lat,
            double lng,
            List<Locker> lockers
    ) {

        for (Locker locker : lockers) {

            double distance =
                    calculateDistance(
                            lat,
                            lng,
                            locker.getLatitude(),
                            locker.getLongitude()
                    );

            if (distance < MIN_DISTANCE_BETWEEN_LOCKERS) {
                return true;
            }
        }

        return false;
    }

    /**
     * Haversine formula
     */
    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        double dLat =
                Math.toRadians(lat2 - lat1);

        double dLon =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2)
                        * Math.sin(dLat / 2)
                        +
                        Math.cos(Math.toRadians(lat1))
                                *
                                Math.cos(Math.toRadians(lat2))
                                *
                                Math.sin(dLon / 2)
                                *
                                Math.sin(dLon / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS * c;
    }
}