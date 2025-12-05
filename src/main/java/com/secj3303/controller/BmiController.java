package com.secj3303.controller;

import java.time.Year;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;

@Controller
public class BmiController {
    
    @Autowired
    private PersonDao personDao;
    
    // Show BMI form
    @GetMapping("/bmi/form")
    public String showBmiForm() {
        return "bmi-form";
    }
    
    // Process BMI calculation - FIXED with proper parameter handling
    @PostMapping("/bmi/result")
    public String calculateBmi(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam("name") String name,
            @RequestParam(value = "yob", required = false) Integer yob,
            @RequestParam("height") Double height,
            @RequestParam("weight") Double weight,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "interest", required = false) String[] interest,
            Model model) {
        
        System.out.println("DEBUG: Received form submission");
        System.out.println("  Name: " + name);
        System.out.println("  Height: " + height);
        System.out.println("  Weight: " + weight);
        System.out.println("  YOB: " + yob);
        System.out.println("  Gender: " + gender);
        
        // Combine interests array into comma-separated string
        String interestsStr = "";
        if (interest != null) {
            interestsStr = String.join(", ", interest);
            System.out.println("  Interests: " + interestsStr);
        }
        
        // Calculate age
        Integer age = null;
        if (yob != null) {
            age = Year.now().getValue() - yob;
        }
        
        // Calculate BMI
        Double bmi = null;
        String category = "Unknown";
        if (height != null && height > 0 && weight != null) {
            bmi = weight / (height * height);
            // Round to 1 decimal place
            bmi = Math.round(bmi * 10.0) / 10.0;
            
            // Determine category
            if (bmi < 18.5) {
                category = "Underweight";
            } else if (bmi < 25) {
                category = "Normal";
            } else if (bmi < 30) {
                category = "Overweight";
            } else {
                category = "Obese";
            }
            
            System.out.println("  Calculated BMI: " + bmi);
            System.out.println("  Category: " + category);
        }
        
        // Create Person object for database
        Person personForDb = new Person();
        personForDb.setName(name);
        personForDb.setYob(yob);
        personForDb.setWeight(weight);
        personForDb.setHeight(height);
        personForDb.setBmi(bmi);
        personForDb.setCategory(category);
        
        // Calculate age in Person object
        if (yob != null) {
            personForDb.setYob(yob); // This triggers age calculation
        }
        
        // Save to database
        try {
            personDao.insert(personForDb);
            System.out.println("  Saved to database with ID: " + personForDb.getId());
        } catch (Exception e) {
            System.out.println("  ERROR saving to database: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Add all data to model for display
        model.addAttribute("id", id);
        model.addAttribute("name", name);
        model.addAttribute("yob", yob);
        model.addAttribute("age", age);
        model.addAttribute("height", height);
        model.addAttribute("weight", weight);
        model.addAttribute("bmi", bmi);
        model.addAttribute("category", category);
        model.addAttribute("gender", gender);
        model.addAttribute("interests", interestsStr);
        model.addAttribute("generatedId", personForDb.getId());
        
        return "bmi-result";
    }
}