package com.example.security.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        // Set HTTP status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Set response content type to JSON
        response.setContentType("application/json");

        // Create JSON error response
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", 401);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", authException.getMessage());
        errorDetails.put("path", request.getServletPath());
        errorDetails.put("timestamp", LocalDateTime.now());

        // Write JSON to response
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorDetails));
    }
}
