package com.example.security.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.dto.UserResponse;
import com.example.security.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // All methods require ADMIN role
public class UserManagementController {

	private final UserService userService;

	/**
	 * Get all users. GET /users. ADMIN only can perform this.
	 */
	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		List<UserResponse> users = userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	/**
	 * Get user by Id. GET /users/{id}. ADMIN only can perform
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
		UserResponse user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}

	@PutMapping("/{id}/role")
	public ResponseEntity<UserResponse> updateUserRole(
			@PathVariable Long id,
			@RequestParam String role) {

		UserResponse user = userService.updateUserRole(id, role);
		return ResponseEntity.ok(user);
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<UserResponse> toggleUserStatus(
			@PathVariable Long id,
			@RequestParam boolean enabled) {

		UserResponse user = userService.toggleUserStatus(id, enabled);
		return ResponseEntity.ok(user);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
