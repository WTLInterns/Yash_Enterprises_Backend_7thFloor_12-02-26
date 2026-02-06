package com.company.attendance.controller;

import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.CustomerAddressEditRequest;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.CustomerAddressEditRequestRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer-address-edit-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerAddressEditController {

    private final CustomerAddressEditRequestRepository editRequestRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Get all address edit requests with optional status filter
     */
    @GetMapping
    public ResponseEntity<?> getAddressEditRequests(
            @RequestParam(required = false) String status) {
        
        try {
            List<CustomerAddressEditRequest> requests;
            
            if (status != null && !status.isEmpty()) {
                CustomerAddressEditRequest.RequestStatus requestStatus = 
                    CustomerAddressEditRequest.RequestStatus.valueOf(status.toUpperCase());
                requests = editRequestRepository.findByStatus(requestStatus);
            } else {
                requests = editRequestRepository.findAll();
            }
            
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            log.error("Failed to fetch address edit requests: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch requests"));
        }
    }

    /**
     * Create new address edit request
     */
    @PostMapping
    public ResponseEntity<?> createEditRequest(
            @RequestBody Map<String, Object> request,
            @RequestParam Long employeeId) {
        
        try {
            Long addressId = Long.valueOf(request.get("addressId").toString());
            
            // Check if address exists
            Optional<CustomerAddress> addressOpt = customerAddressRepository.findById(addressId);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer address not found"));
            }
            
            // Check if employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Employee not found"));
            }
            
            // Check if there's already a pending request for this address
            Optional<CustomerAddressEditRequest> existingRequest = 
                editRequestRepository.findByAddressIdAndStatus(addressId, CustomerAddressEditRequest.RequestStatus.PENDING);
            
            if (existingRequest.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pending request already exists for this address"));
            }
            
            // Create new edit request
            CustomerAddressEditRequest editRequest = new CustomerAddressEditRequest();
            editRequest.setAddressId(addressId);
            editRequest.setRequestedByEmployeeId(employeeId);
            editRequest.setStatus(CustomerAddressEditRequest.RequestStatus.PENDING);
            editRequest.setCreatedAt(LocalDateTime.now());
            
            editRequest = editRequestRepository.save(editRequest);
            
            log.info("Address edit request created: {} by employee {}", editRequest.getId(), employeeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "requestId", editRequest.getId(),
                "message", "Edit request submitted successfully"
            ));
            
        } catch (Exception e) {
            log.error("Failed to create edit request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create request"));
        }
    }

    /**
     * Approve address edit request
     */
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long approvedBy) {
        
        try {
            Optional<CustomerAddressEditRequest> requestOpt = editRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CustomerAddressEditRequest request = requestOpt.get();
            
            if (request.getStatus() != CustomerAddressEditRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request is not pending"));
            }
            
            // Update request status
            request.setStatus(CustomerAddressEditRequest.RequestStatus.APPROVED);
            request.setApprovedBy(approvedBy);
            request.setApprovedAt(LocalDateTime.now());
            
            editRequestRepository.save(request);
            
            // On APPROVE - Backend: customerAddress.isEditable = true
            Optional<CustomerAddress> addressOpt = customerAddressRepository.findById(request.getAddressId());
            if (addressOpt.isPresent()) {
                CustomerAddress address = addressOpt.get();
                address.setEditable(true);
                customerAddressRepository.save(address);
                
                log.info("Address {} marked as editable after request approval", address.getId());
            }
            
            log.info("Address edit request {} approved", requestId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Edit request approved successfully"
            ));
            
        } catch (Exception e) {
            log.error("Failed to approve request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to approve request"));
        }
    }

    /**
     * Reject address edit request
     */
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) Long approvedBy) {
        
        try {
            Optional<CustomerAddressEditRequest> requestOpt = editRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CustomerAddressEditRequest request = requestOpt.get();
            
            if (request.getStatus() != CustomerAddressEditRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request is not pending"));
            }
            
            // Update request status
            request.setStatus(CustomerAddressEditRequest.RequestStatus.REJECTED);
            request.setApprovedBy(approvedBy);
            request.setApprovedAt(LocalDateTime.now());
            
            editRequestRepository.save(request);
            
            log.info("Address edit request {} rejected", requestId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Edit request rejected"
            ));
            
        } catch (Exception e) {
            log.error("Failed to reject request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to reject request"));
        }
    }

    /**
     * Get address edit request by ID
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestById(@PathVariable Long requestId) {
        try {
            Optional<CustomerAddressEditRequest> requestOpt = editRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(requestOpt.get());
            
        } catch (Exception e) {
            log.error("Failed to fetch request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch request"));
        }
    }
}
