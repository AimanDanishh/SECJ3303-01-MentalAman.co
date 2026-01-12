package com.secj3303.dao;

import com.secj3303.model.Counsellor;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CounsellorDaoHibernate implements CounsellorDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Counsellor counsellor) {
        entityManager.persist(counsellor);
    }

    @Override
    public void update(Counsellor counsellor) {
        entityManager.merge(counsellor);
    }

    @Override
    public void delete(Counsellor counsellor) {
        entityManager.remove(entityManager.contains(counsellor) ? counsellor : entityManager.merge(counsellor));
    }

    @Override
    public Counsellor findById(String id) {
        return entityManager.find(Counsellor.class, id);
    }

    @Override
    public Counsellor findByEmail(String email) {
        try {
            TypedQuery<Counsellor> query = entityManager.createQuery(
                    "SELECT c FROM Counsellor c WHERE c.email = :email", 
                    Counsellor.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Optional<Counsellor> findByEmailOptional(String email) {
        try {
            TypedQuery<Counsellor> query = entityManager.createQuery(
                    "SELECT c FROM Counsellor c WHERE c.email = :email", 
                    Counsellor.class);
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Counsellor c WHERE c.email = :email", 
                Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }

    @Override
    public List<Counsellor> findAll() {
        TypedQuery<Counsellor> query = entityManager.createQuery(
                "SELECT c FROM Counsellor c ORDER BY c.name", 
                Counsellor.class);
        return query.getResultList();
    }

    // Additional useful query methods
    public List<Counsellor> findBySpecialty(String specialty) {
        TypedQuery<Counsellor> query = entityManager.createQuery(
                "SELECT c FROM Counsellor c WHERE c.specialty = :specialty ORDER BY c.name", 
                Counsellor.class);
        query.setParameter("specialty", specialty);
        return query.getResultList();
    }

    public List<Counsellor> findByNameContaining(String name) {
        TypedQuery<Counsellor> query = entityManager.createQuery(
                "SELECT c FROM Counsellor c WHERE LOWER(c.name) LIKE LOWER(:name) ORDER BY c.name", 
                Counsellor.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }
}