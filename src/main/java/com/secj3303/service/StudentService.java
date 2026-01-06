package com.secj3303.service;

import com.secj3303.dao.StudentDao;
import com.secj3303.model.Student;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentDao studentDao;
    
    public StudentService(StudentDao studentDao) {
        this.studentDao = studentDao;
    }
    
    public List<Student> getAllStudents() {
        return studentDao.findWithAssessmentCount();
    }
    
    public Optional<Student> getStudentById(Integer id) {
        return studentDao.findById(id);
    }
    
    public Student saveStudent(Student student) {
        return studentDao.save(student);
    }
}