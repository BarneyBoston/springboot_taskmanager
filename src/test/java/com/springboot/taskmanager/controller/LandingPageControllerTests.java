package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = LandingPageController.class)
@ExtendWith(MockitoExtension.class)
class LandingPageControllerTests {

    @Autowired
    @SuppressWarnings("unused")
    private MockMvc mockMvc;

    @MockitoBean
    @SuppressWarnings("unused")
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    void landingPageControllerShowLandingPage() throws Exception {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);
        when(userService.findUserByUsername(username)).thenReturn(user);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("landing-page"));
    }
}
