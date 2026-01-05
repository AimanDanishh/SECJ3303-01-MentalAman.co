package com.secj3303.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.secj3303.model.User;
import com.secj3303.repository.UserRepository;

@Component
public class DemoDataInitializer {

    private final UserRepository userRepository;

    public DemoDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {

        createUserIfNotExists(
            "student@demo.com", "Demo Student", "STUDENT");

        createUserIfNotExists(
            "faculty@demo.com", "Demo Faculty", "FACULTY");

        createUserIfNotExists(
            "counsellor@demo.com", "Demo Counsellor", "COUNSELLOR");

        createUserIfNotExists(
            "admin@demo.com", "System Admin", "ADMINISTRATOR");
    }

    private void createUserIfNotExists(String email, String name, String role) {

        if (userRepository.existsById(email)) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setPassword("{noop}demo123"); // IMPORTANT
        user.setEnabled(true);

        // default preferences
        user.setEmailNotifications(true);
        user.setPushNotifications(true);
        user.setWeeklyReport(true);
        user.setAnonymousMode(false);

        userRepository.save(user);
    }
}
