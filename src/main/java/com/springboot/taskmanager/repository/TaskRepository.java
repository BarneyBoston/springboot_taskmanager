package com.springboot.taskmanager.repository;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    Task findByIdAndUser(Long id, User user);
}
