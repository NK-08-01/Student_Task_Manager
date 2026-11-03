package com.student.taskmanager.repository;

import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.Task;
import com.student.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * TaskRepository
 * 
 * Spring Data JPA Repository interface for Task entity.
 * 
 * Key Concepts:
 * - Extends JpaRepository<Task, Long>: Provides out-of-the-box CRUD methods:
 *   save(), findById(), findAll(), deleteById(), count(), etc.
 * - Derived Query Methods: Spring Data JPA automatically writes the SQL query
 *   based on the method name (e.g., findByTitleContainingIgnoreCase).
 * - Custom JPQL Queries: Uses @Query for flexible multi-criteria search and filters.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Search tasks where the title contains the keyword (case-insensitive).
     * SQL Equivalent: SELECT * FROM tasks WHERE LOWER(title) LIKE LOWER('%keyword%');
     */
    List<Task> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Filter tasks by specific status.
     * SQL Equivalent: SELECT * FROM tasks WHERE status = ?;
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Filter tasks by priority.
     * SQL Equivalent: SELECT * FROM tasks WHERE priority = ?;
     */
    List<Task> findByPriority(Priority priority);

    /**
     * Filter tasks by both status and priority.
     */
    List<Task> findByStatusAndPriority(TaskStatus status, Priority priority);

    /**
     * Count total tasks having a specific status (used for dashboard metrics).
     */
    long countByStatus(TaskStatus status);

    /**
     * Find active tasks due within a date range (for upcoming deadlines).
     */
    List<Task> findByDeadlineBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, TaskStatus status);

    /**
     * Find overdue tasks (deadline before today and not COMPLETED).
     */
    List<Task> findByDeadlineBeforeAndStatusNot(LocalDate today, TaskStatus status);

    /**
     * Flexible multi-criteria query for searching and filtering simultaneously.
     * If a parameter is NULL, that condition is ignored.
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority)")
    List<Task> searchAndFilterTasks(
            @Param("keyword") String keyword,
            @Param("status") TaskStatus status,
            @Param("priority") Priority priority,
            Sort sort
    );
}
