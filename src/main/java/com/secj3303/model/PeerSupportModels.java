package com.secj3303.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PeerSupportModels implements Serializable {

    // --- Data Structures ---

    public static class Reply implements Serializable {
        private int id;
        private String author;
        private String authorInitials;
        private String time;
        private String content;
        private int likes;

        public Reply(int id, String author, String authorInitials, String time, String content, int likes) {
            this.id = id;
            this.author = author;
            this.authorInitials = authorInitials;
            this.time = time;
            this.content = content;
            this.likes = likes;
        }

        public Reply() {}

        // --- Getters ---
        public int getId() { return id; }
        public String getAuthor() { return author; }
        public String getAuthorInitials() { return authorInitials; }
        public String getTime() { return time; }
        public String getContent() { return content; }
        public int getLikes() { return likes; }
        
        // --- SETTER ADDED for Controller Access ---
        public void setId(int id) { this.id = id; }
        public void setAuthor(String author) { this.author = author; }
        public void setAuthorInitials(String authorInitials) { this.authorInitials = authorInitials; }
        public void setTime(String time) { this.time = time; }
        public void setContent(String content) { this.content = content; }
        public void setLikes(int likes) { this.likes = likes; }
    }

    public static class Post implements Serializable {
        private int id;
        private String author;
        private String authorInitials;
        private String time;
        private String category;
        private String title;
        private String content;
        private int likes;
        private List<Reply> replies;
        private boolean trending;
        private boolean helpful;

        public Post(int id, String author, String authorInitials, String time, String category, String title, String content, int likes, List<Reply> replies, boolean trending, boolean helpful) {
            this.id = id;
            this.author = author;
            this.authorInitials = authorInitials;
            this.time = time;
            this.category = category;
            this.title = title;
            this.content = content;
            this.likes = likes;
            this.replies = replies;
            this.trending = trending;
            this.helpful = helpful;
        }

        public Post() {}

        // --- Getters ---
        public int getId() { return id; }
        public String getAuthor() { return author; }
        public String getAuthorInitials() { return authorInitials; }
        public String getTime() { return time; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public int getLikes() { return likes; }
        public List<Reply> getReplies() { return replies; }
        public boolean isTrending() { return trending; }
        public boolean isHelpful() { return helpful; }

        // --- SETTERS ADDED for Controller Access (Fixing Encapsulation Errors) ---
        public void setId(int id) { this.id = id; }
        public void setAuthor(String author) { this.author = author; }
        public void setAuthorInitials(String authorInitials) { this.authorInitials = authorInitials; }
        public void setTime(String time) { this.time = time; }
        public void setLikes(int likes) { this.likes = likes; }
        public void setReplies(List<Reply> replies) { this.replies = replies; }
        public void setTrending(boolean trending) { this.trending = trending; }
        public void setHelpful(boolean helpful) { this.helpful = helpful; }
        
        // Form properties (setter needed for new post submission)
        public void setCategory(String category) { this.category = category; }
        public void setTitle(String title) { this.title = title; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class ReportForm implements Serializable {
        public int postId;
        public String reason;
        public String details;
        
        public ReportForm() {}
        public void setPostId(int postId) { this.postId = postId; }
        public void setReason(String reason) { this.reason = reason; }
        public void setDetails(String details) { this.details = details; }
        
        public int getPostId() { return postId; }
        public String getReason() { return reason; }
        public String getDetails() { return details; }
    }


    // --- Static Data and Helpers ---

    public static List<Post> getInitialPosts() {
        return new ArrayList<>(Arrays.asList(
            new Post(
                1, "Anonymous Student", "AS", "2 hours ago", "anxiety", "Struggling with exam anxiety", "Finals are coming up and I'm feeling overwhelmed. Has anyone found helpful strategies for managing exam stress?", 24,
                Arrays.asList(
                    new Reply(1, "Anonymous Faculty", "AF", "1 hour ago", "I've found that creating a study schedule helps reduce anxiety. Break your studying into manageable chunks!", 8),
                    new Reply(2, "Anonymous Student", "AS", "30 minutes ago", "Meditation and deep breathing exercises have really helped me. There are some great apps for this!", 5)
                ), true, true
            ),
            new Post(
                2, "Anonymous User", "AU", "3 hours ago", "stress", "EARN $5000 FROM HOME - CLICK HERE NOW!!!", "Hey everyone! I found this AMAZING opportunity to make money from home. Just click this link and sign up! You can make thousands of dollars working just 2 hours a day! Message me for details. This is NOT a scam, I promise! Limited spots available!!!", 0,
                new ArrayList<>(), false, false
            ),
            new Post(
                3, "Anonymous Faculty", "AF", "5 hours ago", "motivation", "Daily gratitude practice changed my life", "I wanted to share how starting a simple gratitude journal has helped me maintain a more positive outlook. It only takes 5 minutes each morning!", 45,
                Arrays.asList(
                    new Reply(1, "Anonymous Student", "AS", "4 hours ago", "This is inspiring! How do you stay consistent with it?", 3)
                ), true, false
            ),
            new Post(4, "Anonymous Student", "AS", "1 day ago", "stress", "Need advice on work-life balance", "Finding it hard to balance studies, part-time work, and personal time. How do you all manage everything?", 18, new ArrayList<>(), false, false),
            new Post(5, "Anonymous Student", "AS", "1 day ago", "depression", "Feeling isolated - seeking support", "Been feeling disconnected from friends and struggling to reach out. Anyone else experienced this?", 31, new ArrayList<>(), false, true)
        ));
    }

    public static final List<Map<String, String>> CATEGORIES = Arrays.asList(
        Map.of("id", "all", "label", "All Posts", "count", "156"),
        Map.of("id", "anxiety", "label", "Anxiety", "count", "45"),
        Map.of("id", "stress", "label", "Stress", "count", "38"),
        Map.of("id", "depression", "label", "Depression", "count", "32"),
        Map.of("id", "motivation", "label", "Motivation", "count", "41")
    );
    
    public static final List<Map<String, String>> REPORT_REASONS = Arrays.asList(
        Map.of("value", "harassment", "label", "Harassment or Bullying"),
        Map.of("value", "self-harm", "label", "Self-harm Content"),
        Map.of("value", "spam", "label", "Spam or Misleading"),
        Map.of("value", "hate-speech", "label", "Hate Speech"),
        Map.of("value", "inappropriate", "label", "Inappropriate Content"),
        Map.of("value", "other", "label", "Other")
    );

    public static class ContentCheckResult implements Serializable {
        public boolean isClean;
        public String warning;

        public ContentCheckResult(boolean isClean, String warning) {
            this.isClean = isClean;
            this.warning = warning;
        }
    }

    public static ContentCheckResult checkContentForHarmfulText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ContentCheckResult(true, "");
        }
        
        final String lowerText = text.toLowerCase();
        
        final Map<String, List<String>> harmfulKeywords = Map.of(
            "selfHarm", Arrays.asList("kill myself", "end my life", "suicide", "self harm", "cut myself", "hurt myself"),
            "violence", Arrays.asList("kill you", "hurt you", "attack", "violence", "weapon"),
            "spam", Arrays.asList("click here", "buy now", "limited time", "earn money", "make $", "buy this", "!!!"),
            "harassment", Arrays.asList("you suck", "idiot", "stupid", "loser", "hate you"),
            "inappropriate", Arrays.asList("drugs", "alcohol abuse", "explicit")
        );

        // Check for self-harm content
        if (harmfulKeywords.get("selfHarm").stream().anyMatch(lowerText::contains)) {
            return new ContentCheckResult(false, "Your message contains content related to self-harm. If you're in crisis, please contact a mental health professional immediately. Crisis Hotline: 1-800-273-8255");
        }

        // Check for violence
        if (harmfulKeywords.get("violence").stream().anyMatch(lowerText::contains)) {
            return new ContentCheckResult(false, "Your message contains violent content. This forum is for supportive discussions only.");
        }

        // Check for spam
        long spamCount = harmfulKeywords.get("spam").stream().filter(lowerText::contains).count();
        if (spamCount >= 3 || (lowerText.contains("!!!") && spamCount >= 2)) {
            return new ContentCheckResult(false, "Your message appears to be spam or promotional content. Please share genuine experiences and support.");
        }

        // Check for harassment
        if (harmfulKeywords.get("harassment").stream().anyMatch(lowerText::contains)) {
            return new ContentCheckResult(false, "Your message contains language that may be hurtful. Please be respectful and supportive to all members.");
        }

        return new ContentCheckResult(true, "");
    }
}