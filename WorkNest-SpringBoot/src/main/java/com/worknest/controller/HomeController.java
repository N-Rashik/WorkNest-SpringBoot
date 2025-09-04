package com.worknest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    // Handles "/" and "/index"
    @GetMapping({"", "index"})
    public String index() {
        return "index"; // returns the Thymeleaf template "index.html"
    }
}
