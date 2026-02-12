package com.company.attendance.service;

import com.company.attendance.exception.GeocodingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class GeocodingService {

    @Value("${geocoding.api.provider:google}")
    private String provider;

    @Value("${google.maps.api.key:}")
    private String apiKey;

    @Value("${geocoding.api.url:https://maps.googleapis.com/maps/api/geocode/json}")
    private String apiUrl;
    
    @Value("${geocoding.timeout:5000}")
    private int timeoutMs;
    
    @Value("${geocoding.retry.attempts:3}")
    private int maxRetries;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Constructor for dependency injection
    public GeocodingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Using default timeout configuration for simplicity
    }

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
            log.debug("Reverse geocoding lat={}, lng={}", latitude, longitude);
            
            if ("google".equalsIgnoreCase(provider)) {
                return reverseGeocodeWithGoogle(latitude, longitude);
            } else if ("nominatim".equalsIgnoreCase(provider)) {
                return reverseGeocodeWithNominatim(latitude, longitude);
            } else {
                log.warn("Unsupported geocoding provider: {}", provider);
                return null;
            }

        } catch (Exception e) {
            log.error("Reverse geocode failed for lat={}, lng={}", latitude, longitude, e);
            return null;
        }
    }

    private String reverseGeocodeWithGoogle(Double latitude, Double longitude) {
        try {
            String url = String.format("%s?latlng=%s,%s&key=%s", apiUrl, latitude, longitude, apiKey);
            
            log.debug("Calling Google Reverse Geocoding API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            
            if (body == null || !response.getStatusCode().is2xxSuccessful()) {
                log.warn("Google Reverse Geocoding API request failed with status: {}", response.getStatusCode());
                return null;
            }

            // Parse JSON response properly
            JsonNode root = objectMapper.readTree(body);
            
            // Check if Google API returned OK status
            if (!root.has("status") || !"OK".equals(root.get("status").asText())) {
                log.warn("Google Reverse Geocoding API returned status: {}", root.has("status") ? root.get("status").asText() : "unknown");
                return null;
            }
            
            // Check if results array exists and has elements
            if (!root.has("results") || !root.get("results").isArray() || root.get("results").size() == 0) {
                log.warn("No results found in Google Reverse Geocoding response");
                return null;
            }
            
            // Extract formatted address from first result
            JsonNode firstResult = root.get("results").get(0);
            if (!firstResult.has("formatted_address")) {
                log.warn("Invalid response structure from Google Reverse Geocoding API");
                return null;
            }
            
            String formattedAddress = firstResult.get("formatted_address").asText();
            log.debug("Successfully reverse geocoded lat={}, lng={} to: {}", latitude, longitude, formattedAddress);
            return formattedAddress;
            
        } catch (Exception e) {
            log.error("Error reverse geocoding with Google: lat={}, lng={}", latitude, longitude, e);
            return null;
        }
    }

    private String reverseGeocodeWithNominatim(Double latitude, Double longitude) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("format", "jsonv2")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "AttendanceCRM/1.0 (admin@company.com)");
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return null;
            }

            // ✅ FIXED: Proper JSON parsing
            JsonNode root = objectMapper.readTree(body);
            
            if (root.has("display_name")) {
                return root.get("display_name").asText();
            }

            return null;

        } catch (Exception e) {
            log.error("Nominatim reverse geocoding failed for lat={}, lng={}", latitude, longitude, e);
            return null;
        }
    }

    /**
     * Geocode address to get latitude and longitude
     * Uses Google Geocoding API by default
     */
    public LatLng geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("Cannot geocode empty address");
            return null;
        }

        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Geocoding address: {}", address);
            
            LatLng result;
            if ("google".equalsIgnoreCase(provider)) {
                result = geocodeWithGoogle(address);
            } else if ("nominatim".equalsIgnoreCase(provider)) {
                result = geocodeWithNominatim(address);
            } else {
                log.warn("Unsupported geocoding provider: {}", provider);
                result = null;
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("Geocoding completed in {}ms - Result: {}", responseTime, result != null ? "SUCCESS" : "FAILED");
            
            return result;
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Geocoding failed after {}ms for address: {}", responseTime, address, e);
            return null;
        }
    }

    private LatLng geocodeWithNominatim(String address) {
        try {
            // Respect Nominatim rate limit
            Thread.sleep(1100);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "AttendanceCRM/1.0 (contact: admin@company.com)");
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Build URL correctly (let Spring encode ONCE)
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiUrl)
                    .queryParam("format", "json")
                    .queryParam("q", address)   // ✅ NOT encoded manually
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            log.debug("Calling Nominatim API: {}", url);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            if (body == null || body.equals("[]")) {
                return null;
            }

            // ✅ FIXED: Proper JSON parsing instead of string splitting
            JsonNode root = objectMapper.readTree(body);
            
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                
                // Nominatim returns "lat" and "lon" fields
                JsonNode latNode = first.get("lat");
                JsonNode lonNode = first.get("lon");
                
                if (latNode != null && lonNode != null) {
                    double lat = latNode.asDouble();
                    double lon = lonNode.asDouble();
                    
                    log.debug("Successfully geocoded '{}' to lat: {}, lon: {}", address, lat, lon);
                    return new LatLng(lat, lon);
                }
            }
            
            log.warn("No coordinates found in Nominatim response for address: {}", address);
            return null;

        } catch (Exception e) {
            log.error("Nominatim geocoding failed for address: {}", address, e);
            return null;
        }
    }

    private LatLng geocodeWithGoogle(String address) {
        return geocodeWithSimpleRetry(address);
    }
    
    // Simple blocking retry - RELIABLE & SIMPLE
    private LatLng geocodeWithSimpleRetry(String address) {
        GeocodingException lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return geocodeAddressInternal(address);
            } catch (Exception e) {
                lastException = new GeocodingException("Geocoding failed on attempt " + attempt + ": " + e.getMessage(), e);
                log.warn("Geocoding attempt {} failed for address: {}", attempt, address);
                
                if (attempt < maxRetries) {
                    // Simple blocking delay - RELIABLE
                    try {
                        Thread.sleep(1000L * attempt); // 1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new GeocodingException("Geocoding interrupted", ie);
                    }
                }
            }
        }
        
        throw lastException;
    }
    
    // Internal geocoding method with improved timeout handling
    private LatLng geocodeAddressInternal(String address) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new GeocodingException("Google Maps API key not configured");
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("%s?address=%s&key=%s", apiUrl, encodedAddress, apiKey);
            
            log.debug("Calling Google Geocoding API for address: {}", address);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            // Use configured timeout (already set in constructor)
            // No need to recreate factory for each request
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            
            if (body == null || !response.getStatusCode().is2xxSuccessful()) {
                throw new GeocodingException("Google API returned status: " + response.getStatusCode());
            }

            // Parse JSON response properly
            JsonNode root = objectMapper.readTree(body);
            
            // Check if Google API returned OK status
            if (!root.has("status") || !"OK".equals(root.get("status").asText())) {
                String statusText = root.has("status") ? root.get("status").asText() : "unknown";
                throw new GeocodingException("Google API returned status: " + statusText);
            }
            
            // Check if results array exists and has elements
            if (!root.has("results") || !root.get("results").isArray() || root.get("results").size() == 0) {
                throw new GeocodingException("No results found for address: " + address);
            }
            
            // Extract coordinates from first result
            JsonNode firstResult = root.get("results").get(0);
            if (!firstResult.has("geometry") || !firstResult.get("geometry").has("location")) {
                throw new GeocodingException("Invalid response structure from Google API");
            }
            
            JsonNode location = firstResult.get("geometry").get("location");
            if (!location.has("lat") || !location.has("lng")) {
                throw new GeocodingException("Missing coordinates in Google API response");
            }
            
            double lat = location.get("lat").asDouble();
            double lng = location.get("lng").asDouble();
            
            log.debug("Successfully geocoded '{}' to lat: {}, lon: {}", address, lat, lng);
            return new LatLng(lat, lng);
            
        } catch (RestClientException e) {
            throw new GeocodingException("Network error during geocoding: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new GeocodingException("Failed to parse geocoding response: " + e.getMessage(), e);
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
        
        // ✅ FIXED: Only add country if provided (don't hardcode India)
        if (country != null && !country.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country.trim());
        }
        
        return fullAddress.toString();
    }
}
