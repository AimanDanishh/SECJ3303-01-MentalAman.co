package com.secj3303.dao;

import com.secj3303.model.Counsellor;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<Counsellor> findAll() {
        TypedQuery<Counsellor> query = entityManager.createQuery(
                "SELECT c FROM Counsellor c ORDER BY c.name", 
                Counsellor.class);
        return query.getResultList();
    }
}