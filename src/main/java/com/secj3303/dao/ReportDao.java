package com.secj3303.dao;
import java.util.List;

import com.secj3303.model.Report;

public interface ReportDao {
    int save(Report report);
    boolean existsByPostIdAndReason(int postId, String reason);

    List<Report> findAll();
    void delete(int id);
    void deleteByPostId(int postId);
}