package com.student.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.taskmanager.dto.DashboardStatsDTO;
import com.student.taskmanager.dto.TaskRequestDTO;
import com.student.taskmanager.dto.TaskResponseDTO;
import com.student.taskmanager.entity.Priority;
import com.student.taskmanager.entity.TaskStatus;
import com.student.taskmanager.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST API Integration Tests for TaskController using MockMvc.
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/tasks - Should create task and return 201 Created")
    void testCreateTask() throws Exception {
        TaskRequestDTO requestDTO = new TaskRequestDTO(
                "Build REST API",
                "Learn Spring Boot 3",
                "Web Technology",
                Priority.HIGH,
                TaskStatus.PENDING,
                LocalDate.now().plusDays(2)
        );

        TaskResponseDTO responseDTO = new TaskResponseDTO(
                1L,
                "Build REST API",
                "Learn Spring Boot 3",
                "Web Technology",
                Priority.HIGH,
                TaskStatus.PENDING,
                LocalDate.now().plusDays(2),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Build REST API"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return 200 OK and list of tasks")
    void testGetAllTasks() throws Exception {
        TaskResponseDTO task = new TaskResponseDTO(
                1L, "Test Task", "Desc", "OS",
                Priority.MEDIUM, TaskStatus.PENDING, LocalDate.now(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(taskService.getAllTasks(any(), any(), any(), any(), any())).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("GET /api/tasks/stats - Should return 200 OK and dashboard statistics")
    void testGetDashboardStats() throws Exception {
        DashboardStatsDTO stats = new DashboardStatsDTO(5, 2, 2, 1, 1, 0);

        when(taskService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.completedTasks").value(2))
                .andExpect(jsonPath("$.pendingTasks").value(2));
    }
}
