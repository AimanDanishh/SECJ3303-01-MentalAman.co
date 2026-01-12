package com.secj3303.dao;

import com.secj3303.model.CarePlan;
import com.secj3303.model.CarePlanActivity;
import java.util.Optional;

public interface CarePlanDao {
    Optional<CarePlan> findByStudentId(Integer studentId);
    CarePlan save(CarePlan carePlan);
    CarePlanActivity findActivityById(Integer id);
    CarePlanActivity updateActivity(CarePlanActivity activity);
}