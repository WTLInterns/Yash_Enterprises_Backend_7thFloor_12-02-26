package com.company.attendance.repository;

import com.company.attendance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Employee> findByPhone(String phone);

    // Methods used by EmployeeService
    Optional<Employee> findByUserId(String userId);
    Optional<Employee> findByEmployeeId(String employeeId);
    List<Employee> findByStatus(Employee.Status status);
    boolean existsByUserId(String userId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsById(Long id);
    List<Employee> findByTeamId(Long teamId);
    
    // Add JOIN FETCH queries to load all relationships
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.role " +
           "LEFT JOIN FETCH e.team " +
           "LEFT JOIN FETCH e.designation " +
           "LEFT JOIN FETCH e.reportingManager " +
           "LEFT JOIN FETCH e.organization " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.shift " +
           "ORDER BY e.id")
    List<Employee> findAllWithRelationships();
    
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.role " +
           "LEFT JOIN FETCH e.team " +
           "LEFT JOIN FETCH e.designation " +
           "LEFT JOIN FETCH e.reportingManager " +
           "LEFT JOIN FETCH e.organization " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.shift " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdWithRelationships(Long id);
}

