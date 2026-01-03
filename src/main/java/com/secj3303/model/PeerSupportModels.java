package com.secj3303.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PeerSupportModels implements Serializable {

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