package com.example.security.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.security.dto.TaskRequest;
import com.example.security.dto.TaskResponse;
import com.example.security.entity.ROLE;
import com.example.security.entity.Task;
import com.example.security.entity.User;
import com.example.security.exception.ResourceNotFoundException;
import com.example.security.exception.UnauthorizedException;
import com.example.security.repository.TaskRepository;
import com.example.security.repository.UserRepository;
import com.example.security.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
    private final UserRepository userRepository;

	/**
	 * Create a new task for the authenticated user
	 */
	@Transactional
	public TaskResponse createTask(TaskRequest request) {
		User currentUser = getCurrentUser();

		Task task = Task.builder().title(request.getTitle()).description(request.getDescription())
				.user(currentUser)
				.build();

		Task savedTask = taskRepository.save(task);
		return mapToTaskResponse(savedTask);
	}

	/**
	 * Get all tasks (role-based filtering)
	 * - USER: Only their own tasks
	 * - MANAGER/ADMIN: All tasks
	 */
	public List<TaskResponse> getAllTasks() {
		User currentUser = getCurrentUser();
		List<Task> tasks;
		// Check user role
		if (currentUser.getRole() == ROLE.USER) {
			// Regular users see only their tasks
			tasks = taskRepository.findByUserId(currentUser.getId()).orElse(Collections.EMPTY_LIST);
		} else {
			// Managers and Admins see all tasks
			tasks = taskRepository.findAll();
		}
		return tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList());
	}

	/**
	 * Get a single task by ID
	 */
	public TaskResponse getTaskById(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		User currentUser = getCurrentUser();

		// Check if user has permission to view this task
		if (currentUser.getRole() == ROLE.USER && !task.getUser().getId().equals(currentUser.getId())) {
			throw new UnauthorizedException("You don't have permission to view this task");
		}

		return mapToTaskResponse(task);
	}

	/**
	 * Update a task
	 * - Owner can update their own tasks
	 * - MANAGER/ADMIN can update any task
	 */
	@Transactional
	public TaskResponse updateTask(Long taskId, TaskRequest request) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		User currentUser = getCurrentUser();

		// Check permissions
		boolean isOwner = task.getUser().getId().equals(currentUser.getId());
		boolean isManagerOrAdmin = currentUser.getRole() == ROLE.MANAGER ||
				currentUser.getRole() == ROLE.ADMIN;

		if (!isOwner && !isManagerOrAdmin) {
			throw new UnauthorizedException("You don't have permission to update this task");
		}

		// Update task fields
		task.setTitle(request.getTitle());
		task.setDescription(request.getDescription());

		Task updatedTask = taskRepository.save(task);

		return mapToTaskResponse(updatedTask);
	}

	/**
	 * Delete a task
	 * - Owner can delete their own tasks
	 * - ADMIN can delete any task
	 */
	@Transactional
	public void deleteTask(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

		User currentUser = getCurrentUser();

		// Check permissions
		boolean isOwner = task.getUser().getId().equals(currentUser.getId());
		boolean isAdmin = currentUser.getRole() == ROLE.ADMIN;

		if (!isOwner && !isAdmin) {
			throw new UnauthorizedException("You don't have permission to delete this task");
		}

		taskRepository.delete(task);
	}

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

	/**
	 * Helper method to convert Task entity to TaskResponse DTO
	 */
	private TaskResponse mapToTaskResponse(Task task) {
		return TaskResponse.builder().id(task.getId()).title(task.getTitle()).description(task.getDescription())
				.status(task.getStatus()).username(task.getUser().getUsername())
				.createdAt(task.getCreatedAt()).updatedAt(task.getUpdatedAt()).build();
	}
}
