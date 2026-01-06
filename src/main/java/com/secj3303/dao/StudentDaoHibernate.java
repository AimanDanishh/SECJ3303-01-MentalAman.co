package com.secj3303.dao;

import com.secj3303.dao.StudentDao;
import com.secj3303.model.Student;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class StudentDaoHibernate implements StudentDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Optional<Student> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Student.class, id));
    }
    
    @Override
    public List<Student> findAll() {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s", Student.class);
        return query.getResultList();
    }
    
    @Override
    @Transactional
    public Student save(Student student) {
        if (student.getId() == null) {
            entityManager.persist(student);
            return student;
        } else {
            return entityManager.merge(student);
        }
    }
    
    @Override
    @Transactional
    public Student update(Student student) {
        return entityManager.merge(student);
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        Student student = entityManager.find(Student.class, id);
        if (student != null) {
            entityManager.remove(student);
        }
    }
    
    @Override
    @Transactional
    public void delete(Student student) {
        entityManager.remove(entityManager.contains(student) ? student : entityManager.merge(student));
    }
    
    @Override
    public Optional<Student> findByEmail(String email) {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s WHERE s.email = :email", Student.class);
        query.setParameter("email", email);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<Student> findByStudentId(String studentId) {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s WHERE s.studentId = :studentId", Student.class);
        query.setParameter("studentId", studentId);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Student> findByRiskLevel(String riskLevel) {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s WHERE s.riskLevel = :riskLevel", Student.class);
        query.setParameter("riskLevel", riskLevel);
        return query.getResultList();
    }
    
    @Override
    public List<Student> findByDepartment(String department) {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s WHERE s.department = :department", Student.class);
        query.setParameter("department", department);
        return query.getResultList();
    }
    
    @Override
    public List<Student> findByYear(String year) {
        TypedQuery<Student> query = entityManager.createQuery(
            "SELECT s FROM Student s WHERE s.year = :year", Student.class);
        query.setParameter("year", year);
        return query.getResultList();
    }
    
    @Override
    public List<Student> findWithAssessmentCount() {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT s, " +
            "(SELECT COUNT(ar) FROM AssessmentResult ar WHERE ar.student = s), " +
            "(SELECT MAX(ar.date) FROM AssessmentResult ar WHERE ar.student = s) " +
            "FROM Student s", Object[].class);
        
        List<Object[]> results = query.getResultList();
        List<Student> students = new java.util.ArrayList<>();
        
        for (Object[] row : results) {
            Student student = (Student) row[0];
            Long assessmentCount = (Long) row[1];
            String lastAssessment = (String) row[2];
            
            student.setAssessmentCount(assessmentCount != null ? assessmentCount.intValue() : 0);
            student.setLastAssessment(lastAssessment);
            students.add(student);
        }
        
        return students;
    }
}