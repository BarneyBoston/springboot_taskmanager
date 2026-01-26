package com.springboot.taskmanager.repository;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
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
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();
        user = userRepository.save(user);

        Task task = Task.builder()
                .title("Test Task")
                .description("A task for testing")
                .dueDate(LocalDate.now().plusDays(1))
                .priority("HIGH")
                .status("TO_DO")
                .user(user)
                .build();

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
        User user = User.builder()
                .username("emptyuser")
                .password("password")
                .email("empty@example.com")
                .role("ROLE_USER")
                .build();
        user = userRepository.save(user);

        List<Task> tasks = taskRepository.findByUser(user);
        assertThat(tasks).isEmpty();
    }

    @Test
    void taskRepositoryFindByIdAndUserWithDifferentUserReturnsNull() {
        User user1 = User.builder()
                .username("user1")
                .password("pw")
                .email("u1@example.com")
                .role("ROLE_USER")
                .build();
        user1 = userRepository.save(user1);

        Task task = Task.builder()
                .title("User1 Task")
                .description("desc")
                .dueDate(LocalDate.now())
                .priority("LOW")
                .status("TO_DO")
                .user(user1)
                .build();
        Task saved = taskRepository.save(task);

        User user2 = User.builder()
                .username("user2")
                .password("pw")
                .email("u2@example.com")
                .role("ROLE_USER")
                .build();
        user2 = userRepository.save(user2);

        Task found = taskRepository.findByIdAndUser(saved.getId(), user2);
        assertThat(found).isNull();
    }

    @Test
    void userRepositoryFindByUsername() {
        User user = User.builder()
                .username("findme")
                .password("pw")
                .email("findme@example.com")
                .role("ROLE_USER")
                .build();
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
        User user = User.builder()
                .username("withuser")
                .password("pw")
                .email("with@example.com")
                .role("ROLE_USER")
                .build();
        user = userRepository.save(user);

        Task task = Task.builder()
                .description("no title")
                .dueDate(LocalDate.now())
                .priority("MEDIUM")
                .status("TO_DO")
                .user(user)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void taskRepositorySaveWithoutUserShouldFail() {
        Task task = Task.builder()
                .title("NoUser")
                .description("no user")
                .dueDate(LocalDate.now())
                .priority("LOW")
                .status("TO_DO")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    void userRepositoryCascadeSaveTasksWhenSavingUser() {
        User user = User.builder()
                .username("cascadeUser")
                .password("pw")
                .email("cascade@example.com")
                .role("ROLE_USER")
                .build();

        Task task = Task.builder()
                .title("Cascade Task")
                .description("Created via cascade")
                .dueDate(LocalDate.now())
                .priority("MEDIUM")
                .status("TO_DO")
                .user(user)
                .build();

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
        User user = User.builder()
                .username("upUser")
                .password("pw")
                .email("up@example.com")
                .role("ROLE_USER")
                .build();
        user = userRepository.save(user);

        Task task = Task.builder()
                .title("Old Title")
                .status("TO_DO")
                .description("old")
                .dueDate(LocalDate.now())
                .priority("LOW")
                .user(user)
                .build();
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
        User user = User.builder()
                .username("delUser")
                .password("pw")
                .email("del@example.com")
                .role("ROLE_USER")
                .build();
        user = userRepository.save(user);

        Task task = Task.builder()
                .title("ToDelete")
                .description("delete me")
                .dueDate(LocalDate.now())
                .priority("LOW")
                .status("TO_DO")
                .user(user)
                .build();
        Task saved = taskRepository.save(task);

        taskRepository.deleteById(saved.getId());
        Optional<Task> opt = taskRepository.findById(saved.getId());
        assertThat(opt).isEmpty();

        List<Task> tasks = taskRepository.findByUser(user);
        assertThat(tasks).isEmpty();
    }

    @Test
    void userRepositoryDeleteUserCascadesTasks() {
        User user = User.builder()
                .username("userToDelete")
                .password("pw")
                .email("delete@example.com")
                .role("ROLE_USER")
                .build();

        Task task = Task.builder()
                .title("TaskToCascadeDelete")
                .description("cascade")
                .dueDate(LocalDate.now())
                .priority("MEDIUM")
                .status("TO_DO")
                .user(user)
                .build();

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
        User user = User.builder()
                .username("orphanUser")
                .password("pw")
                .email("orph@example.com")
                .role("ROLE_USER")
                .build();

        Task task = Task.builder()
                .title("OrphanTask")
                .description("orphan")
                .dueDate(LocalDate.now())
                .priority("LOW")
                .status("TO_DO")
                .user(user)
                .build();

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
        User user1 = User.builder()
                .username("duplicate")
                .password("pw")
                .email("dup@example.com")
                .role("ROLE_USER")
                .build();
        userRepository.saveAndFlush(user1);

        User user2 = User.builder()
                .username("duplicate")
                .password("pw")
                .email("dup2@example.com")
                .role("ROLE_USER")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    void userRepositoryHelpers() {
        long initial = userRepository.count();

        User user = User.builder()
                .username("helperUser")
                .password("pw")
                .email("help@example.com")
                .role("ROLE_USER")
                .build();
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
