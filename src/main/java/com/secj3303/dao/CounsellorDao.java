package com.secj3303.dao;

import com.secj3303.model.Counsellor;
import java.util.List;

public interface CounsellorDao {
    void save(Counsellor counsellor);
    void update(Counsellor counsellor);
    void delete(Counsellor counsellor);
    Counsellor findById(String id);
    List<Counsellor> findAll();
}