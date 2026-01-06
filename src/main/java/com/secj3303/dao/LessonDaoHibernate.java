package com.secj3303.dao;
 
import java.util.Optional;
 
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.secj3303.model.Lesson;
@Repository
@Transactional
public class LessonDaoHibernate implements LessonDao {

    @PersistenceContext
    private EntityManager em;

    public void save(Lesson lesson) {
        if (lesson.getId() == null) {
            em.persist(lesson);
        } else {
            em.merge(lesson);
        }
    }

    public void deleteById(Long id) {
        Lesson lesson = em.find(Lesson.class, id);
        if (lesson != null) em.remove(lesson);
    }

    public Optional<Lesson> findById(Long id) {
        return Optional.ofNullable(em.find(Lesson.class, id));
    }
}
