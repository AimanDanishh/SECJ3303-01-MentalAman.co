package com.secj3303.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.Message;

@Controller
@RequestMapping("/coach")
public class AICoachController {

    private static final String HISTORY_KEY = "coachChatHistory";
    private static final String DEFAULT_VIEW = "coach";

    private List<Message> getChatHistory(HttpSession session) {
        List<Message> history = (List<Message>) session.getAttribute(HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
            // NF1: Greet user when chat opens
            Message greeting = new Message("Hello! I'm your AI Mental Health Coach. I'm here to provide support and guidance for your wellbeing journey. How are you feeling today?", "ai");
            history.add(greeting);
            session.setAttribute(HISTORY_KEY, history);
        }
        return history;
    }

    // --- Main Chat View (Replaces initial render and useEffect) ---
    
    @GetMapping
    public String coachView(HttpSession session, Model model) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("messages", getChatHistory(session));
        
        // Pass the user role if needed for context, though not directly used in this view logic
        // model.addAttribute("userRole", session.getAttribute("currentUser").getRole());

        // Ensure the main app-layout renders this fragment
        return "app-layout";
    }

    // --- Message Sending (Replaces handleSendMessage) ---
    
    @PostMapping("/send")
    public String sendMessage(@RequestParam String inputMessage, 
                              HttpSession session, 
                              RedirectAttributes redirect) {
        
        String cleanMessage = inputMessage.trim();
        if (cleanMessage.isEmpty()) {
            return "redirect:/coach";
        }

        // AF2: Simulate connection error (5% chance)
        if (ThreadLocalRandom.current().nextDouble() < 0.05) {
            redirect.addFlashAttribute("connectionError", true);
            return "redirect:/coach";
        }

        List<Message> history = getChatHistory(session);
        
        // NF3: User sends message
        history.add(new Message(cleanMessage, "user"));
        session.setAttribute(HISTORY_KEY, history); // Save history before redirect

        // Simulate AI thinking delay (1000ms + random 1500ms)
        try {
            TimeUnit.MILLISECONDS.sleep(1000 + ThreadLocalRandom.current().nextInt(1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // NF4: AI responds with dummy logic
        String aiResponse = generateAIResponse(cleanMessage);
        
        // Add AI response
        history.add(new Message(aiResponse, "ai"));
        session.setAttribute(HISTORY_KEY, history);

        // Success: Redirect back to the GET view to display the new messages
        return "redirect:/coach";
    }

    // --- Clear Chat History (Replaces handleClearChat) ---

    @PostMapping("/clear")
    public String clearChat(HttpSession session) {
        session.removeAttribute(HISTORY_KEY);
        // Redirect will trigger the greeting logic in getChatHistory()
        return "redirect:/coach";
    }

    // --- AI Response Logic (Replicates TSX generateAIResponse) ---

    private String generateAIResponse(String userInput) {
        String input = userInput.toLowerCase().trim();

        // NF4: AI Coach responds using dummy logic

        // Crisis keywords - immediate referral
        if (input.contains("suicid") || input.contains("harm myself") || input.contains("end it") || input.contains("kill myself")) {
            return "I'm concerned about what you've shared. Please reach out to a counselor immediately or contact a crisis helpline:\n\n🆘 National Crisis Hotline: 988\n📞 Crisis Text Line: Text HOME to 741741\n\nYou don't have to face this alone. A counselor can provide the specialized support you need right now. Please reach out to them - your life matters.";
        }

        // Greetings
        if (Pattern.compile("^(hi|hello|hey|good morning|good afternoon|good evening)").matcher(input).find()) {
            return "Hello! It's great to hear from you. How can I support you today?";
        }

        // Stress-related keywords
        if (input.contains("stress") || input.contains("stressed")) {
            return "I understand you're feeling stressed. Stress is a common experience, especially during challenging times. Have you tried any relaxation techniques like deep breathing or taking short breaks? I can guide you through a simple breathing exercise if you'd like.";
        }

        // Anxiety-related keywords
        if (input.contains("anxious") || input.contains("anxiety") || input.contains("worried") || input.contains("nervous")) {
            return "Anxiety can be overwhelming, but you're taking a positive step by talking about it. Remember that it's okay to feel this way. Some helpful strategies include: grounding exercises (5-4-3-2-1 technique), gentle physical activity, and talking to someone you trust. Would you like to explore any of these approaches?";
        }

        // Depression or sadness
        if (input.contains("depressed") || input.contains("sad") || input.contains("down") || input.contains("hopeless")) {
            return "I'm sorry you're feeling this way. Your feelings are valid, and it's important that you're reaching out. Please consider speaking with a counselor who can provide professional support. In the meantime, try to maintain small daily routines, connect with supportive people, and be gentle with yourself. Would you like me to help you book a counseling session?";
        }

        // Sleep issues
        if (input.contains("sleep") || input.contains("insomnia") || input.contains("tired") || input.contains("exhausted")) {
            return "Sleep is crucial for mental and physical wellbeing. Consider establishing a consistent bedtime routine, limiting screen time before bed, and creating a comfortable sleep environment. Our platform has a 'Better Sleep' learning module that might be helpful. Would you like to explore it?";
        }

        // Exam/academic stress
        if (input.contains("exam") || input.contains("test") || input.contains("assignment") || input.contains("study") || input.contains("academic")) {
            return "Academic pressure is very common among students. Remember to break large tasks into smaller, manageable steps. Use techniques like the Pomodoro method (25 minutes of focused work with 5-minute breaks). Don't forget to take care of yourself - your wellbeing matters more than perfect grades. Have you tried creating a study schedule?";
        }

        // Loneliness or isolation
        if (input.contains("lonely") || input.contains("alone") || input.contains("isolated") || input.contains("no friends")) {
            return "Feeling lonely can be really difficult. You're not alone in feeling this way. Consider joining our peer support forum where you can connect with other students who understand what you're going through. Small steps like joining a club, attending campus events, or even just saying hello to classmates can help. Would you like me to point you to our community resources?";
        }

        // Overwhelmed
        if (input.contains("overwhelmed") || input.contains("too much") || input.contains("can't cope")) {
            return "Feeling overwhelmed is a sign that you're carrying a lot right now. Let's take this one step at a time. Try to prioritize your tasks - what absolutely needs to be done today? What can wait? Remember, it's okay to ask for help or extensions. Taking even a 5-minute break to breathe can make a difference. How can I best support you right now?";
        }

        // Positive feelings
        if (input.contains("good") || input.contains("great") || input.contains("happy") || input.contains("better")) {
            return "That's wonderful to hear! I'm glad you're feeling positive. Keep nurturing those good feelings through self-care and activities that bring you joy. What's been going well for you?";
        }

        // Gratitude or thanks
        if (input.contains("thank") || input.contains("thanks")) {
            return "You're very welcome! I'm here whenever you need support. Remember, taking care of your mental health is a sign of strength, not weakness. Is there anything else I can help you with?";
        }

        // Requesting counselor
        if (input.contains("counselor") || input.contains("therapist") || input.contains("professional help")) {
            return "Seeking professional support is a great decision. I can help you book a counseling session. Our counselors are experienced in various areas including stress management, anxiety, depression, and academic challenges. Would you like me to show you available time slots?";
        }

        // Breathing exercises
        if (input.contains("breathing") || input.contains("breathe") || input.contains("calm down")) {
            return "Let me guide you through a simple breathing exercise:\n\n1. Find a comfortable position\n2. Breathe in slowly through your nose for 4 counts\n3. Hold for 4 counts\n4. Breathe out slowly through your mouth for 6 counts\n5. Repeat 5 times\n\nThis can help activate your body's relaxation response. How are you feeling after trying this?";
        }

        // Self-care
        if (input.contains("self-care") || input.contains("self care")) {
            return "Self-care is essential for maintaining mental wellbeing! Some ideas include: taking short walks, practicing mindfulness, enjoying a hobby, connecting with friends, getting adequate sleep, eating nutritious meals, and setting healthy boundaries. What self-care activities resonate with you?";
        }

        // Motivation
        if (input.contains("motivation") || input.contains("unmotivated") || input.contains("no energy")) {
            return "Loss of motivation is common and doesn't mean there's something wrong with you. Start small - set one tiny, achievable goal for today. Celebrate small wins. Remember your 'why' - what matters to you? Sometimes motivation follows action, not the other way around. What's one small thing you could do today?";
        }

        // General mental health
        if (input.contains("mental health") || input.contains("wellbeing") || input.contains("wellness")) {
            return "Mental health is just as important as physical health. Our platform offers various resources including self-assessments, learning modules, peer support, and counseling services. Regular self-care, maintaining social connections, and seeking help when needed are all important. What aspect of your mental health would you like to focus on?";
        }
        
        // AF1: Generic supportive response when AI cannot interpret
        return "I appreciate you sharing that with me. While I can provide general support and guidance, some situations benefit from personalized professional help. Our counselors are available if you'd like to discuss this further. In the meantime, please remember: you're not alone, your feelings are valid, and seeking support is a sign of strength. Is there a specific area of wellbeing you'd like to explore together?";
    }
}