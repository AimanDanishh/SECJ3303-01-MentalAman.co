package com.secj3303.dao;

import com.secj3303.model.MoodEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoodEntryDao {
    // Save a new mood entry
    MoodEntry save(MoodEntry moodEntry);
    
    // Update an existing mood entry
    MoodEntry update(MoodEntry moodEntry);
    
    // Delete a mood entry by ID
    boolean delete(Integer id);
    
    // Find mood entry by ID
    Optional<MoodEntry> findById(Integer id);
    
    // Find all mood entries for a specific user
    List<MoodEntry> findByUsername(String username);
    
    // Find mood entries for a user within a date range
    List<MoodEntry> findByUsernameAndDateRange(String username, LocalDate startDate, LocalDate endDate);
    
    // Find mood entry by username and specific date
    Optional<MoodEntry> findByUsernameAndDate(String username, LocalDate date);
    
    // Find mood entries for a user from the last N days
    List<MoodEntry> findRecentByUsername(String username, int days);
    
    // Count total entries for a user
    long countByUsername(String username);
    
    // Check if a user already has an entry for a specific date
    boolean existsByUsernameAndDate(String username, LocalDate date);
}