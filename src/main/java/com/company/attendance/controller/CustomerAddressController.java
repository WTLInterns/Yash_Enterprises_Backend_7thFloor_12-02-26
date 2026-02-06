package com.company.attendance.controller;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Customer Address Controller with auto-lock functionality
 * After edit → auto lock again
 */
@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerAddressController {

    private final CustomerAddressRepository customerAddressRepository;

    @GetMapping
    public ResponseEntity<?> listAddresses(
            @RequestParam Long clientId,
            @RequestParam(required = false, defaultValue = "false") boolean withCoordinates) {
        try {
            List<CustomerAddress> addresses = withCoordinates
                    ? customerAddressRepository.findWithCoordinatesByClientId(clientId)
                    : customerAddressRepository.findByClientIdOrderByAddressType(clientId);

            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Failed to list addresses for client {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch addresses"));
        }
    }

    /**
     * Update customer address
     * After edit → auto lock again (isEditable = false)
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @RequestBody Map<String, Object> updates) {
        
        try {
            CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Check if address is editable
            if (!address.getEditable()) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Address not editable",
                    "message", "Please submit an edit request first"
                ));
            }
            
            // Update address fields
            if (updates.containsKey("addressText")) {
                address.setAddressText((String) updates.get("addressText"));
            }
            if (updates.containsKey("latitude")) {
                address.setLatitude(((Number) updates.get("latitude")).doubleValue());
            }
            if (updates.containsKey("longitude")) {
                address.setLongitude(((Number) updates.get("longitude")).doubleValue());
            }
            
            // ✅ REQUIRED RULE: Lock address after edit
            address.setEditable(false);
            
            customerAddressRepository.save(address);
            
            log.info("Address {} updated and locked", addressId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Address updated successfully and locked"
            ));
            
        } catch (Exception e) {
            log.error("Failed to update address {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update address"));
        }
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddress(@PathVariable Long addressId) {
        try {
            CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            return ResponseEntity.ok(address);
            
        } catch (Exception e) {
            log.error("Failed to get address {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get address"));
        }
    }
}
