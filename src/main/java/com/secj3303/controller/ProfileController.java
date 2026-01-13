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
import com.secj3303.dao.StudentDao;
import com.secj3303.dao.CounsellorDao;
import com.secj3303.model.Counsellor;
import com.secj3303.model.Person;
import com.secj3303.model.Student;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final PersonDao personDao;
    private final StudentDao studentDao;
    private final CounsellorDao counsellorDao;

    public ProfileController(PersonDao personDao, StudentDao studentDao, CounsellorDao counsellorDao) {
        this.personDao = personDao;
        this.studentDao = studentDao;
        this.counsellorDao = counsellorDao;
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

        // Update Student if exists
        studentDao.findByEmail(email).ifPresent(student -> {
            // Update the student with the same personal information
            student.setName(formPerson.getName());
            
            studentDao.update(student);
        });

        // Check if user is a Counsellor and update
        Counsellor counsellor = counsellorDao.findByEmail(email);
        if (counsellor != null) {
            // Note: Counsellor may not have all the same fields as Person
            // Adjust based on your actual Counsellor model
            counsellor.setName(formPerson.getName());

            counsellorDao.update(counsellor);
        }

        return "redirect:/profile";
    }
}
