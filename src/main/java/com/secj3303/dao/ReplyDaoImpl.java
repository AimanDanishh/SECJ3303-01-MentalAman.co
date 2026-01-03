package com.secj3303.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.secj3303.model.Reply;

@Repository
@Transactional
public class ReplyDaoImpl implements ReplyDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Reply> findByPostId(int postId) {
        String jpql = "FROM Reply r WHERE r.post.id = :postId ORDER BY r.id ASC";
        TypedQuery<Reply> query = entityManager.createQuery(jpql, Reply.class);
        query.setParameter("postId", postId);
        return query.getResultList();
    }

    @Override
    public Reply findById(int id) {
        return entityManager.find(Reply.class, id);
    }

    @Override
    public int save(Reply reply) {
        entityManager.persist(reply);
        return reply.getId();
    }

    @Override
    public void update(Reply reply) {
        entityManager.merge(reply);
    }

    @Override
    public void delete(int id) {
        Reply reply = entityManager.find(Reply.class, id);
        if (reply != null) {
            entityManager.remove(reply);
        }
    }
}