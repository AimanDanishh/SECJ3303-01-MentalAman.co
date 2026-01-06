package com.secj3303.dao;

import java.util.Optional;

import com.secj3303.model.Lesson;

public interface LessonDao {
    void save(Lesson lesson);
    void deleteById(Long id);
    Optional<Lesson> findById(Long id);
}
