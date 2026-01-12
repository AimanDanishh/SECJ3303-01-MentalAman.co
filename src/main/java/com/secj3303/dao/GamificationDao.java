package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

import com.secj3303.model.Gamification;

public interface GamificationDao {
    void syncUserProgress(String userEmail);
    Optional<Gamification> findByUserEmail(String userEmail);
    List<Gamification> getLeaderboard(int limit);
}