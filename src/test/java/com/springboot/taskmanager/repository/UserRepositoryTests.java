package com.springboot.taskmanager.repository;

import com.springboot.taskmanager.entity.Task;
import com.springboot.taskmanager.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTests {


    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Autowired
    @SuppressWarnings("unused")
    private TaskRepository taskRepository;

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

    @Test
    void userRepositoryGetAll() {
        long initial = userRepository.count();

        User user1 = User.builder()
                .username("getall1")
                .password("pw")
                .email("getall1@example.com")
                .role("ROLE_USER")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("getall2")
                .password("pw")
                .email("getall2@example.com")
                .role("ROLE_USER")
                .build();
        userRepository.save(user2);

        User user3 = User.builder()
                .username("getall3")
                .password("pw")
                .email("getall3@example.com")
                .role("ROLE_USER")
                .build();
        userRepository.save(user3);

        assertThat(userRepository.count()).isEqualTo(initial + 3);

        List<User> all = userRepository.findAll();
        assertThat(all).isNotEmpty();
    }
}
