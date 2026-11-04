CREATE DATABASE IF NOT EXISTS codemate
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE codemate;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    fullname VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    otp INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS time_managements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(200) NULL,
    start_time DATETIME(6) NOT NULL,
    end_time DATETIME(6) NOT NULL,
    duration_seconds INT NOT NULL,
    session_status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_time_user_start (user_id, start_time),
    CONSTRAINT fk_time_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_time_duration CHECK (duration_seconds > 0),
    CONSTRAINT chk_time_status CHECK (session_status IN ('COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS todo_list (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    completed BIT(1) NOT NULL DEFAULT b'0',
    due_date DATE NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'URGENT',
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_todo_user_date (user_id, due_date),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS water_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    log_date DATE NOT NULL,
    total_ml INT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_water_user_day UNIQUE (user_id, log_date),
    CONSTRAINT fk_water_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_water_total CHECK (total_ml >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Du lieu mau cho nguoi dung 15khai09 va 15khai09ok (tu ngay 31/10/2026 den 03/11/2026)
INSERT INTO users (id, username, fullname, password, email) VALUES
(1, '15khai09', 'Nguyen Quang Khai', '$2a$10$w099yYVqNf2YqT/r8oD7Q.VpZzYw1e4H9gZ5G/g0e2w5a/8q2w5a', 'khai@example.com'),
(2, '15khai09ok', 'Nguyễn Quang Khải', '$2a$10$w099yYVqNf2YqT/r8oD7Q.VpZzYw1e4H9gZ5G/g0e2w5a/8q2w5a', 'khai2@example.com')
ON DUPLICATE KEY UPDATE fullname=VALUES(fullname);

INSERT INTO water_log (log_date, total_ml, user_id) VALUES
('2026-10-31', 1750, 1),
('2026-11-01', 2000, 1),
('2026-11-02', 2250, 1),
('2026-11-03', 2750, 1),
('2026-10-31', 1750, 2),
('2026-11-01', 2000, 2),
('2026-11-02', 2250, 2),
('2026-11-03', 2750, 2)
ON DUPLICATE KEY UPDATE total_ml=VALUES(total_ml);

INSERT INTO time_managements (task_name, start_time, end_time, duration_seconds, session_status, user_id) VALUES
('Hoc Giai tich 1', '2026-10-31 08:30:00', '2026-10-31 08:55:00', 1500, 'COMPLETED', 1),
('Luyen tap Java Core', '2026-10-31 10:00:00', '2026-10-31 10:25:00', 1500, 'COMPLETED', 1),
('Lap trinh cau truc du lieu', '2026-11-01 09:00:00', '2026-11-01 09:25:00', 1500, 'COMPLETED', 1),
('Thiet ke giao dien FXML', '2026-11-01 14:00:00', '2026-11-01 14:25:00', 1500, 'COMPLETED', 1),
('Hoc tieng Anh TOEIC', '2026-11-01 16:00:00', '2026-11-01 16:25:00', 1500, 'COMPLETED', 1),
('Lap trinh Hibernate ORM', '2026-11-02 08:30:00', '2026-11-02 08:55:00', 1500, 'COMPLETED', 1),
('Viet bao cao PTDACNTT', '2026-11-02 10:00:00', '2026-11-02 10:25:00', 1500, 'COMPLETED', 1),
('Fix bug co so du lieu', '2026-11-02 14:00:00', '2026-11-02 14:25:00', 1500, 'COMPLETED', 1),
('On tap kiem tra', '2026-11-02 20:00:00', '2026-11-02 20:25:00', 1500, 'COMPLETED', 1),
('Luyen thuat toan LeetCode', '2026-11-03 14:00:00', '2026-11-03 14:25:00', 1500, 'COMPLETED', 1),
('Hoc van', '2026-11-03 16:00:00', '2026-11-03 16:25:00', 1500, 'COMPLETED', 1);
