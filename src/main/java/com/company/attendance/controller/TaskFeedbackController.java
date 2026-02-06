package com.company.attendance.controller;

import com.company.attendance.entity.TaskFeedback;
import com.company.attendance.entity.Task;
import com.company.attendance.entity.Employee;
import com.company.attendance.repository.TaskFeedbackRepository;
import com.company.attendance.repository.TaskRepository;
import com.company.attendance.repository.EmployeeRepository;
import com.company.attendance.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Task Feedback Controller
 * Sales Executive submits feedback → Admin views it
 * NO customer interaction
 */
@RestController
@RequestMapping("/api/task-feedback")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TaskFeedbackController {

    private final TaskFeedbackRepository taskFeedbackRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    /**
     * Submit feedback (Sales Executive)
     * Flow: Task completed → Feedback form → Submit → Admin notified
     */
    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestBody Map<String, Object> feedbackData,
            @RequestParam Long employeeId) {
        
        try {
            // Validate required fields
            if (!feedbackData.containsKey("taskId") || !feedbackData.containsKey("rating")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Missing required fields",
                    "message", "taskId and rating are required"
                ));
            }

            Integer rating = (Integer) feedbackData.get("rating");
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid rating",
                    "message", "Rating must be between 1 and 5"
                ));
            }

            Long taskId = Long.valueOf(feedbackData.get("taskId").toString());
            String feedbackText = (String) feedbackData.get("feedbackText");

            // Create feedback record
            TaskFeedback feedback = new TaskFeedback();
            feedback.setTask(taskRepository.findById(taskId).orElse(null));
            feedback.setClient(taskRepository.findById(taskId).orElse(null).getClient());
            feedback.setEmployee(employeeRepository.findById(employeeId).orElse(null));
            feedback.setRating(rating);
            feedback.setFeedbackText(feedbackText);
            feedback.setCreatedAt(LocalDateTime.now());

            feedback = taskFeedbackRepository.save(feedback);

            // ✅ Notify admin (NOT customer)
            String message = String.format(
                "Feedback submitted for Task #%d by Employee #%d - Rating: %d/5",
                feedback.getTask() != null ? feedback.getTask().getId() : 0,
                employeeId,
                rating
            );

            // Send admin notification (using a simple approach for now)
            // TODO: Implement proper admin notification system
            log.info("Task feedback submitted: taskId={}, employeeId={}, rating={}", 
                feedback.getTask() != null ? feedback.getTask().getId() : 0, employeeId, rating);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedbackId", feedback.getId(),
                "message", "Feedback submitted successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to submit feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to submit feedback"));
        }
    }

    /**
     * Get all feedback (Admin)
     */
    @GetMapping
    public ResponseEntity<?> getAllFeedback(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating) {
        
        try {
            List<TaskFeedback> feedback;

            if (employeeId != null) {
                feedback = taskFeedbackRepository.findByEmployeeId(employeeId);
            } else if (clientId != null) {
                feedback = taskFeedbackRepository.findByClientId(clientId);
            } else if (minRating != null && maxRating != null) {
                feedback = taskFeedbackRepository.findByRatingRange(minRating, maxRating);
            } else {
                feedback = taskFeedbackRepository.findAll();
            }

            return ResponseEntity.ok(feedback);

        } catch (Exception e) {
            log.error("Failed to fetch feedback: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to fetch feedback"));
        }
    }

    /**
     * Get feedback by task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getFeedbackByTask(@PathVariable Long taskId) {
        try {
            List<TaskFeedback> feedback = taskFeedbackRepository.findByTaskId(taskId);
            return ResponseEntity.ok(feedback);

        } catch (Exception e) {
            log.error("Failed to get feedback for task {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get feedback"));
        }
    }

    /**
     * Get feedback statistics (Admin Dashboard)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getFeedbackStats() {
        try {
            // This would need implementation for statistics
            return ResponseEntity.ok(Map.of(
                "totalFeedback", taskFeedbackRepository.count(),
                "averageRating", 0.0, // Would calculate actual average
                "message", "Statistics endpoint - needs implementation"
            ));

        } catch (Exception e) {
            log.error("Failed to get feedback stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get statistics"));
        }
    }
}
