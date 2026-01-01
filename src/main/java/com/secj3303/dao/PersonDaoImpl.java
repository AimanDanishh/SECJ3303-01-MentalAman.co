package com.secj3303.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import com.secj3303.model.Person;

@Repository
@Transactional
public class PersonDaoImpl implements PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Person> findAll() {
        return entityManager
                .createQuery("FROM Person", Person.class)
                .getResultList();
    }

    @Override
    public Person findById(int id) {
        return entityManager.find(Person.class, id);
    }

    @Override
    public int insert(Person person) {
        entityManager.persist(person);
        return 1; // Hibernate manages ID automatically
    }

    @Override
    public void update(Person person) {
        entityManager.merge(person);
    }

    @Override
    public int delete(int id) {
        Person person = entityManager.find(Person.class, id);
        if (person != null) {
            entityManager.remove(person);
            return 1;
        }
        return 0;
    }
}
