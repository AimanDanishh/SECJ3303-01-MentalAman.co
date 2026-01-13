package com.secj3303.dao;

import com.secj3303.model.Referral;
import java.util.List;

public interface ReferralDao extends GenericDao<Referral> {
    List<Referral> findByStudentId(String studentId);
    List<Referral> findByStatus(String status);
}