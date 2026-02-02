package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.service.TaskService;
import com.springboot.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@ExtendWith(MockitoExtension.class)
class TaskControllerTests {

    @Autowired
    @SuppressWarnings("unused")
    private MockMvc mockMvc;

    @MockitoBean
    @SuppressWarnings("unused")
    private TaskService taskService;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerListTasks() throws Exception {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);
        when(userService.findUserByUsername(username)).thenReturn(user);

        Task sampleTask = Task.builder().title("Sample").build();
        when(taskService.findAllUserTasks(user)).thenReturn(Collections.singletonList(sampleTask));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("task-list"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("task"));

        verify(taskService).findAllUserTasks(user);
    }

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerSaveTask() throws Exception {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);
        when(userService.findUserByUsername(username)).thenReturn(user);

        mockMvc.perform(post("/tasks/save")
                        .param("title", "SaveTask")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(taskService).saveTask(any(Task.class), eq(user));
    }

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerUpdateStatus() throws Exception {
        String username = "testuser";
        Long taskId = 1L;
        String status = "DONE";

        User user = new User();
        user.setUsername(username);

        when(userService.findUserByUsername(username)).thenReturn(user);

        Task existingTask = Task.builder().id(taskId).title("Test Task").status("TODO").build();

        when(taskService.findTaskByIdAndUser(taskId, user))
                .thenReturn(Optional.of(existingTask));

        mockMvc.perform(post("/tasks/update-status")
                        .param("id", taskId.toString())
                        .param("newStatus", status)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("successMessage", "Task 'Test Task' moved to DONE."));

        verify(taskService).saveTask(any(Task.class), eq(user));
    }

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerDeleteTask() throws Exception {
        String username = "testuser";
        Long taskId = 3L;

        User user = new User();
        user.setUsername(username);

        when(userService.findUserByUsername(username)).thenReturn(user);

        Task existingTask = Task.builder().id(taskId).title("Test Task").status("TODO").build();

        when(taskService.deleteTaskByIdAndUser(existingTask.getId(), user))
                .thenReturn(true);

        mockMvc.perform(get(String.format("/tasks/delete/%d", taskId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"))
                .andExpect(flash().attribute("successMessage", "Task deleted successfully."));

        verify(taskService).deleteTaskByIdAndUser(taskId,user);
    }

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerSaveTaskEmptyTitle() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findUserByUsername("testuser")).thenReturn(user);

        mockMvc.perform(post("/tasks/save")
                        .param("title", "   ")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Task title cannot be empty."));

        verify(taskService, Mockito.never()).saveTask(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void taskControllerUpdateStatusTaskNotFound() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findUserByUsername("testuser")).thenReturn(user);

        when(taskService.findTaskByIdAndUser(99L, user)).thenReturn(Optional.empty());

        mockMvc.perform(post("/tasks/update-status")
                        .param("id", "99")
                        .param("newStatus", "DONE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Task not found or unauthorized access."));
    }
}
