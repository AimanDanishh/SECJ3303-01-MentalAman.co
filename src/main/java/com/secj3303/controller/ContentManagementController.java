package com.secj3303.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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

import com.secj3303.model.ContentManagementModels;
import com.secj3303.model.ContentManagementModels.ContentItem;

@Controller
@RequestMapping("/content")
public class ContentManagementController {

    private static final String CONTENT_KEY = "contentItems";
    private static final String DEFAULT_VIEW = "content";

    private List<ContentItem> getContentItems(HttpSession session) {
        List<ContentItem> items = (List<ContentItem>) session.getAttribute(CONTENT_KEY);
        if (items == null) {
            items = ContentManagementModels.getInitialContentItems();
            session.setAttribute(CONTENT_KEY, items);
        }
        return items;
    }

    // --- 1. Main View Handler (List/Tabs) ---
    
    @GetMapping
    public String contentDashboard(
        @RequestParam(defaultValue = "module") String selectedType,
        @RequestParam(required = false) Integer viewId,
        @RequestParam(required = false) Integer editId,
        Model model, HttpSession session
    ) {
        List<ContentItem> allItems = getContentItems(session);
        
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("contentItems", allItems);
        model.addAttribute("filteredContent", allItems.stream()
            .filter(item -> item.getType().equals(selectedType))
            .collect(Collectors.toList()));
        
        // Default flags (List Mode)
        model.addAttribute("isCreating", false);
        model.addAttribute("isEditing", false);
        model.addAttribute("isViewing", false);
        model.addAttribute("categories", ContentManagementModels.CATEGORIES);

        // --- Handle View Mode (Replicates isViewing conditional) ---
        if (viewId != null) {
            Optional<ContentItem> itemOpt = allItems.stream().filter(i -> i.getId() == viewId).findFirst();
            if (itemOpt.isPresent()) {
                model.addAttribute("isViewing", true);
                model.addAttribute("selectedItem", itemOpt.get());
                return "app-layout";
            }
        }
        
        // --- Handle Edit Mode (Replicates isEditing conditional) ---
        if (editId != null) {
            Optional<ContentItem> itemOpt = allItems.stream().filter(i -> i.getId() == editId).findFirst();
            if (itemOpt.isPresent()) {
                model.addAttribute("isEditing", true);
                model.addAttribute("selectedItem", itemOpt.get());
                model.addAttribute("formData", itemOpt.get()); // Pre-populate form
                return "app-layout";
            }
        }

        // List Mode
        return "app-layout";
    }
    
    // --- 2. Create Form View Handler ---

    @GetMapping("/create")
    public String createContentForm(@RequestParam String selectedType, Model model) {
        model.addAttribute("currentView", DEFAULT_VIEW);
        model.addAttribute("isCreating", true);
        model.addAttribute("selectedType", selectedType);
        
        // Provide a blank form object
        ContentItem newForm = new ContentItem();
        newForm.setType(selectedType);
        model.addAttribute("formData", newForm);
        model.addAttribute("categories", ContentManagementModels.CATEGORIES);
        
        return "app-layout";
    }

    // --- 3. Save/Update Handler (Replicates handleSave) ---

    @PostMapping("/save")
    public String handleSave(@ModelAttribute ContentItem formData, 
                             HttpSession session, RedirectAttributes redirect) {
        
        // --- Validation Logic (Replicates TSX logic) ---
        if (formData.getTitle() == null || formData.getTitle().trim().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Title is required.");
        } else if (formData.getDescription() == null || formData.getDescription().trim().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Description is required.");
        } else if (formData.getCategory() == null || formData.getCategory().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Please select a category.");
        }
        
        if (redirect.getFlashAttributes().containsKey("errorMessage")) {
            redirect.addFlashAttribute("showError", true);
            // If error, redirect back to the form
            if (formData.getId() != 0) {
                redirect.addAttribute("editId", formData.getId());
            } else {
                redirect.addAttribute("selectedType", formData.getType());
                return "redirect:/content/create";
            }
            return "redirect:/content";
        }
        
        // Simulate save failure (10% chance)
        if (ThreadLocalRandom.current().nextDouble() < 0.1) {
             redirect.addFlashAttribute("errorMessage", "Failed to save content. Database connection error. Please try again.");
             redirect.addFlashAttribute("showError", true);
             redirect.addAttribute("selectedType", formData.getType());
             return "redirect:/content";
        }

        List<ContentItem> items = getContentItems(session);
        String currentDateTime = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        if (formData.getId() != 0) {
            // Update existing item
            items.stream().filter(i -> i.getId() == formData.getId()).findFirst().ifPresent(item -> {
                item.setTitle(formData.getTitle());
                item.setDescription(formData.getDescription());
                item.setCategory(formData.getCategory());
                item.setStatus(formData.getStatus());
                item.setLastModified(currentDateTime);
            });
            redirect.addFlashAttribute("successMessage", "\"" + formData.getTitle() + "\" has been successfully updated!");
        } else {
            // Create new item
            AtomicInteger maxId = new AtomicInteger(items.stream().mapToInt(ContentItem::getId).max().orElse(0));
            formData.setId(maxId.incrementAndGet());
            formData.setCreatedDate(currentDateTime);
            formData.setLastModified(currentDateTime);
            formData.setCreatedBy("Current Admin");
            items.add(0, formData); // Add to the top
            redirect.addFlashAttribute("successMessage", "\"" + formData.getTitle() + "\" has been successfully created!");
        }

        session.setAttribute(CONTENT_KEY, items);
        redirect.addFlashAttribute("showSuccess", true);
        redirect.addAttribute("selectedType", formData.getType());
        return "redirect:/content";
    }

    // --- 4. Delete Handler (Replicates handleDelete) ---

    @PostMapping("/delete/{id}")
    public String handleDelete(@PathVariable int id, @RequestParam String itemType, HttpSession session, RedirectAttributes redirect) {
        List<ContentItem> items = getContentItems(session);
        
        Optional<ContentItem> itemOpt = items.stream().filter(i -> i.getId() == id).findFirst();
        if (itemOpt.isPresent()) {
            String title = itemOpt.get().getTitle();
            items.removeIf(i -> i.getId() == id);
            session.setAttribute(CONTENT_KEY, items);
            redirect.addFlashAttribute("successMessage", "\"" + title + "\" has been successfully deleted.");
            redirect.addFlashAttribute("showSuccess", true);
        }

        redirect.addAttribute("selectedType", itemType);
        return "redirect:/content";
    }
}