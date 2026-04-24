package com.company.attendance.controller;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.service.GeocodingService;
import com.company.attendance.exception.GeocodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Customer Address Controller with multi-address support
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerAddressController {

    private final CustomerAddressRepository customerAddressRepository;
    private final GeocodingService geocodingService;
    private final TaskRepository taskRepository;

    /**
     * Get ALL addresses for ALL customers in one call — used by the customers list page
     * to avoid N per-customer requests.
     */
    @GetMapping("/addresses/all")
    public ResponseEntity<List<CustomerAddress>> getAllAddresses() {
        return ResponseEntity.ok(customerAddressRepository.findAll());
    }

    /**
     * Get all addresses for a customer
     */
    @GetMapping("/{clientId}/addresses")
    public ResponseEntity<?> getCustomerAddresses(@PathVariable Long clientId) {
        try {
            List<CustomerAddress> addresses = customerAddressRepository.findByClientIdOrderByAddressType(clientId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Failed to fetch addresses for customer {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch addresses"));
        }
    }

    /**
     * Create or update customer addresses
     */
    @PostMapping("/{clientId}/addresses")
    @Transactional
    public ResponseEntity<?> saveCustomerAddresses(
            @PathVariable Long clientId,
            @RequestBody List<CustomerAddress> addresses) {
        try {
            // Validate: Exactly one primary address
            long primaryCount = addresses.stream()
                .filter(addr -> addr.getAddressType() == CustomerAddress.AddressType.PRIMARY)
                .count();
            
            if (primaryCount != 1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Exactly one primary address is required"
                ));
            }
            
            // Validate: Primary address must have coordinates
            CustomerAddress primaryAddress = addresses.stream()
                .filter(addr -> addr.getAddressType() == CustomerAddress.AddressType.PRIMARY)
                .findFirst()
                .orElse(null);
            
            if (primaryAddress != null && (primaryAddress.getLatitude() == null || primaryAddress.getLongitude() == null)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Primary address must have latitude and longitude"
                ));
            }

            List<CustomerAddress> existing = customerAddressRepository.findByClientIdOrderByAddressType(clientId);
            Set<Long> existingIds = new HashSet<>();
            for (CustomerAddress e : existing) {
                if (e.getId() != null) existingIds.add(e.getId());
            }

            Set<Long> incomingIds = new HashSet<>();
            for (CustomerAddress addr : addresses) {
                if (addr.getId() != null) incomingIds.add(addr.getId());
            }

            // Delete removed addresses only if not referenced by tasks
            for (CustomerAddress e : existing) {
                Long existingId = e.getId();
                if (existingId == null) continue;

                if (!incomingIds.contains(existingId)) {
                    if (taskRepository.existsByCustomerAddressId(existingId)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "error", "Cannot remove an address that is referenced by tasks",
                            "addressId", existingId
                        ));
                    }
                    customerAddressRepository.delete(e);
                }
            }

            // Upsert incoming addresses (update if id exists, else insert)
            for (CustomerAddress addr : addresses) {
                addr.setClientId(clientId);
                if (addr.getAddressType() != CustomerAddress.AddressType.PRIMARY) {
                    addr.setIsPrimary(false);
                }

                if (addr.getId() != null && existingIds.contains(addr.getId())) {
                    CustomerAddress db = customerAddressRepository.findById(addr.getId())
                        .orElseThrow(() -> new RuntimeException("Address not found"));

                    if (!Objects.equals(db.getClientId(), clientId)) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "Address does not belong to customer",
                            "addressId", addr.getId()
                        ));
                    }

                    db.setAddressType(addr.getAddressType());
                    db.setAddressLine(addr.getAddressLine());
                    db.setCity(addr.getCity());
                    db.setState(addr.getState());
                    db.setPincode(addr.getPincode());
                    db.setTaluka(addr.getTaluka());
                    db.setDistrict(addr.getDistrict());
                    db.setCountry(addr.getCountry());
                    db.setLatitude(addr.getLatitude());
                    db.setLongitude(addr.getLongitude());
                    db.setIsPrimary(addr.getIsPrimary());

                    customerAddressRepository.save(db);
                } else {
                    addr.setId(null);
                    customerAddressRepository.save(addr);
                }
            }

            List<CustomerAddress> savedAddresses = customerAddressRepository.findByClientIdOrderByAddressType(clientId);
            return ResponseEntity.ok(savedAddresses);
            
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to save addresses for customer {} due to data integrity violation: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Cannot save addresses due to existing references (tasks/deals/etc)",
                "details", e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to save addresses for customer {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save addresses"));
        }
    }

    /**
     * Update customer address
     */
    @PutMapping("/{clientId}/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long clientId,
            @PathVariable Long addressId,
            @RequestBody CustomerAddress addressUpdate) {
        try {
            CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify address belongs to customer
            if (!address.getClientId().equals(clientId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Address does not belong to customer"));
            }
            
            // Update fields
            if (addressUpdate.getAddressLine() != null) {
                address.setAddressLine(addressUpdate.getAddressLine());
            }
            if (addressUpdate.getCity() != null) {
                address.setCity(addressUpdate.getCity());
            }
            if (addressUpdate.getPincode() != null) {
                address.setPincode(addressUpdate.getPincode());
            }
            if (addressUpdate.getLatitude() != null) {
                address.setLatitude(addressUpdate.getLatitude());
            }
            if (addressUpdate.getLongitude() != null) {
                address.setLongitude(addressUpdate.getLongitude());
            }
            
            CustomerAddress savedAddress = customerAddressRepository.save(address);
            return ResponseEntity.ok(savedAddress);
            
        } catch (Exception e) {
            log.error("Failed to update address {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update address"));
        }
    }

    /**
     * Delete customer address
     */
    @DeleteMapping("/{clientId}/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long clientId,
            @PathVariable Long addressId) {
        try {
            CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify address belongs to customer
            if (!address.getClientId().equals(clientId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Address does not belong to customer"));
            }
            
            // Prevent deleting primary address
            if (address.getIsPrimary() != null && address.getIsPrimary()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete primary address"));
            }

            if (taskRepository.existsByCustomerAddressId(addressId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Cannot delete an address that is referenced by tasks",
                    "addressId", addressId
                ));
            }
            
            customerAddressRepository.delete(address);
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete address {} due to data integrity violation: {}", addressId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Cannot delete address due to existing references",
                "addressId", addressId
            ));
        } catch (Exception e) {
            log.error("Failed to delete address {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete address"));
        }
    }

    /**
     * Geocode address to get latitude/longitude
     */
    @PostMapping("/geocode")
    public ResponseEntity<?> geocodeAddress(@RequestBody Map<String, String> request) {
        try {
            String addressLine = request.get("addressLine");
            String city = request.get("city");
            String pincode = request.get("pincode");
            String state = request.get("state");
            String country = request.get("country");

            // Basic validation
            if ((addressLine == null || addressLine.isBlank()) && 
                (city == null || city.isBlank())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Address line or city is required", "errorCode", "VALIDATION_ERROR"));
            }

            // Build full address
            String fullAddress = geocodingService.buildFullAddress(
                addressLine, city, pincode, state, country
            );

            log.info("Geocoding address: {}", fullAddress);
            GeocodingService.LatLng coordinates = geocodingService.geocodeAddress(fullAddress);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("address", fullAddress);
            
            if (coordinates != null) {
                response.put("success", true);
                response.put("latitude", coordinates.getLat());
                response.put("longitude", coordinates.getLng());
                log.info("Successfully geocoded '{}' to lat: {}, lon: {}", fullAddress, coordinates.getLat(), coordinates.getLng());
                return ResponseEntity.ok(response);
            }
            
            // No coordinates found
            response.put("success", false);
            response.put("message", "Unable to determine coordinates for this address");
            response.put("latitude", null);
            response.put("longitude", null);
            log.warn("Failed to geocode address: {}", fullAddress);
            return ResponseEntity.ok(response);
            
        } catch (GeocodingException e) {
            log.error("Geocoding failed: {}", e.getMessage(), e);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("latitude", null);
            response.put("longitude", null);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(response);
        } catch (Exception e) {
            log.error("Unexpected error in geocode: {}", e.getMessage(), e);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Geocoding service temporarily unavailable");
            response.put("latitude", null);
            response.put("longitude", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }

    /**
     * Reverse geocode to get address from coordinates
     */
    @PostMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestBody Map<String, Double> request) {
        try {
            Double latitude = request.get("latitude");
            Double longitude = request.get("longitude");
            
            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Latitude and longitude are required")
                );
            }

            String address = geocodingService.reverseGeocode(latitude, longitude);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            
            if (address == null) {
                response.put("success", false);
                response.put("message", "Unable to determine address for these coordinates");
                response.put("address", null);
                return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("address", address);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Reverse geocode failed", e);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Reverse geocoding service unavailable");
            return ResponseEntity.ok(response);
        }
    }
}
