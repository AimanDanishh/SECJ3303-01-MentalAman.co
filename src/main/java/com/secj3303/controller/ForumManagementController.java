package com.secj3303.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.dao.PostDao;
import com.secj3303.dao.ReportDao;
import com.secj3303.model.Post;
import com.secj3303.model.Report;

@Controller
@RequestMapping("/forum-management") // Updated URL path
public class ForumManagementController {

    @Autowired
    private PostDao postDao;

    @Autowired
    private ReportDao reportDao;

    public static class ReportedPostDTO {
        public Report report;
        public Post post;

        public ReportedPostDTO(Report report, Post post) {
            this.report = report;
            this.post = post;
        }
        public Report getReport() { return report; }
        public Post getPost() { return post; }
    }

    private void addLayoutData(Model model) {
        model.addAttribute("user", new MockUser("Admin", "admin@healthhub.com", "ROLE_ADMINISTRATOR"));
        
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
    public String dashboard(
            @RequestParam(defaultValue = "posts") String view, 
            Model model
    ) {
        addLayoutData(model);
        
        model.addAttribute("currentView", "forum-management"); 
        model.addAttribute("activeView", view);

        if ("moderation".equals(view)) {
            List<Report> reports = reportDao.findAll();
            List<ReportedPostDTO> reportedPosts = new ArrayList<>();

            for (Report r : reports) {
                try {
                    Post p = postDao.findById(r.getPostId());
                    if (p != null) {
                        reportedPosts.add(new ReportedPostDTO(r, p));
                    } else {
                        reportDao.delete(r.getId());
                    }
                } catch (Exception e) {

                }
            }
            model.addAttribute("reportedPosts", reportedPosts);
        
        } else {
            List<Post> allPosts = postDao.findAll();
            model.addAttribute("allPosts", allPosts);
        }

        return "app-layout";
    }

    // Delete a Post 
    @PostMapping("/delete-post/{id}")
    public String deletePost(@PathVariable int id, RedirectAttributes redirect) {
        try {
            reportDao.deleteByPostId(id); 
            postDao.delete(id);
            redirect.addFlashAttribute("successMessage", "Forum post deleted successfully.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Error deleting post.");
        }
        return "redirect:/forum-management?view=posts";
    }

    // Dismiss a Report
    @PostMapping("/moderation/dismiss/{reportId}")
    public String dismissReport(@PathVariable int reportId, RedirectAttributes redirect) {
        try {
            reportDao.delete(reportId);
            redirect.addFlashAttribute("successMessage", "Report dismissed. Post remains active.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Error dismissing report.");
        }
        return "redirect:/forum-management?view=moderation";
    }

    // Delete Reported Post
    @PostMapping("/moderation/delete/{postId}")
    public String deleteReportedPost(@PathVariable int postId, RedirectAttributes redirect) {
        try {
            reportDao.deleteByPostId(postId); // Clean up reports
            postDao.delete(postId);           // Delete content
            redirect.addFlashAttribute("successMessage", "Post and associated reports deleted successfully.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Error deleting post.");
        }
        return "redirect:/forum-management?view=moderation";
    }


    public static class MockUser {
        public String name, email, role;
        public MockUser(String n, String e, String r) { name=n; email=e; role=r; }
    }
}