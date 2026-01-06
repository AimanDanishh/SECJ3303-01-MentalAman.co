package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {
    Optional<T> findById(Integer id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    void delete(Integer id);
    void delete(T entity);
}