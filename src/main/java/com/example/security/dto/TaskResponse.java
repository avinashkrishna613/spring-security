package com.example.security.dto;

import java.time.LocalDateTime;

import com.example.security.entity.TASKSTATUS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TASKSTATUS status;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
