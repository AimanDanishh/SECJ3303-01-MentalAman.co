package com.secj3303.dao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.Gamification;

@Repository
@Transactional
public class GamificationDaoHibernate implements GamificationDao {

    @PersistenceContext
    private EntityManager em;

    private static final int POINTS_PER_MODULE = 100;
    private static final int POINTS_PER_QUIZ = 50;

    @Override
    public Optional<Gamification> findByUserEmail(String userEmail) {
        try {
            return Optional.of(em.createQuery(
                "SELECT g FROM Gamification g WHERE g.userEmail = :email", Gamification.class)
                .setParameter("email", userEmail)
                .getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void syncUserProgress(String userEmail) {
        // 1. READ-ONLY: Query module_progress to count achievements
        String sql = """
            SELECT 
                COUNT(CASE WHEN progress = 100 THEN 1 END) as completed_modules,
                COUNT(CASE WHEN quizPassed = true THEN 1 END) as passed_quizzes
            FROM module_progress 
            WHERE user_email = ?
        """;
        
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, userEmail);
        
        Object[] results = (Object[]) query.getSingleResult();
        int completedModules = ((Number) results[0]).intValue();
        int passedQuizzes = ((Number) results[1]).intValue();

        // 2. CALCULATE New State
        int calculatedXp = (completedModules * POINTS_PER_MODULE) + (passedQuizzes * POINTS_PER_QUIZ);
        int newLevel = (calculatedXp / 200) + 1;

        // 3. WRITE Update to Gamifications Table
        Gamification gamification = findByUserEmail(userEmail)
                .orElse(new Gamification(userEmail));

        gamification.setXpPoints(calculatedXp);
        gamification.setCurrentLevel(newLevel);
        
        updateStreak(gamification);
        
        if (gamification.getId() == null) {
            em.persist(gamification);
        } else {
            em.merge(gamification);
        }
    }

    @Override
    public List<Gamification> getLeaderboard(int limit) {
        return em.createQuery(
            "SELECT g FROM Gamification g ORDER BY g.xpPoints DESC", Gamification.class)
            .setMaxResults(limit)
            .getResultList();
    }

    private void updateStreak(Gamification g) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = g.getLastActivity();
        
        if (last == null) {
            g.setDailyStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(last.toLocalDate(), now.toLocalDate());
            if (daysBetween == 1) {
                g.setDailyStreak(g.getDailyStreak() + 1);
            } else if (daysBetween > 1) {
                g.setDailyStreak(1); // Reset if missed a day
            }
            else if (daysBetween == 0 && g.getDailyStreak() == 0) {
                g.setDailyStreak(1); // Fixes the "stuck at 0" issue if initialized today
            }
        }
        g.setLastActivity(now);
    }
}