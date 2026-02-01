package com.springboot.taskmanager.service;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void taskServiceFindAllUserTasks() {
        User user = User.builder()
                .username("findAllTasksUser")
                .password("pw")
                .email("findAllTaskUsers@example.com")
                .role("ROLE_USER")
                .build();

        Task task1 = Task.builder()
                .id(1L)
                .title("Task 1")
                .description("Description 1")
                .user(user)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .description("Description 2")
                .user(user)
                .build();

        when(taskRepository.findByUser(Mockito.any(User.class))).thenReturn(List.of(task1, task2));

        List<Task> tasks = taskService.findAllUserTasks(user);

        assertEquals(2, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
        assertEquals("Task 2", tasks.get(1).getTitle());

        verify(taskRepository).findByUser(user);
    }


    @Test
    void taskServiceFindTaskByIdAndUser_Found() {
        User user = User.builder().username("u").password("pw").email("e").role("ROLE_USER").build();
        Task task = Task.builder().id(1L).title("T1").description("D").user(user).build();

        when(taskRepository.findByIdAndUser(1L, user)).thenReturn(task);

        Optional<Task> result = taskService.findTaskByIdAndUser(1L, user);

        assertTrue(result.isPresent());
        assertEquals("T1", result.get().getTitle());
        verify(taskRepository).findByIdAndUser(1L, user);
    }

    @Test
    void taskServiceFindTaskByIdAndUser_NotFound() {
        User user = User.builder().username("u2").password("pw").email("e2").role("ROLE_USER").build();

        when(taskRepository.findByIdAndUser(99L, user)).thenReturn(null);

        Optional<Task> result = taskService.findTaskByIdAndUser(99L, user);

        assertFalse(result.isPresent());
        verify(taskRepository).findByIdAndUser(99L, user);
    }

    @Test
    void taskServiceSaveTask_SetsUserAndSaves() {
        User user = User.builder().username("saveUser").password("pw").email("save@example.com").role("ROLE_USER").build();
        Task task = Task.builder().title("New Task").description("New Desc").build();

        when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(5L); // simulate save assigning an ID
            return t;
        });

        Task saved = taskService.saveTask(task, user);

        // capture saved argument to ensure user was set before save
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task captured = captor.getValue();

        assertEquals(user, captured.getUser());
        assertEquals(user, saved.getUser());
        assertEquals(5L, saved.getId());
    }

    @Test
    void taskServiceDeleteTaskByIdAndUser_Success() {
        User user = User.builder().username("delUser").password("pw").email("del@example.com").role("ROLE_USER").build();
        Task task = Task.builder().id(10L).title("ToDelete").description("D").user(user).build();

        when(taskRepository.findByIdAndUser(10L, user)).thenReturn(task);

        boolean deleted = taskService.deleteTaskByIdAndUser(10L, user);

        assertTrue(deleted);
        verify(taskRepository).findByIdAndUser(10L, user);
        verify(taskRepository).delete(task);
    }

    @Test
    void taskServiceDeleteTaskByIdAndUser_NotFound() {
        User user = User.builder().username("delUser2").password("pw").email("del2@example.com").role("ROLE_USER").build();

        when(taskRepository.findByIdAndUser(11L, user)).thenReturn(null);

        boolean deleted = taskService.deleteTaskByIdAndUser(11L, user);

        assertFalse(deleted);
        verify(taskRepository).findByIdAndUser(11L, user);
        Mockito.verify(taskRepository, Mockito.never()).delete(Mockito.any());
    }
}
