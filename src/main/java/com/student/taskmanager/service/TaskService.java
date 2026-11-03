package com.student.taskmanager.service;

import com.student.taskmanager.dto.DashboardStatsDTO;
import com.student.taskmanager.dto.TaskRequestDTO;
import com.student.taskmanager.dto.TaskResponseDTO;
import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.TaskStatus;

import java.util.List;

/**
 * TaskService Interface
 * 
 * Defines the contract for business logic operations related to Task management.
 * 
 * Why use an interface and implementation?
 * 1. Loose Coupling: Controllers depend on abstractions, not concrete classes.
 * 2. Testing: Easy to mock in unit tests.
 * 3. Maintainability: Implementations can be changed without altering callers.
 */
public interface TaskService {

    /**
     * Create and save a new task.
     */
    TaskResponseDTO createTask(TaskRequestDTO requestDTO);

    /**
     * Retrieve all tasks with optional search, filtering, and sorting.
     */
    List<TaskResponseDTO> getAllTasks(String keyword, TaskStatus status, Priority priority, String sortBy, String sortDir);

    /**
     * Retrieve a single task by its unique ID.
     */
    TaskResponseDTO getTaskById(Long id);

    /**
     * Update an existing task's details.
     */
    TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO);

    /**
     * Delete a task by its ID.
     */
    void deleteTask(Long id);

    /**
     * Update only the status of an existing task.
     */
    TaskResponseDTO updateTaskStatus(Long id, TaskStatus status);

    /**
     * Search tasks by keyword in title, subject, or description.
     */
    List<TaskResponseDTO> searchTasks(String keyword);

    /**
     * Filter tasks by status and/or priority.
     */
    List<TaskResponseDTO> filterTasks(TaskStatus status, Priority priority);

    /**
     * Compute summary statistics for the student dashboard.
     */
    DashboardStatsDTO getDashboardStats();
}
