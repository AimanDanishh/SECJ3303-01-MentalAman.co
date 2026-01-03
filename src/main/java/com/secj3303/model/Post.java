package com.secj3303.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "forum_posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String author;
    private String authorInitials;
    private String time;
    private String category;
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private int likes;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reply> replies = new ArrayList<>();
    
    private boolean trending;
    private boolean helpful;
    
    public Post() {}
    
    public Post(int id, String author, String authorInitials, String time, 
                String category, String title, String content, int likes, 
                boolean trending, boolean helpful) {
        this.id = id;
        this.author = author;
        this.authorInitials = authorInitials;
        this.time = time;
        this.category = category;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.trending = trending;
        this.helpful = helpful;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getAuthorInitials() { return authorInitials; }
    public void setAuthorInitials(String authorInitials) { this.authorInitials = authorInitials; }
    
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    
    public List<Reply> getReplies() { return replies; }
    public void setReplies(List<Reply> replies) { this.replies = replies; }
    
    public boolean isTrending() { return trending; }
    public void setTrending(boolean trending) { this.trending = trending; }
    
    public boolean isHelpful() { return helpful; }
    public void setHelpful(boolean helpful) { this.helpful = helpful; }
}