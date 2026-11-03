package com.student.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * StudentTaskManagerApplication
 * 
 * Main entry point of the Spring Boot application.
 * 
 * Key Spring Boot Concepts:
 * - @SpringBootApplication: A meta-annotation that encapsulates three critical annotations:
 *   1. @Configuration: Tags the class as a source of bean definitions.
 *   2. @EnableAutoConfiguration: Tells Spring Boot to automatically configure Spring
 *      based on the jar dependencies added in pom.xml (e.g. DataSource, Hibernate, Jackson, Tomcat).
 *   3. @ComponentScan: Automatically scans the current package ('com.student.taskmanager')
 *      and all sub-packages for @Component, @Service, @Repository, and @RestController classes.
 * 
 * Execution Flow:
 * 1. Java starts in main().
 * 2. SpringApplication.run() initializes the ApplicationContext.
 * 3. Embedded Tomcat web server is launched on port 8080.
 * 4. Spring Data JPA connects to MySQL and updates the database schema.
 * 5. Static resources (index.html, styles.css, app.js) and REST APIs are served.
 */
@SpringBootApplication
public class StudentTaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentTaskManagerApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  Student Task Manager Application is Running!");
        System.out.println("  Web UI: http://localhost:8080/");
        System.out.println("  REST API Base: http://localhost:8080/api/tasks");
        System.out.println("==========================================================");
    }
}
