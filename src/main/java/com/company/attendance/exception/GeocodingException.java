package com.company.attendance.exception;

/**
 * Exception thrown when geocoding operations fail
 */
public class GeocodingException extends RuntimeException {
    public GeocodingException(String message) {
        super(message);
    }
    
    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
