package com.secj3303.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.GamificationDao;
import com.secj3303.model.Gamification;
import com.secj3303.model.Gamification.Badge;
import com.secj3303.model.Gamification.LeaderboardEntry;

@Service
@Transactional
public class GamificationService {

    private final GamificationDao gamificationDao;

    public GamificationService(GamificationDao gamificationDao) {
        this.gamificationDao = gamificationDao;
    }

    public Gamification getUserGamificationProfile(String userEmail) {
        // Sync first to ensure data is fresh
        gamificationDao.syncUserProgress(userEmail); 
        return gamificationDao.findByUserEmail(userEmail)
                .orElse(new Gamification(userEmail));
    }

    public int getPointsToNextLevel(int currentXp) {
        int level = (currentXp / 200) + 1;
        int nextLevelXp = level * 200;
        return nextLevelXp - currentXp;
    }

    /**
     * UNLOCK LOGIC: Takes the static list and flips 'earned' to true based on real stats.
     */
    public List<Badge> getUnlockedBadges(Gamification profile) {
        List<Badge> badges = Gamification.getBadges(); // Fetches list where all are false
        
        // 1. First Steps (XP > 100)
        if (profile.getXpPoints() >= 100) {
            badges.get(0).earned = true; 
        }
        
        // 2. Consistent Learner (Streak >= 7)
        if (profile.getDailyStreak() >= 7) {
            badges.get(1).earned = true;
        }

        // 3. Knowledge Seeker (Approx 5 modules worth of points)
        if (profile.getXpPoints() >= 500) {
            badges.get(4).earned = true;
        }
        
        return badges;
    }

    /**
     * LEADERBOARD LOGIC: Merges real users with dummy data
     */
    public List<LeaderboardEntry> getFunctionalLeaderboard(String currentUserEmail) {
        List<Gamification> realUsers = gamificationDao.getLeaderboard(10);
        List<LeaderboardEntry> staticEntries = Gamification.getStaticLeaderboard();
        List<LeaderboardEntry> finalLeaderboard = new ArrayList<>();
        
        int rankCounter = 1;

        // Add Real Users
        for (Gamification g : realUsers) {
            String displayName = g.getUserEmail().split("@")[0];
            boolean isMe = g.getUserEmail().equals(currentUserEmail);
            String badgeEmoji = getBadgeForRank(rankCounter);
            
            finalLeaderboard.add(new LeaderboardEntry(
                rankCounter++, displayName, g.getXpPoints(), 
                g.getCurrentLevel(), badgeEmoji, isMe
            ));
        }

        // Add Dummies if we have fewer than 5 users
        if (finalLeaderboard.size() < 5) {
            for (LeaderboardEntry dummy : staticEntries) {
                // Prevent duplicate "You" entries if user is already ranked
                if (!dummy.name.equals("You") && finalLeaderboard.size() < 5) {
                    dummy.rank = rankCounter++;
                    finalLeaderboard.add(dummy);
                }
            }
        }
        return finalLeaderboard;
    }
    
    private String getBadgeForRank(int rank) {
        if (rank == 1) return "👑";
        if (rank == 2) return "🥈";
        if (rank == 3) return "🥉";
        return "";
    }
}