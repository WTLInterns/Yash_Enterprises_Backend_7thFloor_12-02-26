package com.company.attendance.mapper;

import com.company.attendance.dto.LeaveRequestDto;
import com.company.attendance.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(leaveRequest.getEmployee() != null ? leaveRequest.getEmployee().getFullName() : null)")
    @Mapping(target = "approvedByName", expression = "java(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getFullName() : null)")
    LeaveRequestDto toDto(LeaveRequest leaveRequest);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvalRole", ignore = true)
    LeaveRequest toEntity(LeaveRequestDto dto);

    default String map(LeaveRequest.LeaveType type) {
        return type != null ? type.name() : null;
    }

    default LeaveRequest.LeaveType mapLeaveType(String type) {
        if (type == null || type.trim().isEmpty()) return null;
        return LeaveRequest.LeaveType.valueOf(type.trim().toUpperCase());
    }

    default String map(LeaveRequest.Status status) {
        return status != null ? status.name() : null;
    }

    default LeaveRequest.Status mapStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        return LeaveRequest.Status.valueOf(status.trim().toUpperCase());
    }
}
