package com.example.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.dto.AuthResponse;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.dto.UserResponse;
import com.example.security.service.AuthService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody @NonNull RegisterRequest registerRequest) {
		String response = authService.register(registerRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @NonNull LoginRequest loginRequest) {
		AuthResponse login = authService.login(loginRequest);
		return new ResponseEntity<>(login, HttpStatus.CREATED);
	}

	/**
	 * Get current authenticated user details
	 * GET /auth/me
	 */
	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser() {
		UserResponse response = authService.getCurrentUser();
		return ResponseEntity.ok(response);
	}
}
