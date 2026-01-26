package com.springboot.taskmanager.repository;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TaskManagerRepositoryTests {

    @Autowired
    @SuppressWarnings("unused")
    private TaskRepository taskRepository;

    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Test
    void taskRepositorySaveAndFindByUser() {
        // create and save a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        // create and save a task for that user
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("A task for testing");
        task.setUser(user);

        Task saved = taskRepository.save(task);
        assertThat(saved.getId()).isNotNull();

        // find by user
        List<Task> tasks = taskRepository.findByUser(user);
        assertThat(tasks).isNotEmpty().hasSize(1);
        assertThat(tasks.getFirst().getTitle()).isEqualTo("Test Task");

        // find by id and user
        Task found = taskRepository.findByIdAndUser(saved.getId(), user);
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void taskRepositoryFindByUserWhenNoTasks() {
        User user = new User();
        user.setUsername("emptyuser");
        user.setPassword("password");
        user = userRepository.save(user);

        List<Task> tasks = taskRepository.findByUser(user);
        assertThat(tasks).isEmpty();
    }

    @Test
    void taskRepositoryFindByIdAndUserWithDifferentUserReturnsNull() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pw");
        user1 = userRepository.save(user1);

        Task task = new Task();
        task.setTitle("User1 Task");
        task.setUser(user1);
        Task saved = taskRepository.save(task);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pw");
        user2 = userRepository.save(user2);

        Task found = taskRepository.findByIdAndUser(saved.getId(), user2);
        assertThat(found).isNull();
    }

    @Test
    void userRepositoryFindByUsername() {
        User user = new User();
        user.setUsername("findme");
        user.setPassword("pw");
        userRepository.save(user);

        User found = userRepository.findByUsername("findme");
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("findme");
    }

    @Test
    void userRepositoryFindByUsernameNotFound() {
        User found = userRepository.findByUsername("doesnotexist");
        assertThat(found).isNull();
    }

    @Test
    void taskRepositorySaveWithoutTitleShouldFail() {
        User user = new User();
        user.setUsername("withuser");
        user.setPassword("pw");
        user = userRepository.save(user);

        Task task = new Task();
        // no title set -> should violate not-null constraint
        task.setUser(user);

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void taskRepositorySaveWithoutUserShouldFail() {
        Task task = new Task();
        task.setTitle("NoUser");

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void userRepositoryCascadeSaveTasksWhenSavingUser() {
        User user = new User();
        user.setUsername("cascadeUser");
        user.setPassword("pw");

        Task task = new Task();
        task.setTitle("Cascade Task");
        task.setDescription("Created via cascade");
        task.setUser(user);

        List<Task> list = new ArrayList<>();
        list.add(task);
        user.setTasks(list);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();

        List<Task> tasks = taskRepository.findByUser(savedUser);
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.getFirst().getTitle()).isEqualTo("Cascade Task");
    }

    @Test
    void taskRepositoryUpdateTask() {
        User user = new User();
        user.setUsername("upUser");
        user.setPassword("pw");
        user = userRepository.save(user);

        Task task = new Task();
        task.setTitle("Old Title");
        task.setStatus("TO_DO");
        task.setUser(user);
        Task saved = taskRepository.save(task);

        saved.setTitle("New Title");
        saved.setStatus("DONE");
        taskRepository.saveAndFlush(saved);

        Task updated = taskRepository.findByIdAndUser(saved.getId(), user);
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("New Title");
        assertThat(updated.getStatus()).isEqualTo("DONE");
    }

    @Test
    void taskRepositoryDeleteTaskById() {
        User user = new User();
        user.setUsername("delUser");
        user.setPassword("pw");
        user = userRepository.save(user);

        Task task = new Task();
        task.setTitle("ToDelete");
        task.setUser(user);
        Task saved = taskRepository.save(task);

        taskRepository.deleteById(saved.getId());
        Optional<Task> opt = taskRepository.findById(saved.getId());
        assertThat(opt).isEmpty();

        List<Task> tasks = taskRepository.findByUser(user);
        assertThat(tasks).isEmpty();
    }

    @Test
    void userRepositoryDeleteUserCascadesTasks() {
        User user = new User();
        user.setUsername("userToDelete");
        user.setPassword("pw");

        Task task = new Task();
        task.setTitle("TaskToCascadeDelete");
        task.setUser(user);

        List<Task> list = new ArrayList<>();
        list.add(task);
        user.setTasks(list);

        User savedUser = userRepository.save(user);
        Long taskId = taskRepository.findByUser(savedUser).getFirst().getId();

        userRepository.deleteById(savedUser.getId());

        Optional<Task> opt = taskRepository.findById(taskId);
        assertThat(opt).isEmpty();
    }

    @Test
    void userRepositoryOrphanRemovalWhenRemovingFromList() {
        User user = new User();
        user.setUsername("orphanUser");
        user.setPassword("pw");

        Task task = new Task();
        task.setTitle("OrphanTask");
        task.setUser(user);

        List<Task> list = new ArrayList<>();
        list.add(task);
        user.setTasks(list);

        User savedUser = userRepository.save(user);
        Long taskId = taskRepository.findByUser(savedUser).getFirst().getId();

        // remove task from user's list and save user
        savedUser.getTasks().clear();
        userRepository.saveAndFlush(savedUser);

        Optional<Task> opt = taskRepository.findById(taskId);
        assertThat(opt).isEmpty();
    }

    @Test
    void userRepositoryDuplicateUsernameThrows() {
        User user1 = new User();
        user1.setUsername("duplicate");
        user1.setPassword("pw");
        userRepository.saveAndFlush(user1);

        User user2 = new User();
        user2.setUsername("duplicate");
        user2.setPassword("pw");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    void userRepositoryHelpers() {
        long initial = userRepository.count();

        User user = new User();
        user.setUsername("helperUser");
        user.setPassword("pw");
        user = userRepository.save(user);

        assertThat(userRepository.count()).isEqualTo(initial + 1);
        assertThat(userRepository.existsById(user.getId())).isTrue();

        List<User> all = userRepository.findAll();
        assertThat(all).isNotEmpty();

        // cleanup
        userRepository.deleteById(user.getId());
        assertThat(userRepository.existsById(user.getId())).isFalse();
    }
}
