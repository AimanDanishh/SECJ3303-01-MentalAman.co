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

import com.secj3303.dao.PersonDao;
import com.secj3303.model.PeerSupportModels;
import com.secj3303.model.PeerSupportModels.ContentCheckResult;
import com.secj3303.model.Person;
import com.secj3303.model.Post;
import com.secj3303.model.Reply;
import com.secj3303.model.Report;
import com.secj3303.service.ForumService;

@Controller
@RequestMapping("/forum")
public class PeerSupportController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private PersonDao personDao;

    private static final String DEFAULT_VIEW = "peer-support";

    // ===============================
    // FORUM DASHBOARD
    // ===============================
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

        Person person = getAuthenticatedPerson(authentication);

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

        model.addAttribute("user", person); // keep name "user" for UI compatibility

        return "app-layout";
    }

    // ===============================
    // CREATE POST
    // ===============================
    @PostMapping("/create")
    public String handleCreatePost(
            @ModelAttribute("newPostFormData") Post newPost,
            Authentication authentication,
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

        Person person = getAuthenticatedPerson(authentication);

        newPost.setAuthor(getAnonymousNameByRole(person.getRole()));
        newPost.setAuthorInitials(getAuthorInitialsByRole(person.getRole()));
        newPost.setTime("Just now");
        newPost.setLikes(0);
        newPost.setTrending(false);
        newPost.setHelpful(false);

        forumService.createPost(newPost);

        redirect.addFlashAttribute("alert", "Your post has been published anonymously! ✓");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", newPost.getCategory());

        return "redirect:/forum";
    }

    // ===============================
    // REPLY
    // ===============================
    @PostMapping("/reply/{postId}")
    public String handleSubmitReply(
            @PathVariable int postId,
            @RequestParam String replyText,
            Authentication authentication,
            RedirectAttributes redirect
    ) {
        if (replyText.trim().isEmpty()) {
            redirect.addFlashAttribute("alert", "Reply cannot be empty.");
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        ContentCheckResult contentCheck =
                PeerSupportModels.checkContentForHarmfulText(replyText);

        if (!contentCheck.isClean) {
            redirect.addFlashAttribute("alert", "Content Warning: " + contentCheck.warning);
            redirect.addFlashAttribute("alertType", "error");
            redirect.addAttribute("expandedId", postId);
            return "redirect:/forum";
        }

        Post post = forumService.getPostById(postId);
        if (post != null) {
            Person person = getAuthenticatedPerson(authentication);

            Reply reply = new Reply();
            reply.setPost(post);
            reply.setAuthor(getAnonymousNameByRole(person.getRole()));
            reply.setAuthorInitials(getAuthorInitialsByRole(person.getRole()));
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

    // ===============================
    // LIKE POST
    // ===============================
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

    // ===============================
    // LIKE REPLY
    // ===============================
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

    // ===============================
    // REPORT
    // ===============================
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

        forumService.createReport(report);

        redirect.addFlashAttribute("alert",
                "Report submitted successfully! Our moderation team will review this within 24 hours.");
        redirect.addFlashAttribute("alertType", "success");
        redirect.addAttribute("category", "all");

        return "redirect:/forum";
    }

    // ===============================
    // DUMMY DATA
    // ===============================
    @GetMapping("/init")
    public String initializeDummyData(RedirectAttributes redirect) {
        forumService.initializeDummyData();
        redirect.addFlashAttribute("alert", "Dummy data initialized successfully!");
        redirect.addFlashAttribute("alertType", "success");
        return "redirect:/forum";
    }

    // ===============================
    // HELPERS
    // ===============================
    private Person getAuthenticatedPerson(Authentication authentication) {
        String email = authentication.getName();
        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw new RuntimeException("Person not found: " + email);
        }
        return person;
    }

    private String getAnonymousNameByRole(String role) {
        switch (role.toUpperCase()) {
            case "STUDENT": return "Anonymous Student";
            case "FACULTY": return "Anonymous Faculty";
            case "COUNSELLOR": return "Anonymous Counsellor";
            case "ADMINISTRATOR": return "Anonymous Admin";
            default: return "Anonymous User";
        }
    }

    private String getAuthorInitialsByRole(String role) {
        switch (role.toUpperCase()) {
            case "STUDENT": return "AS";
            case "FACULTY": return "AF";
            case "COUNSELLOR": return "AC";
            case "ADMINISTRATOR": return "AA";
            default: return "AU";
        }
    }
}
