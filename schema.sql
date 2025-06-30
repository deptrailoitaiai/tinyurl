-- ===================================
-- TẠO CÁC DATABASE CHO TỪNG SERVICE
-- ===================================

-- Database cho URL Service
CREATE DATABASE IF NOT EXISTS url_service;

-- Database cho Analytics Batch Service  
CREATE DATABASE IF NOT EXISTS ana_batch_service;

-- Database cho Analytics Realtime Service
CREATE DATABASE IF NOT EXISTS ana_rt_service;

-- Database cho User Management Service
CREATE DATABASE IF NOT EXISTS user_mgmt_service;

-- ===================================
-- URL SERVICE TABLES
-- ===================================

USE url_service;

CREATE TABLE url_urls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    original_url TEXT NOT NULL,
    short_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(500),
    password_hash VARCHAR(255),
    status ENUM('active', 'inactive', 'expired') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    last_clicked_at TIMESTAMP NULL
);

-- ===================================
-- ANALYTICS BATCH SERVICE TABLES
-- ===================================

USE ana_batch_service;

CREATE TABLE ana_batch_url_daily_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    url_id BIGINT NOT NULL,
    date DATE NOT NULL,
    click_count BIGINT DEFAULT 0,
    UNIQUE KEY unique_url_date (url_id, date)
);

-- ===================================
-- ANALYTICS REALTIME SERVICE TABLES
-- ===================================

USE ana_rt_service;

CREATE TABLE ana_rt_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_type ENUM('desktop', 'mobile', 'tablet', 'other') NOT NULL,
    browser_name VARCHAR(100),
    os_name VARCHAR(100)
);

CREATE TABLE ana_rt_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    country_code VARCHAR(2),
    country_name VARCHAR(100),
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ana_rt_click_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    url_daily_stats_id BIGINT,
    ip_address VARCHAR(45),
    referrer VARCHAR(500),
    device_id BIGINT,
    location_id BIGINT,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES ana_rt_devices(id),
    FOREIGN KEY (location_id) REFERENCES ana_rt_locations(id)
);

-- ===================================
-- USER MANAGEMENT SERVICE TABLES
-- ===================================

USE user_mgmt_service;

CREATE TABLE user_mgmt_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200),
    email_verified BOOLEAN DEFAULT FALSE,
    status ENUM('active', 'inactive', 'suspended') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL
);