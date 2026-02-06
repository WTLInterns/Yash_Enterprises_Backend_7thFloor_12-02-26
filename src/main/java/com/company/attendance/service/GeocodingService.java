package com.company.attendance.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class GeocodingService {

    @Value("${geocoding.api.provider:nominatim}")
    private String provider;

    @Value("${geocoding.api.key:}")
    private String apiKey;

    @Value("${geocoding.api.url:https://nominatim.openstreetmap.org/search}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    public static class LatLng {
        private final double lat;
        private final double lng;

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    public String reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        try {
            if (!"nominatim".equalsIgnoreCase(provider)) {
                return null;
            }

            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%s&lon=%s",
                    latitude,
                    longitude
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "attendance-backend/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return null;
            }

            if (body.contains("\"display_name\"")) {
                String[] parts = body.split("\\\"display_name\\\"\\s*:\\s*\\\"");
                if (parts.length > 1) {
                    return parts[1].split("\\\"")[0];
                }
            }

            return null;
        } catch (Exception e) {
            log.debug("Reverse geocode failed for lat={}, lng={}", latitude, longitude, e);
            return null;
        }
    }

    /**
     * Geocode address to get latitude and longitude
     * Uses OpenStreetMap Nominatim (free) by default
     */
    public LatLng geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Cannot geocode empty address");
            return null;
        }

        try {
            log.debug("Geocoding address: {}", address);
            
            if ("nominatim".equalsIgnoreCase(provider)) {
                return geocodeWithNominatim(address);
            } else if ("google".equalsIgnoreCase(provider)) {
                return geocodeWithGoogle(address);
            } else {
                log.warn("Unsupported geocoding provider: {}", provider);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to geocode address: {}", address, e);
            return null;
        }
    }

    private LatLng geocodeWithNominatim(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("%s?format=json&q=%s&limit=1", apiUrl, encodedAddress);
            
            log.debug("Calling Nominatim API: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            if (response != null && !response.contains("[]")) {
                // Parse JSON response (simplified - in production use proper JSON parser)
                if (response.contains("\"lat\"") && response.contains("\"lon\"")) {
                    String latStr = response.split("\"lat\":\"")[1].split("\"")[0];
                    String lonStr = response.split("\"lon\":\"")[1].split("\"")[0];
                    
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lonStr);
                    
                    log.debug("Geocoded {} to lat: {}, lng: {}", address, lat, lng);
                    return new LatLng(lat, lng);
                }
            }
            
            log.warn("No results found for address: {}", address);
            return null;
        } catch (Exception e) {
            log.error("Error geocoding with Nominatim: {}", address, e);
            return null;
        }
    }

    private LatLng geocodeWithGoogle(String address) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Google Maps API key not configured");
            return null;
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s", 
                                    encodedAddress, apiKey);
            
            log.debug("Calling Google Geocoding API");
            
            String response = restTemplate.getForObject(url, String.class);
            if (response != null && response.contains("\"status\":\"OK\"")) {
                // Parse JSON response (simplified - in production use proper JSON parser)
                if (response.contains("\"location\"")) {
                    String latStr = response.split("\"lat\"")[1].split(":")[1].split(",")[0].trim();
                    String lngStr = response.split("\"lng\"")[1].split(":")[1].split("}")[0].trim();
                    
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    
                    log.debug("Geocoded {} to lat: {}, lng: {}", address, lat, lng);
                    return new LatLng(lat, lng);
                }
            }
            
            log.warn("Google Geocoding failed for address: {}", address);
            return null;
        } catch (Exception e) {
            log.error("Error geocoding with Google: {}", address, e);
            return null;
        }
    }

    /**
     * Build full address string from components
     */
    public String buildFullAddress(String address, String city, String pincode, String state, String country) {
        StringBuilder fullAddress = new StringBuilder();
        
        if (address != null && !address.trim().isEmpty()) {
            fullAddress.append(address.trim());
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city.trim());
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(state.trim());
        }
        
        if (pincode != null && !pincode.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(pincode.trim());
        }
        
        if (country != null && !country.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country.trim());
        }
        
        return fullAddress.toString();
    }
}
