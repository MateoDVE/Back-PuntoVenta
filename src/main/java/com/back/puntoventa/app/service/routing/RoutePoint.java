package com.back.puntoventa.app.service.routing;

/**
 * Domain DTO representing a point/node for route optimization.
 */
public record RoutePoint(String id, double latitude, double longitude, String priority) {
    public RoutePoint {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        if (priority == null) {
            throw new IllegalArgumentException("priority must not be null");
        }
    }
}
