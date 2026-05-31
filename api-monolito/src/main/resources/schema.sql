-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS tcc_bes_db;

-- Use the database
USE tcc_bes_db;

-- Create usuarios table
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample user for testing
INSERT INTO usuarios (username, password, email, first_name, last_name) 
VALUES ('admin', 'admin123', 'admin@example.com', 'Admin', 'User');
