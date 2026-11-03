package com.student.taskmanager.service;

import com.student.taskmanager.dto.DashboardStatsDTO;
import com.student.taskmanager.dto.TaskRequestDTO;
import com.student.taskmanager.dto.TaskResponseDTO;
import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.Task;
import com.student.taskmanager.entity.TaskStatus;
import com.student.taskmanager.exception.ResourceNotFoundException;
import com.student.taskmanager.repository.TaskRepository;
import com.student.taskmanager.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TaskServiceImpl using Mockito.
 * Demonstrates how to test business logic in isolation without a real database.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Unit Task");
        sampleTask.setSubject("Computer Networks");
        sampleTask.setDescription("Test task description");
        sampleTask.setPriority(Priority.HIGH);
        sampleTask.setStatus(TaskStatus.PENDING);
        sampleTask.setDeadline(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        TaskRequestDTO requestDTO = new TaskRequestDTO(
                "Test Unit Task",
                "Test task description",
                "Computer Networks",
                Priority.HIGH,
                TaskStatus.PENDING,
                LocalDate.now().plusDays(5)
        );

        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponseDTO result = taskService.createTask(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Unit Task", result.getTitle());
        assertEquals(Priority.HIGH, result.getPriority());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should return task by ID when it exists")
    void testGetTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        TaskResponseDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Unit Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task ID is not found")
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Should update task status successfully")
    void testUpdateTaskStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDTO result = taskService.updateTaskStatus(1L, TaskStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("Should delete task when ID exists")
    void testDeleteTask_Success() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        assertDoesNotThrow(() -> taskService.deleteTask(1L));
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent task")
    void testDeleteTask_NotFound() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(99L));
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should calculate dashboard statistics correctly")
    void testGetDashboardStats() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(4L);
        when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.findByDeadlineBetweenAndStatusNot(any(), any(), eq(TaskStatus.COMPLETED)))
                .thenReturn(Collections.emptyList());
        when(taskRepository.findByDeadlineBeforeAndStatusNot(any(), eq(TaskStatus.COMPLETED)))
                .thenReturn(Collections.emptyList());

        DashboardStatsDTO stats = taskService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalTasks());
        assertEquals(4L, stats.getCompletedTasks());
        assertEquals(3L, stats.getPendingTasks());
        assertEquals(3L, stats.getInProgressTasks());
    }
}
