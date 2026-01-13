package com.secj3303.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.secj3303.dao.PersonDao;
import com.secj3303.dao.StudentDao;
import com.secj3303.dao.CounsellorDao;
import com.secj3303.model.Person;
import com.secj3303.model.Student;
import com.secj3303.model.Counsellor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class DemoDataInitializer {

    private final PersonDao personDao;
    private final StudentDao studentDao;
    private final CounsellorDao counsellorDao;
    private final Random random = new Random();

    public DemoDataInitializer(PersonDao personDao, StudentDao studentDao, CounsellorDao counsellorDao) {
        this.personDao = personDao;
        this.studentDao = studentDao;
        this.counsellorDao = counsellorDao;
    }
    
    @EventListener(ContextRefreshedEvent.class)
    public void init() {

        createPersonIfNotExists("student1@demo.com", "John Smith", "STUDENT");
        createPersonIfNotExists("student2@demo.com", "Sarah Johnson", "STUDENT");
        createPersonIfNotExists("faculty@demo.com", "Demo Faculty", "FACULTY");
        createPersonIfNotExists("counsellor1@demo.com", "Dr. Emily Chen", "COUNSELLOR");
        createPersonIfNotExists("counsellor2@demo.com", "Prof. Michael Wong", "COUNSELLOR");
        createPersonIfNotExists("admin@demo.com", "System Admin", "ADMINISTRATOR");

        createRoleSpecificRecords();
    }

    private void createPersonIfNotExists(String email, String name, String role) {
        Person existing = personDao.findByEmail(email);
        if (existing != null) {
            return;
        }

        Person person = new Person();
        person.setEmail(email);
        person.setName(name);
        person.setRole(role);
        person.setPassword("{noop}demo123"); 
        person.setEnabled(true);
        person.setMatrixId("A" + System.currentTimeMillis() % 1000000);

        person.setYob(2000);
        person.setWeight(65.0);
        person.setHeight(1.70);

        personDao.insert(person);
    }
    
    private void createRoleSpecificRecords() {
        createStudentRecord("student1@demo.com", "Computer Science", "Year 3");
        createStudentRecord("student2@demo.com", "Psychology", "Year 2");
        
        createCounsellorRecord("counsellor1@demo.com", "Academic Counseling");
        createCounsellorRecord("counsellor2@demo.com", "Mental Health");
    }
    
    private void createStudentRecord(String email, String department, String year) {
        if (studentDao.findByEmail(email).isPresent()) {
            return;
        }
        
        Person person = personDao.findByEmail(email);
        if (person == null || !"STUDENT".equals(person.getRole())) {
            return;
        }

        Student student = new Student();
        student.setName(person.getName());
        student.setEmail(email);
        student.setStudentId(generateStudentId());
        student.setDepartment(department);
        student.setYear(year);
        student.setCurrentGrade(getRandomGrade());
        student.setAttendance(70 + random.nextInt(31)); // 70-100%
        student.setLastActivity(getRandomDate());
        student.setRiskLevel("mild");
        
        student.calculateInitials();
        
        studentDao.save(student);
        System.out.println("Student record created for: " + email);
    }
    
    private void createCounsellorRecord(String email, String specialty) {
        if (counsellorDao.existsByEmail(email)) {
            return;
        }
        
        Person person = personDao.findByEmail(email);
        if (person == null || !"COUNSELLOR".equals(person.getRole())) {
            return;
        }
        
        String code = "CSLR" + String.format("%03d", random.nextInt(1000));
        Counsellor counsellor = new Counsellor(
            person.getName(),
            email,
            specialty,
            code
        );
        
        counsellorDao.save(counsellor);
        System.out.println("Counsellor record created for: " + email + " with ID: " + counsellor.getId());
    }
    
    // Helper methods for generating demo data
    private String generateStudentId() {
        return "STU" + (20000 + random.nextInt(30000));
    }
    
    private String getRandomGrade() {
        String[] grades = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};
        return grades[random.nextInt(grades.length)];
    }
    
    private String getRandomDate() {
        LocalDate date = LocalDate.now().minusDays(random.nextInt(30));
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}