package com.student.taskmanager.dto;

import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * TaskRequestDTO (Data Transfer Object)
 * 
 * Why use DTOs instead of passing the JPA Entity directly?
 * 1. Security: Prevents client from tampering with fields like 'id' or 'createdAt'.
 * 2. Validation: We can apply @NotBlank, @Size annotations without cluttering our database entity.
 * 3. Decoupling: Decouples our database schema from the external API contract.
 */
public class TaskRequestDTO {

    @NotBlank(message = "Task title is required and cannot be empty")
    @Size(min = 3, max = 150, message = "Task title must be between 3 and 150 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Subject is required (e.g., Data Structures, Operating Systems)")
    @Size(max = 100, message = "Subject name cannot exceed 100 characters")
    private String subject;

    private Priority priority = Priority.MEDIUM;

    private TaskStatus status = TaskStatus.PENDING;

    private LocalDate deadline;

    // Default constructor
    public TaskRequestDTO() {
    }

    // Parameterized constructor
    public TaskRequestDTO(String title, String description, String subject, Priority priority, TaskStatus status, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : TaskStatus.PENDING;
        this.deadline = deadline;
    }

    // Getters and Setters
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
}
