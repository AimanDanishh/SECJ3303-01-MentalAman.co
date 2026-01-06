package com.secj3303.dao;

import java.util.List;
import java.util.Optional;

<<<<<<< HEAD:src/main/java/com/secj3303/repository/ModuleProgressRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secj3303.model.ModuleProgress;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {
    
    Optional<ModuleProgress> findByUserEmailAndModuleId(String userEmail, Long moduleId);
    
    @Query("SELECT mp FROM ModuleProgress mp WHERE mp.userEmail = :userEmail")
    List<ModuleProgress> findAllByUserEmail(@Param("userEmail") String userEmail);
}
=======
import com.secj3303.model.ModuleProgress;

public interface ModuleProgressDao {

    Optional<ModuleProgress> findByUserEmailAndModuleId(
            String userEmail,
            Long moduleId
    );

    void save(ModuleProgress progress);
}
>>>>>>> 1b3288df7651228c27f52d41c137c2653dfd0932:src/main/java/com/secj3303/dao/ModuleProgressDao.java
