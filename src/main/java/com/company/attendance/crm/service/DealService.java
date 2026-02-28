package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import com.company.attendance.crm.service.AuditService;
import com.company.attendance.service.DepartmentAwareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DealService {
    private final DealRepository dealRepository;
    private final AuditService auditService;
    private final DepartmentAwareService departmentAwareService;

    public Optional<Deal> findById(Long id) {
        return dealRepository.findById(id);
    }

    public Page<Deal> list(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }
    
    /**
     * 🔥 DEPARTMENT-AWARE: List deals with department filtering
     */
    public Page<Deal> listWithDepartmentFilter(
            Pageable pageable, 
            String userId, 
            String userRole, 
            String userDepartment) {
        
        DepartmentAwareService.UserContext context = 
            departmentAwareService.getUserContext(userId, userRole, userDepartment);
        
        if (context == null) {
            return Page.empty(pageable);
        }
        
        if (context.isAdmin) {
            // Admin sees all deals
            return dealRepository.findAll(pageable);
        } else {
            // Non-admin sees only their department's deals
            return dealRepository.findByDepartment(context.department, pageable);
        }
    }
    
    /**
     * 🔥 DEPARTMENT-AWARE: Get single deal with department check
     */
    public Optional<Deal> findByIdWithDepartmentCheck(
            Long id, 
            String userId, 
            String userRole, 
            String userDepartment) {
        
        DepartmentAwareService.UserContext context = 
            departmentAwareService.getUserContext(userId, userRole, userDepartment);
        
        if (context == null) {
            return Optional.empty();
        }
        
        Optional<Deal> deal = dealRepository.findById(id);
        if (deal.isEmpty()) {
            return Optional.empty();
        }
        
        // Check department access
        if (!departmentAwareService.canAccessDepartment(context, deal.get().getDepartment())) {
            return Optional.empty(); // Access denied
        }
        
        return deal;
    }
    
    /**
     * 🔥 DEPARTMENT-AWARE: Find deals by client with department filter
     */
    public List<Deal> findByClientIdWithDepartmentFilter(
            Long clientId,
            String userId, 
            String userRole, 
            String userDepartment) {
        
        DepartmentAwareService.UserContext context = 
            departmentAwareService.getUserContext(userId, userRole, userDepartment);
        
        if (context == null) {
            return List.of();
        }
        
        List<Deal> deals = dealRepository.findByClientId(clientId);
        
        // Filter by department if not admin
        if (!context.isAdmin) {
            return deals.stream()
                .filter(deal -> context.department.equals(deal.getDepartment()))
                .toList();
        }
        
        return deals;
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Transactional
    public Deal create(Deal deal) {
        // Set audit fields
        auditService.setAuditFields(deal);
        return dealRepository.save(deal);
    }

    @Transactional
    public Deal update(Deal deal) {
        // Update audit fields
        auditService.updateAuditFields(deal);
        return dealRepository.save(deal);
    }

    public void delete(Long id) {
        dealRepository.deleteById(id);
    }
}
