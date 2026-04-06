package com.company.attendance.service;

import com.company.attendance.entity.LeaveRequest;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequest save(LeaveRequest leave) {
        return leaveRequestRepository.save(leave);
    }

    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> findAllForAdmin() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> findMyLeaves() {
        Employee me = getCurrentEmployee();
        return leaveRequestRepository.findByEmployee_IdOrderByIdDesc(me.getId());
    }

    public List<LeaveRequest> findTlLeaves() {
        Employee me = getCurrentEmployee();
        return leaveRequestRepository.findByEmployee_Tl_IdOrderByIdDesc(me.getId());
    }

    public List<LeaveRequest> findManagerLeaves() {
        Employee me = getCurrentEmployee();
        return leaveRequestRepository.findByEmployee_ReportingManager_IdOrderByIdDesc(me.getId());
    }

    public LeaveRequest applyLeave(LeaveRequest leave) {
        if (leave == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave payload is required");
        }

        Employee me = getCurrentEmployee();
        leave.setId(null);
        leave.setEmployee(me);
        leave.setStatus(LeaveRequest.Status.PENDING);
        leave.setApprovedBy(null);
        leave.setApprovedAt(null);
        leave.setApprovalRole(null);

        return leaveRequestRepository.save(leave);
    }

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    public LeaveRequest getById(Long id) {
        return leaveRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("LeaveRequest not found"));
    }

    public LeaveRequest update(Long id, LeaveRequest updated) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Leave update not allowed");
    }

    public LeaveRequest approve(Long id) {
        return decide(id, LeaveRequest.Status.APPROVED);
    }

    public LeaveRequest reject(Long id) {
        return decide(id, LeaveRequest.Status.REJECTED);
    }

    private LeaveRequest decide(Long id, LeaveRequest.Status targetStatus) {
        if (targetStatus == null || targetStatus == LeaveRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target status");
        }

        Employee approver = getCurrentEmployee();
        String role = getCurrentRole();
        if (!isApproverRole(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only TL, MANAGER, or ADMIN can approve/reject leaves");
        }

        LeaveRequest leave = getById(id);
        if (leave.getStatus() != LeaveRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave request already finalized");
        }

        leave.setStatus(targetStatus);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
        leave.setApprovalRole(role);
        return leaveRequestRepository.save(leave);
    }

    private boolean isApproverRole(String role) {
        if (role == null) return false;
        final String r = role.trim().toUpperCase();
        return r.equals("TL") || r.equals("MANAGER") || r.equals("ADMIN");
    }

    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = auth.getName();
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga == null) continue;
            String a = ga.getAuthority();
            if (a == null) continue;
            if (a.startsWith("ROLE_")) return a.substring(5);
            return a;
        }
        return null;
    }

    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }
}
