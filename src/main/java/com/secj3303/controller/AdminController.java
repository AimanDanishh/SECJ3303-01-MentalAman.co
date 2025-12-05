package com.secj3303.controller;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.secj3303.model.AdminData;
import com.secj3303.model.AdminData.ContentModule;
import com.secj3303.model.AdminData.FlaggedContentItem;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String CONTENT_KEY = "contentManagement";
    private static final String FLAGGED_KEY = "flaggedContent";
    private static final String DEFAULT_VIEW = "admin";

    // --- Session Access Utilities ---
    
    private List<ContentModule> getContentModules(HttpSession session) {
        List<ContentModule> modules = (List<ContentModule>) session.getAttribute(CONTENT_KEY);
        if (modules == null) {
            modules = AdminData.getInitialContentManagement();
            session.setAttribute(CONTENT_KEY, modules);
        }
        return modules;
    }

    private List<FlaggedContentItem> getFlaggedContent(HttpSession session) {
        List<FlaggedContentItem> flagged = (List<FlaggedContentItem>) session.getAttribute(FLAGGED_KEY);
        if (flagged == null) {
            flagged = AdminData.getInitialFlaggedContent();
            session.setAttribute(FLAGGED_KEY, flagged);
        }
        return flagged;
    }

    // --- 1. Main Dashboard (Replaces main return block) ---

    @GetMapping
    public String adminDashboard(HttpSession session, Model model) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("modules", getContentModules(session));
        model.addAttribute("flaggedContent", getFlaggedContent(session));
        model.addAttribute("systemStats", AdminData.SYSTEM_STATS);
        model.addAttribute("recentActivity", AdminData.RECENT_ACTIVITY);
        model.addAttribute("analyticsData", AdminData.ANALYTICS_DATA);
        
        // View flags (all false for main dashboard)
        model.addAttribute("isViewingModule", false);
        model.addAttribute("isEditingModule", false);
        model.addAttribute("isAddingModule", false);

        return "app-layout";
    }

    // --- 2. Module Detail View (Replaces isViewingModule conditional) ---

    @GetMapping("/module/view")
    public String viewModule(@RequestParam int id, HttpSession session, Model model) {
        List<ContentModule> modules = getContentModules(session);
        Optional<ContentModule> moduleOpt = modules.stream().filter(m -> m.getId() == id).findFirst();

        if (moduleOpt.isEmpty()) {
            return "redirect:/admin";
        }
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("isViewingModule", true);
        model.addAttribute("selectedModule", moduleOpt.get());
        
        // Ensure other flags are false
        model.addAttribute("isEditingModule", false);
        model.addAttribute("isAddingModule", false);
        
        return "app-layout";
    }

    // --- 3. Module Form View (Replaces isEditingModule / isAddingModule conditional) ---

    @GetMapping("/module/form")
    public String moduleForm(@RequestParam(required = false) Integer id, HttpSession session, Model model) {
        ContentModule moduleForm;
        
        if (id != null) {
            // Edit Mode
            List<ContentModule> modules = getContentModules(session);
            moduleForm = modules.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .orElseGet(ContentModule::new);
            
            model.addAttribute("isEditingModule", true);
            model.addAttribute("isAddingModule", false);
        } else {
            // Add Mode
            moduleForm = new ContentModule();
            model.addAttribute("isEditingModule", false);
            model.addAttribute("isAddingModule", true);
        }

        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("moduleFormData", moduleForm);
        model.addAttribute("categories", AdminData.CATEGORIES);
        model.addAttribute("isViewingModule", false);
        
        return "app-layout";
    }

    // --- Module Save Logic (Replaces handleSaveModule) ---

    @PostMapping("/module/save")
    public String saveModule(@ModelAttribute ContentModule moduleFormData, 
                             HttpSession session, RedirectAttributes redirect) {
        
        List<ContentModule> modules = getContentModules(session);
        
        if (moduleFormData.getModule() == null || moduleFormData.getModule().isEmpty() ||
            moduleFormData.getDescription() == null || moduleFormData.getDescription().isEmpty() ||
            moduleFormData.getCategory() == null || moduleFormData.getCategory().isEmpty()) {
            
            redirect.addFlashAttribute("errorMessage", "Please fill in all required fields.");
            // Send user back to the form
            redirect.addAttribute("id", moduleFormData.getId() != 0 ? moduleFormData.getId() : null);
            return "redirect:/admin/module/form";
        }

        if (moduleFormData.getId() != 0) {
            // Update existing module (replicating TSX logic)
            modules.replaceAll(m -> m.getId() == moduleFormData.getId() ? moduleFormData : m);
            redirect.addFlashAttribute("successMessage", "Module \"" + moduleFormData.getModule() + "\" has been successfully updated!");
        } else {
            // Add new module
            AtomicInteger maxId = new AtomicInteger(modules.stream().mapToInt(ContentModule::getId).max().orElse(0));
            moduleFormData.setId(maxId.incrementAndGet());
            modules.add(moduleFormData);
            redirect.addFlashAttribute("successMessage", "Module \"" + moduleFormData.getModule() + "\" has been successfully created!");
        }

        session.setAttribute(CONTENT_KEY, modules);
        return "redirect:/admin";
    }

    // --- Module Delete Logic (Replaces handleDeleteModule) ---

    @PostMapping("/module/delete")
    public String deleteModule(@RequestParam int id, HttpSession session, RedirectAttributes redirect) {
        List<ContentModule> modules = getContentModules(session);
        
        Optional<ContentModule> moduleToDelete = modules.stream().filter(m -> m.getId() == id).findFirst();
        if (moduleToDelete.isPresent()) {
            modules.removeIf(m -> m.getId() == id);
            session.setAttribute(CONTENT_KEY, modules);
            redirect.addFlashAttribute("successMessage", "Module \"" + moduleToDelete.get().getModule() + "\" has been successfully deleted.");
        }
        
        return "redirect:/admin";
    }

    // --- Flagged Content Actions (Replacing handleRemoveContent/handleDismissFlag) ---

    @PostMapping("/flagged/remove")
    public String removeFlaggedContent(@RequestParam int id, @RequestParam String type, HttpSession session, RedirectAttributes redirect) {
        List<FlaggedContentItem> flagged = getFlaggedContent(session);
        
        flagged.removeIf(item -> item.getId() == id);
        session.setAttribute(FLAGGED_KEY, flagged);
        
        redirect.addFlashAttribute("successMessage", type + " has been permanently removed and user has been notified.");
        return "redirect:/admin";
    }

    @PostMapping("/flagged/dismiss")
    public String dismissFlag(@RequestParam int id, @RequestParam String type, HttpSession session, RedirectAttributes redirect) {
        List<FlaggedContentItem> flagged = getFlaggedContent(session);
        
        flagged.removeIf(item -> item.getId() == id);
        session.setAttribute(FLAGGED_KEY, flagged);
        
        redirect.addFlashAttribute("successMessage", "Flag for \"" + type + "\" has been dismissed. Content remains visible.");
        return "redirect:/admin";
    }
}