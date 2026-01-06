package com.secj3303.dao;

import java.util.Optional;

import com.secj3303.model.ModuleProgress;

public interface ModuleProgressDao {

    Optional<ModuleProgress> findByUserEmailAndModuleId(
            String userEmail,
            Long moduleId
    );

    void save(ModuleProgress progress);
}
