-- ==============================================================================
-- Sample Seed Data for Student Task Manager
-- Useful for testing or manual insertion via MySQL Workbench
-- ==============================================================================

USE student_task_manager;

INSERT INTO tasks (title, description, subject, priority, status, deadline, created_at, updated_at) VALUES
('Implement AVL Tree in Java', 'Complete the insertion, deletion, and rotation logic for the Data Structures lab assignment.', 'Data Structures', 'HIGH', 'IN_PROGRESS', CURRENT_DATE + INTERVAL 2 DAY, NOW(), NOW()),
('Operating Systems: Round Robin Scheduler', 'Simulate CPU scheduling using the Round Robin algorithm with a time quantum of 4ms.', 'Operating Systems', 'HIGH', 'PENDING', CURRENT_DATE + INTERVAL 4 DAY, NOW(), NOW()),
('DBMS Normalization Assignment', 'Normalize the university library database schema up to 3NF and BCNF. Submit PDF report.', 'Database Management Systems', 'MEDIUM', 'COMPLETED', CURRENT_DATE - INTERVAL 1 DAY, NOW(), NOW()),
('Computer Networks Socket Programming', 'Write a TCP client-server chat program using Java Socket API.', 'Computer Networks', 'MEDIUM', 'PENDING', CURRENT_DATE + INTERVAL 7 DAY, NOW(), NOW()),
('Web Technology: Portfolio Website', 'Design a responsive portfolio website using HTML5, CSS3, and modern JavaScript.', 'Web Technology', 'LOW', 'COMPLETED', CURRENT_DATE - INTERVAL 3 DAY, NOW(), NOW()),
('Software Engineering: UML Diagrams', 'Draw Class Diagram, Sequence Diagram, and Use Case Diagram for the Online Shopping System.', 'Software Engineering', 'LOW', 'IN_PROGRESS', CURRENT_DATE + INTERVAL 5 DAY, NOW(), NOW());
