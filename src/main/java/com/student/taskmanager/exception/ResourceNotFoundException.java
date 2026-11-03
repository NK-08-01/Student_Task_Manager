package com.student.taskmanager.exception;

/**
 * ResourceNotFoundException
 * 
 * Custom runtime exception thrown when a requested resource (e.g., Task with given ID)
 * does not exist in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
