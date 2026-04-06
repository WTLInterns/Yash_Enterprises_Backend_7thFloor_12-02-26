package com.company.attendance.repository;

import com.company.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByEmployeeId(Long employeeId);

    Optional<Attendance> findFirstByEmployee_IdAndDate(Long employeeId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :from AND :to")
    List<Attendance> findByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT a FROM Attendance a WHERE a.date BETWEEN :from AND :to AND a.employee.team.id = :teamId")
    List<Attendance> findByDateBetweenAndTeamId(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("teamId") Long teamId);

    void deleteByEmployeeId(Long employeeId);
}

