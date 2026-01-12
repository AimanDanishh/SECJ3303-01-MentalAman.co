package com.secj3303.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.GamificationDao;
import com.secj3303.dao.LearningModuleDao;
import com.secj3303.dao.ModuleProgressDao;
import com.secj3303.model.LearningModule;
import com.secj3303.model.ModuleProgress;

@Service
@Transactional
public class LearningService {

    private final ModuleProgressDao progressDao;
    private final LearningModuleDao moduleDao;
    private final GamificationDao gamificationDao;

    public LearningService(ModuleProgressDao progressDao,
                           LearningModuleDao moduleDao,
                           GamificationDao gamificationDao) {
        this.progressDao = progressDao;
        this.moduleDao = moduleDao;
        this.gamificationDao = gamificationDao;
    }

    // ======================================================
    // MARK LESSON AS COMPLETE (MODULE-LEVEL ONLY)
    // ======================================================
    public void completeLesson(String userEmail, Long moduleId) {

        LearningModule module = moduleDao.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        int totalLessons = module.getLessons().size();
        if (totalLessons == 0) return;

        ModuleProgress progress = progressDao
                .findByUserEmailAndModuleId(userEmail, moduleId)
                .orElseGet(() -> {
                    ModuleProgress p = new ModuleProgress();
                    p.setUserEmail(userEmail);
                    p.setModule(module);      // ✅ FIXED
                    p.setProgress(0);
                    p.setQuizPassed(false);
                    return p;
                });

        // Prevent exceeding 100%
        if (progress.getProgress() >= 100) return;

        int increment = (int) Math.ceil(100.0 / totalLessons);
        int newProgress = Math.min(progress.getProgress() + increment, 100);

        progress.setProgress(newProgress);
        progressDao.save(progress);
        gamificationDao.syncUserProgress(userEmail);
    }

    // ======================================================
    // COMPLETE MODULE (QUIZ PASSED)
    // ======================================================
    public void completeModule(String userEmail, Long moduleId) {

        LearningModule module = moduleDao.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        ModuleProgress progress = progressDao
                .findByUserEmailAndModuleId(userEmail, moduleId)
                .orElseGet(() -> {
                    ModuleProgress p = new ModuleProgress();
                    p.setUserEmail(userEmail);
                    p.setModule(module);      // ✅ FIXED
                    return p;
                });

        progress.setProgress(100);
        progress.setQuizPassed(true);
        progressDao.save(progress);
        gamificationDao.syncUserProgress(userEmail);
    }
}
