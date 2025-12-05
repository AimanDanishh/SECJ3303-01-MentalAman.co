package com.secj3303.controller; // Assuming 'com.example' package based on prior files

import com.secj3303.model.PeerSupportModels;
import com.secj3303.model.PeerSupportModels.Post;
import com.secj3303.model.PeerSupportModels.Reply;
import com.secj3303.model.PeerSupportModels.ReportForm;
import com.secj3303.model.PeerSupportModels.ContentCheckResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession; // Use jakarta for modern Spring
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList; // MISSING IMPORT ADDED
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/forum")
public class PeerSupportController {

    private static final String POSTS_KEY = "forumPosts";
    private static final String DEFAULT_VIEW = "forum";
    // NOTE: TIME_FORMATTER is not used in the provided methods, but kept for context.
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a"); 

    private List<Post> getPosts(HttpSession session) {
        List<Post> posts = (List<Post>) session.getAttribute(POSTS_KEY);
        if (posts == null) {
            posts = PeerSupportModels.getInitialPosts();
            session.setAttribute(POSTS_KEY, posts);
        }
        return posts;
    }

    // --- Main View and Filtering ---

    @GetMapping
    public String forumDashboard(
        @RequestParam(defaultValue = "all") String category,
        @RequestParam(required = false) Integer expandedId,
        @RequestParam(required = false) String modal,
        @RequestParam(required = false) Integer reportId,
        Model model, HttpSession session
    ) {
        List<Post> allPosts = getPosts(session);
        
        List<Post> filteredPosts = category.equals("all") ? allPosts : 
            allPosts.stream().filter(p -> p.getCategory().equals(category)).collect(Collectors.toList());

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("categories", PeerSupportModels.CATEGORIES);
        model.addAttribute("reportReasons", PeerSupportModels.REPORT_REASONS);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("filteredPosts", filteredPosts);
        model.addAttribute("expandedPost", expandedId);
        
        // --- Modal States (Replaces React useState for modals) ---
        model.addAttribute("showCreatePostModal", "create".equals(modal));
        model.addAttribute("showReportModal", "report".equals(modal) && reportId != null);
        
        if ("report".equals(modal) && reportId != null) {
            Optional<Post> postOpt = allPosts.stream().filter(p -> p.getId() == reportId).findFirst();
            if (postOpt.isPresent()) {
                model.addAttribute("reportPostId", reportId);
                // Pre-populate form object for error handling/initial report setup
                if (!model.containsAttribute("reportFormData")) {
                     ReportForm form = new ReportForm();
                     form.setPostId(reportId);
                     model.addAttribute("reportFormData", form);
                }
            } else {
                model.addAttribute("showReportModal", false);
            }
        }
        
        // Pass empty post form if creating
        if ("create".equals(modal) && !model.containsAttribute("newPostFormData")) {
             model.addAttribute("newPostFormData", new Post());
        }

        return "app-layout";
    }

    // --- Create New Post Handler ---

    @PostMapping("/create")
    public String handleCreatePost(@ModelAttribute Post newPostFormData, HttpSession session, RedirectAttributes redirect) {
        
        // --- Content Filter (Server-side) ---
        ContentCheckResult titleCheck = PeerSupportModels.checkContentForHarmfulText(newPostFormData.getTitle());
        ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(newPostFormData.getContent());
        
        if (!titleCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", titleCheck.warning);
        } else if (!contentCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", contentCheck.warning);
        }
        
        // Validation (Replicating TSX logic)
        if (newPostFormData.getTitle() == null || newPostFormData.getTitle().trim().isEmpty() ||
            newPostFormData.getContent() == null || newPostFormData.getContent().trim().isEmpty() ||
            redirect.getFlashAttributes().containsKey("contentWarning")) {
            
            // Redirect back to the form with data and warning
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("newPostFormData", newPostFormData); 
            redirect.addAttribute("modal", "create");
            redirect.addAttribute("category", newPostFormData.getCategory());
            return "redirect:/forum";
        }
        
        // Success: Post message anonymously
        List<Post> posts = getPosts(session);
        AtomicInteger maxId = new AtomicInteger(posts.stream().mapToInt(Post::getId).max().orElse(0));
        
        // --- FIX: Use Setters to avoid 'private access' error ---
        newPostFormData.setId(maxId.incrementAndGet());
        newPostFormData.setAuthor("Anonymous User");
        newPostFormData.setAuthorInitials("AU");
        newPostFormData.setTime("Just now");
        newPostFormData.setLikes(0);
        newPostFormData.setReplies(new ArrayList<>()); // FIX: Use ArrayList constructor
        newPostFormData.setTrending(false);
        newPostFormData.setHelpful(false);

        posts.add(0, newPostFormData);
        session.setAttribute(POSTS_KEY, posts);
        
        redirect.addFlashAttribute("alert", "Your post has been published anonymously! ✓");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", newPostFormData.getCategory());
        return "redirect:/forum";
    }

