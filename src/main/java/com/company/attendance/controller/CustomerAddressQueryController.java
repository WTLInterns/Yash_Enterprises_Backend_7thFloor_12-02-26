package com.company.attendance.controller;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerAddressQueryController {

    private final CustomerAddressRepository customerAddressRepository;

    /**
     * REQUIRED for mobile task screen
     * GET /api/customer-addresses/{id}
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddressById(@PathVariable Long addressId) {

        return customerAddressRepository.findById(addressId)
                .map(address -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("id", address.getId());
                    response.put("address", address.getAddressLine());
                    response.put("latitude", address.getLatitude());
                    response.put("longitude", address.getLongitude());
                    response.put("city", address.getCity());
                    response.put("state", address.getState());
                    response.put("pincode", address.getPincode());
                    response.put("country", address.getCountry());
                    response.put("addressType", address.getAddressType());
                    response.put("isPrimary", address.getIsPrimary());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
