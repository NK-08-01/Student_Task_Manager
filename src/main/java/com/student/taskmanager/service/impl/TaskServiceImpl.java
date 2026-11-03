package com.student.taskmanager.service.impl;

import com.student.taskmanager.dto.DashboardStatsDTO;
import com.student.taskmanager.dto.TaskRequestDTO;
import com.student.taskmanager.dto.TaskResponseDTO;
import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.Task;
import com.student.taskmanager.entity.TaskStatus;
import com.student.taskmanager.exception.ResourceNotFoundException;
import com.student.taskmanager.repository.TaskRepository;
import com.student.taskmanager.service.TaskService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskServiceImpl
 * 
 * Concrete implementation of the TaskService interface.
 * 
 * Key Concepts:
 * - @Service: Specialization of @Component, registers this class as a Spring Bean
 *   in the application context so it can be injected into controllers.
 * - @Transactional: Manages database transactions. If an unhandled runtime exception
 *   occurs during a method call, all database changes within that method are rolled back.
 * - Constructor Injection: Best practice for Dependency Injection in Spring. Spring automatically
 *   supplies the required TaskRepository bean when creating this service instance.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    /**
     * Constructor injection for TaskRepository.
     */
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO requestDTO) {
        // Step 1: Map DTO data to new JPA Entity
        Task task = new Task();
        task.setTitle(requestDTO.getTitle().trim());
        task.setDescription(requestDTO.getDescription() != null ? requestDTO.getDescription().trim() : null);
        task.setSubject(requestDTO.getSubject().trim());
        task.setPriority(requestDTO.getPriority() != null ? requestDTO.getPriority() : Priority.MEDIUM);
        task.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : TaskStatus.PENDING);
        task.setDeadline(requestDTO.getDeadline());

        // Step 2: Save to database using Spring Data JPA
        Task savedTask = taskRepository.save(task);

        // Step 3: Convert the saved entity into a response DTO
        return TaskResponseDTO.fromEntity(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(String keyword, TaskStatus status, Priority priority, String sortBy, String sortDir) {
        // Build dynamic Sort object (defaults to sorting by deadline ascending)
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (sortBy != null && !sortBy.isBlank()) ? sortBy : "deadline";

        // Sort by deadline (nulls last) or by requested property
        Sort sort = Sort.by(direction, sortProperty);

        // Use custom flexible query if any search/filter criteria is present
        List<Task> tasks;
        boolean hasFilter = (keyword != null && !keyword.isBlank()) || status != null || priority != null;

        if (hasFilter) {
            String sanitizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
            tasks = taskRepository.searchAndFilterTasks(sanitizedKeyword, status, priority, sort);
        } else {
            tasks = taskRepository.findAll(sort);
        }

        return tasks.stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return TaskResponseDTO.fromEntity(task);
    }

    @Override
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO) {
        // Fetch existing task or throw 404
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Update fields with new values from request
        task.setTitle(requestDTO.getTitle().trim());
        task.setDescription(requestDTO.getDescription() != null ? requestDTO.getDescription().trim() : null);
        task.setSubject(requestDTO.getSubject().trim());
        if (requestDTO.getPriority() != null) {
            task.setPriority(requestDTO.getPriority());
        }
        if (requestDTO.getStatus() != null) {
            task.setStatus(requestDTO.getStatus());
        }
        task.setDeadline(requestDTO.getDeadline());

        // Save updated entity to database
        Task updatedTask = taskRepository.save(task);
        return TaskResponseDTO.fromEntity(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        // Verify task exists before deleting
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", "id", id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long id, TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return TaskResponseDTO.fromEntity(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> searchTasks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllTasks(null, null, null, "deadline", "asc");
        }
        return taskRepository.findByTitleContainingIgnoreCase(keyword.trim())
                .stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> filterTasks(TaskStatus status, Priority priority) {
        List<Task> tasks;
        if (status != null && priority != null) {
            tasks = taskRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else if (priority != null) {
            tasks = taskRepository.findByPriority(priority);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long pendingTasks = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);

        // Upcoming deadlines: non-completed tasks due between today and today + 3 days
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        List<Task> upcoming = taskRepository.findByDeadlineBetweenAndStatusNot(today, threeDaysLater, TaskStatus.COMPLETED);
        long upcomingCount = upcoming.size();

        // Overdue tasks: non-completed tasks with deadline strictly before today
        List<Task> overdue = taskRepository.findByDeadlineBeforeAndStatusNot(today, TaskStatus.COMPLETED);
        long overdueCount = overdue.size();

        return new DashboardStatsDTO(
                totalTasks,
                completedTasks,
                pendingTasks,
                inProgressTasks,
                upcomingCount,
                overdueCount
        );
    }
}
