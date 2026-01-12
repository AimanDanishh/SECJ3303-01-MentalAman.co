package com.secj3303.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class AssessmentDataInitializer {
    
    @Autowired
    private DataSource dataSource;
    
    @EventListener(ContextRefreshedEvent.class)
    public void initializeAssessmentData() {
        System.out.println("=========================================================================");
        System.out.println("=== STARTING ASSESSMENT DATA INITIALIZATION ===");
        System.out.println("=========================================================================");
        
        try {
            // Create JdbcTemplate from DataSource
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Wait a moment for Hibernate to create tables
            Thread.sleep(1500);
            
            // Check if data already exists
            if (isDataAlreadyLoaded(jdbcTemplate)) {
                System.out.println("✅ Assessment data already exists. Skipping initialization.");
                System.out.println("=========================================================================");
                return;
            }
            
            System.out.println("🔄 Loading assessment data programmatically...");
            
            // Load the data
            boolean success = loadAssessmentData(jdbcTemplate);
            
            if (success) {
                System.out.println("✅ ASSESSMENT DATA INITIALIZATION SUCCESSFUL!");
                System.out.println("=========================================================================");
            } else {
                System.err.println("❌ ASSESSMENT DATA INITIALIZATION FAILED!");
                System.out.println("=========================================================================");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in AssessmentDataInitializer: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=========================================================================");
        }
    }
    
    private boolean isDataAlreadyLoaded(JdbcTemplate jdbcTemplate) {
        try {
            // Try to query the assessments table
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessments", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            // Table doesn't exist yet or other error
            System.out.println("🟡 Assessments table check: " + e.getMessage());
            return false;
        }
    }
    
    private boolean loadAssessmentData(JdbcTemplate jdbcTemplate) {
        try {
            System.out.println("🔄 Step 1: Resetting ID sequences...");
            resetIdSequences(jdbcTemplate);
            
            System.out.println("🔄 Step 2: Inserting assessments...");
            insertAssessments(jdbcTemplate);
            
            System.out.println("🔄 Step 3: Inserting depression questions...");
            insertDepressionQuestions(jdbcTemplate);
            
            System.out.println("🔄 Step 4: Inserting anxiety questions...");
            insertAnxietyQuestions(jdbcTemplate);
            
            System.out.println("🔄 Step 5: Inserting stress questions...");
            insertStressQuestions(jdbcTemplate);
            
            System.out.println("🔄 Step 6: Inserting well-being questions...");
            insertWellbeingQuestions(jdbcTemplate);
            
            // Verify the data was inserted
            Integer assessmentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assessments", Integer.class);
            Integer questionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM questions", Integer.class);
            
            System.out.println("📊 Summary:");
            System.out.println("   - Assessments loaded: " + assessmentCount + " (expected: 4)");
            System.out.println("   - Questions loaded: " + questionCount + " (expected: 34)");
            
            return assessmentCount != null && assessmentCount == 4 &&
                   questionCount != null && questionCount == 34;
            
        } catch (Exception e) {
            System.err.println("❌ Error loading assessment data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void resetIdSequences(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE assessments ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE questions ALTER COLUMN id RESTART WITH 1");
            System.out.println("   ✓ ID sequences reset");
        } catch (Exception e) {
            System.err.println("   ⚠️  Could not reset ID sequences: " + e.getMessage());
            // Continue anyway - might not be H2 database
        }
    }
    
    private void insertAssessments(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO assessments (id, title, description, category, duration, color) VALUES " +
            "(1, 'Depression Screening (PHQ-9)', 'Patient Health Questionnaire - 9 item depression screening tool for assessing depression symptoms.', 'Depression', '5-7 minutes', 'blue'), " +
            "(2, 'Anxiety Screening (GAD-7)', 'Generalized Anxiety Disorder - 7 item scale. Assesses anxiety symptoms and severity.', 'Anxiety', '5 minutes', 'purple'), " +
            "(3, 'Stress Assessment (PSS-10)', 'Perceived Stress Scale - 10 item questionnaire. Measures perceived stress levels.', 'Stress', '6-8 minutes', 'orange'), " +
            "(4, 'Well-being Assessment', 'General mental well-being and life satisfaction evaluation.', 'Well-being', '4-5 minutes', 'green')";
        
        jdbcTemplate.execute(sql);
        System.out.println("   ✓ 4 assessments inserted");
    }
    
    private void insertDepressionQuestions(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO questions (id, text, type, scale_max, assessment_id) VALUES " +
            "(1, 'Over the last 2 weeks, how often have you felt little interest or pleasure in doing things?', 'scale', 3, 1), " +
            "(2, 'Over the last 2 weeks, how often have you felt down, depressed, or hopeless?', 'scale', 3, 1), " +
            "(3, 'Over the last 2 weeks, how often have you had trouble falling or staying asleep, or sleeping too much?', 'scale', 3, 1), " +
            "(4, 'Over the last 2 weeks, how often have you felt tired or having little energy?', 'scale', 3, 1), " +
            "(5, 'Over the last 2 weeks, how often have you had poor appetite or overeating?', 'scale', 3, 1), " +
            "(6, 'Over the last 2 weeks, how often have you felt bad about yourself, or that you are a failure, or have let yourself or your family down?', 'scale', 3, 1), " +
            "(7, 'Over the last 2 weeks, how often have you had trouble concentrating on things, such as reading the newspaper or watching television?', 'scale', 3, 1), " +
            "(8, 'Over the last 2 weeks, how often have you been moving or speaking so slowly that other people could have noticed, or the opposite - being so fidgety or restless that you have been moving around a lot more than usual?', 'scale', 3, 1), " +
            "(9, 'Over the last 2 weeks, how often have you had thoughts that you would be better off dead, or of hurting yourself in some way?', 'scale', 3, 1)";
        
        jdbcTemplate.execute(sql);
        System.out.println("   ✓ 9 depression questions inserted");
    }
    
    private void insertAnxietyQuestions(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO questions (id, text, type, scale_max, assessment_id) VALUES " +
            "(10, 'Over the last 2 weeks, how often have you felt nervous, anxious, or on edge?', 'scale', 3, 2), " +
            "(11, 'Over the last 2 weeks, how often have you been unable to stop or control worrying?', 'scale', 3, 2), " +
            "(12, 'Over the last 2 weeks, how often have you been worrying too much about different things?', 'scale', 3, 2), " +
            "(13, 'Over the last 2 weeks, how often have you had trouble relaxing?', 'scale', 3, 2), " +
            "(14, 'Over the last 2 weeks, how often have you been so restless that it is hard to sit still?', 'scale', 3, 2), " +
            "(15, 'Over the last 2 weeks, how often have you become easily annoyed or irritable?', 'scale', 3, 2), " +
            "(16, 'Over the last 2 weeks, how often have you felt afraid as if something awful might happen?', 'scale', 3, 2)";
        
        jdbcTemplate.execute(sql);
        System.out.println("   ✓ 7 anxiety questions inserted");
    }
    
    private void insertStressQuestions(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO questions (id, text, type, scale_max, assessment_id) VALUES " +
            "(17, 'In the last month, how often have you been upset because of something that happened unexpectedly?', 'scale', 4, 3), " +
            "(18, 'In the last month, how often have you felt that you were unable to control the important things in your life?', 'scale', 4, 3), " +
            "(19, 'In the last month, how often have you felt nervous and stressed?', 'scale', 4, 3), " +
            "(20, 'In the last month, how often have you felt confident about your ability to handle your personal problems?', 'scale', 4, 3), " +
            "(21, 'In the last month, how often have you felt that things were going your way?', 'scale', 4, 3), " +
            "(22, 'In the last month, how often have you found that you could not cope with all the things that you had to do?', 'scale', 4, 3), " +
            "(23, 'In the last month, how often have you been able to control irritations in your life?', 'scale', 4, 3), " +
            "(24, 'In the last month, how often have you felt that you were on top of things?', 'scale', 4, 3), " +
            "(25, 'In the last month, how often have you been angered because of things that were outside of your control?', 'scale', 4, 3), " +
            "(26, 'In the last month, how often have you felt difficulties were piling up so high that you could not overcome them?', 'scale', 4, 3)";
        
        jdbcTemplate.execute(sql);
        System.out.println("   ✓ 10 stress questions inserted");
    }
    
    private void insertWellbeingQuestions(JdbcTemplate jdbcTemplate) {
        String sql = "INSERT INTO questions (id, text, type, scale_max, assessment_id) VALUES " +
            "(27, 'In general, how satisfied are you with your life?', 'scale', 5, 4), " +
            "(28, 'How often do you feel happy or content?', 'scale', 5, 4), " +
            "(29, 'How often do you feel that your life has meaning and purpose?', 'scale', 5, 4), " +
            "(30, 'How would you rate your overall psychological well-being?', 'scale', 5, 4), " +
            "(31, 'How often do you feel optimistic about your future?', 'scale', 5, 4), " +
            "(32, 'How often do you feel that you have positive relationships with others?', 'scale', 5, 4), " +
            "(33, 'How often do you feel engaged and interested in your daily activities?', 'scale', 5, 4), " +
            "(34, 'How often do you feel that you are living in accordance with your values?', 'scale', 5, 4)";
        
        jdbcTemplate.execute(sql);
        System.out.println("   ✓ 8 well-being questions inserted");
    }
}