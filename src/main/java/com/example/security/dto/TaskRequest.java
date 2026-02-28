package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
