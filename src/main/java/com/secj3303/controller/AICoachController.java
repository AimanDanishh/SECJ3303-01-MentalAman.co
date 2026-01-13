package com.secj3303.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.ResponseBody; // CRITICAL IMPORT
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.Message;

@Controller
@RequestMapping("/coach")
public class AICoachController {

    private static final String HISTORY_KEY = "coachChatHistory";

    private List<Message> getChatHistory(HttpSession session) {
        List<Message> history = (List<Message>) session.getAttribute(HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
            Message greeting = new Message("Hello! I'm your AI Mental Health Coach. I'm here to provide support and guidance for your wellbeing journey. How are you feeling today?", "ai");
            history.add(greeting);
            session.setAttribute(HISTORY_KEY, history);
        }
        return history;
    }
    
    // Helper to inject mock data so app-layout doesn't break
    private void addMockLayoutData(Model model) {
        model.addAttribute("modules", new ArrayList<>());
        model.addAttribute("lessons", new ArrayList<>());
        model.addAttribute("quizQuestions", new ArrayList<>());
        model.addAttribute("completedCount", 0);
        model.addAttribute("inProgressCount", 0);
        model.addAttribute("totalModules", 0);
        model.addAttribute("selectedModule", null);
        model.addAttribute("selectedLesson", null);
        model.addAttribute("showQuiz", false);
        model.addAttribute("quizScore", 0);
        model.addAttribute("achievement", null);
    }
    
    @GetMapping
    public String coachView(HttpSession session, Model model) {
        addMockLayoutData(model);
        model.addAttribute("currentView", "coach");
        
        List<Message> history = getChatHistory(session);
        model.addAttribute("messages", history);

        Object user = session.getAttribute("currentUser");
        if (user == null) {
            Map<String, String> demoUser = new HashMap<>();
            demoUser.put("name", "Student");
            demoUser.put("email", "student@healthhub.com");
            demoUser.put("role", "student");
            session.setAttribute("currentUser", demoUser);
            user = demoUser;
        }
        model.addAttribute("user", user);

        return "app-layout";
    }

    // --- UPDATED METHOD FOR AJAX ---
    @PostMapping("/send")
    @ResponseBody // <--- This is the key change! Returns JSON instead of HTML redirect
    public Message sendMessage(@RequestParam String inputMessage, HttpSession session) {
        String cleanMessage = inputMessage.trim();
        if (cleanMessage.isEmpty()) return null;

        List<Message> history = getChatHistory(session);
        
        // 1. Add User Message
        history.add(new Message(cleanMessage, "user"));
        
        // 2. Simulate Thinking Delay (Optional, adds realism)
        try {
            TimeUnit.MILLISECONDS.sleep(500 + ThreadLocalRandom.current().nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Generate and Add AI Response
        String responseText = generateAIResponse(cleanMessage);
        Message aiMessage = new Message(responseText, "ai");
        history.add(aiMessage);
        
        session.setAttribute(HISTORY_KEY, history);

        // 4. Return ONLY the AI message to the frontend
        return aiMessage;
    }

    @PostMapping("/clear")
    public String clearChat(HttpSession session) {
        session.removeAttribute(HISTORY_KEY);
        return "redirect:/coach";
    }

    private String generateAIResponse(String userInput) {
        String input = userInput.toLowerCase().trim();

        if (input.contains("suicid") || input.contains("harm myself") || input.contains("end it") || input.contains("kill myself")) {
            return "I'm concerned about what you've shared. Please reach out to a counselor immediately or contact a crisis helpline:\n\n🆘 National Crisis Hotline: 988\n📞 Crisis Text Line: Text HOME to 741741\n\nYou don't have to face this alone.";
        }
        if (Pattern.compile("^(hi|hello|hey|good morning)").matcher(input).find()) {
            return "Hello! How can I support you today?";
        }
        if (input.contains("stress")) {
            return "I understand you're feeling stressed. Have you tried deep breathing exercises? Inhale for 4 seconds, hold for 7, and exhale for 8.";
        }
        if (input.contains("anxious") || input.contains("anxiety")) {
            return "Anxiety can be tough. Try the 5-4-3-2-1 grounding technique: Name 5 things you see, 4 you feel, 3 you hear, 2 you smell, and 1 you taste.";
        }
        if (input.contains("sleep") || input.contains("tired")) {
            return "Sleep is vital. Try avoiding screens 30 minutes before bed and keeping your room cool and dark.";
        }
        
        return "Thank you for sharing. Could you tell me more about that? I'm here to listen.";
    }
}