package com.student.taskmanager.controller;

import com.student.taskmanager.dto.DashboardStatsDTO;
import com.student.taskmanager.dto.TaskRequestDTO;
import com.student.taskmanager.dto.TaskResponseDTO;
import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.TaskStatus;
import com.student.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * TaskController
 * 
 * REST Controller exposing HTTP endpoints for the Student Task Manager.
 * 
 * Key Spring Boot Annotations Explained:
 * - @RestController: Convenience annotation combining @Controller and @ResponseBody.
 *   Every method returns data serialized directly into JSON (via Jackson).
 * - @RequestMapping("/api/tasks"): Defines the base URL route for all methods in this controller.
 * - @CrossOrigin: Enables Cross-Origin Resource Sharing so frontends on different ports can call these APIs.
 * - @PostMapping, @GetMapping, @PutMapping, @DeleteMapping, @PatchMapping: Map standard HTTP verbs.
 * - @Valid: Validates incoming request payloads against annotations in TaskRequestDTO (@NotBlank, etc.).
 * - @PathVariable: Extracts dynamic values from URI path segments (e.g. /api/tasks/{id}).
 * - @RequestParam: Extracts query string parameters (e.g. ?keyword=java&status=PENDING).
 * - ResponseEntity<T>: Represents the entire HTTP response (status code, headers, and body).
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    /**
     * Spring automatically injects the TaskService bean into this constructor.
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 1. CREATE A NEW TASK
     * POST /api/tasks
     * Returns: 201 CREATED with the newly created TaskResponseDTO
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO requestDTO) {
        TaskResponseDTO createdTask = taskService.createTask(requestDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * 2. GET ALL TASKS (With optional search, filter, and sort)
     * GET /api/tasks
     * Optional Query Params:
     * - keyword: search in title, subject
     * - status: PENDING, IN_PROGRESS, COMPLETED
     * - priority: LOW, MEDIUM, HIGH
     * - sortBy: deadline, title, priority, createdAt (default: deadline)
     * - sortDir: asc, desc (default: asc)
     * Returns: 200 OK with list of tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        List<TaskResponseDTO> tasks = taskService.getAllTasks(keyword, status, priority, sortBy, sortDir);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 3. GET A TASK BY ID
     * GET /api/tasks/{id}
     * Returns: 200 OK or 404 NOT FOUND if ID doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * 4. UPDATE A TASK
     * PUT /api/tasks/{id}
     * Returns: 200 OK with updated task details
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO requestDTO) {

        TaskResponseDTO updatedTask = taskService.updateTask(id, requestDTO);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * 5. DELETE A TASK
     * DELETE /api/tasks/{id}
     * Returns: 204 NO_CONTENT on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 6. UPDATE TASK STATUS
     * PATCH /api/tasks/{id}/status
     * Supports both:
     * - Query param: PATCH /api/tasks/1/status?status=COMPLETED
     * - JSON body: PATCH /api/tasks/1/status with body {"status": "COMPLETED"}
     * Returns: 200 OK with updated task
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam(required = false) TaskStatus status,
            @RequestBody(required = false) Map<String, String> body) {

        TaskStatus resolvedStatus = status;

        // If status wasn't supplied as a query parameter, check JSON body
        if (resolvedStatus == null && body != null && body.containsKey("status")) {
            resolvedStatus = TaskStatus.valueOf(body.get("status").toUpperCase().trim());
        }

        if (resolvedStatus == null) {
            throw new IllegalArgumentException("Task status is required (e.g. PENDING, IN_PROGRESS, COMPLETED)");
        }

        TaskResponseDTO updatedTask = taskService.updateTaskStatus(id, resolvedStatus);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * 7. SEARCH TASKS BY KEYWORD
     * GET /api/tasks/search?keyword=java
     * Returns: 200 OK with matching tasks
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponseDTO>> searchTasks(@RequestParam String keyword) {
        List<TaskResponseDTO> results = taskService.searchTasks(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 8. FILTER TASKS BY STATUS AND/OR PRIORITY
     * GET /api/tasks/filter?status=PENDING&priority=HIGH
     * Returns: 200 OK with filtered tasks
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TaskResponseDTO>> filterTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority) {

        List<TaskResponseDTO> results = taskService.filterTasks(status, priority);
        return ResponseEntity.ok(results);
    }

    /**
     * 9. GET DASHBOARD STATISTICS
     * GET /api/tasks/stats
     * Returns: 200 OK with total, completed, pending, in-progress, and urgent counts
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = taskService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
