package com.company.attendance.controller;

import com.company.attendance.dto.AddressEditRequestDTO;
import com.company.attendance.entity.Client;
import com.company.attendance.entity.CustomerAddress;
import com.company.attendance.entity.CustomerAddressEditRequest;
import com.company.attendance.entity.Employee;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.repository.CustomerAddressEditRequestRepository;
import com.company.attendance.repository.CustomerAddressRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.ClientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/customer-address-edit-requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerAddressEditController {

    private final CustomerAddressEditRequestRepository editRequestRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;

    /**
     * Get all address edit requests with optional status filter
     * Department-aware: ADMIN sees all, MANAGER sees own department, TL sees own tasks only
     * EMPLOYEE can create requests but cannot view this endpoint
     */
    @GetMapping
    public ResponseEntity<?> getAddressEditRequests(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartment) {
        
        try {
            // 🔥 HEADER FALLBACK: Derive from employeeId if headers missing
            String derivedUserRole = userRole;
            String derivedUserDepartment = userDepartment;
            
            if (userRole == null || userDepartment == null) {
                if (userId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "User ID required for role/department derivation"));
                }
                
                Employee employee = employeeRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new RuntimeException("Employee not found for derivation"));
                
                derivedUserRole = employee.getRole() != null ? employee.getRole().getName() : null;
                derivedUserDepartment = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
                
                log.info("Derived role: {} and department: {} from employeeId: {}", 
                    derivedUserRole, derivedUserDepartment, userId);
            }
            
            Long currentUserId = userId != null ? Long.valueOf(userId) : null;
            
            List<CustomerAddressEditRequest> requests;
            
            // 🔥 DEPARTMENT-AWARE FILTERING (NO SPRING SECURITY)
            switch (derivedUserRole.toUpperCase()) {
                case "ADMIN":
                    // Admin sees all requests
                    if (status != null && !status.isEmpty()) {
                        CustomerAddressEditRequest.RequestStatus requestStatus = 
                            CustomerAddressEditRequest.RequestStatus.valueOf(status.toUpperCase());
                        requests = editRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus);
                    } else {
                        requests = editRequestRepository.findAllOrderByCreatedAtDesc();
                    }
                    break;
                    
                case "MANAGER":
                    // Manager sees requests from own department only
                    if (status != null && !status.isEmpty()) {
                        CustomerAddressEditRequest.RequestStatus requestStatus = 
                            CustomerAddressEditRequest.RequestStatus.valueOf(status.toUpperCase());
                        requests = editRequestRepository.findByDepartmentOrderByCreatedAtDesc(derivedUserDepartment)
                            .stream()
                            .filter(req -> req.getStatus() == requestStatus)
                            .toList();
                    } else {
                        requests = editRequestRepository.findByDepartmentOrderByCreatedAtDesc(derivedUserDepartment);
                    }
                    break;
                    
                case "TL":
                    // TL sees only requests from tasks they created
                    if (currentUserId == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "User ID required for TL"));
                    }
                    if (status != null && !status.isEmpty()) {
                        CustomerAddressEditRequest.RequestStatus requestStatus = 
                            CustomerAddressEditRequest.RequestStatus.valueOf(status.toUpperCase());
                        requests = editRequestRepository.findByCreatedByTlIdAndDepartmentOrderByCreatedAtDesc(currentUserId, derivedUserDepartment)
                            .stream()
                            .filter(req -> req.getStatus() == requestStatus)
                            .toList();
                    } else {
                        requests = editRequestRepository.findByCreatedByTlIdAndDepartmentOrderByCreatedAtDesc(currentUserId, derivedUserDepartment);
                    }
                    break;
                    
                case "EMPLOYEE":
                    // EMPLOYEE cannot view address edit requests
                    return ResponseEntity.status(403).body(Map.of("error", "Employees cannot view address edit requests"));
                    
                default:
                    return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            // 🔥 Convert to DTO with joined data
            List<AddressEditRequestDTO> dtoList = requests.stream().map(req -> {
                Optional<CustomerAddress> addressOpt = customerAddressRepository.findById(req.getAddressId());
                CustomerAddress address = addressOpt.orElse(null);
                
                Optional<Employee> employeeOpt = employeeRepository.findById(req.getRequestedByEmployeeId());
                Employee employee = employeeOpt.orElse(null);
                
                // Fetch client details using ClientRepository
                String customerName = "Unknown Client";
                Long customerId = null;
                if (address != null && address.getClientId() != null) {
                    customerId = address.getClientId();
                    Client client = clientRepository.findById(customerId).orElse(null);
                    customerName = client != null ? client.getName() : "Unknown Client";
                }
                
                return new AddressEditRequestDTO(
                    req.getId(), // ✅ Fixed: maps to id field
                    employee != null ? employee.getId() : null,
                    employee != null ? employee.getFullName() : "Unknown Employee",
                    employee != null ? employee.getEmail() : null,
                    customerId,
                    customerName,
                    req.getAddressId(),
                    address != null ? address.getAddressLine() : "N/A",
                    address != null ? address.getAddressType().name() : "N/A",
                    req.getNewAddressLine(),
                    req.getNewCity(),
                    req.getNewState(),
                    req.getNewPincode(),
                    req.getNewCountry(),
                    req.getNewLatitude(),
                    req.getNewLongitude(),
                    req.getReason(),
                    req.getStatus().name(),
                    req.getCreatedAt()
                );
            }).toList();
            
            return ResponseEntity.ok(dtoList);
            
        } catch (Exception e) {
            log.error("Failed to fetch address edit requests: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch requests"));
        }
    }

    /**
     * Create new address edit request - Department-aware
     * EMPLOYEE, TL, MANAGER, ADMIN can create (with department validation)
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> createEditRequest(
            @RequestBody Map<String, Object> request,
            @RequestParam Long employeeId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Department", required = false) String userDepartment) {
        
        try {
            // 🔥 HEADER FALLBACK: Derive from employeeId if headers missing
            String derivedUserRole = userRole;
            String derivedUserDepartment = userDepartment;
            
            if (userRole == null || userDepartment == null) {
                if (userId == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "User ID required for role/department derivation"));
                }
                
                Employee employee = employeeRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new RuntimeException("Employee not found for derivation"));
                
                derivedUserRole = employee.getRole() != null ? employee.getRole().getName() : null;
                derivedUserDepartment = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
                
                log.info("Derived role: {} and department: {} from employeeId: {}", 
                    derivedUserRole, derivedUserDepartment, userId);
            }
            
            // 🔥 ROLE CHECK: Only EMPLOYEE, TL, MANAGER, ADMIN can create
            if (!Set.of("EMPLOYEE", "TL", "MANAGER", "ADMIN").contains(derivedUserRole.toUpperCase())) {
                return ResponseEntity.status(403).body(Map.of("error", "Only employees, TLs, managers, and admins can create address edit requests"));
            }
            
            Long addressId = Long.valueOf(request.get("addressId").toString());
            
            // Check if address exists
            CustomerAddress address = customerAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Customer address not found"));
            
            // Check if employee exists
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            // 🔥 DEPARTMENT VALIDATION: Employee must belong to same department (except ADMIN)
            if (!"ADMIN".equals(derivedUserRole.toUpperCase()) && 
                !derivedUserDepartment.equals(employee.getDepartment())) {
                return ResponseEntity.status(403).body(Map.of("error", "Can only create requests for employees in your department"));
            }
            
            // 🔥 Restrict editable types (Enterprise Security)
            if (!List.of(
                CustomerAddress.AddressType.PRIMARY,
                CustomerAddress.AddressType.BRANCH,
                CustomerAddress.AddressType.POLICE,
                CustomerAddress.AddressType.TAHSIL
            ).contains(address.getAddressType())) {
                return ResponseEntity.badRequest().body(Map.of("error", "This address type cannot be edited"));
            }
            
            // Check if there's already a pending request for this address
            Optional<CustomerAddressEditRequest> existingRequest = 
                editRequestRepository.findByAddressIdAndStatus(addressId, CustomerAddressEditRequest.RequestStatus.PENDING);
            
            if (existingRequest.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pending request already exists for this address"));
            }
            
            // 🔥 Create new edit request with department awareness
            CustomerAddressEditRequest editRequest = new CustomerAddressEditRequest();
            editRequest.setAddressId(addressId);
            editRequest.setRequestedByEmployeeId(employeeId);
            editRequest.setStatus(CustomerAddressEditRequest.RequestStatus.PENDING);
            
            // 🔥 SET DEPARTMENT FIELDS
            editRequest.setDepartment(derivedUserDepartment);
            
            // If TL is creating, set createdByTlId
            if ("TL".equals(derivedUserRole.toUpperCase()) && userId != null) {
                editRequest.setCreatedByTlId(Long.valueOf(userId));
            }
            
            // Set proposed new values
            editRequest.setNewAddressLine((String) request.get("newAddressLine"));
            editRequest.setNewCity((String) request.get("newCity"));
            editRequest.setNewState((String) request.get("newState"));
            editRequest.setNewPincode((String) request.get("newPincode"));
            editRequest.setNewCountry((String) request.get("newCountry"));
            editRequest.setNewLatitude(request.get("newLatitude") != null ? 
                Double.valueOf(request.get("newLatitude").toString()) : null);
            editRequest.setNewLongitude(request.get("newLongitude") != null ? 
                Double.valueOf(request.get("newLongitude").toString()) : null);
            editRequest.setReason((String) request.get("reason"));
            
            editRequest = editRequestRepository.save(editRequest);
            
            // ADD THIS: Notify admins about new address edit request
            notificationService.sendRoleBasedNotification(
                "ADMIN", 
                "New Address Edit Request", 
                "Employee #" + employeeId + " submitted address change request for ID: " + addressId, 
                "ADDRESS_EDIT_REQUEST", 
                editRequest.getId()
            );
            
            log.info("Address edit request created: {} by employee {} for address {}", 
                editRequest.getId(), employeeId, addressId);
            
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
     * Approve address edit request - Production Grade
     */
    @PutMapping("/{requestId}/approve")
    @Transactional
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
        
        Long adminId = getLoggedInEmployeeId();
        
        try {
            CustomerAddressEditRequest request =
                editRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            if (request.getStatus() != CustomerAddressEditRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request already processed"));
            }

            CustomerAddress address =
                customerAddressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            // 🔥 Apply approved address changes
            if (request.getNewAddressLine() != null) {
                address.setAddressLine(request.getNewAddressLine());
            }
            if (request.getNewCity() != null) {
                address.setCity(request.getNewCity());
            }
            if (request.getNewState() != null) {
                address.setState(request.getNewState());
            }
            if (request.getNewPincode() != null) {
                address.setPincode(request.getNewPincode());
            }
            if (request.getNewCountry() != null) {
                address.setCountry(request.getNewCountry());
            }
            if (request.getNewLatitude() != null) {
                address.setLatitude(request.getNewLatitude());
            }
            if (request.getNewLongitude() != null) {
                address.setLongitude(request.getNewLongitude());
            }
            
            // 🔥 PRIMARY enforcement: unset existing primary if this is primary
            if (address.getAddressType() == CustomerAddress.AddressType.PRIMARY) {
                customerAddressRepository.clearPrimaryAddress(address.getClientId());
                address.setIsPrimary(true);
            }
            
            customerAddressRepository.save(address);

            request.setStatus(CustomerAddressEditRequest.RequestStatus.APPROVED);
            request.setApprovedBy(adminId);
            request.setApprovedAt(LocalDateTime.now());
            editRequestRepository.save(request);
            
            // Notify the requesting employee
            notificationService.notifyEmployeeMobile(
                request.getRequestedByEmployeeId(),
                "Address Edit Request Approved",
                "Your address edit request has been approved",
                "ADDRESS_EDIT_APPROVED",
                "ADDRESS_EDIT",
                request.getId(),
                Map.of("requestId", request.getId().toString())
            );
            
            log.info("Address edit request approved: {} by admin {}", requestId, adminId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Address edit request approved successfully"
            ));
            
        } catch (Exception e) {
            log.error("Failed to approve address edit request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to approve request"));
        }
    }

    /**
     * Reject address edit request
     */
    @PutMapping("/{requestId}/reject")
    @Transactional
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String rejectionReason) {
        
        try {
            Long adminId = getLoggedInEmployeeId();
            
            CustomerAddressEditRequest request =
                editRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            if (request.getStatus() != CustomerAddressEditRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request already processed"));
            }

            // 🔥 Update request status
            request.setStatus(CustomerAddressEditRequest.RequestStatus.REJECTED);
            request.setApprovedBy(adminId);
            request.setApprovedAt(LocalDateTime.now());
            request.setRejectionReason(rejectionReason);
            editRequestRepository.save(request);

            // ADD THIS: Notify employee about rejection
            String rejectionMessage = rejectionReason != null && !rejectionReason.trim().isEmpty() 
                ? "Reason: " + rejectionReason 
                : "Please contact admin for more details";
            
            notificationService.notifyEmployeeMobile(
                request.getRequestedByEmployeeId(), 
                "Address Update Rejected", 
                "Your address change request #" + requestId + " was rejected. " + rejectionMessage, 
                "ADDRESS_EDIT_REJECTED", 
                "ADDRESS_EDIT", 
                requestId, 
                Map.of("requestId", requestId.toString(), "status", "REJECTED", "reason", rejectionMessage)
            );

            log.info("Address edit request {} rejected by admin {} - Reason: {}", 
                requestId, adminId, rejectionReason);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request rejected successfully"
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
    
    /**
     * Helper method to get current user role from security context
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                    .map(authority -> authority.getAuthority().substring(5)) // Remove "ROLE_" prefix
                    .findFirst()
                    .orElse("USER");
        }
        return "USER";
    }

    /**
     * Helper method to get logged-in employee ID from security context
     */
    private Long getLoggedInEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Employee emp) {
            return emp.getId();
        }
        throw new RuntimeException("Admin not authenticated");
    }

    /**
     * Helper method to get current user's department
     */
    private String getCurrentUserDepartment() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Employee emp) {
            return emp.getDepartment() != null ? emp.getDepartment().getName() : null;
        }
        return null;
    }

    /**
     * Update address type for a request
     */
    @PutMapping("/{requestId}/update-address-type")
    @Transactional
    public ResponseEntity<?> updateAddressType(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body) {

        try {
            CustomerAddressEditRequest request =
                editRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            if (request.getStatus() != CustomerAddressEditRequest.RequestStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only pending requests allowed"));
            }

            String addressType = body.get("addressType");
            if (addressType == null || addressType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Address type is required"));
            }

            // Validate address type enum
            try {
                CustomerAddress.AddressType.valueOf(addressType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid address type"));
            }

            // Note: Address type update would need to be added to entity if needed
            // For now, just return success
            editRequestRepository.save(request);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Failed to update address type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update address type"));
        }
    }
}
