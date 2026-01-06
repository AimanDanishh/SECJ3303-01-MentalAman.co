package com.secj3303.dao;

import com.secj3303.model.ModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ModuleProgressDao extends JpaRepository<ModuleProgress, Long> {
    
    // Find all module progress for a user
    List<ModuleProgress> findByUserEmail(String userEmail);
    
    // Find specific module progress for a user
    Optional<ModuleProgress> findByUserEmailAndModuleId(String userEmail, String moduleId);
    
    // Count completed modules (100% progress)
    @Query("SELECT COUNT(DISTINCT mp.moduleId) FROM ModuleProgress mp WHERE mp.userEmail = :userEmail AND mp.progress = 100")
    int countCompletedModulesByUser(@Param("userEmail") String userEmail);
    
    // Count passed quizzes
    @Query("SELECT COUNT(*) FROM ModuleProgress mp WHERE mp.userEmail = :userEmail AND mp.quizPassed = true")
    int countPassedQuizzesByUser(@Param("userEmail") String userEmail);
    
    // Get all distinct users who have module progress
    @Query("SELECT DISTINCT mp.userEmail FROM ModuleProgress mp")
    List<String> findDistinctUserEmails();
    
    // Find most recent module progress updates
    @Query("SELECT mp FROM ModuleProgress mp WHERE mp.userEmail = :userEmail ORDER BY mp.updatedAt DESC")
    List<ModuleProgress> findRecentProgressByUser(@Param("userEmail") String userEmail);
    
    // Get total progress sum for a user
    @Query("SELECT COALESCE(SUM(mp.progress), 0) FROM ModuleProgress mp WHERE mp.userEmail = :userEmail")
    int getTotalProgressPoints(@Param("userEmail") String userEmail);
}