package com.secj3303.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import com.secj3303.model.Post;
import com.secj3303.model.Reply;
import com.secj3303.model.Report;
import com.secj3303.model.User;
import com.secj3303.service.ForumService;

@Controller
@RequestMapping("/forum")
public class PeerSupportController {

    @Autowired
    private ForumService forumService;

    private static final String DEFAULT_VIEW = "peer-support";

    @GetMapping
    public String forumDashboard(
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(required = false) Integer expandedId,
            @RequestParam(required = false) String modal,
            @RequestParam(required = false) Integer reportId,
            Model model,
            Authentication authentication,
            HttpSession session
    ) {
        List<Post> filteredPosts = forumService.getPostsByCategory(category);
        
        // Load replies for each post if expanded
        if (expandedId != null) {
            for (Post post : filteredPosts) {
                if (post.getId() == expandedId) {
                    // Replies are loaded when accessing getReplies()
                    break;
                }
            }
        }

        User user = buildUser(authentication);

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("categories", forumService.getCategoriesWithCounts());
        model.addAttribute("reportReasons", PeerSupportModels.REPORT_REASONS);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("filteredPosts", filteredPosts);
        model.addAttribute("expandedPost", expandedId);

        model.addAttribute("showCreatePostModal", "create".equals(modal));
        model.addAttribute("showReportModal", "report".equals(modal) && reportId != null);

        if ("report".equals(modal) && reportId != null) {
            Report report = new Report();
            report.setPostId(reportId);
            model.addAttribute("reportFormData", report);
        }

        if ("create".equals(modal)) {
            model.addAttribute("newPostFormData", new Post());
        }

        model.addAttribute("user", user);

        return "app-layout";
    }

    @PostMapping("/create")
    public String handleCreatePost(
            @ModelAttribute("newPostFormData") Post newPost,
            RedirectAttributes redirect
    ) {
        ContentCheckResult titleCheck = PeerSupportModels.checkContentForHarmfulText(newPost.getTitle());
        ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(newPost.getContent());

        if (!titleCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", titleCheck.warning);
        } else if (!contentCheck.isClean) {
            redirect.addFlashAttribute("contentWarning", contentCheck.warning);
        }

        if (newPost.getTitle() == null || newPost.getTitle().trim().isEmpty() ||
            newPost.getContent() == null || newPost.getContent().trim().isEmpty() ||
            redirect.getFlashAttributes().containsKey("contentWarning")) {
            
            redirect.addFlashAttribute("showError", true);
            redirect.addFlashAttribute("newPostFormData", newPost);
            redirect.addAttribute("modal", "create");
            redirect.addAttribute("category", newPost.getCategory());
            return "redirect:/forum";
        }

        // Set default values
        newPost.setAuthor("Anonymous User");
        newPost.setAuthorInitials("AU");
        newPost.setTime("Just now");
        newPost.setLikes(0);
        newPost.setTrending(false);
        newPost.setHelpful(false);

        // Save to database using DAO
        forumService.createPost(newPost);

        redirect.addFlashAttribute("alert", "Your post has been published anonymously! ✓");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", newPost.getCategory());

        return "redirect:/forum";
    }

    @PostMapping("/reply/{postId}")
    public String handleSubmitReply(
            @PathVariable int postId,
            @RequestParam String replyText,
            RedirectAttributes redirect
    ) {
        if (replyText.trim().isEmpty()) {
            redirect.addFlashAttribute("alert", "Reply cannot be empty.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        // Note: Changed from ContentCheckResult to PeerSupportModels.ContentCheckResult
        PeerSupportModels.ContentCheckResult contentCheck = PeerSupportModels.checkContentForHarmfulText(replyText);

        if (!contentCheck.isClean) {
            redirect.addFlashAttribute("alert", "Content Warning: " + contentCheck.warning);
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        Post post = forumService.getPostById(postId);
        if (post != null) {
            Reply reply = new Reply();
            reply.setPost(post);
            reply.setAuthor("Anonymous User");
            reply.setAuthorInitials("AU");
            reply.setTime("Just now");
            reply.setContent(replyText);
            reply.setLikes(0);

            forumService.createReply(reply);

            redirect.addFlashAttribute("alert", "Your reply has been posted anonymously! ✓");
            redirect.addFlashAttribute("alertType", "success");
        }

        redirect.addAttribute("expandedId", postId);
        return "redirect:/forum";
    }

    @PostMapping("/like/{postId}")
    public String handleLike(
            @PathVariable int postId,
            @RequestParam String currentCategory,
            @RequestParam(required = false) Integer expandedId,
            RedirectAttributes redirect
    ) {
        forumService.likePost(postId);

        if (expandedId != null) {
            redirect.addAttribute("expandedId", expandedId);
        }
        redirect.addAttribute("category", currentCategory);
        return "redirect:/forum";
    }

    @PostMapping("/reply/like/{postId}/{replyId}")
    public String handleReplyLike(
            @PathVariable int postId,
            @PathVariable int replyId,
            @RequestParam String currentCategory,
            RedirectAttributes redirect
    ) {
        forumService.likeReply(replyId);

        redirect.addAttribute("category", currentCategory);
        redirect.addAttribute("expandedId", postId);
        return "redirect:/forum";
    }

    @PostMapping("/report/submit")
    public String handleSubmitReport(
            @ModelAttribute Report report,
            RedirectAttributes redirect
    ) {
        if (report.getReason() == null || report.getReason().isEmpty()) {
            redirect.addFlashAttribute("alert", "Please select a reason for reporting.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addFlashAttribute("reportFormData", report);
            redirect.addAttribute("modal", "report");
            redirect.addAttribute("reportId", report.getPostId());
            return "redirect:/forum";
        }

        // Save report to database using DAO
        forumService.createReport(report);

        redirect.addFlashAttribute("alert", "Report submitted successfully! Our moderation team will review this within 24 hours.");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", "all");

        return "redirect:/forum";
    }

    // Helper method to initialize dummy data (optional endpoint)
    @GetMapping("/init")
    public String initializeDummyData(RedirectAttributes redirect) {
        forumService.initializeDummyData();
        redirect.addFlashAttribute("alert", "Dummy data initialized successfully!");
        redirect.addFlashAttribute("alertType", "success");
        return "redirect:/forum";
    }

    private User buildUser(Authentication authentication) {
        User user = new User();
        user.setEmail(authentication.getName());
        user.setName(authentication.getName().split("@")[0]);
        user.setRole(
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", "")
                        .toLowerCase()
        );
        return user;
    }
}