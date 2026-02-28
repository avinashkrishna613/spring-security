package com.example.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.security.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
     Optional<List<Task>> findByUserId(Long userId);
}
