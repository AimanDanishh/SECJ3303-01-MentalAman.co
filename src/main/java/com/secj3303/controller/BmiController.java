package com.secj3303.controller;

import java.time.Year;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;
import com.secj3303.model.User;

@Controller
public class BmiController {

    @Autowired
    private PersonDao personDao;

    // =========================
    // Show BMI form
    // =========================
    @GetMapping("/bmi/form")
    public String showBmiForm(Authentication authentication, Model model) {

        if (authentication != null) {
            model.addAttribute("user", buildUser(authentication));
        }

        model.addAttribute("currentView", "bmi");
        return "bmi-form";
    }

    // =========================
    // Process BMI calculation
    // =========================
    @PostMapping("/bmi/result")
    public String calculateBmi(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam("name") String name,
            @RequestParam(value = "yob", required = false) Integer yob,
            @RequestParam("height") Double height,
            @RequestParam("weight") Double weight,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "interest", required = false) String[] interest,
            Authentication authentication,
            Model model) {

        // =========================
        // Combine interests
        // =========================
        String interestsStr = "";
        if (interest != null) {
            interestsStr = String.join(", ", interest);
        }

        // =========================
        // Calculate age
        // =========================
        Integer age = null;
        if (yob != null) {
            age = Year.now().getValue() - yob;
        }

        // =========================
        // Calculate BMI
        // =========================
        Double bmi = null;
        String category = "Unknown";

        if (height != null && height > 0 && weight != null) {
            bmi = Math.round((weight / (height * height)) * 10.0) / 10.0;

            if (bmi < 18.5) category = "Underweight";
            else if (bmi < 25) category = "Normal";
            else if (bmi < 30) category = "Overweight";
            else category = "Obese";
        }

        // =========================
        // Save to DB (Hibernate)
        // =========================
        Person person = new Person();
        person.setName(name);
        person.setYob(yob);
        person.setWeight(weight);
        person.setHeight(height);
        //person.setBmi(bmi);
        //person.setCategory(category);

        personDao.insert(person);

        // =========================
        // Model attributes
        // =========================
        if (authentication != null) {
            model.addAttribute("user", buildUser(authentication));
        }

        model.addAttribute("currentView", "bmi");

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
        model.addAttribute("generatedId", person.getId());

        return "bmi-result";
    }

    // =========================
    // Helper
    // =========================
    private User buildUser(Authentication authentication) {
        User user = new User();
        user.setEmail(authentication.getName());
        user.setName(authentication.getName().split("@")[0]);
        user.setRole(
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "")
                        .toLowerCase()
        );
        return user;
    }
}
