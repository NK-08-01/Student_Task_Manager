# Student Task Manager 🎓

> A complete, beginner-friendly Web Application built with **Java 17**, **Spring Boot 3**, **Spring Data JPA**, **Hibernate**, **MySQL**, and a responsive modern **HTML5 / CSS3 / JavaScript** frontend.

Designed specifically for Computer Science students learning Java backend development to understand full-stack layered architecture, RESTful API design, database persistence, and asynchronous JavaScript integration.

---

## 📑 Table of Contents
1. [Project Overview & Features](#-project-overview--features)
2. [Technology Stack](#-technology-stack)
3. [Layered Architecture Explained](#-layered-architecture-explained)
4. [Database Setup (MySQL)](#-database-setup-mysql)
5. [Configuration (`application.properties`)](#-configuration-applicationproperties)
6. [How to Run the Application](#-how-to-run-the-application)
   - [Running with Maven (Terminal)](#1-running-with-maven-terminal)
   - [Running in IntelliJ IDEA](#2-running-in-intellij-idea)
   - [Running in VS Code](#3-running-in-vs-code)
7. [REST API Documentation & Postman Guide](#-rest-api-documentation--postman-guide)
8. [Testing the Application](#-testing-the-application)

---

## 🚀 Project Overview & Features

### A. Academic Dashboard
- **Real-Time Database Statistics**: Total tasks, In Progress, Pending, and Completed counts computed dynamically via MySQL.
- **Urgent Deadlines Alert Banner**: Automatically highlights tasks due within 3 days or overdue.
- **Today's Date Indicator**: Helps students keep track of daily deadlines.

### B. Task Management
- **Add New Task**: Form modal with validation for title, subject, priority, deadline, and description.
- **View All Tasks**: Switch between modern **Card Grid View** and structured **Table View**.
- **View Task Details**: Complete modal showing creation timestamps and database IDs.
- **Edit Existing Task**: Pre-populated modal form to update assignment details.
- **Delete Task**: Safe deletion with a confirmation dialog.
- **One-Click Status Change**: Quick dropdown directly on task cards to transition between `PENDING`, `IN_PROGRESS`, and `COMPLETED`.

### C. Search & Filter
- **Live Search**: Debounced search by title and subject.
- **Multi-Filter**: Filter by Status (`PENDING`, `IN_PROGRESS`, `COMPLETED`) and Priority (`LOW`, `MEDIUM`, `HIGH`).
- **Deadline Sorting**: Sort tasks by nearest or furthest deadline.
- **Filter Tags**: Visual pills indicating active filters with quick one-click clear.

---

## 🛠 Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Language** | Java 17 | Core backend programming language (LTS) |
| **Framework** | Spring Boot 3.2.5 | Modern enterprise Java framework |
| **Web / REST** | Spring MVC & Jackson | RESTful controllers and JSON serialization |
| **ORM / Persistence** | Spring Data JPA & Hibernate 6 | Object-Relational Mapping to MySQL |
| **Validation** | Jakarta Bean Validation | `@NotBlank`, `@NotNull`, `@Size` on requests |
| **Database** | MySQL 8.x | Relational database storage |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+ Fetch API) | Clean, responsive single-page web app |
| **CSS Framework** | Bootstrap 5.3 & Bootstrap Icons | Modern typography, grid system, cards, modals |
| **Build Tool** | Apache Maven | Dependency management & project build |

---

## 🏗 Layered Architecture Explained

This project follows the industry-standard **Layered Architecture pattern**. Each layer has a single, well-defined responsibility:

```
[ Browser / Frontend Client ]
       │  (HTTP Requests: GET, POST, PUT, DELETE, PATCH)
       ▼
[ Controller Layer ]   --> com.student.taskmanager.controller
       │  (Validates input, maps URLs, returns ResponseEntity with HTTP status codes)
       ▼
[ Service Layer ]      --> com.student.taskmanager.service
       │  (Business logic, DTO <-> Entity mapping, calculations)
       ▼
[ Repository Layer ]   --> com.student.taskmanager.repository
       │  (Spring Data JPA queries, interacts with database)
       ▼
[ Database Layer ]     --> MySQL (student_task_manager database)
```

### 1. `entity` Package (`Task.java`, `Priority.java`, `TaskStatus.java`)
- **Responsibility**: Represents database tables as Java objects (Object-Relational Mapping).
- Uses annotations like `@Entity`, `@Table(name = "tasks")`, `@Id`, and `@Column`.
- Automatically generates timestamps using `@PrePersist` and `@PreUpdate`.

### 2. `dto` Package (`TaskRequestDTO.java`, `TaskResponseDTO.java`, `DashboardStatsDTO.java`)
- **Responsibility**: Transfers data between the client and controller.
- **Why DTOs?** Prevents exposing internal database structures, avoids malicious field updates (e.g., someone modifying primary keys), and encapsulates custom validation rules.

### 3. `repository` Package (`TaskRepository.java`)
- **Responsibility**: Direct interface with the MySQL database.
- Extends `JpaRepository<Task, Long>` to inherit pre-built methods (`save`, `findById`, `findAll`, `deleteById`).
- Provides custom JPQL and derived query methods like `findByTitleContainingIgnoreCase` and `countByStatus`.

### 4. `service` Package (`TaskService.java` & `TaskServiceImpl.java`)
- **Responsibility**: Core brain of the application containing business rules.
- Manages transactions (`@Transactional`), coordinates repository calls, converts DTOs to entities, and computes statistics.

### 5. `controller` Package (`TaskController.java`)
- **Responsibility**: Handles incoming HTTP requests and returns JSON responses.
- Annotated with `@RestController` and `@RequestMapping("/api/tasks")`.
- Translates outcomes into standard HTTP status codes (`201 Created`, `200 OK`, `204 No Content`, `404 Not Found`).

### 6. `exception` Package (`GlobalExceptionHandler.java`, `ResourceNotFoundException.java`, `ErrorResponse.java`)
- **Responsibility**: Centralized error management using `@RestControllerAdvice`.
- Catches errors like 404 (Task not found) or 400 (Validation failed) and sends clean, informative JSON responses.

---

## 🗄 Database Setup (MySQL)

### Step 1: Open MySQL Workbench or MySQL Command Line Client
Log into your MySQL server:
```bash
mysql -u root -p
```

### Step 2: Create the Database
Execute the SQL command:
```sql
CREATE DATABASE IF NOT EXISTS student_task_manager;
```

*(Note: Spring Boot is configured with `createDatabaseIfNotExist=true` so it can also create the database automatically if your MySQL user has sufficient privileges).*

### Step 3 (Optional): Initialize Schema & Seed Data
You can inspect or run the SQL files located in `src/main/resources/`:
- `src/main/resources/schema.sql` (Creates table `tasks` with indexes)
- `src/main/resources/data.sql` (Inserts realistic CS student assignments)

---

## ⚙ Configuration (`application.properties`)

Open `src/main/resources/application.properties` and verify your MySQL credentials:

```properties
# MySQL Connection URL
spring.datasource.url=jdbc:mysql://localhost:3306/student_task_manager?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# Database Credentials - Change password to match your MySQL setup
spring.datasource.username=root
spring.datasource.password=root

# Hibernate ORM Settings
# 'update' automatically creates/updates database tables based on Task.java
spring.jpa.hibernate.ddl-auto=update

# Shows generated SQL in console for learning
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## 🚀 How to Run the Application

### 1. Running with Maven (Terminal)

Open PowerShell or Command Prompt in the project folder (`Task-Manager`):

```powershell
# Compile and run the application
mvn spring-boot:run
```

Once started, open your web browser and go to:
👉 **`http://localhost:8080/`**

---

### 2. Running in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Click **File -> Open...** and select the `Task-Manager` folder.
3. Wait for IntelliJ to sync Maven dependencies (bottom status bar).
4. Navigate to `src/main/java/com/student/taskmanager/StudentTaskManagerApplication.java`.
5. Right-click anywhere in the file and select **Run 'StudentTaskManagerApplication'** (or click the green Play icon).
6. Open your browser at `http://localhost:8080/`.

---

### 3. Running in VS Code

1. Open **VS Code**.
2. Install the **Extension Pack for Java** and **Spring Boot Extension Pack** from the Extensions tab (`Ctrl+Shift+X`).
3. Open the `Task-Manager` folder.
4. Press `F5` or click **Run -> Start Debugging**.
5. Open your browser at `http://localhost:8080/`.

---

## 📡 REST API Documentation & Postman Guide

Base URL: `http://localhost:8080/api/tasks`

| Method | Endpoint | Query Parameters | Description |
|---|---|---|---|
| `POST` | `/api/tasks` | None | Create a new task |
| `GET` | `/api/tasks` | `keyword`, `status`, `priority`, `sortBy`, `sortDir` | Get all tasks |
| `GET` | `/api/tasks/{id}` | None | Get task by ID |
| `PUT` | `/api/tasks/{id}` | None | Update task details |
| `PATCH` | `/api/tasks/{id}/status` | `status=COMPLETED` or JSON body | Update task status |
| `DELETE` | `/api/tasks/{id}` | None | Delete a task |
| `GET` | `/api/tasks/search` | `keyword=java` | Search tasks by title/subject |
| `GET` | `/api/tasks/filter` | `status=PENDING&priority=HIGH` | Filter tasks |
| `GET` | `/api/tasks/stats` | None | Get dashboard counts |

### Sample cURL Commands

#### 1. Create a Task (POST)
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Operating Systems Lab 3",
    "subject": "Operating Systems",
    "description": "Implement FIFO and LRU page replacement algorithms.",
    "priority": "HIGH",
    "status": "PENDING",
    "deadline": "2026-11-10"
  }'
```

#### 2. Get All Tasks (GET)
```bash
curl http://localhost:8080/api/tasks
```

#### 3. Update Task Status (PATCH)
```bash
curl -X PATCH "http://localhost:8080/api/tasks/1/status?status=COMPLETED"
```

#### 4. Delete a Task (DELETE)
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

#### 5. Get Dashboard Statistics (GET)
```bash
curl http://localhost:8080/api/tasks/stats
```

---

## 🧪 Testing the Application

Automated unit tests and Spring MVC tests are included in `src/test/java/`.

To run all automated tests:
```powershell
mvn test
```

The tests use an in-memory **H2 database** so they run without altering your actual MySQL database.
