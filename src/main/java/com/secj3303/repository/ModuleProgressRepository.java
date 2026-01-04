package com.secj3303.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secj3303.model.ModuleProgress;

public interface ModuleProgressRepository
        extends JpaRepository<ModuleProgress, Long> {

    Optional<ModuleProgress> findByUserEmailAndModuleId(
            String userEmail,
            Long moduleId
    );
}
