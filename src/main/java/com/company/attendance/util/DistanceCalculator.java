package com.company.attendance.util;

public class DistanceCalculator {
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * Used everywhere in the system for consistent distance calculations
     */
    public static double distanceMeters(
            double lat1, double lon1,
            double lat2, double lon2) {

        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) {
            return Double.MAX_VALUE;
        }

        final double R = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    /**
     * Check if within task radius (200 meters)
     */
    public static boolean isWithinTaskRadius(double lat1, double lon1, double lat2, double lon2) {
        return distanceMeters(lat1, lon1, lat2, lon2) <= 200.0;
    }
    
    /**
     * Check if within idle detection radius (30 meters)
     */
    public static boolean isWithinIdleRadius(double lat1, double lon1, double lat2, double lon2) {
        return distanceMeters(lat1, lon1, lat2, lon2) <= 30.0;
    }
    
    /**
     * Format distance for display
     */
    public static String formatDistance(double meters) {
        if (meters >= 1000) {
            return String.format("%.1f km", meters / 1000);
        }
        return String.format("%.0f m", meters);
    }
}
