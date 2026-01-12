package com.secj3303.dao;

import com.secj3303.model.Counsellor;
import java.util.List;
import java.util.Optional;

public interface CounsellorDao {
    void save(Counsellor counsellor);
    void update(Counsellor counsellor);
    void delete(Counsellor counsellor);
    Counsellor findById(String id);
    Counsellor findByEmail(String email);
    List<Counsellor> findAll();
    boolean existsByEmail(String email);
    Optional<Counsellor> findByEmailOptional(String email);
}