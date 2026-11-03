package com.student.taskmanager.entity;

/**
 * TaskStatus tracks the current completion lifecycle of a task.
 * 
 * - PENDING: Task is assigned or planned but work hasn't started yet.
 * - IN_PROGRESS: Student is actively working on this task.
 * - COMPLETED: Task is finished and submitted.
 */
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
