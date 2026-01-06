-- File: src/main/resources/schema.sql
-- This is a backup SQL script in case Hibernate fails

CREATE DATABASE IF NOT EXISTS digitalmentalhealth 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE digitalmentalhealth;

-- Drop tables if they exist (in reverse order due to foreign keys)
DROP TABLE IF EXISTS forum_reports;
DROP TABLE IF EXISTS forum_replies;
DROP TABLE IF EXISTS forum_posts;
DROP TABLE IF EXISTS forum_categories;

-- Forum categories table
CREATE TABLE IF NOT EXISTS forum_categories (
    id VARCHAR(50) PRIMARY KEY,
    label VARCHAR(100) NOT NULL,
    count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum posts table
CREATE TABLE IF NOT EXISTS forum_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    author VARCHAR(100) DEFAULT 'Anonymous User',
    author_initials VARCHAR(10) DEFAULT 'AU',
    time VARCHAR(50),
    category VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    likes INT DEFAULT 0,
    trending BOOLEAN DEFAULT FALSE,
    helpful BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum replies table
CREATE TABLE IF NOT EXISTS forum_replies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author VARCHAR(100) DEFAULT 'Anonymous User',
    author_initials VARCHAR(10) DEFAULT 'AU',
    time VARCHAR(50),
    content TEXT NOT NULL,
    likes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Forum reports table
CREATE TABLE IF NOT EXISTS forum_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    details TEXT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial categories
INSERT IGNORE INTO forum_categories (id, label, count) VALUES
('all', 'All Posts', 0),
('anxiety', 'Anxiety', 0),
('stress', 'Stress', 0),
('depression', 'Depression', 0),
('motivation', 'Motivation', 0);

-- Insert sample dummy data
INSERT IGNORE INTO forum_posts (author, author_initials, time, category, title, content, likes, trending, helpful) VALUES
('Anonymous Student', 'AS', '2 hours ago', 'anxiety', 'Struggling with exam anxiety', 'Finals are coming up and I''m feeling overwhelmed. Has anyone found helpful strategies for managing exam stress?', 24, TRUE, TRUE),
('Anonymous User', 'AU', '3 hours ago', 'stress', 'EARN $5000 FROM HOME - CLICK HERE NOW!!!', 'Hey everyone! I found this AMAZING opportunity to make money from home. Just click this link and sign up! You can make thousands of dollars working just 2 hours a day! Message me for details. This is NOT a scam, I promise! Limited spots available!!!', 0, FALSE, FALSE),
('Anonymous Faculty', 'AF', '5 hours ago', 'motivation', 'Daily gratitude practice changed my life', 'I wanted to share how starting a simple gratitude journal has helped me maintain a more positive outlook. It only takes 5 minutes each morning!', 45, TRUE, FALSE),
('Anonymous Student', 'AS', '1 day ago', 'stress', 'Need advice on work-life balance', 'Finding it hard to balance studies, part-time work, and personal time. How do you all manage everything?', 18, FALSE, FALSE),
('Anonymous Student', 'AS', '1 day ago', 'depression', 'Feeling isolated - seeking support', 'Been feeling disconnected from friends and struggling to reach out. Anyone else experienced this?', 31, FALSE, TRUE);

-- Insert sample replies
INSERT IGNORE INTO forum_replies (post_id, author, author_initials, time, content, likes) VALUES
(1, 'Anonymous Faculty', 'AF', '1 hour ago', 'I''ve found that creating a study schedule helps reduce anxiety. Break your studying into manageable chunks!', 8),
(1, 'Anonymous Student', 'AS', '30 minutes ago', 'Meditation and deep breathing exercises have really helped me. There are some great apps for this!', 5),
(3, 'Anonymous Student', 'AS', '4 hours ago', 'This is inspiring! How do you stay consistent with it?', 3);