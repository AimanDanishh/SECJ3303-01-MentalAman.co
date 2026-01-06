package com.secj3303.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secj3303.model.ModuleProgress;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {
    
    Optional<ModuleProgress> findByUserEmailAndModuleId(String userEmail, Long moduleId);
    
    @Query("SELECT mp FROM ModuleProgress mp WHERE mp.userEmail = :userEmail")
    List<ModuleProgress> findAllByUserEmail(@Param("userEmail") String userEmail);
}