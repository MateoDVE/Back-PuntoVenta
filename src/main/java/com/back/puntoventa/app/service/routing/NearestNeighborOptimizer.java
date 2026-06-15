package com.back.puntoventa.app.service.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class NearestNeighborOptimizer {

    private static final Logger log = LoggerFactory.getLogger(NearestNeighborOptimizer.class);

    public List<RoutePoint> optimizeRoute(double startLat, double startLng, List<RoutePoint> pendingPoints) {
        Objects.requireNonNull(pendingPoints, "pendingPoints must not be null");
        if (pendingPoints.isEmpty()) {
            throw new IllegalArgumentException("pendingPoints must not be empty");
        }

        List<RoutePoint> remaining = new ArrayList<>(pendingPoints);
        List<RoutePoint> result = new ArrayList<>(remaining.size());

        double currentLat = startLat;
        double currentLng = startLng;

        while (!remaining.isEmpty()) {
            RoutePoint nearest = null;
            double nearestWeightedDistance = Double.MAX_VALUE;
            double nearestRawDistance = Double.MAX_VALUE;

            for (RoutePoint p : remaining) {
                double rawDist = haversineKm(currentLat, currentLng, p.latitude(), p.longitude());
                double weight = weightForPriority(p.priority());
                double weighted = rawDist * weight;

                if (weighted < nearestWeightedDistance) {
                    nearestWeightedDistance = weighted;
                    nearestRawDistance = rawDist;
                    nearest = p;
                }
            }

            if (nearest == null) {
                break;
            }

            log.info("Moving from current location ({}, {}) to point {} at ({}, {}) — distance: {} km, weighted: {}",
                    currentLat, currentLng, nearest.id(), nearest.latitude(), nearest.longitude(), nearestRawDistance, nearestWeightedDistance);

            result.add(nearest);
            // update current
            currentLat = nearest.latitude();
            currentLng = nearest.longitude();

            // remove chosen
            Iterator<RoutePoint> it = remaining.iterator();
            while (it.hasNext()) {
                if (it.next().id().equals(nearest.id())) {
                    it.remove();
                    break;
                }
            }
        }

        return result;
    }

    private static double weightForPriority(String priority) {
        if (priority == null) return 1.0;
        return switch (priority.toUpperCase()) {
            case "ALTA" -> 0.5;
            case "MEDIA" -> 0.8;
            default -> 1.0;
        };
    }

    // Haversine formula to compute distance between two lat/lng points in kilometers
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
