package com.secj3303.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;

@Component
public class DemoDataInitializer {

    private final PersonDao personDao;

    public DemoDataInitializer(PersonDao personDao) {
        this.personDao = personDao;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {

        createPersonIfNotExists(
            "student@demo.com", "Demo Student", "STUDENT");

        createPersonIfNotExists(
            "faculty@demo.com", "Demo Faculty", "FACULTY");

        createPersonIfNotExists(
            "counsellor@demo.com", "Demo Counsellor", "COUNSELLOR");

        createPersonIfNotExists(
            "admin@demo.com", "System Admin", "ADMINISTRATOR");
    }

    private void createPersonIfNotExists(String email, String name, String role) {

        // Check by email
        Person existing = personDao.findByEmail(email);
        if (existing != null) {
            return;
        }

        Person person = new Person();
        person.setEmail(email);
        person.setName(name);
        person.setRole(role);
        person.setPassword("{noop}demo123"); // IMPORTANT for Spring Security
        person.setEnabled(true);

        // Optional profile defaults
        person.setYob(2000);
        person.setWeight(65.0);
        person.setHeight(1.70);

        personDao.insert(person);
    }
}