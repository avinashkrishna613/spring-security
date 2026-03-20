package com.example.security.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.dto.TaskRequest;
import com.example.security.dto.TaskResponse;
import com.example.security.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Get all tasks (role-based filtering)
     * GET /tasks
     * - USER: returns only their tasks
     * - MANAGER/ADMIN: returns all tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get a single task by ID
     * GET /tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Create a new task
     * POST /tasks
     * Any authenticated user can create tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return new ResponseEntity<>(task, HttpStatus.CREATED);
    }

    /**
     * Update a task
     * PUT /tasks/{id}
     * Owner or MANAGER/ADMIN can update
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    /**
     * Delete a task
     * DELETE /tasks/{id}
     * Owner or ADMIN can delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @taskService.isTaskOwner(#id)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
