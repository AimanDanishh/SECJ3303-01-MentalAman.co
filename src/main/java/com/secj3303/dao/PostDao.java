package com.secj3303.dao;

import java.util.List;

import com.secj3303.model.Post;

public interface PostDao {
    List<Post> findAll();
    List<Post> findByCategory(String category);
    Post findById(int id);
    int save(Post post);
    void update(Post post);
    void delete(int id);
    int countByCategory(String category);
    List<Post> findAllOrderByLikesDesc();
}