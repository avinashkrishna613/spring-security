package com.example.security.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.security.dto.UserResponse;
import com.example.security.entity.ROLE;
import com.example.security.entity.User;
import com.example.security.exception.BadRequestException;
import com.example.security.exception.ResourceNotFoundException;
import com.example.security.exception.UnauthorizedException;
import com.example.security.repository.UserRepository;
import com.example.security.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	/**
	 * Get all users (ADMIN only)
	 * This method should be called from a controller with @PreAuthorize("hasRole('ADMIN')")
	 */
	public List<UserResponse> getAllUsers() {
		List<User> users = userRepository.findAll();
		return users.stream().map(this::mapToUserResponse).collect(Collectors.toList());
	}

	/**
	 * Get user by ID
	 */
	public UserResponse getUserById(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		return mapToUserResponse(user);
	}

	/**
	 * Update user role (ADMIN only)
	 * This method should be called from a controller with @PreAuthorize("hasRole('ADMIN')")
	 */
	@Transactional
	public UserResponse updateUserRole(Long userId, String newRole) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		// Validate role
		ROLE role;
		try {
			role = ROLE.valueOf(newRole.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Invalid role: " + newRole + ". Valid roles are: USER, MANAGER, ADMIN");
		}

		// Prevent admin from removing their own admin role
		User currentUser = getCurrentUser();
		if (user.getId().equals(currentUser.getId()) && role != ROLE.ADMIN) {
			throw new BadRequestException("You cannot remove your own admin privileges");
		}

		user.setRole(role);
		User updatedUser = userRepository.save(user);

		return mapToUserResponse(updatedUser);
	}

	/**
	 * Enable or disable a user (ADMIN only)
	 */
	@Transactional
	public UserResponse toggleUserStatus(Long userId, boolean enabled) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		// Prevent admin from disabling themselves
		User currentUser = getCurrentUser();
		if (user.getId().equals(currentUser.getId()) && !enabled) {
			throw new BadRequestException("You cannot disable your own account");
		}

		user.setEnabled(enabled);
		User updatedUser = userRepository.save(user);

		return mapToUserResponse(updatedUser);
	}

	/**
	 * Delete a user (ADMIN only)
	 */
	@Transactional
	public void deleteUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		// Prevent admin from deleting themselves
		User currentUser = getCurrentUser();
		if (user.getId().equals(currentUser.getId())) {
			throw new BadRequestException("You cannot delete your own account");
		}

		userRepository.delete(user);
	}

	/**
	 * Helper method to get current authenticated user from SecurityContext
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
	 * Helper method to convert User entity to UserResponse DTO
	 */
	private UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
				.role(user.getRole().name()).enabled(user.isEnabled()).build();
	}
}
