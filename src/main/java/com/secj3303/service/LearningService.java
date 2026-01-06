package com.secj3303.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.LearningModule;
import com.secj3303.model.ModuleProgress;
import com.secj3303.repository.LearningModuleRepository;
import com.secj3303.repository.ModuleProgressRepository;

@Service
@Transactional
public class LearningService {

    private final ModuleProgressRepository progressRepo;
    private final LearningModuleRepository moduleRepo;

    public LearningService(ModuleProgressRepository progressRepo, 
                           LearningModuleRepository moduleRepo) {
        this.progressRepo = progressRepo;
        this.moduleRepo = moduleRepo;
    }

    // ========================================================================
    // INCREMENT LESSON PROGRESS (Dynamic Calculation)
    // ========================================================================
    public void incrementProgress(String userEmail, Long moduleId) {

        // 1. Fetch the module to know how many lessons it has
        LearningModule module = moduleRepo.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        int totalLessons = module.getLessons().size();
        if (totalLessons == 0) return;

        // 2. Fetch or Create the progress record for this user
        ModuleProgress progress = progressRepo
                .findByUserEmailAndModuleId(userEmail, moduleId)
                .orElseGet(() -> {
                    ModuleProgress p = new ModuleProgress();
                    p.setUserEmail(userEmail);
                    p.setModuleId(moduleId);
                    p.setProgress(0);
                    p.setQuizPassed(false);
                    return p;
                });

        // 3. Dynamic Calculation: 
        // We calculate what percentage one single lesson represents
        int currentProgress = progress.getProgress();
        int increment = (int) Math.ceil(100.0 / totalLessons);
        
        int newProgress = Math.min(currentProgress + increment, 100);
        
        // Safety check: if it's the last lesson, make sure it hits exactly 100
        // (This prevents rounding issues like ending at 99%)
        progress.setProgress(newProgress);

        progressRepo.save(progress);
    }

    // ========================================================================
    // COMPLETE MODULE (Used when Quiz is Passed)
    // ========================================================================
    public void completeModule(String userEmail, Long moduleId) {

        ModuleProgress progress = progressRepo
                .findByUserEmailAndModuleId(userEmail, moduleId)
                .orElseGet(() -> {
                    ModuleProgress p = new ModuleProgress();
                    p.setUserEmail(userEmail);
                    p.setModuleId(moduleId);
                    return p;
                });

        progress.setProgress(100);
        progress.setQuizPassed(true);

        progressRepo.save(progress);
        
        // Optional: Unlock next module logic
        unlockNextModule(moduleId);
    }
    
    private void unlockNextModule(Long currentModuleId) {
        // You can implement logic here to find moduleId + 1 
        // and set locked = false in the database
    }
}