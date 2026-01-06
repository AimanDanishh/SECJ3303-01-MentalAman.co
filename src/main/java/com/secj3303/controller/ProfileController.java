package com.secj3303.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final PersonDao personDao;

    public ProfileController(PersonDao personDao) {
        this.personDao = personDao;
    }

    // =========================
    // Allow ONLY safe fields to bind
    // =========================
    @InitBinder("user")
    protected void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(
            "name",
            "yob",
            "weight",
            "height"
        );
    }

    // =========================
    // View Profile
    // =========================
    @GetMapping
    public String viewProfile(
            @RequestParam(name = "edit", required = false) Boolean edit,
            Authentication authentication,
            Model model) {

        String email = authentication.getName();
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found");
        }

        model.addAttribute("user", person); // keep attribute name
        model.addAttribute("currentView", "profile");
        model.addAttribute("isEditing", edit != null && edit);

        return "app-layout";
    }

    // =========================
    // Update Profile
    // =========================
    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute("user") Person formPerson,
            Authentication authentication) {

        String email = authentication.getName();
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found");
        }

        person.setName(formPerson.getName());
        person.setYob(formPerson.getYob());
        person.setWeight(formPerson.getWeight());
        person.setHeight(formPerson.getHeight());

        personDao.update(person);

        return "redirect:/profile";
    }
}
