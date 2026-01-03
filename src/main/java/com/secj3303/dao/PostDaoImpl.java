package com.secj3303.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.Post;

@Repository
@Transactional
public class PostDaoImpl implements PostDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Post> findAll() {
        String jpql = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.replies ORDER BY p.id DESC";
        return entityManager.createQuery(jpql, Post.class).getResultList();
    }

    @Override
    public List<Post> findByCategory(String category) {
        String jpql = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.replies WHERE p.category = :category ORDER BY p.id DESC";
        TypedQuery<Post> query = entityManager.createQuery(jpql, Post.class);
        query.setParameter("category", category);
        return query.getResultList();
    }

    @Override
    public Post findById(int id) {
        String jpql = "SELECT p FROM Post p LEFT JOIN FETCH p.replies WHERE p.id = :id";
        TypedQuery<Post> query = entityManager.createQuery(jpql, Post.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    @Override
    public int save(Post post) {
        entityManager.persist(post);
        return post.getId();
    }

    @Override
    public void update(Post post) {
        entityManager.merge(post);
    }

    @Override
    public void delete(int id) {
        Post post = entityManager.find(Post.class, id);
        if (post != null) {
            entityManager.remove(post);
        }
    }

    @Override
    public int countByCategory(String category) {
        String jpql = "SELECT COUNT(p) FROM Post p WHERE p.category = :category";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("category", category);
        return query.getSingleResult().intValue();
    }

    @Override
    public List<Post> findAllOrderByLikesDesc() {
        String jpql = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.replies ORDER BY p.likes DESC, p.id DESC";
        return entityManager.createQuery(jpql, Post.class).getResultList();
    }
}