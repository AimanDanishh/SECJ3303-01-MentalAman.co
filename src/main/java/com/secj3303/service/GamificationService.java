package com.secj3303.service;

import java.util.ArrayList;
import java.util.Comparator;
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

    public List<Badge> getUnlockedBadges(Gamification profile) {
        List<Badge> badges = Gamification.getBadges(); 
        
        if (profile.getXpPoints() >= 100) badges.get(0).earned = true; 
        if (profile.getDailyStreak() >= 7) badges.get(1).earned = true;
        if (profile.getXpPoints() >= 500) badges.get(4).earned = true;
        
        return badges;
    }

    public List<LeaderboardEntry> getFunctionalLeaderboard(String currentUserEmail) {
        List<LeaderboardEntry> allEntries = new ArrayList<>();

        // 1. Convert Real Users to Entries
        List<Gamification> realUsers = gamificationDao.getLeaderboard(20); // Get more to sort correctly
        for (Gamification g : realUsers) {
            String displayName = g.getUserEmail().split("@")[0];
            boolean isMe = g.getUserEmail().equals(currentUserEmail);
            
            // We temporarily set rank to 0, we will fix it after sorting
            allEntries.add(new LeaderboardEntry(
                0, displayName, g.getXpPoints(), 
                g.getCurrentLevel(), "", isMe
            ));
        }

        // 2. Add Dummy Users
        List<LeaderboardEntry> staticEntries = Gamification.getStaticLeaderboard();
        for (LeaderboardEntry dummy : staticEntries) {
            if (!dummy.name.equals("You")) {
                allEntries.add(dummy);
            }
        }

        // 3. Sort Everything by Points (Descending)
        allEntries.sort(Comparator.comparingInt((LeaderboardEntry e) -> e.points).reversed());

        // 4. Assign Ranks & Limit to Top 10
        List<LeaderboardEntry> finalLeaderboard = new ArrayList<>();
        int rank = 1;
        
        for (LeaderboardEntry entry : allEntries) {
            if (rank > 10) break; // Only show top 10
            
            entry.rank = rank; // Assign correct rank based on points
            entry.badge = getBadgeForRank(rank); // Assign crown/medal
            finalLeaderboard.add(entry);
            rank++;
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