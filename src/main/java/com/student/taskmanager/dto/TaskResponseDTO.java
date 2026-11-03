package com.student.taskmanager.dto;

import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.Task;
import com.student.taskmanager.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TaskResponseDTO
 * 
 * Standard format returned to the frontend client for a Task.
 */
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String subject;
    private Priority priority;
    private TaskStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public TaskResponseDTO() {
    }

    public TaskResponseDTO(Long id, String title, String description, String subject,
                           Priority priority, TaskStatus status, LocalDate deadline,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Helper static factory method to convert a Task JPA entity into a TaskResponseDTO.
     */
    public static TaskResponseDTO fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getSubject(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
