package com.springboot.taskmanager.service;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public List<Task> findAllUserTasks(User user) {
        return taskRepository.findByUser(user);
    }

    public Optional<Task> findTaskByIdAndUser(Long taskId, User user) {
        return Optional.ofNullable(taskRepository.findByIdAndUser(taskId, user));
    }

    public Task saveTask(Task task, User user) {
        task.setUser(user); // Ensure the task is correctly linked to the user
        return taskRepository.save(task);
    }

    public boolean deleteTaskByIdAndUser(Long taskId, User user) {
        Task taskToDelete = taskRepository.findByIdAndUser(taskId, user);
        if (taskToDelete != null) {
            taskRepository.delete(taskToDelete);
            return true;
        }
        return false;
    }

}
