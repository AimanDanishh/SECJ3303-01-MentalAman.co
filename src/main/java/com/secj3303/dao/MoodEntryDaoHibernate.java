package com.secj3303.dao;

import com.secj3303.dao.MoodEntryDao;
import com.secj3303.model.MoodEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class MoodEntryDaoHibernate implements MoodEntryDao {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public MoodEntry save(MoodEntry moodEntry) {
        entityManager.persist(moodEntry);
        return moodEntry;
    }
    
    @Override
    public MoodEntry update(MoodEntry moodEntry) {
        return entityManager.merge(moodEntry);
    }
    
    @Override
    public boolean delete(Integer id) {
        MoodEntry moodEntry = entityManager.find(MoodEntry.class, id);
        if (moodEntry != null) {
            entityManager.remove(moodEntry);
            return true;
        }
        return false;
    }
    
    @Override
    public Optional<MoodEntry> findById(Integer id) {
        MoodEntry moodEntry = entityManager.find(MoodEntry.class, id);
        return Optional.ofNullable(moodEntry);
    }
    
    @Override
    public List<MoodEntry> findByUsername(String username) {
        String jpql = "SELECT m FROM MoodEntry m WHERE m.username = :username ORDER BY m.entryDate DESC";
        TypedQuery<MoodEntry> query = entityManager.createQuery(jpql, MoodEntry.class);
        query.setParameter("username", username);
        return query.getResultList();
    }
    
    @Override
    public List<MoodEntry> findByUsernameAndDateRange(String username, LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT m FROM MoodEntry m WHERE m.username = :username " +
                     "AND m.entryDate BETWEEN :startDate AND :endDate " +
                     "ORDER BY m.entryDate DESC";
        TypedQuery<MoodEntry> query = entityManager.createQuery(jpql, MoodEntry.class);
        query.setParameter("username", username);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public Optional<MoodEntry> findByUsernameAndDate(String username, LocalDate date) {
        String jpql = "SELECT m FROM MoodEntry m WHERE m.username = :username AND m.entryDate = :date";
        TypedQuery<MoodEntry> query = entityManager.createQuery(jpql, MoodEntry.class);
        query.setParameter("username", username);
        query.setParameter("date", date);
        
        List<MoodEntry> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<MoodEntry> findRecentByUsername(String username, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDate endDate = LocalDate.now();
        
        return findByUsernameAndDateRange(username, startDate, endDate);
    }
    
    @Override
    public long countByUsername(String username) {
        String jpql = "SELECT COUNT(m) FROM MoodEntry m WHERE m.username = :username";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("username", username);
        return query.getSingleResult();
    }
    
    @Override
    public boolean existsByUsernameAndDate(String username, LocalDate date) {
        String jpql = "SELECT COUNT(m) FROM MoodEntry m WHERE m.username = :username AND m.entryDate = :date";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("username", username);
        query.setParameter("date", date);
        
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }
}