package com.secj3303.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.ModuleProgress;
import com.secj3303.repository.ModuleProgressRepository;

@Service
@Transactional
public class LearningService {

    private final ModuleProgressRepository progressRepo;

    public LearningService(ModuleProgressRepository progressRepo) {
        this.progressRepo = progressRepo;
    }

    // =========================
    // INCREMENT LESSON PROGRESS
    // =========================
    public void incrementProgress(String userEmail, Long moduleId) {

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

        // simple increment logic (example: +25 per lesson)
        int newProgress = Math.min(progress.getProgress() + 25, 100);
        progress.setProgress(newProgress);

        progressRepo.save(progress);
    }

    // =========================
    // COMPLETE MODULE (QUIZ)
    // =========================
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
    }
}
