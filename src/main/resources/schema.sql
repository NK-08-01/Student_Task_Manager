-- ==============================================================================
-- Student Task Management Database Schema
-- Database: student_task_manager
-- Suitable for running directly in MySQL Workbench or via MySQL CLI
-- ==============================================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS student_task_manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE student_task_manager;

-- 2. Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique task identifier',
    title VARCHAR(150) NOT NULL COMMENT 'Brief task title / heading',
    description TEXT COMMENT 'Detailed task notes or requirements',
    subject VARCHAR(100) COMMENT 'Subject or course name (e.g. DBMS, OS, Data Structures)',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT 'Task priority: LOW, MEDIUM, HIGH',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Task status: PENDING, IN_PROGRESS, COMPLETED',
    deadline DATE COMMENT 'Due date for task completion',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp when task was created',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Timestamp when task was last updated',
    
    INDEX idx_task_status (status),
    INDEX idx_task_priority (priority),
    INDEX idx_task_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stores student academic tasks';
