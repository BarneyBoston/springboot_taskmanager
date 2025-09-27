package com.springboot.taskmanager.controller;

import com.springboot.taskmanager.constants.ApplicationConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ApplicationConstants.LANDING_PAGE_URL)
public class LandingPageController {

    @GetMapping
    public String showLandingPage() {
        return "landing-page";
    }
}
