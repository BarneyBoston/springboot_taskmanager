package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.constants.ApplicationConstants;
import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.service.TaskService;
import com.springboot.taskmanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
@RequestMapping(ApplicationConstants.TASKS_PAGE_URL)
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findUserByUsername(username);
    }

    @GetMapping
    public String listTasks(Model model, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        model.addAttribute("tasks", taskService.findAllUserTasks(user));

        model.addAttribute("task", new Task());

        return "task-list";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute("task") Task task,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(authentication);

        if (task.getId() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Cannot update through the Quick Add form.");
            return ApplicationConstants.REDIRECT_MESSAGE + ApplicationConstants.TASKS_PAGE_URL;
        }

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Task title cannot be empty.");
            return ApplicationConstants.REDIRECT_MESSAGE + ApplicationConstants.TASKS_PAGE_URL;
        }

        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("TO_DO");
        }

        taskService.saveTask(task, user);
        redirectAttributes.addFlashAttribute("successMessage", "New task added successfully!");
        return ApplicationConstants.REDIRECT_MESSAGE + ApplicationConstants.TASKS_PAGE_URL;
    }

    @PostMapping("/update-status")
    public String updateTaskStatus(@RequestParam("id") Long taskId,
                                   @RequestParam("newStatus") String newStatus,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(authentication);
        Optional<Task> optionalTask = taskService.findTaskByIdAndUser(taskId, user);

        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setStatus(newStatus);
            taskService.saveTask(task, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Task '%s' moved to %s.", task.getTitle(), newStatus));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found or unauthorized access.");
        }

        return ApplicationConstants.REDIRECT_MESSAGE + ApplicationConstants.TASKS_PAGE_URL;
    }


    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(authentication);

        if (taskService.deleteTaskByIdAndUser(id, user)) {
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Task not found or unauthorized access.");
        }

        return ApplicationConstants.REDIRECT_MESSAGE + ApplicationConstants.TASKS_PAGE_URL;
    }
}