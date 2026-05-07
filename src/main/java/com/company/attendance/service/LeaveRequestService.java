package com.company.attendance.service;

import com.company.attendance.dto.LeaveRequestDto;
import com.company.attendance.entity.LeaveRequest;
import com.company.attendance.entity.Employee;
import com.company.attendance.mapper.LeaveRequestMapper;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final NotificationService notificationService;

    private void notifyLeaveApply(Employee employee, LeaveRequest saved) {
        try {
            final Long leaveId = saved != null ? saved.getId() : null;
            final Long employeeId = employee != null ? employee.getId() : null;
            final String employeeName = employee != null ? employee.getFullName() : "Employee";

            final String title = "New Leave Request";
            final String body = employeeName + " applied for leave";
            final Map<String, String> data = new HashMap<>();
            data.put("type", "LEAVE_APPLY");
            if (leaveId != null) data.put("leaveId", String.valueOf(leaveId));
            if (employeeId != null) data.put("employeeId", String.valueOf(employeeId));
            data.put("message", body);

            log.info("[LeaveNotification] event=LEAVE_APPLY employeeId={} leaveId={} recipients=ADMIN+MANAGER", employeeId, leaveId);

            notificationService.sendRoleBasedNotification("ADMIN", title, body, "LEAVE_APPLY", leaveId);
            notificationService.sendRoleBasedNotification("MANAGER", title, body, "LEAVE_APPLY", leaveId);
        } catch (Exception e) {
            log.error("[LeaveNotification] event=LEAVE_APPLY failed: {}", e.getMessage(), e);
        }
    }

    private void notifyLeaveDecision(LeaveRequest saved, String type) {
        try {
            final Long leaveId = saved != null ? saved.getId() : null;
            final Long requesterId = saved != null && saved.getEmployee() != null ? saved.getEmployee().getId() : null;

            final String title;
            final String body;
            if ("LEAVE_APPROVED".equals(type)) {
                title = "Leave Approved";
                body = "Your leave request has been approved";
            } else {
                title = "Leave Rejected";
                body = "Your leave request has been rejected";
            }

            final Map<String, String> data = new HashMap<>();
            data.put("type", type);
            if (leaveId != null) data.put("leaveId", String.valueOf(leaveId));
            if (requesterId != null) data.put("employeeId", String.valueOf(requesterId));
            data.put("message", body);

            log.info("[LeaveNotification] event={} leaveId={} recipientEmployeeId={}", type, leaveId, requesterId);

            if (requesterId != null) {
                notificationService.notifyEmployeeMobile(requesterId, title, body, type, "LEAVE", leaveId, data);
                notificationService.notifyEmployeeWeb(requesterId, title, body, type, "LEAVE", leaveId, data);
            }
        } catch (Exception e) {
            log.error("[LeaveNotification] event={} failed: {}", type, e.getMessage(), e);
        }
    }

    public LeaveRequest save(LeaveRequest leave) {
        return leaveRequestRepository.save(leave);
    }

    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> findAllForAdmin() {
        return leaveRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> findAllForAdmin(Long employeeId) {
        Employee requester = requireEmployee(employeeId);
        requireAdminRole(requester);
        return leaveRequestRepository.findAll()
                .stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> findLeavesByEmployeeId(Long employeeId, Integer month, Integer year) {
        Employee employee = requireEmployee(employeeId);

        if (month == null && year == null) {
            return leaveRequestRepository.findByEmployee_IdOrderByIdDesc(employee.getId())
                    .stream()
                    .map(leaveRequestMapper::toDto)
                    .collect(Collectors.toList());
        }

        if (month == null || year == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both month and year must be provided together"
            );
        }

        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid month");
        }
        if (year < 2000 || year > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid year");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return leaveRequestRepository.findByEmployeeIdOverlappingDateRangeOrderByIdDesc(
                        employee.getId(),
                        startDate,
                        endDate
                )
                .stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> findTlLeaves(Long tlEmployeeId) {
        Employee tl = requireEmployee(tlEmployeeId);
        return leaveRequestRepository.findByEmployee_Tl_IdOrderByIdDesc(tl.getId())
                .stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> findManagerLeaves(Long managerEmployeeId) {
        Employee manager = requireEmployee(managerEmployeeId);
        return leaveRequestRepository.findByEmployee_ReportingManager_IdOrderByIdDesc(manager.getId())
                .stream()
                .map(leaveRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDto applyLeave(Long employeeId, LeaveRequest leave) {
        if (leave == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave payload is required");
        }

        Employee employee = requireEmployee(employeeId);
        leave.setId(null);
        leave.setEmployee(employee);
        leave.setStatus(LeaveRequest.Status.PENDING);
        leave.setApprovedBy(null);
        leave.setApprovedAt(null);
        leave.setApprovalRole(null);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        notifyLeaveApply(employee, saved);
        return leaveRequestMapper.toDto(saved);
    }

    public Optional<LeaveRequest> findById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public LeaveRequestDto getLeave(Long id, Long employeeId) {
        Employee requester = requireEmployee(employeeId);
        LeaveRequest leave = getById(id);

        if (!canAccessLeave(requester, leave)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this leave request");
        }
        return leaveRequestMapper.toDto(leave);
    }

    public LeaveRequest getById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LeaveRequest not found"));
    }

    @Transactional
    public LeaveRequestDto update(Long id, Long employeeId, LeaveRequest updated) {
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave payload is required");
        }
        Employee employee = requireEmployee(employeeId);

        LeaveRequest existing = getById(id);
        if (existing.getEmployee() == null || existing.getEmployee().getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave request is missing employee assignment");
        }
        if (!existing.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit another employee's leave");
        }
        if (existing.getStatus() != LeaveRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING leaves can be edited");
        }

        if (updated.getLeaveType() != null) existing.setLeaveType(updated.getLeaveType());
        if (updated.getFromDate() != null) existing.setFromDate(updated.getFromDate());
        if (updated.getToDate() != null) existing.setToDate(updated.getToDate());
        if (updated.getReason() != null) existing.setReason(updated.getReason());

        if (existing.getFromDate() == null || existing.getToDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate and toDate are required");
        }
        if (existing.getFromDate().isAfter(existing.getToDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate");
        }

        LeaveRequest saved = leaveRequestRepository.save(existing);
        return leaveRequestMapper.toDto(saved);
    }

    @Transactional
    public LeaveRequestDto approve(Long id, Long approverEmployeeId) {
        return decide(id, approverEmployeeId, LeaveRequest.Status.APPROVED);
    }

    @Transactional
    public LeaveRequestDto reject(Long id, Long approverEmployeeId) {
        return decide(id, approverEmployeeId, LeaveRequest.Status.REJECTED);
    }

    private LeaveRequestDto decide(Long id, Long approverEmployeeId, LeaveRequest.Status targetStatus) {
        if (targetStatus == null || targetStatus == LeaveRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target status");
        }

        Employee approver = requireEmployee(approverEmployeeId);
        String role = getEmployeeRoleName(approver);
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
        LeaveRequest saved = leaveRequestRepository.save(leave);

        if (targetStatus == LeaveRequest.Status.APPROVED) {
            notifyLeaveDecision(saved, "LEAVE_APPROVED");
        } else if (targetStatus == LeaveRequest.Status.REJECTED) {
            notifyLeaveDecision(saved, "LEAVE_REJECTED");
        }

        return leaveRequestMapper.toDto(saved);
    }

    private boolean isApproverRole(String role) {
        if (role == null) return false;
        final String r = role.trim().toUpperCase();
        return r.equals("TL") || r.equals("MANAGER") || r.equals("ADMIN");
    }

    private Employee requireEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeId is required");
        }
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private String getEmployeeRoleName(Employee employee) {
        if (employee == null || employee.getRole() == null) return null;
        String role = employee.getRole().getName();
        return role != null ? role.trim() : null;
    }

    private void requireAdminRole(Employee employee) {
        String role = getEmployeeRoleName(employee);
        if (role == null || !role.trim().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can access this resource");
        }
    }

    private boolean canAccessLeave(Employee requester, LeaveRequest leave) {
        if (requester == null || leave == null) return false;
        if (leave.getEmployee() != null && leave.getEmployee().getId() != null
                && leave.getEmployee().getId().equals(requester.getId())) {
            return true;
        }
        String role = getEmployeeRoleName(requester);
        return isApproverRole(role);
    }

    @Transactional
    public void delete(Long id, Long employeeId) {
        Employee employee = requireEmployee(employeeId);
        LeaveRequest existing = getById(id);
        if (existing.getEmployee() == null || existing.getEmployee().getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave request is missing employee assignment");
        }
        if (!existing.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete another employee's leave");
        }
        if (existing.getStatus() != LeaveRequest.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING leaves can be deleted");
        }
        leaveRequestRepository.deleteById(existing.getId());
    }
}
