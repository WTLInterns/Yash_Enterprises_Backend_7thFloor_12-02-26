package com.company.attendance.util;

public class LocationUtils {
    private static final double EARTH_RADIUS = 6371000; // meters
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     */
    public static double distanceMeters(
            double lat1, double lon1,
            double lat2, double lon2) {
        
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) {
            return Double.MAX_VALUE;
        }
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
    
    /**
     * Check if employee is within allowed distance of customer
     */
    public static boolean isWithinCustomerRadius(
            double employeeLat, double employeeLng,
            double customerLat, double customerLng,
            double maxDistanceMeters) {
        
        double distance = distanceMeters(employeeLat, employeeLng, customerLat, customerLng);
        return distance <= maxDistanceMeters;
    }
    
    /**
     * Get distance in human readable format
     */
    public static String formatDistance(double meters) {
        if (meters >= 1000) {
            return String.format("%.1f km", meters / 1000);
        }
        return String.format("%.0f m", meters);
    }
}
