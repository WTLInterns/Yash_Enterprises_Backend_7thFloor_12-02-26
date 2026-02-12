package com.company.attendance.integration;

import com.company.attendance.service.GeocodingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GeocodingIntegrationTest {

    @Autowired
    private GeocodingService geocodingService;

    @Test
    public void testGeocodeServiceBasicFunctionality() {
        // Test that service doesn't crash with valid input
        assertDoesNotThrow(() -> {
            geocodingService.geocodeAddress("123 Main St, Bangalore");
        });
    }

    @Test
    public void testGeocodeEmptyAddress() {
        // Test that empty address returns null
        GeocodingService.LatLng result = geocodingService.geocodeAddress("");
        assertNull(result);
    }

    @Test
    public void testGeocodeNullAddress() {
        // Test that null address returns null
        GeocodingService.LatLng result = geocodingService.geocodeAddress(null);
        assertNull(result);
    }
}
