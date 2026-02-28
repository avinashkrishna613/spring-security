package com.example.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.security.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
     List<Task> findByUserId(Long userId);
}
