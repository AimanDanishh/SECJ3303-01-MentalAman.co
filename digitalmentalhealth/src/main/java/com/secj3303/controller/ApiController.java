package com.secj3303.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secj3303.model.Person;

@RestController
public class ApiController {

    @GetMapping("/api/person")
    public Person getPerson() {
        // returns JSON automatically (via Jackson)
        return new Person("Siti", 1995, 1.60, 55.0);
    }
}