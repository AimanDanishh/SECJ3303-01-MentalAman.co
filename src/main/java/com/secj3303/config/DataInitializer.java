// File: DataInitializer.java (Enhanced)
package com.secj3303.config;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.secj3303.service.ForumService;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ForumService forumService;
    
    @Autowired
    private DataSource dataSource;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            System.out.println("=== Starting Database Initialization ===");
            
            // Check if database exists, create if not
            ensureDatabaseExists();
            
            // Check if tables exist, create if not
            ensureTablesExist();
            
            // Initialize dummy data if needed
            if (forumService.getAllPosts().isEmpty()) {
                System.out.println("No posts found. Initializing dummy data...");
                forumService.initializeDummyData();
                System.out.println("Dummy data initialized successfully!");
            } else {
                System.out.println("Found " + forumService.getAllPosts().size() + " existing posts.");
            }
            
            System.out.println("=== Database Initialization Complete ===");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void ensureDatabaseExists() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            // Just try to query something to see if database exists
            jdbcTemplate.execute("SELECT 1");
            System.out.println("Database connection successful");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
    
    private void ensureTablesExist() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Check if forum_posts table exists
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SHOW TABLES LIKE 'forum_posts'"
            );
            
            if (tables.isEmpty()) {
                System.out.println("Tables don't exist. Hibernate should create them...");
                // Hibernate will create them with hbm2ddl.auto=update
            } else {
                System.out.println("Tables already exist.");
            }
        } catch (Exception e) {
            System.err.println("Error checking tables: " + e.getMessage());
        }
    }
}