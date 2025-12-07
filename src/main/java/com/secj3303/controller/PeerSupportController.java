package com.secj3303.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.PeerSupportModels;
import com.secj3303.model.PeerSupportModels.ContentCheckResult;
import com.secj3303.model.PeerSupportModels.Post;
import com.secj3303.model.PeerSupportModels.Reply;
import com.secj3303.model.PeerSupportModels.ReportForm;
import com.secj3303.service.AuthenticationService;

@Controller
@RequestMapping("/forum")
public class PeerSupportController {
    private final AuthenticationService authenticationService;
    private static final String POSTS_KEY = "forumPosts";
    
    // 1. MATCH FILENAME: This tells app-layout which file to inject (peer-support.html)
    private static final String DEFAULT_VIEW = "peer-support";
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a"); 

    public PeerSupportController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private List<Post> getPosts(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) session.getAttribute(POSTS_KEY);
        if (posts == null) {
            posts = new ArrayList<>(PeerSupportModels.getInitialPosts());
            session.setAttribute(POSTS_KEY, posts);
        }
        return posts;
    }

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
        
        model.addAttribute("showCreatePostModal", "create".equals(modal));
        model.addAttribute("showReportModal", "report".equals(modal) && reportId != null);
        
        if ("report".equals(modal) && reportId != null) {
            Optional<Post> postOpt = allPosts.stream().filter(p -> p.getId() == reportId).findFirst();
            if (postOpt.isPresent()) {
                model.addAttribute("reportPostId", reportId);
                if (!model.containsAttribute("reportFormData")) {
                     ReportForm form = new ReportForm();
                     form.setPostId(reportId);
                     model.addAttribute("reportFormData", form);
                }
            } else {
                model.addAttribute("showReportModal", false);
            }
        }
        
        if ("create".equals(modal) && !model.containsAttribute("newPostFormData")) {
             model.addAttribute("newPostFormData", new Post());
        }
        
        model.addAttribute("user", authenticationService.getAuthenticatedUser(session));
        
        // 2. RETURN LAYOUT: load app-layout.html, which will use DEFAULT_VIEW to find peer-support
        return "app-layout";
    }

    // --- Create New Post Handler ---
    @PostMapping("/create")
    public String handleCreatePost(@ModelAttribute Post newPostFormData, HttpSession session, RedirectAttributes redirect) {
        
        ContentCheckResult titleCheck = PeerSupportModels.checkContentForHarmfulText(newPostFormData.getTitle());
        ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(newPostFormData.getContent());
        
        if (!titleCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", titleCheck.warning);
        } else if (!contentCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", contentCheck.warning);
        }
        
        if (newPostFormData.getTitle() == null || newPostFormData.getTitle().trim().isEmpty() ||
            newPostFormData.getContent() == null || newPostFormData.getContent().trim().isEmpty() ||
            redirect.getFlashAttributes().containsKey("contentWarning")) {
            
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("newPostFormData", newPostFormData); 
            redirect.addAttribute("modal", "create");
            redirect.addAttribute("category", newPostFormData.getCategory());
            return "redirect:/forum";
        }
        
        List<Post> posts = getPosts(session);
        List<Post> mutablePosts = new ArrayList<>(posts);
        
        AtomicInteger maxId = new AtomicInteger(mutablePosts.stream().mapToInt(Post::getId).max().orElse(0));
        
        newPostFormData.setId(maxId.incrementAndGet());
        newPostFormData.setAuthor("Anonymous User");
        newPostFormData.setAuthorInitials("AU");
        newPostFormData.setTime("Just now");
        newPostFormData.setLikes(0);
        newPostFormData.setReplies(new ArrayList<>());
        newPostFormData.setTrending(false);
        newPostFormData.setHelpful(false);

        mutablePosts.add(0, newPostFormData);
        session.setAttribute(POSTS_KEY, mutablePosts);
        
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
        
        ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(replyText);
        if (!contentCheck.isClean) {
            redirect.addFlashAttribute("alert", "Content Warning: " + contentCheck.warning);
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        List<Post> posts = getPosts(session);
        posts.stream().filter(p -> p.getId() == postId).findFirst().ifPresent(post -> {
            
            List<Reply> mutableReplies = new ArrayList<>(post.getReplies());
            post.setReplies(mutableReplies);

            AtomicInteger maxReplyId = new AtomicInteger(mutableReplies.stream().mapToInt(Reply::getId).max().orElse(0));

            Reply newReply = new Reply();
            newReply.setId(maxReplyId.incrementAndGet());
            newReply.setAuthor("Anonymous User");
            newReply.setAuthorInitials("AU");
            newReply.setTime("Just now");
            newReply.setContent(replyText);
            newReply.setLikes(0);
            
            mutableReplies.add(newReply);
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
        
        return "redirect:/forum?category=" + currentCategory + "&expandedId=" + postId;
    }
    
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
        
        redirect.addFlashAttribute("alert", "Report submitted successfully! Our moderation team will review this within 24 hours.");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", "all");
        return "redirect:/forum";
    }
}