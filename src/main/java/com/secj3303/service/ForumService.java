package com.secj3303.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.dao.CategoryDao;
import com.secj3303.dao.PostDao;
import com.secj3303.dao.ReplyDao;
import com.secj3303.dao.ReportDao;
import com.secj3303.model.Category;
import com.secj3303.model.Post;
import com.secj3303.model.Reply;
import com.secj3303.model.Report;

@Service
@Transactional
public class ForumService {

    @Autowired
    private PostDao postDao;
    
    @Autowired
    private ReplyDao replyDao;
    
    @Autowired
    private ReportDao reportDao;
    
    @Autowired
    private CategoryDao categoryDao;

    public List<Post> getAllPosts() {
        return postDao.findAllOrderByLikesDesc();
    }
    
    public List<Post> getPostsByCategory(String category) {
        if ("all".equals(category)) {
            return postDao.findAllOrderByLikesDesc();
        }
        return postDao.findByCategory(category);
    }
    
    public Post getPostById(int id) {
        return postDao.findById(id);
    }
    
    public int createPost(Post post) {
        // Calculate total posts for "all" category
        updateAllCategoryCount();
        return postDao.save(post);
    }
    
    public void updatePost(Post post) {
        postDao.update(post);
    }
    
    public void likePost(int postId) {
        Post post = postDao.findById(postId);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            postDao.update(post);
        }
    }
    
    public int createReply(Reply reply) {
        return replyDao.save(reply);
    }
    
    public void likeReply(int replyId) {
        Reply reply = replyDao.findById(replyId);
        if (reply != null) {
            reply.setLikes(reply.getLikes() + 1);
            replyDao.update(reply);
        }
    }
    
    public int createReport(Report report) {
        return reportDao.save(report);
    }
    
    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }
    
    public List<Map<String, String>> getCategoriesWithCounts() {
        List<Category> categories = categoryDao.findAll();
        
        // Update all category count with total posts
        updateAllCategoryCount();
        
        return categories.stream()
                .map(cat -> {
                    int count;
                    if ("all".equals(cat.getId())) {
                        // Get total post count for "all" category
                        count = (int) postDao.findAll().stream().count();
                    } else {
                        count = postDao.countByCategory(cat.getId());
                    }
                    return Map.of(
                        "id", cat.getId(),
                        "label", cat.getLabel(),
                        "count", String.valueOf(count)
                    );
                })
                .collect(Collectors.toList());
    }
    
    private void updateAllCategoryCount() {
        Category allCategory = categoryDao.findById("all");
        if (allCategory != null) {
            int totalPosts = (int) postDao.findAll().stream().count();
            allCategory.setCount(totalPosts);
            categoryDao.update(allCategory);
        }
    }
    
    public void initializeDummyData() {
        // Clear existing data
        List<Post> posts = postDao.findAll();
        for (Post post : posts) {
            postDao.delete(post.getId());
        }
        
        // Insert initial categories if not exists
        if (categoryDao.findById("all") == null) {
            categoryDao.save(new Category("all", "All Posts", 5));
        }
        if (categoryDao.findById("anxiety") == null) {
            categoryDao.save(new Category("anxiety", "Anxiety", 1));
        }
        if (categoryDao.findById("stress") == null) {
            categoryDao.save(new Category("stress", "Stress", 2));
        }
        if (categoryDao.findById("depression") == null) {
            categoryDao.save(new Category("depression", "Depression", 1));
        }
        if (categoryDao.findById("motivation") == null) {
            categoryDao.save(new Category("motivation", "Motivation", 1));
        }
        
        // Create initial posts
        Post post1 = new Post(0, "Anonymous Student", "AS", "2 hours ago", "anxiety", 
            "Struggling with exam anxiety", 
            "Finals are coming up and I'm feeling overwhelmed. Has anyone found helpful strategies for managing exam stress?", 
            24, true, true);
        
        Post post2 = new Post(0, "Anonymous User", "AU", "3 hours ago", "stress", 
            "EARN $5000 FROM HOME - CLICK HERE NOW!!!", 
            "Hey everyone! I found this AMAZING opportunity to make money from home. Just click this link and sign up! You can make thousands of dollars working just 2 hours a day! Message me for details. This is NOT a scam, I promise! Limited spots available!!!", 
            0, false, false);
        
        Post post3 = new Post(0, "Anonymous Faculty", "AF", "5 hours ago", "motivation", 
            "Daily gratitude practice changed my life", 
            "I wanted to share how starting a simple gratitude journal has helped me maintain a more positive outlook. It only takes 5 minutes each morning!", 
            45, true, false);
        
        Post post4 = new Post(0, "Anonymous Student", "AS", "1 day ago", "stress", 
            "Need advice on work-life balance", 
            "Finding it hard to balance studies, part-time work, and personal time. How do you all manage everything?", 
            18, false, false);
        
        Post post5 = new Post(0, "Anonymous Student", "AS", "1 day ago", "depression", 
            "Feeling isolated - seeking support", 
            "Been feeling disconnected from friends and struggling to reach out. Anyone else experienced this?", 
            31, false, true);

        // Save posts
        int post1Id = postDao.save(post1);
        postDao.save(post2);
        postDao.save(post3);
        postDao.save(post4);
        postDao.save(post5);
        
        // Update category counts
        updateAllCategoryCount();

        // Create replies for post1
        Post savedPost1 = postDao.findById(post1Id);
        
        Reply reply1 = new Reply(0, "Anonymous Faculty", "AF", "1 hour ago", 
            "I've found that creating a study schedule helps reduce anxiety. Break your studying into manageable chunks!", 8);
        reply1.setPost(savedPost1);
        
        Reply reply2 = new Reply(0, "Anonymous Student", "AS", "30 minutes ago", 
            "Meditation and deep breathing exercises have really helped me. There are some great apps for this!", 5);
        reply2.setPost(savedPost1);
        
        // Create reply for post3
        Reply reply3 = new Reply(0, "Anonymous Student", "AS", "4 hours ago", 
            "This is inspiring! How do you stay consistent with it?", 3);
        reply3.setPost(post3);

        // Save replies
        replyDao.save(reply1);
        replyDao.save(reply2);
        replyDao.save(reply3);
    }
}