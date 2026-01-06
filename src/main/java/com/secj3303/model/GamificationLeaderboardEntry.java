package com.secj3303.model;

public class GamificationLeaderboardEntry {
    
    private String userEmail;
    private String userName;
    private int rank;
    private int points;
    private int level;
    private int streak;
    private String badgeEmoji;
    private boolean isDummyUser;
    
    // Constructor
    public GamificationLeaderboardEntry(String userEmail, String userName, int rank, 
                                       int points, int level, int streak, 
                                       String badgeEmoji, boolean isDummyUser) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.rank = rank;
        this.points = points;
        this.level = level;
        this.streak = streak;
        this.badgeEmoji = badgeEmoji;
        this.isDummyUser = isDummyUser;
    }
    
    // Getters
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public int getRank() { return rank; }
    public int getPoints() { return points; }
    public int getLevel() { return level; }
    public int getStreak() { return streak; }
    public String getBadgeEmoji() { return badgeEmoji; }
    public boolean isDummyUser() { return isDummyUser; }
    public boolean isCurrentUser(String currentUserEmail) {
        return userEmail.equals(currentUserEmail);
    }
}