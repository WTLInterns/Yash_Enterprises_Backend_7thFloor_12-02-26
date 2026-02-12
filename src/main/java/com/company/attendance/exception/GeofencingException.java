package com.company.attendance.exception;

/**
 * Exception thrown when geofencing restrictions are violated
 */
public class GeofencingException extends RuntimeException {
    public GeofencingException(String message) {
        super(message);
    }
}
