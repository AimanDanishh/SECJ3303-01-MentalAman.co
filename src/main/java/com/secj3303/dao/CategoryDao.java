package com.secj3303.dao;

import java.util.List;

import com.secj3303.model.Category;

public interface CategoryDao {
    List<Category> findAll();
    Category findById(String id);
    void save(Category category);
    void update(Category category);
}