    // --- Reply Handler ---
    
    @PostMapping("/reply/{postId}")
    public String handleSubmitReply(@PathVariable int postId, @RequestParam String replyText, HttpSession session, RedirectAttributes redirect) {
        
        if (replyText.trim().isEmpty()) {
            redirect.addFlashAttribute("alert", "Reply cannot be empty.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }
        
        // --- Content Filter (Server-side) ---
        ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(replyText);
        if (!contentCheck.isClean) {
            redirect.addFlashAttribute("alert", "Content Warning: " + contentCheck.warning);
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        List<Post> posts = getPosts(session);
        posts.stream().filter(p -> p.getId() == postId).findFirst().ifPresent(post -> {
            
            AtomicInteger maxReplyId = new AtomicInteger(post.getReplies().stream().mapToInt(Reply::getId).max().orElse(0));

            Reply newReply = new Reply();
            
            // --- FIX: Use Setters for Reply ---
            newReply.setId(maxReplyId.incrementAndGet());
            newReply.setAuthor("Anonymous User");
            newReply.setAuthorInitials("AU");
            newReply.setTime("Just now");
            newReply.setContent(replyText);
            newReply.setLikes(0);
            
            post.getReplies().add(newReply);
            session.setAttribute(POSTS_KEY, posts);
            
            redirect.addFlashAttribute("alert", "Your reply has been posted anonymously! ✓");
            redirect.addFlashAttribute("alertType", "success");
        });
        
        redirect.addAttribute("expandedId", postId);
        return "redirect:/forum";
    }
    
    // --- Like Handlers ---

    @PostMapping("/like/{postId}")
    public String handleLike(@PathVariable int postId, @RequestParam String currentCategory, @RequestParam(required = false) Integer expandedId, HttpSession session) {
        List<Post> posts = getPosts(session);
        posts.stream().filter(p -> p.getId() == postId).findFirst().ifPresent(post -> {
            post.setLikes(post.getLikes() + 1);
        });
        session.setAttribute(POSTS_KEY, posts);
        
        // Maintain view state
        if (expandedId != null) {
            return "redirect:/forum?category=" + currentCategory + "&expandedId=" + expandedId;
        }
        return "redirect:/forum?category=" + currentCategory;
    }

    @PostMapping("/reply/like/{postId}/{replyId}")
    public String handleReplyLike(@PathVariable int postId, @PathVariable int replyId, @RequestParam String currentCategory, HttpSession session) {
        List<Post> posts = getPosts(session);
        posts.stream().filter(p -> p.getId() == postId).findFirst().ifPresent(post -> {
            post.getReplies().stream().filter(r -> r.getId() == replyId).findFirst().ifPresent(reply -> {
                reply.setLikes(reply.getLikes() + 1);
            });
        });
        session.setAttribute(POSTS_KEY, posts);
        
        // Maintain view state
        return "redirect:/forum?category=" + currentCategory + "&expandedId=" + postId;
    }
    
    // --- Report Handler ---

    @PostMapping("/report/submit")
    public String handleSubmitReport(@ModelAttribute ReportForm reportFormData, HttpSession session, RedirectAttributes redirect) {
        
        if (reportFormData.getReason() == null || reportFormData.getReason().isEmpty()) {
             redirect.addFlashAttribute("alert", "Please select a reason for reporting.");
             redirect.addFlashAttribute("alertType", "error");
             redirect.addFlashAttribute("reportFormData", reportFormData);
             redirect.addAttribute("modal", "report");
             redirect.addAttribute("reportId", reportFormData.getPostId());
             return "redirect:/forum";
        }
        
        // In a real app, this would log the report to an admin dashboard (similar to AtRiskReferral)
        // We simulate success here.
        
        redirect.addFlashAttribute("alert", "Report submitted successfully! Our moderation team will review this within 24 hours.");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", "all");
        return "redirect:/forum";
    }
}