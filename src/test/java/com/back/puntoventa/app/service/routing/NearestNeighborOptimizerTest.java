package com.back.puntoventa.app.service.routing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NearestNeighborOptimizerTest {

    @Test
    void farAltaPriorityVisitedBeforeCloserBaja() {
        NearestNeighborOptimizer optimizer = new NearestNeighborOptimizer();

        // Start at 0,0
        RoutePoint closeBaja = new RoutePoint("CLOSE", 0.01, 0.0, "BAJA"); // ~1.11 km
        RoutePoint farAlta = new RoutePoint("FAR", 0.019, 0.0, "ALTA"); // ~2.11 km but weighted 0.5 -> ~1.05 km
        RoutePoint neutral = new RoutePoint("NEUTRAL", 0.04, 0.0, "BAJA");

        List<RoutePoint> pending = List.of(closeBaja, farAlta, neutral);

        List<RoutePoint> optimized = optimizer.optimizeRoute(0.0, 0.0, pending);

        // Expect FAR to be visited first because its weighted distance becomes smaller
        assertEquals("FAR", optimized.get(0).id());
    }

    @Test
    void emptyPendingPointsThrows() {
        NearestNeighborOptimizer optimizer = new NearestNeighborOptimizer();
        assertThrows(IllegalArgumentException.class, () -> optimizer.optimizeRoute(0.0, 0.0, List.of()));
    }
}
