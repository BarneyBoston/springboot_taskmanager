package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.dto.UserRegistrationDto;
import com.springboot.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTests {

    @Autowired
    @SuppressWarnings("unused")
    private MockMvc mockMvc;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserService userService;

    @Test
    void authControllerLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void authControllerRegister() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userDto"))
                .andExpect(view().name("registration"));
    }

    @Test
    void shouldRedirectToLoginWhenRegistrationIsSuccessful() throws Exception {
        mockMvc.perform(post("/register")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .param("email", "test@example.com")
                        .param("username", "testuser")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).registerNewUser(any(UserRegistrationDto.class));
    }

    @Test
    void shouldReturnRegisterViewWithErrorMessageWhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/register")
                        .param("password", "pass1")
                        .param("confirmPassword", "pass2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("registrationError", "Error: Passwords do not match!"));

        verify(userService, never()).registerNewUser(any());
    }

    @Test
    void shouldReturnRegisterViewWithErrorMessageWhenUserAlreadyExists() throws Exception {
        String errorMessage = "User with this email already exists!";

        doThrow(new RuntimeException(errorMessage))
                .when(userService).registerNewUser(any(UserRegistrationDto.class));

        mockMvc.perform(post("/register")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("registrationError", errorMessage));
    }
}
