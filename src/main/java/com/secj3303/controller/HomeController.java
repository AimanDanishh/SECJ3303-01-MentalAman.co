package com.secj3303.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index";  // This shows your Campus Cafeteria page
    }
    
    @GetMapping("/health")
    public String healthHome() {
        return "redirect:/person/list";  // Digital Mental Health entry point
    }
}