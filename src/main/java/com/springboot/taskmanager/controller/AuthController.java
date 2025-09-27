package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.constants.ApplicationConstants;
import com.springboot.taskmanager.dto.UserRegistrationDto;
import com.springboot.taskmanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(ApplicationConstants.LOGIN_PAGE_URL)
    public String login() {
        return "login";
    }


    @GetMapping(ApplicationConstants.REGISTER_PAGE_URL)
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return ApplicationConstants.VIEW_REGISTER;
    }

    @PostMapping(ApplicationConstants.REGISTER_PAGE_URL)
    public String registerUserAccount(@ModelAttribute("userDto") UserRegistrationDto registrationDto, Model model) {

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("registrationError", "Error: Passwords do not match!");
            return ApplicationConstants.VIEW_REGISTER;
        }

        try {
            userService.registerNewUser(registrationDto);
        } catch (RuntimeException e) {
            model.addAttribute("registrationError", e.getMessage());
            return ApplicationConstants.VIEW_REGISTER;
        }

        return "redirect:" + ApplicationConstants.LOGIN_PAGE_URL + "?registered";
    }
}
