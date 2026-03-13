package com.example.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.security.dto.AuthResponse;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.entity.ROLE;
import com.example.security.entity.User;
import com.example.security.exception.ResourceNotFoundException;
import com.example.security.repository.UserRepository;
import com.example.security.security.JwtUtils;
import com.example.security.security.UserDetailsImpl;

import io.micrometer.common.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public void register(RegisterRequest registerRequest) {
        if (StringUtils.isBlank(registerRequest.getUsername()) || StringUtils.isBlank(registerRequest.getPassword()) || StringUtils.isBlank(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Username and password must not be blank");
        }

        userRepository.findByUsername(registerRequest.getUsername()).ifPresent(user -> {
            throw new IllegalArgumentException("Username already exists");
        });

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(encodedPassword)
                .role(registerRequest.getRole() != null ? registerRequest.getRole() : ROLE.USER)
                .build();
        userRepository.save(user);
    }

    /**
     * The ideal flow for login is, /auth/login -> user gives details like username, password -> controller hits -> calls authservice.login
     * Create unauthenticated object -> authentication manager -> authentication provider(daoauthprovider) ->
     *
     * @param loginRequest
     * @return
     */
    public AuthResponse login(LoginRequest loginRequest) {
        if (StringUtils.isBlank(loginRequest.getUsername()) || StringUtils.isBlank(loginRequest.getPassword())) {
            throw new IllegalArgumentException("Username and password must not be blank");
        }

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token based on principle of the authentication object
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Return response with token and user info
        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );
    }

    public User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("No authentication found in security context");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
