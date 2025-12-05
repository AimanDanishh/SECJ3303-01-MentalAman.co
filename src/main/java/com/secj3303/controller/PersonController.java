package com.secj3303.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;

@Controller
@RequestMapping("/person")
public class PersonController {
    
    @Autowired
    private PersonDao personDao;
    
    // Show form to add new person
    @GetMapping("/form")
    public String showAddForm(Model model) {
        Person person = new Person();
        model.addAttribute("person", person);
        model.addAttribute("action", "add");
        return "form";
    }
    
    // Process add person
    @PostMapping("/add")
    public String addPerson(
            @RequestParam String name,
            @RequestParam(required = false) Integer yob,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double height,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String[] interests) {
        
        System.out.println("Adding person: " + name);
        
        // Create Person object - this will auto-calculate age and BMI
        Person person = new Person();
        person.setName(name);
        person.setYob(yob);
        person.setWeight(weight);
        person.setHeight(height);
        
        // The BMI and age are calculated automatically in the setters
        System.out.println("Calculated Age: " + person.getAge());
        System.out.println("Calculated BMI: " + person.getBmi());
        System.out.println("Category: " + person.getCategory());
        
        // Save to database
        int result = personDao.insert(person);
        System.out.println("Insert result: " + result + " rows affected");
        
        return "redirect:/person/list";
    }
    
    // Show form to edit person
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Person person = personDao.findById(id);
        
        if (person != null) {
            System.out.println("Editing person: " + person.getName() + " (ID: " + person.getId() + ")");
            model.addAttribute("person", person);
            model.addAttribute("action", "edit");
            return "form";
        }
        
        return "redirect:/person/list";
    }
    
    // Process edit person
    @PostMapping("/edit")
    public String editPerson(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam(required = false) Integer yob,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double height,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String[] interests) {
        
        System.out.println("Updating person ID: " + id);
        
        // Get existing person
        Person person = personDao.findById(id);
        if (person == null) {
            return "redirect:/person/list";
        }
        
        // Update fields - this will auto-calculate age and BMI
        person.setName(name);
        person.setYob(yob);
        person.setWeight(weight);
        person.setHeight(height);
        
        System.out.println("Updated Age: " + person.getAge());
        System.out.println("Updated BMI: " + person.getBmi());
        System.out.println("Category: " + person.getCategory());
        
        // Update in database
        personDao.update(person);
        
        return "redirect:/person/list";
    }
    
    // List all persons
    @GetMapping("/list")
    public String listPersons(Model model) {
        model.addAttribute("persons", personDao.findAll());
        return "list";
    }
    
    // Delete person
    @GetMapping("/delete/{id}")
    public String deletePerson(@PathVariable int id) {
        personDao.delete(id);
        return "redirect:/person/list";
    }
    
    // BMI Calculator form
    @GetMapping("/bmi")
    public String showBmiForm(Model model) {
        Person person = new Person();
        model.addAttribute("person", person);
        model.addAttribute("action", "bmi");
        return "bmi-form"; // Separate form for BMI
    }
}