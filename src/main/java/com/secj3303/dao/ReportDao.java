package com.secj3303.dao;

import com.secj3303.model.Report;

public interface ReportDao {
    int save(Report report);
    boolean existsByPostIdAndReason(int postId, String reason);
}