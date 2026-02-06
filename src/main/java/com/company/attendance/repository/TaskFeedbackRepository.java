package com.company.attendance.repository;

import com.company.attendance.entity.TaskFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskFeedbackRepository extends JpaRepository<TaskFeedback, Long> {

    /**
     * Find feedback by task
     */
    List<TaskFeedback> findByTaskId(Long taskId);

    /**
     * Find feedback by employee
     */
    List<TaskFeedback> findByEmployeeId(Long employeeId);

    /**
     * Find feedback by client
     */
    List<TaskFeedback> findByClientId(Long clientId);

    /**
     * Find feedback by employee and date range
     */
    @Query("SELECT tf FROM TaskFeedback tf WHERE tf.employee.id = :employeeId AND tf.createdAt BETWEEN :startDate AND :endDate")
    List<TaskFeedback> findByEmployeeIdAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find feedback by rating range
     */
    @Query("SELECT tf FROM TaskFeedback tf WHERE tf.rating BETWEEN :minRating AND :maxRating")
    List<TaskFeedback> findByRatingRange(
        @Param("minRating") Integer minRating,
        @Param("maxRating") Integer maxRating
    );

    /**
     * Count feedback by employee
     */
    @Query("SELECT COUNT(tf) FROM TaskFeedback tf WHERE tf.employee.id = :employeeId")
    long countByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Average rating by employee
     */
    @Query("SELECT AVG(tf.rating) FROM TaskFeedback tf WHERE tf.employee.id = :employeeId")
    Double getAverageRatingByEmployeeId(@Param("employeeId") Long employeeId);
}
