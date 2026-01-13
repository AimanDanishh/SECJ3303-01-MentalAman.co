package com.secj3303.dao;

import com.secj3303.model.CarePlan;
import com.secj3303.model.CarePlanActivity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Optional;

@Repository
@Transactional
public class CarePlanDaoHibernate implements CarePlanDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CarePlan> findByStudentId(Integer studentId) {
        try {
            TypedQuery<CarePlan> query = entityManager.createQuery(
                "SELECT cp FROM CarePlan cp WHERE cp.student.id = :studentId", CarePlan.class);
            query.setParameter("studentId", studentId);
            return Optional.ofNullable(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public CarePlan save(CarePlan carePlan) {
        if (carePlan.getId() == null) {
            entityManager.persist(carePlan);
            return carePlan;
        } else {
            return entityManager.merge(carePlan);
        }
    }

    @Override
    public CarePlanActivity findActivityById(Integer id) {
        return entityManager.find(CarePlanActivity.class, id);
    }

    @Override
    public CarePlanActivity updateActivity(CarePlanActivity activity) {
        return entityManager.merge(activity);
    }
}